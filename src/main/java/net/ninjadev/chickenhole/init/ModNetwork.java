package net.ninjadev.chickenhole.init;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.ninjadev.chickenhole.network.ChickenScreenPayload;
import net.ninjadev.chickenhole.network.DigPayload;

public class ModNetwork {

    public static void init() {
        PayloadTypeRegistry.playS2C().register(ChickenScreenPayload.ID, ChickenScreenPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(DigPayload.ID, DigPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(DigPayload.ID, DigPayload::handle);
    }
}
