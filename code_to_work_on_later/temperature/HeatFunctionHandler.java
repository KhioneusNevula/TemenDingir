package com.gm910.temendingir.world.temperature;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.gm910.temendingir.api.util.BlockInfo;
import com.google.common.collect.Sets;

import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BubbleColumnBlock;
import net.minecraft.block.material.Material;
import net.minecraft.state.Property;
import net.minecraft.tags.ITag;
import net.minecraft.util.CachedBlockInfo;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

/**
 * This handles the effects of a heat transfer on certain blocks as well as
 * whether a certain block needs to be monitored specially
 * 
 * @author borah
 *
 */
public class HeatFunctionHandler {

	/**
	 * The default heat rates for given materials
	 */
	public static final Set<Consumer<HeatPropagateEvent>> EVENT_LISTENERS = new HashSet<>();

	public static final Set<Predicate<CachedBlockInfo>> SHOULD_MONITOR_BLOCK = new HashSet<>();

	public static void initSet() {
		System.out.println("Registering heat functions");
		Set<Consumer<HeatPropagateEvent>> l = EVENT_LISTENERS;
		Set<Predicate<CachedBlockInfo>> m = SHOULD_MONITOR_BLOCK;

		HeatEmitterHandler.HEAT_EMISSION_MATERIAL_MAP.keySet().forEach((k) -> m.add(monitorMaterialType(k)));
		HeatEmitterHandler.HEAT_EMISSION_BLOCK_MAP.keySet().forEach((k) -> m.add(monitorBlockType(k)));
		HeatEmitterHandler.HEAT_EMISSION_SPECIFIC_BLOCK_FUNCTIONS.forEach((k) -> m.add((cbi) -> k.apply(cbi) != null));

		l.add(getCauseFire());

		l.add(changeBlock(monitorMaterialType(Material.ROCK), new BlockInfo(Blocks.LAVA.getDefaultState()), 300,
				false));
		l.add(changeBlock(monitorMaterialType(Material.IRON), new BlockInfo(Blocks.LAVA.getDefaultState()), 350,
				false));
		l.add(changeBlock(monitorBlockType(Blocks.SAND), new BlockInfo(Blocks.GLASS.getDefaultState()), 400, false));
		l.add(changeBlock(monitorBlockType(Blocks.RED_SAND), new BlockInfo(Blocks.RED_STAINED_GLASS.getDefaultState()),
				400, false));
		l.add(changeBlock(monitorBlockType(Blocks.WATER),
				new BlockInfo(Blocks.BUBBLE_COLUMN.getDefaultState().with(BubbleColumnBlock.DRAG, true)), 100, false));
		l.add(changeBlock(monitorBlockType(Blocks.WATER), new BlockInfo(Blocks.FROSTED_ICE.getDefaultState()), 15,
				true));
		l.add(changeBlock(monitorBlockType(Blocks.WATER), new BlockInfo(Blocks.ICE.getDefaultState()), 10, true));
		l.add(changeBlock(monitorBlockType(Blocks.WATER), new BlockInfo(Blocks.PACKED_ICE.getDefaultState()), 5, true));
		l.add(changeBlock(monitorBlockType(Blocks.WATER), new BlockInfo(Blocks.BLUE_ICE.getDefaultState()), 1, true));

		l.add(changeBlock(monitorBlockType(Blocks.FROSTED_ICE), new BlockInfo(Blocks.WATER.getDefaultState()), 50,
				false));
		l.add(changeBlock(monitorBlockType(Blocks.ICE), new BlockInfo(Blocks.WATER.getDefaultState()), 50, false));
		l.add(changeBlock(monitorBlockType(Blocks.PACKED_ICE), new BlockInfo(Blocks.WATER.getDefaultState()), 100,
				false));
		l.add(changeBlock(monitorBlockType(Blocks.BLUE_ICE), new BlockInfo(Blocks.WATER.getDefaultState()), 130,
				false));
		System.out.println("Heat functions registered");

		MinecraftForge.EVENT_BUS.post(new TemperatureMonitoringRegistrationEvent());
	}

	public static boolean shouldBeMonitored(CachedBlockInfo info) {
		for (Predicate<CachedBlockInfo> pred : SHOULD_MONITOR_BLOCK) {
			if (pred.test(info))
				return true;
		}
		return false;
	}

	/**
	 * This runs if and only if one/both of the parties in a heat transfer are the
	 * given block, and will set the temperature to a given value constantly
	 * 
	 * @param block
	 * @param value
	 * @param target       whether this is the block is target of the heat; if 0,
	 *                     selects the heat sender, if 1, selects target, and if 2,
	 *                     selects both
	 * @param doIfSunlight if the interaction is from sunlight and sending pos is
	 *                     selected and this is true, the high block the sunlight
	 *                     hit will be heated anyway
	 * @return
	 */
	public static Consumer<HeatPropagateEvent> createConstantTempSetterForBlock(Block block, float value, int target,
			boolean doIfSunlight) {

		return (event) -> {
			if ((target == 0 || target == 2) && event.getState().getBlock() != block
					|| (target == 1 || target == 2) && event.getToState().getBlock() != block)
				return;
			if ((target == 0 || target == 2) && !(!doIfSunlight && event.isSunlightOrEntity())) {
				event.getTemperatureHandler().setTemperatureAt(event.getPos(), value);
				event.setHeatLost(0);
				event.setReturnHeat(0);
			}
			if (target == 1 || target == 2) {
				event.getTemperatureHandler().setTemperatureAt(event.getTo(), value);
				event.setHeatAttained(0);
			}
		};
	}

	public static Consumer<HeatPropagateEvent> getCauseFire() {
		return (event) -> {
			if (event.predictTemperatureAtTo() >= Temperatures.FIRE_TEMPERATURE) {

				System.out.println("Fire attempt at " + event.getTo() + " because temperature is "
						+ event.predictTemperatureAtTo());
				boolean flag = tryCatchFire(event.getWorld(), event.getTo(), 300, event.getWorld().rand, Direction.UP);
				if (!flag) {
					for (Direction dir : Direction.values()) {
						BlockPos adjpos = event.getTo().offset(dir);

						tryCatchFire(event.getWorld(), adjpos, 200, event.getWorld().rand, Direction.UP);
					}
				}
			}
		};
	}

	private static boolean tryCatchFire(World worldIn, BlockPos pos, int chance, Random random, Direction face) {
		int i = worldIn.getBlockState(pos).getFlammability(worldIn, pos, face);
		if (random.nextInt(chance) < i) {
			BlockState blockstate = worldIn.getBlockState(pos);
			if (!worldIn.isRainingAt(pos)) {
				BlockState toPlace = AbstractFireBlock.getFireForPlacement(worldIn, pos);
				worldIn.setBlockState(pos, toPlace, 3);
				return true;
			}

			blockstate.catchFire(worldIn, pos, face, null);
		}

		return false;

	}

	/**
	 * Adds an event changing the given block to another one iff it satisfies the
	 * predicate
	 * 
	 * @param forBlock
	 * @param placeBlock
	 * @param temp
	 * @param below      whether to check if the block's temperature is BELOW this
	 *                   value, so whether to check getFrom() instead of getTo()
	 * @return
	 */
	public static Consumer<HeatPropagateEvent> changeBlock(Predicate<CachedBlockInfo> forBlock,
			Function<CachedBlockInfo, BlockInfo> placeBlock, float temp, boolean below) {
		return (event) -> {
			BlockPos op = below ? event.getPos() : event.getTo();
			CachedBlockInfo cbi = new CachedBlockInfo(event.getWorld(), op, true);
			float atTemp = below ? event.predictTemperatureAtFrom() : event.predictTemperatureAtTo();
			if (forBlock.test(cbi)
					&& (below ? event.predictTemperatureAtFrom() <= temp : event.predictTemperatureAtTo() >= temp)) {
				BlockInfo placer = placeBlock.apply(cbi);
				System.out.println("Attempt to place " + placer + " at " + op + " because temperature is " + atTemp
						+ " and greater than " + temp);
				placer.place(event.getWorld(), op);
			}

		};
	}

	public static Consumer<HeatPropagateEvent> changeBlock(Predicate<CachedBlockInfo> forBlock, BlockInfo placeBlock,
			float temp, boolean below) {
		return changeBlock(forBlock, (cbi) -> placeBlock, temp, below);
	}

	public static Predicate<CachedBlockInfo> monitorMaterialType(Material mat) {
		return (cbi) -> cbi.getBlockState().getMaterial() == mat;
	}

	public static Predicate<CachedBlockInfo> monitorBlockType(Block block) {
		return (cbi) -> cbi.getBlockState().getBlock() == block;
	}

	public static <T extends Comparable<T>> Predicate<CachedBlockInfo> monitorBlockState(@Nullable Block block,
			Property<T> property, T... value) {
		Predicate<CachedBlockInfo> blockChecker;
		if (block != null) {
			blockChecker = (cbi) -> cbi.getBlockState().getBlock() == block;
		} else {
			blockChecker = (cbi) -> cbi.getBlockState().hasProperty(property);
		}
		Set<T> values = Sets.newHashSet(value);
		return blockChecker.and((cbi) -> values.contains(cbi.getBlockState().get(property)));
	}

	public static Predicate<CachedBlockInfo> monitorBlockTag(ITag<Block> tag) {
		return (cbi) -> cbi.getBlockState().isIn(tag);
	}

}
