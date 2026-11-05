package com.supermartijn642.tesseract;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.gui.WidgetScreen;
import com.supermartijn642.core.registry.ClientRegistrationHandler;
import com.supermartijn642.tesseract.manager.TesseractChannelManager;
import com.supermartijn642.tesseract.manager.TesseractTracker;
import com.supermartijn642.tesseract.screen.TesseractScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.core.BlockPos;

/**
 * Created 3/19/2020 by SuperMartijn642
 */
public class TesseractClient implements ClientModInitializer {

    @Override
    public void onInitializeClient(){
        register();
    }

    public static void register(){
        ClientRegistrationHandler handler = ClientRegistrationHandler.get("tesseract");
        // Register the block renderer
        handler.registerCustomBlockEntityRenderer(() -> Tesseract.tesseract_tile, TesseractBlockEntityRenderer::new);

        ClientPlayConnectionEvents.DISCONNECT.register((listener, client) -> onDisconnect());
    }

    private static void onDisconnect(){
        TesseractTracker.CLIENT.clear();
        TesseractChannelManager.CLIENT.clear();
    }

    public static void openScreen(BlockPos pos){
        ClientUtils.displayScreen(WidgetScreen.of(new TesseractScreen(ClientUtils.getWorld(), pos)));
    }
}
