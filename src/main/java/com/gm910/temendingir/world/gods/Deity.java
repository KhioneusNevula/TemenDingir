package com.gm910.temendingir.world.gods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.gm910.temendingir.api.util.GMHelper;
import com.gm910.temendingir.api.util.GMNBT;
import com.gm910.temendingir.api.util.ServerPos;
import com.gm910.temendingir.world.gods.cap.DeityData;
import com.gm910.temendingir.world.gods.cap.DeityDilmunManager;
import com.google.common.collect.Lists;

import net.minecraft.command.ICommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class Deity implements ICommandSource {

	private static final float FAVOR_MAX = 100;
	private static final float FAVOR_MIN = 0;
	private String name;
	private UUID uuid = UUID.randomUUID();
	private Map<UUID, Float> followers = new HashMap<>();
	private InvocationItems invocation = new InvocationItems();
	private UUID creator;
	private DeityData data;
	private TextFormatting nameColor;
	private Random rand = new Random();
	private ChunkPos dilmunChunk;
	private NamingConvention naming;
	private Set<UUID> travelingPlayers = new HashSet<>();
	private DeityDilmunManager settings = new DeityDilmunManager(this);

	public Deity(String name, NamingConvention naming, UUID creator) {
		this.name = name;
		this.creator = creator;
		this.nameColor = TextFormatting.values()[rand.nextInt(TextFormatting.values().length)];
		this.naming = naming;
	}

	public void setData(DeityData data) {
		this.data = data;
	}

	public DeityDilmunManager getSettings() {
		return settings;
	}

	public void setSettings(DeityDilmunManager dilmunManager) {
		this.settings = dilmunManager;
	}

	public NamingConvention getNaming() {
		return naming;
	}

	/**
	 * List of players traveling to the deity's creation area in Dilmun
	 * 
	 * @return
	 */
	public Set<UUID> getTravelingPlayers() {
		return travelingPlayers;
	}

	public void setNaming(NamingConvention naming) {
		this.naming = naming;
	}

	public DeityData getData() {
		return data;
	}

	public String getName() {
		return name;
	}

	public UUID getCreator() {
		return creator;
	}

	public ChunkPos getDilmunChunk() {
		return dilmunChunk;
	}

	public void setDilmunChunk(ChunkPos dilmunChunk) {
		this.dilmunChunk = dilmunChunk;
	}

	public CreationStage getCreationStage() {

		if (!this.invocation.isComplete()) {
			return CreationStage.EMBRYONIC;
		} else if (!this.settings.isComplete()) {
			return CreationStage.RUDIMENTARY;
		} else {
			return CreationStage.COMPLETE;
		}
	}

	public void setInvocation(InvocationItems invocation) {
		this.invocation = invocation;
	}

	public InvocationItems getInvocation() {
		return invocation;
	}

	public UUID getUuid() {
		return uuid;
	}

	public Deity(CompoundNBT nbt) {

		this.name = nbt.getString("Name");
		this.invocation = new InvocationItems(nbt.getCompound("Invocation"));
		this.creator = nbt.getUniqueId("Creator");
		this.uuid = nbt.getUniqueId("ID");
		this.nameColor = TextFormatting.fromColorIndex(nbt.getInt("NameColor"));
		if (nbt.contains("DilmunChunk"))
			this.dilmunChunk = new ChunkPos(nbt.getLong("DilmunChunk"));
		this.settings = new DeityDilmunManager(this, nbt.getCompound("Settings"));
		this.naming = NamingConvention.from(nbt.getString("NamingCon"));
	}

	public CompoundNBT serialize() {
		CompoundNBT nbt = new CompoundNBT();
		nbt.putString("Name", name);
		nbt.put("Invocation", this.invocation.serialize());
		nbt.putUniqueId("Creator", this.creator);
		nbt.putUniqueId("ID", this.uuid);
		nbt.putInt("NameColor", this.nameColor.getColorIndex());
		if (dilmunChunk != null) {
			nbt.putLong("DilmunChunk", this.dilmunChunk.asLong());
		}
		nbt.put("Settings", settings.write());
		nbt.putString("NamingCon", naming.toString());
		return nbt;
	}

	@Override
	public void sendMessage(ITextComponent component, UUID senderUUID) {
	}

	/**
	 * Sends this message as if it were from a player with the deity's name
	 * 
	 * @param component
	 * @param receiver
	 */
	public void sendMessageTo(ICommandSource receiver, ITextComponent component) {
		receiver.sendMessage((new StringTextComponent(
				TextFormatting.BOLD + "" + this.nameColor + "[" + this.name + "] " + TextFormatting.RESET))
						.append(component),
				this.creator);
	}

	@Override
	public boolean shouldReceiveFeedback() {
		return false;
	}

	@Override
	public boolean shouldReceiveErrors() {
		return false;
	}

	@Override
	public boolean allowLogging() {
		return false;
	}

	public Set<UUID> getFollowerIDs() {
		return new HashSet<>(followers.keySet());
	}

	public Set<Entity> getFollowers() {
		return followers.keySet().stream().map((e) -> ServerPos.getEntityFromUUID(e, data.getServer()))
				.collect(Collectors.toSet());
	}

	public void setFollowerFavor(UUID follower, float num) {
		this.followers.put(follower, (float) GMHelper.clamp(num, FAVOR_MIN, FAVOR_MAX));
	}

	public void addFollower(UUID follower) {
		this.setFollowerFavor(follower, 50);
	}

	public void removeFollower(UUID follower) {
		this.followers.remove(follower);
	}

	@Override
	public String toString() {
		return "Deity " + this.name + " of " + this.invocation;
	}

	public static enum CreationStage {
		EMBRYONIC, RUDIMENTARY, COMPLETE
	}

	public static class InvocationItems {
		public static final int NUMBER = 4;
		List<Item> items = new ArrayList<>();

		public InvocationItems() {
		}

		public InvocationItems(InvocationItems other) {
			this.items.addAll(other.items);
		}

		public InvocationItems(CompoundNBT nbt) {
			this.items.addAll(
					GMNBT.createList((ListNBT) nbt.get("Items"), (e) -> Item.getItemById(((IntNBT) e).getInt())));
		}

		public CompoundNBT serialize() {
			CompoundNBT nbt = new CompoundNBT();
			nbt.put("Items", GMNBT.makeList(items, (i) -> IntNBT.valueOf(Item.getIdFromItem(i))));
			return nbt;
		}

		public InvocationItems addItem(Item stack) {
			if (!isComplete()) {
				items.add(stack);
			}
			return this;
		}

		public boolean isComplete() {
			return items.size() == NUMBER;
		}

		public boolean match(Item... itemstacks) {
			if (itemstacks.length != NUMBER) {
				return false;
			}
			List<Item> given = Lists.newArrayList(itemstacks);
			Map<Item, Integer> checkerMap = countingMap(items);
			Map<Item, Integer> givenMap = countingMap(given);
			System.out.println("Matching " + checkerMap + " with " + givenMap);

			return checkerMap.equals(givenMap);
		}

		private Map<Item, Integer> countingMap(List<Item> items) {
			Map<Item, Integer> map = new HashMap<>();
			for (Item i : items) {
				map.put(i, map.getOrDefault(i, 0) + 1);
			}
			return map;
		}

		public List<Item> getItems() {
			return items;
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof InvocationItems && ((InvocationItems) obj).match(this.items.toArray(new Item[0]));
		}

		@Override
		public String toString() {
			return super.toString() + this.items;
		}
	}

	public static class NamingConvention {
		String religionName;
		String followerSingularNoun;
		String followerPluralNoun;

		public NamingConvention(String religionName, String followerSingularNoun, String followerPluralNoun) {
			this.religionName = religionName;
			this.followerSingularNoun = followerSingularNoun;
			this.followerPluralNoun = followerPluralNoun;
		}

		public static NamingConvention from(String combined) {

			String[] parts = combined.split("\n");
			if (parts.length != 3)
				return null;
			return new NamingConvention(parts[0], parts[1], parts[2]);
		}

		public String getReligionName() {
			return religionName;
		}

		public String getFollowerPluralNoun() {
			return followerPluralNoun;
		}

		public String getFollowerSingularNoun() {
			return followerSingularNoun;
		}

		@Override
		public String toString() {
			return religionName + '\n' + followerPluralNoun + '\n' + followerSingularNoun;
		}
	}
}
