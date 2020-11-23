package com.gm910.temendingir.blocks;

import com.gm910.temendingir.blocks.tile.PrimordialWater;
import com.gm910.temendingir.init.TileInit;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.material.Material;
import net.minecraft.pathfinding.PathType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.Explosion;
import net.minecraft.world.ExplosionContext;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PrimordialWaterBlock extends ModBlock {

	public PrimordialWaterBlock() {
		super(Properties.create(Material.MISCELLANEOUS).doesNotBlockMovement().zeroHardnessAndResistance()
				.sound(SoundType.HONEY).setLightLevel((e) -> 10).setOpaque((a, b, c) -> false)
				.setSuffocates((a, b, c) -> false).setBlocksVision((a, b, c) -> false).notSolid());
		this.addTileEntity(TileInit.PRIMORDIAL_WATER::get, (e, f) -> true);

	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public float getAmbientOcclusionLightValue(BlockState state, IBlockReader worldIn, BlockPos pos) {
		return 1.0F;
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
		return true;
	}

	@Override
	public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
		return type == PathType.AIR && !this.canCollide ? true : super.allowsMovement(state, worldIn, pos, type);
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		PrimordialWater te = (PrimordialWater) worldIn.getTileEntity(pos);
		if (te.getEmbryo() != null) {
			worldIn.createExplosion(null, DamageSource.MAGIC, (ExplosionContext) null, pos.getX(), pos.getY(),
					pos.getZ(), 2, false, Explosion.Mode.DESTROY);
		}
		super.onReplaced(state, worldIn, pos, newState, isMoving);
	}

	@Override
	public boolean isTransparent(BlockState state) {
		return true;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return VoxelShapes.empty();
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return super.getRenderType(state);
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				if (Math.abs(x) == Math.abs(z))
					continue;
				BlockPos pos2 = pos.add(x, 0, z);
				BlockPos diff = pos.subtract(pos2);
				Direction dir = Direction.getFacingFromVector(diff.getX(), 0, diff.getZ());
				BlockState state2 = worldIn.getBlockState(pos2);
				if (!(state2.getBlock() instanceof StairsBlock) || state2.get(StairsBlock.FACING) != dir) {
					worldIn.setBlockState(pos, Blocks.WATER.getDefaultState());
					break;
				}
			}
		}
	}

}