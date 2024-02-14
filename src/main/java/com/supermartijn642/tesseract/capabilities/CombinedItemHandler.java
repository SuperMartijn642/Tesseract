package com.supermartijn642.tesseract.capabilities;

import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.TesseractBlockEntity;
import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractReference;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import java.util.Collections;
import java.util.Iterator;

/**
 * Created 16/04/2023 by SuperMartijn642
 */
@SuppressWarnings("UnstableApiUsage")
public class CombinedItemHandler implements Storage<ItemVariant> {

    private final Channel channel;
    private final TesseractBlockEntity requester;

    public CombinedItemHandler(Channel channel, TesseractBlockEntity requester){
        this.channel = channel;
        this.requester = requester;
    }

    @Override
    public boolean supportsInsertion(){
        return this.requester.canSend(EnumChannelType.ITEMS);
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction){
        if(this.pushRecurrentCall())
            return 0;

        if(!this.requester.canSend(EnumChannelType.ITEMS) || resource.isBlank()){
            this.popRecurrentCall();
            return 0;
        }

        long leftOver = maxAmount;
        loop:
        for(TesseractReference reference : this.channel.receivingTesseracts){
            if(reference.canBeAccessed()){
                TesseractBlockEntity entity = reference.getTesseract();
                if(entity != this.requester){
                    for(Storage<ItemVariant> handler : entity.getSurroundingItemCapabilities()){
                        if(handler.supportsInsertion()){
                            leftOver -= handler.insert(resource, leftOver, transaction);
                            if(leftOver <= 0)
                                break loop;
                        }
                    }
                }
            }
        }

        this.popRecurrentCall();

        return maxAmount - leftOver;
    }

    @Override
    public boolean supportsExtraction(){
        return this.requester.canReceive(EnumChannelType.ITEMS);
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction){
        if(this.pushRecurrentCall())
            return 0;

        if(!this.requester.canReceive(EnumChannelType.ITEMS) || resource.isBlank()){
            this.popRecurrentCall();
            return 0;
        }

        long leftOver = maxAmount;
        loop:
        for(TesseractReference reference : this.channel.sendingTesseracts){
            if(reference.canBeAccessed()){
                TesseractBlockEntity entity = reference.getTesseract();
                if(entity != this.requester){
                    for(Storage<ItemVariant> handler : entity.getSurroundingItemCapabilities()){
                        if(handler.supportsExtraction()){
                            leftOver -= handler.extract(resource, leftOver, transaction);
                            if(leftOver <= 0)
                                break loop;
                        }
                    }
                }
            }
        }

        this.popRecurrentCall();

        return maxAmount - leftOver;
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator(){
        Iterator<TesseractReference> tesseracts = this.channel.tesseracts.iterator();
        return new FlatMapIterator<>(new FlatMapIterator<>(tesseracts, reference -> {
            if(reference.canBeAccessed()){
                TesseractBlockEntity entity = reference.getTesseract();
                if(entity != this.requester)
                    return entity.getSurroundingItemCapabilities().iterator();
            }
            return Collections.emptyIterator();
        }, this::pushRecurrentCall, this::popRecurrentCall), Storage::iterator);
    }

    /**
     * Checks whether this is a recurrent call to this combined capability.
     * If not, it will just increase the recurrent call counter.
     */
    private boolean pushRecurrentCall(){
        if(this.requester.recurrentCalls >= 1)
            return true;
        this.requester.recurrentCalls++;
        return false;
    }

    private void popRecurrentCall(){
        this.requester.recurrentCalls--;
    }
}
