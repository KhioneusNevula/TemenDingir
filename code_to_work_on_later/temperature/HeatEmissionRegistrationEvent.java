package com.gm910.temendingir.world.temperature;

import java.util.function.Function;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.CachedBlockInfo;
import net.minecraftforge.eventbus.api.Event;

public class HeatEmissionRegistrationEvent extends Event {

	public HeatEmissionRegistrationEvent() {
	}

	public void registerBlockHeatRate(Block block, Float value) {
		HeatEmitterHandler.HEAT_EMISSION_BLOCK_MAP.put(block, value);
	}

	public void registerMaterialHeatRate(Material material, Float value) {
		HeatEmitterHandler.HEAT_EMISSION_MATERIAL_MAP.put(material, value);
	}

	public void registerSpecificHeatRateFunction(Function<CachedBlockInfo, Float> function) {
		HeatEmitterHandler.HEAT_EMISSION_SPECIFIC_BLOCK_FUNCTIONS.add(function);
	}

}
