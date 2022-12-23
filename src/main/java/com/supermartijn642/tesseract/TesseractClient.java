package com.supermartijn642.tesseract;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.gui.WidgetScreen;
import com.supermartijn642.core.registry.ClientRegistrationHandler;
import com.supermartijn642.tesseract.screen.TesseractScreen;
import net.minecraft.util.math.BlockPos;

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
        ClientUtils.displayScreen(new WidgetScreen<TesseractScreen>(new TesseractScreen(ClientUtils.getWorld(), pos)) {
            @Override
            public void render(MatrixStack poseStack, int mouseX, int mouseY, float partialTicks){
                this.widget.offsetLeft = (this.width - this.widget.width()) / 2;
                this.widget.offsetTop = (this.height - this.widget.height()) / 2;
                super.render(poseStack, mouseX, mouseY, partialTicks);
            }
        });
    }
}
