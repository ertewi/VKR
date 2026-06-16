package com.ertores.client;

import com.ertores.client.gui.ProcessingMachineScreen;
import com.ertores.menu.ProcessingMachineMenu;
import com.ertores.registry.ModMenus;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ErtoresModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		registerProcessingMachineScreen();
	}

	private static void registerProcessingMachineScreen() {
		try {
			Class<?> constructorClass = Class.forName("net.minecraft.client.gui.screens.MenuScreens$ScreenConstructor");
			Object constructor = Proxy.newProxyInstance(
					ErtoresModClient.class.getClassLoader(),
					new Class<?>[]{constructorClass},
					(proxy, method, args) -> {
						if ("create".equals(method.getName())) {
							return new ProcessingMachineScreen(
									(ProcessingMachineMenu) args[0],
									(Inventory) args[1],
									(Component) args[2]
							);
						}
						if ("toString".equals(method.getName())) {
							return "ertores:processing_machine_screen";
						}
						return null;
					}
			);
			Method register = MenuScreens.class.getDeclaredMethod("register", MenuType.class, constructorClass);
			register.setAccessible(true);
			register.invoke(null, ModMenus.PROCESSING_MACHINE, constructor);
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException("Failed to register ertOres processing machine screen", exception);
		}
	}
}
