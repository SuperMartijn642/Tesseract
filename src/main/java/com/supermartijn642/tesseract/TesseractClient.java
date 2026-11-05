package com.supermartijn642.tesseract;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.gui.WidgetScreen;
import com.supermartijn642.core.registry.ClientRegistrationHandler;
import com.supermartijn642.tesseract.manager.TesseractChannelManager;
import com.supermartijn642.tesseract.manager.TesseractTracker;
import com.supermartijn642.tesseract.screen.TesseractScreen;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

/**
 * Created 3/19/2020 by SuperMartijn642
 */
public class TesseractClient {

    public static void register(){
        ClientRegistrationHandler handler = ClientRegistrationHandler.get("tesseract");
        // Register the block renderer
        handler.registerCustomBlockEntityRenderer(() -> Tesseract.tesseract_tile, TesseractBlockEntityRenderer::new);

        MinecraftForge.EVENT_BUS.register(TesseractClient.class);
    }

    @SubscribeEvent
    public static void onDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent e){
        // This event is fired on the network thread
        ClientUtils.getMinecraft().addScheduledTask(() -> {
            TesseractTracker.CLIENT.clear();
            TesseractChannelManager.CLIENT.clear();
        });
    }

    public static void openScreen(BlockPos pos){
        ClientUtils.displayScreen(WidgetScreen.of(new TesseractScreen(ClientUtils.getWorld(), pos)));
    }
}
