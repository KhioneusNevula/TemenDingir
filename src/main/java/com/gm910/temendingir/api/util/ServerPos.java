package com.gm910.temendingir.api.util;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;

import net.minecraft.client.Minecraft;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants.NBT;

public class ServerPos extends BlockPos {

	private ResourceLocation d;

	public ServerPos(GlobalPos source) {
		this(source.getPos(), source.getDimension().getLocation());
	}

	public ServerPos(ServerPos source) {
		super(source);
		this.d = source.d;
	}

	public ServerPos(Entity source) {
		this(source.getPositionVec(), source.getEntityWorld().getDimensionKey().getLocation());
	}

	public ServerPos(TileEntity source) {
		this(source.getPos(), source.getWorld().getDimensionKey().getLocation());
	}

	public ServerPos(Vector3d vec, ResourceLocation d) {
		super(vec);
		this.d = d;
	}

	public ServerPos(IPosition p_i50799_1_, ResourceLocation d) {
		super(p_i50799_1_);
		this.d = d;

	}

	public ServerPos(Vector3i source, ResourceLocation d) {
		super(source);
		this.d = d;
	}

	public ServerPos(Vector3d vec, RegistryKey<World> d) {
		this(vec, d.getLocation());
	}

	public ServerPos(IPosition p_i50799_1_, RegistryKey<World> d) {
		this(p_i50799_1_, d.getLocation());

	}

	public ServerPos(Vector3i source, RegistryKey<World> d) {
		this(source, d.getLocation());
	}

	public ServerPos(int x, int y, int z, ResourceLocation d) {
		super(x, y, z);
		this.d = d;
	}

	public ServerPos(double x, double y, double z, ResourceLocation d) {
		super(x, y, z);
		this.d = d;
	}

	public ServerPos(int x, int y, int z, RegistryKey<World> d) {
		this(x, y, z, d.getLocation());
	}

	public ServerPos(double x, double y, double z, RegistryKey<World> d) {
		this(x, y, z, d.getLocation());
	}

	public ResourceLocation getD() {
		return d;
	}

	public ServerPos setDimension(ResourceLocation d) {
		return new ServerPos(this, d);
	}

	public ServerPos setDimension(RegistryKey<World> d) {
		return new ServerPos(this, d);
	}

	public BlockPos getPos() {
		return new BlockPos(this);
	}

	public BlockPos castToPos() {
		return (BlockPos) this;
	}

	@Override
	public boolean equals(Object p_equals_1_) {
		if (!(p_equals_1_ instanceof ServerPos)) {
			if (p_equals_1_ instanceof GlobalPos) {
				return this.getPos().equals(p_equals_1_)
						&& this.getD().equals(((GlobalPos) p_equals_1_).getDimension().getLocation());
			} else {
				return super.equals(p_equals_1_);
			}
		} else {
			return super.equals(p_equals_1_) && ((ServerPos) p_equals_1_).d == d;
		}
	}

	public ServerPos up(int n) {
		return new ServerPos(super.up(n), d);
	}

	public ServerPos up() {
		return up(1);
	}

	public ServerPos down(int n) {
		return new ServerPos(super.down(n), d);
	}

	public ServerPos down() {
		return down(1);
	}

	public ServerPos north(int n) {
		return new ServerPos(super.north(n), d);
	}

	public ServerPos north() {
		return new ServerPos(super.north(), d);
	}

	public ServerPos south(int n) {
		return new ServerPos(super.south(n), d);
	}

	public ServerPos south() {
		return new ServerPos(super.south(), d);
	}

	public ServerPos east(int n) {
		return new ServerPos(super.east(n), d);
	}

	public ServerPos east() {
		return new ServerPos(super.east(), d);
	}

	public ServerPos west(int n) {
		return new ServerPos(super.west(n), d);
	}

	public ServerPos west() {
		return new ServerPos(super.west(), d);
	}

	public ServerPos offset(Direction facing, int n) {
		return new ServerPos(super.offset(facing, n), d);
	}

	public ServerPos offset(Direction facing) {
		return new ServerPos(super.offset(facing), d);
	}

	public ServerPos add(double x, double y, double z) {
		return new ServerPos(super.add(x, y, z), d);
	}

	public ServerPos add(int x, int y, int z) {
		return new ServerPos(super.add(x, y, z), d);
	}

	public ServerPos add(Vector3i vec) {
		return new ServerPos(super.add(vec), d);
	}

	@Override
	public String getCoordinatesAsString() {
		return super.getCoordinatesAsString() + ", " + d.toString();
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("x", this.getX()).add("y", this.getY()).add("z", this.getZ())
				.add("d", this.getD()).toString();
	}

	public ServerPos toImmutable() {
		return this;
	}

	public ServerPos subtract(Vector3i vec) {
		return new ServerPos(super.subtract(vec), d);
	}

	public ServerPos rotate(Rotation rotationIn) {
		return new ServerPos(super.rotate(rotationIn), d);
	}


	/**
	 * Gets blockpos from nbt OR serverpos depending on whether the nbt is
	 * configured for a serverpos or blockpos
	 * 
	 * @param nbt
	 * @return
	 */
	public static BlockPos bpFromNBT(CompoundNBT nbt) {

		if (nbt.contains("D", NBT.TAG_INT)) {
			return new ServerPos(nbt.getInt("X"), nbt.getInt("Y"), nbt.getInt("Z"), new ResourceLocation(nbt.getString("D")));
		} else {
			return new BlockPos(nbt.getInt("X"), nbt.getInt("Y"), nbt.getInt("Z"));
		}
	}

	/**
	 * If returntype is not a serverpos, returns null;
	 */
	public static ServerPos fromNBT(CompoundNBT nbt) {
		BlockPos pos = bpFromNBT(nbt);
		return pos instanceof ServerPos ? (ServerPos) pos : null;
	}

	/**
	 * Works for blockpos or serverpos
	 * 
	 * @param pos
	 * @return
	 */
	public static CompoundNBT toNBT(BlockPos pos) {
		CompoundNBT nbt = new CompoundNBT();
		nbt.putInt("X", pos.getX());
		nbt.putInt("Y", pos.getY());
		nbt.putInt("Z", pos.getZ());
		if (pos instanceof ServerPos)
			nbt.putString("D", ((ServerPos) pos).getD().toString());
		return nbt;
	}

	public CompoundNBT toNBT() {
		return ServerPos.toNBT(this);
	}

	public ServerWorld getWorld(MinecraftServer server) {
		return server.forgeGetWorldMap().entrySet().stream().filter((e) ->e.getKey().getLocation().equals(d)).map((m) -> m.getValue()).findAny().orElse(null);
	}

	public boolean isClientInWorld(Minecraft mc) {
		return mc.world.getDimensionKey().getLocation().equals(d);
	}

	public static Entity getEntityFromUUID(UUID en, MinecraftServer server) {
		for (ServerWorld world : server.getWorlds()) {
			if (world.getEntityByUuid(en) != null)
				return world.getEntityByUuid(en);
		}
		return null;
	}

	public static Entity getEntityFromUUID(UUID en, World world, BlockPos pos, double range) {
		List<Entity> list = world.getEntitiesWithinAABB(Entity.class, (new AxisAlignedBB(pos)).grow(range),
				(e) -> e.getUniqueID().equals(en));
		if (list.isEmpty())
			return null;
		return list.get(0);
	}

	public static Entity getEntityFromID(int en, ServerWorld server) {

		return server.getEntityByID(en);
	}

	public static <T> T serializeVec3d(Vector3d vec, DynamicOps<T> ops) {

		return ops.createList(
				Lists.newArrayList(ops.createDouble(vec.x), ops.createDouble(vec.y), ops.createDouble(vec.z)).stream());
	}

	public static <T> Vector3d deserializeVec3d(Dynamic<T> dyn) {
		List<Double> ls = dyn.asStream().map((d) -> d.asDouble(0)).collect(Collectors.toList());
		return new Vector3d(ls.get(0), ls.get(1), ls.get(2));
	}

}
