package com.gm910.temendingir.blocks.tile;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.gm910.temendingir.TemenDingir;
import com.gm910.temendingir.api.networking.messages.Networking;
import com.gm910.temendingir.api.networking.messages.types.TaskParticles;
import com.gm910.temendingir.api.util.BlockInfo;
import com.gm910.temendingir.api.util.GMNBT;
import com.gm910.temendingir.api.util.GMWorld;
import com.gm910.temendingir.api.util.ServerPos;
import com.gm910.temendingir.blocks.PylonOfConsecrationBlock;
import com.gm910.temendingir.init.BlockInit;
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
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
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
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = TemenDingir.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AltarOfConsecration extends TileEntity implements ITickableTileEntity, ILinkTileNexus {

	private Deity invoked;
	private UUID invokedId;
	private Set<Vector3i> pylonOffsets = new HashSet<>();
	public boolean isActivated = false;

	private static BlockPattern altarOfConsecrationPattern;
	private static BlockPattern altarOfConsecrationCompletePattern;

	private Set<BlockPos> particlePositions = new HashSet<>();

	public AltarOfConsecration() {
		super(TileInit.ALTAR_OF_CONSECRATION.get());
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void remove() {
		super.remove();
		MinecraftForge.EVENT_BUS.unregister(this);
	}

	@SubscribeEvent
	public void breakEvent(BlockEvent.BreakEvent event) {
		if (event.getWorld().isRemote())
			return;
		if (this.getLinkedPositions().contains(event.getPos())) {
			if (this.invoked != null) {
				if (!invoked.isFollower(event.getPlayer().getUniqueID()) && !event.getPlayer().isCreative()) {
					event.setCanceled(true);
				}
			}
		}
	}

	@SubscribeEvent
	public void updateEvent(BlockEvent.NeighborNotifyEvent event) {
		PatternHelper altar = getCompleteStructurePattern().match(world, pos);
		boolean break1 = false;
		if (altar == null) {
			System.out.println("No altar pattern found");
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
					// System.out.println(frame.getDisplayedItem());
					if (frame.getDisplayedItem().isEmpty()) {
						ItemStack temp1 = frame.getDisplayedItem().copy();
						temp1.grow(1);
						items.add(temp1);
					} else {
						items.add(frame.getDisplayedItem());
					}
				} else {
					System.out.println("No item frame");
					break1 = true;

					break;
				}
			}
			if (!break1 && invoked != null) {
				Deity d = (DeityData.get(world.getServer()).getFromItems(items.toArray(new ItemStack[0])));
				if (d != this.invoked) {
					System.out.println("Deity " + d + " is not invoked " + this.invoked);
					break1 = true;
				}
			}

		}
		if (pylonOffsets.size() == 2) {
			for (BlockPos linkedpos : this.getLinkedPositions()) {
				if (!this.canLinkTo(new BlockInfo(world, linkedpos))) {
					this.removeLinkedPosition(linkedpos);
				}
			}
			this.markDirty();
			world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 1 | 2);
		}

		if (break1) {
			world.setBlockState(pos, Blocks.STONE_BRICK_WALL.getDefaultState(), 1 | 2);
			world.removeTileEntity(pos);
		} else {

		}
	}

	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		CompoundNBT nbt = new CompoundNBT();
		if (invoked != null) {
			this.particlePositions.clear();
			this.particlePositions.addAll(invoked.getConsecratedOutline().get(world.getDimensionKey()));
		}
		nbt.put("ParticlePositions", GMNBT.makeList(this.particlePositions, (e) -> ServerPos.toNBT(e)));

		// System.out.println("updating packet");
		return new SUpdateTileEntityPacket(pos, -1, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		super.onDataPacket(net, pkt);
		particlePositions.clear();
		particlePositions.addAll(GMNBT.createList((ListNBT) pkt.getNbtCompound().get("ParticlePositions"), (inbt) -> {
			return ServerPos.bpFromNBT((CompoundNBT) inbt);
		}));
	}

	@Override
	public void tick() {
		if (world.isRemote) {
			if (world.isBlockPowered(pos) && world.getGameTime() % 10 == 0) {
				for (BlockPos pos2 : this.particlePositions) {
					if (!((ClientWorld) world).isBlockLoaded(pos2))
						continue;
					world.addOptionalParticle(ParticleTypes.END_ROD, true, pos2.getX() + GMWorld.getSmallOffset(1),
							pos2.getY() + GMWorld.getSmallOffset(1), pos2.getZ() + GMWorld.getSmallOffset(1), 0, 0, 0);
				}
			}
			return;
		}

		if (invoked != null
				&& !particlePositions.equals(invoked.getConsecratedOutline().get(world.getDimensionKey()))) {
			this.markDirty();
			world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 1 | 2);

		}
	}

	@Override
	public void onLoad() {
		if (invokedId != null && !world.isRemote) {
			this.invoked = DeityData.get(world.getServer()).getFromUUID(invokedId);

			invoked.recalculateConsecrationAreaAndOutline();
		}
	}

	private static BlockPattern getGenerationPattern() {
		if (altarOfConsecrationPattern == null) {
			altarOfConsecrationPattern = BlockPatternBuilder.start().aisle("???", "?t?", "???")
					.aisle("sss", "sls", "sss").where('?', CachedBlockInfo.hasState(BlockStateMatcher.ANY))
					.where('s', CachedBlockInfo.hasState(BlockMaterialMatcher.forMaterial(Material.ROCK)))
					.where('t', CachedBlockInfo.hasState((
							e) -> e.getBlock() == Blocks.STONE_BRICK_WALL /** TODO PLACEHOLDER block for consecrator **/
					)).where('l', CachedBlockInfo.hasState((e) -> e.getBlock().isIn(BlockTags.LOGS)
							&& e.get(RotatedPillarBlock.AXIS) == Direction.Axis.Y))
					.build();
		}

		return altarOfConsecrationPattern;
	}

	private static BlockPattern getCompleteStructurePattern() {
		if (altarOfConsecrationCompletePattern == null)
			altarOfConsecrationCompletePattern = BlockPatternBuilder.start().aisle("???", "?t?", "???")
					.aisle("sss", "sls", "sss").where('?', CachedBlockInfo.hasState(BlockStateMatcher.ANY))
					.where('s',
							CachedBlockInfo.hasState(BlockMaterialMatcher.forMaterial(Material.ROCK)))
					.where('t',
							CachedBlockInfo.hasState(BlockStateMatcher.forBlock(BlockInit.ALTAR_OF_CONSECRATION.get())))
					.where('l', CachedBlockInfo.hasState((e) -> e.getBlock().isIn(BlockTags.LOGS)
							&& e.get(RotatedPillarBlock.AXIS) == Direction.Axis.Y))
					.build();
		return altarOfConsecrationCompletePattern;
	}

	@Override
	public void read(BlockState state, CompoundNBT nbt) {
		super.read(state, nbt);
		if (nbt.hasUniqueId("Deity")) {
			this.invokedId = nbt.getUniqueId("Deity");
		}
		if (nbt.contains("PylonOffsets"))
			this.pylonOffsets.addAll(GMNBT.createPosList((ListNBT) nbt.get("PylonOffsets")));

	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		if (invoked != null)
			compound.putUniqueId("Deity", this.invoked.getUuid());
		if (!pylonOffsets.isEmpty())
			compound.put("PylonOffsets", GMNBT.makePosList(this.pylonOffsets));
		return super.write(compound);
	}

	@SubscribeEvent
	public static void createAltar(BlockEvent.NeighborNotifyEvent event) {
		if (event.getWorld().isRemote())
			return;

		BlockPos clicked = event.getPos();
		ServerWorld world = (ServerWorld) event.getWorld();

		if (event.getState().getBlock() == Blocks.STONE_BRICK_WALL) {
			System.out.println("brick wall exists");
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
			BlockPos altarpos = origin.up();
			LightningBoltEntity zap = GMWorld.summonLightning(null, world, altarpos, null);
			zap.setEffectOnly(true);

			Deity d = (DeityData.get(world.getServer()).getFromItems(items.toArray(new ItemStack[0])));
			if (d != null) {
				System.out.println(d);
				System.out.println(world.getBlockState(altarpos));
				world.setBlockState(altarpos, BlockInit.ALTAR_OF_CONSECRATION.get().getDefaultState());

				System.out.println(world.getBlockState(altarpos));
				((AltarOfConsecration) world.getTileEntity(altarpos)).invoked = d;
			}
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

	public Set<BlockPos> getLinkedPositions() {
		return pylonOffsets.stream().map((p) -> this.pos.add(p)).collect(Collectors.toSet());
	}

	public Set<Vector3i> getLinkOffsetVectors() {
		return new HashSet<>(pylonOffsets);
	}

	public Rectangle getRectangleOfRegion() {
		if (this.pylonOffsets.size() < 2) {
			return null;
		}
		BlockPos pos1 = this.getLinkedPositions().stream().findFirst().orElse(null);
		BlockPos pos2 = this.getLinkedPositions().stream().filter((p) -> !pos1.equals(p)).findFirst().orElse(null);

		System.out.println(pos1 + " " + pos2);
		Rectangle rect = new Rectangle();

		rect.setFrameFromDiagonal(pos1.getX(), pos1.getZ(), pos2.getX(), pos2.getZ());

		return rect;
	}

	public void updateHolyLandState() {
		System.out.println("Deity " + invoked);
		if (this.pylonOffsets.size() < 2) {
			System.out.println(pylonOffsets + " is not long enough");
			this.invoked.removeHolyRegion(new ServerPos(this));
		} else {
			if (this.isActivated) {
				this.invoked.addHolyRegion(new ServerPos(this), getRectangleOfRegion());

			} else {

				this.invoked.removeHolyRegion(new ServerPos(this));
			}
		}

		this.particlePositions.clear();
		this.particlePositions.addAll(invoked.getConsecratedOutline().get(world.getDimensionKey()));
		// System.out.println(particlePositions);

	}

	@Override
	public void addLinkedPosition(BlockPos pos) {
		if (pos instanceof ServerPos && ((ServerPos) pos).getWorld(world.getServer()) != world) {
			System.out.println("Position to be added " + pos + " is in the wrong world.");
			return;
		}
		if (world.getBlockState(pos).get(PylonOfConsecrationBlock.LINKED)) {
			System.out.println("Other end is already linked");
			return;
		}
		if (this.pylonOffsets.add(pos.subtract(this.pos))) {
			System.out.println("Added " + pos + " to " + pylonOffsets);
		}
		world.setBlockState(pos, world.getBlockState(pos).with(PylonOfConsecrationBlock.LINKED, true));

		this.updateHolyLandState();
	}

	@Override
	public void removeLinkedPosition(BlockPos pos) {
		if (this.pylonOffsets.remove(pos.subtract(this.pos))) {

			System.out.println("removed " + pos + " from " + pylonOffsets);
		}
		GMWorld.summonLightning(null, world, pos, null).setEffectOnly(true);
		if (world.getBlockState(pos).hasProperty(PylonOfConsecrationBlock.LINKED)) {
			world.setBlockState(pos, world.getBlockState(pos).with(PylonOfConsecrationBlock.LINKED, false));
		}
		this.updateHolyLandState();
	}

	public void clearLinkedPositions() {
		for (BlockPos pos : this.getLinkedPositions()) {
			this.removeLinkedPosition(pos);
		}
	}

	@Override
	public boolean canContinueLinking() {
		return pylonOffsets.size() < 2;
	}

	@Override
	public boolean canLinkTo(BlockInfo tile) {

		return tile.getState().getBlock() == BlockInit.PYLON_OF_CONSECRATION.get();
	}

}