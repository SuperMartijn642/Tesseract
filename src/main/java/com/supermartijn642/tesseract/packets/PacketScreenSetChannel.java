package com.supermartijn642.tesseract.packets;

import com.supermartijn642.core.network.BlockEntityBasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.TesseractBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Created 4/23/2020 by SuperMartijn642
 */
public class PacketScreenSetChannel extends BlockEntityBasePacket<TesseractBlockEntity> {

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
    public void write(FriendlyByteBuf buffer){
        super.write(buffer);
        buffer.writeInt(this.type.getIndex());
        buffer.writeInt(this.id);
    }

    @Override
    public void read(FriendlyByteBuf buffer){
        super.read(buffer);
        this.type = EnumChannelType.byIndex(buffer.readInt());
        this.id = buffer.readInt();
    }

    @Override
    protected void handle(TesseractBlockEntity entity, PacketContext context){
        entity.setChannel(this.type, this.id);
    }
}
