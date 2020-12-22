package com.gm910.temendingir.world.temperature;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.FireBlock;
import net.minecraft.block.material.Material;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.Property;
import net.minecraft.tags.ITag;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.util.CachedBlockInfo;
import net.minecraftforge.common.MinecraftForge;

/**
 * This handles the amount of heat that certain blocks give off continuously in
 * addition to their natural heat; this amount is slightly randomized. If the
 * amount should not be randomized, give a negative number
 * 
 * @author borah
 *
 */
public class HeatEmitterHandler {

	public static final float DEFAULT = 0;

	/**
	 * The default heat rates for given materials
	 */
	public static final Map<Material, Float> HEAT_EMISSION_MATERIAL_MAP = new HashMap<>();

	/**
	 * The default heat rates for given blocks
	 */
	public static final Map<Block, Float> HEAT_EMISSION_BLOCK_MAP = new HashMap<>();

	/**
	 * Specific functions that calculate a value for specific blocks, such as
	 * checking if a Furnace is lit; return null if not applicable
	 */
	public static final Set<Function<CachedBlockInfo, Float>> HEAT_EMISSION_SPECIFIC_BLOCK_FUNCTIONS = new HashSet<>();

	public static void initTemperatureValues() {
		Map<Material, Float> m = HEAT_EMISSION_MATERIAL_MAP;
		Map<Block, Float> b = HEAT_EMISSION_BLOCK_MAP;
		Set<Function<CachedBlockInfo, Float>> s = HEAT_EMISSION_SPECIFIC_BLOCK_FUNCTIONS;

		m.put(Material.LAVA, 500f);
		m.put(Material.DRAGON_EGG, 5f);
		//m.put(Material.STRUCTURE_VOID, 0);

		b.put(Blocks.TORCH, 10f);
		//b.put(Blocks.SOUL_FIRE, 0f);
		b.put(Blocks.NETHER_PORTAL, 50f);
		//b.put(Blocks.END_PORTAL, 0f);
		b.put(Blocks.MAGMA_BLOCK, 100f);
		b.put(Blocks.GLOWSTONE, 30f);

		s.add(makeFireAgeChecker());
		s.add(makeFurnaceChecker());
		s.add(makeCampfireChecker());

		MinecraftForge.EVENT_BUS.post(new HeatEmissionRegistrationEvent());
	}

	public static float getEmissionFor(CachedBlockInfo cache) {
		Float value = null;
		for (Function<CachedBlockInfo, Float> pred : HEAT_EMISSION_SPECIFIC_BLOCK_FUNCTIONS) {
			value = pred.apply(cache);
			if (value != null) {
				return value.floatValue();
			}
		}
		if (cache.getTileEntity() != null) {
			//TODO  check for the HeatProvider capability in tile entity
		}
		value = HEAT_EMISSION_BLOCK_MAP.get(cache.getBlockState().getBlock());
		if (value == null) {
			value = HEAT_EMISSION_MATERIAL_MAP.getOrDefault(cache.getBlockState().getMaterial(), DEFAULT);
		}
		return value;
	}

	public static <T extends Comparable<T>> Function<CachedBlockInfo, Float> makePropertyChecker(@Nullable Block block,
			Property<T> property, T forValue, float value) {
		Predicate<CachedBlockInfo> checker;
		if (block == null) {
			checker = (cbi) -> cbi.getBlockState().hasProperty(property);
		} else {
			checker = (cbi) -> cbi.getBlockState().getBlock() == block;
		}
		return ((cbi) -> checker.test(cbi) && cbi.getBlockState().get(property).equals(forValue) ? value : null);
	}

	public static Function<CachedBlockInfo, Float> makeFireAgeChecker() {

		return ((cbi) -> {

			BlockState state = cbi.getBlockState();
			if (state.getBlock() != Blocks.FIRE)
				return null;

			int age = state.get(FireBlock.AGE) + 1;
			return Math.max(age, 5) * 20f;

		});
	}

	public static Function<CachedBlockInfo, Float> makeFurnaceChecker() {

		return ((cbi) -> {

			BlockState state = cbi.getBlockState();
			if (!(state.getBlock() instanceof AbstractFurnaceBlock))
				return DEFAULT;
			if (!(state.get(AbstractFurnaceBlock.LIT)))
				return DEFAULT;
			AbstractFurnaceTileEntity furnace = (AbstractFurnaceTileEntity) cbi.getTileEntity();
			float burntime = furnace.write(new CompoundNBT()).getInt("BurnTime") / 100f;
			float heat = -3f / (burntime + 0.3f) + 10f;
			return heat;

		});
	}

	public static Function<CachedBlockInfo, Float> makeCampfireChecker() {
		return (cbi) -> {

			BlockState state = cbi.getBlockState();
			if (!(state.getBlock() instanceof CampfireBlock))
				return null;
			if (state.getBlock() == Blocks.SOUL_CAMPFIRE && state.get(CampfireBlock.LIT))
				return 0f;
			return state.get(CampfireBlock.LIT) ? (state.get(CampfireBlock.SIGNAL_FIRE) ? 150f : 100f) : null;
		};
	}

	public static <T extends Comparable<T>> Function<CachedBlockInfo, Float> makeTagChecker(ITag<Block> tag,
			float value) {

		return ((cbi) -> cbi.getBlockState().isIn(tag) ? value : null);
	}

	public static enum HeatType {
		RAISE_TO, COOL_TO, MAINTAIN
	}

}
