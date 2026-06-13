package com.ertores.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.ArrayList;
import java.util.List;

public class LapisGeodeFeature extends Feature<NoneFeatureConfiguration> {

    public LapisGeodeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        int outerRadius = 4 + random.nextInt(3);
        int oreRadius = outerRadius - 1;
        int innerRadius = Math.max(1, outerRadius - 2);

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int placed = 0;

        List<BlockPos> positions = new ArrayList<>();

        for (int x = -outerRadius; x <= outerRadius; x++) {
            for (int y = -outerRadius; y <= outerRadius; y++) {
                for (int z = -outerRadius; z <= outerRadius; z++) {
                    double distSq = x*x + y*y + z*z;
                    double maxDistSq = outerRadius * outerRadius;
                    double oreDistSq = oreRadius * oreRadius;
                    double minDistSq = innerRadius * innerRadius;

                    if (distSq > maxDistSq) continue;

                    pos.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    BlockState currentState = level.getBlockState(pos);

                    if (!isReplaceableStone(currentState)) continue;

                    if (distSq <= minDistSq) {
                        continue;
                    } else if (distSq > oreDistSq) {
                        level.setBlock(pos, Blocks.CALCITE.defaultBlockState(), 2);
                    } else {
                        BlockState oreState = getLapisOreState(pos.getY());
                        level.setBlock(pos, oreState, 2);
                        placed++;
                    }
                }
            }
        }

        return placed > 0;
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

    private BlockState getLapisOreState(int y) {
        if (y < 0) {
            return Blocks.DEEPSLATE_LAPIS_ORE.defaultBlockState();
        }
        return Blocks.LAPIS_ORE.defaultBlockState();
    }
}