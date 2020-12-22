package com.gm910.temendingir.item;

import com.gm910.temendingir.api.language.Translate;
import com.gm910.temendingir.world.temperature.Temperatures;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;

public class ThermometerItem extends ModItem {

	public ThermometerItem() {
		super(new Item.Properties().maxStackSize(1).group(ItemGroup.TOOLS));
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		if (context.getWorld().isRemote)
			return super.onItemUse(context);

		Temperatures temps = Temperatures.get(context.getWorld().getChunkAt(context.getPos()));
		context.getPlayer()
				.sendStatusMessage(Translate.make("temperature.read", temps.getTemperatureAt(context.getPos())), true);
		return super.onItemUse(context);
	}

}
