package com.gm910.temendingir.world.gods.cap.dilmunmanager;

import com.google.common.collect.Lists;

public enum WorshipCondition implements SettingTypeEnum {
	DAY("day", 2), NIGHT("night", 2), BUILDING("building", 2), LIBRARY("library", 1), NETHER("nether", 3);

	public final String id;
	public final int points;

	private WorshipCondition(String id, int points) {
		this.id = id;
		this.points = points;
	}

	public static WorshipCondition fromId(String id) {

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