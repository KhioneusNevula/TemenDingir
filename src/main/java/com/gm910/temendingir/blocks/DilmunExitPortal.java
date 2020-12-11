package com.gm910.temendingir.blocks;

import java.util.Random;

import com.gm910.temendingir.api.util.ServerPos;
import com.gm910.temendingir.world.gods.Deity;
import com.gm910.temendingir.world.gods.cap.DeityData;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class DilmunExitPortal extends ModBlock {

	public DilmunExitPortal() {
		super(Block.Properties.create(Material.PORTAL).doesNotBlockMovement().tickRandomly()
				.hardnessAndResistance(-1, 3600000).sound(SoundType.GLASS).noDrops().notSolid()
				.setLightLevel((s) -> 10));

	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return VoxelShapes.empty();
	}

	@Override
	public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		super.animateTick(stateIn, worldIn, pos, rand);
		for (float x = 0; x <= 1; x += 0.1) {
			for (float y = 0; y <= 1; y += 0.1) {
				for (float z = 0; z <= 1; z += 0.1) {
					if (rand.nextInt(10) >= 3)
						continue;
					worldIn.addParticle(new RedstoneParticleData(x, y, z, rand.nextFloat()), pos.getX() + x,
							pos.getY() + y, pos.getZ() + z, rand.nextFloat() * 0.1 - 0.05,
							rand.nextFloat() * 0.1 - 0.05, rand.nextFloat() * 0.1 - 0.05);
					// TODO no visible particles...
				}
			}
		}
	}

	@Override
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {

		super.onEntityCollision(state, worldIn, pos, entityIn);
		if (!(worldIn instanceof ServerWorld))
			return;
		ServerWorld world = (ServerWorld) worldIn;
		Deity d = DeityData.get(world.getServer()).getFromPositionWithinDilmun(pos);
		if (d != null) {
			if (!d.getExtraEntityInfo(entityIn.getUniqueID()).getBoolean("InDilmunPortal")) {
				ServerPos former = ServerPos
						.fromNBT(d.getExtraEntityInfo(entityIn.getUniqueID()).getCompound("PositionBeforeDilmun"));
				if (former == null) {
					World over = world.getServer().getWorld(World.OVERWORLD);
					BlockPos spawn = new BlockPos(over.getWorldInfo().getSpawnX(), over.getWorldInfo().getSpawnY(),
							over.getWorldInfo().getSpawnZ());
					former = new ServerPos(spawn, over);
				}
				// TODO totally buggy dimensionalportation
				entityIn.setPosition(former.getX(), former.getY(), former.getZ());
				entityIn.changeDimension(former.getWorld(d.getData().getServer()));
				d.getExtraEntityInfo(entityIn.getUniqueID()).remove("PositionBeforeDilmun");
			}
		}
	}

}
