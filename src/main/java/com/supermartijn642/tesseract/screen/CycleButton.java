package com.supermartijn642.tesseract.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

/**
 * Created 7/5/2020 by SuperMartijn642
 */
public abstract class CycleButton extends AbstractButton {

    private static final ResourceLocation TEXTURE = new ResourceLocation("tesseract", "textures/gui/buttons.png");

    private int textureX;

    public CycleButton(int x, int y, int textureX){
        super(x, y, 20, 20, "");
        this.textureX = textureX;
    }

    protected abstract int getCycleIndex();

    @Override
    public void renderButton(int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_){
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getTextureManager().bindTexture(TEXTURE);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        this.drawTexture(this.x, this.y, this.textureX + this.getCycleIndex() * 20, (this.active ? this.isHovered ? 1 : 0 : 2) * 20, this.width, this.height);
        this.renderBg(minecraft, p_renderButton_1_, p_renderButton_2_);
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
