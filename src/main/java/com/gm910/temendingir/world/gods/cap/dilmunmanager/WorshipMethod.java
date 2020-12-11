package com.gm910.temendingir.world.gods.cap.dilmunmanager;

import com.google.common.collect.Lists;

public enum WorshipMethod implements SettingTypeEnum {
	HUMAN("human", 1), HOSTILE("hostile", 1), PASSIVE("passive", 2), TOOL("tool", 3), FOOD("food", 5),
	PLANT("plant", 6), WEALTH("wealth", 3);

	public final String id;
	public final int points;

	private WorshipMethod(String id, int points) {
		this.id = id;
		this.points = points;
	}

	public static WorshipMethod fromId(String id) {

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