package com.supermartijn642.tesseract.packets;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractChannelManager;
import net.minecraft.network.PacketBuffer;

/**
 * Created 4/23/2020 by SuperMartijn642
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
    public void write(PacketBuffer buffer){
        buffer.writeVarInt(this.type.getIndex());
        buffer.writeInt(this.id);
    }

    @Override
    public void read(PacketBuffer buffer){
        this.type = EnumChannelType.byIndex(buffer.readVarInt());
        this.id = buffer.readInt();
    }

    @Override
    public void handle(PacketContext context){
        TesseractChannelManager.CLIENT.removeChannel(this.type, this.id, ClientUtils.getPlayer());
    }
}
