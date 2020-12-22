package com.gm910.temendingir.blocks.tile.invokers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.gm910.temendingir.TemenDingir;
import com.gm910.temendingir.api.networking.messages.Networking;
import com.gm910.temendingir.api.networking.messages.types.TaskParticles;
import com.gm910.temendingir.api.util.BlockInfo;
import com.gm910.temendingir.api.util.GMWorld;
import com.gm910.temendingir.api.util.ServerSensitiveServerPos;
import com.gm910.temendingir.init.BlockInit;
import com.gm910.temendingir.init.TileInit;
import com.gm910.temendingir.world.gods.Deity;
import com.gm910.temendingir.world.gods.cap.DeityData;
import com.google.common.collect.ImmutableSet;

import net.minecraft.block.Blocks;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.CachedBlockInfo;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IEntityReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.event.world.BlockEvent.NeighborNotifyEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

public abstract class InvokerTileHelper<Tile extends InvokerTileEntity, CreationEvent extends Event> {

	private static final Set<InvokerTileHelper<?, ?>> HELPERS = new HashSet<>();

	public static final InvokerTileHelper<AltarOfConsecration, NeighborNotifyEvent> CONSECRATOR = new InvokerTileHelper<AltarOfConsecration, NeighborNotifyEvent>(
			TileInit.ALTAR_OF_CONSECRATION::get, NeighborNotifyEvent.class, AltarOfConsecration.getGenerationPattern(),
			new ResourceLocation(TemenDingir.MODID, "altar_of_consecration"),
			() -> new BlockInfo(BlockInit.ALTAR_OF_CONSECRATION.get().getDefaultState())) {

		@Override
		public ServerSensitiveServerPos getPosForPlacementFromCreationEvent(NeighborNotifyEvent event) {

			return (event.getWorld() instanceof ServerWorld) && event.getState().getBlock() == Blocks.STONE_BRICK_WALL
					? new ServerSensitiveServerPos(event.getPos(), (ServerWorld) event.getWorld())
					: null;
		}

		@Override
		public boolean canReplaceWithTile(Vector3i palmThumbFingerOffset, CachedBlockInfo cbi) {

			return cbi.getBlockState().getBlock() == Blocks.STONE_BRICK_WALL;
		}
	};

	public static final InvokerTileHelper<FireOfCreation, EntityPlaceEvent> FIRE_OF_CREATION = new InvokerTileHelper<FireOfCreation, EntityPlaceEvent>(
			TileInit.FIRE_OF_CREATION::get, EntityPlaceEvent.class, FireOfCreation.getGenerationPattern(),
			new ResourceLocation(TemenDingir.MODID, "fire_of_creation"),
			() -> new BlockInfo(BlockInit.FIRE_OF_CREATION.get().getDefaultState())) {

		@Override
		public ServerSensitiveServerPos getPosForPlacementFromCreationEvent(EntityPlaceEvent event) {

			return (event.getWorld() instanceof ServerWorld) && event.getState().getBlock() == Blocks.TORCH
					? new ServerSensitiveServerPos(event.getPos(), (ServerWorld) event.getWorld())
					: null;
		}

		@Override
		public boolean canReplaceWithTile(Vector3i palmThumbFingerOffset, CachedBlockInfo cbi) {

			return cbi.getBlockState().getBlock() == Blocks.TORCH;
		}
	};

	public static final InvokerTileHelper<FireOfWorship, EntityPlaceEvent> FIRE_OF_WORSHIP = new InvokerTileHelper<FireOfWorship, EntityPlaceEvent>(
			TileInit.FIRE_OF_WORSHIP::get, EntityPlaceEvent.class, FireOfWorship.getGenerationPattern(),
			new ResourceLocation(TemenDingir.MODID, "fire_of_creation"),
			() -> new BlockInfo(BlockInit.FIRE_OF_WORSHIP.get().getDefaultState())) {

		@Override
		public ServerSensitiveServerPos getPosForPlacementFromCreationEvent(EntityPlaceEvent event) {

			return (event.getWorld() instanceof ServerWorld) && event.getState().getBlock() == Blocks.TORCH
					? new ServerSensitiveServerPos(event.getPos(), (ServerWorld) event.getWorld())
					: null;
		}

		@Override
		public boolean canReplaceWithTile(Vector3i palmThumbFingerOffset, CachedBlockInfo cbi) {

			return cbi.getBlockState().getBlock() == Blocks.TORCH;
		}
	};
	/**
	 * The event type to listen for creation (e.g. placing a torch)
	 */
	private Class<CreationEvent> creationEventListener;
	/**
	 * The pattern that senses the invoker; must detect the structure that creates
	 * the tile entity
	 */
	private BlockPattern pattern;

	public final ResourceLocation name;

	/**
	 * The supplier to place the finished tile; tile entity MUST be an
	 * invokertileentity
	 */
	private Supplier<BlockInfo> onPlacement;

	public final Supplier<TileEntityType<Tile>> tileEntityType;

	public InvokerTileHelper(Supplier<TileEntityType<Tile>> tileType, Class<CreationEvent> eventCreator,
			BlockPattern pattern, ResourceLocation name, Supplier<BlockInfo> placer) {
		this.creationEventListener = eventCreator;
		this.pattern = pattern;
		this.name = name;
		this.onPlacement = placer;
		this.tileEntityType = tileType;
		HELPERS.add(this);
	}

	public void registerToEventBuses() {
		MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, creationEventListener, this::onCreation);
		MinecraftForge.EVENT_BUS.register(this);
	}

	public static void registerAll() {
		HELPERS.forEach((e) -> e.registerToEventBuses());
	}

	public Class<CreationEvent> getCreationEventClass() {
		return creationEventListener;
	}

	public BlockPattern getPattern() {
		return pattern;
	}

	public BlockInfo getPlacer() {
		return this.onPlacement.get();
	}

	/**
	 * This tells what exact section of the structure to replace with the special
	 * tile entity by sensing the CachedBlockInfo of that section; A
	 * palm-thumb-finger (x-y-z) vector3i is given in case this sensing is
	 * positional
	 */
	public abstract boolean canReplaceWithTile(Vector3i palmThumbFingerOffset, CachedBlockInfo cbi);

	/**
	 * Returns the serverPos (server-sensitive to avoid unnecessary data structures)
	 * that need to be checked ; null if the event is invalid
	 * 
	 * @param event
	 * @return
	 */
	public abstract ServerSensitiveServerPos getPosForPlacementFromCreationEvent(CreationEvent event);

	public void onCreation(CreationEvent event) {
		System.out.println("Checking event for " + name);
		ServerSensitiveServerPos operativePos = getPosForPlacementFromCreationEvent(event);
		if (operativePos == null)
			return;

		BlockPos clicked = operativePos.getPos();
		ServerWorld world = operativePos.getWorld();

		System.out.println("starting to scan for structure of " + name);
		BlockPattern pattern = getPattern();
		BlockPattern.PatternHelper altar = getPattern().match(world, clicked);

		if (altar == null) {
			System.out.println("did not match " + clicked + " for " + name);
			return;
		}

		List<ItemFrameEntity> frames = InvokerTileHelper.getItemFrames(getPattern(), altar, Direction.UP, null);

		Deity d = (DeityData.get(world.getServer()).getFromItemFrames(frames));

		if (d != null) {
			BlockPos origin = null;
			for (int x = 0; x < pattern.getPalmLength(); x++) {
				for (int y = 0; y < pattern.getThumbLength(); y++) {
					for (int z = 0; z < pattern.getFingerLength(); z++) {
						CachedBlockInfo info = altar.translateOffset(x, y, z);
						if (this.canReplaceWithTile(new Vector3i(x, y, z), info)) {
							origin = info.getPos();
							break;
						}
					}
				}
			}
			if (origin == null)
				throw new IllegalStateException("No position matches origin matcher for " + name + "!");
			this.successEffect(world, origin);

			BlockInfo info = this.getPlacer();
			if (info.getTile() != null && !(info.getTile() instanceof InvokerTileEntity)) {
				throw new IllegalStateException("Tried to place a block that is not an invoker: " + info.toString());
			}
			info.place(world, origin);
			((InvokerTileEntity) world.getTileEntity(origin)).invoked = d;
			System.out.println("Tile " + name + " placed for god " + d);
		}
		for (ItemFrameEntity fram : frames) {
			BlockPos posf = fram.getHangingPosition();
			if (d != null) {

				Networking.sendToChunk(
						new TaskParticles(ParticleTypes.SOUL, posf.getX() + 0.5, posf.getY() + 0.5, posf.getZ() + 0.5,
								world.getDimensionKey().getLocation(), 0, 0, 0, true, false, false),
						world.getChunkAt(posf));
			} else {

				Networking.sendToChunk(
						new TaskParticles(ParticleTypes.SMOKE, posf.getX() + 0.5, posf.getY() + 0.5, posf.getZ() + 0.5,
								world.getDimensionKey().getLocation(), 0, 0, 0, true, false, false),
						world.getChunkAt(posf));
			}
		}
	}

	/**
	 * Creates an effect acknowledging that an invoker was completed, by default
	 * lightning
	 */
	public void successEffect(World in, BlockPos at) {

		LightningBoltEntity zap = GMWorld.summonLightning(null, in, at.up(), null);
		zap.setEffectOnly(true);
	}

	/**
	 * Returns predicate to check existence of item frame at the given direction (or
	 * any direction if null)
	 * 
	 * @param direction
	 * @return
	 */
	public static Predicate<CachedBlockInfo> itemFramePredicate(@Nullable Direction direction) {
		return (cbi) -> {
			return getItemFrame(cbi, direction) != null;
		};
	}

	/**
	 * Returns a predicate that ensures that a certain CachedBlockInfo has no item
	 * frame at it. Use this with Predicate.and to force an "any" block matcher to
	 * ensure no stray item frames are in the structure and mess up later checking
	 * 
	 * @return
	 */
	public static Predicate<CachedBlockInfo> noItemFramePredicate() {
		return itemFramePredicate(null).negate();
	}

	/**
	 * Returns item frame facing the given direction; if the direction is not given
	 * the direction will not be factored in and a random item frame will be
	 * returned from the ones occupying that block.
	 * 
	 * @param cbi
	 * @param direction
	 * @return
	 */
	public static ItemFrameEntity getItemFrame(CachedBlockInfo cbi, @Nullable Direction direction) {
		if (!(cbi.getWorld() instanceof IEntityReader)) {
			return null;
		}
		List<ItemFrameEntity> itemFrames = ((IEntityReader) cbi.getWorld()).getEntitiesWithinAABB(ItemFrameEntity.class,
				new AxisAlignedBB(cbi.getPos()), (a) -> a.getHangingPosition().equals(cbi.getPos()));

		if (direction != null) {
			List<ItemFrameEntity> onlyInvalids = new ArrayList<>(itemFrames);
			onlyInvalids.removeIf((e) -> e.getHorizontalFacing() == direction);
			if (!onlyInvalids.isEmpty())
				return null;
			itemFrames.removeIf((e) -> e.getHorizontalFacing() != direction);
		}
		return itemFrames.stream().findFirst().orElse(null);
	}

	/**
	 * 
	 * @param helper
	 * @param forAll     if used, every item frame must face this direction to be
	 *                   considered
	 * @param directions if used, the item frames at these positional offsets (which
	 *                   are given as palm, thumb, finger) must face these
	 *                   directions to be returned. If neither directions nor forAll
	 *                   is used, direction will not be cosnidered. directions takes
	 *                   precedence over forAll; if both parameters are given forAll
	 *                   will only be used for parameters which have no
	 *                   corresponding directions-map value
	 * @return item frames in the given block pattern
	 */
	public static List<ItemFrameEntity> getItemFrames(BlockPattern pattern, BlockPattern.PatternHelper helper,
			@Nullable Direction forAll, @Nullable Map<Vector3i, Direction> directions) {
		List<ItemFrameEntity> frames = new ArrayList<>();
		for (int x = 0; x < pattern.getPalmLength(); x++) {
			for (int y = 0; y < pattern.getThumbLength(); y++) {
				for (int z = 0; z < pattern.getFingerLength(); z++) {
					CachedBlockInfo cbi = helper.translateOffset(x, y, z);

					Direction dir = null;
					if (directions != null) {
						dir = directions.get(new Vector3i(x, y, z));
					}
					if (dir == null && forAll != null) {
						dir = forAll;
					}
					ItemFrameEntity frame = getItemFrame(cbi, dir);
					if (frame != null) {
						frames.add(frame);
					}
				}
			}
		}
		return frames;
	}

	public static Collection<InvokerTileHelper<?, ?>> getInvokerTileHelpers() {
		return new ImmutableSet.Builder<InvokerTileHelper<?, ?>>().addAll(HELPERS).build();
	}

	public <T extends InvokerTileEntity, C extends CreationEvent> InvokerTileHelper<T, C> getInvoker(
			ResourceLocation name) {
		return (InvokerTileHelper<T, C>) HELPERS.stream().filter((e) -> e.name.equals(name)).findFirst().orElse(null);
	}

	public <T extends InvokerTileEntity, C extends CreationEvent> InvokerTileHelper<T, C> getInvoker(
			TileEntityType<T> forTile) {
		return (InvokerTileHelper<T, C>) HELPERS.stream().filter((e) -> e.tileEntityType.get().equals(forTile))
				.findAny().orElse(null);
	}

}
