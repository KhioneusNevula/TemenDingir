package com.gm910.temendingir.world.temperature;

import java.util.function.Predicate;

import net.minecraft.util.CachedBlockInfo;
import net.minecraftforge.eventbus.api.Event;

public class TemperatureMonitoringRegistrationEvent extends Event {

	public TemperatureMonitoringRegistrationEvent() {
	}

	public void registerTemperatureMonitor(Predicate<CachedBlockInfo> checker) {
		HeatFunctionHandler.SHOULD_MONITOR_BLOCK.add(checker);
	}

}
