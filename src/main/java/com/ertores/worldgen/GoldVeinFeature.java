package com.ertores.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;

public class GoldVeinFeature extends Feature<OreConfiguration> {

    public GoldVeinFeature(Codec<OreConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<OreConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        OreConfiguration config = context.config();

        int placed = generateGoldVein(level, random, config, origin);
        return placed > 0;
    }

    private int generateGoldVein(
            WorldGenLevel level,
            RandomSource random,
            OreConfiguration config,
            BlockPos origin
    ) {
        int placed = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        int veinRadius = 8;
        int veinHeight = 12;

        double centerY = origin.getY();

        ImprovedNoise shapeNoise = new ImprovedNoise(random);
        ImprovedNoise branchNoise = new ImprovedNoise(RandomSource.create());

        try (BulkSectionAccess sectionGetter = new BulkSectionAccess(level)) {
            for (int x = -veinRadius; x <= veinRadius; x++) {
                for (int z = -veinRadius; z <= veinRadius; z++) {
                    int worldX = origin.getX() + x;
                    int worldZ = origin.getZ() + z;

                    double distXZ = Math.sqrt(x * x + z * z) / veinRadius;

                    double thicknessNoise = shapeNoise.noise(worldX * 0.1, 0, worldZ * 0.1);

                    double density = (1 - distXZ) * (0.4 + thicknessNoise * 0.3);

                    if (density < 0.2 || random.nextDouble() > density) continue;

                    double verticalOffsetNoise = branchNoise.noise(worldX * 0.08, 0, worldZ * 0.08) * veinHeight * 0.5;
                    int veinTop = (int)(centerY + verticalOffsetNoise - veinHeight / 3);
                    int veinBottom = (int)(centerY + verticalOffsetNoise + veinHeight / 3);

                    if (distXZ > 0.6) {
                        veinBottom = veinTop + random.nextInt(3) + 2;
                    }

                    for (int y = veinTop; y <= veinBottom; y++) {
                        if (level.isOutsideBuildHeight(y)) continue;

                        pos.set(worldX, y, worldZ);

                        LevelChunkSection section = sectionGetter.getSection(pos);
                        if (section == null) continue;

                        int sx = SectionPos.sectionRelative(worldX);
                        int sy = SectionPos.sectionRelative(y);
                        int sz = SectionPos.sectionRelative(worldZ);

                        BlockState currentBlock = section.getBlockState(sx, sy, sz);

                        if (!canReplace(currentBlock, config)) continue;

                        BlockState placeBlock = null;
                        double rand = random.nextDouble();

                        if (rand < 0.02) {
                            placeBlock = Blocks.RAW_GOLD_BLOCK.defaultBlockState();
                        }
                        else if (rand < 0.15) {
                            placeBlock = Blocks.NETHER_QUARTZ_ORE.defaultBlockState();
                        }
                        else if (rand < 0.7) {
                            placeBlock = getGoldOreState(currentBlock);
                        }
                        else {
                            placeBlock = Blocks.QUARTZ_BLOCK.defaultBlockState();
                        }

                        if (placeBlock != null) {
                            section.setBlockState(sx, sy, sz, placeBlock, false);
                            placed++;
                        }
                    }
                }
            }
        }

        return placed;
    }

    private BlockState getGoldOreState(BlockState current) {
        if (current.is(Blocks.DEEPSLATE) || current.is(Blocks.TUFF)) {
            return Blocks.DEEPSLATE_GOLD_ORE.defaultBlockState();
        }
        return Blocks.GOLD_ORE.defaultBlockState();
    }

    private boolean canReplace(BlockState state, OreConfiguration config) {
        for (OreConfiguration.TargetBlockState target : config.targetStates) {
            if (target.target.test(state, RandomSource.create())) {
                return true;
            }
        }
        return false;
    }
}