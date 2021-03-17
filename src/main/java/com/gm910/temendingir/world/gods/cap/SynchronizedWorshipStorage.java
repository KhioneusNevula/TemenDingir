package com.gm910.temendingir.world.gods.cap;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class SynchronizedWorshipStorage implements IStorage<SyncingWorshiperData> {

	@Override
	public INBT writeNBT(Capability<SyncingWorshiperData> capability, SyncingWorshiperData instance,
			Direction side) {
		return instance.writeToNBT();
	}

	@Override
	public void readNBT(Capability<SyncingWorshiperData> capability, SyncingWorshiperData instance,
			Direction side, INBT nbt) {
		instance.readFromNBT((CompoundNBT) nbt);
	}

}
