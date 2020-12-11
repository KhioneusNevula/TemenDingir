package com.gm910.temendingir.blocks.tile;

import com.gm910.temendingir.api.util.BlockInfo;

import net.minecraft.util.math.BlockPos;

public interface ILinkTileNexus {

	public void addLinkedPosition(BlockPos pos);

	public void removeLinkedPosition(BlockPos pos);

	public boolean canContinueLinking();

	public boolean canLinkTo(BlockInfo tile);
}
