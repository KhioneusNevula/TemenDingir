package com.gm910.temendingir.entities;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class DilmunItemFrameClickedEvent extends PlayerEvent {

	public final DilmunItemFrameEntity frame;

	public DilmunItemFrameClickedEvent(PlayerEntity player, DilmunItemFrameEntity frame) {
		super(player);
		this.frame = frame;
	}

}
