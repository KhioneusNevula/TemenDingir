package com.gm910.temendingir.world.gods.cap.dilmunmanager;

import com.google.common.collect.Lists;

public enum RelationshipTrait implements SettingTypeEnum {
	WARLIKE("warlike"), INVASIVE("invasive"), PEACEFUL("peaceful");

	public final String id;

	private RelationshipTrait(String id) {
		this.id = id;
	}

	public static RelationshipTrait fromId(String id) {

		return Lists.newArrayList(values()).stream().filter((e) -> e.id.equals(id)).findAny().orElse(null);
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public int getPoints() {
		return 1;
	}
}