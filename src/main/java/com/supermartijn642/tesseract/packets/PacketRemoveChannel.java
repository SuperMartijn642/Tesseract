package com.supermartijn642.tesseract.packets;

import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.manager.TesseractChannelManager;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Created 12/16/2020 by SuperMartijn642
 */
public class PacketRemoveChannel {

    private EnumChannelType type;
    private int id;

    public PacketRemoveChannel(EnumChannelType type, int id){
        this.type = type;
        this.id = id;
    }

    public void encode(PacketBuffer buffer){
        buffer.writeInt(this.type.getIndex());
        buffer.writeInt(this.id);
    }

    public static PacketRemoveChannel decode(PacketBuffer buffer){
        return new PacketRemoveChannel(EnumChannelType.byIndex(buffer.readInt()), buffer.readInt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx){
        ctx.get().setPacketHandled(true);
        ctx.get().enqueueWork(() -> TesseractChannelManager.CLIENT.removeChannel(this.type, this.id));
    }
}
