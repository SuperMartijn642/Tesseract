package com.supermartijn642.tesseract.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

/**
 * Created 7/5/2020 by SuperMartijn642
 */
public abstract class CycleButton extends AbstractButton {

    private static final ResourceLocation TEXTURE = new ResourceLocation("tesseract", "textures/gui/buttons.png");

    private int textureX;

    public CycleButton(int x, int y, int textureX){
        super(x, y, 20, 20, new StringTextComponent(""));
        this.textureX = textureX;
    }

    protected abstract int getCycleIndex();

    @Override
    public void func_230431_b_(MatrixStack matrixStack, int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_){
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getTextureManager().bindTexture(TEXTURE);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.field_230695_q_);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        this.drawTexture(this.field_230690_l_, this.field_230691_m_, this.textureX + this.getCycleIndex() * 20, (this.field_230693_o_ ? this.field_230692_n_ ? 1 : 0 : 2) * 20, this.field_230688_j_, this.field_230689_k_);
        this.func_230441_a_(matrixStack, minecraft, p_renderButton_1_, p_renderButton_2_);
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
