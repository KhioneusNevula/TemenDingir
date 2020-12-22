package com.gm910.temendingir.blocks.tile.invokers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.gm910.temendingir.TemenDingir;
import com.gm910.temendingir.api.networking.messages.Networking;
import com.gm910.temendingir.api.networking.messages.types.TaskParticles;
import com.gm910.temendingir.api.util.GMNBT;
import com.gm910.temendingir.api.util.ServerPos;
import com.gm910.temendingir.damage.DivineDamageSource;
import com.gm910.temendingir.init.BlockInit;
import com.gm910.temendingir.init.TileInit;
import com.gm910.temendingir.world.gods.cap.dilmunmanager.DeityDilmunSettings.SettingType;
import com.gm910.temendingir.world.gods.cap.dilmunmanager.WorshipMethod;
import com.google.common.collect.Sets;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.WallBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.pattern.BlockMaterialMatcher;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.block.pattern.BlockStateMatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.CachedBlockInfo;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = TemenDingir.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FireOfWorship extends InvokerTileEntity implements ITickableTileEntity {

	private Set<Entity> graspedEntities = new HashSet<>();
	/**
	 * Only used when read/writeNBT and onLoad is called to ensure we can access the
	 * actual entities once te world loads
	 */
	private Set<UUID> graspedIds = new HashSet<>();

	private double maxGraspDistance = 5;

	private static BlockPattern fireOfWorshipPattern;

	public FireOfWorship() {
		super(TileInit.FIRE_OF_WORSHIP.get());
	}

	@Override
	protected void revertState() {
		world.setBlockState(pos, Blocks.TORCH.getDefaultState());
	}

	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		CompoundNBT nbt = new CompoundNBT();

		return new SUpdateTileEntityPacket(pos, -1, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		super.onDataPacket(net, pkt);
	}

	@Override
	public void tick() {
		if (world.isRemote) {

			return;
		}
		if (this.invoked == null)
			return;
		if (this.invoked.getCreationStage().isComplete()) {
			doSacrifices();

		} else {

			if (!world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos)).isEmpty())
				System.out.println("Deity " + invoked.getName() + " needs settings " + SettingType.values().stream()
						.filter((e) -> !(invoked.getSettings().areSettingsValid(e))).collect(Collectors.toSet()));

		}

	}

	public void doSacrifices() {
		List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos).grow(0.3));
		Set<WorshipMethod> worshipMethods = invoked.getSettings().getActiveSettingsFor(SettingType.WORSHIP_METHODS);

		for (Entity en : entities) {
			boolean valid = false;
			for (WorshipMethod method : worshipMethods) {
				if (!method.isValidSacrifice(en)) {

					continue;
				}
				this.graspedEntities.add(en);
				valid = true;
				break;
			}
			if (!valid) {
				System.out.println("Launching " + en + (en instanceof ItemEntity ? ((ItemEntity) en).getItem() : ""));
				en.addVelocity(world.rand.nextDouble() * 0.5 - 0.25, 0.1, en.world.rand.nextDouble() * 0.5 - 0.25);
				for (int i = 0; i < 20; i++)
					Networking.sendToTracking(new TaskParticles(ParticleTypes.ENCHANTED_HIT,
							en.getPosX() + world.rand.nextDouble() - 0.5, en.getPosY() + world.rand.nextDouble() - 0.5,
							en.getPosZ() + world.rand.nextDouble() - 0.5, en.world.getDimensionKey().getLocation(),
							world.rand.nextDouble() * 4 - 2, world.rand.nextDouble() * 4 - 2,
							world.rand.nextDouble() * 4 - 2, true, false, false), en);
			}
		}

		for (Entity en : Sets.newHashSet(this.graspedEntities)) {
			en.setFire(10);
			Vector3d posVec = new Vector3d(this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5);
			Vector3d toward = (posVec).subtract(en.getPositionVec());
			double dist = posVec.distanceTo(en.getPositionVec());
			toward = toward.normalize().mul(dist * 0.2, dist * 0.2, dist * 0.2);
			//Vector3d destinationVec = en.getPositionVec().add(toward);
			en.setMotion(en.getMotion().add(toward));
			if (en instanceof LivingEntity) {
				((LivingEntity) en).attackEntityFrom(DivineDamageSource.holyFireDamage(invoked), 5 / (float) dist);

			} else {
				en.remove();

			}
			if (en.removed) {
				for (WorshipMethod method : worshipMethods) {
					double pts = method.getSacrificePoints(en);

					if (pts == 0)
						continue;
					int didSacrifice = invoked.getEnergyStorage().makeSacrifice(world, en, new ServerPos(this), method,
							pts);
					graspedEntities.remove(en);
					break;
				}
			}
		}
		graspedEntities.removeIf(
				(e) -> e.getDistanceSq(Vector3d.copy(pos).add(0.5, 0.5, 0.5)) > Math.pow(maxGraspDistance, 2));
	}

	@Override
	protected BlockPattern getCompletedPattern() {
		// TODO Auto-generated method stub
		return getGenerationPattern();
	}

	public static BlockPattern getGenerationPattern() {
		//if (fireOfWorshipPattern == null) {
		fireOfWorshipPattern = BlockPatternBuilder.start().aisle("???", "f?f", "sss").aisle("?t?", "?w?", "sss")
				.aisle("???", "f?f", "sss").where('?', CachedBlockInfo.hasState(BlockStateMatcher.ANY))
				.where('s', CachedBlockInfo.hasState(BlockMaterialMatcher.forMaterial(Material.ROCK)))
				.where('f', CachedBlockInfo.hasState((e) -> e.isIn(BlockTags.FENCES)))
				.where('w', CachedBlockInfo.hasState((e) -> e.getBlock() instanceof WallBlock))
				.where('t', CachedBlockInfo.hasState(BlockStateMatcher.forBlock(Blocks.TORCH)
						.or(BlockStateMatcher.forBlock(BlockInit.FIRE_OF_WORSHIP.get()))))
				.build();
		//}

		return fireOfWorshipPattern;
	}

	@Override
	public void read(BlockState state, CompoundNBT nbt) {
		super.read(state, nbt);
		this.graspedIds.clear();
		this.graspedIds.addAll(GMNBT.createUUIDList(((ListNBT) nbt.get("Grasped"))));
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		super.write(compound);
		this.graspedIds.clear();
		this.graspedIds.addAll(this.graspedEntities.stream().map((e) -> e.getUniqueID()).collect(Collectors.toSet()));
		compound.put("Grasped", GMNBT.makeUUIDList(this.graspedIds));

		return compound;
	}

	@Override
	public void onLoad() {
		super.onLoad();
		if (!world.isRemote) {
			this.graspedEntities.clear();
			this.graspedEntities.addAll(graspedIds.stream()
					.map((e) -> ServerPos.getEntityFromUUID(e, world.getServer())).collect(Collectors.toSet()));
		}

	}

}