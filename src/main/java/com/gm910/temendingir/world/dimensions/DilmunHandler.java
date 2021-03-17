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
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class DilmunHandler {

	public static final ResourceLocation DILMUN_DESIGNER_RL = new ResourceLocation(TemenDingir.MODID,
			"dilmun_designer");

	@SubscribeEvent
	public static void breakBlock(BlockEvent.BreakEvent event) {
		if (event.getWorld() instanceof ServerWorld) {
			if (((ServerWorld) event.getWorld()).getDimensionKey().getLocation()
					.equals(DimensionInit.DILMUN.getLocation())) {
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public static void placeBlock(BlockEvent.EntityPlaceEvent event) {
		if (event.getWorld() instanceof ServerWorld) {
			if (((ServerWorld) event.getWorld()).getDimensionKey().getLocation()
					.equals(DimensionInit.DILMUN.getLocation())) {
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public static void explode(ExplosionEvent.Detonate event) {
		if (!event.getWorld().isRemote && event.getWorld().getDimensionKey() == DimensionInit.DILMUN) {
			event.getAffectedBlocks().clear();
		}
	}

	@SubscribeEvent
	public static void claimDilmunForDeity(PlayerEvent.PlayerChangedDimensionEvent event) {
		if (event.getTo().equals(DimensionInit.DILMUN)) {
			DeityData data = DeityData.get(event.getPlayer().getServer());
			Deity deity = data.getFromFollowerUUID(event.getPlayer().getUniqueID());
			if (deity == null) {
				System.out.println("Godless player traveling to dilmun");
				return;
			}
			new Thread(() -> {
				synchronized (deity) {
					if (deity.getDilmunChunk() == null) {
						System.out.println("Claiming a dilmunic chunk for deity " + deity);
						ChunkPos toClaim = claimNewDilmunChunk(deity);

						deity.setDilmunChunk(toClaim);
						System.out.println("Constructing a dilmunic structure for deity " + deity);
						buildDilmunStructure(toClaim, deity);
					}
					ChunkPos dchunk = deity.getDilmunChunk();
					if (deity.getExtraEntityInfo(event.getPlayer().getUniqueID()).contains("PositionBeforeDilmun")) {
						System.out.println("Sending player " + event.getPlayer().getDisplayName().getString()
								+ " to dilmunic chunk for deity " + deity + " at " + dchunk);
						BlockPos positionTo = deity.getSettings().getExitPortal();
						event.getPlayer().setPosition(positionTo.getX(), positionTo.getY() + 0.3, positionTo.getZ());
					}
				}
			}, "dilmundimensionTravel" + event.getPlayer().getScoreboardName()).start();
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
			System.out.println("Found " + new ChunkPos(0, 0) + " for " + deity);
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
		System.out.println("Found " + candidatesSorted.get(0) + " for " + deity);
		return candidatesSorted.get(0);
	}

	private static void buildDilmunStructure(ChunkPos pos, Deity d) {
		ServerWorld dilmun = d.getData().getServer().getWorld(DimensionInit.DILMUN);
		for (int x = pos.x - 1; x <= 1; x++) {
			for (int z = pos.z - 1; z <= 1; z++) {
				dilmun.forceChunk(x, z, false);
			}
		}
		boolean suc = loadStructure(DILMUN_DESIGNER_RL, dilmun, pos.asBlockPos(), d);
		System.out.println((suc ? "Loaded " : "Failed to load ") + "dilmunic structure for " + d + " at " + pos);
	}

	public static boolean loadStructure(ResourceLocation name, ServerWorld world, BlockPos blockpos, Deity deity) {
		System.out.println("Loading " + name + " for " + deity);
		DilmunStructureConfig config = new DilmunStructureConfig(deity.getUuid().toString());
		StructureManager manager = world.func_241112_a_();

		TemplateManager tempman = world.getStructureTemplateManager();
		Template template;
		try {
			template = tempman.getTemplate(name);
		} catch (ResourceLocationException resourcelocationexception) {
			return false;
		}
		BlockPos size = template.getSize();
		MutableBoundingBox box = MutableBoundingBox.createProper(blockpos.getX(), blockpos.getY(), blockpos.getZ(),
				blockpos.getX() + size.getX(), blockpos.getY() + size.getY(), blockpos.getZ() + size.getZ());
		List<StructurePiece> templist = Lists.newArrayList();
		DilmunStructurePiece.func_204760_a(tempman, blockpos, Rotation.NONE, templist, world.rand, config);
		DilmunStructurePiece.Piece piece = (DilmunStructurePiece.Piece) templist.get(0);
		return piece.func_230383_a_(world, manager, world.getChunkProvider().getChunkGenerator(), world.rand, box,
				new ChunkPos(blockpos), blockpos);
		/*
		 * DilmunStructure.Start start = (Start)
		 * StructureInit.DILMUN_DESIGNER_DEFAULT_STRUCTURE.getStartFactory().create(
		 * StructureInit.DILMUN_DESIGNER_DEFAULT_STRUCTURE, blockpos.getX(),
		 * blockpos.getZ(), box, 1, world.getSeed());
		 * start.func_230364_a_(world.func_241828_r(),
		 * world.getChunkProvider().getChunkGenerator(),
		 * world.getStructureTemplateManager(), blockpos.getX(), blockpos.getX(),
		 * world.getBiome(blockpos), config); start.func_230366_a_(world, manager,
		 * world.getChunkProvider().getChunkGenerator(), world.rand, box, new
		 * ChunkPos(blockpos)); boolean success = false; boolean first = true; for
		 * (StructurePiece e : start.getComponents()) { e.buildComponent(e,
		 * start.getComponents(), world.rand);
		 * 
		 * boolean res = e.func_230383_a_(world, manager,
		 * world.getChunkProvider().generator, world.rand, box, new ChunkPos(blockpos),
		 * blockpos); if (first) { success = res; first = false; } else { success =
		 * success && res; } }
		 * 
		 * return success;
		 */
		/*
		 * TemplateManager manager = world.getStructureTemplateManager(); Template
		 * template; try { template = manager.getTemplate(name); } catch
		 * (ResourceLocationException resourcelocationexception) { return false; }
		 * 
		 * BlockPos size = template.getSize(); PlacementSettings placementsettings =
		 * (new PlacementSettings()).setIgnoreEntities(true) .setChunk(new
		 * ChunkPos(blockpos))
		 * .setBoundingBox(MutableBoundingBox.createProper(blockpos.getX(),
		 * blockpos.getY(), blockpos.getZ(), blockpos.getX() + size.getX(),
		 * blockpos.getY() + size.getY(), blockpos.getZ() + size.getZ()))
		 * .func_215223_c(true).func_237133_d_(true);
		 * 
		 * 
		 * 
		 * 
		 * BlockPos blockpos2 = blockpos; template.func_237144_a_(world, blockpos2,
		 * placementsettings, world.rand);
		 */

	}

}
