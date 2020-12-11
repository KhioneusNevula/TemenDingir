package com.gm910.temendingir.world.dimensions;

import com.mojang.serialization.Codec;

import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.feature.template.TemplateManager;

public class DilmunStructure extends Structure<DilmunStructureConfig> {

	public static final Codec<DilmunStructureConfig> DEITY_FIELD_CODEC = Codec.STRING.fieldOf("deity").orElse("")
			.xmap(DilmunStructureConfig::new, (p_236635_0_) -> {
				return p_236635_0_.deityUUID;
			}).codec();

	public DilmunStructure(Codec<DilmunStructureConfig> p_i231989_1_) {
		super(p_i231989_1_);
	}

	@Override
	public Structure.IStartFactory<DilmunStructureConfig> getStartFactory() {
		return DilmunStructure.Start::new;
	}

	public static class Start extends StructureStart<DilmunStructureConfig> {
		public Start(Structure<DilmunStructureConfig> p_i225817_1_, int p_i225817_2_, int p_i225817_3_,
				MutableBoundingBox p_i225817_4_, int p_i225817_5_, long p_i225817_6_) {
			super(p_i225817_1_, p_i225817_2_, p_i225817_3_, p_i225817_4_, p_i225817_5_, p_i225817_6_);
		}

		@Override
		public void func_230364_a_(DynamicRegistries p_230364_1_, ChunkGenerator generator, TemplateManager p_230364_3_,
				int p_230364_4_, int p_230364_5_, Biome p_230364_6_, DilmunStructureConfig p_230364_7_) {

			Rotation rotation = Rotation.randomRotation(this.rand);
			BlockPos blockpos = new BlockPos(p_230364_4_ * 16, 90, p_230364_5_ * 16);
			DilmunStructurePiece.func_204760_a(p_230364_3_, blockpos, rotation, this.components, this.rand,
					p_230364_7_);
			this.recalculateStructureSize();
		}
	}
}
