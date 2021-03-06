package com.gm910.temendingir.init;

import com.gm910.temendingir.TemenDingir;
import com.gm910.temendingir.blocks.LightBlock.LightBlockTile;
import com.gm910.temendingir.blocks.tile.EntityProjectorTile;
import com.gm910.temendingir.blocks.tile.PrimordialWater;
import com.gm910.temendingir.blocks.tile.invokers.AltarOfConsecration;
import com.gm910.temendingir.blocks.tile.invokers.FireOfCreation;
import com.gm910.temendingir.blocks.tile.invokers.FireOfWorship;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.client.registry.ClientRegistry;
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

	public static final RegistryObject<TileEntityType<FireOfWorship>> FIRE_OF_WORSHIP = TILE_TYPES.register(
			"fire_of_worship",
			() -> TileEntityType.Builder.create(FireOfWorship::new, BlockInit.FIRE_OF_WORSHIP.get()).build(null));

	public static final RegistryObject<TileEntityType<AltarOfConsecration>> ALTAR_OF_CONSECRATION = TILE_TYPES
			.register("altar_of_consecration", () -> TileEntityType.Builder
					.create(AltarOfConsecration::new, BlockInit.ALTAR_OF_CONSECRATION.get()).build(null));

	public static final RegistryObject<TileEntityType<LightBlockTile>> LIGHT_BLOCK = TILE_TYPES.register("light_block",
			() -> TileEntityType.Builder.create(LightBlockTile::new, BlockInit.LIGHT_BLOCK.get()).build(null));

	public static final RegistryObject<TileEntityType<EntityProjectorTile>> ENTITY_PROJECTOR = TILE_TYPES
			.register("entity_projector", () -> TileEntityType.Builder
					.create(EntityProjectorTile::new, BlockInit.ENTITY_PROJECTOR.get()).build(null));

	public static void registerTESRs() {
		/*
		 * ClientRegistry.bindTileEntityRenderer(TileInit.WORLD_CONTROLLER.get(),
		 * WorldControllerTESR::new);
		 */
		ClientRegistry.bindTileEntityRenderer(ENTITY_PROJECTOR.get(), EntityProjectorTile.EntityProjectorRenderer::new);
	}

}
