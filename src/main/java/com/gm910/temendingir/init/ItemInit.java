package com.gm910.temendingir.init;

import com.gm910.temendingir.TemenDingir;
import com.gm910.temendingir.item.BindingScepterItem;
import com.gm910.temendingir.item.GodListerItem;
import com.gm910.temendingir.item.RandomGodGeneratorItem;

import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class ItemInit {
	private ItemInit() {
	}

	public static final ITag.INamedTag<Item> SPECIAL_PLANTS_TAG = ItemTags
			.makeWrapperTag(TemenDingir.MODID + ":special_plants");

	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
			TemenDingir.MODID);

	public static final RegistryObject<Item> GIDRU = ITEMS.register("gidru", BindingScepterItem::new);

	public static final RegistryObject<Item> RANDOM_GOD_GENERATOR = ITEMS.register("random_god_generator",
			RandomGodGeneratorItem::new);

	public static final RegistryObject<Item> GOD_LISTER = ITEMS.register("god_lister", GodListerItem::new);
	//public static final RegistryObject<Item> THERMOMETER = ITEMS.register("thermometer", ThermometerItem::new);

	public static void registerISTERs() {

	}

}
