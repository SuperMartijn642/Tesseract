package com.supermartijn642.tesseract.capabilities;

import com.supermartijn642.tesseract.TesseractBlockEntity;
import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractReference;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

/**
 * Created 3/20/2020 by SuperMartijn642
 */
public class CombinedFluidHandler extends CombinedResourceHandler<FluidResource> {

    public CombinedFluidHandler(Channel channel, TesseractReference requester){
        super(channel, requester, FluidResource.EMPTY, TesseractBlockEntity::getSurroundingFluidCapabilities);
    }
}
