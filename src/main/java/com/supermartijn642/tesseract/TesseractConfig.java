package com.supermartijn642.tesseract;

import com.supermartijn642.configlib.ModConfigBuilder;

import java.util.function.Supplier;

/**
 * Created 7/11/2021 by SuperMartijn642
 */
public class TesseractConfig {

    public static final Supplier<Boolean> enableThermalRecipe;

    static{
        ModConfigBuilder builder = new ModConfigBuilder("tesseract");

        builder.push("General");
        enableThermalRecipe = builder.comment("Should the alternative Thermal Series recipe be used when the Thermal Series mods are installed?").define("enableThermalRecipe", false);
        builder.pop();

        builder.build();
    }

    public static void init(){
        // need this to initialize the class
    }

}
