package com.gm910.temendingir.world.temperature;

import com.gm910.temendingir.capabilities.IModCapability;

import net.minecraft.tileentity.TileEntity;

public class HeatProvider implements IModCapability<TileEntity> {

	private TileEntity tile;

	private float generatedHeat = 0f;

	private float heatRate = 1f;

	private float catchFireAt = 100f;

	public HeatProvider() {
		// TODO fill in heat provider class
	}

	public float getGeneratedHeat() {
		return generatedHeat;
	}

	public void setGeneratedHeat(float generatedHeat) {
		this.generatedHeat = generatedHeat;
	}

	public float getHeatRate() {
		return heatRate;
	}

	public void setHeatRate(float heatRate) {
		this.heatRate = heatRate;
	}

	public float getCatchFireAt() {
		return catchFireAt;
	}

	public void setCatchFireAt(float catchFireAt) {
		this.catchFireAt = catchFireAt;
	}

	@Override
	public TileEntity $getOwner() {
		return tile;
	}

	@Override
	public void $setOwner(TileEntity e) {
		tile = e;
	}

}
