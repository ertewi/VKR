package com.ertores.processing;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public record ProcessingRecipe(
		Identifier id,
		MachineOperation operation,
		Item input,
		int inputCount,
		ProcessingOutput result,
		Optional<ProcessingOutput> byproduct,
		int processingTime
) {
	public boolean matches(MachineOperation operation, ItemStack stack) {
		return this.operation == operation && stack.getItem() == input && stack.getCount() >= inputCount;
	}
}
