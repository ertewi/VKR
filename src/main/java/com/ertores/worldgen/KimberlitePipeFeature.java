package com.ertores.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BulkSectionAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

public class KimberlitePipeFeature extends Feature<OreConfiguration> {

    public KimberlitePipeFeature(Codec<OreConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<OreConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        OreConfiguration config = context.config();

        int placed = generateKimberlitePipe(level, random, config, origin);

        return placed > 0;
    }

    private int generateKimberlitePipe(
            WorldGenLevel level,
            RandomSource random,
            OreConfiguration config,
            BlockPos origin
    ) {
        int placed = 0;
        BlockPos.MutableBlockPos orePos = new BlockPos.MutableBlockPos();

        try (BulkSectionAccess sectionGetter = new BulkSectionAccess(level)) {
            // Центр трубки со случайным смещением
            double centerX = origin.getX() + random.nextDouble() * 8 - 4;
            double centerZ = origin.getZ() + random.nextDouble() * 8 - 4;

            double topRadius = 3.0 + random.nextDouble() * 3.0;
            double bottomRadius = 1.0 + random.nextDouble() * 1.5;
            double wallThickness = 1.0 + random.nextDouble() * 1.5;

            // Высота трубки
            int pipeHeight = 30 + random.nextInt(40);
            int craterDepth = 2 + random.nextInt(3);
            int searchRadius = 12;

            double pipeTop = origin.getY() + pipeHeight * 0.3;
            double pipeBottom = pipeTop - pipeHeight;

            for (int y = origin.getY() - searchRadius; y < origin.getY() + pipeHeight + searchRadius; y++) {
                if (level.isOutsideBuildHeight(y)) continue;

                double outerRadius;
                double innerRadius;

                if (y > pipeTop) {
                    double craterProgress = (y - pipeTop) / craterDepth;
                    if (craterProgress > 1.0) continue;
                    outerRadius = topRadius * (1.0 + craterProgress * 0.3);
                    innerRadius = outerRadius - wallThickness;
                    if (innerRadius < 0) innerRadius = 0;
                } else if (y > pipeBottom) {
                    double progress = (pipeTop - y) / pipeHeight;
                    outerRadius = topRadius * Math.pow(1.0 - progress, 2.5) + bottomRadius * progress;
                    innerRadius = outerRadius - wallThickness;
                    if (innerRadius < 0) innerRadius = 0;
                } else {
                    double rootProgress = (pipeBottom - y) / 10.0;
                    if (rootProgress > 1.0) continue;
                    outerRadius = bottomRadius * (1.0 - rootProgress);
                    innerRadius = 0;
                }

                if (outerRadius < 0.3) continue;

                int xMin = Mth.floor(centerX - outerRadius - 1);
                int xMax = Mth.floor(centerX + outerRadius + 1);
                int zMin = Mth.floor(centerZ - outerRadius - 1);
                int zMax = Mth.floor(centerZ + outerRadius + 1);

                for (int x = xMin; x <= xMax; x++) {
                    for (int z = zMin; z <= zMax; z++) {
                        double dx = x + 0.5 - centerX;
                        double dz = z + 0.5 - centerZ;
                        double dist = Math.sqrt(dx * dx + dz * dz);

                        boolean inWall = (dist >= innerRadius && dist <= outerRadius);
                        boolean isSolidBottom = (y <= pipeBottom + wallThickness * 2 &&
                                y > pipeBottom &&
                                dist <= outerRadius);

                        if (inWall || isSolidBottom) {
                            boolean hasDiamond = false;

                            if (y > pipeTop) {
                                hasDiamond = random.nextFloat() < 0.05;
                            } else if (y > pipeTop - pipeHeight * 0.3) {
                                hasDiamond = random.nextFloat() < 0.2;
                            } else if (y > pipeTop - pipeHeight * 0.7) {
                                hasDiamond = random.nextFloat() < 0.1;
                            } else {
                                hasDiamond = random.nextFloat() < 0.05;
                            }

                            if (hasDiamond || dist < outerRadius * 0.3) {
                                placed += placeOreBlock(level, random, config, sectionGetter, orePos, x, y, z);
                            }
                        }
                    }
                }
            }
        }

        return placed;
    }

    private int placeOreBlock(
            WorldGenLevel level,
            RandomSource random,
            OreConfiguration config,
            BulkSectionAccess sectionGetter,
            BlockPos.MutableBlockPos orePos,
            int x, int y, int z
    ) {
        orePos.set(x, y, z);

        if (level.ensureCanWrite(orePos)) {
            LevelChunkSection section = sectionGetter.getSection(orePos);
            if (section != null) {
                int sx = SectionPos.sectionRelative(x);
                int sy = SectionPos.sectionRelative(y);
                int sz = SectionPos.sectionRelative(z);

                BlockState blockState = section.getBlockState(sx, sy, sz);

                for (OreConfiguration.TargetBlockState target : config.targetStates) {
                    if (OreFeature.canPlaceOre(
                            blockState,
                            sectionGetter::getBlockState,
                            random,
                            config,
                            target,
                            orePos
                    )) {
                        section.setBlockState(sx, sy, sz, target.state, false);
                        return 1;
                    }
                }
            }
        }

        return 0;
    }
}