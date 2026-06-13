package com.ertores.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class EmeraldOceanFeature extends Feature<NoneFeatureConfiguration> {

    public EmeraldOceanFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        int chunkX = origin.getX() >> 4;
        int chunkZ = origin.getZ() >> 4;

        if (!isOnOceanBoundary(level, chunkX, chunkZ)) return false;
        if (random.nextInt(50) != 0) return false;

        int placed = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        int emeralds = random.nextInt(4) + 1;

        for (int i = 0; i < emeralds; i++) {
            int x = origin.getX() + random.nextInt(16);
            int z = origin.getZ() + random.nextInt(16);
            int y = findReasonableHeight(level, x, z, random);

            if (y < 20 || y > 70) continue;

            pos.set(x, y, z);
            BlockState currentState = level.getBlockState(pos);

            if (isReplaceableStone(currentState)) {
                BlockState oreState = getEmeraldOreForStone(currentState);
                level.setBlock(pos, oreState, 2);
                placed++;
            }
        }

        return placed > 0;
    }

    private boolean isOnOceanBoundary(WorldGenLevel level, int chunkX, int chunkZ) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        boolean hasOcean = false;
        boolean hasLand = false;

        int minX = chunkX * 16;
        int minZ = chunkZ * 16;

        int[][] checkPoints = {{0, 0}, {0, 15}, {15, 0}, {15, 15}, {8, 8}};

        for (int[] point : checkPoints) {
            int dx = point[0];
            int dz = point[1];
            pos.set(minX + dx, 64, minZ + dz);

            Holder<Biome> biomeHolder = level.getBiome(pos);
            String biomeName = getBiomeName(biomeHolder);


            if (isOceanBiome(biomeName)) {
                hasOcean = true;
            } else if (isLandBiome(biomeName)) {
                hasLand = true;
            }

            if (hasOcean && hasLand) {
                return true;
            }
        }

        return false;
    }

    private String getBiomeName(Holder<Biome> biomeHolder) {
        return biomeHolder.unwrapKey()
                .map(key -> key.identifier().toString())
                .orElseGet(() -> {
                    String toString = biomeHolder.value().toString();
                    if (toString.contains(":")) {
                        int start = toString.indexOf("[") + 1;
                        int end = toString.indexOf("]");
                        if (start > 0 && end > start) {
                            return toString.substring(start, end);
                        }
                    }
                    return toString.toLowerCase();
                });
    }

    private boolean isOceanBiome(String biomeName) {
        String lower = biomeName.toLowerCase();
        return lower.contains("ocean") ||
                lower.contains("deep") ||
                lower.equals("minecraft:river");
    }

    private boolean isLandBiome(String biomeName) {
        String lower = biomeName.toLowerCase();
        return !lower.contains("ocean") &&
                !lower.contains("deep") &&
                !lower.contains("river") &&
                !lower.contains("beach") &&
                !lower.contains("cave");
    }

    private int findReasonableHeight(WorldGenLevel level, int x, int z, RandomSource random) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int y = 70; y >= 20; y--) {
            pos.set(x, y, z);
            BlockState state = level.getBlockState(pos);

            if (!state.isAir() && !state.is(Blocks.WATER)) {
                int depth = 2 + random.nextInt(5);
                return Math.max(20, y - depth);
            }
        }
        return 40;
    }

    private boolean isReplaceableStone(BlockState state) {
        return state.is(Blocks.STONE) ||
                state.is(Blocks.DEEPSLATE) ||
                state.is(Blocks.GRANITE) ||
                state.is(Blocks.DIORITE) ||
                state.is(Blocks.ANDESITE) ||
                state.is(Blocks.TUFF) ||
                state.is(Blocks.CALCITE);
    }

    private BlockState getEmeraldOreForStone(BlockState stone) {
        if (stone.is(Blocks.DEEPSLATE)) {
            return Blocks.DEEPSLATE_EMERALD_ORE.defaultBlockState();
        }
        if (stone.is(Blocks.TUFF)) {
            return Blocks.DEEPSLATE_EMERALD_ORE.defaultBlockState();
        }
        return Blocks.EMERALD_ORE.defaultBlockState();
    }
}