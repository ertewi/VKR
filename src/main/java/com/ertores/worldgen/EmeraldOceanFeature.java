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

        // Проверяем границу океан-суша в этом чанке
        if (!isOnOceanBoundary(level, chunkX, chunkZ)) {
            return false;
        }

        // Редкий шанс (уменьшил для теста)
        if (random.nextInt(50) != 0) {  // ← временно 50 вместо 150
            return false;
        }

        int placed = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        // Генерируем 1-4 блока
        int emeralds = random.nextInt(4) + 1;

        for (int i = 0; i < emeralds; i++) {
            int x = origin.getX() + random.nextInt(16);
            int z = origin.getZ() + random.nextInt(16);
            int y = findReasonableHeight(level, x, z, random);

            if (y < 20 || y > 70) continue;

            pos.set(x, y, z);
            BlockState currentState = level.getBlockState(pos);

            // Исправлено: заменяем любую каменную породу, а не только STONE
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

        // Сканируем углы и центр чанка для большей точности
        int[][] checkPoints = {{0, 0}, {0, 15}, {15, 0}, {15, 15}, {8, 8}};

        for (int[] point : checkPoints) {
            int dx = point[0];
            int dz = point[1];
            pos.set(minX + dx, 64, minZ + dz);

            Holder<Biome> biomeHolder = level.getBiome(pos);
            // Исправлено: правильный способ получить имя биома
            String biomeName = getBiomeName(biomeHolder);


            if (isOceanBiome(biomeName)) {
                hasOcean = true;
            } else if (isLandBiome(biomeName)) {
                hasLand = true;
            }

            // Ранний выход, если оба условия выполнены
            if (hasOcean && hasLand) {
                return true;
            }
        }

        return false;
    }

    private String getBiomeName(Holder<Biome> biomeHolder) {
        // Пробуем получить ID биома через ResourceKey
        return biomeHolder.unwrapKey()
                .map(key -> key.identifier().toString())  // identifier() для Mojmap
                .orElseGet(() -> {
                    // Fallback: пытаемся извлечь имя из toString() биома
                    String toString = biomeHolder.value().toString();
                    // Очищаем от лишних символов, если нужно
                    if (toString.contains(":")) {
                        // Пример: "Biome[minecraft:plains]" -> "minecraft:plains"
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
        // Исключаем океаны, реки и пляжи
        return !lower.contains("ocean") &&
                !lower.contains("deep") &&
                !lower.contains("river") &&
                !lower.contains("beach") &&
                !lower.contains("cave");
    }

    private int findReasonableHeight(WorldGenLevel level, int x, int z, RandomSource random) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        // Ищем дно (первый твердый блок под водой)
        for (int y = 70; y >= 20; y--) {
            pos.set(x, y, z);
            BlockState state = level.getBlockState(pos);

            // Нашли твердый блок (не воздух и не вода)
            if (!state.isAir() && !state.is(Blocks.WATER)) {
                // Возвращаем позицию на 2-6 блоков ниже (внутрь породы)
                int depth = 2 + random.nextInt(5);
                return Math.max(20, y - depth);
            }
        }
        return 40; // Дефолтная глубина
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
        // Возвращаем правильный вариант руды в зависимости от породы
        if (stone.is(Blocks.DEEPSLATE)) {
            return Blocks.DEEPSLATE_EMERALD_ORE.defaultBlockState();
        }
        if (stone.is(Blocks.TUFF)) {
            // Tuff обычно использует deepslate вариант
            return Blocks.DEEPSLATE_EMERALD_ORE.defaultBlockState();
        }
        // Для всех остальных пород - обычная изумрудная руда
        return Blocks.EMERALD_ORE.defaultBlockState();
    }
}