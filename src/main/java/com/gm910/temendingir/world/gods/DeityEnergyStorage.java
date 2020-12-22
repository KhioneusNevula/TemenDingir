package com.gm910.temendingir.world.gods;

import com.gm910.temendingir.api.networking.messages.Networking;
import com.gm910.temendingir.api.util.ServerPos;
import com.gm910.temendingir.world.gods.cap.dilmunmanager.WorshipMethod;
import com.gm910.temendingir.world.gods.event.DeitySacrificeEvent;
import com.gm910.temendingir.world.gods.tasks.TaskSacrificeReceived;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

/**
 * The deity energy storage; locks when deity is not complete
 * 
 * @author borah
 *
 */
public class DeityEnergyStorage {
	protected double energy;
	protected Deity owner;

	public DeityEnergyStorage(double energy, Deity owner) {
		this.energy = Math.max(0, energy);
		this.owner = owner;
	}

	/**
	 * Returns false if the god is not at a stage where it can receive energy
	 * 
	 * @param maxReceive
	 * @return
	 */
	public boolean receiveEnergy(double maxReceive) {

		if (owner.getCreationStage().isComplete()) {
			energy += maxReceive;
			return true;
		}
		return false;
	}

	/**
	 * Returns 0 if successful, 1 if the deity is incomplete and 2 if the event was
	 * canceled (canceled event code takes precedence)
	 * 
	 * @param energy
	 * @return
	 */
	public int makeSacrifice(World world, Entity en, ServerPos at, WorshipMethod method, double energy) {
		DeitySacrificeEvent event = new DeitySacrificeEvent(this.owner, en, method, energy);
		if (MinecraftForge.EVENT_BUS.post(event)) {
			return 2;
		}
		if (!this.receiveEnergy(energy)) {

			Networking.sendToChunk(new TaskSacrificeReceived(at, energy, true), world.getChunkAt(at));
			System.out.println(owner.getName() + " failed to gain " + energy + " and still has " + getEnergyStored()
					+ " from " + en.getDisplayName().getString());
			return 1;
		}

		Networking.sendToChunk(new TaskSacrificeReceived(at, energy, false), world.getChunkAt(at));
		System.out.println(owner.getName() + " gained " + energy + " and now has " + getEnergyStored() + " from "
				+ en.getDisplayName().getString());
		return 0;
	}

	/**
	 * Return 0 if the god has no energy or is not at a stage where it can receive
	 * energy
	 * 
	 * @param maxExtract
	 * @param simulate
	 * @return
	 */
	public double extractEnergy(double maxExtract, boolean simulate) {
		double energyExtracted = Math.min(energy, maxExtract);
		if (!simulate || owner.getCreationStage().isComplete()) {
			energy -= energyExtracted;
		}
		return owner.getCreationStage().isComplete() ? energyExtracted : 0;
	}

	public double getEnergyStored() {
		return energy;
	}

	public void forceSetEnergyStored(double energy) {
		this.energy = Math.max(0, energy);
	}

	public Deity getOwner() {
		return owner;
	}

}
