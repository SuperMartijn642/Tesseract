package com.supermartijn642.tesseract.capabilities;

import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.TesseractBlockEntity;
import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractReference;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import team.reborn.energy.api.EnergyStorage;

import java.util.function.Supplier;

/**
 * Created 3/20/2020 by SuperMartijn642
 */
public class CombinedEnergyStorage implements EnergyStorage {

    private final Channel channel;
    private final TesseractReference requester;

    public CombinedEnergyStorage(Channel channel, TesseractReference requester){
        this.channel = channel;
        this.requester = requester;
    }

    @Override
    public long getAmount(){
        return this.runSafe(0L, () -> {
            long amount = 0;
            for(TesseractReference reference : this.channel.tesseracts){
                if(reference != this.requester && reference.canBeAccessed()){
                    TesseractBlockEntity entity = reference.getTesseract();
                    for(EnergyStorage handler : entity.getSurroundingEnergyCapabilities()){
                        long handlerAmount = handler.getAmount();
                        if(handlerAmount < 0)
                            throw new IllegalStateException("Energy storage of class '" + handler.getClass().getName() + "' obtained from block entity '" + entity.getClass().getName() + "' returned '" + handlerAmount + "' for #getAmount()!");
                        amount += handlerAmount;
                    }
                }
            }
            return amount;
        });
    }

    @Override
    public long getCapacity(){
        return this.runSafe(0L, () -> {
            long capacity = 0;
            for(TesseractReference reference : this.channel.tesseracts){
                if(reference != this.requester && reference.canBeAccessed()){
                    TesseractBlockEntity entity = reference.getTesseract();
                    for(EnergyStorage handler : entity.getSurroundingEnergyCapabilities()){
                        long handlerCapacity = handler.getCapacity();
                        if(handlerCapacity < 0)
                            throw new IllegalStateException("Energy storage of class '" + handler.getClass().getName() + "' obtained from block entity '" + entity.getClass().getName() + "' returned '" + handlerCapacity + "' for #getCapacity()!");
                        capacity += handlerCapacity;
                    }
                }
            }
            return capacity;
        });
    }

    @Override
    public boolean supportsInsertion(){
        return this.requester.canSend(EnumChannelType.ENERGY);
    }

    @Override
    public long insert(long amount, TransactionContext transaction){
        StoragePreconditions.notNegative(amount);
        if(!this.requester.canSend(this.channel.type))
            return 0;
        return this.runSafe(0L, () -> {
            long leftOver = amount;
            for(TesseractReference reference : this.channel.receivingTesseracts){
                if(reference != this.requester && reference.canBeAccessed()){
                    TesseractBlockEntity entity = reference.getTesseract();
                    for(EnergyStorage handler : entity.getSurroundingEnergyCapabilities()){
                        if(handler.supportsInsertion()){
                            long inserted = handler.insert(leftOver, transaction);
                            if(inserted < 0)
                                throw new IllegalStateException("Energy storage of class '" + handler.getClass().getName() + "' obtained from block entity '" + entity.getClass().getName() + "' returned '" + inserted + "' for #insert()!");
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
        return this.requester.canReceive(EnumChannelType.ENERGY);
    }

    @Override
    public long extract(long amount, TransactionContext transaction){
        StoragePreconditions.notNegative(amount);
        if(!this.requester.canReceive(this.channel.type))
            return 0;
        return this.runSafe(0L, () -> {
            long leftOver = amount;
            for(TesseractReference reference : this.channel.sendingTesseracts){
                if(reference != this.requester && reference.canBeAccessed()){
                    TesseractBlockEntity entity = reference.getTesseract();
                    for(EnergyStorage handler : entity.getSurroundingEnergyCapabilities()){
                        if(handler.supportsExtraction()){
                            long extracted = handler.extract(leftOver, transaction);
                            if(extracted < 0)
                                throw new IllegalStateException("Energy storage of class '" + handler.getClass().getName() + "' obtained from block entity '" + entity.getClass().getName() + "' returned '" + extracted + "' for #extract()!");
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
