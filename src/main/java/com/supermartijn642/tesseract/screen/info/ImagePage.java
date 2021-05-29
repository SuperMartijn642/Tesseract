package com.supermartijn642.tesseract.screen.info;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.supermartijn642.core.gui.ScreenUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

/**
 * Created 4/18/2021 by SuperMartijn642
 */
public class ImagePage extends Page {

    private final int imageWidth, imageHeight;
    private final float scale;
    private final ResourceLocation image;

    public ImagePage(int index, ITextComponent title, ITextComponent text, int imageWidth, int imageHeight, ResourceLocation image){
        super(index, title, text);
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.scale = (float)InfoScreen.WIDTH / imageWidth;
        this.image = image;
    }

    @Override
    public int getTopHeight(){
        return this.imageHeight;
    }

    @Override
    public void renderTop(MatrixStack matrixStack, int mouseX, int mouseY){
        ScreenUtils.bindTexture(this.image);
        matrixStack.scale(this.scale, this.scale, 1);
        ScreenUtils.drawTexture(matrixStack, 0, 0, this.imageWidth, this.imageHeight);
    }
}
