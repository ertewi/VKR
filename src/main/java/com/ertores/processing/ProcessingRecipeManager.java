package com.ertores.processing;

import com.ertores.ErtoresMod;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ProcessingRecipeManager implements ResourceManagerReloadListener {
	private static final ProcessingRecipeManager INSTANCE = new ProcessingRecipeManager();
	private static volatile List<ProcessingRecipe> recipes = List.of();

	private ProcessingRecipeManager() {
	}

	public static void register() {
		ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(ErtoresMod.id("processing_recipes"), INSTANCE);
	}

	public static Optional<ProcessingRecipe> find(MachineOperation operation, ItemStack stack) {
		if (stack.isEmpty()) {
			return Optional.empty();
		}

		return recipes.stream()
				.filter(recipe -> recipe.matches(operation, stack))
				.findFirst();
	}

	public static Optional<ProcessingRecipe> findByInputItem(MachineOperation operation, ItemStack stack) {
		if (stack.isEmpty()) {
			return Optional.empty();
		}

		return recipes.stream()
				.filter(recipe -> recipe.operation() == operation && recipe.input() == stack.getItem())
				.findFirst();
	}

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		List<ProcessingRecipe> loaded = new ArrayList<>();
		Map<Identifier, Resource> resources = resourceManager.listResources("processing", id -> id.getPath().endsWith(".json"));

		for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
			try (BufferedReader reader = entry.getValue().openAsReader()) {
				JsonObject json = GsonHelper.parse(reader);
				loaded.add(parseRecipe(entry.getKey(), json));
			} catch (Exception exception) {
				ErtoresMod.LOGGER.warn("Skipping invalid processing recipe {}", entry.getKey(), exception);
			}
		}

		recipes = List.copyOf(loaded);
		ErtoresMod.LOGGER.info("Loaded {} ertOres processing recipes.", recipes.size());
	}

	private static ProcessingRecipe parseRecipe(Identifier id, JsonObject json) {
		MachineOperation operation = MachineOperation.byId(GsonHelper.getAsString(json, "operation"));
		JsonObject input = GsonHelper.getAsJsonObject(json, "input");
		JsonObject result = GsonHelper.getAsJsonObject(json, "result");
		Optional<ProcessingOutput> byproduct = GsonHelper.isObjectNode(json, "byproduct")
				? Optional.of(parseOutput(GsonHelper.getAsJsonObject(json, "byproduct")))
				: Optional.empty();

		return new ProcessingRecipe(
				id,
				operation,
				resolveItem(GsonHelper.getAsString(input, "item")),
				GsonHelper.getAsInt(input, "count", 1),
				parseOutput(result),
				byproduct,
				Math.max(1, GsonHelper.getAsInt(json, "processing_time", 200))
		);
	}

	private static ProcessingOutput parseOutput(JsonObject json) {
		return new ProcessingOutput(
				resolveItem(GsonHelper.getAsString(json, "item")),
				Math.max(1, GsonHelper.getAsInt(json, "count", 1))
		);
	}

	private static Item resolveItem(String id) {
		Identifier identifier = Identifier.parse(id);
		Item item = BuiltInRegistries.ITEM.getValue(identifier);

		if (item == Items.AIR) {
			throw new IllegalArgumentException("Unknown item: " + id);
		}

		return item;
	}
}
