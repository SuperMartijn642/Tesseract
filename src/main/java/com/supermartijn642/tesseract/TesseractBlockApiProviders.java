package com.supermartijn642.tesseract;

import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import java.util.function.Consumer;

/**
 * Created 16/04/2023 by SuperMartijn642
 */
public class TesseractBlockApiProviders {

    public static void register(){
        ModLoadingContext.get().getActiveContainer().getEventBus().addListener((Consumer<RegisterCapabilitiesEvent>)event -> {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, Tesseract.tesseract_tile, (entity, side) -> entity.getItemCapability());
            event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, Tesseract.tesseract_tile, (entity, side) -> entity.getFluidCapability());
            event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, Tesseract.tesseract_tile, (entity, side) -> entity.getEnergyCapability());
        });
    }
}
