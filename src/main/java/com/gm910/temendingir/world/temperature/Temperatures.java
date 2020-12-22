package com.gm910.temendingir.world.temperature;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.Nullable;

import com.gm910.temendingir.TemenDingir;
import com.gm910.temendingir.api.util.GMNBT;
import com.gm910.temendingir.capabilities.GMCaps;
import com.gm910.temendingir.capabilities.IModCapability;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;

import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.CachedBlockInfo;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.BlockEvent.NeighborNotifyEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * We can say these temperatures start at absolute zero, but they are not
 * necessarily kelvin since we have to map the temperatures to minecraft scales.
 *
 * @author borah
 *
 */
public class Temperatures implements IModCapability<Chunk> {

	public static final ResourceLocation NAME = new ResourceLocation(TemenDingir.MODID, "temperatures");

	private Chunk chunk;

	public static final float FIRE_TEMPERATURE = 100f;

	/**
	 * Default temperature for this chunk, based on biome.
	 */
	private float defaultTemperature = 0f;
	/**
	 * Temperatures at specific blocks; a return value of null means the default
	 * temperature
	 */
	private Map<BlockPos, Float> specificTemperatures = new TreeMap<>();

	/**
	 * These blocks are to be monitored because, for example, they're fire blocks
	 * and stuff like that
	 */
	private Set<BlockPos> monitoredBlocks = new HashSet<>();

	/**
	 * BlockPositions to be updated next tick
	 */
	private Set<BlockPos> nextTick = new HashSet<>();

	public Temperatures() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	public boolean isDefaultTemperature(BlockPos pos) {
		if (outOfChunk(pos)) {
			return get(chunk.getWorld().getChunkAt(pos)).isDefaultTemperature(pos);
		}
		return specificTemperatures.get(pos) == null;
	}

	public float getTemperatureAt(BlockPos pos) {
		if (outOfChunk(pos)) {
			return get(chunk.getWorld().getChunkAt(pos)).getTemperatureAt(pos);
		}
		return specificTemperatures.getOrDefault(pos, this.defaultTemperature);
	}

	/**
	 * Whether this position is out of the chunk
	 * 
	 * @param pos
	 * @return
	 */
	public boolean outOfChunk(BlockPos pos) {
		return this.chunk.getWorld().getChunkAt(pos) != this.chunk;
	}

	public static Temperatures get(Chunk chunk) {
		return chunk.getCapability(GMCaps.TEMPERATURES).orElse(null);
	}

	public static Temperatures get(World world, BlockPos pos) {
		return get(world.getChunkAt(pos));
	}

	/**
	 * Returns the temperature formerly at this position, or null if it was default.
	 * Similarly, if the temperature is to be set to default, put null for value
	 * 
	 * @param pos
	 * @return previous temperature
	 */
	public Float setTemperatureAt(BlockPos pos, Float value) {
		if (outOfChunk(pos)) {
			return get(chunk.getWorld().getChunkAt(pos)).setTemperatureAt(pos, value);
		}
		return value == null ? this.specificTemperatures.remove(pos) : this.specificTemperatures.put(pos, value);
	}

	public Float changeTemperatureAt(BlockPos pos, float changeBy) {
		return this.setTemperatureAt(pos, this.getTemperatureAt(pos) + changeBy);
	}

	/**
	 * Sends heat to the given position and returns the heat that did not pass
	 * through as well as the recalculated heat value lost from the initial. null if
	 * the block was unable to be loaded
	 * 
	 * @param fromPos       if doEvent is true, set this, or else it will be
	 *                      considered "sunlight"
	 * @param maxSend
	 * @param toPos
	 * @param load          whether to load the targetted block when calculating
	 *                      heat
	 * @param doEvent       whether to send a {@link HeatPropagateEvent}
	 * @param calculateLoss whether to calculate a random percentage lost when the
	 *                      heat travels
	 * @return pair first = return, second = recalculated amount lost from initial
	 */
	public Pair<Float, Float> sendHeat(@Nullable BlockPos fromPos, float maxSend, BlockPos toPos, boolean load,
			boolean doEvent, boolean calculateLoss) {

		if (outOfChunk(toPos)) {
			return get(chunk.getWorld().getChunkAt(toPos)).sendHeat(fromPos, maxSend, toPos, load, doEvent,
					calculateLoss);
		}
		CachedBlockInfo cbi = new CachedBlockInfo(chunk.getWorld(), toPos, load);
		if (cbi.getBlockState() == null)
			return null;

		float rate = HeatRateHandler.getRateFor(cbi);
		float passage = Math.min(rate, maxSend);
		float ret = Math.max(maxSend - rate, 0);
		float lostPassage = passage;
		if (calculateLoss) {
			Random rand = chunk.getWorld().rand;
			float weight = rand.nextFloat();
			float percentagePassage = -0.04f / (weight + 0.07f) + 1;
			weight = rand.nextFloat();
			float percentageRet = -0.04f / (weight + 0.07f) + 1;
			passage *= percentagePassage;
			ret *= percentageRet;
		}
		if (doEvent) {
			HeatPropagateEvent event = new HeatPropagateEvent(this, (ServerWorld) chunk.getWorld(),
					fromPos == null ? new BlockPos(toPos.getX(), chunk.getWorld().getHeight(), toPos.getZ()) : fromPos,
					toPos, passage, passage, ret, fromPos == null);
			HeatFunctionHandler.EVENT_LISTENERS.forEach((c) -> c.accept(event));
			MinecraftForge.EVENT_BUS.post(event);
			passage = event.getHeatAttained();
			lostPassage = event.getHeatLost();
			ret = event.getReturnHeat();
		}
		this.changeTemperatureAt(toPos, passage);
		return Pair.of(ret, lostPassage);

	}

	public void generateHeat(CachedBlockInfo cbi) {
		float randomHeat = HeatEmitterHandler.getEmissionFor(cbi);
		Random rand = chunk.getWorld().rand;
		if (randomHeat > 0) {
			randomHeat *= -0.03f / (rand.nextFloat() + 0.03f / (1.3f)) + 1.3f; // The heat amount added will be randomized; rarely, heat will not increase at all during certain ticks
		} else {
			randomHeat *= -1;
		}
		if (randomHeat != 0) {
			this.changeTemperatureAt(cbi.getPos(), randomHeat);
		}
	}

	/**
	 * Does a tick at the given block; returns a set of blocks to tick next
	 * 
	 */
	public Set<BlockPos> doTick(BlockPos at) {
		if (this.outOfChunk(at)) {
			get(chunk.getWorld().getChunkAt(at)).doTick(at);
			return Sets.newHashSet();
		}
		CachedBlockInfo cbi = new CachedBlockInfo(chunk.getWorld(), at, true);
		Set<BlockPos> theSet = new HashSet<>();
		this.generateHeat(cbi);
		theSet.addAll(this.doPropagations(cbi));
		if (Math.abs(this.getTemperatureAt(at) - this.defaultTemperature) < 0.001
				&& !this.monitoredBlocks.contains(at)) {
			this.setTemperatureAt(at, null);
		}
		return theSet;
	}

	/**
	 * Does the act of propagating heat from the given block to surrounding ones
	 * 
	 * @param at
	 */
	public Set<BlockPos> doPropagations(CachedBlockInfo cbi) {
		BlockPos at = cbi.getPos();
		if (outOfChunk(at)) {
			return get(chunk.getWorld().getChunkAt(at)).doPropagations(cbi);

		}
		Random rand = chunk.getWorld().rand;
		Set<BlockPos> theSet = new HashSet<>();
		for (Direction dir : Direction.values()) {
			BlockPos to = at.offset(dir);
			float currentHeat = this.getTemperatureAt(at);
			float otherHeat = this.getTemperatureAt(to);
			if (otherHeat >= currentHeat) {
				theSet.add(to);
				continue; // If the other block is hotter, it will diffuse into this block anyway so skip it
			}
			float diff = currentHeat - otherHeat;
			float amountSent = diff;
			float weight = rand.nextFloat();
			float percentagePassage = -0.03f / (weight + 0.03f / 1.5f) + 1.5f; // randomize the amount of heat sent, slightly
			amountSent *= percentagePassage;
			amountSent = Math.min(currentHeat, amountSent); // if the amount of heat sent is more than this can handle, minimize it to give all heat i guess

			Pair<Float, Float> ret = this.sendHeat(at, amountSent, to, false, true, true);
			if (ret == null)
				continue;
			this.changeTemperatureAt(at, -ret.getSecond() + ret.getFirst());
			theSet.add(to);
		}
		return theSet;
	}

	@SubscribeEvent
	public void loaded(ChunkEvent.Load event) {
		if (event.getWorld().isRemote() || event.getChunk() != this.chunk) {
			return;
		}
		chunk.getWorld().getServer().deferTask(this::onChunkLoad);
	}

	@SubscribeEvent
	public void update(NeighborNotifyEvent event) {
		if (event.getWorld().isRemote() || event.getWorld().getChunk(event.getPos()) != this.chunk) {
			return;
		}
		this.checkIfMonitored(event.getPos());
	}

	@SubscribeEvent
	public void update(TickEvent.WorldTickEvent event) {
		if (event.side.isClient() || event.phase == TickEvent.Phase.START
				|| event.world.chunkExists(chunk.getPos().x, chunk.getPos().z)
				|| event.world.getChunk(chunk.getPos().asBlockPos()) != chunk) {
			return;
		}
		Set<BlockPos> positionsToBeTicked = new HashSet<>(this.monitoredBlocks);
		positionsToBeTicked.addAll(this.nextTick);
		this.nextTick.clear();
		for (BlockPos pos : positionsToBeTicked) {
			if (event.world.getChunk(pos) != this) {
				if (event.world.getChunk(pos) != null) {
					get(event.world.getChunkAt(pos)).nextTick.add(pos);
				}
				continue;
			}
			this.nextTick.addAll(this.doTick(pos));

		}

		this.performSunlightHeating();

	}

	public void performSunlightHeating() {
		ChunkPos cpos = chunk.getPos();
		BlockPos.Mutable pos = new BlockPos.Mutable(0, 0, 0);
		for (int x = cpos.getXStart(); x <= cpos.getXEnd(); x++) {
			for (int z = cpos.getZStart(); z <= cpos.getZEnd(); z++) {
				int y = chunk.getWorld().getHeight();
				while (!World.isYOutOfBounds(y) && (!chunk.getWorld().canSeeSky(pos)
						|| chunk.getWorld().getBlockState(pos).getBlock() == Blocks.AIR))
					y--;
				if (World.isYOutOfBounds(y))
					continue;
				pos.setPos(x, y, z);
				float temp = chunk.getWorld().getBiome(pos).getTemperature(pos) + 0.01f;
				this.sendHeat(null, temp, pos.toImmutable(), true, true, true);
				this.nextTick.add(pos.toImmutable());
			}
		}
	}

	public void onChunkLoad() {
		ChunkPos cpos = chunk.getPos();
		float defaultTemp = 0;
		int count = 0;
		Iterator<BlockPos> iter = BlockPos.getAllInBox(new BlockPos(cpos.getXStart(), 0, cpos.getZStart()),
				new BlockPos(cpos.getXEnd(), chunk.getWorld().getHeight(), cpos.getZEnd())).iterator();
		while (iter.hasNext()) {
			count++;
			BlockPos pos = iter.next();
			this.checkIfMonitored(pos);
			Biome biome = chunk.getWorld().getBiome(pos);
			defaultTemp += (biome.getTemperature() + 0.01) * 25;
		}
		defaultTemp /= count;
		this.defaultTemperature = defaultTemp;
	}

	/**
	 * If this position should be monitored, add it to the monitoring set
	 * 
	 * @param at
	 */
	public void checkIfMonitored(BlockPos at) {
		if (HeatFunctionHandler.shouldBeMonitored(new CachedBlockInfo(chunk.getWorld(), at, true))) {
			this.monitoredBlocks.add(at);
		}
	}

	public float getDefaultTemperature() {
		return defaultTemperature;
	}

	@Override
	public Chunk $getOwner() {
		return chunk;
	}

	@Override
	public void $setOwner(Chunk e) {
		this.chunk = e;
	}

	public CompoundNBT serializeNBT() {
		CompoundNBT nbt = new CompoundNBT();

		nbt.putFloat("DefaultTemperature", defaultTemperature);
		nbt.put("Temperatures", GMNBT.makeList(this.specificTemperatures.entrySet(), (entry) -> {
			CompoundNBT t = new CompoundNBT();

			t.putLong("Pos", entry.getKey().toLong());
			t.putFloat("Temp", entry.getValue());
			return t;

		}));
		nbt.put("Monitored", GMNBT.makePosList(this.monitoredBlocks));
		nbt.put("NextTick", GMNBT.makePosList(this.nextTick));
		return nbt;
	}

	public void deserializeNBT(CompoundNBT nbt) {
		this.defaultTemperature = nbt.getFloat("DefaultTemperature");
		this.specificTemperatures.clear();
		this.specificTemperatures.putAll(GMNBT.createMap(GMNBT.getList(nbt, "DefaultTemperatures"), (inbt) -> {
			CompoundNBT t = (CompoundNBT) inbt;

			return Pair.of(BlockPos.fromLong(t.getLong("Pos")), t.getFloat("Temp"));
		}));
		this.monitoredBlocks.clear();
		this.monitoredBlocks.addAll(GMNBT.createPosList(GMNBT.getList(nbt, "Monitored")));
		this.nextTick.clear();
		this.nextTick.addAll(GMNBT.createPosList(GMNBT.getList(nbt, "NextTick")));
	}

	public static class TemperatureStorage implements IStorage<Temperatures> {

		@Override
		public INBT writeNBT(Capability<Temperatures> capability, Temperatures instance, Direction side) {
			return instance.serializeNBT();
		}

		@Override
		public void readNBT(Capability<Temperatures> capability, Temperatures instance, Direction side, INBT nbt) {
			instance.deserializeNBT((CompoundNBT) nbt);
		}

	}

}
