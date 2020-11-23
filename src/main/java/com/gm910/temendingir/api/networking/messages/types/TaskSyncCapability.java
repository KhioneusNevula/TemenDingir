package com.gm910.temendingir.api.networking.messages.types;

import com.gm910.temendingir.api.networking.messages.ModTask;
import com.gm910.temendingir.api.util.ModReflect;
import com.gm910.temendingir.capabilities.GMCaps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;

public class TaskSyncCapability extends ModTask {

	public INBT capComp;

	public int entity;

	public String capField;

	public ResourceLocation dim;

	public TaskSyncCapability() {

	}

	public <T> TaskSyncCapability(int entityId, String field, LivingEntity en) {
		Capability<T> cap = ModReflect.getField(GMCaps.class, Capability.class, field, null, null);
		this.capComp = cap.writeNBT(en.getCapability(cap).orElse(null), (Direction) null);
		this.entity = en.getEntityId();
		this.dim = en.world.getDimensionKey().getLocation();
		this.capField = field;
	}

	@Override
	public void run() {
		if (this.getWorldRef().getDimensionKey().getLocation().equals(dim)) {
			return;
		}
		Entity e = getWorldRef().getEntityByID(entity);
		if (!(e instanceof LivingEntity)) {
			System.out.println("Tried to sync capability data but seems to have gotten wrong entity");
		}
		LivingEntity entity = (LivingEntity) e;
		Capability cap = ModReflect.getField(GMCaps.class, Capability.class, capField, null, null);
		cap.readNBT(entity.getCapability(cap).orElse(null), null, capComp);

	}

	@Override
	public CompoundNBT write() {
		CompoundNBT nbt = super.write();
		nbt.put("comp", capComp);
		nbt.putInt("entity", entity);
		nbt.putString("dim", dim.toString());
		nbt.putString("field", capField);
		return nbt;
	}

	@Override
	protected void read(CompoundNBT nbt) {
		super.read(nbt);
		capComp = nbt.get("comp");
		entity = nbt.getInt("entity");
		dim = new ResourceLocation(nbt.getString("dim"));
		capField = nbt.getString("field");
	}

	@Override
	public String toString() {
		return super.toString() + ": " + capComp;
	}

}
