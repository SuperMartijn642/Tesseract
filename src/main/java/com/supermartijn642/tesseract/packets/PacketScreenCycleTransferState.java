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

/**
 * Created 7/5/2020 by SuperMartijn642
 */
public class PacketScreenCycleTransferState implements IMessage, IMessageHandler<PacketScreenCycleTransferState,IMessage> {

    private BlockPos pos;
    private EnumChannelType type;

    public PacketScreenCycleTransferState(BlockPos pos, EnumChannelType type){
        this.pos = pos;
        this.type = type;
    }

    public PacketScreenCycleTransferState(){
    }

    @Override
    public void fromBytes(ByteBuf buf){
        this.pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        byte[] bytes = new byte[buf.readInt()];
        buf.readBytes(bytes);
        this.type = EnumChannelType.valueOf(new String(bytes));
    }

    @Override
    public void toBytes(ByteBuf buffer){
        buffer.writeInt(this.pos.getX());
        buffer.writeInt(this.pos.getY());
        buffer.writeInt(this.pos.getZ());
        byte[] bytes = this.type.name().getBytes();
        buffer.writeInt(bytes.length);
        buffer.writeBytes(bytes);
    }

    @Override
    public IMessage onMessage(PacketScreenCycleTransferState message, MessageContext ctx){
        World world = ctx.getServerHandler().player.world;
        if(world == null)
            return null;
        ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
            TileEntity tile = world.getTileEntity(message.pos);
            if(tile instanceof TesseractTile)
                ((TesseractTile)tile).cycleTransferState(message.type);
        });
        return null;
    }
}
