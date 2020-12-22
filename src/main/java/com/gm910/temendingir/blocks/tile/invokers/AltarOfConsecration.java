package com.gm910.temendingir.blocks.tile.invokers;

import java.awt.Rectangle;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.gm910.temendingir.TemenDingir;
import com.gm910.temendingir.api.networking.messages.Networking;
import com.gm910.temendingir.api.networking.messages.types.TaskParticles;
import com.gm910.temendingir.api.util.BlockInfo;
import com.gm910.temendingir.api.util.GMNBT;
import com.gm910.temendingir.api.util.GMWorld;
import com.gm910.temendingir.api.util.ServerPos;
import com.gm910.temendingir.blocks.PylonOfConsecrationBlock;
import com.gm910.temendingir.blocks.tile.ILinkTileNexus;
import com.gm910.temendingir.init.BlockInit;
import com.gm910.temendingir.init.TileInit;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.pattern.BlockMaterialMatcher;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.block.pattern.BlockStateMatcher;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.CachedBlockInfo;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = TemenDingir.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AltarOfConsecration extends InvokerTileEntity implements ITickableTileEntity, ILinkTileNexus {

	public static final double ENERGY_PER_TICK_PER_BLOCK = 1.0 / 36000;
	public static final int DISCOUNT_PER_NEXUS = 2;

	private Set<Vector3i> pylonOffsets = new HashSet<>();
	public boolean isActivated = false;

	private static BlockPattern altarOfConsecrationPattern;

	private Set<BlockPos> particlePositions = new HashSet<>();

	public AltarOfConsecration() {
		super(TileInit.ALTAR_OF_CONSECRATION.get());
	}

	@SubscribeEvent
	public void breakEvent(BlockEvent.BreakEvent event) {
		if (event.getWorld().isRemote())
			return;
		if (this.getLinkedPositions().contains(event.getPos()) && ((ServerWorld) event.getWorld()).getDimensionKey()
				.getLocation().equals(this.world.getDimensionKey().getLocation())) {
			if (this.invoked != null) {
				if (!invoked.isFollower(event.getPlayer().getUniqueID()) && !event.getPlayer().isCreative()) {
					event.setCanceled(true);
				}
				if (!event.isCanceled()) {
					this.removeLinkedPosition(event.getPos(), event.getPlayer(), Hand.MAIN_HAND);
				}
			}
		}
	}

	@Override
	protected BlockPattern getCompletedPattern() {
		return getGenerationPattern();
	}

	@SubscribeEvent
	public void updateLinkedPositions(BlockEvent.NeighborNotifyEvent event) {
		if (world.isRemote)
			return;
		if (pylonOffsets.size() == 2) {
			for (BlockPos linkedpos : this.getLinkedPositions()) {
				if (!this.canLinkTo(new BlockInfo(world, linkedpos))) {
					this.removeLinkedPosition(linkedpos, null, null);
				}
			}
			this.markDirty();
			world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 1 | 2);
		}

	}

	@Override
	protected void revertState() {
		world.setBlockState(pos, Blocks.STONE_BRICK_WALL.getDefaultState(), 1 | 2);
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
			if (world.getGameTime() % 20 == 0) {
				for (BlockPos pos2 : new HashSet<>(this.particlePositions)) {
					if (!((ClientWorld) world).isBlockLoaded(pos2))
						continue;
					for (int i = 0; i < world.rand.nextInt(30); i++)
						world.addOptionalParticle(ParticleTypes.ENCHANT, true, pos2.getX() + GMWorld.getSmallOffset(1),
								pos2.getY() + GMWorld.getSmallOffset(1), pos2.getZ() + GMWorld.getSmallOffset(1), 0, 0,
								0);
				}
			}
			return;
		}

		if (invoked != null
				&& particlePositions.size() != (invoked.getConsecratedOutline().get(world.getDimensionKey())).size()) {

			this.markDirty();
			world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 1 | 2);
		}
		if (invoked != null) {
			if (!invoked.getCreationStage().isComplete()) {
				Networking.sendToChunk(
						new TaskParticles(ParticleTypes.BARRIER, pos.getX(), pos.getY() + 1, pos.getZ(),
								world.getDimensionKey().getLocation(), 0, 0, 0, false, false, false),
						world.getChunkAt(this.pos));
			}
		}
	}

	public static BlockPattern getGenerationPattern() {
		if (altarOfConsecrationPattern == null) {
			altarOfConsecrationPattern = BlockPatternBuilder.start().aisle("?i?", "sss").aisle("iti", "sls")
					.aisle("?i?", "sss").where('?', CachedBlockInfo.hasState(BlockStateMatcher.ANY))
					.where('s', CachedBlockInfo.hasState(BlockMaterialMatcher.forMaterial(Material.ROCK)))
					.where('t',
							CachedBlockInfo.hasState((e) -> e.getBlock() == Blocks.STONE_BRICK_WALL
									|| e.getBlock() == BlockInit.ALTAR_OF_CONSECRATION
											.get() /** TODO PLACEHOLDER block for consecrator **/
							))
					.where('l',
							CachedBlockInfo.hasState((e) -> e.getBlock().isIn(BlockTags.LOGS)
									&& e.get(RotatedPillarBlock.AXIS) == Direction.Axis.Y))
					.where('i', InvokerTileHelper.itemFramePredicate(Direction.UP)).build();
		}

		return altarOfConsecrationPattern;
	}

	@Override
	public void read(BlockState state, CompoundNBT nbt) {
		super.read(state, nbt);
		if (nbt.contains("PylonOffsets"))
			this.pylonOffsets.addAll(GMNBT.createPosList((ListNBT) nbt.get("PylonOffsets")));

	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		if (!pylonOffsets.isEmpty())
			compound.put("PylonOffsets", GMNBT.makePosList(this.pylonOffsets));
		return super.write(compound);
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
			if (this.isActivated) { // TODO Since redstone turns OFF holy land
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
	public void addLinkedPosition(BlockPos pos, PlayerEntity player, Hand hand) {
		if (pos instanceof ServerPos && ((ServerPos) pos).getWorld(world.getServer()) != world) {
			System.out.println("Position to be added " + pos + " is in a different world: " + ((ServerPos) pos).getD());
			return;
		}
		if (world.getBlockState(pos).get(PylonOfConsecrationBlock.LINKED)) {
			System.out.println("Other end is already linked");
			return;
		}
		BlockPos toAdd = pos.subtract(this.pos);
		if (this.pylonOffsets.add(toAdd)) {
			System.out.println("Added " + pos + " to " + pylonOffsets);
		}
		if (pylonOffsets.size() == 2) {
			Vector3i from = pylonOffsets.stream().findFirst().get();
			Vector3i to = pylonOffsets.stream().filter((e) -> !e.equals(from)).findFirst().get();
			if ((from.getX() >= 0) == (to.getX() >= 0) || (from.getZ() >= 0) == (to.getZ() >= 0)) {
				// TODO this if statement must be thoroughly tested
				pylonOffsets.remove(toAdd);
				System.out.println("Nexus was not within pylonOffsets; removing " + pos + " from " + pylonOffsets);
				return;
			}
		}

		world.setBlockState(pos, world.getBlockState(pos).with(PylonOfConsecrationBlock.LINKED, true));

		this.updateHolyLandState();
	}

	@Override
	public void removeLinkedPosition(BlockPos pos, PlayerEntity player, Hand hand) {
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
			this.removeLinkedPosition(pos, null, null);
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