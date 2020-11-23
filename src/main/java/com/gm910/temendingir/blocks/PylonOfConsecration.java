package com.gm910.temendingir.blocks;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PylonOfConsecration extends ModBlock {
	public static final BooleanProperty LINKED = BooleanProperty.create("linked");

	public PylonOfConsecration() {
		super(Properties.create(Material.ROCK).setLightLevel((e) -> e.get(LINKED) ? 15 : 2));
		this.setDefaultState(this.stateContainer.getBaseState().with(LINKED, Boolean.valueOf(false)));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		if (stateIn.get(LINKED)) {
			double d0 = pos.getX() + (rand.nextDouble() * 1.2) - 0.6;
			double d1 = pos.getY() + (rand.nextDouble() * 1.2) - 0.6;
			double d2 = pos.getZ() + (rand.nextDouble() * 1.2) - 0.6;
			worldIn.addParticle(ParticleTypes.ENCHANT, d0, d1, d2, 0.0D, 0.0D, 0.0D);
		}
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(LINKED);
	}
}
