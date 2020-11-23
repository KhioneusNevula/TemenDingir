package com.gm910.temendingir.init;

import com.gm910.temendingir.TemenDingir;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;

public class BiomeInit {

	public static final RegistryKey<Biome> DILMUN = RegistryKey.getOrCreateKey(Registry.BIOME_KEY,
			new ResourceLocation(TemenDingir.MODID, "biome"));
}
