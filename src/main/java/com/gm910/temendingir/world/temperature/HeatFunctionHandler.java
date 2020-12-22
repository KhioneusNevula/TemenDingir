package com.gm910.temendingir.world.temperature;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.state.Property;
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

	public static final Map<Block, Float> LAVA_AT_TEMP = new HashMap<>();

	public static void initSet() {
		Set<Consumer<HeatPropagateEvent>> l = EVENT_LISTENERS;
		Set<Predicate<CachedBlockInfo>> m = SHOULD_MONITOR_BLOCK;
		Map<Block, Float> la = LAVA_AT_TEMP;

		HeatEmitterHandler.HEAT_EMISSION_MATERIAL_MAP.keySet().forEach((k) -> m.add(monitorMaterialType(k)));
		HeatEmitterHandler.HEAT_EMISSION_BLOCK_MAP.keySet().forEach((k) -> m.add(monitorBlockType(k)));
		HeatEmitterHandler.HEAT_EMISSION_SPECIFIC_BLOCK_FUNCTIONS.forEach((k) -> m.add((cbi) -> k.apply(cbi) != null));

		l.add(getCauseFire());

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
			if ((target == 0 || target == 2) && !(!doIfSunlight && event.isSunlight())) {
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
				worldIn.setBlockState(pos, Blocks.FIRE.getDefaultState(), 3);
				return true;
			}

			blockstate.catchFire(worldIn, pos, face, null);
		}
		return false;

	}

	public static Consumer<HeatPropagateEvent> getCauseLava() {
		return (event) -> {
			if (LAVA_AT_TEMP.get(event.getToState().getBlock()) != null) {
				//TODO lava heat
			}
		};
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

}
