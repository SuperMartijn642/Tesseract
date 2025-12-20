package com.supermartijn642.tesseract.capabilities;

import com.supermartijn642.tesseract.TesseractBlockEntity;
import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractReference;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;

/**
 * Created 16/04/2023 by SuperMartijn642
 */
public class CombinedFluidHandler extends CombinedResourceHandler<FluidVariant> {

    public CombinedFluidHandler(Channel channel, TesseractReference requester){
        super(channel, requester, TesseractBlockEntity::getSurroundingFluidCapabilities);
    }
}
