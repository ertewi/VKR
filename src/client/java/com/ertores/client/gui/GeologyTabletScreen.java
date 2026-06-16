package com.ertores.client.gui;

import com.ertores.client.geology.ClientGeologyData;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;

public class GeologyTabletScreen extends Screen {
	private static final int RADIUS = 16;
	private static final int TILE = 7;
	private static final int GAP = 1;
	private static final int GRID = RADIUS * 2 + 1;
	private static final int PANEL_BG = 0xEE1F2428;
	private static final int PANEL_LINE = 0xFF667078;
	private static final int CURRENT = 0xFFFFFFFF;
	private static final int UNEXPLORED = 0xFF303438;

	public GeologyTabletScreen() {
		super(Component.translatable("screen.ertores.geology_tablet"));
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);

		if (minecraft == null || minecraft.player == null || minecraft.level == null) {
			return;
		}

		ChunkPos center = ChunkPos.containing(minecraft.player.blockPosition());
		ClientGeologyData.surveyAround(minecraft.level, center, 1);

		int mapSize = GRID * TILE + (GRID - 1) * GAP;
		int panelWidth = mapSize + 170;
		int panelHeight = Math.max(mapSize + 28, 280);
		int panelX = (width - panelWidth) / 2;
		int panelY = (height - panelHeight) / 2;
		int mapX = panelX + 14;
		int mapY = panelY + 38;
		int infoX = mapX + mapSize + 18;

		graphics.fill(RenderPipelines.GUI, panelX, panelY, panelX + panelWidth, panelY + panelHeight, PANEL_BG);
		graphics.outline(panelX, panelY, panelWidth, panelHeight, PANEL_LINE);
		graphics.text(font, title, panelX + 14, panelY + 12, 0xE6E6E6, false);
		graphics.text(font, "Chunk " + center.x() + ", " + center.z(), infoX, panelY + 38, 0xD8D8D8, false);
		graphics.text(font, "Surveyed: " + ClientGeologyData.surveyedCount(), infoX, panelY + 52, 0xB8C0C8, false);

		ClientGeologyData.SurveyResult hovered = null;
		ChunkPos hoveredPos = null;

		for (int dz = -RADIUS; dz <= RADIUS; dz++) {
			for (int dx = -RADIUS; dx <= RADIUS; dx++) {
				ChunkPos pos = new ChunkPos(center.x() + dx, center.z() + dz);
				ClientGeologyData.SurveyResult result = ClientGeologyData.get(pos);
				int x = mapX + (dx + RADIUS) * (TILE + GAP);
				int y = mapY + (dz + RADIUS) * (TILE + GAP);
				int color = result == null ? UNEXPLORED : result.displayColor();

				graphics.fill(RenderPipelines.GUI, x, y, x + TILE, y + TILE, color);
				if (dx == 0 && dz == 0) {
					graphics.outline(x - 1, y - 1, TILE + 2, TILE + 2, CURRENT);
				}

				if (mouseX >= x && mouseX < x + TILE && mouseY >= y && mouseY < y + TILE) {
					hovered = result;
					hoveredPos = pos;
				}
			}
		}

		drawLegend(graphics, infoX, panelY + 82);

		if (hoveredPos != null) {
			String text = "Chunk " + hoveredPos.x() + ", " + hoveredPos.z();
			if (hovered == null) {
				text += ": unexplored";
			} else {
				text += ": " + hovered.dominant().label() + " (" + hovered.total() + ")";
			}
			graphics.setTooltipForNextFrame(Component.literal(text), mouseX, mouseY);
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private void drawLegend(GuiGraphicsExtractor graphics, int x, int y) {
		legend(graphics, x, y, ClientGeologyData.OreType.IRON);
		legend(graphics, x, y + 14, ClientGeologyData.OreType.COPPER);
		legend(graphics, x, y + 28, ClientGeologyData.OreType.GOLD);
		legend(graphics, x, y + 42, ClientGeologyData.OreType.DIAMOND);
		legend(graphics, x, y + 56, ClientGeologyData.OreType.COAL);
		legend(graphics, x, y + 70, ClientGeologyData.OreType.LAPIS);
		legend(graphics, x, y + 84, ClientGeologyData.OreType.EMERALD);
		legend(graphics, x, y + 98, ClientGeologyData.OreType.REDSTONE);
		graphics.fill(RenderPipelines.GUI, x, y + 118, x + 9, y + 127, UNEXPLORED);
		graphics.text(font, "Unexplored", x + 14, y + 118, 0xD0D0D0, false);
	}

	private void legend(GuiGraphicsExtractor graphics, int x, int y, ClientGeologyData.OreType type) {
		graphics.fill(RenderPipelines.GUI, x, y, x + 9, y + 9, type.color());
		graphics.text(font, type.label(), x + 14, y, 0xD0D0D0, false);
	}
}
