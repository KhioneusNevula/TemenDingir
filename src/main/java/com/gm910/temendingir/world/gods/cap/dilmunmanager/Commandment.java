package com.gm910.temendingir.world.gods.cap.dilmunmanager;

import com.google.common.collect.Lists;

public enum Commandment implements SettingTypeEnum {
	NO_KILL_PEACEFUL("nokillpn", 2), NO_KILL_HOSTILE("nokillh", 4), LOVE_ONE_ANOTHER("loveoa", 2),
	LOVE_YOUR_NEIGHBOR("loveyn", 1), NO_POTIONCRAFT("nopotion", 1), NO_ENCHANTING("noench", 3),
	NO_CURSING("nocurse", 1), NO_CHEST_GEAR("nochestgear", 5), NO_EATING_MEAT("nomeat", 2), NO_CROPS("nocrops", 3),
	NO_FALSE_IDOLS("nofalseidols", 1), NO_SLEEP("nosleep", 2);

	public static final float FAVOR_NUMERATOR = 6f;

	public final String id;
	public final int points;

	private Commandment(String id, int points) {
		this.id = id;
		this.points = points;
	}

	public static Commandment fromId(String id) {

		return Lists.newArrayList(values()).stream().filter((e) -> e.id.equals(id)).findAny().orElse(null);
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public int getPoints() {
		return points;
	}

	public float getFavorSubtraction() {
		return FAVOR_NUMERATOR / this.points;
	}
}