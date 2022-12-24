package com.supermartijn642.tesseract.generators;

import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.core.generator.TagGenerator;
import com.supermartijn642.tesseract.Tesseract;

/**
 * Created 23/12/2022 by SuperMartijn642
 */
public class TesseractTagGenerator extends TagGenerator {

    public TesseractTagGenerator(ResourceCache cache){
        super("tesseract", cache);
    }

    @Override
    public void generate(){
        this.blockMineableWithPickaxe().add(Tesseract.tesseract);
    }
}
