package com.gm910.temendingir.blocks.tile;

import java.util.ArrayList;
import java.util.List;

import com.gm910.temendingir.TemenDingir;
import com.gm910.temendingir.api.networking.messages.Networking;
import com.gm910.temendingir.api.networking.messages.types.TaskParticles;
import com.gm910.temendingir.api.util.GMWorld;
import com.gm910.temendingir.api.util.ServerPos;
import com.gm910.temendingir.init.BlockInit;
import com.gm910.temendingir.init.DimensionInit;
import com.gm910.temendingir.init.TileInit;
import com.gm910.temendingir.world.gods.Deity;
import com.gm910.temendingir.world.gods.cap.DeityData;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.pattern.BlockMaterialMatcher;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPattern.PatternHelper;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.block.pattern.BlockStateMatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.CachedBlockInfo;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = TemenDingir.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FireOfCreation extends TileEntity implements ITickableTileEntity {

	private Deity invoked;

	private static BlockPattern fireOfCreationPattern;
	private static BlockPattern fireOfCreationCompletePattern;

	public FireOfCreation() {
		super(TileInit.FIRE_OF_CREATION.get());
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void remove() {
		super.remove();
		MinecraftForge.EVENT_BUS.unregister(this);
	}

	@SubscribeEvent
	public void updateEvent(BlockEvent.NeighborNotifyEvent event) {
		PatternHelper altar = getCompleteStructurePattern().match(world, pos);
		boolean break1 = false;
		if (altar == null) {
			break1 = true;
		} else {
			List<ItemStack> items = new ArrayList<>();

			List<ItemFrameEntity> frames = world.getEntitiesWithinAABB(ItemFrameEntity.class,
					new AxisAlignedBB(altar.getFrontTopLeft().add(1, 1, 1)).grow(4));
			BlockPos[] framePositions = { altar.getFrontTopLeft().add(0, 0, -1), altar.getFrontTopLeft().add(-1, 0, 0),
					altar.getFrontTopLeft().add(-2, 0, -1), altar.getFrontTopLeft().add(-1, 0, -2) };

			for (BlockPos posf : framePositions) {

				ItemFrameEntity frame = frames.stream()
						.filter((e) -> e.getHangingPosition().equals(posf) && e.getHorizontalFacing() == Direction.UP)
						.findAny().orElse(null);
				if (frame != null) {
					System.out.println(frame.getDisplayedItem());
					if (frame.getDisplayedItem().isEmpty()) {
						ItemStack temp1 = frame.getDisplayedItem().copy();
						temp1.grow(1);
						items.add(temp1);
					} else {
						items.add(frame.getDisplayedItem());
					}
				} else {
					break1 = true;

					break;
				}
			}
			if (!break1) {
				Deity d = (DeityData.get(world.getServer()).getFromItems(items.toArray(new ItemStack[0])));
				if (this.invoked != null && d != this.invoked) {

					break1 = true;
				}
			}

		}
		if (break1) {
			world.setBlockState(pos, Blocks.TORCH.getDefaultState());
		} else {
		}
	}

	public void launch(Entity e) {
		System.out.println("Launching " + e + (e instanceof ItemEntity ? ((ItemEntity) e).getItem() : ""));
		e.addVelocity(e.world.rand.nextDouble() - 0.5, 0.3, e.world.rand.nextDouble() - 0.5);
		for (int i = 0; i < 20; i++)
			Networking.sendToTracking(
					new TaskParticles(ParticleTypes.ENCHANTED_HIT, e.getPosX() + world.rand.nextDouble() - 0.5,
							e.getPosY() + world.rand.nextDouble() - 0.5, e.getPosZ() + world.rand.nextDouble() - 0.5,
							e.world.getDimensionKey().getLocation(), world.rand.nextDouble() * 4 - 2,
							world.rand.nextDouble() * 4 - 2, world.rand.nextDouble() * 4 - 2, true, false, false),
					e);
	}

	public void consumeItem(ItemEntity booke) {
		booke.remove();
		System.out.println(booke.getItem() + " consumed");
		for (int i = 0; i < 20; i++)
			Networking.sendToTracking(
					new TaskParticles(ParticleTypes.SOUL_FIRE_FLAME, booke.getPosX() + world.rand.nextDouble() - 0.5,
							booke.getPosY() + world.rand.nextDouble() - 0.5,
							booke.getPosZ() + world.rand.nextDouble() - 0.5,
							booke.world.getDimensionKey().getLocation(), world.rand.nextDouble() * 4 - 2,
							world.rand.nextDouble() * 4 - 2, world.rand.nextDouble() * 4 - 2, true, false, false),
					booke);
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
		if (world.isRemote) {

			return;
		}
		if (this.invoked == null)
			return;
		List<PlayerEntity> entities = world.getEntitiesWithinAABB(PlayerEntity.class, new AxisAlignedBB(pos));
		// TODO why is dimension changing malfunctioning
		entities.forEach((e) -> {
			if (this.invoked.getFollowerIDs().contains(e.getUniqueID())
					&& !invoked.getExtraEntityInfo(e.getUniqueID()).getBoolean("InDilmunPortal")) {
				invoked.getExtraEntityInfo(e.getUniqueID()).put("PositionBeforeDilmun", new ServerPos(e).toNBT());
				invoked.getExtraEntityInfo(e.getUniqueID()).putBoolean("InDilmunPortal", true);
				e.changeDimension(world.getServer().getWorld(DimensionInit.DILMUN));

			}
		});

	}

	private static BlockPattern getGenerationPattern() {
		if (fireOfCreationPattern == null) {
			fireOfCreationPattern = BlockPatternBuilder.start().aisle("???", "?t?", "???").aisle("sss", "sls", "sss")
					.where('?', CachedBlockInfo.hasState(BlockStateMatcher.ANY))
					.where('s', CachedBlockInfo.hasState(BlockMaterialMatcher.forMaterial(Material.ROCK)))
					.where('t', CachedBlockInfo.hasState((e) -> e.getBlock() == Blocks.TORCH))
					.where('l', CachedBlockInfo.hasState((e) -> e.getBlock().isIn(BlockTags.LOGS)
							&& e.get(RotatedPillarBlock.AXIS) == Direction.Axis.Y))
					.build();
		}

		return fireOfCreationPattern;
	}

	private static BlockPattern getCompleteStructurePattern() {
		if (fireOfCreationCompletePattern == null)
			fireOfCreationCompletePattern = BlockPatternBuilder.start().aisle("???", "?t?", "???")
					.aisle("sss", "sls", "sss").where('?', CachedBlockInfo.hasState(BlockStateMatcher.ANY)).where('s',
							CachedBlockInfo.hasState(BlockMaterialMatcher.forMaterial(Material.ROCK)))
					.where('t', CachedBlockInfo.hasState(BlockStateMatcher.forBlock(BlockInit.FIRE_OF_CREATION.get())))
					.where('l', CachedBlockInfo.hasState((e) -> e.getBlock().isIn(BlockTags.LOGS)
							&& e.get(RotatedPillarBlock.AXIS) == Direction.Axis.Y))
					.build();
		return fireOfCreationCompletePattern;
	}

	@Override
	public void read(BlockState state, CompoundNBT nbt) {
		super.read(state, nbt);
//TODO nbt this fireofcreation
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		// TODO nbt the fire of creation
		return super.write(compound);
	}

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
			List<ItemFrameEntity> frames = world.getEntitiesWithinAABB(ItemFrameEntity.class,
					new AxisAlignedBB(altar.getFrontTopLeft().add(1, 1, 1)).grow(4));
			List<ItemStack> items = new ArrayList<>();

			BlockPos[] framePositions = { altar.getFrontTopLeft().add(0, 0, -1), altar.getFrontTopLeft().add(-1, 0, 0),
					altar.getFrontTopLeft().add(-2, 0, -1), altar.getFrontTopLeft().add(-1, 0, -2) };

			for (BlockPos posf : framePositions) {
				ItemFrameEntity frame = frames.stream()
						.filter((e) -> e.getHangingPosition().equals(posf) && e.getHorizontalFacing() == Direction.UP)
						.findAny().orElse(null);
				if (frame != null) {
					System.out.println(frame.getDisplayedItem());
					if (frame.getDisplayedItem().isEmpty()) {
						ItemStack temp1 = frame.getDisplayedItem().copy();
						temp1.grow(1);
						items.add(temp1);
					} else {
						items.add(frame.getDisplayedItem());
					}
				} else {
					return;
				}
			}
			BlockPos origin = altar.getFrontTopLeft().add(-1, -1, -1);
			LightningBoltEntity zap = GMWorld.summonLightning(null, world, origin.up(), null);
			zap.setEffectOnly(true);

			world.setBlockState(origin.up(), BlockInit.FIRE_OF_CREATION.get().getDefaultState());
			Deity d = (DeityData.get(world.getServer()).getFromItems(items.toArray(new ItemStack[0])));
			System.out.println(d);
			((FireOfCreation) world.getTileEntity(origin.up())).invoked = d;
			for (BlockPos posf : framePositions) {
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
	}

}