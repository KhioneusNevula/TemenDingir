package com.gm910.temendingir.world.gods.cap;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.gm910.temendingir.TemenDingir;
import com.gm910.temendingir.api.networking.messages.ModTask;
import com.gm910.temendingir.api.networking.messages.Networking;
import com.gm910.temendingir.api.networking.messages.types.TaskSyncCapability;
import com.gm910.temendingir.api.util.GMNBT;
import com.gm910.temendingir.capabilities.GMCaps;
import com.gm910.temendingir.capabilities.IModCapability;
import com.gm910.temendingir.world.gods.Deity;
import com.gm910.temendingir.world.gods.Deity.HierarchicPosition;
import com.gm910.temendingir.world.gods.Deity.NamingConvention;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Data about a player that is read-only on client and read/manipulated on
 * server side. This capability will get synchronized whenever "update" is
 * called
 */
public class SyncingWorshiperData implements IModCapability<LivingEntity> {

	public static final ResourceLocation NAME = new ResourceLocation(TemenDingir.MODID, "synchronized_worshiper_data");

	private static Set<SyncingWorshiperData> datas = new HashSet<>();

	private static Thread syncThread;

	private LivingEntity owner;

	private boolean initialized = false;

	private UUID deityUUID;
	private String deityName;
	private NamingConvention naming;
	private HierarchicPosition position;
	private Set<BlockPos> consecrationBorders = new HashSet<>();

	public SyncingWorshiperData() {
		MinecraftForge.EVENT_BUS.register(this);
		synchronized (datas) {
			datas.add(this);
		}
	}

	@SubscribeEvent
	public void livUp(PlayerTickEvent event) {
		if (event.player != this.owner)
			return;
		if (get(event.player) != this) {
			MinecraftForge.EVENT_BUS.unregister(this);
			return;
		}
		if (!event.player.world.isRemote) {

			if (syncThread == null || !syncThread.isAlive()) {
				MinecraftServer server = event.player.getServer();
				syncThread = new Thread(() -> {
					while (server.isServerRunning()) {//TODO maybe sync regular entity data at some point?
						if (server.getServerTime() % 20 == 0) {
							Set<SyncingWorshiperData> dats;
							synchronized (datas) {
								dats = new HashSet<>(datas);
								dats.removeIf((a) -> !(a.owner instanceof PlayerEntity));
							}
							for (SyncingWorshiperData dat : dats) {
								dat.update(true);
							}

						}
					}
				}, "syncWorshiperCap");
				syncThread.start();
			}
		}
	}

	public String getDeityName() {
		return deityName;
	}

	public NamingConvention getNaming() {
		return naming;
	}

	/**
	 * updates data (Server side only)
	 * 
	 * @param synchronize whether to send a synchronize packet
	 */
	public void update(boolean synchronize) {
		if (!owner.isServerWorld())
			throw new IllegalStateException("Tried to update deity capability for "
					+ (owner.getDisplayName() == null ? owner : owner.getDisplayName().getString()) + "["
					+ owner.getUniqueID() + "] from client side");
		// TODO synchronized worshiper data
		if (owner.getServer() == null)
			return;
		Deity invoked = DeityData.get(owner.getServer()).getFromFollowerUUID(owner.getUniqueID());
		if (invoked != null) {
			this.deityUUID = invoked.getUuid();
			this.deityName = invoked.getName();
			this.naming = invoked.getNaming();
			this.consecrationBorders = invoked.getConsecratedOutline().get(owner.world.getDimensionKey());

			initialized = true;
		}
		if (synchronize) {
			ModTask syncer = new TaskSyncCapability("SYNCHRONIZED_WORSHIPER_DATA", null, owner);
			if (!(owner instanceof ServerPlayerEntity)) {
				Networking.sendToTracking(syncer, owner);
			} else {
				Networking.sendToTrackingAndSelf(syncer, (ServerPlayerEntity) owner);
			}
		}
	}

	public boolean isDevotee() {
		return getDeityUUID() != null;
	}

	public UUID getDeityUUID() {
		return deityUUID;
	}

	public HierarchicPosition getPosition() {
		return position;
	}

	public Set<BlockPos> getConsecrationBorders() {
		return consecrationBorders;
	}

	public boolean isClientSide() {
		return !isServerSide();
	}

	public boolean isServerSide() {
		return owner.isServerWorld();
	}

	public boolean hasBeenInitialized() {
		return initialized;
	}

	@Override
	public LivingEntity $getOwner() {
		return owner;
	}

	@Override
	public void $setOwner(LivingEntity e) {
		owner = e;
	}

	public static SyncingWorshiperData get(LivingEntity entity) {
		return entity.getCapability(GMCaps.SYNCHRONIZED_WORSHIPER_DATA).orElse(null);
	}

	public CompoundNBT writeToNBT() {
		CompoundNBT nbt = new CompoundNBT();
		// TODO nbt for synch cap

		if (deityUUID != null)
			nbt.putUniqueId("Deity", this.deityUUID);
		if (deityName != null)
			nbt.putString("DeityName", this.deityName);
		nbt.put("ConsecrationOutline", GMNBT.makePosList(this.consecrationBorders));
		if (naming != null)
			nbt.putString("Naming", this.naming.toString());
		if (this.isServerSide()) {
			nbt.putBoolean("Initialized", initialized);
		}
		return nbt;
	}

	public void readFromNBT(CompoundNBT nbt) {
		//TODO read nbt for synch cap
		if (nbt.hasUniqueId("Deity"))
			this.deityUUID = nbt.getUniqueId("Deity");
		if (nbt.contains("DeityName"))
			this.deityName = nbt.getString("DeityName");
		this.consecrationBorders = new HashSet<>(GMNBT.createPosList(GMNBT.getList(nbt, "ConsecrationOutline")));
		if (nbt.contains("Naming"))
			this.naming = NamingConvention.from(nbt.getString("Naming"));

		if (this.isClientSide() && nbt.getBoolean("Initialized")) {
			this.initialized = true;
		}

	}

}
