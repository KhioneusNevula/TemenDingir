package com.gm910.temendingir.world.gods.cap;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class DeityStorage implements IStorage<DeityData> {

	@Override
	public INBT writeNBT(Capability<DeityData> capability, DeityData instance, Direction side) {
		return instance.write(new CompoundNBT());
	}

	@Override
	public void readNBT(Capability<DeityData> capability, DeityData instance, Direction side, INBT nbt) {
		instance.read((CompoundNBT) nbt);
	}

}
