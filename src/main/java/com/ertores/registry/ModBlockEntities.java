package com.ertores.registry;

import com.ertores.ErtoresMod;
import com.ertores.block.entity.ProcessingMachineBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {
	public static BlockEntityType<ProcessingMachineBlockEntity> PROCESSING_MACHINE;

	private ModBlockEntities() {
	}

	public static void initialize() {
		PROCESSING_MACHINE = Registry.register(
				BuiltInRegistries.BLOCK_ENTITY_TYPE,
				ErtoresMod.id("processing_machine"),
				FabricBlockEntityTypeBuilder.create(
						ProcessingMachineBlockEntity::new,
						ModBlocks.CRUSHER,
						ModBlocks.GRINDER,
						ModBlocks.WASHER,
						ModBlocks.ELECTRIC_FURNACE
				).build()
		);
	}
}
