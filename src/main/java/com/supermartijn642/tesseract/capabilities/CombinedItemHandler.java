package com.supermartijn642.tesseract.capabilities;

import com.supermartijn642.tesseract.TesseractBlockEntity;
import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractReference;
import net.neoforged.neoforge.transfer.item.ItemResource;

/**
 * Created 3/20/2020 by SuperMartijn642
 */
public class CombinedItemHandler extends CombinedResourceHandler<ItemResource> {

    public CombinedItemHandler(Channel channel, TesseractReference requester){
        super(channel, requester, ItemResource.EMPTY, TesseractBlockEntity::getSurroundingItemCapabilities);
    }
}
