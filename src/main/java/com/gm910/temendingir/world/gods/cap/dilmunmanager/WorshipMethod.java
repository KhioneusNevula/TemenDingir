package com.gm910.temendingir.world.gods.cap.dilmunmanager;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import com.gm910.temendingir.init.ItemInit;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import it.unimi.dsi.fastutil.objects.Object2DoubleFunction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.INPC;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.IItemTier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTier;
import net.minecraft.item.TieredItem;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag.INamedTag;
import net.minecraft.tags.ItemTags;
import net.minecraftforge.common.brewing.BrewingRecipe;

public enum WorshipMethod implements SettingTypeEnum {

	HUMAN("human", 1,
			(e) -> (e instanceof PlayerEntity || e instanceof AbstractVillagerEntity)
					? ((LivingEntity) e).getMaxHealth()
					: 0),
	HOSTILE("hostile", 1, (e) -> {
		if (e instanceof MobEntity) {
			MobEntity mob = (MobEntity) e;
			if (mob.getType().getClassification() == EntityClassification.MONSTER) {
				return mob.getMaxHealth();
			}
		}

		return 0f;
	}),

	PASSIVE("passive", 2, (e) -> {
		if (e instanceof MobEntity) {
			MobEntity mob = (MobEntity) e;

			if (mob.getType().getClassification().getAnimal() && !(mob instanceof INPC)) {
				return mob.getMaxHealth();
			}
		}

		return 0f;
	}), TOOL("tool", 3, (e) -> {

		if (e instanceof ItemEntity) {
			double ret = 0;
			ItemEntity en = (ItemEntity) e;
			ItemStack s = en.getItem();
			Item i = s.getItem();
			if (!s.getToolTypes().isEmpty()) {
				ret = 7f;
				if (i instanceof TieredItem) {
					TieredItem t = (TieredItem) i;
					IItemTier tier = t.getTier();
					ret += (tier.getHarvestLevel() + 1) * 0.03 * (s.getMaxDamage() - s.getDamage());
				}
			}
			return ret;
		}
		return 0;
	}), FOOD("food", 5, (e) -> e instanceof ItemEntity && ((ItemEntity) e).getItem().getItem().isFood() ? 1f : 0f),
	PLANT("plant", 6, (e) -> {
		if (e instanceof ItemEntity) {
			ItemStack s = ((ItemEntity) e).getItem();
			Item i = s.getItem();
			float ret = 0f;
			INamedTag<?>[] itags = { ItemTags.CRIMSON_STEMS, ItemTags.LEAVES, ItemTags.LOGS, ItemTags.SAPLINGS,
					ItemTags.WARPED_STEMS, ItemInit.SPECIAL_PLANTS_TAG };
			INamedTag<?>[] blocks = { BlockTags.CROPS, BlockTags.FLOWERS };
			for (INamedTag<?> t : itags) {
				INamedTag<Item> tag = (INamedTag<Item>) t;
				if (i.isIn(tag)) {
					ret = tag == ItemInit.SPECIAL_PLANTS_TAG ? 10f : 1f;
				}

			}
			return ret * s.getCount();

		}
		return 0f;
	}), WEALTH("wealth", 3, (e) -> {
		if (e instanceof ItemEntity) {
			ItemStack s = ((ItemEntity) e).getItem();
			Item i = s.getItem();
			double ret = 0f;
			switch (i.getRarity(s)) {

			case UNCOMMON:
				ret = 800f;
				System.out.println("Found uncommon item " + s);
				break;
			case RARE:
				ret = 6000f;
				System.out.println("Found rare item " + s);
				break;
			case EPIC:
				ret = 12000f;
				System.out.println("Found epic item " + s);
				break;
			default:
				break;
			}
			if (ret == 0) {

				Set<ItemTier> tiers = Sets.newHashSet(ItemTier.values());
				for (ItemTier tier : tiers) {
					if (tier == ItemTier.WOOD)
						continue;
					double am = 0.01 * (tier.getMaxUses());
					Ingredient repair = tier.getRepairMaterial();
					if (repair.test(s)) {
						ret = am * s.getCount();
						System.out.println("Found armor material " + s + " as " + tier + " and set to " + ret);
						break;
					}
					System.out.println("Found no armor material for " + s + " as " + tier);
					Collection<IRecipe<?>> recipes = ((ItemEntity) e).world.getServer().getRecipeManager().getRecipes()
							.stream()
							.filter((o) -> (o instanceof ICraftingRecipe || o instanceof FurnaceRecipe
									|| o instanceof BrewingRecipe) && !o.getRecipeOutput().isEmpty())
							.collect(Collectors.toSet());
					rec: for (IRecipe<?> recipe : recipes) {
						boolean change = false;

						for (Ingredient in : recipe.getIngredients()) {
							for (ItemStack vers : repair.getMatchingStacks()) {
								if (recipe.getRecipeOutput().getItem() == i && in.test(vers)) {
									System.out.println("Found recipe " + recipe + " and matched ingredient " + in
											+ " with  armor material ingredient " + vers + " and added " + am);
									ret += am;
									change = true;
									break;
								} else if (in.test(s) && repair.test(recipe.getRecipeOutput())) {
									System.out.println("Found recipe " + recipe + " and matched ingredient " + in
											+ " with sacrificed item stack " + s + " because result was armor material "
											+ recipe.getRecipeOutput() + " and added " + am);
									ret += (recipe instanceof FurnaceRecipe ? am * 0.5
											: am / recipe.getIngredients().size());
									change = true;
									break rec;
								}
							}
						}
						if (recipe.getRecipeOutput().getItem() == i) {

							System.out.println("Recipe " + recipe + " of type " + recipe.getType()
									+ " with ingredients " + recipe.getIngredients() + " failed for " + s);
						} else if (repair.test(recipe.getRecipeOutput())) {

							System.out.println("Recipe " + recipe + " of type " + recipe.getType()
									+ " with ingredients " + recipe.getIngredients() + " and output "
									+ recipe.getRecipeOutput() + " failed");
						}
						if (change)
							break;

					}
				}
			}
			if (ret == 0) {
				System.out.println("No wealth value for " + s);
			}

			return ret * s.getCount();
		}
		return 0f;
	});

	public final String id;
	public final int points;
	private final Object2DoubleFunction<Entity> pointsFunc;

	private WorshipMethod(String id, int points, Object2DoubleFunction<Entity> pointsFunc) {
		this.id = id;
		this.points = points;
		this.pointsFunc = pointsFunc;
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

	public boolean isValidSacrifice(Entity e) {
		return this.pointsFunc.apply(e) > 0;
	}

	public double getSacrificePoints(Entity e) {
		return this.pointsFunc.applyAsDouble(e);
	}
}