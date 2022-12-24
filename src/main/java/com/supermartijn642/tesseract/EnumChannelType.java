package com.supermartijn642.tesseract;

import com.supermartijn642.core.TextComponents;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.text.ITextComponent;

import java.util.function.Supplier;

/**
 * Created 3/20/2020 by SuperMartijn642
 */
public enum EnumChannelType {

    ITEMS(0, () -> Items.CHEST, "gui.tesseract.type.items"),
    FLUID(1, () -> Items.BUCKET, "gui.tesseract.type.fluid"),
    ENERGY(2, () -> Items.REDSTONE, "gui.tesseract.type.energy");

    private final int index;
    public final Supplier<Item> item;
    public final String translationKey;

    EnumChannelType(int index, Supplier<Item> item, String translationKey){
        this.index = index;
        this.item = item;
        this.translationKey = translationKey;
    }

    public int getIndex(){
        return this.index;
    }

    public ITextComponent getTranslation(){
        return TextComponents.translation(this.translationKey).get();
    }

    public static EnumChannelType byIndex(int index){
        for(EnumChannelType type : values())
            if(type.index == index)
                return type;
        return null;
    }
}
