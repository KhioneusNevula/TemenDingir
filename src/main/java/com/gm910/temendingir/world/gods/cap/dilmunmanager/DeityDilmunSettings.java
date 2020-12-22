package com.gm910.temendingir.world.gods.cap.dilmunmanager;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.gm910.temendingir.api.language.Translate;
import com.gm910.temendingir.api.util.GMHelper;
import com.gm910.temendingir.api.util.GMNBT;
import com.gm910.temendingir.api.util.ServerPos;
import com.gm910.temendingir.world.gods.Deity;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanRBTreeMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.WallSignBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.TriPredicate;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.world.BlockEvent.NeighborNotifyEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DeityDilmunSettings {

	private Deity deity;

	/**
	 * TODO nbt serialize this <br>
	 * If you put a null in the pair, then it is unbounded in that direction (or, in
	 * the case of a minimum, zero)
	 */
	private Map<SettingType<?>, Pair<Integer, Integer>> minMaxPointMap = GMHelper.createHashMap((e) -> {
		e.put(SettingType.PRONOUNS, Pair.of(1, 1));
		e.put(SettingType.COMMANDMENTS, Pair.of(7, null));
		e.put(SettingType.WORSHIP_CONDITIONS, Pair.of(4, null));
		e.put(SettingType.CONSECRATION_PERMISSIONS, Pair.of(null, null));
		e.put(SettingType.CONSECRATION_PROTECTIONS, Pair.of(null, 7));
		e.put(SettingType.RELATIONSHIP_TRAITS, Pair.of(1, 1));
		e.put(SettingType.WORSHIP_METHODS, Pair.of(1, 7));
		e.put(SettingType.WORSHIP_MODIFIERS, Pair.of(null, 4));

	});

	private Object2BooleanMap<SettingTypeEnum> settingsMap = new Object2BooleanRBTreeMap<>(
			(e1, e2) -> e1.name().compareTo(e2.name()));

	private Set<EntityType<?>> enemies = new HashSet<>(); // TODO figure out how to make an enemies map thing

	private Map<SettingTypeEnum, ServerPos> settingPositionsMap = new TreeMap<>(
			(e1, e2) -> e1.name().compareTo(e2.name()));

	private Map<SettingType<?>, ServerPos> settingSignsMap = new TreeMap<>();

	private BlockPos exitPortal;

	public DeityDilmunSettings(Deity deity) {
		this.deity = deity;
		MinecraftForge.EVENT_BUS.register(this);
	}

	public Deity getDeity() {
		return deity;
	}

	public boolean isComplete() {

		boolean settings = true;
		for (SettingType<?> settingtype : SettingType.values()) {
			/*for (SettingTypeEnum setting : settingtype.getEnumValues()) {
				if (!this.settingsMap.containsKey(setting)) {
					settings = false;
					break outer;
				}
			}*/
			if (!areSettingsValid(settingtype)) {
				settings = false;
				break;
			}

		}

		boolean head = true;
		// TODO finish the "isComplete" method of the deity settings

		return head && settings;
	}

	public int getCurrentNumberOfPointsUsedFor(SettingType<?> setting) {
		int pts = 0;
		for (SettingTypeEnum en : this.settingsMap.keySet()) {
			if (this.settingsMap.getBoolean(en) && SettingType.getTypeFromEnum(en) == setting) {
				pts += en.getPoints();
			}
		}
		return pts;
	}

	/**
	 * Whether the number of points expended for this setting type is valid
	 * 
	 * @param setting
	 * @return
	 */
	public boolean areSettingsValid(SettingType<?> setting) {

		int p = this.getCurrentNumberOfPointsUsedFor(setting);
		Integer minI = this.minMaxPointMap.get(setting).getFirst();
		int min = minI == null ? Integer.MIN_VALUE : minI;
		Integer maxI = this.minMaxPointMap.get(setting).getSecond();
		int max = maxI == null ? Integer.MAX_VALUE : maxI;
		return p >= min && p <= max;
	}

	public boolean isDilmunChunkFullyLoaded() {

		return !SettingType.values().stream().flatMap((e) -> Sets.newHashSet(e.enumClass.getEnumConstants()).stream())
				.filter((m) -> settingPositionsMap.get(m) == null).findAny().isPresent();
	}

	public void initialize() {
		List<Pronoun> pronouns = Lists.newArrayList(Pronoun.values());
		pronouns.sort((e1, e2) -> (new Random()).nextInt(2) - 1);
		this.setSettingsValue(pronouns.get(0), true);
	}

	public boolean getSettingsValue(SettingTypeEnum setting) {
		return this.settingsMap.getBoolean(setting);
	}

	public ServerPos getPos(SettingTypeEnum forSetting) {
		return this.settingPositionsMap.get(forSetting);
	}

	public ServerPos getSignPos(SettingType<?> forSetting) {
		return this.settingSignsMap.get(forSetting);
	}

	public ServerPos setSignPos(SettingType<?> forSetting, ServerPos anchor) {
		return this.settingSignsMap.put(forSetting, anchor);
	}

	public ServerPos setPos(SettingTypeEnum forSetting, ServerPos pos) {
		return this.settingPositionsMap.put(forSetting, pos);
	}

	public boolean setSettingsValue(SettingTypeEnum setting, boolean value) {
		return this.settingsMap.put(setting, value);
	}

	public BlockPos getExitPortal() {
		return exitPortal;
	}

	public void setExitPortal(BlockPos exitPortal) {
		this.exitPortal = exitPortal;
	}

	/**
	 * Returns all currently active settings for the given type
	 * 
	 * @param <T>
	 * @param type
	 * @return
	 */
	public <T extends SettingTypeEnum> Set<T> getActiveSettingsFor(SettingType<T> type) {
		return type.getEnumValues().stream().filter((e) -> this.getSettingsValue(e)).collect(Collectors.toSet());
	}

	/**
	 * Returns all inactive settings for the given type
	 * 
	 * @param <T>
	 * @param type
	 * @return
	 */
	public <T extends SettingTypeEnum> Set<T> getInactiveSettingsFor(SettingType<T> type) {
		return type.getEnumValues().stream().filter((e) -> !this.getSettingsValue(e)).collect(Collectors.toSet());
	}

	public boolean setSettingsValueAndUpdate(SettingTypeEnum setting, boolean value) {
		boolean v = this.setSettingsValue(setting, value);
		SettingType<?> type = SettingType.getTypeFromEnum(setting);
		ServerPos pos = this.getPos(setting);
		ServerWorld world = pos.getWorld(this.deity.getData().getServer());
		type.updateBlock(setting, this);
		System.out.println("Switching block at " + pos + " to " + world.getBlockState(pos)
				+ " in correspondence with the setting value " + value + " for " + setting + " of type " + type);
		type.updateSigns(this);
		return v;
	}

	@SubscribeEvent
	public void rightClick(RightClickBlock event) {
		if (event.getWorld().isRemote
				|| !this.settingPositionsMap.values().stream()
						.anyMatch((e) -> e.getDKey().equals(event.getWorld().getDimensionKey()))
				|| !this.isDilmunChunkFullyLoaded())
			return;

		if (event.getCancellationResult().isSuccessOrConsume())
			return;

		for (SettingTypeEnum val : this.settingPositionsMap.keySet()) {
			if (this.settingPositionsMap.get(val).getPos().equals(event.getPos())) {
				SettingType<?> type = SettingType.getTypeFromEnum(val);
				boolean b = type.onRightClick(event, val, this);
				// TODO why was this a predicate again?

			}
		}

	}

	@SubscribeEvent
	public void blockupdate(NeighborNotifyEvent event) {
		if (!(event.getWorld() instanceof ServerWorld)
				|| !this.settingPositionsMap.values().stream()
						.anyMatch((e) -> e.getDKey().equals(((ServerWorld) event.getWorld()).getDimensionKey()))
				|| !this.isDilmunChunkFullyLoaded())
			return;

		for (SettingTypeEnum val : this.settingPositionsMap.keySet()) {
			SettingType<?> type = SettingType.getTypeFromEnum(val);
			if (this.settingPositionsMap.get(val).getPos().equals(event.getPos())) {

				type.updateBlock(val, this);
			}

			if (this.settingSignsMap.get(type) != null && this.settingSignsMap.get(type).equals(event.getPos())) {

				type.updateSigns(this);
			}
		}

	}

	public CompoundNBT write() {
		CompoundNBT nbt = new CompoundNBT();

		nbt.put("Settings", GMNBT.makeList(settingsMap.keySet(), (k) -> {
			CompoundNBT tag = new CompoundNBT();

			tag.putString("EnumType", SettingType.getTypeFromEnum(k).prefix);
			tag.putString("Key", k.getId());
			tag.putBoolean("Value", settingsMap.getBoolean(k));

			return tag;

		}));
		nbt.put("SettingPositions", GMNBT.makeList(settingPositionsMap.keySet(), (k) -> {
			CompoundNBT tag = new CompoundNBT();
			tag.putString("EnumType", SettingType.getTypeFromEnum(k).prefix);
			tag.putString("Key", k.getId());
			tag.put("Value", settingPositionsMap.get(k).toNBT());

			return tag;

		}));
		nbt.put("SignPositions", GMNBT.makeList(settingSignsMap.keySet(), (k) -> {
			CompoundNBT tag = new CompoundNBT();
			tag.putString("EnumType", k.prefix);
			tag.put("Value", settingSignsMap.get(k).toNBT());

			return tag;

		}));
		if (exitPortal != null)
			nbt.putLong("ExitPortal", exitPortal.toLong());
		return nbt;
	}

	public DeityDilmunSettings(Deity deity, CompoundNBT nbt) {
		this(deity);
		this.settingsMap.putAll(GMNBT.createMap((ListNBT) nbt.get("Settings"), (inbt) -> {
			CompoundNBT tag = (CompoundNBT) inbt;
			SettingType<?> type = SettingType.getTypeFromPrefix(tag.getString("EnumType"));
			return Pair.of(type.fromId(tag.getString("Key")), tag.getBoolean("Value"));
		}));
		this.settingPositionsMap.putAll(GMNBT.createMap((ListNBT) nbt.get("SettingPositions"), (inbt) -> {
			CompoundNBT tag = (CompoundNBT) inbt;
			SettingType<?> type = SettingType.getTypeFromPrefix(tag.getString("EnumType"));
			return Pair.of(type.fromId(tag.getString("Key")), ServerPos.fromNBT(tag.getCompound("Value")));
		}));
		this.settingSignsMap.putAll(GMNBT.createMap((ListNBT) nbt.get("SignPositions"), (inbt) -> {
			CompoundNBT tag = (CompoundNBT) inbt;
			SettingType<?> type = SettingType.getTypeFromPrefix(tag.getString("EnumType"));
			return Pair.of(type, ServerPos.fromNBT(tag.getCompound("Value")));
		}));
		if (nbt.contains("ExitPortal")) {
			this.exitPortal = BlockPos.fromLong(nbt.getLong("ExitPortal"));
		}
		// TODO serialize more stuf
	}

	public static class SettingType<T extends SettingTypeEnum> implements Comparable<SettingType<?>> {
		private static Set<SettingType<?>> values = Sets.newHashSet();
		public static final SettingType<Pronoun> PRONOUNS = new SettingType<>("pronoun", Pronoun.class, Pronoun::fromId,
				(new DirectionalCreationOfSignBlock(Direction.NORTH))::accept, (new SingleSelectRightClick())::accept);
		public static final SettingType<Commandment> COMMANDMENTS = new SettingType<>("com", Commandment.class,
				Commandment::fromId, new DirectionalCreationOfSignBlock(Direction.SOUTH)::accept,
				new RegularToggleRightClick()::accept)
						.setUpdateSigns((new RegularSignUpdate<Commandment>(Direction.SOUTH))::accept);
		public static final SettingType<WorshipMethod> WORSHIP_METHODS = new SettingType<>("wormet",
				WorshipMethod.class, WorshipMethod::fromId, new DirectionalCreationOfSignBlock(Direction.EAST)::accept,
				new RegularToggleRightClick()::accept)
						.setUpdateSigns((new RegularSignUpdate<WorshipMethod>(Direction.EAST))::accept);
		public static final SettingType<WorshipCondition> WORSHIP_CONDITIONS = new SettingType<>("worcon",
				WorshipCondition.class, WorshipCondition::fromId,
				new DirectionalCreationOfSignBlock(Direction.EAST)::accept, new RegularToggleRightClick()::accept)
						.setUpdateSigns((new RegularSignUpdate<WorshipCondition>(Direction.EAST))::accept);
		public static final SettingType<WorshipModifier> WORSHIP_MODIFIERS = new SettingType<>("wormod",
				WorshipModifier.class, WorshipModifier::fromId,
				new DirectionalCreationOfSignBlock(Direction.EAST)::accept, new RegularToggleRightClick()::accept)
						.setUpdateSigns((new RegularSignUpdate<WorshipModifier>(Direction.EAST))::accept);

		public static final SettingType<ConsecrationPermission> CONSECRATION_PERMISSIONS = new SettingType<>("conper",
				ConsecrationPermission.class, ConsecrationPermission::fromId,
				new DirectionalCreationOfSignBlock(Direction.NORTH)::accept, new RegularToggleRightClick()::accept)
						.setUpdateSigns((new RegularSignUpdate<ConsecrationPermission>(Direction.NORTH))::accept);

		public static final SettingType<ConsecrationProtection> CONSECRATION_PROTECTIONS = new SettingType<>("conpro",
				ConsecrationProtection.class, ConsecrationProtection::fromId,
				new DirectionalCreationOfSignBlock(Direction.NORTH)::accept, new RegularToggleRightClick()::accept)
						.setUpdateSigns((new RegularSignUpdate<ConsecrationProtection>(Direction.NORTH))::accept);

		public static final SettingType<RelationshipTrait> RELATIONSHIP_TRAITS = new SettingType<>("rel",
				RelationshipTrait.class, RelationshipTrait::fromId,
				new DirectionalCreationOfSignBlock(Direction.SOUTH)::accept, new SingleSelectRightClick()::accept)
						.setUpdateSigns((new RegularSignUpdate<RelationshipTrait>(Direction.SOUTH))::accept);

		public final String prefix;
		public final Class<T> enumClass;
		public final Function<String, T> fromId;
		public final BiConsumer<SettingTypeEnum, DeityDilmunSettings> updateBlockState;
		public final TriPredicate<Event, SettingTypeEnum, DeityDilmunSettings> onRightClick;
		private BiConsumer<SettingType<T>, DeityDilmunSettings> updateSigns = (t, e) -> {
		};

		private SettingType(String prefix, Class<T> enumClass, Function<String, T> fromId,
				BiConsumer<SettingTypeEnum, DeityDilmunSettings> updateBlock,
				TriPredicate<Event, SettingTypeEnum, DeityDilmunSettings> onRightClick) {
			this.prefix = prefix;
			this.enumClass = enumClass;
			values.add(this);
			this.fromId = fromId;
			this.updateBlockState = updateBlock;
			this.onRightClick = onRightClick;
		}

		/**
		 * This is only to be run after enum block pos is set Will put the appropriate
		 * "button" there (sign, item frame, etc)
		 * 
		 * @param en
		 * @param manager
		 */
		public void updateBlock(SettingTypeEnum en, DeityDilmunSettings manager) {
			this.updateBlockState.accept(en, manager);
		}

		/**
		 * Only to be run after sign block pos is set Will put the appropriate sign
		 * signifier there
		 * 
		 * @param manager
		 */
		public void updateSigns(DeityDilmunSettings manager) {
			this.updateSigns.accept(this, manager);
		}

		public SettingType<T> setUpdateSigns(BiConsumer<SettingType<T>, DeityDilmunSettings> func) {
			this.updateSigns = func;
			return this;
		}

		public boolean onRightClick(Event ev, SettingTypeEnum en, DeityDilmunSettings man) {
			return onRightClick.test(ev, en, man);
		}

		public T fromId(String key) {
			return fromId.apply(key);
		}

		public static Set<SettingType<?>> values() {
			return values;
		}

		public Set<T> getEnumValues() {
			return Sets.newHashSet(this.enumClass.getEnumConstants());
		}

		public static <T extends SettingTypeEnum> SettingType<T> getTypeFromEnum(SettingTypeEnum val) {
			return (SettingType<T>) values().stream()
					.filter((e) -> val != null && val.getClass().isAssignableFrom(e.enumClass)).findAny().orElse(null);
		}

		public static <T extends SettingTypeEnum> SettingType<T> getTypeFromPrefix(String prefix) {
			return (SettingType<T>) values().stream().filter((e) -> e.prefix.equals(prefix)).findAny().orElse(null);
		}

		@Override
		public String toString() {
			return "SettingType " + prefix;
		}

		public static class RegularSignUpdate<T extends SettingTypeEnum> {

			public final Direction facing;

			public RegularSignUpdate(Direction f) {
				this.facing = f;
			}

			public void accept(SettingType<T> type, DeityDilmunSettings man) {

				ServerPos pos = man.getSignPos(type);
				if (pos == null)
					return;
				ServerWorld world = pos.getWorld(man.deity.getData().getServer());
				boolean valid = man.areSettingsValid(type);
				BlockState state = valid ? Blocks.DARK_OAK_WALL_SIGN.getDefaultState()
						: Blocks.BIRCH_WALL_SIGN.getDefaultState();
				state = state.with(WallSignBlock.FACING, facing);
				world.setBlockState(pos, state, 0);
				Pair<Integer, Integer> minmax = man.minMaxPointMap.get(type);
				if (minmax == null)
					throw new IllegalStateException(type + " has a null min-max");

				String sec = "range";
				if (minmax.getFirst() != null) {
					sec += "min";
				}
				if (minmax.getSecond() != null) {
					sec += "max";
				}

				System.out.println(type + " has pts " + man.getCurrentNumberOfPointsUsedFor(type));
				ITextComponent name = Translate.make("sign." + type.prefix);
				ITextComponent s = Translate.make("sign." + sec, name, man.getCurrentNumberOfPointsUsedFor(type),
						minmax.getFirst(), minmax.getSecond());
				SignTileEntity tile = TileEntityType.SIGN.create();
				world.setTileEntity(pos, tile);
				final int standardTextSize = 16;

				if (s.getString().length() > standardTextSize * 4) {
					throw new IllegalArgumentException("Cannot write text with over " + (standardTextSize * 4)
							+ " chars \"" + s.getString() + "\" to sign");
				}

				String msg = s.getString();
				int i = 0;
				while (!msg.isEmpty() && i < 4) {
					String temp = msg;
					if (msg.length() > standardTextSize) {
						temp = msg.substring(0, standardTextSize);
					}
					msg = msg.replace(temp, "");
					tile.setText(i, new StringTextComponent(temp));
					i++;
				}
				tile.setTextColor(valid ? DyeColor.WHITE : DyeColor.RED);
				System.out.println("Sign " + type.prefix + " set to " + s.getString());
			}
		}

		public static class DirectionalCreationOfSignBlock {

			public final Direction facing;

			public DirectionalCreationOfSignBlock(Direction dir) {
				this.facing = dir;
			}

			public void accept(SettingTypeEnum en, DeityDilmunSettings man) {
				ServerPos pos = man.getPos(en);
				ServerWorld world = pos.getWorld(man.deity.getData().getServer());
				SettingType<?> type = SettingType.getTypeFromEnum(en);
				boolean currentValue = man.getSettingsValue(en);
				BlockState state = currentValue ? Blocks.WARPED_WALL_SIGN.getDefaultState()
						: Blocks.CRIMSON_WALL_SIGN.getDefaultState();
				state = state.with(WallSignBlock.FACING, facing);
				world.setBlockState(pos, state);

				ITextComponent s = Translate.make(type.prefix + "." + en.getId(), en.getPoints());
				SignTileEntity tile = (SignTileEntity) world.getTileEntity(pos);
				final int standardTextSize = 13;
				if (s.getString().length() > standardTextSize * 4) {
					throw new IllegalArgumentException("Cannot write text with over " + (standardTextSize * 4)
							+ " chars \"" + s.getString() + "\" to sign");
				}
				String msg = s.getString();
				int i = 0;
				while (!msg.isEmpty() && i < 4) {
					String temp = msg;
					if (msg.length() > standardTextSize) {
						temp = msg.substring(0, standardTextSize);
					}
					msg = msg.replace(temp, "");
					tile.setText(i, new StringTextComponent(temp));
					i++;
				}
				tile.setTextColor(DyeColor.WHITE);
			}
		}

		public static class RegularToggleRightClick {

			public boolean accept(Event event, SettingTypeEnum val, DeityDilmunSettings manager) {

				boolean value = !manager.getSettingsValue(val);
				SettingType<?> type = SettingType.getTypeFromEnum(val);

				manager.setSettingsValueAndUpdate(val, value);
				System.out.println("Player clicked"
						+ (event instanceof RightClickBlock ? " " + ((RightClickBlock) event).getPos() : "")
						+ " and it is a valid position for dilmun and is now " + value + ". Currently, the " + type
						+ " has " + manager.getCurrentNumberOfPointsUsedFor(type)
						+ " points used while its min and max are " + manager.minMaxPointMap.get(type));
				return true;
			}
		}

		public static class SingleSelectRightClick {
			public boolean accept(Event event, SettingTypeEnum val, DeityDilmunSettings manager) {

				boolean value = !manager.getSettingsValue(val);
				SettingType<?> type = SettingType.getTypeFromEnum(val);

				manager.setSettingsValueAndUpdate(val, value);
				if (value) {
					for (SettingTypeEnum en : type.getEnumValues()) {
						if (en == val)
							continue;
						manager.setSettingsValueAndUpdate(en, false);
					}
				} else {

				}

				System.out.println("Player clicked"
						+ (event instanceof RightClickBlock ? " " + ((RightClickBlock) event).getPos() : "")
						+ " and it is a valid position for dilmun and is now " + value + ". Currently, the " + type
						+ " has " + manager.getCurrentNumberOfPointsUsedFor(type)
						+ " points used while its min and max are " + manager.minMaxPointMap.get(type));
				return true;
			}
		}

		@Override
		public int compareTo(SettingType<?> o) {

			return prefix.compareTo(o.prefix);
		}
	}

}
