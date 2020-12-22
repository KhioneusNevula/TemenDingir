package com.gm910.temendingir.world.gods;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import com.gm910.temendingir.api.language.Translate;
import com.gm910.temendingir.api.util.GMHelper;
import com.gm910.temendingir.api.util.GMNBT;
import com.gm910.temendingir.api.util.GMWorld;
import com.gm910.temendingir.api.util.NonNullTreeMap;
import com.gm910.temendingir.api.util.ServerPos;
import com.gm910.temendingir.blocks.DilmunExitPortal;
import com.gm910.temendingir.blocks.FireOfCreationBlock;
import com.gm910.temendingir.blocks.tile.invokers.AltarOfConsecration;
import com.gm910.temendingir.damage.DivineDamageSource;
import com.gm910.temendingir.world.gods.cap.DeityData;
import com.gm910.temendingir.world.gods.cap.dilmunmanager.Commandment;
import com.gm910.temendingir.world.gods.cap.dilmunmanager.ConsecrationPermission;
import com.gm910.temendingir.world.gods.cap.dilmunmanager.ConsecrationProtection;
import com.gm910.temendingir.world.gods.cap.dilmunmanager.DeityDilmunSettings;
import com.gm910.temendingir.world.gods.cap.dilmunmanager.Pronoun;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.command.ICommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class Deity implements ICommandSource {

	public static final float FAVOR_MAX = 100;
	public static final float FAVOR_MIN = 0;
	public static final int TOTAL_HOLY_LAND_HEIGHT = 40;
	public static final float INITIAL_FOLLOWER_FAVOR = 50;
	private String name;
	private UUID uuid = UUID.randomUUID();
	private Map<UUID, Float> followers = new TreeMap<>();
	private InvocationItems invocation = new InvocationItems();
	private UUID creator;
	private DeityData data;
	private TextFormatting nameColor;
	private Random rand = new Random();
	private ChunkPos dilmunChunk;
	private NamingConvention naming;
	private DeityDilmunSettings settings = new DeityDilmunSettings(this);
	private CompoundNBT tagCompound = new CompoundNBT();
	private DeityEnergyStorage energy = new DeityEnergyStorage(0, this);
	private boolean deactivateHolyLand = false;

	private Map<ServerPos, Rectangle> consecrationNexi = new TreeMap<>();
	private Map<RegistryKey<World>, Set<BlockPos>> consecratedRegion = new NonNullTreeMap<>(Sets::newHashSet, null);
	private Map<RegistryKey<World>, Set<BlockPos>> consecratedOutline = new NonNullTreeMap<>(Sets::newHashSet, null);
	private Map<RegistryKey<World>, Set<Point>> groundArea = new NonNullTreeMap<>(Sets::newHashSet, null);

	private Deity.Level level = Deity.Level.FIRST;

	public Deity(String name, NamingConvention naming, UUID creator) {
		this.name = name;
		this.creator = creator;
		this.nameColor = TextFormatting.values()[rand.nextInt(TextFormatting.values().length)];
		this.naming = naming;
	}

	public void setData(DeityData data) {
		this.data = data;
	}

	public DeityDilmunSettings getSettings() {
		return settings;
	}

	public void setSettings(DeityDilmunSettings dilmunManager) {
		this.settings = dilmunManager;
	}

	public NamingConvention getNaming() {
		return naming;
	}

	/**
	 * tt for they/them, ss for she/her, hh for he/him, ii for it/it, and ii if no
	 * pronoun is selected
	 * 
	 * @return
	 */
	public String getPronounCode() {
		for (Pronoun pron : Pronoun.values()) {
			if (this.settings.getSettingsValue(pron)) {
				return pron.id;
			}
		}
		return Pronoun.IT.id;
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
		this.settings = new DeityDilmunSettings(this, nbt.getCompound("Settings"));
		this.naming = NamingConvention.from(nbt.getString("NamingCon"));
		this.consecrationNexi.clear();
		this.consecrationNexi.putAll(GMNBT.createMap((ListNBT) nbt.get("ConsecrationNexi"), (k) -> {
			return ServerPos.fromNBT(((CompoundNBT) k).getCompound("Pos"));
		}, (v) -> {
			return GMNBT.rectangleFromNBT(((CompoundNBT) v).getCompound("Rectangle"));
		}));
		this.tagCompound = nbt.getCompound("ExtraData");
		this.level = Level.values()[nbt.getInt("Level")];

		this.energy.forceSetEnergyStored(nbt.getDouble("Energy"));
	}

	public CompoundNBT serialize() {
		CompoundNBT nbt = new CompoundNBT();
		this.recalculateConsecrationAreaAndOutline();
		nbt.putString("Name", name);
		nbt.put("Invocation", this.invocation.serialize());
		nbt.putUniqueId("Creator", this.creator);
		nbt.putUniqueId("ID", this.uuid);
		nbt.putInt("NameColor", this.nameColor.getColorIndex());
		if (dilmunChunk != null) {
			nbt.putLong("DilmunChunk", this.dilmunChunk.asLong());
		}
		nbt.put("ConsecrationNexi", GMNBT.makeList(this.consecrationNexi.entrySet(), (entry) -> {
			CompoundNBT tag = new CompoundNBT();
			tag.put("Pos", entry.getKey().toNBT());
			tag.put("Rectangle", GMNBT.rectangleToNBT(entry.getValue()));
			return tag;
		}));
		nbt.put("Settings", settings.write());
		nbt.putString("NamingCon", naming.toString());
		nbt.put("ExtraData", this.tagCompound);
		nbt.putInt("Level", level.ordinal());
		nbt.putDouble("Energy", energy.getEnergyStored());
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

	public DeityEnergyStorage getEnergyStorage() {
		return energy;
	}

	public double getEnergyStored() {
		return energy.getEnergyStored();
	}

	@Override
	public boolean allowLogging() {
		return false;
	}

	public Deity.Level getLevel() {
		return level;
	}

	public void setLevel(Deity.Level level) {
		this.level = level;
	}

	public Set<UUID> getFollowerIDs() {
		return new HashSet<>(followers.keySet());
	}

	public boolean isFollower(UUID uuid) {
		return followers.containsKey(uuid);
	}

	public Set<Entity> getFollowers() {
		return followers.keySet().stream().map((e) -> ServerPos.getEntityFromUUID(e, data.getServer()))
				.collect(Collectors.toSet());
	}

	public void setFollowerFavor(UUID follower, float num) {
		if (this.followers.containsKey(follower)) {
			this.followers.put(follower, (float) GMHelper.clamp(num, FAVOR_MIN, FAVOR_MAX));
		} else {
			throw new IllegalArgumentException(
					follower + " is not a follower of " + this + " but tried to set favor to " + num);
		}
	}

	public float getFollowerFavor(UUID follower) {
		return followers.getOrDefault(follower, 0f);
	}

	public void changeFollowerFavor(UUID follower, float by, boolean notifyPlayer) {
		float prev = this.getFollowerFavor(follower);
		this.setFollowerFavor(follower, this.getFollowerFavor(follower) + by);
		if (notifyPlayer) {
			Entity e = ServerPos.getEntityFromUUID(follower, data.getServer());
			float diff = this.getFollowerFavor(follower) - prev;
			boolean decrease = diff < 0;
			if (decrease) {
				this.sendMessageTo(e,
						Translate.make("deity.lostfavor." + this.getPronounCode(), this.getName(), -diff));
			} else if (diff == 0) {
				if (by < 0) {

					this.sendMessageTo(e,
							Translate.make("deity.minimumfavor." + this.getPronounCode(), this.getName(), -diff));
					// TODO some event occurs
				}
			} else {

				this.sendMessageTo(e,
						Translate.make("deity.gainedfavor." + this.getPronounCode(), this.getName(), diff));
			}
		}
	}

	public void addFollower(UUID follower) {
		this.followers.put(follower, 0f);
		this.setFollowerFavor(follower, INITIAL_FOLLOWER_FAVOR);
	}

	public void pledgeEntityToDeity(LivingEntity e) {
		Deity already = DeityData.get(e.getServer()).getFromFollowerUUID(e.getUniqueID());
		if (already != null) {

			e.sendMessage(Translate.make("deity.cannotpledge." + this.getPronounCode(), this.getName(),
					already.getNaming().followerSingularNoun), e.getUniqueID());
			return;
		}
		e.sendMessage(Translate.make("deity.pledged." + this.getPronounCode(), this.getName()), e.getUniqueID());
		this.addFollower(e.getUniqueID());
	}

	public void removeFollower(UUID follower) {
		this.followers.remove(follower);
	}

	public void removeHolyRegion(ServerPos tilePos) {
		Rectangle rect = this.consecrationNexi.remove(tilePos);
		if (rect != null) {
			System.out.println("Removed " + rect + " at " + tilePos + " to " + this.name);
			this.recalculateConsecrationAreaAndOutline();
		}
	}

	public void clearHolyRegion() {
		this.consecrationNexi.clear();
		this.consecratedOutline.clear();
		this.consecratedRegion.clear();
		this.recalculateConsecrationAreaAndOutline();
		System.out.println("Cleared holy land for " + this.name);
	}

	public void addHolyRegion(ServerPos tilePos, Rectangle region) {
		this.consecrationNexi.put(tilePos, region);
		System.out.println("Added " + region + " at " + tilePos + " to " + this.name);
		this.recalculateConsecrationAreaAndOutline();
	}

	public Map<RegistryKey<World>, Set<BlockPos>> getConsecratedOutline() {
		return consecratedOutline;
	}

	public Map<RegistryKey<World>, Set<BlockPos>> getConsecratedRegion() {
		return consecratedRegion;
	}

	public Map<ServerPos, Rectangle> getConsecrationNexi() {
		return consecrationNexi;
	}

	public boolean isWithinHolyLand(ServerPos pos) {
		return this.consecratedRegion.get(pos.getDKey()).contains(pos.getPos());
	}

	public boolean isWithinHolyLand(Entity e) {
		return this.consecratedRegion.get(e.world.getDimensionKey()).stream()
				.anyMatch((m) -> e.world.getEntitiesWithinAABB(e.getClass(), new AxisAlignedBB(m)).contains(e));
	}

	public boolean isInDilmun(BlockPos pos) {

		ChunkPos p = (new ChunkPos(pos));
		return p.x - this.dilmunChunk.x <= 1 && p.z - this.dilmunChunk.z <= 1;
	}

	public CompoundNBT getTagCompound() {
		return tagCompound;
	}

	public void setTagCompound(CompoundNBT tagCompound) {
		this.tagCompound = tagCompound;
	}

	public Collection<PlayerEntity> getPlayerFollowers() {
		return this.getFollowers().stream().filter((e) -> e instanceof PlayerEntity).map((e) -> (PlayerEntity) e)
				.collect(Collectors.toSet());
	}

	public ListNBT getExtraEntityInfo() {
		if (!tagCompound.contains("EntityStates")) {
			tagCompound.put("EntityStates", new ListNBT());
		}
		return (ListNBT) this.tagCompound.get("EntityStates");
	}

	public CompoundNBT getExtraEntityInfo(UUID forE) {
		ListNBT en = this.getExtraEntityInfo();
		CompoundNBT nbt = en.stream()
				.filter((e) -> e instanceof CompoundNBT && ((CompoundNBT) e).getUniqueId("Player").equals(forE))
				.map((e) -> ((CompoundNBT) e).getCompound("Data")).findAny().orElse(null);
		if (nbt == null) {
			CompoundNBT tag = new CompoundNBT();
			tag.putUniqueId("Player", forE);

			nbt = new CompoundNBT();
			tag.put("Data", nbt);
			en.add(tag);
		}
		return nbt;
	}

	@SubscribeEvent
	public void containerOpen(PlayerContainerEvent.Open event) {
		if (event.getEntity().world.isRemote)
			return;
		if (this.isWithinHolyLand(event.getPlayer()) && this.getCreationStage().isComplete()) {
			if (!this.isFollower(event.getPlayer().getUniqueID())) {
				if (this.settings.getSettingsValue(ConsecrationPermission.NO_OPENING)) {
					((ServerPlayerEntity) event.getPlayer()).closeScreen();
				}
			}
		}
	}

	@SubscribeEvent
	public void itemUse(PlayerInteractEvent.RightClickItem event) {
		if (event.getEntity().world.isRemote)
			return;
		if (this.isWithinHolyLand(event.getPlayer()) && this.getCreationStage().isComplete()) {
			if (!this.isFollower(event.getPlayer().getUniqueID())) {
				if (this.settings.getSettingsValue(ConsecrationPermission.NO_TOUCHING)) {
					event.setCanceled(true);
					event.setCancellationResult(ActionResultType.FAIL);

				}
			}
		}
	}

	@SubscribeEvent
	public void blockInteract(PlayerInteractEvent.RightClickBlock event) {
		if (event.getEntity().world.isRemote)
			return;
		if (this.isWithinHolyLand(new ServerPos(event.getPos(), event.getWorld().getDimensionKey()))
				&& this.getCreationStage().isComplete()) {
			if (!this.isFollower(event.getPlayer().getUniqueID())) {
				if (this.settings.getSettingsValue(ConsecrationPermission.NO_TOUCHING)
						|| this.settings.getSettingsValue(ConsecrationPermission.NO_SLEEPING)
								&& event.getWorld().getBlockState(event.getPos()).getBlock().isIn(BlockTags.BEDS)) {
					event.setCanceled(true);
					event.setCancellationResult(ActionResultType.FAIL);

				}

			}
		}
	}

	@SubscribeEvent
	public void entityInteract(PlayerInteractEvent.EntityInteract event) {
		if (event.getEntity().world.isRemote)
			return;
		if (this.isWithinHolyLand(event.getEntity()) && this.getCreationStage().isComplete()) {
			if (!this.isFollower(event.getPlayer().getUniqueID())) {
				if (this.settings.getSettingsValue(ConsecrationPermission.NO_INTERACTING)) {
					event.setCanceled(true);
					event.setCancellationResult(ActionResultType.FAIL);

				}
			}
		}
	}

	@SubscribeEvent
	public void entityInteract(PlayerInteractEvent.EntityInteractSpecific event) {
		if (event.getEntity().world.isRemote)
			return;
		if (this.isWithinHolyLand(event.getEntity()) && this.getCreationStage().isComplete()) {
			if (!this.isFollower(event.getPlayer().getUniqueID())) {
				if (this.settings.getSettingsValue(ConsecrationPermission.NO_INTERACTING)) {
					event.setCanceled(true);
					event.setCancellationResult(ActionResultType.FAIL);

				}
			}
		}
	}

	@SubscribeEvent
	public void sleep(SleepFinishedTimeEvent event) {
		if (this.getCreationStage().isComplete()) {
			for (PlayerEntity player : this.getPlayerFollowers()) {
				if (player.isSleeping() && this.isFollower(player.getUniqueID())
						&& this.settings.getSettingsValue(Commandment.NO_SLEEP)) {
					this.changeFollowerFavor(player.getUniqueID(), -Commandment.NO_SLEEP.getFavorSubtraction(), true);
				}
			}
		}
	}

	@SubscribeEvent
	public void breakEvent(BreakEvent event) {
		if (event.getWorld().isRemote())
			return;
		if (this.getCreationStage().isComplete()
				&& this.isWithinHolyLand(new ServerPos(event.getPos(), event.getPlayer().world.getDimensionKey()))) {
			if (!this.isFollower(event.getPlayer().getUniqueID())) {
				if (this.settings.getSettingsValue(ConsecrationPermission.NO_GRIEFING)
						|| this.settings.getSettingsValue(ConsecrationPermission.ADVENTURE)) {
					event.setCanceled(true);
				}
			}
		}
	}

	@SubscribeEvent
	public void attacked(LivingAttackEvent event) {
		if (event.getEntity().world.isRemote)
			return;
		if (this.getCreationStage().isComplete()) {
			if ((event.getEntity() instanceof PlayerEntity)) {
				ServerPlayerEntity player = (ServerPlayerEntity) event.getEntity();
				if (this.isWithinHolyLand(player)) {
					if (this.isFollower(player.getUniqueID())) {
						if (this.settings.getSettingsValue(ConsecrationProtection.NO_HARM)
								&& event.getSource().getTrueSource() instanceof PlayerEntity) {
							event.setCanceled(true);
						}
					}
				}
			} else if (event.getSource().getTrueSource() instanceof PlayerEntity
					&& isWithinHolyLand(event.getEntity())) {
				ServerPlayerEntity player = (ServerPlayerEntity) event.getSource().getTrueSource();
				if (!this.isFollower(player.getUniqueID())) {
					if (this.settings.getSettingsValue(ConsecrationPermission.NO_HURTING)) {
						event.setCanceled(true);
					}

				}
			}
		}
	}

	@SubscribeEvent
	public void livingUpdate(LivingUpdateEvent event) {
		if (event.getEntity().world.isRemote)
			return;
		if (this.getExtraEntityInfo(event.getEntity().getUniqueID()).getBoolean("InDilmunPortal")) {
			if (!(event.getEntity().world.getBlockState(event.getEntity().getPosition())
					.getBlock() instanceof DilmunExitPortal)
					&& !(event.getEntity().world.getBlockState(event.getEntity().getPosition())
							.getBlock() instanceof FireOfCreationBlock)) {
				this.getExtraEntityInfo(event.getEntityLiving().getUniqueID()).remove("InDilmunPortal");
			}
		}
		if (!(event.getEntity() instanceof PlayerEntity))
			return;
		ServerPlayerEntity player = (ServerPlayerEntity) event.getEntity();
		if (this.getCreationStage().isComplete() && settings.getSettingsValue(Commandment.NO_SLEEP)
				&& this.isFollower(player.getUniqueID())) {
			player.getStats().setValue(player, Stats.CUSTOM.get(Stats.TIME_SINCE_REST), 0);
		}
		if (isWithinHolyLand(player)) {
			this.getExtraEntityInfo(player.getUniqueID()).putBoolean("WasInHolyLand", true);
			DeityDilmunSettings settings = this.getSettings();
			if (this.getCreationStage().isComplete()) {
				if (this.isFollower(player.getUniqueID())) {
					if (settings.getSettingsValue(ConsecrationProtection.HEALING)) {
						if (player.world.getGameTime() % 100 == 0) {
							player.addPotionEffect(new EffectInstance(Effects.INSTANT_HEALTH, 1));
						}
					}

					if (settings.getSettingsValue(ConsecrationProtection.DISCOUNTED_TRADE)) {
						if (player.getActivePotionEffect(Effects.HERO_OF_THE_VILLAGE) == null) {

							player.addPotionEffect(new EffectInstance(Effects.HERO_OF_THE_VILLAGE, 25));
						}

					}

					if (settings.getSettingsValue(ConsecrationProtection.NO_HUNGER)) {
						if (player.getFoodStats().needFood()) {
							player.getFoodStats().setFoodLevel(20);
						}
						if (player.getActivePotionEffect(Effects.SATURATION) == null) {
							player.addPotionEffect(new EffectInstance(Effects.SATURATION, 10));
						}
						player.removeActivePotionEffect(Effects.HUNGER);
					}
					if (settings.getSettingsValue(ConsecrationProtection.NO_DROWNING)) {
						if (player.getAir() < player.getMaxAir()) {
							player.setAir(player.getMaxAir());
						}
					}
				} else {
					if (!settings.getSettingsValue(ConsecrationPermission.ADVENTURE)) {
						if (settings.getSettingsValue(ConsecrationPermission.NO_GRIEFING)) {
							boolean allowEdit = player.abilities.allowEdit;
							this.getExtraEntityInfo(player.getUniqueID()).putBoolean("AllowedEdit", allowEdit);
							player.abilities.allowEdit = false;

							// TODO more permissions, fix serializing thing
						}
					} else {
						if (player.interactionManager.getGameType() == GameType.SURVIVAL) {
							player.setGameType(GameType.ADVENTURE);
							this.getExtraEntityInfo(player.getUniqueID()).putBoolean("WasSurvivalMode", true);
						} else {
							boolean allowEdit = player.abilities.allowEdit;
							this.getExtraEntityInfo(player.getUniqueID()).putBoolean("AllowedEdit", allowEdit);
							player.abilities.allowEdit = false;
						}
						// TODO more permissions, fix serializing thing
					}

					if (settings.getSettingsValue(ConsecrationPermission.NO_ENTRY)) {

						event.getEntityLiving().applyKnockback(1, 1, 1);

					}

					if (settings.getSettingsValue(ConsecrationPermission.NO_ENTRY_HARM)
							&& event.getEntity().world.getGameTime() % 50 == 0) {
						GMWorld.summonMagicLightning(DivineDamageSource.holyLandDamage(this), event.getEntity().world,
								event.getEntity().getPosition(), null);
					}

				}
			}
		} else {
			CompoundNBT nbt = this.getExtraEntityInfo(player.getUniqueID());
			if (nbt.getBoolean("WasInHolyLand")) {
				nbt.remove("WasInHolyLand");// TODO needs work
				if (nbt.contains("AllowedEdit")) {
					player.abilities.allowEdit = nbt.getBoolean("AllowedEdit");
					nbt.remove("AllowedEdit");
				}
				if (nbt.contains("WasSurvivalMode")) {
					player.setGameType(GameType.SURVIVAL);
					nbt.remove("WasSurvivalMode");
				}
			}
		}
	}

	@SubscribeEvent
	public void kill(LivingDeathEvent event) {
		if (event.getSource().getTrueSource() != null) {
			Entity violator = event.getSource().getTrueSource();
			if (this.getCreationStage().isComplete() && this.isFollower(violator.getUniqueID())) {
				if ((event.getEntityLiving() instanceof MobEntity)) {
					MobEntity mob = (MobEntity) event.getEntityLiving();
					if (mob.getClassification(true).getPeacefulCreature()
							&& this.settings.getSettingsValue(Commandment.NO_KILL_PEACEFUL)) {
						this.changeFollowerFavor(violator.getUniqueID(),
								-Commandment.NO_KILL_PEACEFUL.getFavorSubtraction(), true);
					}
					if (!mob.getClassification(true).getPeacefulCreature()
							&& this.settings.getSettingsValue(Commandment.NO_KILL_HOSTILE)) {
						this.changeFollowerFavor(violator.getUniqueID(),
								-Commandment.NO_KILL_HOSTILE.getFavorSubtraction(), true);
					}
					if (this.isFollower(mob.getUniqueID())
							&& this.settings.getSettingsValue(Commandment.LOVE_YOUR_NEIGHBOR)) {
						this.changeFollowerFavor(violator.getUniqueID(),
								-Commandment.LOVE_YOUR_NEIGHBOR.getFavorSubtraction(), true);
					}
				}
				if (event.getEntityLiving() instanceof PlayerEntity) {
					if (this.settings.getSettingsValue(Commandment.LOVE_ONE_ANOTHER)) {
						this.changeFollowerFavor(violator.getUniqueID(),
								-Commandment.LOVE_ONE_ANOTHER.getFavorSubtraction(), true);
					}
				}
			}
		}
	}

	public Map<RegistryKey<World>, Set<Point>> getGroundArea() {
		return groundArea;
	}

	@SubscribeEvent
	public void serverTick(ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			return;
		}
		int blocks = (int) this.getGroundArea().entrySet().stream().flatMap((entry) -> entry.getValue().stream())
				.count();
		//System.out.println("Blocks " + blocks);
		int discount = this.getConsecrationNexi().size() * AltarOfConsecration.DISCOUNT_PER_NEXUS;
		if (blocks <= discount) {
			blocks = discount;
		} else {
			blocks -= discount;
		}

		double energySubtracted = blocks * AltarOfConsecration.ENERGY_PER_TICK_PER_BLOCK;

		double extract = this.energy.extractEnergy(energySubtracted, true);
		boolean previous = this.deactivateHolyLand;
		if (energySubtracted > extract || extract == 0) {
			this.deactivateHolyLand = true; // TODO figure out this god consecration energy problem
		} else {
			this.deactivateHolyLand = false;
		}
		if (previous != this.deactivateHolyLand) { // If there was a change in the holy land state, recalculate the holy land
			System.out.println(deactivateHolyLand + " " + energySubtracted + " intended but only " + extract);
			this.recalculateConsecrationAreaAndOutline();

		}
	}

	public void recalculateConsecrationAreaAndOutline() {
		// TODO optimize this
		System.out.println("Recalculating holy land");
		this.consecratedOutline.clear();
		this.consecratedRegion.clear();
		this.groundArea.clear();

		if (this.deactivateHolyLand || !this.getCreationStage().isComplete()) {
			System.out.println(deactivateHolyLand ? "Holy land inactive" : "");

		}
		for (ServerWorld world : this.consecrationNexi.keySet().stream().map((p) -> p.getWorld(this.data.getServer()))
				.collect(Collectors.toSet())) {

			Set<ServerPos> serverPositions = this.consecrationNexi.entrySet().stream()
					.filter((en) -> en.getKey().getWorld(this.data.getServer()).equals(world)).map((m) -> m.getKey())
					.collect(Collectors.toSet());
			Set<Point> groundSet = Sets.newHashSet();
			Set<BlockPos> blockSet = Sets.newHashSet();
			Set<BlockPos> outline = Sets.newHashSet();
			for (ServerPos p : serverPositions) {
				world.notifyBlockUpdate(p, world.getBlockState(p), world.getBlockState(p), 1 | 2);
				Rectangle r = consecrationNexi.get(p);
				System.out.println(r);
				for (int x = (int) r.getMinX(); x <= r.getMaxX(); x++) {
					for (int z = (int) r.getMinY(); z <= r.getMaxY(); z++) {
						if (!this.deactivateHolyLand && this.getCreationStage().isComplete()) {
							for (int y = p.getY() - TOTAL_HOLY_LAND_HEIGHT / 2; y <= p.getY()
									+ TOTAL_HOLY_LAND_HEIGHT / 2; y++) {
								blockSet.add(new BlockPos(x, y, z));

							}
						}
						groundSet.add(new Point(x, z));
					}
				}
				if (!this.deactivateHolyLand && this.getCreationStage().isComplete()) {

					BlockPos.Mutable mutable = new BlockPos.Mutable();
					for (int x = (int) r.getMinX() - 1; x <= r.getMaxX() + 1; x++) {

						for (int z = (int) r.getMinY() - 1; z <= r.getMaxY() + 1; z++) {
							for (int y = p.getY() - TOTAL_HOLY_LAND_HEIGHT / 2 - 1; y <= p.getY()
									+ TOTAL_HOLY_LAND_HEIGHT / 2 + 2; y++) {
								mutable.setPos(x, y, z);
								if (!blockSet.contains(mutable)) {
									outline.add(mutable.toImmutable());
								}
							}
						}
					}
				}
			}
			outline.removeIf(blockSet::contains);
			this.consecratedRegion.put(world.getDimensionKey(), blockSet);
			this.consecratedOutline.put(world.getDimensionKey(), outline);
			this.groundArea.put(world.getDimensionKey(), groundSet);
		}
		System.out.println(groundArea.values().stream().flatMap((e) -> e.stream()).count() + " ground area");

	}

	@Override
	public String toString() {
		return this.name + " deity with " + this.invocation;
	}

	public String getHolyBookInfo() {

		// TODO holy book info
		return null;

	}

	public static enum CreationStage {
		/** The deity is not even in the deity register yet */
		EMBRYONIC,
		/** The god has not had its settings finalized */
		RUDIMENTARY, COMPLETE;

		public boolean isComplete() {
			return this == COMPLETE;
		}

		public boolean isRudimentary() {
			return this == RUDIMENTARY;
		}

		public boolean isEmbryonic() {
			return this == EMBRYONIC;
		}
	}

	public static enum Level {
		FIRST, INFUSION, AUTOMATION, WAR, MONOPOLY, APOCALYPSE;
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

		public InvocationItems addAll(Item... stacks) {
			for (Item item : stacks) {
				this.addItem(item);
			}
			return this;
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
			return "Inv" + this.items;
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
