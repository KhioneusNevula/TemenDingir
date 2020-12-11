package com.gm910.temendingir.world.gods.cap.dilmunmanager;

import com.google.common.collect.Lists;

public enum ConsecrationProtection implements SettingTypeEnum {
	NO_HARM("noharm", 5), HEALING("healing", 2), NO_HUNGER("nohunger", 1), NO_DROWNING("nodrowning", 1),
	DISCOUNTED_TRADE("discount", 3);

	public final String id;
	public final int points;

	private ConsecrationProtection(String id, int points) {
		this.id = id;
		this.points = points;
	}

	public static ConsecrationProtection fromId(String id) {

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
}