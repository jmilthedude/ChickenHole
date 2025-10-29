package net.ninjadev.chickenhole.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.ninjadev.chickenhole.client.screen.ChickenScreen;
import net.ninjadev.chickenhole.network.ChickenScreenPayload;

public class ChickenHoleClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ChickenScreenPayload.ID, this::openChickenScreen);
    }

    private void openChickenScreen(ChickenScreenPayload payload, ClientPlayNetworking.Context context) {
        MinecraftClient client = context.client();
        client.execute(() -> {
            if (client.player == null || client.world == null) return;
            client.setScreen(new ChickenScreen(payload.getOrigin()));
        });
    }
}
