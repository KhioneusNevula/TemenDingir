package com.gm910.temendingir.init;

import com.gm910.temendingir.TemenDingir;
import com.gm910.temendingir.effect.ModEffect;

import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.Potion;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class EffectInit {

	/**
	 * Up to 5?
	 */
	public static final DeferredRegister<Effect> EFFECTS = DeferredRegister.create(ForgeRegistries.POTIONS,
			TemenDingir.MODID);

	public static final DeferredRegister<Potion> POTION_TYPES = DeferredRegister.create(ForgeRegistries.POTION_TYPES,
			TemenDingir.MODID);

	public static final RegistryObject<Effect> GLOWING_HEAD = EFFECTS.register("glowing_head",
			() -> new ModEffect(EffectType.NEUTRAL, 0xFFFF00));

	public static final RegistryObject<Potion> GLOWING_HEAD_POTION = POTION_TYPES.register("glowing_head",
			() -> new Potion(new EffectInstance(GLOWING_HEAD.get(), 1200)));

}
