package com.supermartijn642.tesseract.capabilities;

import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.TesseractBlockEntity;
import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractReference;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import team.reborn.energy.api.EnergyStorage;

/**
 * Created 3/20/2020 by SuperMartijn642
 */
@SuppressWarnings("UnstableApiUsage")
public class CombinedEnergyStorage implements EnergyStorage {

    private final Channel channel;
    private final TesseractBlockEntity requester;

    public CombinedEnergyStorage(Channel channel, TesseractBlockEntity requester){
        this.channel = channel;
        this.requester = requester;
    }

    @Override
    public boolean supportsInsertion(){
        return this.requester.canSend(EnumChannelType.ENERGY);
    }

    @Override
    public long insert(long maxAmount, TransactionContext transaction){
        if(this.pushRecurrentCall())
            return 0;

        if(!this.requester.canSend(EnumChannelType.ENERGY) || maxAmount <= 0){
            this.popRecurrentCall();
            return 0;
        }

        long leftOver = maxAmount;
        loop:
        for(TesseractReference reference : this.channel.receivingTesseracts){
            if(reference.canBeAccessed()){
                TesseractBlockEntity entity = reference.getTesseract();
                if(entity != this.requester){
                    for(EnergyStorage handler : entity.getSurroundingCapabilities(EnergyStorage.SIDED)){
                        if(handler.supportsInsertion()){
                            leftOver -= handler.insert(leftOver, transaction);
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
        return this.requester.canReceive(EnumChannelType.ENERGY);
    }

    @Override
    public long extract(long maxAmount, TransactionContext transaction){
        if(this.pushRecurrentCall())
            return 0;

        if(!this.requester.canReceive(EnumChannelType.ENERGY) || maxAmount <= 0){
            this.popRecurrentCall();
            return 0;
        }

        long leftOver = maxAmount;
        loop:
        for(TesseractReference reference : this.channel.receivingTesseracts){
            if(reference.canBeAccessed()){
                TesseractBlockEntity entity = reference.getTesseract();
                if(entity != this.requester){
                    for(EnergyStorage handler : entity.getSurroundingCapabilities(EnergyStorage.SIDED)){
                        if(handler.supportsExtraction()){
                            leftOver -= handler.extract(leftOver, transaction);
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
    public long getAmount(){
        if(this.pushRecurrentCall())
            return 0;

        long energy = 0;
        for(TesseractReference reference : this.channel.receivingTesseracts){
            if(reference.canBeAccessed()){
                TesseractBlockEntity entity = reference.getTesseract();
                if(entity != this.requester){
                    for(EnergyStorage handler : entity.getSurroundingCapabilities(EnergyStorage.SIDED))
                        energy += handler.getAmount();
                }
            }
        }

        this.popRecurrentCall();

        return energy;
    }

    @Override
    public long getCapacity(){
        if(this.pushRecurrentCall())
            return 0;

        long capacity = 0;
        for(TesseractReference reference : this.channel.receivingTesseracts){
            if(reference.canBeAccessed()){
                TesseractBlockEntity entity = reference.getTesseract();
                if(entity != this.requester){
                    for(EnergyStorage handler : entity.getSurroundingCapabilities(EnergyStorage.SIDED))
                        capacity += handler.getCapacity();
                }
            }
        }

        this.popRecurrentCall();

        return capacity;
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
