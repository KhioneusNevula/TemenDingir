package com.gm910.temendingir.blocks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gm910.temendingir.api.util.GMHelper;
import com.gm910.temendingir.api.util.ServerPos;
import com.gm910.temendingir.init.BlockInit;
import com.gm910.temendingir.init.EffectInit;
import com.gm910.temendingir.init.TileInit;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class LightBlock extends ModBlock {

	private static final Map<ServerPos, BlockState> stateStorage = new HashMap<>();
	private static final Map<BlockState, FluidState> fluidStateStorage = new HashMap<>();

	public LightBlock() {
		super(Properties.create(Material.AIR).doesNotBlockMovement().noDrops().notSolid());
		this.addTileEntity(TileInit.LIGHT_BLOCK::get, (m, m1) -> true);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {

		int l = ((LightBlockTile) world.getTileEntity(pos)).getLightLevel();
		return 15;// l;
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.INVISIBLE;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {

		return VoxelShapes.empty();
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		super.onReplaced(state, worldIn, pos, newState, isMoving);
		LightBlock.fluidStateStorage.remove(state);
	}

	@SubscribeEvent
	public void livingUpdate(LivingUpdateEvent event) {
		if (event.getEntity().world.isRemote)
			return;
		if (event.getEntityLiving().getActivePotionEffect(EffectInit.GLOWING_HEAD.get()) != null) {
			for (int x = -1; x <= 1; x++) {
				for (int z = -1; z <= 1; z++) {
					BlockPos pos = new BlockPos(event.getEntity().getPosX(), event.getEntity().getPosYEye(),
							event.getEntity().getPosZ());
					if ((event.getEntity().world.getBlockState(pos).getMaterial().isLiquid()
							|| event.getEntity().world.getBlockState(pos).getMaterial() == Material.AIR)
							&& event.getEntity().world.getBlockState(pos).getBlock() != BlockInit.LIGHT_BLOCK.get()) {
						// FluidState prevState =
						// event.getEntity().world.getBlockState(pos).getFluidState();

						BlockState newState = BlockInit.LIGHT_BLOCK.get().getDefaultState();
						if (event.getEntity().world.setBlockState(pos, newState)) {
						}
					}
				}
			}
		}
	}

	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {

		// System.out.println("Light block " + pos);
		stateStorage.put(new ServerPos(pos, worldIn.getDimensionKey()), oldState);
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return fluidStateStorage.getOrDefault(state, super.getFluidState(state));
	}

	public static class LightBlockTile extends TileEntity implements ITickableTileEntity {

		private int lightLevel;
		private BlockState oldState = Blocks.AIR.getDefaultState();

		public LightBlockTile() {
			super(TileInit.LIGHT_BLOCK.get());

		}

		public BlockState getOldState() {
			return oldState;
		}

		@Override
		public void onLoad() {
			super.onLoad();
			if (world.isRemote)
				return;
			oldState = stateStorage.remove(new ServerPos(this.pos, this.world.getDimensionKey()));
			if (oldState == null)
				oldState = Blocks.AIR.getDefaultState();
			fluidStateStorage.put(this.getBlockState(), oldState.getFluidState());
		}

		public int getLightLevel() {
			return (int) GMHelper.clamp(lightLevel, 0, 15);
		}

		@Override
		public void remove() {
			super.remove();
			fluidStateStorage.remove(this.getBlockState());
		}

		@Override
		public void tick() {
			List<LivingEntity> entities = world.getEntitiesWithinAABB(LivingEntity.class,
					(new AxisAlignedBB(pos)).grow(1, 0, 1));
			int lightAmplifier = -1;
			for (LivingEntity en : entities) {
				if (en.isPotionActive(EffectInit.GLOWING_HEAD.get())) {
					lightAmplifier = Math.max(lightAmplifier,
							en.getActivePotionEffect(EffectInit.GLOWING_HEAD.get()).getAmplifier());
					// System.out.println("lamp: " + lightAmplifier);
				}
			}
			// System.out.println("Light block at " + pos + " ens " + entities + " la " +
			// lightAmplifier);
			if (lightAmplifier < 0) {
				world.setBlockState(pos, oldState);
			} else {
				this.lightLevel = (int) GMHelper.clamp((lightAmplifier * 2 + 7), 5, 15);
			}
		}

	}

}
