package com.gm910.temendingir.world.gods.cap.dilmunmanager;

import com.google.common.collect.Lists;

public enum Pronoun implements SettingTypeEnum {
	HE("hh"), SHE("ss"), THEY("tt"), IT("ii");

	public final String id;

	private Pronoun(String id) {
		this.id = id;
	}

	public static Pronoun fromId(String id) {

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