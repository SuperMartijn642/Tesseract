package com.supermartijn642.tesseract.generators;

import com.supermartijn642.core.generator.BlockStateGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.tesseract.Tesseract;

/**
 * Created 23/12/2022 by SuperMartijn642
 */
public class TesseractBlockStateGenerator extends BlockStateGenerator {

    public TesseractBlockStateGenerator(ResourceCache cache){
        super("tesseract", cache);
    }

    @Override
    public void generate(){
        this.blockState(Tesseract.tesseract).emptyVariant(variant -> variant.model("block/on"));
    }
}
