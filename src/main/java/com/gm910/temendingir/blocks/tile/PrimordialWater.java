package com.gm910.temendingir.blocks.tile;

import java.util.List;
import java.util.Random;

import com.gm910.temendingir.TemenDingir;
import com.gm910.temendingir.api.networking.messages.Networking;
import com.gm910.temendingir.api.networking.messages.types.TaskParticles;
import com.gm910.temendingir.api.util.GMHelper;
import com.gm910.temendingir.api.util.GMWorld;
import com.gm910.temendingir.api.util.ServerPos;
import com.gm910.temendingir.init.BlockInit;
import com.gm910.temendingir.init.TileInit;
import com.gm910.temendingir.world.gods.Deity;
import com.gm910.temendingir.world.gods.Deity.CreationStage;
import com.gm910.temendingir.world.gods.Deity.InvocationItems;
import com.gm910.temendingir.world.gods.Deity.NamingConvention;
import com.gm910.temendingir.world.gods.cap.DeityData;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.block.pattern.BlockStateMatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.CachedBlockInfo;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = TemenDingir.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PrimordialWater extends TileEntity implements ITickableTileEntity {

	private Deity embryo;

	private String embryoDisplayName = "";

	private float[] redGreenBlueAlpha = new float[4];

	private int[] redGreenBlueAlphaTicks = GMHelper.create(new int[4], (e) -> {
		Random rand = new Random();
		e[0] = rand.nextInt(10) + 1;
		e[1] = rand.nextInt(10) + 1;
		e[2] = rand.nextInt(10) + 1;
		e[3] = rand.nextInt(10) + 1;
	});

	private static BlockPattern primordialAltarPattern;

	public PrimordialWater() {
		super(TileInit.PRIMORDIAL_WATER.get());
	}

	public void launch(Entity e) {
		System.out.println("Launching " + e + (e instanceof ItemEntity ? ((ItemEntity) e).getItem() : ""));
		e.addVelocity(e.world.rand.nextDouble() - 0.5, 0.3, e.world.rand.nextDouble() - 0.5);
		for (int i = 0; i < 20; i++)
			Networking.sendToTracking(
					new TaskParticles(ParticleTypes.ENCHANTED_HIT, e.getPosX() + world.rand.nextDouble() - 0.5,
							e.getPosY() + world.rand.nextDouble() - 0.5, e.getPosZ() + world.rand.nextDouble() - 0.5,
							e.world.getDimensionKey().getLocation(), world.rand.nextDouble() * 4 - 2,
							world.rand.nextDouble() * 4 - 2, world.rand.nextDouble() * 4 - 2, true, false, false),
					e);
	}

	public void consumeItem(ItemEntity booke) {
		booke.remove();
		System.out.println(booke.getItem() + " consumed");
		for (int i = 0; i < 20; i++)
			Networking.sendToTracking(
					new TaskParticles(ParticleTypes.SOUL_FIRE_FLAME, booke.getPosX() + world.rand.nextDouble() - 0.5,
							booke.getPosY() + world.rand.nextDouble() - 0.5,
							booke.getPosZ() + world.rand.nextDouble() - 0.5,
							booke.world.getDimensionKey().getLocation(), world.rand.nextDouble() * 4 - 2,
							world.rand.nextDouble() * 4 - 2, world.rand.nextDouble() * 4 - 2, true, false, false),
					booke);
	}

	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		CompoundNBT nbt = new CompoundNBT();
		if (embryo != null) {
			nbt.putString("embryoName", embryo.getName());
		}
		return new SUpdateTileEntityPacket(pos, -1, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		super.onDataPacket(net, pkt);
		embryoDisplayName = pkt.getNbtCompound().getString("embryoName");
	}

	@Override
	public void tick() {
		if (world.isRemote) {
			if (this.embryoDisplayName.isEmpty()) {
				return;
			}

			for (int i = 0; i < 4; i++) {
				// System.out.println(Arrays.toString(redGreenBlueAlphaTicks) + " " +
				// Arrays.toString(redGreenBlueAlpha));
				if (redGreenBlueAlphaTicks[i] != 0) {
					redGreenBlueAlphaTicks[i] -= 1;

				} else {
					redGreenBlueAlpha[i] = (float) GMHelper.clamp(redGreenBlueAlpha[i] + 0.1f, 0, 1);

					redGreenBlueAlphaTicks[i] = new Random().nextInt(10) + 1;
				}
			}

			for (int i = 0; i < 60; i++) {
				world.addParticle(
						new RedstoneParticleData(redGreenBlueAlpha[0], redGreenBlueAlpha[1], redGreenBlueAlpha[2],
								redGreenBlueAlpha[3]),
						false, pos.getX() + 0.5 + world.rand.nextFloat() * 0.1 - 0.05,
						pos.getY() + 1 + 0.5 + world.rand.nextFloat() * 0.1 - 0.05,
						pos.getZ() + 0.5 + world.rand.nextFloat() * 0.1 - 0.05, 0, 0, 0);
			}
			for (int i = 0; i < 300; i++) {

				world.addParticle(ParticleTypes.ENCHANT, false, pos.getX() + 0.5 + world.rand.nextFloat() * 0.1 - 0.05,
						pos.getY() + world.rand.nextFloat() * i, pos.getZ() + 0.5 + world.rand.nextFloat() * 0.1 - 0.05,
						0, 0, 0);
			}

			return;
		}
		List<ItemEntity> items = world.getEntitiesWithinAABB(ItemEntity.class, new AxisAlignedBB(this.pos).grow(0.2));
		DeityData data = DeityData.get(world.getServer());
		if (embryo == null) {
			ItemEntity booke = items.stream().filter((e) -> e.getItem().getItem() == Items.WRITTEN_BOOK).findAny()
					.orElse(null);
			if (booke == null) {
				items.forEach((it) -> launch(it));
				// System.out.println("null book item");
				return;
			}
			ItemStack book = booke.getItem();
			PlayerEntity player = world.getServer().getPlayerList()
					.getPlayerByUsername(book.getTag().getString("author"));

			if (player == null) {

				items.forEach((it) -> launch(it));
				// System.out.println("null player");
				return;
			}
			String name = book.getTag().getString("title");
			if (name.trim().isEmpty()) {

				items.forEach((it) -> launch(it));
				System.out.println("null title");
				return;
			}
			ListNBT pages = book.getTag().getList("pages", NBT.TAG_STRING);
			if (pages.isEmpty()) {
				items.forEach((it) -> launch(it));
				System.out.println("No pages");
				return;
			}
			String page1 = ITextComponent.Serializer.getComponentFromJson(pages.getString(0)).getString();
			NamingConvention naming = NamingConvention.from(page1);
			if (naming == null) {
				items.forEach((it) -> launch(it));
				System.out.println("religion name etc etc not given: " + page1);
				return;
			}
			embryo = new Deity(name, naming, player.getUniqueID());
			items.remove(booke);

			this.consumeItem(booke);

			world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 1);
			System.out.println("God " + name + " created embryonically");
		}
		if (embryo != null && embryo.getCreationStage() == CreationStage.EMBRYONIC) {
			for (ItemEntity itemen : items) {
				for (int i = 0; i < itemen.getItem().getCount(); i++) {
					if (!embryo.getInvocation().isComplete()) {
						InvocationItems invoc = new InvocationItems(embryo.getInvocation());
						invoc.addItem(itemen.getItem().getItem());
						if (data.getDeities().stream().anyMatch((e) -> e.getInvocation().equals(invoc))) {
							this.launch(itemen);
							return;
						}

						embryo.getInvocation().addItem(itemen.getItem().getItem());
						System.out.println("Added " + itemen.getItem() + " to " + embryo.getInvocation().getItems()
								+ " of " + embryo);
						if (itemen.getItem().getCount() > 1) {
							itemen.getItem().shrink(1);
						} else {

							this.consumeItem(itemen);
						}
					}
				}
			}
			if (embryo.getInvocation().isComplete()) {

				data.birthDeity(embryo, new ServerPos(pos, world.getDimensionKey()));
				this.embryo = null;
				world.setBlockState(pos,
						Blocks.OAK_FENCE.getDefaultState().with(BlockStateProperties.WATERLOGGED, true)
								.with(FenceBlock.EAST, true).with(FenceBlock.NORTH, true).with(FenceBlock.WEST, true)
								.with(FenceBlock.SOUTH, true));
			}
		}
	}

	public Deity getEmbryo() {
		return embryo;
	}

	private static BlockPattern getAltarPattern() {
		// if (primordialAltarPattern == null) {
		primordialAltarPattern = BlockPatternBuilder.start().aisle("???", "?w?").aisle("?t?", "nfs").aisle("???", "?e?")
				.where('?', CachedBlockInfo.hasState(BlockStateMatcher.ANY))
				.where('n', CachedBlockInfo.hasState(
						(e) -> e.getBlock() instanceof StairsBlock && e.get(StairsBlock.FACING) == Direction.NORTH))
				.where('s', CachedBlockInfo.hasState(
						(e) -> e.getBlock() instanceof StairsBlock && e.get(StairsBlock.FACING) == Direction.SOUTH))
				.where('e', CachedBlockInfo.hasState(
						(e) -> e.getBlock() instanceof StairsBlock && e.get(StairsBlock.FACING) == Direction.EAST))
				.where('w', CachedBlockInfo.hasState(
						(e) -> e.getBlock() instanceof StairsBlock && e.get(StairsBlock.FACING) == Direction.WEST))
				.where('t', CachedBlockInfo.hasState((e) -> e.getBlock() == Blocks.TORCH))
				.where('f',
						CachedBlockInfo
								.hasState((e) -> e.getBlock() instanceof FenceBlock && !e.getFluidState().isEmpty()))
				.build();
		// }

		return primordialAltarPattern;
	}

	@Override
	public void read(BlockState state, CompoundNBT nbt) {
		super.read(state, nbt);
		if (nbt.contains("Embryo")) {
			this.embryo = new Deity(nbt.getCompound("Embryo"));
		}
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		if (embryo != null)
			compound.put("Embryo", embryo.serialize());
		return super.write(compound);
	}

	@SubscribeEvent
	public static void createPrimordialWater(RightClickBlock event) {
		if (event.getWorld().isRemote)
			return;
		BlockPos clicked = event.getPos();
		Item used = event.getItemStack().getItem();
		ServerWorld world = (ServerWorld) event.getWorld();
		if (event.getItemStack().getItem() == Items.BLAZE_ROD) {
			for (Deity d : DeityData.get(world.getServer()).getDeities()) {
				d.setDilmunChunk(null);
			}
			System.out.println(DeityData.get(world.getServer()).getDeities());
		}
		if (world.getBlockState(clicked).getBlock() == Blocks.TORCH && used == Items.STICK) {
			System.out.println("Torch clicked");
			BlockPattern.PatternHelper altar = getAltarPattern().match(world, clicked);
			if (altar == null) {
				System.out.println(" did not match " + clicked);
				return;
			}
			BlockPos origin = altar.getFrontTopLeft().add(-1, -1, -1);
			LightningBoltEntity zap = GMWorld.summonLightning(null, world, origin.up(), null);
			zap.setEffectOnly(true);
			world.setBlockState(origin.up(), Blocks.AIR.getDefaultState());
			world.setBlockState(origin, BlockInit.PRIMORDIAL_WATER.get().getDefaultState());
			event.getPlayer().addItemStackToInventory(new ItemStack(Items.WRITABLE_BOOK));
		}
	}

}