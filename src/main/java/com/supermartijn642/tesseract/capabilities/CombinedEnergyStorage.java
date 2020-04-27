package com.supermartijn642.tesseract.capabilities;

import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.TesseractTile;
import com.supermartijn642.tesseract.manager.TesseractLocation;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.List;

/**
 * Created 3/20/2020 by SuperMartijn642
 */
public class CombinedEnergyStorage implements IEnergyStorage {

    private final List<TesseractLocation> tesseracts;
    private final TesseractTile requester;

    public CombinedEnergyStorage(List<TesseractLocation> tesseracts, TesseractTile requester){
        this.tesseracts = tesseracts;
        this.requester = requester;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate){
        if(!this.requester.canSend(EnumChannelType.ENERGY) || maxReceive <= 0)
            return 0;
        int amount = maxReceive;
        for(TesseractLocation location : this.tesseracts){
            if(location.isValid() && location.getTesseract() != this.requester && location.canReceive(EnumChannelType.ENERGY)){
                for(IEnergyStorage storage : location.getTesseract().getSurroundingCapabilities(CapabilityEnergy.ENERGY)){
                    if(!storage.canReceive())
                        continue;
                    int amount2 = storage.receiveEnergy(amount, simulate);
                    if(amount2 > 0)
                        amount -= amount2;
                    if(amount <= 0)
                        break;
                }
            }
        }
        return maxReceive - amount;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate){
        if(!this.requester.canReceive(EnumChannelType.ENERGY) || maxExtract <= 0)
            return 0;
        int amount = maxExtract;
        for(TesseractLocation location : this.tesseracts){
            if(location.isValid() && location.getTesseract() != this.requester && location.canSend(EnumChannelType.ENERGY)){
                for(IEnergyStorage storage : location.getTesseract().getSurroundingCapabilities(CapabilityEnergy.ENERGY)){
                    if(!storage.canExtract())
                        continue;
                    int amount2 = storage.extractEnergy(amount, simulate);
                    if(amount2 > 0)
                        amount -= amount2;
                    if(amount <= 0)
                        break;
                }
            }
        }
        return maxExtract - amount;
    }

    @Override
    public int getEnergyStored(){
        int amount = 0;
        for(TesseractLocation location : this.tesseracts){
            if(location.isValid() && location.getTesseract() != this.requester){
                for(IEnergyStorage storage : location.getTesseract().getSurroundingCapabilities(CapabilityEnergy.ENERGY)){
                    amount += storage.getEnergyStored();
                }
            }
        }
        return amount;
    }

    @Override
    public int getMaxEnergyStored(){
        int amount = 0;
        for(TesseractLocation location : this.tesseracts){
            if(location.isValid() && location.getTesseract() != this.requester){
                for(IEnergyStorage storage : location.getTesseract().getSurroundingCapabilities(CapabilityEnergy.ENERGY)){
                    amount += storage.getMaxEnergyStored();
                }
            }
        }
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
}
