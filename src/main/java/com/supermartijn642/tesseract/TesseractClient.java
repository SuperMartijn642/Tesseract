package com.supermartijn642.tesseract;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.gui.WidgetScreen;
import com.supermartijn642.core.registry.ClientRegistrationHandler;
import com.supermartijn642.tesseract.screen.TesseractScreen;
import net.minecraft.core.BlockPos;

/**
 * Created 3/19/2020 by SuperMartijn642
 */
public class TesseractClient {

    public static void register(){
        ClientRegistrationHandler handler = ClientRegistrationHandler.get("tesseract");
        // Register the block renderer
        handler.registerCustomBlockEntityRenderer(() -> Tesseract.tesseract_tile, TesseractBlockEntityRenderer::new);
    }

    public static void openScreen(BlockPos pos){
        ClientUtils.displayScreen(WidgetScreen.of(new TesseractScreen(ClientUtils.getWorld(), pos)));
    }
}
