package com.gm910.temendingir.init;

import com.gm910.temendingir.TemenDingir;

import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class StructureInit {

	public static final DeferredRegister<Structure<?>> STRUCTURES = DeferredRegister
			.create(ForgeRegistries.STRUCTURE_FEATURES, TemenDingir.MODID);
	/*
	 * public static final DilmunStructure DILMUN_DESIGNER_DEFAULT_STRUCTURE = new
	 * DilmunStructure( DilmunStructure.DEITY_FIELD_CODEC);
	 */

	/*
	 * public static final RegistryObject<Structure<?>> DILMUN_DESIGNER =
	 * STRUCTURES.register("dilmun_designer", () -> new
	 * DilmunStructure(DilmunStructure.DEITY_FIELD_CODEC));
	 */
}
