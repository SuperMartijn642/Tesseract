package com.supermartijn642.tesseract.generators;

import com.supermartijn642.core.generator.RecipeGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.tesseract.Tesseract;
import com.supermartijn642.tesseract.recipe_conditions.TesseractRecipeCondition;

/**
 * Created 23/12/2022 by SuperMartijn642
 */
public class TesseractRecipeGenerator extends RecipeGenerator {

    public TesseractRecipeGenerator(ResourceCache cache){
        super("tesseract", cache);
    }

    @Override
    public void generate(){
        // Tesseract recipe
        this.shaped(Tesseract.tesseract.asItem())
            .pattern("ABA")
            .pattern("BCB")
            .pattern("ABA")
            .input('A', "obsidian")
            .input('B', "enderpearl")
            .input('C', "blockDiamond")
            .notCondition(new TesseractRecipeCondition());

        // Thermal Expansion recipe
        this.shaped("thermal", Tesseract.tesseract.asItem())
            .pattern("ABA")
            .pattern("BCB")
            .pattern("ABA")
            .input('A', "ingotEnderium")
            .input('B', "blockGlassHardened")
            .input('C', "blockDiamond")
            .condition(new TesseractRecipeCondition());
    }
}
