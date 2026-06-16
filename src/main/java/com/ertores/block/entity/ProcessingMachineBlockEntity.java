package com.ertores.block.entity;

import com.ertores.block.ProcessingMachineBlock;
import com.ertores.processing.MachineOperation;
import com.ertores.processing.ProcessingOutput;
import com.ertores.processing.ProcessingRecipe;
import com.ertores.processing.ProcessingRecipeManager;
import com.ertores.menu.ProcessingMachineMenu;
import com.ertores.registry.ModBlockEntities;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Optional;

public class ProcessingMachineBlockEntity extends BlockEntity implements Container, ExtendedMenuProvider<BlockPos> {
	private static final int INPUT_SLOT = 0;
	private static final int OUTPUT_SLOT = 1;
	private static final int BYPRODUCT_SLOT = 2;
	private static final int SLOT_COUNT = 3;

	private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
	private int progress;
	private int maxProgress;
	private final ContainerData data = new ContainerData() {
		@Override
		public int get(int index) {
			return switch (index) {
				case 0 -> progress;
				case 1 -> maxProgress;
				default -> 0;
			};
		}

		@Override
		public void set(int index, int value) {
			switch (index) {
				case 0 -> progress = value;
				case 1 -> maxProgress = value;
				default -> {
				}
			}
		}

		@Override
		public int getCount() {
			return 2;
		}
	};

	public ProcessingMachineBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.PROCESSING_MACHINE, pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, ProcessingMachineBlockEntity machine) {
		if (!(state.getBlock() instanceof ProcessingMachineBlock block)) {
			return;
		}

		MachineOperation operation = block.operation();
		boolean wasActive = state.getValue(ProcessingMachineBlock.ACTIVE);
		ItemStack input = machine.items.get(INPUT_SLOT);

		if (machine.maxProgress <= 0) {
			Optional<ProcessingRecipe> recipe = ProcessingRecipeManager.find(operation, input);
			if (recipe.isEmpty() || !machine.canAccept(recipe.get())) {
				machine.progress = 0;
				if (wasActive) {
					level.setBlock(pos, state.setValue(ProcessingMachineBlock.ACTIVE, false), Block.UPDATE_ALL);
				}
				return;
			}

			machine.maxProgress = recipe.get().processingTime();
			machine.progress = 0;
			machine.setChanged();
		}

		if (!wasActive) {
			level.setBlock(pos, state.setValue(ProcessingMachineBlock.ACTIVE, true), Block.UPDATE_ALL);
		}

		machine.progress++;

		if (machine.progress >= machine.maxProgress) {
			ProcessingRecipeManager.find(operation, input).ifPresent(recipe -> {
				input.shrink(recipe.inputCount());
				machine.finishRecipe(recipe);
			});
			machine.progress = 0;
			machine.maxProgress = 0;
			level.setBlock(pos, state.setValue(ProcessingMachineBlock.ACTIVE, false), Block.UPDATE_ALL);
			machine.setChanged();
		}
	}

	public boolean insertOne(ItemStack heldStack, Player player) {
		if (heldStack.isEmpty() || maxProgress > 0) {
			return false;
		}

		MachineOperation operation = operation();
		Optional<ProcessingRecipe> recipe = ProcessingRecipeManager.findByInputItem(operation, heldStack);
		if (recipe.isEmpty()) {
			return false;
		}

		ItemStack input = items.get(INPUT_SLOT);
		int targetCount = Math.max(1, recipe.get().inputCount());
		int currentCount = input.isEmpty() ? 0 : input.getCount();
		int insertCount = Math.min(heldStack.getCount(), targetCount - currentCount);
		if (insertCount <= 0) {
			return false;
		}

		ItemStack candidate = heldStack.copyWithCount(insertCount);
		if (!input.isEmpty() && (!ItemStack.isSameItemSameComponents(input, candidate) || input.getCount() >= input.getItem().getDefaultMaxStackSize())) {
			return false;
		}

		if (input.isEmpty()) {
			items.set(INPUT_SLOT, candidate);
		} else {
			input.grow(insertCount);
		}

		if (!player.isCreative()) {
			heldStack.shrink(insertCount);
		}

		setChanged();
		return true;
	}

	public boolean extractTo(Player player) {
		ItemStack output = items.get(OUTPUT_SLOT);
		boolean extracted = moveToPlayer(player, output);
		if (extracted) {
			items.set(OUTPUT_SLOT, ItemStack.EMPTY);
		}

		ItemStack byproduct = items.get(BYPRODUCT_SLOT);
		boolean extractedByproduct = moveToPlayer(player, byproduct);
		if (extractedByproduct) {
			items.set(BYPRODUCT_SLOT, ItemStack.EMPTY);
		}

		if (extracted || extractedByproduct) {
			setChanged();
		}

		return extracted || extractedByproduct;
	}

	public Component status() {
		if (maxProgress > 0) {
			int percent = progress * 100 / maxProgress;
			return Component.translatable("message.ertores.machine_progress", percent);
		}

		if (!items.get(INPUT_SLOT).isEmpty()) {
			return Component.translatable("message.ertores.machine_waiting");
		}

		return Component.translatable("message.ertores.machine_empty");
	}

	public void dropStoredItems() {
		if (level == null) {
			return;
		}

		for (ItemStack stack : items) {
			Block.popResource(level, worldPosition, stack);
		}
		items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
	}

	public ContainerData data() {
		return data;
	}

	public boolean canProcessInput(ItemStack stack) {
		return ProcessingRecipeManager.findByInputItem(operation(), stack).isPresent();
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
		ContainerHelper.loadAllItems(input, items);

		input.read("input", ItemStack.OPTIONAL_CODEC).ifPresent(stack -> items.set(INPUT_SLOT, stack));
		input.read("output", ItemStack.OPTIONAL_CODEC).ifPresent(stack -> items.set(OUTPUT_SLOT, stack));
		input.read("byproduct", ItemStack.OPTIONAL_CODEC).ifPresent(stack -> items.set(BYPRODUCT_SLOT, stack));
		this.progress = input.getIntOr("progress", 0);
		this.maxProgress = input.getIntOr("max_progress", 0);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		ContainerHelper.saveAllItems(output, items);
		output.putInt("progress", progress);
		output.putInt("max_progress", maxProgress);
	}

	private void finishRecipe(ProcessingRecipe recipe) {
		items.set(OUTPUT_SLOT, merge(items.get(OUTPUT_SLOT), recipe.result()));
		recipe.byproduct().ifPresent(result -> items.set(BYPRODUCT_SLOT, merge(items.get(BYPRODUCT_SLOT), result)));
	}

	private boolean canAccept(ProcessingRecipe recipe) {
		return canMerge(items.get(OUTPUT_SLOT), recipe.result())
				&& recipe.byproduct().map(result -> canMerge(items.get(BYPRODUCT_SLOT), result)).orElse(true);
	}

	private static ItemStack merge(ItemStack current, ProcessingOutput result) {
		if (current.isEmpty()) {
			return result.stack();
		}

		current.grow(result.count());
		return current;
	}

	private static boolean canMerge(ItemStack current, ProcessingOutput result) {
		if (current.isEmpty()) {
			return true;
		}

		return current.getItem() == result.item() && current.getCount() + result.count() <= current.getItem().getDefaultMaxStackSize();
	}

	private static boolean moveToPlayer(Player player, ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}

		ItemStack remaining = stack.copy();
		if (!player.addItem(remaining)) {
			Block.popResource(player.level(), player.blockPosition(), remaining);
		}

		return true;
	}

	private MachineOperation operation() {
		if (getBlockState().getBlock() instanceof ProcessingMachineBlock block) {
			return block.operation();
		}

		return MachineOperation.CRUSHING;
	}

	@Override
	public int getContainerSize() {
		return SLOT_COUNT;
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack stack : items) {
			if (!stack.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack getItem(int slot) {
		return items.get(slot);
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		ItemStack stack = ContainerHelper.removeItem(items, slot, amount);
		if (!stack.isEmpty()) {
			setChanged();
		}
		return stack;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		return ContainerHelper.takeItem(items, slot);
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		items.set(slot, stack);
		stack.limitSize(getMaxStackSize(stack));
		setChanged();
	}

	@Override
	public boolean stillValid(Player player) {
		return Container.stillValidBlockEntity(this, player);
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		return slot == INPUT_SLOT && canProcessInput(stack);
	}

	@Override
	public void clearContent() {
		items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
	}

	@Override
	public Component getDisplayName() {
		return getBlockState().getBlock().getName();
	}

	@Override
	public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
		return new ProcessingMachineMenu(containerId, inventory, this, data);
	}

	@Override
	public BlockPos getScreenOpeningData(ServerPlayer player) {
		return worldPosition;
	}
}
