package com.supermartijn642.tesseract.capabilities;

import com.supermartijn642.tesseract.TesseractBlockEntity;
import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractReference;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created 23/12/2025 by SuperMartijn642
 */
public class CombinedResourceHandler<S extends Resource> implements ResourceHandler<S> {

    private final Channel channel;
    private final TesseractReference requester;
    private final S emptyResource;
    private final Function<TesseractBlockEntity,Iterable<ResourceHandler<S>>> getSurroundingCapabilities;

    public CombinedResourceHandler(Channel channel, TesseractReference requester, S emptyResource, Function<TesseractBlockEntity,Iterable<ResourceHandler<S>>> getSurroundingCapabilities){
        this.channel = channel;
        this.requester = requester;
        this.emptyResource = emptyResource;
        this.getSurroundingCapabilities = getSurroundingCapabilities;
    }

    @Override
    public int size(){
        return this.runSafe(0, () -> {
            int size = 0;
            for(TesseractReference reference : this.channel.tesseracts){
                if(reference != this.requester && reference.canBeAccessed()){
                    TesseractBlockEntity entity = reference.getTesseract();
                    for(ResourceHandler<ItemResource> handler : entity.getSurroundingItemCapabilities()){
                        int handlerSize = handler.size();
                        if(handlerSize < 0)
                            throw new IllegalStateException("Resource handler of class '" + handler.getClass().getName() + "' obtained from block entity '" + entity.getClass().getName() + "' returned '" + handlerSize + "' for #size()!");
                        size += handlerSize;
                    }
                }
            }
            return size;
        });
    }

    @Override
    public S getResource(int index){
        if(index < 0)
            return this.emptyResource;
        return this.runSafe(this.emptyResource, () -> {
            int counter = 0;
            for(TesseractReference reference : this.channel.tesseracts){
                if(reference != this.requester && reference.canBeAccessed()){
                    TesseractBlockEntity entity = reference.getTesseract();
                    for(ResourceHandler<S> handler : this.getSurroundingCapabilities.apply(entity)){
                        if(index - counter < handler.size())
                            return handler.getResource(index - counter);
                        else
                            counter += handler.size();
                    }
                }
            }
            return this.emptyResource;
        });
    }

    @Override
    public long getAmountAsLong(int index){
        if(index < 0)
            return 0;
        return this.runSafe(0L, () -> {
            int counter = 0;
            for(TesseractReference reference : this.channel.tesseracts){
                if(reference != this.requester && reference.canBeAccessed()){
                    TesseractBlockEntity entity = reference.getTesseract();
                    for(ResourceHandler<S> handler : this.getSurroundingCapabilities.apply(entity)){
                        if(index - counter < handler.size()){
                            long amount = handler.getAmountAsLong(index - counter);
                            if(amount < 0)
                                throw new IllegalStateException("Resource handler of class '" + handler.getClass().getName() + "' obtained from block entity '" + entity.getClass().getName() + "' returned '" + amount + "' for #getAmountAsLong()!");
                            return amount;
                        }else
                            counter += handler.size();
                    }
                }
            }
            return 0L;
        });
    }

    @Override
    public long getCapacityAsLong(int index, S resource){
        if(index < 0)
            return 0;
        return this.runSafe(0L, () -> {
            int counter = 0;
            for(TesseractReference reference : this.channel.tesseracts){
                if(reference != this.requester && reference.canBeAccessed()){
                    TesseractBlockEntity entity = reference.getTesseract();
                    for(ResourceHandler<S> handler : this.getSurroundingCapabilities.apply(entity)){
                        if(index - counter < handler.size()){
                            long capacity = handler.getCapacityAsLong(index - counter, resource);
                            if(capacity < 0)
                                throw new IllegalStateException("Resource handler of class '" + handler.getClass().getName() + "' obtained from block entity '" + entity.getClass().getName() + "' returned '" + capacity + "' for #capacityAsLong()!");
                            return capacity;
                        }else
                            counter += handler.size();
                    }
                }
            }
            return 0L;
        });
    }

    @Override
    public boolean isValid(int index, S resource){
        TransferPreconditions.checkNonEmpty(resource);
        if(index < 0)
            return false;
        return this.runSafe(true, () -> {
            int counter = 0;
            for(TesseractReference reference : this.channel.tesseracts){
                if(reference != this.requester && reference.canBeAccessed()){
                    TesseractBlockEntity entity = reference.getTesseract();
                    for(ResourceHandler<S> handler : this.getSurroundingCapabilities.apply(entity)){
                        if(index - counter < handler.size())
                            return handler.isValid(index - counter, resource);
                        else
                            counter += handler.size();
                    }
                }
            }
            return true;
        });
    }

    @Override
    public int insert(int index, S resource, int amount, TransactionContext transaction){
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if(index < 0 || !this.requester.canSend(this.channel.type))
            return 0;
        return this.runSafe(0, () -> {
            int counter = 0;
            // Unfortunately, as a specific index is specified, we need to iterate over all tesseracts rather than just the ones that can actually receive resources
            for(TesseractReference reference : this.channel.tesseracts){
                if(reference != this.requester && reference.canBeAccessed()){
                    TesseractBlockEntity entity = reference.getTesseract();
                    for(ResourceHandler<S> handler : this.getSurroundingCapabilities.apply(entity)){
                        if(index - counter < handler.size()){
                            if(!reference.canReceive(this.channel.type))
                                return 0;
                            int inserted = handler.insert(index - counter, resource, amount, transaction);
                            if(inserted < 0)
                                throw new IllegalStateException("Resource handler of class '" + handler.getClass().getName() + "' obtained from block entity '" + entity.getClass().getName() + "' returned '" + inserted + "' for #insert()!");
                            return inserted;
                        }else
                            counter += handler.size();
                    }
                }
            }
            return 0;
        });
    }

    @Override
    public int insert(S resource, int amount, TransactionContext transaction){
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if(!this.requester.canSend(this.channel.type))
            return 0;
        return this.runSafe(0, () -> {
            int leftOver = amount;
            for(TesseractReference reference : this.channel.receivingTesseracts){
                if(reference != this.requester && reference.canBeAccessed()){
                    TesseractBlockEntity entity = reference.getTesseract();
                    for(ResourceHandler<S> handler : this.getSurroundingCapabilities.apply(entity)){
                        int inserted = handler.insert(resource, leftOver, transaction);
                        if(inserted < 0)
                            throw new IllegalStateException("Resource handler of class '" + handler.getClass().getName() + "' obtained from block entity '" + entity.getClass().getName() + "' returned '" + inserted + "' for #insert()!");
                        leftOver -= inserted;
                        if(leftOver <= 0)
                            return amount;
                    }
                }
            }
            return amount - leftOver;
        });
    }

    @Override
    public int extract(int index, S resource, int amount, TransactionContext transaction){
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if(index < 0 || !this.requester.canReceive(this.channel.type))
            return 0;
        return this.runSafe(0, () -> {
            int counter = 0;
            // Unfortunately, as a specific index is specified, we need to iterate over all tesseracts rather than just the ones that can actually provide resources
            for(TesseractReference reference : this.channel.tesseracts){
                if(reference != this.requester && reference.canBeAccessed()){
                    TesseractBlockEntity entity = reference.getTesseract();
                    for(ResourceHandler<S> handler : this.getSurroundingCapabilities.apply(entity)){
                        if(index - counter < handler.size()){
                            if(!reference.canSend(this.channel.type))
                                return 0;
                            int extracted = handler.extract(index - counter, resource, amount, transaction);
                            if(extracted < 0)
                                throw new IllegalStateException("Resource handler of class '" + handler.getClass().getName() + "' obtained from block entity '" + entity.getClass().getName() + "' returned '" + extracted + "' for #extract()!");
                            return extracted;
                        }else
                            counter += handler.size();
                    }
                }
            }
            return 0;
        });
    }

    @Override
    public int extract(S resource, int amount, TransactionContext transaction){
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if(!this.requester.canReceive(this.channel.type))
            return 0;
        return this.runSafe(0, () -> {
            int leftOver = amount;
            for(TesseractReference reference : this.channel.sendingTesseracts){
                if(reference != this.requester && reference.canBeAccessed()){
                    TesseractBlockEntity entity = reference.getTesseract();
                    for(ResourceHandler<S> handler : this.getSurroundingCapabilities.apply(entity)){
                        int extracted = handler.extract(resource, leftOver, transaction);
                        if(extracted < 0)
                            throw new IllegalStateException("Resource handler of class '" + handler.getClass().getName() + "' obtained from block entity '" + entity.getClass().getName() + "' returned '" + extracted + "' for #extract()!");
                        leftOver -= extracted;
                        if(leftOver <= 0)
                            return amount;
                    }
                }
            }
            return amount - leftOver;
        });
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
