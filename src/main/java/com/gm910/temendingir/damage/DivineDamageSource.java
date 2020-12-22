package com.gm910.temendingir.damage;

import javax.annotation.Nullable;

import com.gm910.temendingir.world.gods.Deity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class DivineDamageSource extends DamageSource {
	@Nullable
	protected final Deity divineSource;

	public static DivineDamageSource holyLandDamage(Deity source) {
		return (DivineDamageSource) new DivineDamageSource("land", source).setDamageIsAbsolute();
	}

	public static DivineDamageSource holyFireDamage(Deity source) {
		return (DivineDamageSource) new DivineDamageSource("fire", source).setFireDamage().setDamageIsAbsolute();
	}

	public DivineDamageSource(String damageTypeIn, Deity divineSource) {
		super(damageTypeIn);
		this.divineSource = divineSource;
		this.setMagicDamage();
		this.setDamageBypassesArmor();
	}

	/**
	 * Retrieves the deity who the power came from
	 */
	@Nullable
	public Deity getDivineSource() {
		return this.divineSource;
	}

	/**
	 * Gets the death message that is displayed when the player dies
	 */
	@Override
	public ITextComponent getDeathMessage(LivingEntity entityLivingBaseIn) {
		String s = "death.divine." + this.damageType + "." + this.divineSource.getPronounCode();
		return new TranslationTextComponent(s, entityLivingBaseIn.getDisplayName(), this.divineSource.getName());
	}

	@Override
	public String toString() {
		return "DeityDamageSource (" + this.divineSource + ")";
	}
}
