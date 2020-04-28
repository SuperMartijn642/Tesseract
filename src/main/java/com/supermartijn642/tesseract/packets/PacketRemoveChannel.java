package com.supermartijn642.tesseract.packets;

import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.Tesseract;
import com.supermartijn642.tesseract.manager.TesseractChannelManager;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

/**
 * Created 4/23/2020 by SuperMartijn642
 */
public class PacketRemoveChannel {

    private EnumChannelType type;
    private int id;

    public PacketRemoveChannel(EnumChannelType type, int id){
        this.type = type;
        this.id = id;
    }

    public void encode(PacketBuffer buffer){
        buffer.writeString(this.type.name());
        buffer.writeInt(this.id);
    }

    public static PacketRemoveChannel decode(PacketBuffer buffer){
        return new PacketRemoveChannel(EnumChannelType.valueOf(buffer.readString(32767)), buffer.readInt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx){
        ctx.get().setPacketHandled(true);
        TesseractChannelManager.SERVER.removeChannel(this.type, this.id);
        Tesseract.CHANNEL.send(PacketDistributor.ALL.noArg(), new PacketSendChannels(this.type));
    }
}
