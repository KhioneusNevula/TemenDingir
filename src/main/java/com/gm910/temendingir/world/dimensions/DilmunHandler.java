package com.gm910.temendingir.world.dimensions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.gm910.temendingir.TemenDingir;
import com.gm910.temendingir.init.DimensionInit;
import com.gm910.temendingir.world.gods.Deity;
import com.gm910.temendingir.world.gods.cap.DeityData;
import com.google.common.collect.Lists;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class DilmunHandler {

	public static final ResourceLocation DILMUN_DESIGNER_RL = new ResourceLocation(TemenDingir.MODID,
			"dilmun_designer");

	@SubscribeEvent
	public static void claimDilmunForDeity(PlayerEvent.PlayerChangedDimensionEvent event) {
		if (event.getTo().equals(DimensionInit.DILMUN)) {
			DeityData data = DeityData.get(event.getPlayer().getServer());
			Deity deity = data.getFromFollowerUUID(event.getPlayer().getUniqueID());
			if (deity == null)
				return;
			if (!deity.getTravelingPlayers().contains(event.getPlayer().getUniqueID())) {
				return;
			}
			if (deity.getDilmunChunk() != null)
				return;
			ChunkPos toClaim = claimNewDilmunChunk(deity);
			deity.setDilmunChunk(toClaim);
			buildDilmunStructure(toClaim, deity);
			if (event.getPlayer().getTags().contains("traveler")) {
				event.getPlayer().setPosition(toClaim.getXStart() + 8, 100, toClaim.getZStart() + 8);
				event.getPlayer().removeTag("traveler");
			}
		}
	}

	private static ChunkPos claimNewDilmunChunk(Deity deity) {
		Set<ChunkPos> claimeds = deity.getData().getDeities().stream().flatMap((e) -> {
			ChunkPos c = e.getDilmunChunk();
			if (c != null) {
				return Lists.newArrayList(c).stream();
			} else {
				return Lists.<ChunkPos>newArrayList().stream();
			}
		}).collect(Collectors.toSet());
		if (claimeds.isEmpty()) {
			return new ChunkPos(0, 0);
		}
		Set<ChunkPos> candidates = new HashSet<>();
		for (ChunkPos claimed : claimeds) {
			for (int x = -2; x <= 2; x += 2) {
				for (int z = -2; z <= 2; z += 2) {
					if (x == 0 && z == 0)
						continue;
					ChunkPos new1 = new ChunkPos(claimed.x + x, claimed.z + z);
					if (!claimeds.contains(new1)) {
						candidates.add(new1);
					}
				}
			}
		}
		if (candidates.isEmpty()) {
			throw new IllegalStateException("For some reason, we found no claimable chunks around " + claimeds);
		}
		List<ChunkPos> candidatesSorted = new ArrayList<>(candidates);
		candidatesSorted.sort((c1, c2) -> {

			return (int) (c1.asBlockPos().distanceSq(new BlockPos(0, 0, 0))
					- c2.asBlockPos().distanceSq(new BlockPos(0, 0, 0)));
		});
		return candidatesSorted.get(0);
	}

	private static void buildDilmunStructure(ChunkPos pos, Deity d) {

	}

	public boolean loadStructure(ResourceLocation name, ServerWorld world, BlockPos blockpos) {

		TemplateManager manager = world.getStructureTemplateManager();

		Template template;
		try {
			template = manager.getTemplate(name);
		} catch (ResourceLocationException resourcelocationexception) {
			return false;
		}

		PlacementSettings placementsettings = (new PlacementSettings()).setIgnoreEntities(true)
				.setChunk(new ChunkPos(blockpos));

		BlockPos blockpos2 = blockpos;
		template.func_237144_a_(world, blockpos2, placementsettings, world.rand);
		return true;

	}

}
