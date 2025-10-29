package net.ninjadev.chickenhole;

import net.fabricmc.api.ModInitializer;
import net.ninjadev.chickenhole.init.ModEvents;
import net.ninjadev.chickenhole.init.ModNetwork;

public class ChickenHole implements ModInitializer {

    @Override
    public void onInitialize() {
        ModNetwork.init();
        ModEvents.init();

    }
}
