package com.gm910.temendingir.world.gods.cap.dilmunmanager;

import com.google.common.collect.Lists;

public enum WorshipModifier implements SettingTypeEnum {
	NATURE("nature", 3), LIBRARY("library", 3), FLAME("flame", 2), WATER("water", 2), ART("art", 1),
	DECORATION("decoration", 1);

	public final String id;
	public final int points;

	private WorshipModifier(String id, int points) {
		this.id = id;
		this.points = points;
	}

	public static WorshipModifier fromId(String id) {

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