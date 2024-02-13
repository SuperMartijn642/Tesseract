package com.supermartijn642.tesseract.packets;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractChannelManager;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Created 12/16/2020 by SuperMartijn642
 */
public class PacketRemoveChannel implements BasePacket {

    private EnumChannelType type;
    private int id;

    public PacketRemoveChannel(Channel channel){
        this.type = channel.type;
        this.id = channel.id;
    }

    public PacketRemoveChannel(){
    }

    @Override
    public void write(FriendlyByteBuf buffer){
        buffer.writeInt(this.type.getIndex());
        buffer.writeInt(this.id);
    }

    @Override
    public void read(FriendlyByteBuf buffer){
        this.type = EnumChannelType.byIndex(buffer.readInt());
        this.id = buffer.readInt();
    }

    @Override
    public void handle(PacketContext context){
        TesseractChannelManager.CLIENT.removeChannel(this.type, this.id, ClientUtils.getPlayer());
    }
}
