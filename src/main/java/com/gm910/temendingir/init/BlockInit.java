package com.gm910.temendingir.init;

import com.gm910.temendingir.TemenDingir;
import com.gm910.temendingir.blocks.AltarOfConsecrationBlock;
import com.gm910.temendingir.blocks.DilmunExitPortal;
import com.gm910.temendingir.blocks.DivineWallBlock;
import com.gm910.temendingir.blocks.EntityProjectorBlock;
import com.gm910.temendingir.blocks.FireOfCreationBlock;
import com.gm910.temendingir.blocks.FireOfWorshipBlock;
import com.gm910.temendingir.blocks.LightBlock;
import com.gm910.temendingir.blocks.ModBlock.BlockRegistryObject;
import com.gm910.temendingir.blocks.PrimordialWaterBlock;
import com.gm910.temendingir.blocks.PylonOfConsecrationBlock;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
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
	public static final RegistryObject<Block> FIRE_OF_WORSHIP = new BlockRegistryObject("fire_of_worship",
			FireOfWorshipBlock::new).createRegistryObject();
	public static final RegistryObject<Block> ALTAR_OF_CONSECRATION = new BlockRegistryObject("altar_of_consecration",
			AltarOfConsecrationBlock::new).createRegistryObject();
	public static final RegistryObject<Block> LIGHT_BLOCK = new BlockRegistryObject("light_block", LightBlock::new)
			.createRegistryObject();
	public static final RegistryObject<Block> DIVINE_WALL = new BlockRegistryObject("divine_wall", DivineWallBlock::new)
			.makeItem(null).createRegistryObject();
	public static final RegistryObject<Block> PYLON_OF_CONSECRATION = new BlockRegistryObject("pylon_of_consecration",
			PylonOfConsecrationBlock::new).makeItem(() -> new Item.Properties().group(ItemGroup.MISC))
					.createRegistryObject();

	public static final RegistryObject<Block> DILMUN_EXIT_PORTAL = new BlockRegistryObject("dilmun_exit_portal",
			DilmunExitPortal::new).createRegistryObject();

	public static final RegistryObject<Block> ENTITY_PROJECTOR = new BlockRegistryObject("entity_projector",
			EntityProjectorBlock::new).makeItem(() -> new Item.Properties().group(ItemGroup.MISC))
					.createRegistryObject();

}
