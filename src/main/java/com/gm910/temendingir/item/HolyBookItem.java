package com.gm910.temendingir.item;

import java.util.List;

import com.gm910.temendingir.api.language.Translate;
import com.gm910.temendingir.api.networking.messages.Networking;
import com.gm910.temendingir.world.gods.Deity;
import com.gm910.temendingir.world.gods.cap.DeityData;
import com.gm910.temendingir.world.gods.tasks.TaskOpenHolyBookGui;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LecternBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

public class HolyBookItem extends ModItem {

	public final boolean blank;

	public HolyBookItem(Properties prop, boolean blank) {
		super(prop);
		this.blank = blank;

	}

	/**
	 * Called when this item is used when targetting a Block
	 */
	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		if (blank)
			return ActionResultType.FAIL;
		World world = context.getWorld();
		BlockPos blockpos = context.getPos();
		BlockState blockstate = world.getBlockState(blockpos);
		if (blockstate.isIn(Blocks.LECTERN)) {
			return LecternBlock.tryPlaceBook(world, blockpos, blockstate, context.getItem())
					? ActionResultType.func_233537_a_(world.isRemote)
					: ActionResultType.PASS;
		} else {
			return ActionResultType.PASS;
		}
	}

	/**
	 * Called to trigger the item's "innate" right click behavior. To handle when
	 * this item is used on a Block, see {@link #onItemUse}.
	 */
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		if (blank || worldIn.isRemote)
			return super.onItemRightClick(worldIn, playerIn, handIn);
		ItemStack itemstack = playerIn.getHeldItem(handIn);
		ITextComponent[] text = getContents((ServerPlayerEntity) playerIn, itemstack);
		itemstack.setDisplayName(text[1]);
		Networking.sendToPlayer(new TaskOpenHolyBookGui(text[0], getDisplayName(itemstack), text[2]),
				(ServerPlayerEntity) playerIn);
		return ActionResult.func_233538_a_(itemstack, worldIn.isRemote());
	}

	public static Deity getDeity(MinecraftServer server, ItemStack stack) {
		if (stack.getOrCreateTag().contains("Deity")) {
			return DeityData.get(server).getFromUUID(stack.getTag().getUniqueId("Deity"));
		}
		return null;
	}

	public static void setDeity(Deity deity, ItemStack stack) {
		stack.getOrCreateTag().putUniqueId("Deity", deity.getUuid());

	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
	}

	/**
	 * Call on server returns [] {contents, title, author}
	 * 
	 * @param stack
	 * @return
	 */
	public static ITextComponent[] getContents(ServerPlayerEntity player, ItemStack stack) {
		//TODO figure out this books tuff
		Deity god = getDeity(player.server, stack);
		if (god == null)
			throw new IllegalArgumentException(stack + " has no tag representing a god in " + stack.getTag());
		ITextComponent contents = new StringTextComponent("");
		ITextComponent title = Translate.make("holybook.title", god.getNaming().getReligionName());
		ITextComponent author = new StringTextComponent(god.getName());

		return new ITextComponent[] {};
	}

}
