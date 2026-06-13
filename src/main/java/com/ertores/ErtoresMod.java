package com.ertores;

import com.ertores.worldgen.*;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErtoresMod implements ModInitializer {
	public static final String MOD_ID = "ertores";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Feature<OreConfiguration> COAL_SEAM =
			Registry.register(
					BuiltInRegistries.FEATURE,
					"ertores:coal_seam",
					new CoalSeamFeature(OreConfiguration.CODEC)
			);
	public static final Feature<OreConfiguration> IRON_SEAM =
			Registry.register(
					BuiltInRegistries.FEATURE,
					"ertores:iron_seam",
					new IronSeamFeature(OreConfiguration.CODEC)
			);
	public static final Feature<OreConfiguration> KIMBERLITE_PIPE =
			Registry.register(
					BuiltInRegistries.FEATURE,
					"ertores:kimberlite_pipe",
					new KimberlitePipeFeature(OreConfiguration.CODEC)
			);
	public static final Feature<OreConfiguration> GOLD_VEIN =
			Registry.register(
					BuiltInRegistries.FEATURE,
					Identifier.parse("ertores:gold_vein"),
					new GoldVeinFeature(OreConfiguration.CODEC)
			);
	public static final Feature<NoneFeatureConfiguration> EMERALD_OCEAN =
			Registry.register(
					BuiltInRegistries.FEATURE,
					Identifier.parse("ertores:emerald_ocean"),
					new EmeraldOceanFeature(NoneFeatureConfiguration.CODEC)
			);
	public static final Feature<NoneFeatureConfiguration> LAPIS_GEODE=
			Registry.register(
					BuiltInRegistries.FEATURE,
					Identifier.parse("ertores:lapis_geode"),
					new LapisGeodeFeature(NoneFeatureConfiguration.CODEC)
			);

	@Override
	public void onInitialize() {
		BiomeModifications.create(Identifier.parse("ertores:add_kimberlite"))
				.add(
						ModificationPhase.ADDITIONS,
						context -> true,
						context -> {
							context.getGenerationSettings().addFeature(
									GenerationStep.Decoration.UNDERGROUND_ORES,
									ResourceKey.create(
											Registries.PLACED_FEATURE,
											Identifier.parse("ertores:ore_diamond_kimberlite_pipe")
									)
							);
						}
				);
		BiomeModifications.create(Identifier.parse("ertores:add_coal"))
				.add(
						ModificationPhase.ADDITIONS,
						context -> true,
						context -> {
							context.getGenerationSettings().addFeature(
									GenerationStep.Decoration.UNDERGROUND_ORES,
									ResourceKey.create(
											Registries.PLACED_FEATURE,
											Identifier.parse("ertores:ore_coal_seam")
									)
							);
						}
				);
		BiomeModifications.create(Identifier.parse("ertores:add_iron"))
				.add(
						ModificationPhase.ADDITIONS,
						context -> true,
						context -> {
							context.getGenerationSettings().addFeature(
									GenerationStep.Decoration.UNDERGROUND_ORES,
									ResourceKey.create(
											Registries.PLACED_FEATURE,
											Identifier.parse("ertores:ore_iron_seam")
									)
							);
						}
				);
		BiomeModifications.create(Identifier.parse("ertores:add_gold"))
				.add(
						ModificationPhase.ADDITIONS,
						context -> true,
						context -> {
							context.getGenerationSettings().addFeature(
									GenerationStep.Decoration.UNDERGROUND_ORES,
									ResourceKey.create(
											Registries.PLACED_FEATURE,
											Identifier.parse("ertores:ore_gold_vein")
									)
							);
						}
				);
		BiomeModifications.create(Identifier.parse("ertores:add_emerald"))
				.add(
						ModificationPhase.ADDITIONS,
						context -> true,
						context -> {
							context.getGenerationSettings().addFeature(
									GenerationStep.Decoration.UNDERGROUND_ORES,
									ResourceKey.create(
											Registries.PLACED_FEATURE,
											Identifier.parse("ertores:ore_emerald_ocean")
									)
							);
						}
				);
		BiomeModifications.create(Identifier.parse("ertores:lapis_geode"))
				.add(
						ModificationPhase.ADDITIONS,
						context -> true,
						context -> {
							context.getGenerationSettings().addFeature(
									GenerationStep.Decoration.UNDERGROUND_ORES,
									ResourceKey.create(
											Registries.PLACED_FEATURE,
											Identifier.parse("ertores:ore_lapis_geode")
									)
							);
						}
				);

		LOGGER.info("ErtOres was initialized.");
	}
}