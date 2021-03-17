package com.gm910.temendingir.capabilities;

import com.gm910.temendingir.world.gods.cap.DeityData;
import com.gm910.temendingir.world.gods.cap.DeityStorage;
import com.gm910.temendingir.world.gods.cap.SynchronizedWorshipStorage;
import com.gm910.temendingir.world.gods.cap.SyncingWorshiperData;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class GMCaps<T, K> implements ICapabilitySerializable<CompoundNBT> {

	@CapabilityInject(DeityData.class)
	public static Capability<DeityData> DEITY_DATA = null;
	@CapabilityInject(SyncingWorshiperData.class)
	public static Capability<SyncingWorshiperData> SYNCHRONIZED_WORSHIPER_DATA = null;

	/*@CapabilityInject(Temperatures.class)
	public static Capability<Temperatures> TEMPERATURES = null;*/

	private Capability<T> capability;

	private K owner;

	private T instance;

	public GMCaps(Capability<T> capability, K owner) {
		this.capability = capability;
		this.owner = owner;
		this.instance = capability.getDefaultInstance();
		if (instance instanceof IModCapability) {
			((IModCapability<K>) instance).$setOwner(owner);
		}
	}

	public K getOwner() {
		return owner;
	}

	public T getInstance() {

		return instance;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		return this.capability.orEmpty(cap, LazyOptional.of(() -> instance));
	}

	@Override
	public CompoundNBT serializeNBT() {
		return (CompoundNBT) this.capability.getStorage().writeNBT(capability, instance, null);
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		this.capability.getStorage().readNBT(capability, instance, null, nbt);
	}

	public static void preInit() {

		CapabilityManager.INSTANCE.register(DeityData.class, new DeityStorage(), () -> {
			return new DeityData();
		});
		CapabilityManager.INSTANCE.register(SyncingWorshiperData.class, new SynchronizedWorshipStorage(), () -> {
			return new SyncingWorshiperData();
		});

		/*CapabilityManager.INSTANCE.register(Temperatures.class, new TemperatureStorage(), () -> {
			return new Temperatures();
		});*/
	}

	@SubscribeEvent
	public static void attachEn(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof LivingEntity) {
			event.addCapability(SyncingWorshiperData.NAME,
					new GMCaps<>(SYNCHRONIZED_WORSHIPER_DATA, event.getObject()));
		}
	}

	@SubscribeEvent
	public static void clone(PlayerEvent.Clone event) {
		SyncingWorshiperData dat = event.getPlayer().getCapability(SYNCHRONIZED_WORSHIPER_DATA).orElse(null);
		dat.readFromNBT(event.getOriginal().getCapability(SYNCHRONIZED_WORSHIPER_DATA).orElse(null).writeToNBT());
	}

	@SubscribeEvent
	public static void attachW(AttachCapabilitiesEvent<World> event) {
		if (!event.getObject().isRemote && event.getObject().getDimensionKey().equals(World.OVERWORLD)) {
			event.addCapability(DeityData.NAME, new GMCaps<>(DEITY_DATA, event.getObject().getServer()));

		}

	}

	@SubscribeEvent
	public static void attachCh(AttachCapabilitiesEvent<Chunk> event) {
		/*if (!event.getObject().getWorld().isRemote) {
			event.addCapability(Temperatures.NAME, new GMCaps<>(TEMPERATURES, event.getObject()));
		}*/
	}

}
