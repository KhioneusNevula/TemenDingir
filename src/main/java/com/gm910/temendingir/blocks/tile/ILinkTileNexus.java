package com.gm910.temendingir.blocks.tile;

import com.gm910.temendingir.api.util.BlockInfo;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public interface ILinkTileNexus {

	public void addLinkedPosition(BlockPos pos, PlayerEntity player, Hand hand);

	public void removeLinkedPosition(BlockPos pos, PlayerEntity player, Hand hand);

	public boolean canContinueLinking();

	public boolean canLinkTo(BlockInfo tile);
}
