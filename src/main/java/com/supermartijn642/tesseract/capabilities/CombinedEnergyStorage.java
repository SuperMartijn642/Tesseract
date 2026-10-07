package com.supermartijn642.tesseract.capabilities;

import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.TesseractBlockEntity;
import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractReference;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.function.Supplier;

/**
 * Created 3/20/2020 by SuperMartijn642
 */
public class CombinedEnergyStorage implements IEnergyStorage {

    private final Channel channel;
    private final TesseractReference requester;

    public CombinedEnergyStorage(Channel channel, TesseractReference requester){
        this.channel = channel;
        this.requester = requester;
    }

    @Override
    public int getEnergyStored(){
        return this.runSafe(0, () -> {
            int amount = 0;
            for(TesseractReference reference : this.channel.tesseracts){
                if(reference != this.requester && reference.canBeAccessed()){
                    TesseractBlockEntity entity = reference.getTesseract();
                    for(IEnergyStorage handler : entity.getSurroundingCapabilities(ForgeCapabilities.ENERGY)){
                        int handlerAmount = handler.getEnergyStored();
                        if(handlerAmount < 0)
                            throw new IllegalStateException("Energy storage of class '" + handler.getClass().getName() + "' obtained from block entity '" + entity.getClass().getName() + "' returned '" + handlerAmount + "' for #getEnergyStored()!");
                        amount += handlerAmount;
                    }
                }
            }
            return amount;
        });
    }

    @Override
    public int getMaxEnergyStored(){
        return this.runSafe(0, () -> {
            int capacity = 0;
            for(TesseractReference reference : this.channel.tesseracts){
                if(reference != this.requester && reference.canBeAccessed()){
                    TesseractBlockEntity entity = reference.getTesseract();
                    for(IEnergyStorage handler : entity.getSurroundingCapabilities(ForgeCapabilities.ENERGY)){
                        int handlerCapacity = handler.getMaxEnergyStored();
                        if(handlerCapacity < 0)
                            throw new IllegalStateException("Energy storage of class '" + handler.getClass().getName() + "' obtained from block entity '" + entity.getClass().getName() + "' returned '" + handlerCapacity + "' for #getMaxEnergyStored()!");
                        capacity += handlerCapacity;
                    }
                }
            }
            return capacity;
        });
    }

    @Override
    public boolean canReceive(){
        return this.requester.canSend(EnumChannelType.ENERGY);
    }

    @Override
    public int receiveEnergy(int amount, boolean simulate){
        if(amount < 0)
            throw new IllegalArgumentException("Insertion amount must not be negative!");
        if(!this.requester.canSend(this.channel.type))
            return 0;
        return this.runSafe(0, () -> {
            int leftOver = amount;
            for(TesseractReference reference : this.channel.receivingTesseracts){
                if(reference != this.requester && reference.canBeAccessed()){
                    TesseractBlockEntity entity = reference.getTesseract();
                    for(IEnergyStorage handler : entity.getSurroundingCapabilities(ForgeCapabilities.ENERGY)){
                        if(!handler.canReceive())
                            continue;
                        int inserted = handler.receiveEnergy(leftOver, simulate);
                        if(inserted < 0)
                            throw new IllegalStateException("Energy storage of class '" + handler.getClass().getName() + "' obtained from block entity '" + entity.getClass().getName() + "' returned '" + inserted + "' for #receiveEnergy()!");
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
    public boolean canExtract(){
        return this.requester.canReceive(EnumChannelType.ENERGY);
    }

    @Override
    public int extractEnergy(int amount, boolean simulate){
        if(amount < 0)
            throw new IllegalArgumentException("Extraction amount must not be negative!");
        if(!this.requester.canReceive(this.channel.type))
            return 0;
        return this.runSafe(0, () -> {
            int leftOver = amount;
            for(TesseractReference reference : this.channel.sendingTesseracts){
                if(reference != this.requester && reference.canBeAccessed()){
                    TesseractBlockEntity entity = reference.getTesseract();
                    for(IEnergyStorage handler : entity.getSurroundingCapabilities(ForgeCapabilities.ENERGY)){
                        if(!handler.canExtract())
                            continue;
                        int extracted = handler.extractEnergy(leftOver, simulate);
                        if(extracted < 0)
                            throw new IllegalStateException("Energy storage of class '" + handler.getClass().getName() + "' obtained from block entity '" + entity.getClass().getName() + "' returned '" + extracted + "' for #extractEnergy()!");
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
