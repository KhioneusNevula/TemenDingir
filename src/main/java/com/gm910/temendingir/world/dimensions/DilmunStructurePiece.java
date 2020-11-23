package com.gm910.temendingir.world.dimensions;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.gm910.temendingir.TemenDingir;
import com.gm910.temendingir.world.gods.Deity;
import com.gm910.temendingir.world.gods.cap.DeityData;
import com.gm910.temendingir.world.gods.cap.DeityDilmunManager;
import com.gm910.temendingir.world.gods.cap.DeityDilmunManager.SettingType;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.gen.feature.structure.IStructurePieceType;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.structure.TemplateStructurePiece;
import net.minecraft.world.gen.feature.template.BlockIgnoreStructureProcessor;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;

public class DilmunStructurePiece {
	private static final ResourceLocation RL = new ResourceLocation(TemenDingir.MODID, "dilmun_designer");

	public static final IStructurePieceType DILMUN = DilmunStructurePiece.Piece::new;

	public static void func_204760_a(TemplateManager p_204760_0_, BlockPos p_204760_1_, Rotation p_204760_2_,
			List<StructurePiece> p_204760_3_, Random p_204760_4_, DilmunStructureConfig config) {

		DilmunStructurePiece.Piece piece = new DilmunStructurePiece.Piece(p_204760_0_, RL, p_204760_1_, p_204760_2_);
		piece.deityUUID = UUID.fromString(config.deityUUID);
		p_204760_3_.add(piece);
	}

	public static class Piece extends TemplateStructurePiece {
		private final ResourceLocation rl;
		private UUID deityUUID;

		public Piece(TemplateManager manager, ResourceLocation p_i48904_2_, BlockPos p_i48904_3_,
				Rotation p_i48904_4_) {
			super(DILMUN, 0);
			this.templatePosition = p_i48904_3_;
			this.rl = p_i48904_2_;
			this.setupStuff(manager);

		}

		public Deity getDeity(IServerWorld world) {
			return DeityData.get(world.getWorld().getServer()).getFromUUID(deityUUID);
		}

		public Piece(TemplateManager p_i50445_1_, CompoundNBT p_i50445_2_) {
			super(DILMUN, p_i50445_2_);
			this.rl = new ResourceLocation(p_i50445_2_.getString("Template"));
			this.setupStuff(p_i50445_1_);
		}

		/**
		 * (abstract) Helper method to read subclass data from NBT
		 */
		@Override
		protected void readAdditional(CompoundNBT tagCompound) {
			super.readAdditional(tagCompound);
			tagCompound.putString("Template", this.rl.toString());
		}

		private void setupStuff(TemplateManager p_204754_1_) {
			Template template = p_204754_1_.getTemplateDefaulted(this.rl);
			PlacementSettings placementsettings = (new PlacementSettings())
					.addProcessor(BlockIgnoreStructureProcessor.AIR_AND_STRUCTURE_BLOCK);
			this.setup(template, this.templatePosition, placementsettings);
		}

		/**
		 * Information:<br>
		 * all "portal" data blocks face south<br>
		 * pronountt, pronounss, pronounii, pronounhh -- wall signs, with all facing
		 * north, determining deity pronouns<br>
		 * commandmentssign = south facing sign giving commandment info<br>
		 * Commandments (south facing wall signs)<br>
		 * <ul>
		 * <li>comnokillpn -> no killing passive or neutral mobs
		 * <li>comnokillh -> no kill hostile mobs
		 * <li>comloveoa -> love one another
		 * <li>comloveyn -> love neighbor
		 * <li>comnopotion -> no potioncraft
		 * <li>comnoench -> no enchanting
		 * <li>comnocurse -> no cursing
		 * <li>comnochestgear -> no wearing chest gear
		 * <li>comnomeat -> no eating meat
		 * <li>comnocrops -> no eatn crops
		 * <li>comnofalseidols -> no false idols
		 * <li>comnosleep -> no sleep
		 * </ul>
		 */
		@Override
		protected void handleDataMarker(String function, BlockPos pos, IServerWorld worldIn, Random rand,
				MutableBoundingBox sbb) {

			// TODO
			DeityDilmunManager settings = this.getDeity(worldIn).getSettings();
			for (SettingType<?> type : DeityDilmunManager.SettingType.values()) {

			}
		}

	}
}
