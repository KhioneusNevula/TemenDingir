package com.gm910.temendingir.blocks;

import com.gm910.temendingir.blocks.tile.AltarOfConsecration;
import com.gm910.temendingir.init.TileInit;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class AltarOfConsecrationBlock extends ModBlock {

	public AltarOfConsecrationBlock() {
		super(Properties.create(Material.ROCK).setLightLevel((e) -> 10));
		this.addTileEntity(TileInit.ALTAR_OF_CONSECRATION::get, (e, f) -> true);

	}

	@Override
	public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, Direction side) {

		return true;
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		AltarOfConsecration altar = (AltarOfConsecration) worldIn.getTileEntity(pos);
		boolean prev = altar.isActivated;
		altar.isActivated = worldIn.isBlockPowered(pos);
		if (prev != altar.isActivated && !worldIn.isRemote) {
			altar.updateHolyLandState();
		}

	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		AltarOfConsecration tile = (AltarOfConsecration) worldIn.getTileEntity(pos);
		tile.clearLinkedPositions();
		super.onReplaced(state, worldIn, pos, newState, isMoving);
	}

}