package com.supermartijn642.tesseract.packets;

import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.TesseractTile;
import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.nio.charset.StandardCharsets;

/**
 * Created 4/23/2020 by SuperMartijn642
 */
public class PacketScreenSetChannel implements IMessage, IMessageHandler<PacketScreenSetChannel,IMessage> {

    private EnumChannelType type;
    private int id;
    private BlockPos pos;

    public PacketScreenSetChannel(EnumChannelType type, int id, BlockPos pos){
        this.type = type;
        this.id = id;
        this.pos = pos;
    }

    public PacketScreenSetChannel(){
    }

    @Override
    public void fromBytes(ByteBuf buf){
        byte[] bytes = new byte[buf.readInt()];
        buf.readBytes(bytes);
        this.type = EnumChannelType.valueOf(new String(bytes, StandardCharsets.UTF_16));
        this.id = buf.readInt();
        this.pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
    }

    @Override
    public void toBytes(ByteBuf buf){
        byte[] bytes = this.type.name().getBytes(StandardCharsets.UTF_16);
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
        buf.writeInt(this.id);
        buf.writeInt(this.pos.getX());
        buf.writeInt(this.pos.getY());
        buf.writeInt(this.pos.getZ());
    }

    @Override
    public IMessage onMessage(PacketScreenSetChannel message, MessageContext ctx){
        if(message.type == null || message.id < -1)
            return null;
        World world = ctx.getServerHandler().player.world;
        if(world == null)
            return null;
        ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
            TileEntity tile = world.getTileEntity(message.pos);
            if(tile instanceof TesseractTile)
                ((TesseractTile)tile).setChannel(message.type, message.id);
        });
        return null;
    }
}
