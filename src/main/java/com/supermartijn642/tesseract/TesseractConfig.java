package com.supermartijn642.tesseract;

import com.supermartijn642.configlib.api.ConfigBuilders;
import com.supermartijn642.configlib.api.IConfigBuilder;

import java.util.function.Supplier;

/**
 * Created 7/11/2021 by SuperMartijn642
 */
public class TesseractConfig {

    public static final Supplier<Integer> saveInterval;
    public static final Supplier<Boolean> enableThermalRecipe;

    static{
        IConfigBuilder builder = ConfigBuilders.newTomlConfig("tesseract", null, false);

        builder.push("General");
        saveInterval = builder.comment("At what interval should tesseract data be saved? A value of '5' means tesseract data gets saved every 5 minutes.").define("saveInterval", 3, 1, 30);
        enableThermalRecipe = builder.comment("Should the alternative Thermal Series recipe be used when the Thermal Series mods are installed?").define("enableThermalRecipe", false);
        builder.pop();

        builder.build();
    }

    public static void init(){
        // need this to initialize the class
    }
}
