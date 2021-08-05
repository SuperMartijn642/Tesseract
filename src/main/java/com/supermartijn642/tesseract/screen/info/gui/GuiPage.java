package com.supermartijn642.tesseract.screen.info.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.tesseract.screen.info.InfoScreen;
import com.supermartijn642.tesseract.screen.info.Page;
import net.minecraft.util.ResourceLocation;

/**
 * Created 7/17/2021 by SuperMartijn642
 */
public class GuiPage extends Page {

    private static final ResourceLocation TESSERACT_GUI_TEXTURE = new ResourceLocation("tesseract", "textures/gui/info/gui_background.png");
    private static final ResourceLocation TESSERACT_GUI_TEXTURE_2 = new ResourceLocation("tesseract", "textures/gui/info/gui_background_with_selection.png");

    private static final int GUI_WIDTH = 279, GUI_HEIGHT = 211;
    private static final int TEXT_WIDTH = 100;

    private final int rectX, rectY, rectWidth, rectHeight;
    private final String textKey;
    private final boolean textLeft, useSecondBackground;

    public GuiPage(int rectX, int rectY, int rectWidth, int rectHeight, String textKey, boolean textLeft, boolean useSecondBackground){
        this.rectX = rectX;
        this.rectY = rectY;
        this.rectWidth = rectWidth;
        this.rectHeight = rectHeight;
        this.textKey = textKey;
        this.textLeft = textLeft;
        this.useSecondBackground = useSecondBackground;
    }

    @Override
    public int getWidth(){
        return TEXT_WIDTH + GUI_WIDTH + 10;
    }

    @Override
    public int getHeight(){
        return GUI_HEIGHT;
    }

    @Override
    public void render(MatrixStack matrixStack){
        int textX = this.textLeft ? 0 : GUI_WIDTH + 10;
        int guiX = this.textLeft ? TEXT_WIDTH + 10 : 0;
        InfoScreen.drawHoveringTab(matrixStack, textX, 0, TEXT_WIDTH, 50);
        ScreenUtils.bindTexture(this.useSecondBackground ? TESSERACT_GUI_TEXTURE_2 : TESSERACT_GUI_TEXTURE);
        ScreenUtils.drawTexture(matrixStack, guiX, 0, GUI_WIDTH, GUI_HEIGHT);
        matrixStack.pushPose();
        matrixStack.translate(guiX, 0, 0);
        matrixStack.scale(GUI_WIDTH / 558f, GUI_HEIGHT / 422f, 0);
        ScreenUtils.fillRect(matrixStack, this.rectX, this.rectY, this.rectWidth, 2, 0xffff0000);
        ScreenUtils.fillRect(matrixStack, this.rectX, this.rectY, 2, this.rectHeight, 0xffff0000);
        ScreenUtils.fillRect(matrixStack, this.rectX, this.rectY + this.rectHeight - 2, this.rectWidth, 2, 0xffff0000);
        ScreenUtils.fillRect(matrixStack, this.rectX + this.rectWidth - 2, this.rectY, 2, this.rectHeight, 0xffff0000);
        matrixStack.popPose();
    }
}
