package com.supermartijn642.tesseract;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

/**
 * Created 3/20/2020 by SuperMartijn642
 */
public enum EnumChannelType {

    ITEMS(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY), FLUID(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY), ENERGY(CapabilityEnergy.ENERGY);

    private Capability<?> capability;

    EnumChannelType(Capability<?> capability){
        this.capability = capability;
    }

    public Capability<?> getCapability(){
        return this.capability;
    }

}
