package com.gm910.temendingir.world.dimensions;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.gm910.temendingir.TemenDingir;
import com.gm910.temendingir.api.util.ServerPos;
import com.gm910.temendingir.init.BlockInit;
import com.gm910.temendingir.world.gods.Deity;
import com.gm910.temendingir.world.gods.cap.DeityData;
import com.gm910.temendingir.world.gods.cap.dilmunmanager.DeityDilmunSettings;
import com.gm910.temendingir.world.gods.cap.dilmunmanager.DeityDilmunSettings.SettingType;
import com.gm910.temendingir.world.gods.cap.dilmunmanager.SettingTypeEnum;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.structure.IStructurePieceType;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.structure.TemplateStructurePiece;
import net.minecraft.world.gen.feature.template.BlockIgnoreStructureProcessor;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;

public class DilmunStructurePiece {
	public static final ResourceLocation RL = new ResourceLocation(TemenDingir.MODID, "dilmun_designer");

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
			this.deityUUID = UUID.fromString(p_i50445_2_.getString("Deity"));
			this.setupStuff(p_i50445_1_);
		}

		@Override
		public boolean func_230383_a_(ISeedReader w, StructureManager p_230383_2_, ChunkGenerator p_230383_3_,
				Random p_230383_4_, MutableBoundingBox p_230383_5_, ChunkPos p_230383_6_, BlockPos p_230383_7_) {

			/*
			 * w.getWorld().getServer().getPlayerList().getPlayers().forEach((e) -> e
			 * .sendMessage(new
			 * StringTextComponent("Reached construction stage of generation"),
			 * e.getUniqueID()));
			 */
			return super.func_230383_a_(w, p_230383_2_, p_230383_3_, p_230383_4_, p_230383_5_, p_230383_6_,
					p_230383_7_);
		}

		/**
		 * (abstract) Helper method to read subclass data from NBT
		 */
		@Override
		protected void readAdditional(CompoundNBT tagCompound) {
			super.readAdditional(tagCompound);
			tagCompound.putString("Template", this.rl.toString());
			tagCompound.putString("Deity", this.deityUUID + "");
		}

		private void setupStuff(TemplateManager p_204754_1_) {
			Template template = p_204754_1_.getTemplateDefaulted(this.rl);
			PlacementSettings placementsettings = (new PlacementSettings())
					.addProcessor(BlockIgnoreStructureProcessor.AIR_AND_STRUCTURE_BLOCK);
			this.setup(template, this.templatePosition, placementsettings);
		}

		/**
		 * Information:<br>
		 * all "portal" data blocks face south; blportal is the bottom left portal<br>
		 * pronountt, pronounss, pronounii, pronounhh -- wall signs, with all facing
		 * north, determining deity pronouns<br>
		 * signcom = south facing sign giving commandment info this<br>
		 * Commandments [com] (south facing wall signs)<br>
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
		 * signwormet = east facing wall sign, worship info <br>
		 * Worship methods [wormet] (east facing wall signs)<br>
		 * <ul>
		 * <li>wormethuman ->Human Sacrifice
		 * <li>wormethostile -> Hostile Mob Sacrifice
		 * <li>wormetpassive -> Passive Mob Sacrifice
		 * <li>wormettool -> Tool Sacrifice
		 * <li>wormetfood -> Food Sacrifice
		 * <li>wormetplant -> Plant Sacrifice
		 * <li>wormetwealth -> Wealth Sacrifice
		 * </ul>
		 * signworcon = east facing wall sign, worship condition info <br>
		 * Worship Conditions [worcon] (east facing wall signs)<br>
		 * <ul>
		 * <li>worconday = worship in day
		 * <li>worconnight = worship at night
		 * <li>worconbuilding = worship in building
		 * <li>worconlibrary = worship in library
		 * <li>worconnether = worship in nether
		 * <li>worcontwilight = worship in twilight forest; TF compat
		 * </ul>
		 * signwormod = east facing wall sign, worship modifier info <br>
		 * Worship Modifiers [wormod] (east facing wall signs)<br>
		 * <ul>
		 * <li>wormodnature = nature
		 * <li>wormodlibrary = library
		 * <li>wormodflame = flame
		 * <li>wormodwater = water
		 * <li>wormodart = art
		 * <li>wormoddecoration = decoration
		 * </ul>
		 * signconper = north facing wall sign, consecration permission info <br>
		 * Consecration Permissions [conper] (north wall signs)<br>
		 * <ul>
		 * <li>conpernogriefing = no griefing
		 * <li>conperadventure = adventure mode
		 * <li>conpernohurting = no hurting
		 * <li>conpernosleeping = no sleeping
		 * <li>conpernoopening = no opening
		 * <li>conpernotouching = no touching
		 * <li>conpernointeracting = no interacting
		 * <li>conpernoentry = no entry
		 * <li>conperharmentry = no entry (harm)
		 * </ul>
		 * signconpro = north facing wall sign, consecration protection info <br>
		 * Consecration Protections [conpro] (north wall signs)<br>
		 * <ul>
		 * <li>conpronoharm = no harm
		 * <li>conprohealing = healing
		 * <li>conpronohunger = no hunger
		 * <li>conpronodrowning = no drowning
		 * <li>conprodiscount = discounted trade
		 * </ul>
		 * signrel = south facing wall sign, relationship info <br>
		 * Relationship Types [rel] (south wall signs)<br>
		 * <ul>
		 * <li>relwarlike = warlike
		 * <li>relinvasive = invasive
		 * <li>relpeaceful = peaceful
		 * </ul>
		 */
		@Override
		protected void handleDataMarker(String function, BlockPos pos, IServerWorld worldIn, Random rand,
				MutableBoundingBox sbb) {

			/*
			 * worldIn.getWorld().getServer().getPlayerList().getPlayers().forEach((e) -> e
			 * .sendMessage(new StringTextComponent("Reached data gen stage of generation"),
			 * e.getUniqueID()));
			 */
			// TODO complete the handling of the data marker
			DeityDilmunSettings settings = this.getDeity(worldIn).getSettings();

			if (function.equals("blportal")) {
				settings.setExitPortal(pos);
			}

			if (function.endsWith("portal")) {
				worldIn.setBlockState(pos, BlockInit.DILMUN_EXIT_PORTAL.get().getDefaultState(), 1);
			}

			for (SettingType<?> type : DeityDilmunSettings.SettingType.values()) {
				if (!function.startsWith(type.prefix)) {

					if (!function.equals("sign" + type.prefix)) {

						continue;
					}
					settings.setSignPos(type, new ServerPos(pos, worldIn.getWorld().getDimensionKey()));
					type.updateSigns(settings);

					continue;
				}
				String data = function.substring(type.prefix.length());
				SettingTypeEnum enom = type.fromId(data);
				if (enom == null) {
					System.out.println("No enum for " + data + " under type " + type);
					return;
				}
				settings.setPos(enom, new ServerPos(pos, worldIn.getWorld().getDimensionKey()));
				type.updateBlock(enom, settings);

			}
		}

	}
}
