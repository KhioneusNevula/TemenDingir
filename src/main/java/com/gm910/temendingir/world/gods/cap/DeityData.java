package com.gm910.temendingir.world.gods.cap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.gm910.temendingir.TemenDingir;
import com.gm910.temendingir.api.language.Translate;
import com.gm910.temendingir.api.util.GMNBT;
import com.gm910.temendingir.api.util.GMWorld;
import com.gm910.temendingir.api.util.ServerPos;
import com.gm910.temendingir.capabilities.GMCaps;
import com.gm910.temendingir.capabilities.IModCapability;
import com.gm910.temendingir.world.gods.Deity;
import com.gm910.temendingir.world.gods.DeityCreationEvent;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;

public class DeityData implements IModCapability<MinecraftServer> {

	public static final ResourceLocation NAME = new ResourceLocation(TemenDingir.MODID, "deity");
	private MinecraftServer server;
	private Set<Deity> deities = new HashSet<>();

	public DeityData() {
	}

	public void setServer(MinecraftServer server) {
		this.server = server;
	}

	public static DeityData get(MinecraftServer server) {
		ServerWorld manager = server.getWorld(World.OVERWORLD);

		DeityData gotten = manager.getCapability(GMCaps.DEITY_DATA).orElse(null);

		return gotten;
	}

	public MinecraftServer getServer() {
		return server;
	}

	public Set<Deity> getDeities() {
		return new HashSet<>(deities);
	}

	public Deity getFromUUID(UUID id) {
		return deities.stream().filter((e) -> e.getUuid().equals(id)).findAny().orElse(null);
	}

	public Deity getFromFollowerUUID(UUID id) {
		return deities.stream().filter((e) -> e.getFollowerIDs().contains(id)).findAny().orElse(null);
	}

	public Deity getFromDilmunChunk(ChunkPos pos) {
		return deities.stream().filter((e) -> e.getDilmunChunk() != null && e.getDilmunChunk().equals(pos)).findAny()
				.orElse(null);
	}

	public Deity getFromPositionWithinDilmun(BlockPos pos) {
		return deities.stream().filter((e) -> e.getDilmunChunk() != null && e.isInDilmun(pos)).findAny().orElse(null);
	}

	public Deity getFromItems(ItemStack... stacks) {
		List<Item> itemslist = new ArrayList<>();
		for (ItemStack stack : stacks) {
			List<Item> items = new ArrayList<>();
			for (int i = 0; i < stack.getCount(); i++)
				items.add(stack.getItem());
			itemslist.addAll(items);
		}
		return getFromItems(itemslist.toArray(new Item[0]));
	}

	public Deity getFromItems(Item... items) {
		return deities.stream().filter((e) -> e.getInvocation().match(items)).findAny().orElse(null);
	}

	public void addDeity(Deity d) {
		this.deities.add(d);
		d.setData(this);
		MinecraftForge.EVENT_BUS.register(d);
	}

	/**
	 * Does all the stuff with sparkles, explosions, text chat, etc for deity
	 * creation
	 * 
	 * @param deity
	 * @param pos
	 * @param world
	 */
	public void birthDeity(Deity deity, ServerPos pos) {
		if (MinecraftForge.EVENT_BUS.post(new DeityCreationEvent(deity, pos))) {
			System.out.println("Creation of " + deity + " at " + pos + " canceled");
			return;
		}
		deity.pledgeEntityToDeity((LivingEntity) ServerPos.getEntityFromUUID(deity.getCreator(), server));
		if (deity.isFollower(deity.getCreator())) {
			deity.setFollowerFavor(deity.getCreator(), Deity.FAVOR_MAX);
		}
		this.getServer().getPlayerList().getPlayers()
				.forEach((e) -> deity.sendMessageTo(e, Translate.make("deity.created", deity.getName())));
		ServerWorld world = pos.getWorld(server);
		GMWorld.summonLightning(null, world, pos, null).setEffectOnly(true);
		this.addDeity(deity);

	}

	public ServerWorld getWorld(ServerPos pos) {
		return pos.getWorld(this.server);
	}

	public void removeDeity(Deity d) {
		this.deities.remove(d);
		MinecraftForge.EVENT_BUS.unregister(d);
	}

	public void read(CompoundNBT nbt) {
		deities.clear();
		System.out.println("Loading deity data " + nbt);
		deities.addAll(GMNBT.createList((ListNBT) nbt.get("Deities"), (inbt) -> {
			CompoundNBT tag = (CompoundNBT) inbt;
			System.out.println("Loading deity from " + tag);
			Deity creado = new Deity(tag);
			creado.setData(this);
			return creado;
		}));
		System.out.println("Loaded deity data " + deities);

	}

	public CompoundNBT write(CompoundNBT compound) {
		compound.put("Deities", GMNBT.makeList(deities, (deity) -> {
			return deity.serialize();
		}));
		System.out.println(
				"Saving deity data for " + deities.stream().map((e) -> e.getName()).collect(Collectors.toSet()));
		return compound;
	}

	@Override
	public MinecraftServer $getOwner() {
		return server;
	}

	@Override
	public void $setOwner(MinecraftServer e) {
		server = e;
	}

}
