package com.ertores.menu;

import com.ertores.block.entity.ProcessingMachineBlockEntity;
import com.ertores.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ProcessingMachineMenu extends AbstractContainerMenu {
	private static final int MACHINE_SLOT_COUNT = 3;
	private static final int DATA_COUNT = 2;
	private static final int MACHINE_START = 0;
	private static final int MACHINE_END = 3;
	private static final int PLAYER_START = 3;
	private static final int PLAYER_END = 39;

	private final Container container;
	private final ContainerData data;

	public ProcessingMachineMenu(int containerId, Inventory playerInventory, BlockPos pos) {
		this(containerId, playerInventory, getContainer(playerInventory.player.level(), pos), getData(playerInventory.player.level(), pos));
	}

	public ProcessingMachineMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
		super(ModMenus.PROCESSING_MACHINE, containerId);
		checkContainerSize(container, MACHINE_SLOT_COUNT);
		checkContainerDataCount(data, DATA_COUNT);
		this.container = container;
		this.data = data;

		addSlot(new InputSlot(container, 0, 44, 35));
		addSlot(new OutputSlot(container, 1, 116, 35));
		addSlot(new OutputSlot(container, 2, 140, 35));
		addStandardInventorySlots(playerInventory, 8, 84);
		addDataSlots(data);
	}

	@Override
	public boolean stillValid(Player player) {
		return container.stillValid(player);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		Slot slot = slots.get(index);
		if (!slot.hasItem()) {
			return ItemStack.EMPTY;
		}

		ItemStack stack = slot.getItem();
		ItemStack original = stack.copy();

		if (index >= MACHINE_START && index < MACHINE_END) {
			if (!moveItemStackTo(stack, PLAYER_START, PLAYER_END, true)) {
				return ItemStack.EMPTY;
			}
			slot.onTake(player, stack);
		} else if (index >= PLAYER_START && index < PLAYER_END) {
			if (!moveItemStackTo(stack, MACHINE_START, MACHINE_START + 1, false)) {
				return ItemStack.EMPTY;
			}
		} else {
			return ItemStack.EMPTY;
		}

		if (stack.isEmpty()) {
			slot.setByPlayer(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}

		return original;
	}

	public int progressPercent() {
		int maxProgress = data.get(1);
		if (maxProgress <= 0) {
			return 0;
		}

		return data.get(0) * 100 / maxProgress;
	}

	public int progressPixels(int width) {
		int maxProgress = data.get(1);
		if (maxProgress <= 0) {
			return 0;
		}

		return data.get(0) * width / maxProgress;
	}

	private static Container getContainer(Level level, BlockPos pos) {
		if (level.getBlockEntity(pos) instanceof ProcessingMachineBlockEntity machine) {
			return machine;
		}

		return new SimpleContainer(MACHINE_SLOT_COUNT);
	}

	private static ContainerData getData(Level level, BlockPos pos) {
		if (level.getBlockEntity(pos) instanceof ProcessingMachineBlockEntity machine) {
			return machine.data();
		}

		return new SimpleContainerData(DATA_COUNT);
	}

	private static class InputSlot extends Slot {
		InputSlot(Container container, int slot, int x, int y) {
			super(container, slot, x, y);
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			return container instanceof ProcessingMachineBlockEntity machine && machine.canProcessInput(stack);
		}
	}

	private static class OutputSlot extends Slot {
		OutputSlot(Container container, int slot, int x, int y) {
			super(container, slot, x, y);
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			return false;
		}
	}
}
