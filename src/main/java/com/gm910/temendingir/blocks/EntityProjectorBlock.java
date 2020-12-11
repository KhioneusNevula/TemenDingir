package com.gm910.temendingir.blocks;

import com.gm910.temendingir.blocks.tile.EntityProjectorTile;
import com.gm910.temendingir.init.TileInit;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.material.Material;

public class EntityProjectorBlock extends ModBlock {
	public EntityProjectorBlock() {
		super(Block.Properties.create(Material.ROCK).notSolid());
		this.addTileEntity(TileInit.ENTITY_PROJECTOR::get, (e, f) -> true);
	}

	@Override
	public net.minecraft.util.ActionResultType onBlockActivated(net.minecraft.block.BlockState state,
			net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos,
			net.minecraft.entity.player.PlayerEntity player, net.minecraft.util.Hand handIn,
			net.minecraft.util.math.BlockRayTraceResult hit) {
		if (worldIn.isRemote())
			return super.onBlockActivated(state, worldIn, pos, player, handIn, hit);
		return ((EntityProjectorTile) worldIn.getTileEntity(pos)).rightClicked(player, handIn,
				player.getHeldItem(handIn));
	}

	@Override
	public net.minecraft.block.BlockRenderType getRenderType(net.minecraft.block.BlockState state) {

		return BlockRenderType.INVISIBLE;
	}
}