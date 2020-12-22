package com.gm910.temendingir.world.temperature;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.collect.Sets;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.material.Material;
import net.minecraft.state.Property;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.CachedBlockInfo;
import net.minecraftforge.common.MinecraftForge;

/**
 * In this mod, heat rate can be considered as the speed that temperature
 * propagates. So, the amount of energy that would be accepted by a certain
 * material per tick. For blocks that are unspecified, defaults to the material
 * value. For new materials, defaults to arbitrary value 10.
 * 
 * @author borah
 *
 */
public class HeatRateHandler {

	public static final float DEFAULT = 10;

	/**
	 * The default heat rates for given materials
	 */
	public static final Object2FloatMap<Material> HEAT_RATE_MATERIAL_MAP = new Object2FloatOpenHashMap<>();

	/**
	 * The default heat rates for given blocks
	 */
	public static final Map<Block, Float> HEAT_RATE_BLOCK_MAP = new HashMap<>();

	/**
	 * Specific functions that calculate a value for specific blocks; return null if
	 * not applicable
	 */
	public static final Set<Function<CachedBlockInfo, Float>> HEAT_RATE_SPECIFIC_BLOCK_FUNCTIONS = new HashSet<>();

	private static final float WATER_VALUE = calcRate(5 * 60);
	private static final float IRON_VALUE = calcRate(0.3f);

	public static void initTemperatureValues() {
		Object2FloatMap<Material> m = HEAT_RATE_MATERIAL_MAP;
		Map<Block, Float> b = HEAT_RATE_BLOCK_MAP;
		Set<Function<CachedBlockInfo, Float>> s = HEAT_RATE_SPECIFIC_BLOCK_FUNCTIONS;

		// TODO make this data-driven someday
		m.put(Material.AIR, calcRate(10));
		m.put(Material.ANVIL, IRON_VALUE);
		m.put(Material.BAMBOO, calcRate(5));
		m.put(Material.WOOD, calcRate(50));
		m.put(Material.BAMBOO_SAPLING, calcRate(5));
		m.put(Material.BARRIER, 0);
		m.put(Material.WATER, WATER_VALUE);
		m.put(Material.BUBBLE_COLUMN, WATER_VALUE);
		m.put(Material.CACTUS, calcRate(10));
		m.put(Material.CAKE, calcRate(40));
		m.put(Material.CARPET, calcRate(5));
		m.put(Material.CLAY, calcRate(3 * 60));
		m.put(Material.CORAL, calcRate(5));
		m.put(Material.DRAGON_EGG, 0);
		m.put(Material.EARTH, calcRate(70));
		m.put(Material.FIRE, Float.MAX_VALUE);
		m.put(Material.GLASS, calcRate(7));
		m.put(Material.GOURD, calcRate(8));
		m.put(Material.ICE, calcRate(2));
		m.put(Material.IRON, IRON_VALUE);
		m.put(Material.LAVA, Float.MAX_VALUE);
		m.put(Material.LEAVES, calcRate(3));
		m.put(Material.MISCELLANEOUS, calcRate(10));
		m.put(Material.NETHER_PLANTS, 0);
		m.put(Material.NETHER_WOOD, 0);
		m.put(Material.OCEAN_PLANT, WATER_VALUE);
		m.put(Material.ORGANIC, calcRate(4));
		m.put(Material.PACKED_ICE, calcRate(8));
		m.put(Material.PISTON, calcRate(2 * 60));
		m.put(Material.PLANTS, calcRate(4));
		m.put(Material.PORTAL, Float.MAX_VALUE);
		m.put(Material.REDSTONE_LIGHT, calcRate(10));
		m.put(Material.ROCK, calcRate(3.7 * 60));
		m.put(Material.SAND, calcRate(70));
		m.put(Material.SEA_GRASS, WATER_VALUE);
		m.put(Material.SHULKER, Float.MAX_VALUE);
		m.put(Material.SNOW, calcRate(0.5));
		m.put(Material.SNOW_BLOCK, calcRate(0.5));
		m.put(Material.SPONGE, calcRate(4));
		m.put(Material.STRUCTURE_VOID, 0);
		m.put(Material.TALL_PLANTS, calcRate(4));
		m.put(Material.TNT, calcRate(80));
		m.put(Material.WATER, WATER_VALUE);
		m.put(Material.WEB, calcRate(7));
		m.put(Material.WOOL, calcRate(2));

		b.put(Blocks.WET_SPONGE, calcRate(10));
		b.put(Blocks.BEDROCK, 0f);
		b.put(Blocks.CAVE_AIR, calcRate(30));

		s.add(makeTagChecker(BlockTags.WOODEN_BUTTONS, calcRate(50)));
		s.add(makePropertyChecker(Blocks.SOUL_CAMPFIRE, CampfireBlock.LIT, Float.MAX_VALUE, true));

		MinecraftForge.EVENT_BUS.post(new HeatRateRegistrationEvent());
	}

	public static float getRateFor(CachedBlockInfo cache) {
		Float value = null;
		for (Function<CachedBlockInfo, Float> pred : HEAT_RATE_SPECIFIC_BLOCK_FUNCTIONS) {
			value = pred.apply(cache);
			if (value != null) {
				return value.floatValue();
			}
		}
		if (cache.getTileEntity() != null) {
			//TODO  check for the HeatProvider capability in tile entity
		}
		value = HEAT_RATE_BLOCK_MAP.get(cache.getBlockState().getBlock());
		if (value == null) {
			value = HEAT_RATE_MATERIAL_MAP.getOrDefault(cache.getBlockState().getMaterial(), DEFAULT);
		}
		return value;
	}

	public static <T extends Comparable<T>> Function<CachedBlockInfo, Float> makePropertyChecker(@Nullable Block block,
			Property<T> property, float value, T... forValue) {
		Predicate<CachedBlockInfo> checker;
		if (block == null) {
			checker = (cbi) -> cbi.getBlockState().hasProperty(property);
		} else {
			checker = (cbi) -> cbi.getBlockState().getBlock() == block;
		}
		Set<T> forValues = Sets.newHashSet(forValue);
		return ((cbi) -> checker.test(cbi) && forValues.contains(cbi.getBlockState().get(property)) ? value : null);
	}

	public static <T extends Comparable<T>> Function<CachedBlockInfo, Float> makeTagChecker(ITag<Block> tag,
			float value) {

		return ((cbi) -> cbi.getBlockState().isIn(tag) ? value : null);
	}

	/**
	 * Calculates heatRate based on the number of seconds it would take for this
	 * material to heat to 100 degrees minecraft-units (fire heat)
	 * 
	 * @param secondsUntilFire
	 * @return
	 */
	public static float calcRate(double secondsUntilFire) {
		return (float) (100 / (secondsUntilFire * 20));
	}

}
