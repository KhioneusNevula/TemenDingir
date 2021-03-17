package com.gm910.temendingir.world.temperature;

import java.util.function.Function;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.CachedBlockInfo;
import net.minecraftforge.eventbus.api.Event;

public class HeatRateRegistrationEvent extends Event {

	public HeatRateRegistrationEvent() {
	}

	public void registerBlockHeatRate(Block block, Float value) {
		HeatRateHandler.HEAT_RATE_BLOCK_MAP.put(block, value);
	}

	public void registerMaterialHeatRate(Material material, Float value) {
		HeatRateHandler.HEAT_RATE_MATERIAL_MAP.put(material, value);
	}

	public void registerSpecificHeatRateFunction(Function<CachedBlockInfo, Float> function) {
		HeatRateHandler.HEAT_RATE_SPECIFIC_BLOCK_FUNCTIONS.add(function);
	}

}
