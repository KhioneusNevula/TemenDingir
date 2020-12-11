package com.gm910.temendingir.api.util;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class GMWorld {

	public static <T extends LightningBoltEntity> T summonLightning(@Nullable EntityType<T> type, World world,
			BlockPos blockpos, @Nullable ServerPlayerEntity caster) {

		LightningBoltEntity lightningboltentity = type != null ? type.create(world)
				: EntityType.LIGHTNING_BOLT.create(world);
		lightningboltentity.moveForced(Vector3d.copyCenteredHorizontally(blockpos));
		lightningboltentity.setCaster(caster);
		world.addEntity(lightningboltentity);
		return (T) lightningboltentity;
	}

	public static <T extends LightningBoltEntity> T summonMagicLightning(DamageSource source, World world,
			BlockPos blockpos, @Nullable ServerPlayerEntity caster) {

		LightningBoltEntity lightningboltentity = new MagicLightning(world, source);
		lightningboltentity.moveForced(Vector3d.copyCenteredHorizontally(blockpos));
		lightningboltentity.setCaster(caster);
		world.addEntity(lightningboltentity);
		return (T) lightningboltentity;
	}

	public static double getSmallOffset(double range) {
		return ((new Random())).nextDouble() * range - range / 2;
	}
}
