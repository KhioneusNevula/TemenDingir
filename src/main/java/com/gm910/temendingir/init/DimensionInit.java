package com.gm910.temendingir.init;

import com.gm910.temendingir.TemenDingir;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class DimensionInit {

	public static final RegistryKey<World> DILMUN = RegistryKey.getOrCreateKey(Registry.WORLD_KEY,
			new ResourceLocation(TemenDingir.MODID, "dilmun"));
}
