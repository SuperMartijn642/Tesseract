package com.supermartijn642.tesseract.capabilities;

import com.supermartijn642.tesseract.TesseractBlockEntity;
import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractReference;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.function.Supplier;

/**
 * Created 3/20/2020 by SuperMartijn642
 */
public class CombinedEnergyStorage implements EnergyHandler {

    private final Channel channel;
    private final TesseractReference requester;

    public CombinedEnergyStorage(Channel channel, TesseractReference requester){
        this.channel = channel;
        this.requester = requester;
    }

    @Override
    public long getAmountAsLong(){
        return this.runSafe(0L, () -> {
            long amount = 0;
            for(TesseractReference reference : this.channel.tesseracts){
                if(reference != this.requester && reference.canBeAccessed()){
                    TesseractBlockEntity entity = reference.getTesseract();
                    for(EnergyHandler handler : entity.getSurroundingEnergyCapabilities()){
                        long handlerAmount = handler.getAmountAsLong();
                        if(handlerAmount < 0)
                            throw new IllegalStateException("Energy handler of class '" + handler.getClass().getName() + "' obtained from block entity '" + entity.getClass().getName() + "' returned '" + handlerAmount + "' for #getAmountAsLong()!");
                        amount += handlerAmount;
                    }
                }
            }
            return Math.max(0, amount);
        });
    }

    @Override
    public long getCapacityAsLong(){
        return this.runSafe(0L, () -> {
            long capacity = 0;
            for(TesseractReference reference : this.channel.tesseracts){
                if(reference != this.requester && reference.canBeAccessed()){
                    TesseractBlockEntity entity = reference.getTesseract();
                    for(EnergyHandler handler : entity.getSurroundingEnergyCapabilities()){
                        long handlerCapacity = handler.getAmountAsLong();
                        if(handlerCapacity < 0)
                            throw new IllegalStateException("Energy handler of class '" + handler.getClass().getName() + "' obtained from block entity '" + entity.getClass().getName() + "' returned '" + handlerCapacity + "' for #getCapacityAsLong()!");
                        capacity += handlerCapacity;
                    }
                }
            }
            return capacity;
        });
    }

    @Override
    public int insert(int amount, TransactionContext transaction){
        TransferPreconditions.checkNonNegative(amount);
        if(!this.requester.canSend(this.channel.type))
            return 0;
        return this.runSafe(0, () -> {
            int leftOver = amount;
            for(TesseractReference reference : this.channel.receivingTesseracts){
                if(reference != this.requester && reference.canBeAccessed()){
                    TesseractBlockEntity entity = reference.getTesseract();
                    for(EnergyHandler handler : entity.getSurroundingEnergyCapabilities()){
                        int inserted = handler.insert(leftOver, transaction);
                        if(inserted < 0)
                            throw new IllegalStateException("Energy handler of class '" + handler.getClass().getName() + "' obtained from block entity '" + entity.getClass().getName() + "' returned '" + inserted + "' for #insert()!");
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
    public int extract(int amount, TransactionContext transaction){
        TransferPreconditions.checkNonNegative(amount);
        if(!this.requester.canReceive(this.channel.type))
            return 0;
        return this.runSafe(0, () -> {
            int leftOver = amount;
            for(TesseractReference reference : this.channel.sendingTesseracts){
                if(reference != this.requester && reference.canBeAccessed()){
                    TesseractBlockEntity entity = reference.getTesseract();
                    for(EnergyHandler handler : entity.getSurroundingEnergyCapabilities()){
                        int extracted = handler.extract(leftOver, transaction);
                        if(extracted < 0)
                            throw new IllegalStateException("Energy handler of class '" + handler.getClass().getName() + "' obtained from block entity '" + entity.getClass().getName() + "' returned '" + extracted + "' for #extract()!");
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
