package com.supermartijn642.tesseract.capabilities;

import com.supermartijn642.tesseract.TesseractBlockEntity;
import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractReference;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import java.util.Collections;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created 23/12/2025 by SuperMartijn642
 */
public class CombinedResourceHandler<S extends TransferVariant<?>> implements Storage<S> {

    private final Channel channel;
    private final TesseractReference requester;
    private final Function<TesseractBlockEntity,Iterable<Storage<S>>> getSurroundingCapabilities;

    public CombinedResourceHandler(Channel channel, TesseractReference requester, Function<TesseractBlockEntity,Iterable<Storage<S>>> getSurroundingCapabilities){
        this.channel = channel;
        this.requester = requester;
        this.getSurroundingCapabilities = getSurroundingCapabilities;
    }

    @Override
    public boolean supportsInsertion(){
        return this.requester.canSend(this.channel.type);
    }

    @Override
    public long insert(S resource, long amount, TransactionContext transaction){
        StoragePreconditions.notBlankNotNegative(resource, amount);
        if(!this.requester.canSend(this.channel.type))
            return 0;
        return this.runSafe(0L, () -> {
            long leftOver = amount;
            for(TesseractReference reference : this.channel.receivingTesseracts){
                if(reference != this.requester && reference.canBeAccessed()){
                    TesseractBlockEntity entity = reference.getTesseract();
                    for(Storage<S> handler : this.getSurroundingCapabilities.apply(entity)){
                        if(handler.supportsInsertion()){
                            long inserted = handler.insert(resource, leftOver, transaction);
                            if(inserted < 0)
                                throw new IllegalStateException("Storage of class '" + handler.getClass().getName() + "' obtained from block entity '" + entity.getClass().getName() + "' returned '" + inserted + "' for #insert()!");
                            leftOver -= inserted;
                            if(leftOver <= 0)
                                return amount;
                        }
                    }
                }
            }
            return amount - leftOver;
        });
    }

    @Override
    public boolean supportsExtraction(){
        return this.requester.canReceive(this.channel.type);
    }

    @Override
    public long extract(S resource, long amount, TransactionContext transaction){
        StoragePreconditions.notBlankNotNegative(resource, amount);
        if(!this.requester.canReceive(this.channel.type))
            return 0;
        return this.runSafe(0L, () -> {
            long leftOver = amount;
            for(TesseractReference reference : this.channel.sendingTesseracts){
                if(reference != this.requester && reference.canBeAccessed()){
                    TesseractBlockEntity entity = reference.getTesseract();
                    for(Storage<S> handler : this.getSurroundingCapabilities.apply(entity)){
                        if(handler.supportsExtraction()){
                            long extracted = handler.extract(resource, leftOver, transaction);
                            if(extracted < 0)
                                throw new IllegalStateException("Storage of class '" + handler.getClass().getName() + "' obtained from block entity '" + entity.getClass().getName() + "' returned '" + extracted + "' for #extract()!");
                            leftOver -= extracted;
                            if(leftOver <= 0)
                                return amount;
                        }
                    }
                }
            }
            return amount - leftOver;
        });
    }

    @Override
    public Iterator<StorageView<S>> iterator(){
        if(this.pushRecurrentCall())
            return Collections.emptyIterator();
        this.popRecurrentCall();

        // TODO the #extract on the storage views should ideally be guarded with a check for whether the relevant tesseract allows receiving resources

        Iterator<TesseractReference> tesseracts = this.channel.tesseracts.iterator();
        return new FlatMapIterator<>(new FlatMapIterator<>(tesseracts, reference -> {
            if(reference != this.requester && reference.canBeAccessed()){
                TesseractBlockEntity entity = reference.getTesseract();
                return this.getSurroundingCapabilities.apply(entity).iterator();
            }
            return Collections.emptyIterator();
        }), Storage::iterator, this::pushRecurrentCall, this::popRecurrentCall);
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

    private <T> T runSafe(T defaultValue, Supplier<T> supplier){
        if(this.pushRecurrentCall())
            return defaultValue;
        try{
            return supplier.get();
        }finally{
            this.popRecurrentCall();
        }
    }
}
