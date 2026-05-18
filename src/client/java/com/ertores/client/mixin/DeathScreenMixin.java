package com.ertores.client.mixin;

import net.minecraft.client.gui.screens.DeathScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;


@Mixin(value = DeathScreen.class)
public class DeathScreenMixin {
    @Overwrite
    public boolean shouldCloseOnEsc() {
        return true;
    }
}