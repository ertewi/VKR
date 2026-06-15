package com.ertores.registry;

import com.ertores.ErtoresMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

public final class ModItems {
	public static final Item CRUSHED_IRON_ORE = register("crushed_iron_ore");
	public static final Item CRUSHED_COPPER_ORE = register("crushed_copper_ore");
	public static final Item CRUSHED_GOLD_ORE = register("crushed_gold_ore");

	public static final Item IRON_ORE_POWDER = register("iron_ore_powder");
	public static final Item COPPER_ORE_POWDER = register("copper_ore_powder");
	public static final Item GOLD_ORE_POWDER = register("gold_ore_powder");

	public static final Item IRON_CONCENTRATE = register("iron_concentrate");
	public static final Item COPPER_CONCENTRATE = register("copper_concentrate");
	public static final Item GOLD_CONCENTRATE = register("gold_concentrate");

	public static final Item TAILINGS = register("tailings");
	public static final Item SLAG = register("slag");
	public static final Item FLUX = register("flux");

	private ModItems() {
	}

	public static void initialize() {
	}

	private static Item register(String name) {
		Identifier id = ErtoresMod.id(name);
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
		return Registry.register(BuiltInRegistries.ITEM, id, new Item(new Item.Properties().setId(key)));
	}
}
