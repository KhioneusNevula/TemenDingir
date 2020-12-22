package com.gm910.temendingir.world.gods.event;

import com.gm910.temendingir.world.gods.Deity;
import com.gm910.temendingir.world.gods.cap.dilmunmanager.WorshipMethod;

import net.minecraft.entity.Entity;
import net.minecraftforge.eventbus.api.Cancelable;

/**
 * Only call this server-side!
 * 
 * @author borah
 *
 */
@Cancelable
public class DeitySacrificeEvent extends DeityEvent {

	private final Entity entity;
	private final WorshipMethod method;
	private double amount;

	public DeitySacrificeEvent(Deity deity, Entity entity, WorshipMethod method, double amount) {
		super(deity);
		this.entity = entity;
		this.method = method;
		this.amount = amount;
	}

	public Entity getEntity() {
		return entity;
	}

	public WorshipMethod getMethod() {
		return method;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

}
