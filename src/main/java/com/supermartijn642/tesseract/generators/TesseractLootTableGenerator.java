package com.supermartijn642.tesseract.generators;

import com.supermartijn642.core.generator.LootTableGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.tesseract.Tesseract;

/**
 * Created 23/12/2022 by SuperMartijn642
 */
public class TesseractLootTableGenerator extends LootTableGenerator {

    public TesseractLootTableGenerator(ResourceCache cache){
        super("tesseract", cache);
    }

    @Override
    public void generate(){
        this.dropSelf(Tesseract.tesseract);
    }
}
