package com.gm910.temendingir.entities;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class DilmunItemFrameEntity extends ItemFrameEntity {

	private ItemStack display = ItemStack.EMPTY;

	public DilmunItemFrameEntity(EntityType<? extends ItemFrameEntity> t, World world) {
		super(t, world);
	}

	public DilmunItemFrameEntity(World worldIn, BlockPos pos, Direction facing) {
		super(worldIn, pos, facing);
	}

	@Override
	public ActionResultType processInitialInteract(PlayerEntity player, Hand hand) {
		if (MinecraftForge.EVENT_BUS.post(new DilmunItemFrameClickedEvent(player, this))) {
			return ActionResultType.FAIL;
		}

		return ActionResultType.PASS;
	}

	@Override
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
		this.display = ItemStack.read(compound.getCompound("Stack"));

	}

	@Override
	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);
		compound.put("Stack", this.display.serializeNBT());
	}

	public void setDilmunItem(ItemStack stack) {
		this.display = stack;
	}

	public ItemStack getDilmunItem() {
		return this.display;
	}

	@Override
	public boolean isCustomNameVisible() {
		return true;
	}

}
