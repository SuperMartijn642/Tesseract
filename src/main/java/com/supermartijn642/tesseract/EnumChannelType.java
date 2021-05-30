package com.supermartijn642.tesseract;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.function.Supplier;

/**
 * Created 3/20/2020 by SuperMartijn642
 */
public enum EnumChannelType {

    ITEMS(0, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, () -> Item.getItemFromBlock(Blocks.CHEST), "gui.tesseract.type.items"),
    FLUID(1, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, () -> Items.BUCKET, "gui.tesseract.type.fluid"),
    ENERGY(2, CapabilityEnergy.ENERGY, () -> Items.REDSTONE, "gui.tesseract.type.energy");

    private final int index;
    private final Capability<?> capability;
    public final Supplier<Item> item;
    public final String translationKey;

    EnumChannelType(int index, Capability<?> capability, Supplier<Item> item, String translationKey){
        this.index = index;
        this.capability = capability;
        this.item = item;
        this.translationKey = translationKey;
    }

    public Capability<?> getCapability(){
        return this.capability;
    }

    public int getIndex(){
        return this.index;
    }

    public ITextComponent getTranslation(){
        return new TextComponentTranslation(this.translationKey);
    }

    public static EnumChannelType byIndex(int index){
        for(EnumChannelType type : values())
            if(type.index == index)
                return type;
        return null;
    }

}
