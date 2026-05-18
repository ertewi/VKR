package com.ertores.server;

import net.fabricmc.api.DedicatedServerModInitializer;

public class ErtoresModServer implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        System.out.println("ERTORES Server is initializing!");
    }
}