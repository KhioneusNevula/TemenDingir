package com.gm910.temendingir.world.gods.event;

import com.gm910.temendingir.api.util.ServerPos;
import com.gm910.temendingir.world.gods.Deity;

import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class DeityCreationEvent extends DeityEvent {

	public final ServerPos pos;

	public DeityCreationEvent(Deity deity, ServerPos pos) {
		super(deity);
		this.pos = pos;
	}
}
