package com.gm910.temendingir.item;

import com.gm910.temendingir.api.language.Translate;
import com.gm910.temendingir.api.util.BlockInfo;
import com.gm910.temendingir.api.util.ServerPos;
import com.gm910.temendingir.blocks.tile.ILinkTileNexus;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Rarity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

/**
 * Called the "gidru"
 * 
 * @author borah
 *
 */
public class BindingScepterItem extends ModItem {

	public BindingScepterItem() {
		super(new Item.Properties().group(ItemGroup.MISC).isImmuneToFire().maxStackSize(1).rarity(Rarity.UNCOMMON));
	}

	public static ServerPos getNexus(ItemStack stack) {
		return ServerPos.fromNBT(stack.getOrCreateTag().getCompound("Nexus"));
	}

	public static void setNexus(ItemStack stack, ServerPos pos) {
		stack.getOrCreateTag().put("Nexus", pos.toNBT());
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		if (context.getWorld().isRemote)
			return super.onItemUse(context);
		ItemStack stack = context.getItem();
		ServerWorld world = (ServerWorld) context.getWorld();
		BlockPos pos = context.getPos();
		CompoundNBT nbt = stack.getOrCreateTag();
		PlayerEntity player = context.getPlayer();
		if (nbt.contains("Nexus")) {
			ServerPos nexuspos = getNexus(stack);
			System.out.println(nexuspos);
			ILinkTileNexus nexus = (ILinkTileNexus) nexuspos.getWorld(world.getServer()).getTileEntity(nexuspos);

			System.out.println(nexus);
			if (nexus.canLinkTo(new BlockInfo(world, pos)) && nexus.canContinueLinking()) {
				nexus.addLinkedPosition(pos);
				stack.getTag().remove("Nexus");
				player.sendMessage(Translate.make("gidru.linked", "[" + nexuspos.getCoordinatesAsString() + "]",
						"[" + pos.getCoordinatesAsString() + "]"), player.getUniqueID());
			} else {
				return ActionResultType.FAIL;
			}
		} else {
			if (!(world.getTileEntity(pos) instanceof ILinkTileNexus)) {
				return ActionResultType.FAIL;
			}
			setNexus(stack, new ServerPos(pos, world.getDimensionKey()));
			System.out.println(stack.getTag());
			player.sendMessage(Translate.make("gidru.bound", "[" + pos.getCoordinatesAsString() + "]"),
					player.getUniqueID());

		}
		return ActionResultType.SUCCESS;
	}

}
