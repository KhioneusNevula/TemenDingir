package com.gm910.temendingir.world.gods.event;

import com.gm910.temendingir.world.gods.Deity;

import net.minecraftforge.eventbus.api.Event;

public abstract class DeityEvent extends Event {

	private final Deity deity;

	public DeityEvent(Deity deity) {
		this.deity = deity;
	}

	public Deity getDeity() {
		return deity;
	}

}
