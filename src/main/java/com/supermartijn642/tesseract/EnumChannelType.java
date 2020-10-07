package com.supermartijn642.tesseract;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.function.Supplier;

/**
 * Created 3/20/2020 by SuperMartijn642
 */
public enum EnumChannelType {

    ITEMS(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, () -> Items.CHEST), FLUID(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, () -> Items.BUCKET), ENERGY(CapabilityEnergy.ENERGY, () -> Items.REDSTONE);

    private Capability<?> capability;
    public Supplier<Item> item;

    EnumChannelType(Capability<?> capability, Supplier<Item> item){
        this.capability = capability;
        this.item = item;
    }

    public Capability<?> getCapability(){
        return this.capability;
    }

}
