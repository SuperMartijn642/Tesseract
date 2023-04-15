package com.supermartijn642.tesseract.generators;

import com.supermartijn642.core.generator.RecipeGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.tesseract.Tesseract;
import net.minecraftforge.common.Tags;

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
        this.shaped(Tesseract.tesseract)
            .pattern("ABA")
            .pattern("BCB")
            .pattern("ABA")
            .input('A', Tags.Items.OBSIDIAN)
            .input('B', Tags.Items.ENDER_PEARLS)
            .input('C', Tags.Items.STORAGE_BLOCKS_DIAMOND);
//            .notCondition(new TesseractRecipeCondition().negate());

        // Thermal Expansion recipe // TODO re-enable this once thermal expansion is updated
//        this.shaped("thermal", Tesseract.tesseract)
//            .pattern("ABA")
//            .pattern("BCB")
//            .pattern("ABA")
//            .input('A', TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("forge", "ingots/enderium")))
//            .input('B', Registries.ITEMS.getValue(new ResourceLocation("thermal", "obsidian_glass")))
//            .input('C', Registries.ITEMS.getValue(new ResourceLocation("thermal", "machine_frame")))
//            .condition(new TesseractRecipeCondition());
    }
}
