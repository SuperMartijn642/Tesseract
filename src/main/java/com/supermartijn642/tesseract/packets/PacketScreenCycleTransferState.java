package com.supermartijn642.tesseract.packets;

import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.core.network.TileEntityBasePacket;
import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.TesseractTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

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
    public void write(FriendlyByteBuf buffer){
        super.write(buffer);
        buffer.writeInt(this.type.getIndex());
    }

    @Override
    public void read(FriendlyByteBuf buffer){
        super.read(buffer);
        this.type = EnumChannelType.byIndex(buffer.readInt());
    }

    @Override
    protected void handle(TesseractTile tile, PacketContext context){
        tile.cycleTransferState(this.type);
    }
}
