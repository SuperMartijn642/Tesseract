package com.supermartijn642.tesseract.recipe_conditions;

import com.google.gson.JsonObject;
import com.supermartijn642.core.CommonUtils;
import com.supermartijn642.core.data.condition.ResourceCondition;
import com.supermartijn642.core.data.condition.ResourceConditionContext;
import com.supermartijn642.core.data.condition.ResourceConditionSerializer;
import com.supermartijn642.tesseract.TesseractConfig;

/**
 * Created 7/11/2021 by SuperMartijn642
 */
public class TesseractRecipeCondition implements ResourceCondition {

    public static final Serializer SERIALIZER = new Serializer();

    public TesseractRecipeCondition(){
    }

    @Override
    public boolean test(ResourceConditionContext context){
        return TesseractConfig.enableThermalRecipe.get() && CommonUtils.isModLoaded("thermal");
    }

    @Override
    public ResourceConditionSerializer<?> getSerializer(){
        return SERIALIZER;
    }

    public static class Serializer implements ResourceConditionSerializer<TesseractRecipeCondition> {

        @Override
        public void serialize(JsonObject json, TesseractRecipeCondition condition){
        }

        @Override
        public TesseractRecipeCondition deserialize(JsonObject json){
            return new TesseractRecipeCondition();
        }
    }
}
