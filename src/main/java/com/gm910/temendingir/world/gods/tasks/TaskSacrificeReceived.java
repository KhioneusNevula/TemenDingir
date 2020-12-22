package com.gm910.temendingir.world.gods.tasks;

import java.util.Random;
import java.util.function.DoubleSupplier;

import com.gm910.temendingir.api.networking.messages.ModTask;
import com.gm910.temendingir.api.util.ServerPos;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.world.World;

public class TaskSacrificeReceived extends ModTask {

	private ServerPos pos;
	private double amount;
	private boolean failed;

	public TaskSacrificeReceived() {
	}

	public TaskSacrificeReceived(ServerPos pos, double amount, boolean failed) {
		this.pos = pos;
		this.amount = amount;
		this.failed = failed;
	}

	@Override
	public void run() {
		if (this.getWorldRef() == null) {
			return;
		}
		if (this.getWorldRef().getDimensionKey() == pos.getDKey()) {
			int times = (int) amount;
			for (int i = 0; i < times; i++) {
				World world = this.getWorldRef();
				Random rand = world.rand;
				DoubleSupplier sup = () -> rand.nextDouble() - 0.5;
				if (!failed) {
					world.addParticle(ParticleTypes.SOUL_FIRE_FLAME, pos.getX() + 0.5 + sup.getAsDouble(),
							pos.getY() + 0.5 + sup.getAsDouble(), pos.getZ() + 0.5 + sup.getAsDouble(),
							sup.getAsDouble() * 0.1, sup.getAsDouble(), sup.getAsDouble() * 0.1);
				} else {

					world.addParticle(ParticleTypes.ANGRY_VILLAGER, pos.getX() + 0.5 + sup.getAsDouble(),
							pos.getY() + 0.5 + sup.getAsDouble(), pos.getZ() + 0.5 + sup.getAsDouble(),
							sup.getAsDouble() * 0.1, sup.getAsDouble(), sup.getAsDouble() * 0.1);
				}
			}
		}
	}

	public ServerPos getPos() {
		return pos;
	}

	public double getAmount() {
		return amount;
	}

	@Override
	public CompoundNBT write() {
		CompoundNBT data = new CompoundNBT();
		data.putDouble("A", amount);
		data.put("P", this.pos.toNBT());
		data.putBoolean("F", this.failed);
		return data;
	}

	@Override
	protected void read(CompoundNBT nbt) {
		this.amount = nbt.getDouble("A");
		this.pos = ServerPos.fromNBT(nbt.getCompound("P"));
		this.failed = nbt.getBoolean("F");
	}

}
