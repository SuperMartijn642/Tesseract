package com.supermartijn642.tesseract.recipe_conditions;

import com.google.gson.JsonObject;
import com.supermartijn642.tesseract.TesseractConfig;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.fml.common.Loader;

import java.util.function.BooleanSupplier;

/**
 * Created 7/11/2021 by SuperMartijn642
 */
public class TesseractRecipeCondition implements IConditionFactory {

    @Override
    public BooleanSupplier parse(JsonContext context, JsonObject json){
        return () -> TesseractConfig.enableThermalRecipe.get() && Loader.isModLoaded("thermalfoundation");
    }
}
