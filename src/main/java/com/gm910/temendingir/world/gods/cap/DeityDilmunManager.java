package com.gm910.temendingir.world.gods.cap;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.gm910.temendingir.world.gods.Deity;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.nbt.CompoundNBT;

public class DeityDilmunManager {

	private Deity deity;

	private Map<SettingType<?>, Enum<?>> settingsMap = new HashMap<>();

	public DeityDilmunManager(Deity deity) {
		this.deity = deity;
	}

	public Deity getDeity() {
		return deity;
	}

	public boolean isComplete() {

		// TODO
		return false;
	}

	public <T extends Enum<T>> T getSettingsValue(SettingType<T> setting) {
		return (T) this.settingsMap.get(setting);
	}

	public <T extends Enum<T>> T setSettingsValue(SettingType<T> setting, T value) {
		return (T) this.settingsMap.put(setting, value);
	}

	public CompoundNBT write() {
		CompoundNBT nbt = new CompoundNBT();

		return nbt;
	}

	public DeityDilmunManager(Deity deity, CompoundNBT nbt) {
		this(deity);

		// TODO
	}

	public static class SettingType<T extends Enum<T>> {
		public static final SettingType<Pronoun> PRONOUNS = new SettingType<>("pronoun", Pronoun.class);
		public static final SettingType<Commandment> COMMANDMENTS = new SettingType<>("com", Commandment.class);

		public final String prefix;
		public final Class<T> enumClass;
		private static Set<SettingType<?>> values = Sets.newHashSet();

		private SettingType(String prefix, Class<T> enumClass) {
			this.prefix = prefix;
			this.enumClass = enumClass;
			values.add(this);
		}

		public static Set<SettingType<?>> values() {
			return values;
		}
	}

	public static enum Pronoun {
		HE("hh"), SHE("ss"), THEY("tt"), IT("ii");

		public final String id;

		private Pronoun(String id) {
			this.id = id;
		}

		public static Pronoun fromId(String id) {

			return Lists.newArrayList(values()).stream().filter((e) -> e.id.equals(id)).findAny().orElse(null);
		}
	}

	public static enum Commandment {
		NO_KILL_PASSIVE_NEUTRAL("nokillpn"), NO_KILL_HOSTILE("nokillh"), LOVE_ONE_ANOTHER("loveoa"),
		LOVE_YOUR_NEIGHBOR("loveyn"), NO_POTIONCRAFT("nopotion"), NO_ENCHANTING("noench"), NO_CURSING("nocurse"),
		NO_CHEST_GEAR("nochestgear"), NO_EATING_MEAT("nomeat"), NO_CROPS("no eating crops"),
		NO_FALSE_IDOLS("nofalseidols"), NO_SLEEP("nosleep");

		public final String id;

		private Commandment(String id) {
			this.id = id;
		}

		public static Commandment fromId(String id) {

			return Lists.newArrayList(values()).stream().filter((e) -> e.id.equals(id)).findAny().orElse(null);
		}
	}

}
