package com.gm910.temendingir.world.gods.cap.dilmunmanager;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.advancements.Advancement;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.data.advancements.AdventureAdvancements;
import net.minecraft.data.advancements.EndAdvancements;
import net.minecraft.data.advancements.HusbandryAdvancements;
import net.minecraft.data.advancements.NetherAdvancements;
import net.minecraft.data.advancements.StoryAdvancements;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

// TODO wealth worship stuff
public class WealthWorshipMethod {

	public static class WealthTier {

	}

	public static class WealthTierDataProvider implements IDataProvider {
		private static final Logger LOGGER = LogManager.getLogger();
		private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
		private final DataGenerator generator;
		private final List<Consumer<Consumer<Advancement>>> advancements = ImmutableList.of(new EndAdvancements(),
				new HusbandryAdvancements(), new AdventureAdvancements(), new NetherAdvancements(),
				new StoryAdvancements());

		public WealthTierDataProvider(DataGenerator generatorIn) {
			this.generator = generatorIn;
		}

		/**
		 * Performs this provider's action.
		 */
		@Override
		public void act(DirectoryCache cache) throws IOException {
			Path path = this.generator.getOutputFolder();
			Set<ResourceLocation> set = Sets.newHashSet();
			Consumer<Advancement> consumer = (advancement) -> {
				if (!set.add(advancement.getId())) {
					throw new IllegalStateException("Duplicate advancement " + advancement.getId());
				} else {
					Path path1 = getPath(path, advancement);

					try {
						IDataProvider.save(GSON, cache, advancement.copy().serialize(), path1);
					} catch (IOException ioexception) {
						LOGGER.error("Couldn't save advancement {}", path1, ioexception);
					}

				}
			};

			for (Consumer<Consumer<Advancement>> consumer1 : this.advancements) {
				consumer1.accept(consumer);
			}

		}

		private static Path getPath(Path pathIn, Advancement advancementIn) {
			return pathIn.resolve("data/" + advancementIn.getId().getNamespace() + "/wealth_tiers/"
					+ advancementIn.getId().getPath() + ".json");
		}

		/**
		 * Gets a name for this provider, to use in logging.
		 */
		@Override
		public String getName() {
			return "Wealth Tiers";
		}

	}

	public static float getTier(Item item) {

		return 0f;
	}
}
