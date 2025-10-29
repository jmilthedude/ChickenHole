package net.ninjadev.chickenhole.util;

import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constants {


    public static final String MOD_ID = "chickenhole";
    public static final String MOD_NAME = "Chicken Hole";
    public static final String VERSION = "1.0.0";
    public static final Identifier SCREEN_ID = Identifier.of(MOD_ID, "chicken_screen");

    public static final Logger LOG = LoggerFactory.getLogger(MOD_ID);
}
