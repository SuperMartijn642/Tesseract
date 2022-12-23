package com.supermartijn642.tesseract.packets;

import com.supermartijn642.core.network.BlockEntityBasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.tesseract.TesseractBlockEntity;
import net.minecraft.core.BlockPos;

/**
 * Created 7/5/2020 by SuperMartijn642
 */
public class PacketScreenCycleRedstoneState extends BlockEntityBasePacket<TesseractBlockEntity> {

    public PacketScreenCycleRedstoneState(BlockPos pos){
        super(pos);
    }

    public PacketScreenCycleRedstoneState(){
    }

    @Override
    protected void handle(TesseractBlockEntity entity, PacketContext context){
        entity.cycleRedstoneState();
    }
}
