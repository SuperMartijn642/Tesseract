package com.supermartijn642.tesseract.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

/**
 * Created 7/5/2020 by SuperMartijn642
 */
public abstract class CycleButton extends GuiButton {

    private static final ResourceLocation TEXTURE = new ResourceLocation("tesseract", "textures/gui/buttons.png");

    private int textureX;

    public CycleButton(int buttonId, int x, int y, int textureX){
        super(buttonId, x, y, 20, 20, "");
        this.textureX = textureX;
    }

    protected abstract void onPress();

    protected abstract int getCycleIndex();

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY, float partialTicks){
        this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
        minecraft.getTextureManager().bindTexture(TEXTURE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        this.drawTexture(this.x, this.y, this.textureX + this.getCycleIndex() * 20, (this.enabled ? this.hovered ? 1 : 0 : 2) * 20, this.width, this.height);
    }

    private void drawTexture(int x, int y, int textureX, int textureY, int width, int height){
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(x, y + height, 0).tex(textureX / 120f, (textureY + height) / 60f).endVertex();
        bufferbuilder.pos(x + width, y + height, 0).tex((textureX + width) / 120f, (textureY + height) / 60f).endVertex();
        bufferbuilder.pos(x + width, y, 0).tex((textureX + width) / 120f, textureY / 60f).endVertex();
        bufferbuilder.pos(x, y, 0).tex(textureX / 120f, textureY / 60f).endVertex();
        tessellator.draw();
    }
}
