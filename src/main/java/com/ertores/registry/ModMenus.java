package com.ertores.registry;

import com.ertores.ErtoresMod;
import com.ertores.menu.ProcessingMachineMenu;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;

public final class ModMenus {
	public static final MenuType<ProcessingMachineMenu> PROCESSING_MACHINE = Registry.register(
			BuiltInRegistries.MENU,
			ErtoresMod.id("processing_machine"),
			new ExtendedMenuType<>(ProcessingMachineMenu::new, BlockPos.STREAM_CODEC)
	);

	private ModMenus() {
	}

	public static void initialize() {
	}
}
