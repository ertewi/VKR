package com.ertores.client.geology;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public final class ClientGeologyData {
	private static final Map<Long, SurveyResult> SURVEYED_CHUNKS = new HashMap<>();
	private static long lastChunk = Long.MIN_VALUE;

	private ClientGeologyData() {
	}

	public static void tick(Minecraft client) {
		if (client.level == null || client.player == null) {
			SURVEYED_CHUNKS.clear();
			lastChunk = Long.MIN_VALUE;
			return;
		}

		ChunkPos chunk = ChunkPos.containing(client.player.blockPosition());
		long key = key(chunk);
		if (key != lastChunk) {
			lastChunk = key;
			surveyAround(client.level, chunk, 1);
		}
	}

	public static void surveyAround(ClientLevel level, ChunkPos center, int radius) {
		for (int x = center.x() - radius; x <= center.x() + radius; x++) {
			for (int z = center.z() - radius; z <= center.z() + radius; z++) {
				if (level.hasChunk(x, z)) {
					survey(level, new ChunkPos(x, z));
				}
			}
		}
	}

	public static SurveyResult get(ChunkPos pos) {
		return SURVEYED_CHUNKS.get(key(pos));
	}

	public static int surveyedCount() {
		return SURVEYED_CHUNKS.size();
	}

	private static void survey(ClientLevel level, ChunkPos chunk) {
		long key = key(chunk);
		if (SURVEYED_CHUNKS.containsKey(key)) {
			return;
		}

		int iron = 0;
		int copper = 0;
		int gold = 0;
		int diamond = 0;
		int coal = 0;
		int lapis = 0;
		int emerald = 0;
		int redstone = 0;

		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int x = chunk.getMinBlockX(); x <= chunk.getMaxBlockX(); x++) {
			for (int z = chunk.getMinBlockZ(); z <= chunk.getMaxBlockZ(); z++) {
				for (int y = -64; y <= 320; y++) {
					pos.set(x, y, z);
					BlockState state = level.getBlockState(pos);
					if (state.is(Blocks.IRON_ORE) || state.is(Blocks.DEEPSLATE_IRON_ORE) || state.is(Blocks.RAW_IRON_BLOCK)) {
						iron++;
					} else if (state.is(Blocks.COPPER_ORE) || state.is(Blocks.DEEPSLATE_COPPER_ORE) || state.is(Blocks.RAW_COPPER_BLOCK)) {
						copper++;
					} else if (state.is(Blocks.GOLD_ORE) || state.is(Blocks.DEEPSLATE_GOLD_ORE) || state.is(Blocks.RAW_GOLD_BLOCK)) {
						gold++;
					} else if (state.is(Blocks.DIAMOND_ORE) || state.is(Blocks.DEEPSLATE_DIAMOND_ORE)) {
						diamond++;
					} else if (state.is(Blocks.COAL_ORE) || state.is(Blocks.DEEPSLATE_COAL_ORE)) {
						coal++;
					} else if (state.is(Blocks.LAPIS_ORE) || state.is(Blocks.DEEPSLATE_LAPIS_ORE)) {
						lapis++;
					} else if (state.is(Blocks.EMERALD_ORE) || state.is(Blocks.DEEPSLATE_EMERALD_ORE)) {
						emerald++;
					} else if (state.is(Blocks.REDSTONE_ORE) || state.is(Blocks.DEEPSLATE_REDSTONE_ORE)) {
						redstone++;
					}
				}
			}
		}

		SURVEYED_CHUNKS.put(key, SurveyResult.of(iron, copper, gold, diamond, coal, lapis, emerald, redstone));
	}

	private static long key(ChunkPos pos) {
		return ((long) pos.x() << 32) ^ (pos.z() & 0xFFFFFFFFL);
	}

	public enum OreType {
		UNKNOWN("Unknown", 0xFF4A4A4A),
		EMPTY("Empty", 0xFF6A6A6A),
		IRON("Iron", 0xFFE2E2D8),
		COPPER("Copper", 0xFFD87935),
		GOLD("Gold", 0xFFFFD24A),
		DIAMOND("Diamond", 0xFF62D7E8),
		COAL("Coal", 0xFF2D2D2D),
		LAPIS("Lapis", 0xFF315ED8),
		EMERALD("Emerald", 0xFF35C46B),
		REDSTONE("Redstone", 0xFFE24135);

		private final String label;
		private final int color;

		OreType(String label, int color) {
			this.label = label;
			this.color = color;
		}

		public String label() {
			return label;
		}

		public int color() {
			return color;
		}
	}

	public record SurveyResult(OreType dominant, int richness, int total) {
		static SurveyResult of(int iron, int copper, int gold, int diamond, int coal, int lapis, int emerald, int redstone) {
			int[] values = {iron, copper, gold, diamond, coal, lapis, emerald, redstone};
			OreType[] types = {OreType.IRON, OreType.COPPER, OreType.GOLD, OreType.DIAMOND, OreType.COAL, OreType.LAPIS, OreType.EMERALD, OreType.REDSTONE};
			int bestIndex = -1;
			int best = 0;
			int total = 0;

			for (int i = 0; i < values.length; i++) {
				total += values[i];
				if (values[i] > best) {
					best = values[i];
					bestIndex = i;
				}
			}

			if (bestIndex < 0) {
				return new SurveyResult(OreType.EMPTY, 0, 0);
			}

			return new SurveyResult(types[bestIndex], Math.min(100, best), total);
		}

		public int displayColor() {
			if (dominant == OreType.EMPTY) {
				return dominant.color();
			}

			int base = dominant.color();
			int boost = Math.min(90, richness * 3);
			int r = Math.min(255, ((base >> 16) & 255) + boost);
			int g = Math.min(255, ((base >> 8) & 255) + boost);
			int b = Math.min(255, (base & 255) + boost);
			return 0xFF000000 | (r << 16) | (g << 8) | b;
		}
	}
}
