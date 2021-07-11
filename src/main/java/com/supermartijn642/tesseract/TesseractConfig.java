package com.supermartijn642.tesseract;

import com.supermartijn642.configlib.ModConfigBuilder;

/**
 * Created 7/11/2021 by SuperMartijn642
 */
public class TesseractConfig {

    static{
        ModConfigBuilder builder = new ModConfigBuilder("tesseract");

        builder.push("General");
        builder.pop();

        builder.build();
    }

    public static void init(){
        // need this to initialize the class
    }

}
