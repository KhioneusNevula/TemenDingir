package com.gm910.temendingir.item;

import java.util.Random;

import com.gm910.temendingir.api.language.NamePhonemicHelper;
import com.gm910.temendingir.api.util.ServerPos;
import com.gm910.temendingir.world.gods.Deity;
import com.gm910.temendingir.world.gods.Deity.NamingConvention;
import com.gm910.temendingir.world.gods.cap.DeityData;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Items;
import net.minecraft.item.Rarity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.registries.ForgeRegistries;

public class RandomGodGeneratorItem extends ModItem {

	public RandomGodGeneratorItem() {
		super(new Item.Properties().maxStackSize(1).group(ItemGroup.TOOLS).rarity(Rarity.EPIC));
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		if (context.getWorld().isRemote)
			return super.onItemUse(context);

		DeityData data = DeityData.get(context.getWorld().getServer());

		BlockPos pos = context.getPos();
		String name = NamePhonemicHelper.generateName(context.getWorld().rand).toString();
		String religionPrefix = NamePhonemicHelper.generateName(context.getWorld().rand).toString();
		String religionName = religionPrefix + "ism";
		String followerName = religionPrefix + "ist";
		String followerPluralName = religionPrefix + "ists";
		NamingConvention convention = new NamingConvention(religionName, followerName, followerPluralName);

		Deity deity = new Deity(name, convention, context.getPlayer().getUniqueID());
		Item[] items = new Item[4];
		while (items[0] == null || data.getFromItems(items) != null) {
			for (int i = 0; i < 4; i++) {
				items[i] = (ForgeRegistries.ITEMS.getValues()).stream()
						.filter((e) -> e != Items.AIR && e.getRarity(new ItemStack(e)) != Rarity.EPIC)
						.sorted((e, m) -> (new Random()).nextInt(2) - 1).findAny().orElse(Items.STONE);
			}
		}
		deity.getInvocation().addAll(items);
		Deity d = data.getFromFollowerUUID(context.getPlayer().getUniqueID());
		if (d != null) {
			d.removeFollower(context.getPlayer().getUniqueID());
		}
		DeityData.get(context.getWorld().getServer()).birthDeity(deity,
				new ServerPos(pos, context.getWorld().getDimensionKey()));
		return super.onItemUse(context);
	}

}
