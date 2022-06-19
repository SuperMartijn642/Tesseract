package com.supermartijn642.tesseract.recipe_conditions;

import com.google.gson.JsonObject;
import com.supermartijn642.tesseract.TesseractConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import net.minecraftforge.fml.ModList;

/**
 * Created 7/11/2021 by SuperMartijn642
 */
public class TesseractRecipeCondition implements ICondition {

    public static final Serializer SERIALIZER = new Serializer();

    public TesseractRecipeCondition(){
    }

    @Override
    public ResourceLocation getID(){
        return SERIALIZER.getID();
    }

    @Override
    public boolean test(IContext context){
        return TesseractConfig.enableThermalRecipe.get() && ModList.get().isLoaded("thermal");
    }

    @Override
    public String toString(){
        return "tesseractRecipeCondition()";
    }

    public static class Serializer implements IConditionSerializer<TesseractRecipeCondition> {

        @Override
        public void write(JsonObject json, TesseractRecipeCondition value){
        }

        @Override
        public TesseractRecipeCondition read(JsonObject json){
            return new TesseractRecipeCondition();
        }

        @Override
        public ResourceLocation getID(){
            return new ResourceLocation("tesseract", "thermal_recipe");
        }
    }

}
