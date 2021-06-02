package com.supermartijn642.tesseract.packets;

import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.manager.TesseractChannelManager;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Created 4/23/2020 by SuperMartijn642
 */
public class PacketScreenRemoveChannel implements BasePacket {

    private EnumChannelType type;
    private int id;

    public PacketScreenRemoveChannel(EnumChannelType type, int id){
        this.type = type;
        this.id = id;
    }

    public PacketScreenRemoveChannel(){
    }

    public void handle(Supplier<NetworkEvent.Context> ctx){
        ctx.get().setPacketHandled(true);
        if(this.type != null && this.id >= 0)
            ctx.get().enqueueWork(() -> {
                TesseractChannelManager.SERVER.removeChannel(this.type, this.id);
            });
    }

    @Override
    public void write(PacketBuffer buffer){
        buffer.writeInt(this.type.getIndex());
        buffer.writeInt(this.id);
    }

    @Override
    public void read(PacketBuffer buffer){
        this.type = EnumChannelType.byIndex(buffer.readInt());
        this.id = buffer.readInt();
    }

    @Override
    public boolean verify(PacketContext context){
        return this.type != null && this.id >= 0;
    }

    @Override
    public void handle(PacketContext context){
        TesseractChannelManager.SERVER.removeChannel(this.type, this.id);
    }
}
