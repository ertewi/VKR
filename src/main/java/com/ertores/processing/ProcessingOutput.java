package com.ertores.processing;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record ProcessingOutput(Item item, int count) {
	public ItemStack stack() {
		return new ItemStack(item, count);
	}
}
