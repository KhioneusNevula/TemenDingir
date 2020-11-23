package com.gm910.temendingir.world.gods;

import com.gm910.temendingir.api.util.ServerPos;

import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class DeityCreationEvent extends Event {

	public final Deity deity;

	public final ServerPos pos;

	public DeityCreationEvent(Deity deity, ServerPos pos) {
		this.deity = deity;
		this.pos = pos;
	}
}
