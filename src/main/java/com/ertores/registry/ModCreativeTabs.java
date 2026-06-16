package com.ertores.registry;

import com.ertores.ErtoresMod;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class ModCreativeTabs {
	private static final ResourceKey<CreativeModeTab> MAIN = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ErtoresMod.id("main"));

	private ModCreativeTabs() {
	}

	public static void initialize() {
		Registry.register(
				BuiltInRegistries.CREATIVE_MODE_TAB,
				MAIN,
				FabricCreativeModeTab.builder()
						.title(Component.translatable("itemGroup.ertores.main"))
						.icon(() -> new ItemStack(ModBlocks.CRUSHER))
						.displayItems((parameters, output) -> {
							output.accept(ModBlocks.CRUSHER);
							output.accept(ModBlocks.GRINDER);
							output.accept(ModBlocks.WASHER);
							output.accept(ModBlocks.ELECTRIC_FURNACE);

							output.accept(ModItems.CRUSHED_IRON_ORE);
							output.accept(ModItems.CRUSHED_COPPER_ORE);
							output.accept(ModItems.CRUSHED_GOLD_ORE);
							output.accept(ModItems.IRON_ORE_POWDER);
							output.accept(ModItems.COPPER_ORE_POWDER);
							output.accept(ModItems.GOLD_ORE_POWDER);
							output.accept(ModItems.IRON_CONCENTRATE);
							output.accept(ModItems.COPPER_CONCENTRATE);
							output.accept(ModItems.GOLD_CONCENTRATE);
							output.accept(ModItems.TAILINGS);
							output.accept(ModItems.SLAG);
							output.accept(ModItems.FLUX);
						})
						.build()
		);
	}
}
