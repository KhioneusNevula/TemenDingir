package com.gm910.temendingir.blocks.tile.invokers;

import java.util.UUID;

import com.gm910.temendingir.world.gods.Deity;
import com.gm910.temendingir.world.gods.cap.DeityData;

import net.minecraft.block.BlockState;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPattern.PatternHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent.NeighborNotifyEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public abstract class InvokerTileEntity extends TileEntity {

	public InvokerTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	protected Deity invoked;
	protected UUID invokedId;

	public void setInvoked(Deity invoked) {
		this.invoked = invoked;
	}

	public Deity getInvoked() {
		return invoked;
	}

	@Override
	public void read(BlockState state, CompoundNBT nbt) {
		super.read(state, nbt);
		if (nbt.hasUniqueId("DeityID"))
			invokedId = nbt.getUniqueId("DeityID");
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		CompoundNBT nbt = super.write(compound);
		if (this.invoked != null)
			nbt.putUniqueId("DeityID", this.invoked.getUuid());
		return nbt;
	}

	@Override
	public void onLoad() {
		super.onLoad();
		if (!this.world.isRemote && this.invokedId != null) {
			this.invoked = DeityData.get(world.getServer()).getFromUUID(invokedId);
		}
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void remove() {
		super.remove();
		MinecraftForge.EVENT_BUS.unregister(this);
	}

	/**
	 * Called when the tileEntity's structure no longer supports it
	 */
	protected void revertState() {

	}

	/**
	 * Returns the pattern that senses the complete structure
	 * 
	 * @return
	 */
	protected abstract BlockPattern getCompletedPattern();

	@SubscribeEvent
	public void onUpdate(NeighborNotifyEvent event) {
		if (world.isRemote)
			return;
		PatternHelper altar = getCompletedPattern().match(world, pos);
		boolean break1 = false;
		if (altar == null) {
			break1 = true;
		} else {

			if (!break1) {
				Deity d = (DeityData.get(world.getServer()).getFromItemFrames(
						InvokerTileHelper.getItemFrames(getCompletedPattern(), altar, Direction.UP, null)));
				if (this.invoked != null && d != this.invoked) {

					break1 = true;
				}
			}

		}
		if (break1) {
			revertState();
		} else {
		}
	}

	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		return new SUpdateTileEntityPacket(pos, -1, write(new CompoundNBT()));
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		super.onDataPacket(net, pkt);
		read(this.getBlockState(), pkt.getNbtCompound());
	}

}
