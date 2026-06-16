package com.ertores.client.gui;

import com.ertores.menu.ProcessingMachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ProcessingMachineScreen extends AbstractContainerScreen<ProcessingMachineMenu> {
	private static final int PANEL = 0xFFB8B0A0;
	private static final int PANEL_DARK = 0xFF3F3A34;
	private static final int SLOT = 0xFFE0D8C8;
	private static final int SLOT_DARK = 0xFF746C60;
	private static final int PROGRESS_EMPTY = 0xFF5E615F;
	private static final int PROGRESS_FULL = 0xFF57A773;

	public ProcessingMachineScreen(ProcessingMachineMenu menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title, 176, 166);
		inventoryLabelY = 72;
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(graphics, mouseX, mouseY, partialTick);

		int x = leftPos;
		int y = topPos;

		graphics.fill(RenderPipelines.GUI, x, y, x + imageWidth, y + imageHeight, PANEL);
		graphics.outline(x, y, imageWidth, imageHeight, PANEL_DARK);

		drawSlot(graphics, x + 43, y + 34);
		drawSlot(graphics, x + 115, y + 34);
		drawSlot(graphics, x + 139, y + 34);

		graphics.fill(RenderPipelines.GUI, x + 68, y + 38, x + 106, y + 48, PROGRESS_EMPTY);
		graphics.outline(x + 67, y + 37, 40, 12, PANEL_DARK);
		int progress = menu.progressPixels(38);
		if (progress > 0) {
			graphics.fill(RenderPipelines.GUI, x + 68, y + 38, x + 68 + progress, y + 48, PROGRESS_FULL);
		}

		graphics.text(font, menu.progressPercent() + "%", x + 79, y + 52, 0x303030, false);
	}

	private static void drawSlot(GuiGraphicsExtractor graphics, int x, int y) {
		graphics.fill(RenderPipelines.GUI, x, y, x + 18, y + 18, SLOT_DARK);
		graphics.fill(RenderPipelines.GUI, x + 1, y + 1, x + 17, y + 17, SLOT);
	}
}
