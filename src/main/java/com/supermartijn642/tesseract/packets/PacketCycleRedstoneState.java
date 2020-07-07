package com.supermartijn642.tesseract.packets;

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
public class PacketCycleRedstoneState implements IMessage, IMessageHandler<PacketCycleRedstoneState,IMessage> {

    private BlockPos pos;

    public PacketCycleRedstoneState(BlockPos pos){
        this.pos = pos;
    }

    public PacketCycleRedstoneState(){
    }

    @Override
    public void fromBytes(ByteBuf buf){
        this.pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
    }

    @Override
    public void toBytes(ByteBuf buffer){
        buffer.writeInt(this.pos.getX());
        buffer.writeInt(this.pos.getY());
        buffer.writeInt(this.pos.getZ());
    }

    @Override
    public IMessage onMessage(PacketCycleRedstoneState message, MessageContext ctx){
        World world = ctx.getServerHandler().player.world;
        if(world == null)
            return null;
        ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
            TileEntity tile = world.getTileEntity(message.pos);
            if(tile instanceof TesseractTile)
                ((TesseractTile)tile).cycleRedstoneState();
        });
        return null;
    }
}
