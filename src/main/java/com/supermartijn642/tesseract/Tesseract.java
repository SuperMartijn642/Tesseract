package com.supermartijn642.tesseract;

import com.supermartijn642.core.network.PacketChannel;
import com.supermartijn642.tesseract.manager.TesseractChannelManager;
import com.supermartijn642.tesseract.manager.TesseractTracker;
import com.supermartijn642.tesseract.packets.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Created 3/19/2020 by SuperMartijn642
 */
@Mod(modid = Tesseract.MODID, name = Tesseract.NAME, version = Tesseract.VERSION, acceptedMinecraftVersions = Tesseract.MC_VERSIONS, dependencies = Tesseract.DEPENDENCIES)
public class Tesseract {

    public static Tesseract instance;

    public static final String MODID = "tesseract";
    public static final String NAME = "Tesseract";
    public static final String MC_VERSIONS = "[1.12.2]";
    public static final String VERSION = "1.0.20";
    public static final String DEPENDENCIES = "required-after:forge@[14.23.5.2779,);required-after:supermartijn642corelib@[1.0.7,1.1.0)";

    public static final PacketChannel CHANNEL = PacketChannel.create("tesseract");

    @GameRegistry.ObjectHolder(MODID + ":tesseract")
    public static BlockTesseract tesseract;

    public Tesseract(){
        instance = this;
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e){
        CHANNEL.registerMessage(PacketCompleteChannelsUpdate.class, PacketCompleteChannelsUpdate::new, true);
        CHANNEL.registerMessage(PacketScreenAddChannel.class, PacketScreenAddChannel::new, true);
        CHANNEL.registerMessage(PacketScreenRemoveChannel.class, PacketScreenRemoveChannel::new, true);
        CHANNEL.registerMessage(PacketScreenSetChannel.class, PacketScreenSetChannel::new, true);
        CHANNEL.registerMessage(PacketScreenCycleRedstoneState.class, PacketScreenCycleRedstoneState::new, true);
        CHANNEL.registerMessage(PacketScreenCycleTransferState.class, PacketScreenCycleTransferState::new, true);
        CHANNEL.registerMessage(PacketAddChannel.class, PacketAddChannel::new, true);
        CHANNEL.registerMessage(PacketRemoveChannel.class, PacketRemoveChannel::new, true);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e){
        MinecraftForge.EVENT_BUS.register(TesseractTracker.class);
        MinecraftForge.EVENT_BUS.register(TesseractChannelManager.class);
    }

}
