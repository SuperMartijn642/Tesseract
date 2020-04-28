package com.supermartijn642.tesseract;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;

import java.nio.FloatBuffer;
import java.util.Random;

/**
 * Created 3/19/2020 by SuperMartijn642
 */
public class TesseractTileRenderer extends TileEntityRenderer<TesseractTile> {

    private static final ResourceLocation END_SKY_TEXTURE = new ResourceLocation("textures/environment/end_sky.png");
    private static final ResourceLocation END_PORTAL_TEXTURE = new ResourceLocation("textures/entity/end_portal.png");
    private static final FloatBuffer MODELVIEW = GLAllocation.createDirectFloatBuffer(16);
    private static final FloatBuffer PROJECTION = GLAllocation.createDirectFloatBuffer(16);
    private final FloatBuffer buffer = GLAllocation.createDirectFloatBuffer(16);

    @Override
    public void render(TesseractTile tile, double x, double y, double z, float partialTicks, int destroyStage){
        if(!tile.renderOn())
            return;

        GlStateManager.pushMatrix();
        GlStateManager.pushLightingAttributes();

        GlStateManager.disableLighting();

        GlStateManager.translated(x + 0.5, y + 0.5, z + 0.5);
        GlStateManager.scaled(0.65, 0.65, 0.65);
        GlStateManager.translated(-0.5, -0.5, -0.5);
        this.renderEnderFaces();

        GlStateManager.popMatrix();
        GlStateManager.popAttributes();
    }

    private void renderEnderFaces(){ // Adapted from TileEntityEndPortalRenderer
        Random random = new Random(31100L);

        GlStateManager.disableLighting();

        GlStateManager.getMatrix(2982, MODELVIEW);
        GlStateManager.getMatrix(2983, PROJECTION);

        int i = 15;

        for(int j = 0; j < i; ++j){
            GlStateManager.pushMatrix();
            float f1 = 2.0F / (float)(18 - j);

            if(j == 0){
                this.bindTexture(END_SKY_TEXTURE);
                f1 = 0.15F;
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            }

            if(j >= 1){
                this.bindTexture(END_PORTAL_TEXTURE);
                Minecraft.getInstance().gameRenderer.setupFogColor(true);
            }

            if(j == 1){
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
            }

            GlStateManager.texGenMode(GlStateManager.TexGen.S, 9216);
            GlStateManager.texGenMode(GlStateManager.TexGen.T, 9216);
            GlStateManager.texGenMode(GlStateManager.TexGen.R, 9216);
            GlStateManager.texGenParam(GlStateManager.TexGen.S, 9474, this.getBuffer(1.0F, 0.0F, 0.0F));
            GlStateManager.texGenParam(GlStateManager.TexGen.T, 9474, this.getBuffer(0.0F, 1.0F, 0.0F));
            GlStateManager.texGenParam(GlStateManager.TexGen.R, 9474, this.getBuffer(0.0F, 0.0F, 1.0F));
            GlStateManager.enableTexGen(GlStateManager.TexGen.S);
            GlStateManager.enableTexGen(GlStateManager.TexGen.T);
            GlStateManager.enableTexGen(GlStateManager.TexGen.R);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(5890);
            GlStateManager.pushMatrix();
            GlStateManager.loadIdentity();
            GlStateManager.translatef(0.5F, 0.5F, 0.0F);
            GlStateManager.scalef(0.5F, 0.5F, 1.0F);
            float f2 = (float)(j + 1);
            GlStateManager.translatef(17.0F / f2, (2.0F + f2 / 1.5F) * (Util.milliTime() % 800000.0F / 800000.0F), 0.0F);
            GlStateManager.rotatef((f2 * f2 * 4321.0F + f2 * 9.0F) * 2.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.scalef(4.5F - f2 / 4.0F, 4.5F - f2 / 4.0F, 1.0F);
            GlStateManager.multMatrix(PROJECTION);
            GlStateManager.multMatrix(MODELVIEW);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
            float f3 = (random.nextFloat() * 0.5F + 0.1F) * f1;
            float f4 = (random.nextFloat() * 0.5F + 0.4F) * f1;
            float f5 = (random.nextFloat() * 0.5F + 0.5F) * f1;

            // SOUTH
            bufferbuilder.pos(0, 0, (double)0 + 1.0D).color(f3, f4, f5, 1.0F).endVertex();
            bufferbuilder.pos((double)0 + 1.0D, 0, (double)0 + 1.0D).color(f3, f4, f5, 1.0F).endVertex();
            bufferbuilder.pos((double)0 + 1.0D, (double)0 + 1.0D, (double)0 + 1.0D).color(f3, f4, f5, 1.0F).endVertex();
            bufferbuilder.pos(0, (double)0 + 1.0D, (double)0 + 1.0D).color(f3, f4, f5, 1.0F).endVertex();

            // NORTH
            bufferbuilder.pos(0, (double)0 + 1.0D, 0).color(f3, f4, f5, 1.0F).endVertex();
            bufferbuilder.pos((double)0 + 1.0D, (double)0 + 1.0D, 0).color(f3, f4, f5, 1.0F).endVertex();
            bufferbuilder.pos((double)0 + 1.0D, 0, 0).color(f3, f4, f5, 1.0F).endVertex();
            bufferbuilder.pos(0, 0, 0).color(f3, f4, f5, 1.0F).endVertex();

            // EAST
            bufferbuilder.pos((double)0 + 1.0D, (double)0 + 1.0D, 0).color(f3, f4, f5, 1.0F).endVertex();
            bufferbuilder.pos((double)0 + 1.0D, (double)0 + 1.0D, (double)0 + 1.0D).color(f3, f4, f5, 1.0F).endVertex();
            bufferbuilder.pos((double)0 + 1.0D, 0, (double)0 + 1.0D).color(f3, f4, f5, 1.0F).endVertex();
            bufferbuilder.pos((double)0 + 1.0D, 0, 0).color(f3, f4, f5, 1.0F).endVertex();

            // WEST
            bufferbuilder.pos(0, 0, 0).color(f3, f4, f5, 1.0F).endVertex();
            bufferbuilder.pos(0, 0, (double)0 + 1.0D).color(f3, f4, f5, 1.0F).endVertex();
            bufferbuilder.pos(0, (double)0 + 1.0D, (double)0 + 1.0D).color(f3, f4, f5, 1.0F).endVertex();
            bufferbuilder.pos(0, (double)0 + 1.0D, 0).color(f3, f4, f5, 1.0F).endVertex();

            // DOWN
            bufferbuilder.pos(0, 0, 0).color(f3, f4, f5, 1.0F).endVertex();
            bufferbuilder.pos((double)0 + 1.0D, 0, 0).color(f3, f4, f5, 1.0F).endVertex();
            bufferbuilder.pos((double)0 + 1.0D, 0, (double)0 + 1.0D).color(f3, f4, f5, 1.0F).endVertex();
            bufferbuilder.pos(0, 0, (double)0 + 1.0D).color(f3, f4, f5, 1.0F).endVertex();

            // UP
            bufferbuilder.pos(0, 1, (double)0 + 1.0D).color(f3, f4, f5, 1.0F).endVertex();
            bufferbuilder.pos((double)0 + 1.0D, 1, (double)0 + 1.0D).color(f3, f4, f5, 1.0F).endVertex();
            bufferbuilder.pos((double)0 + 1.0D, 1, 0).color(f3, f4, f5, 1.0F).endVertex();
            bufferbuilder.pos(0, 1, 0).color(f3, f4, f5, 1.0F).endVertex();

            tessellator.draw();
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(5888);
            this.bindTexture(END_SKY_TEXTURE);
        }

        GlStateManager.disableBlend();
        GlStateManager.disableTexGen(GlStateManager.TexGen.S);
        GlStateManager.disableTexGen(GlStateManager.TexGen.T);
        GlStateManager.disableTexGen(GlStateManager.TexGen.R);
        GlStateManager.enableLighting();
        Minecraft.getInstance().gameRenderer.setupFogColor(false);
    }

    private FloatBuffer getBuffer(float p_147525_1_, float p_147525_2_, float p_147525_3_){
        this.buffer.clear();
        this.buffer.put(p_147525_1_).put(p_147525_2_).put(p_147525_3_).put((float)0.0);
        this.buffer.flip();
        return this.buffer;
    }
}
