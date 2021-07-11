package com.supermartijn642.tesseract.recipe_conditions;

import com.google.gson.JsonObject;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;

import java.util.function.BooleanSupplier;

/**
 * Created 6/26/2021 by SuperMartijn642
 */
public class NotRecipeCondition implements IConditionFactory {

    @Override
    public BooleanSupplier parse(JsonContext context, JsonObject json){
        JsonObject object = json.getAsJsonObject("value");
        BooleanSupplier condition = CraftingHelper.getCondition(object, context);
        return () -> !condition.getAsBoolean();
    }
}
