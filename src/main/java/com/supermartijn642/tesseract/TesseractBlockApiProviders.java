package com.supermartijn642.tesseract;

import com.supermartijn642.core.CommonUtils;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import team.reborn.energy.api.EnergyStorage;

/**
 * Created 16/04/2023 by SuperMartijn642
 */
public class TesseractBlockApiProviders {

    @SuppressWarnings("UnstableApiUsage")
    public static void register(){
        ItemStorage.SIDED.registerForBlockEntity((entity, side) -> entity.getItemCapability(), Tesseract.tesseract_tile);
        FluidStorage.SIDED.registerForBlockEntity((entity, side) -> entity.getFluidCapability(), Tesseract.tesseract_tile);
        if(CommonUtils.isModLoaded("team_reborn_energy"))
            EnergyStorage.SIDED.registerForBlockEntity((entity, side) -> (EnergyStorage)entity.getEnergyCapability(), Tesseract.tesseract_tile);
    }
}
