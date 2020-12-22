package com.gm910.temendingir.api.util;

import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class ServerSensitiveServerPos extends ServerPos {

	private MinecraftServer server;

	public ServerSensitiveServerPos(GlobalPos source, MinecraftServer server) {
		super(source);
		this.server = server;
	}

	public ServerSensitiveServerPos(ServerPos source, MinecraftServer server) {
		super(source);
		this.server = server;
	}

	public ServerSensitiveServerPos(Entity source) {
		super(source);
		this.server = source.getServer();
	}

	public ServerSensitiveServerPos(Entity source, boolean lastTick) {
		super(source, lastTick);
		this.server = source.getServer();
	}

	public ServerSensitiveServerPos(TileEntity source) {
		super(source);
		server = source.getWorld().getServer();
	}

	public ServerSensitiveServerPos(Vector3d vec, World d) {
		super(vec, d);
		this.server = d.getServer();
	}

	public ServerSensitiveServerPos(IPosition p_i50799_1_, World d) {
		super(p_i50799_1_, d);
		this.server = d.getServer();
	}

	public ServerSensitiveServerPos(Vector3i source, World d) {
		super(source, d);
		this.server = d.getServer();
	}

	public ServerSensitiveServerPos(int x, int y, int z, World d) {
		super(x, y, z, d);
		this.server = d.getServer();
	}

	public ServerSensitiveServerPos(double x, double y, double z, World d) {
		super(x, y, z, d);
		this.server = d.getServer();
	}

	public MinecraftServer getServer() {
		return server;
	}

	public ServerSensitiveServerPos setServer(MinecraftServer server) {
		return new ServerSensitiveServerPos(this, server);
	}

	public ServerWorld getWorld() {
		return super.getWorld(server);
	}

	@Override
	public ServerSensitiveServerPos add(double x, double y, double z) {
		return new ServerSensitiveServerPos(super.add(x, y, z), server);
	}

	@Override
	public ServerSensitiveServerPos add(int x, int y, int z) {
		return new ServerSensitiveServerPos(super.add(x, y, z), server);
	}

	@Override
	public ServerSensitiveServerPos add(Vector3i vec) {
		return new ServerSensitiveServerPos(super.add(vec), server);
	}

	public ServerPos castToServerPos() {
		return this;
	}

	public ServerPos getServerPos() {
		return new ServerPos(this);
	}

	@Override
	public ServerSensitiveServerPos down() {
		return new ServerSensitiveServerPos(super.down(), server);
	}

	@Override
	public ServerSensitiveServerPos down(int n) {
		return new ServerSensitiveServerPos(super.down(n), server);
	}

	@Override
	public ServerSensitiveServerPos west(int n) {
		return new ServerSensitiveServerPos(super.west(n), server);
	}

	@Override
	public ServerSensitiveServerPos west() {
		return new ServerSensitiveServerPos(super.west(), server);
	}

	@Override
	public ServerSensitiveServerPos up() {
		return super.up().makeServerSensitive(server);
	}

	@Override
	public ServerSensitiveServerPos up(int n) {
		return super.up(n).makeServerSensitive(server);
	}

	@Override
	public ServerSensitiveServerPos north() {
		return super.north().makeServerSensitive(server);
	}

	@Override
	public ServerSensitiveServerPos north(int n) {
		return super.north(n).makeServerSensitive(server);
	}

	@Override
	public ServerSensitiveServerPos south() {
		return super.south().makeServerSensitive(server);
	}

	@Override
	public ServerSensitiveServerPos south(int n) {
		return super.south(n).makeServerSensitive(server);
	}

	@Override
	public ServerSensitiveServerPos east() {
		return super.east().makeServerSensitive(server);
	}

	@Override
	public ServerSensitiveServerPos east(int n) {
		return super.east(n).makeServerSensitive(server);
	}

	@Override
	public ServerSensitiveServerPos subtract(Vector3i vec) {
		return super.subtract(vec).makeServerSensitive(server);
	}

	@Override
	public ServerSensitiveServerPos rotate(Rotation rotationIn) {
		return super.rotate(rotationIn).makeServerSensitive(server);
	}

	@Override
	public ServerSensitiveServerPos setDimension(RegistryKey<World> d) {
		return super.setDimension(d).makeServerSensitive(server);
	}

	@Override
	public ServerSensitiveServerPos setDimension(ResourceLocation d) {
		return super.setDimension(d).makeServerSensitive(server);
	}

	@Override
	public ServerSensitiveServerPos offset(Direction facing) {
		return super.offset(facing).makeServerSensitive(server);
	}

	@Override
	public ServerSensitiveServerPos offset(Direction facing, int n) {
		return super.offset(facing, n).makeServerSensitive(server);
	}

	/**
	 * gets world from given server; no change in functionality from
	 * {@link ServerPos#getWorld(MinecraftServer)}
	 */
	@Override
	public ServerWorld getWorld(MinecraftServer server) {
		return super.getWorld(server);
	}
}
