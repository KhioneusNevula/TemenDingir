package com.gm910.temendingir;

import com.gm910.temendingir.api.networking.messages.ModChannels;
import com.gm910.temendingir.api.networking.messages.Networking.TaskMessage;
import com.gm910.temendingir.blocks.tile.invokers.InvokerTileHelper;
import com.gm910.temendingir.capabilities.GMCaps;
import com.gm910.temendingir.init.BlockInit;
import com.gm910.temendingir.init.EffectInit;
import com.gm910.temendingir.init.EntityInit;
import com.gm910.temendingir.init.ItemInit;
import com.gm910.temendingir.init.StructureInit;
import com.gm910.temendingir.init.TileInit;
import com.gm910.temendingir.keys.ModKeys;

import net.minecraft.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(TemenDingir.MODID)
public class TemenDingir {
	// Directly reference a log4j logger.
	public static final String MODID = "temendingir";
	public static final String NAME = "Temen-Dingir";
	public static final String VERSION = "1.0";
	public static TemenDingir instance;

	public TemenDingir() {
		instance = this;
		// Register the setup method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		// Register the enqueueIMC method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
		// Register the processIMC method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
		// Register the doClientStuff method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

		// Register ourselves for server and other game events we are interested in

		MinecraftForge.EVENT_BUS.register(this);
		System.out.println("Mod added to event bus");
		ModKeys.firstinit();
		System.out.println("Mod keys first init completed");

		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

		BlockInit.BLOCKS.register(modBus);
		ItemInit.ITEMS.register(modBus);
		TileInit.TILE_TYPES.register(modBus);
		EffectInit.EFFECTS.register(modBus);
		EffectInit.POTION_TYPES.register(modBus);
		StructureInit.STRUCTURES.register(modBus);
		EntityInit.ENTITY_TYPES.register(modBus);

		ModChannels.INSTANCE.registerMessage(ModChannels.id++, TaskMessage.class, TaskMessage::encode,
				TaskMessage::fromBuffer, TaskMessage::handle);

		System.out.println("Modchannels " + ModChannels.INSTANCE + " mes ");
		System.out.println("Modchannels");
	}

	private void setup(final FMLCommonSetupEvent event) {
		GMCaps.preInit();
	}

	private void doClientStuff(final FMLClientSetupEvent event) {
		ModKeys.clientinit();
		ItemInit.registerISTERs();
		TileInit.registerTESRs();
		EntityInit.registerRenderers();
	}

	private void enqueueIMC(final InterModEnqueueEvent event) {/*
																 * // some example code to dispatch IMC to another mod
																 * InterModComms.sendTo("examplemod", "helloworld", () -> {
																 * LOGGER.info("Hello world from the MDK"); return "Hello world";});
																 */
	}

	private void processIMC(final InterModProcessEvent event) {
		// some example code to receive and process InterModComms from other mods
		/*
		 * LOGGER.info("Got IMC {}", event.getIMCStream().
		 * map(m->m.getMessageSupplier().get()). collect(Collectors.toList()));
		 */
	}

	// You can use SubscribeEvent and let the Event Bus discover methods to call
	@SubscribeEvent
	public void onServerStarting(FMLServerStartingEvent event) {
		// do something when the server starts
		InvokerTileHelper.registerAll();
		/*HeatRateHandler.initTemperatureValues();
		HeatEmitterHandler.initTemperatureValues();
		HeatFunctionHandler.initSet();*/
	}

	// You can use EventBusSubscriber to automatically subscribe events on the
	// contained class (this is subscribing to the MOD
	// Event bus for receiving Registry Events)
	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class RegistryEvents {
		@SubscribeEvent
		public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
			// register a new block here

		}
	}
}
