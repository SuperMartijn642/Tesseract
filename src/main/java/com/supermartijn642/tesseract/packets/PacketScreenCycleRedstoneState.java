package com.supermartijn642.tesseract.packets;

import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.core.network.TileEntityBasePacket;
import com.supermartijn642.tesseract.TesseractTile;
import net.minecraft.util.math.BlockPos;

/**
 * Created 7/5/2020 by SuperMartijn642
 */
public class PacketScreenCycleRedstoneState extends TileEntityBasePacket<TesseractTile> {

    public PacketScreenCycleRedstoneState(BlockPos pos){
        super(pos);
    }

    public PacketScreenCycleRedstoneState(){
    }

    @Override
    protected void handle(TesseractTile tile, PacketContext context){
        tile.cycleRedstoneState();
    }
}
