package com.ertores.block.entity;

import com.ertores.block.ProcessingMachineBlock;
import com.ertores.processing.MachineOperation;
import com.ertores.processing.ProcessingOutput;
import com.ertores.processing.ProcessingRecipe;
import com.ertores.processing.ProcessingRecipeManager;
import com.ertores.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Optional;

public class ProcessingMachineBlockEntity extends BlockEntity {
	private ItemStack input = ItemStack.EMPTY;
	private ItemStack output = ItemStack.EMPTY;
	private ItemStack byproduct = ItemStack.EMPTY;
	private int progress;
	private int maxProgress;

	public ProcessingMachineBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.PROCESSING_MACHINE, pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, ProcessingMachineBlockEntity machine) {
		if (!(state.getBlock() instanceof ProcessingMachineBlock block)) {
			return;
		}

		MachineOperation operation = block.operation();
		boolean wasActive = state.getValue(ProcessingMachineBlock.ACTIVE);

		if (machine.maxProgress <= 0) {
			Optional<ProcessingRecipe> recipe = ProcessingRecipeManager.find(operation, machine.input);
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
			ProcessingRecipeManager.find(operation, machine.input).ifPresent(recipe -> {
				machine.input.shrink(recipe.inputCount());
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
			input = candidate;
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
		boolean extracted = moveToPlayer(player, output);
		if (extracted) {
			output = ItemStack.EMPTY;
		}

		boolean extractedByproduct = moveToPlayer(player, byproduct);
		if (extractedByproduct) {
			byproduct = ItemStack.EMPTY;
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

		if (!input.isEmpty()) {
			return Component.translatable("message.ertores.machine_waiting");
		}

		return Component.translatable("message.ertores.machine_empty");
	}

	public void dropStoredItems() {
		if (level == null) {
			return;
		}

		Block.popResource(level, worldPosition, input);
		Block.popResource(level, worldPosition, output);
		Block.popResource(level, worldPosition, byproduct);
		input = ItemStack.EMPTY;
		output = ItemStack.EMPTY;
		byproduct = ItemStack.EMPTY;
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.input = input.read("input", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
		this.output = input.read("output", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
		this.byproduct = input.read("byproduct", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
		this.progress = input.getIntOr("progress", 0);
		this.maxProgress = input.getIntOr("max_progress", 0);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.store("input", ItemStack.OPTIONAL_CODEC, input);
		output.store("output", ItemStack.OPTIONAL_CODEC, this.output);
		output.store("byproduct", ItemStack.OPTIONAL_CODEC, byproduct);
		output.putInt("progress", progress);
		output.putInt("max_progress", maxProgress);
	}

	private void finishRecipe(ProcessingRecipe recipe) {
		output = merge(output, recipe.result());
		recipe.byproduct().ifPresent(result -> byproduct = merge(byproduct, result));
	}

	private boolean canAccept(ProcessingRecipe recipe) {
		return canMerge(output, recipe.result()) && recipe.byproduct().map(result -> canMerge(byproduct, result)).orElse(true);
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
}
