package com.supermartijn642.tesseract;

import com.supermartijn642.tesseract.manager.TesseractChannelManager;
import com.supermartijn642.tesseract.packets.PacketAddChannel;
import com.supermartijn642.tesseract.packets.PacketRemoveChannel;
import com.supermartijn642.tesseract.packets.PacketSendChannels;
import com.supermartijn642.tesseract.packets.PacketSetChannel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Created 3/19/2020 by SuperMartijn642
 */
@Mod(modid = Tesseract.MODID, name = Tesseract.NAME, version = Tesseract.VERSION, acceptedMinecraftVersions = Tesseract.MC_VERSIONS)
public class Tesseract {

    public static Tesseract instance;

    public static final String MODID = "tesseract";
    public static final String NAME = "Tesseract";
    public static final String MC_VERSIONS = "[1.12.2]";
    public static final String VERSION = "1.0.1";

    public static SimpleNetworkWrapper channel;

    @GameRegistry.ObjectHolder(MODID + ":tesseract")
    public static BlockTesseract tesseract;

    public Tesseract(){
        instance = this;
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e){
        channel = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
        channel.registerMessage(PacketSendChannels.class, PacketSendChannels.class, 0, Side.CLIENT);
        channel.registerMessage(PacketAddChannel.class, PacketAddChannel.class, 1, Side.SERVER);
        channel.registerMessage(PacketRemoveChannel.class, PacketRemoveChannel.class, 2, Side.SERVER);
        channel.registerMessage(PacketSetChannel.class, PacketSetChannel.class, 3, Side.SERVER);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e){
        MinecraftForge.EVENT_BUS.register(TesseractChannelManager.class);
    }

}
