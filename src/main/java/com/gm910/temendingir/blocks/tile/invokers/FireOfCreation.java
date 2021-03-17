package com.gm910.temendingir.blocks.tile.invokers;

import java.util.List;

import com.gm910.temendingir.TemenDingir;
import com.gm910.temendingir.api.util.ServerPos;
import com.gm910.temendingir.init.BlockInit;
import com.gm910.temendingir.init.DimensionInit;
import com.gm910.temendingir.init.TileInit;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.pattern.BlockMaterialMatcher;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.block.pattern.BlockStateMatcher;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.CachedBlockInfo;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = TemenDingir.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FireOfCreation extends InvokerTileEntity implements ITickableTileEntity {

	private static BlockPattern fireOfCreationPattern;

	public FireOfCreation() {
		super(TileInit.FIRE_OF_CREATION.get());
	}

	@Override
	protected void revertState() {
		world.setBlockState(pos, Blocks.TORCH.getDefaultState());
	}

	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		CompoundNBT nbt = new CompoundNBT();

		return new SUpdateTileEntityPacket(pos, -1, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		super.onDataPacket(net, pkt);
	}

	@Override
	public void tick() {
		if (!(world instanceof ServerWorld)) {

			return;
		}
		if (this.invoked == null)
			return;
		List<PlayerEntity> entities = world.getEntitiesWithinAABB(PlayerEntity.class, new AxisAlignedBB(pos));
		// TODO why is dimension changing malfunctioning
		ServerWorld other = world.getServer().getWorld(DimensionInit.DILMUN);

		if (other != null) {
			entities.forEach((e) -> {
				if (this.invoked.getFollowerIDs().contains(e.getUniqueID())
						&& !invoked.getExtraEntityInfo(e.getUniqueID()).getBoolean("InDilmunPortal")) {
					invoked.getExtraEntityInfo(e.getUniqueID()).put("PositionBeforeDilmun", new ServerPos(e).toNBT());
					invoked.getExtraEntityInfo(e.getUniqueID()).putBoolean("InDilmunPortal", true);

					if (e instanceof ServerPlayerEntity) {
						((ServerPlayerEntity) e).teleport(other, 0, 0, 0, e.rotationYaw, e.rotationPitch);
					} else {
						e.changeDimension(other);
					}
				}
			});
		}

	}

	@Override
	protected BlockPattern getCompletedPattern() {
		return getGenerationPattern();
	}

	public static BlockPattern getGenerationPattern() {
		//if (fireOfCreationPattern == null) {
		fireOfCreationPattern = BlockPatternBuilder.start().aisle("???", "?i?", "sss").aisle("?t?", "ili", "sls")
				.aisle("???", "?i?", "sss")
				.where('?',
						CachedBlockInfo.hasState(BlockStateMatcher.ANY).and(InvokerTileHelper.noItemFramePredicate()))
				.where('s', CachedBlockInfo.hasState(BlockMaterialMatcher.forMaterial(Material.ROCK)))
				.where('t', CachedBlockInfo.hasState(
						(e) -> e.getBlock() == Blocks.TORCH || e.getBlock() == BlockInit.FIRE_OF_CREATION.get()))
				.where('l', CachedBlockInfo.hasState(
						(e) -> e.getBlock().isIn(BlockTags.LOGS) && e.get(RotatedPillarBlock.AXIS) == Direction.Axis.Y))
				.where('i', InvokerTileHelper.itemFramePredicate(Direction.UP)).build();
		//}

		return fireOfCreationPattern;
	}

	@Override
	public void read(BlockState state, CompoundNBT nbt) {
		super.read(state, nbt);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		// TODO nbt the fire of creation
		return super.write(compound);
	}
	/*
		@SubscribeEvent
		public static void createFireOfCreation(BlockEvent.EntityPlaceEvent event) {
			if (event.getWorld().isRemote())
				return;
	
			BlockPos clicked = event.getPos();
			ServerWorld world = (ServerWorld) event.getWorld();
	
			if (event.getPlacedBlock().getBlock() == Blocks.TORCH) {
				System.out.println("Torch placed");
				BlockPattern.PatternHelper altar = getGenerationPattern().match(world, clicked);
				if (altar == null) {
					System.out.println(" did not match " + clicked);
					return;
				}
	
				List<ItemFrameEntity> frames = InvokerTileHelper.getItemFrames(getGenerationPattern(), altar, Direction.UP,
						null);
	
				Deity d = (DeityData.get(world.getServer()).getFromItemFrames(frames));
	
				if (d != null) {
					BlockPos origin = altar.getFrontTopLeft().add(-1, -1, -1);
					LightningBoltEntity zap = GMWorld.summonLightning(null, world, origin.up(), null);
					zap.setEffectOnly(true);
	
					world.setBlockState(origin.up(), BlockInit.FIRE_OF_CREATION.get().getDefaultState());
					System.out.println(d);
					((FireOfCreation) world.getTileEntity(origin.up())).invoked = d;
				}
				for (ItemFrameEntity fram : frames) {
					BlockPos posf = fram.getHangingPosition();
					if (d != null) {
	
						Networking.sendToChunk(new TaskParticles(ParticleTypes.SOUL, posf.getX() + 0.5, posf.getY() + 0.5,
								posf.getZ() + 0.5, world.getDimensionKey().getLocation(), 0, 0, 0, true, false, false),
								world.getChunkAt(posf));
					} else {
	
						Networking.sendToChunk(new TaskParticles(ParticleTypes.SMOKE, posf.getX() + 0.5, posf.getY() + 0.5,
								posf.getZ() + 0.5, world.getDimensionKey().getLocation(), 0, 0, 0, true, false, false),
								world.getChunkAt(posf));
					}
				}
			}
		}*/

}