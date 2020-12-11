package com.gm910.temendingir.blocks.tile;

import com.gm910.temendingir.init.TileInit;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class EntityProjectorTile extends TileEntity {

	private ItemStackHandler handler = new ItemStackHandler(
			NonNullList.from(ItemStack.EMPTY, new ItemStack(Items.PIG_SPAWN_EGG))) {

		@Override
		public int getSlotLimit(int slot) {
			return 1;
		}

		@Override
		protected int getStackLimit(int slot, ItemStack stack) {
			if (!(stack.getItem() instanceof SpawnEggItem)) {
				return 0;
			}
			return super.getStackLimit(slot, stack);
		}

	};

	public EntityProjectorTile() {
		super(TileInit.ENTITY_PROJECTOR.get());
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		return cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? LazyOptional.of(() -> handler).cast()
				: super.getCapability(cap, side);
	}

	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		super.write(nbt);
		nbt.put("Inventory", handler.serializeNBT());
		return nbt;
	}

	@Override
	public void read(BlockState state, CompoundNBT nbt) {
		super.read(state, nbt);
		handler.deserializeNBT(nbt.getCompound("Inventory"));
	}

	@Override
	public CompoundNBT getUpdateTag() {
		return this.write(new CompoundNBT());
	}

	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		return new SUpdateTileEntityPacket(pos, -1, this.getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		super.onDataPacket(net, pkt);

		this.handleUpdateTag(this.getBlockState(), pkt.getNbtCompound());

		this.read(this.getBlockState(), pkt.getNbtCompound());
	}

	public ActionResultType rightClicked(PlayerEntity player, Hand hand, ItemStack with) {
		if (with.getItem() instanceof SpawnEggItem) {
			ItemStack prev = this.handler.getStackInSlot(0);
			player.world.addEntity(
					new ItemEntity(player.world, player.getPosX(), player.getPosY(), player.getPosZ(), prev));
			ItemStack stack = with.copy();
			stack.setCount(1);
			with.shrink(1);
			handler.setStackInSlot(0, stack);
			this.markDirty();
			this.world.notifyBlockUpdate(pos, this.getBlockState(), this.getBlockState(), 2);
			return ActionResultType.SUCCESS;
		}
		return ActionResultType.PASS;
	}

	public static class EntityProjectorRenderer extends TileEntityRenderer<EntityProjectorTile> {

		public EntityProjectorRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
			super(rendererDispatcherIn);
		}

		@Override
		public void render(EntityProjectorTile tileEntityIn, float partialTicks, MatrixStack matrixStackIn,
				IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
			// System.out.println(tileEntityIn.handler.getStackInSlot(0));
			Entity entity = ((SpawnEggItem) tileEntityIn.handler.getStackInSlot(0).getItem())
					.getType(tileEntityIn.handler.getStackInSlot(0).getTag()).create(tileEntityIn.world);
			entity.setMotion(2, 0, 2);
			matrixStackIn.push();
			matrixStackIn.translate(0.5D, 0.0D, 0.5D);
			if (entity != null) {
				matrixStackIn.translate(0.0D, 0.4F, 0.0D);
				matrixStackIn.translate(0.0D, -0.2F, 0.0D);
				Minecraft.getInstance().getRenderManager().renderEntityStatic(entity, 0.0D, 1.0D, 0.0D, 0.0F,
						partialTicks, matrixStackIn, bufferIn,
						0xFFFFFF + Minecraft.getInstance().getRenderManager().getPackedLight(entity, partialTicks));
			}
			matrixStackIn.pop();
			if (!World.isYOutOfBounds(tileEntityIn.pos.down().getY())) {
				Minecraft.getInstance().getBlockRendererDispatcher().renderBlock(
						Minecraft.getInstance().world.getBlockState(tileEntityIn.pos.down()), matrixStackIn, bufferIn,
						combinedLightIn, combinedOverlayIn);
				for (RenderType rtype : RenderType.getBlockRenderTypes()) {
					Minecraft.getInstance().getBlockRendererDispatcher().renderFluid(tileEntityIn.pos.down(),
							Minecraft.getInstance().world, bufferIn.getBuffer(rtype),
							Minecraft.getInstance().world.getBlockState(tileEntityIn.pos.down()).getFluidState());
				}
			}
		}

	}

}
