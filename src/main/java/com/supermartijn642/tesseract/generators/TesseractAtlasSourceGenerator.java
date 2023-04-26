package com.supermartijn642.tesseract.generators;

import com.supermartijn642.core.generator.AtlasSourceGenerator;
import com.supermartijn642.core.generator.ResourceCache;

/**
 * Created 20/01/2023 by SuperMartijn642
 */
public class TesseractAtlasSourceGenerator extends AtlasSourceGenerator {

    public TesseractAtlasSourceGenerator(ResourceCache cache){
        super("tesseract", cache);
    }

    @Override
    public void generate(){
        this.blockAtlas().texturesFromModel("block/on");
        this.blockAtlas().texturesFromModel("item/tesseract");
    }
}
