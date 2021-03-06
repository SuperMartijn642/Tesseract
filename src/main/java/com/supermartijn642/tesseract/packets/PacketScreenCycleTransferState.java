package com.supermartijn642.tesseract.packets;

import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.core.network.TileEntityBasePacket;
import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.TesseractTile;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

/**
 * Created 7/5/2020 by SuperMartijn642
 */
public class PacketScreenCycleTransferState extends TileEntityBasePacket<TesseractTile> {

    private EnumChannelType type;

    public PacketScreenCycleTransferState(BlockPos pos, EnumChannelType type){
        super(pos);
        this.type = type;
    }

    public PacketScreenCycleTransferState(){
    }

    @Override
    public void write(PacketBuffer buffer){
        super.write(buffer);
        buffer.writeInt(this.type.getIndex());
    }

    @Override
    public void read(PacketBuffer buffer){
        super.read(buffer);
        this.type = EnumChannelType.byIndex(buffer.readInt());
    }

    @Override
    protected void handle(TesseractTile tile, PacketContext context){
        tile.cycleTransferState(this.type);
    }
}
