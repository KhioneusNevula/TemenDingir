package com.gm910.temendingir.init;

import com.gm910.temendingir.TemenDingir;
import com.gm910.temendingir.blocks.LightBlock.LightBlockTile;
import com.gm910.temendingir.blocks.tile.FireOfCreation;
import com.gm910.temendingir.blocks.tile.PrimordialWater;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class TileInit {
	private TileInit() {
	}

	public static final DeferredRegister<TileEntityType<?>> TILE_TYPES = DeferredRegister
			.create(ForgeRegistries.TILE_ENTITIES, TemenDingir.MODID);

	public static final RegistryObject<TileEntityType<PrimordialWater>> PRIMORDIAL_WATER = TILE_TYPES.register(
			"primordial_water",
			() -> TileEntityType.Builder.create(PrimordialWater::new, BlockInit.PRIMORDIAL_WATER.get()).build(null));

	public static final RegistryObject<TileEntityType<FireOfCreation>> FIRE_OF_CREATION = TILE_TYPES.register(
			"fire_of_creation",
			() -> TileEntityType.Builder.create(FireOfCreation::new, BlockInit.FIRE_OF_CREATION.get()).build(null));

	public static final RegistryObject<TileEntityType<LightBlockTile>> LIGHT_BLOCK = TILE_TYPES.register("light_block",
			() -> TileEntityType.Builder.create(LightBlockTile::new, BlockInit.LIGHT_BLOCK.get()).build(null));

	public static void registerTESRs() {
		/*
		 * ClientRegistry.bindTileEntityRenderer(TileInit.WORLD_CONTROLLER.get(),
		 * WorldControllerTESR::new);
		 */
	}

}
