package com.gm910.temendingir.item;

import java.util.stream.Collectors;

import com.gm910.temendingir.api.language.Translate;
import com.gm910.temendingir.world.gods.Deity;
import com.gm910.temendingir.world.gods.cap.DeityData;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;

public class GodListerItem extends ModItem {

	public GodListerItem() {
		super(new Item.Properties().maxStackSize(1).group(ItemGroup.TOOLS));
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		if (context.getWorld().isRemote)
			return super.onItemUse(context);

		DeityData data = DeityData.get(context.getWorld().getServer());
		for (Deity deity : data.getDeities()) {
			context.getPlayer()
					.sendStatusMessage(Translate.make("deity.info." + deity.getPronounCode(), deity.getName(),
							deity.getNaming().getReligionName(),
							deity.getInvocation().getItems().stream()
									.map((item) -> item.getDisplayName(new ItemStack(item)).getString())
									.collect(Collectors.toSet()),
							deity.getEnergyStored()), false);
		}
		return super.onItemUse(context);
	}

}
