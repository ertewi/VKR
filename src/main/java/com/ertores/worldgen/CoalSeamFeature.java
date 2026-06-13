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

public class CoalSeamFeature extends Feature<OreConfiguration> {

    public CoalSeamFeature(Codec<OreConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<OreConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        OreConfiguration config = context.config();

        int placed = generateCoalSeam(level, random, config, origin);
        return placed > 0;
    }

    private int generateCoalSeam(
            WorldGenLevel level,
            RandomSource random,
            OreConfiguration config,
            BlockPos origin
    ) {
        int placed = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        int seamRadius = 14;
        int seamThickness = 1 + random.nextInt(2);
        double centerY = origin.getY();

        ImprovedNoise shapeNoise = new ImprovedNoise(random);
        ImprovedNoise heightNoise = new ImprovedNoise(RandomSource.create());

        try (BulkSectionAccess sectionGetter = new BulkSectionAccess(level)) {
            for (int x = -seamRadius; x <= seamRadius; x++) {
                for (int z = -seamRadius; z <= seamRadius; z++) {
                    int worldX = origin.getX() + x;
                    int worldZ = origin.getZ() + z;

                    double noise = shapeNoise.noise(worldX * 0.02, 0, worldZ * 0.02);

                    double distX = x / (double) seamRadius;
                    double distZ = z / (double) seamRadius;
                    double dist = distX * distX + distZ * distZ;

                    if (dist > 1.0 || noise < -0.3) continue;

                    double hNoise = heightNoise.noise(worldX * 0.05, 0, worldZ * 0.05) * 4;
                    int seamCenterY = (int) (centerY + hNoise);

                    for (int yOffset = -seamThickness; yOffset <= seamThickness; yOffset++) {
                        int worldY = seamCenterY + yOffset;

                        if (level.isOutsideBuildHeight(worldY)) continue;

                        pos.set(worldX, worldY, worldZ);

                        LevelChunkSection section = sectionGetter.getSection(pos);
                        if (section == null) continue;

                        int sx = SectionPos.sectionRelative(worldX);
                        int sy = SectionPos.sectionRelative(worldY);
                        int sz = SectionPos.sectionRelative(worldZ);

                        BlockState currentBlock = section.getBlockState(sx, sy, sz);

                        BlockState placeBlock;
                        double rand = random.nextDouble();

                        if (rand < 0.05) {
                            placeBlock = Blocks.COAL_BLOCK.defaultBlockState();
                        } else if (rand < 0.9) {
                            placeBlock = getCoalOreState(currentBlock);
                        } else {
                            placeBlock = null;
                        }

                        if (placeBlock != null && canReplace(currentBlock, config)) {
                            section.setBlockState(sx, sy, sz, placeBlock, false);
                            placed++;
                        }
                    }
                }
            }
        }

        return placed;
    }

    private BlockState getCoalOreState(BlockState current) {
        if (current.is(Blocks.DEEPSLATE) || current.is(Blocks.TUFF)) {
            return Blocks.DEEPSLATE_COAL_ORE.defaultBlockState();
        }
        return Blocks.COAL_ORE.defaultBlockState();
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