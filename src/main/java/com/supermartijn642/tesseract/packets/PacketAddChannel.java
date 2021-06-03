package com.supermartijn642.tesseract.packets;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractChannelManager;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;

/**
 * Created 4/23/2020 by SuperMartijn642
 */
public class PacketAddChannel implements BasePacket {

    private Channel channel;

    public PacketAddChannel(Channel channel){
        this.channel = channel;
    }

    public PacketAddChannel(){
    }

    @Override
    public void write(PacketBuffer buffer){
        buffer.writeCompoundTag(this.channel.writeClientChannel());
    }

    @Override
    public void read(PacketBuffer buffer){
        try{
            this.channel = Channel.readClientChannel(buffer.readCompoundTag());
        }catch(IOException ignore){}
    }

    @Override
    public boolean verify(PacketContext context){
        return this.channel != null;
    }

    @Override
    public void handle(PacketContext buffer){
        TesseractChannelManager.CLIENT.addChannel(this.channel);
        TesseractChannelManager.CLIENT.sortChannels(ClientUtils.getPlayer(), this.channel.type);
    }
}
