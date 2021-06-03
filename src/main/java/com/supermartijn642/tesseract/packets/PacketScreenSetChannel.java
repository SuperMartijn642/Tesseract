package com.supermartijn642.tesseract.packets;

import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.core.network.TileEntityBasePacket;
import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.TesseractTile;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

/**
 * Created 4/23/2020 by SuperMartijn642
 */
public class PacketScreenSetChannel extends TileEntityBasePacket<TesseractTile> {

    private EnumChannelType type;
    private int id;

    public PacketScreenSetChannel(EnumChannelType type, int id, BlockPos pos){
        super(pos);
        this.type = type;
        this.id = id;
    }

    public PacketScreenSetChannel(){
    }

    @Override
    public void write(PacketBuffer buffer){
        super.write(buffer);
        buffer.writeInt(this.type.getIndex());
        buffer.writeInt(this.id);
    }

    @Override
    public void read(PacketBuffer buffer){
        super.read(buffer);
        this.type = EnumChannelType.byIndex(buffer.readInt());
        this.id = buffer.readInt();
    }

    @Override
    protected void handle(TesseractTile tile, PacketContext context){
        tile.setChannel(this.type, this.id);
    }
}
