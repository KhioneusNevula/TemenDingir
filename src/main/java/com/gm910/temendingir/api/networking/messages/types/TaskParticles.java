package com.gm910.temendingir.api.networking.messages.types;

import com.gm910.temendingir.api.networking.messages.ModTask;
import com.gm910.temendingir.api.util.GMNBT;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

public class TaskParticles extends ModTask {

	private Vector3d pos;
	private ResourceLocation dim;
	private IParticleData type;
	private Vector3d speed;
	private boolean optional;
	private boolean force;
	private boolean ignoreDistance;

	public TaskParticles() {
	}

	/**
	 * 
	 */
	public TaskParticles(IParticleData type, double x, double y, double z, ResourceLocation dim, double xs, double ys,
			double zs, boolean optional, boolean force, boolean ignoreDist) {
		this.type = type;

		this.pos = new Vector3d(x, y, z);
		this.dim = dim;
		this.speed = new Vector3d(xs, ys, zs);
		this.optional = optional;
		this.force = force;
		this.ignoreDistance = ignoreDist;
	}

	@Override
	public void run() {

		World world = getWorldRef();
		if (world != null) {
			if (world.isRemote) {
				if (world.getDimensionKey().getLocation().equals(dim)) {
					if (optional) {
						world.addOptionalParticle(type, ignoreDistance, pos.x, pos.y, pos.z, speed.x, speed.y, speed.z);
					} else {
						world.addParticle(type, force, pos.x, pos.y, pos.z, speed.x, speed.y, speed.z);
					}

				}
			}
		}
	}

	@Override
	public CompoundNBT write() {
		CompoundNBT data = new CompoundNBT();
		data.putString("Particle", type.getType().getRegistryName().toString());
		data.putString("Params", type.getParameters());
		data.putString("Dim", this.dim.toString());
		data.put("Pos", GMNBT.writeVec3d(pos));
		data.putBoolean("Op", this.optional);
		data.putBoolean("Force", this.force);
		data.putBoolean("Ign", this.ignoreDistance);
		data.put("Speed", GMNBT.writeVec3d(this.speed));
		return data;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void read(CompoundNBT nbt) {

		ParticleType type = ForgeRegistries.PARTICLE_TYPES.getValue(new ResourceLocation(nbt.getString("Particle")));
		com.mojang.brigadier.StringReader reader = new com.mojang.brigadier.StringReader(nbt.getString("Params"));
		IParticleData dat = null;
		try {
			dat = type.getDeserializer().deserialize(type, reader);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		this.type = dat;
		this.dim = new ResourceLocation(nbt.getString("Dim"));
		this.pos = GMNBT.readVec3d(nbt.getCompound("Pos"));
		this.speed = GMNBT.readVec3d(nbt.getCompound("speed"));
		this.optional = nbt.getBoolean("Op");
		this.force = nbt.getBoolean("Force");
		this.ignoreDistance = nbt.getBoolean("Ign");

	}

	@Override
	public boolean isLClient() {

		return true;
	}

	@Override
	public boolean isLServer() {
		return false;
	}

	@Override
	public String toString() {
		return super.toString() + ": " + this.type.getType().getRegistryName() + " with params "
				+ this.type.getParameters() + " at " + this.pos;
	}

}
