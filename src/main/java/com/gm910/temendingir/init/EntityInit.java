package com.gm910.temendingir.init;

import com.gm910.temendingir.TemenDingir;
import com.gm910.temendingir.api.util.MagicLightning;
import com.gm910.temendingir.api.util.MagicLightningRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class EntityInit {

	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITIES,
			TemenDingir.MODID);

	public static final RegistryObject<EntityType<MagicLightning>> MAGIC_LIGHTNING = ENTITY_TYPES.register(
			"magic_lightning",
			() -> EntityType.Builder.<MagicLightning>create(MagicLightning::new, EntityClassification.MISC)
					.disableSerialization().size(0, 0).func_233608_b_(Integer.MAX_VALUE).trackingRange(16)
					.build("magic_lightning"));

	public static void registerRenderers() {

		Minecraft.getInstance().getRenderManager().register(MAGIC_LIGHTNING.get(),
				new MagicLightningRenderer(Minecraft.getInstance().getRenderManager()));
	}
}
