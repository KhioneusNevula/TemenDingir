package com.gm910.temendingir.api.util;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.CollectionNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.nbt.NumberNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.server.ServerWorld;

public class GMNBT {

	public static <T> ListNBT makeList(Iterable<T> iter, Function<T, INBT> serializer) {
		ListNBT ls = new ListNBT();
		for (T ob : iter) {
			ls.add(serializer.apply(ob));
		}
		return ls;
	}

	public static ListNBT makePosList(Iterable<? extends Vector3i> iter) {
		return makeList(iter, (s) -> (ServerPos.toNBT(s)));
	}

	public static ListNBT makeUUIDList(Iterable<UUID> iter) {
		return makeList(iter, (s) -> {
			CompoundNBT tg = new CompoundNBT();
			tg.putUniqueId("ID", s);
			return tg;
		});
	}

	public static ListNBT makeUUIDListFromEntities(Iterable<? extends Entity> iter) {
		return makeList(iter, (s) -> {
			CompoundNBT tg = new CompoundNBT();
			tg.putUniqueId("ID", s.getUniqueID());
			return tg;
		});
	}

	/**
	 * Returns list of CompoundNBTs with integers
	 * 
	 * @param iter
	 * @return
	 */
	public static ListNBT makeIDListFromEntities(Iterable<? extends Entity> iter) {
		return makeList(iter, (s) -> {
			CompoundNBT tg = new CompoundNBT();
			tg.putInt("ID", s.getEntityId());
			return tg;
		});
	}

	/**
	 * Returns list of Ints
	 * 
	 * @param iter
	 * @return
	 */
	public static ListNBT makeIntListFromEntities(Iterable<? extends Entity> iter) {
		return makeList(iter, (s) -> {
			return IntNBT.valueOf(s.getEntityId());
		});
	}

	/**
	 * Returns list of Ints
	 * 
	 * @param iter
	 * @return
	 */
	public static IntArrayNBT makeIntArrayFromEntities(Iterable<? extends Entity> iter) {
		List<Integer> ls = new ArrayList<>();
		for (Entity e : iter) {
			ls.add(e.getEntityId());
		}
		return new IntArrayNBT(ls);
	}

	public static List<Entity> createEntityListFromUUIDs(ListNBT ls, MinecraftServer server) {
		return createList(ls, (b) -> {
			return ServerPos.getEntityFromUUID(((CompoundNBT) b).getUniqueId("ID"), server);
		});
	}

	public static <T extends Entity> List<T> createEntityListFromUUIDs(ListNBT ls, ServerWorld server) {
		return createList(ls, (b) -> {
			return (T) server.getEntityByUuid(((CompoundNBT) b).getUniqueId("ID"));
		});
	}

	public static List<Entity> createEntityListFromIDs(CollectionNBT<?> ls, ServerWorld server) {
		return createList(ls, (b) -> {
			if (b instanceof NumberNBT) {
				return ServerPos.getEntityFromID(((NumberNBT) b).getInt(), server);
			} else {
				return ServerPos.getEntityFromID(((CompoundNBT) b).getInt("ID"), server);
			}

		});
	}

	public static List<UUID> createUUIDList(ListNBT ls) {
		return createList(ls, (b) -> {
			return ((CompoundNBT) b).getUniqueId("ID");
		});
	}

	public static void forEach(ListNBT ls, Consumer<INBT> cons) {
		for (INBT base : ls) {
			cons.accept(base);
		}
	}

	public static <R extends INBT, T> List<T> createList(CollectionNBT<R> ls, Function<R, T> func) {
		List<T> lws = new ArrayList<>();
		if (ls == null) {
			throw new IllegalArgumentException("Null NBTTagList");
		}
		for (R b : ls) {
			lws.add(func.apply(b));
		}
		return lws;
	}

	public static <T extends Vector3i> List<T> createPosList(ListNBT ls) {
		return createList(ls, (b) -> (T) ServerPos.fromNBT((CompoundNBT) b));
	}

	public static List<BlockPos> createServerPosList(ListNBT ls) {
		return createList(ls, (b) -> ServerPos.fromNBT((CompoundNBT) b));
	}

	public static <T, K> Map<T, K> createMap(ListNBT ls, Function<INBT, T> keyFunc, Function<INBT, K> valFunc) {
		Map<T, K> lws = new HashMap<>();

		for (INBT b : ls) {

			lws.put(keyFunc.apply(b), valFunc.apply(b));
		}
		return lws;
	}

	public static <T, K> Map<T, K> createMap(ListNBT ls, Function<INBT, Pair<T, K>> func) {
		Map<T, K> lws = new HashMap<>();

		for (INBT b : ls) {

			Pair<T, K> p = func.apply(b);

			lws.put(p.getFirst(), p.getSecond());
		}
		return lws;
	}

	public static CompoundNBT writeVec3d(Vector3d vec) {
		CompoundNBT nbt = new CompoundNBT();
		nbt.putDouble("X", vec.x);
		nbt.putDouble("Y", vec.y);
		nbt.putDouble("Z", vec.z);
		return nbt;
	}

	public static Vector3d readVec3d(CompoundNBT nbt) {
		return new Vector3d(nbt.getDouble("X"), nbt.getDouble("Y"), nbt.getDouble("Z"));
	}

	public static Dynamic<INBT> makeDynamic(INBT data) {
		return new Dynamic<>(NBTDynamicOps.INSTANCE, data);
	}

	public static CompoundNBT rectangleToNBT(Rectangle rect) {
		CompoundNBT nbt = new CompoundNBT();
		nbt.putInt("X", (int) rect.getMinX());
		nbt.putInt("Y", (int) rect.getMinY());
		nbt.putInt("W", (int) rect.getWidth());
		nbt.putInt("H", (int) rect.getHeight());
		return nbt;
	}

	public static Rectangle rectangleFromNBT(CompoundNBT nbt) {
		return new Rectangle(nbt.getInt("X"), nbt.getInt("Y"), nbt.getInt("W"), nbt.getInt("H"));
	}

}
