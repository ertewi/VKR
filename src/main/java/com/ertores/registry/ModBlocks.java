package com.ertores.registry;

import com.ertores.ErtoresMod;
import com.ertores.block.ProcessingMachineBlock;
import com.ertores.processing.MachineOperation;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class ModBlocks {
	public static final ProcessingMachineBlock CRUSHER = registerMachine("crusher", MachineOperation.CRUSHING);
	public static final ProcessingMachineBlock GRINDER = registerMachine("grinder", MachineOperation.GRINDING);
	public static final ProcessingMachineBlock WASHER = registerMachine("washer", MachineOperation.ENRICHING);
	public static final ProcessingMachineBlock ELECTRIC_FURNACE = registerMachine("electric_furnace", MachineOperation.SMELTING);

	private ModBlocks() {
	}

	public static void initialize() {
	}

	private static ProcessingMachineBlock registerMachine(String name, MachineOperation operation) {
		Identifier id = ErtoresMod.id(name);
		ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);
		ProcessingMachineBlock block = new ProcessingMachineBlock(
				operation,
				BlockBehaviour.Properties.of()
						.setId(blockKey)
						.mapColor(MapColor.METAL)
						.strength(3.5F, 6.0F)
						.requiresCorrectToolForDrops()
						.sound(SoundType.METAL)
		);

		Registry.register(BuiltInRegistries.BLOCK, id, block);
		Registry.register(BuiltInRegistries.ITEM, id, new BlockItem(block, new Item.Properties().setId(itemKey)));
		return block;
	}
}
