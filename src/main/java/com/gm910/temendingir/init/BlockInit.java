package com.gm910.temendingir.init;

import com.gm910.temendingir.TemenDingir;
import com.gm910.temendingir.blocks.DivineWallBlock;
import com.gm910.temendingir.blocks.FireOfCreationBlock;
import com.gm910.temendingir.blocks.LightBlock;
import com.gm910.temendingir.blocks.ModBlock.BlockRegistryObject;
import com.gm910.temendingir.blocks.PrimordialWaterBlock;

import net.minecraft.block.Block;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockInit {

	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,
			TemenDingir.MODID);

	public static final RegistryObject<Block> PRIMORDIAL_WATER = new BlockRegistryObject("primordial_water",
			PrimordialWaterBlock::new).createRegistryObject();
	public static final RegistryObject<Block> FIRE_OF_CREATION = new BlockRegistryObject("fire_of_creation",
			FireOfCreationBlock::new).createRegistryObject();
	public static final RegistryObject<Block> LIGHT_BLOCK = new BlockRegistryObject("light_block", LightBlock::new)
			.createRegistryObject();
	public static final RegistryObject<Block> DIVINE_WALL = new BlockRegistryObject("divine_wall", DivineWallBlock::new)
			.makeItem(null).createRegistryObject();

}
