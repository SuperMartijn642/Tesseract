package com.supermartijn642.tesseract.capabilities;

import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.TesseractTile;
import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractReference;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * Created 3/20/2020 by SuperMartijn642
 */
public class CombinedEnergyStorage implements IEnergyStorage {

    private final Channel channel;
    private final TesseractTile requester;

    public CombinedEnergyStorage(Channel channel, TesseractTile requester){
        this.channel = channel;
        this.requester = requester;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate){
        if(this.pushRecurrentCall())
            return 0;

        if(!this.requester.canSend(EnumChannelType.ENERGY) || maxReceive <= 0)
            return 0;

        int amount = maxReceive;

        loop:
        for(TesseractReference location : this.channel.receivingTesseracts){
            if(location.isValid() && location.canReceive(EnumChannelType.ENERGY)){
                TesseractTile tile = location.getTesseract();
                if(tile != this.requester){
                    for(IEnergyStorage storage : tile.getSurroundingCapabilities(CapabilityEnergy.ENERGY)){
                        if(!storage.canReceive())
                            continue;
                        int amount2 = storage.receiveEnergy(amount, simulate);
                        if(amount2 > 0)
                            amount -= amount2;
                        if(amount <= 0)
                            break loop;
                    }
                }
            }
        }

        this.popRecurrentCall();

        return maxReceive - amount;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate){
        if(this.pushRecurrentCall())
            return 0;

        if(!this.requester.canReceive(EnumChannelType.ENERGY) || maxExtract <= 0)
            return 0;

        int amount = maxExtract;

        loop:
        for(TesseractReference location : this.channel.sendingTesseracts){
            if(location.isValid() && location.canSend(EnumChannelType.ENERGY)){
                TesseractTile tile = location.getTesseract();
                if(tile != this.requester){
                    for(IEnergyStorage storage : tile.getSurroundingCapabilities(CapabilityEnergy.ENERGY)){
                        if(!storage.canExtract())
                            continue;
                        int amount2 = storage.extractEnergy(amount, simulate);
                        if(amount2 > 0)
                            amount -= amount2;
                        if(amount <= 0)
                            break loop;
                    }
                }
            }
        }

        this.popRecurrentCall();

        return maxExtract - amount;
    }

    @Override
    public int getEnergyStored(){
        if(this.pushRecurrentCall())
            return 0;

        int amount = 0;
        for(TesseractReference location : this.channel.tesseracts){
            if(location.isValid()){
                TesseractTile tile = location.getTesseract();
                if(tile != this.requester){
                    for(IEnergyStorage storage : tile.getSurroundingCapabilities(CapabilityEnergy.ENERGY))
                        amount += storage.getEnergyStored();
                }
            }
        }

        this.popRecurrentCall();

        return amount;
    }

    @Override
    public int getMaxEnergyStored(){
        if(this.pushRecurrentCall())
            return 0;

        int amount = 0;
        for(TesseractReference location : this.channel.tesseracts){
            if(location.isValid()){
                TesseractTile tile = location.getTesseract();
                if(tile != this.requester){
                    for(IEnergyStorage storage : tile.getSurroundingCapabilities(CapabilityEnergy.ENERGY))
                        amount += storage.getMaxEnergyStored();
                }
            }
        }

        this.popRecurrentCall();

        return amount;
    }

    @Override
    public boolean canExtract(){
        return this.requester.canReceive(EnumChannelType.ENERGY);
    }

    @Override
    public boolean canReceive(){
        return this.requester.canSend(EnumChannelType.ENERGY);
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
