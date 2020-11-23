package com.gm910.temendingir.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DivineWallBlock extends ModBlock {

	public DivineWallBlock() {
		super(Properties.create(Material.BARRIER).noDrops().setLightLevel((e) -> 15));
	}

	@Override
	public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player,
			boolean willHarvest, FluidState fluid) {
		return super.removedByPlayer(state, world, pos, player, willHarvest, fluid);
	}

}
