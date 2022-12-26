package com.supermartijn642.tesseract;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.render.CustomBlockEntityRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import java.nio.FloatBuffer;
import java.util.Random;

/**
 * Created 3/19/2020 by SuperMartijn642
 */
public class TesseractBlockEntityRenderer implements CustomBlockEntityRenderer<TesseractBlockEntity> {

    private static final ResourceLocation END_SKY_TEXTURE = new ResourceLocation("textures/environment/end_sky.png");
    private static final ResourceLocation END_PORTAL_TEXTURE = new ResourceLocation("textures/entity/end_portal.png");
    private static final FloatBuffer MODELVIEW = GLAllocation.createDirectFloatBuffer(16);
    private static final FloatBuffer PROJECTION = GLAllocation.createDirectFloatBuffer(16);
    private final FloatBuffer buffer = GLAllocation.createDirectFloatBuffer(16);

    @Override
    public void render(TesseractBlockEntity entity, float partialTicks, int combinedOverlay, float alpha){
        if(!entity.renderOn())
            return;

        GlStateManager.pushMatrix();

        GlStateManager.translate(0.5, 0.5, 0.5);
        GlStateManager.scale(0.65, 0.65, 0.65);
        GlStateManager.translate(-0.5, -0.5, -0.5);
        this.renderEnderFaces();

        GlStateManager.popMatrix();
    }

    private void renderEnderFaces(){ // Adapted from TileEntityEndPortalRenderer
        Random random = new Random(31100L);

        GlStateManager.disableLighting();

        GlStateManager.getFloat(2982, MODELVIEW);
        GlStateManager.getFloat(2983, PROJECTION);

        int i = 15;

        for(int j = 0; j < i; ++j){
            GlStateManager.pushMatrix();
            float f1 = 2f / (float)(18 - j);

            if(j == 0){
                ScreenUtils.bindTexture(END_SKY_TEXTURE);
                f1 = 0.15F;
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            }

            if(j >= 1){
                ScreenUtils.bindTexture(END_PORTAL_TEXTURE);
                ClientUtils.getMinecraft().entityRenderer.setupFogColor(true);
            }

            if(j == 1){
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
            }

            GlStateManager.texGen(GlStateManager.TexGen.S, 9216);
            GlStateManager.texGen(GlStateManager.TexGen.T, 9216);
            GlStateManager.texGen(GlStateManager.TexGen.R, 9216);
            GlStateManager.texGen(GlStateManager.TexGen.S, 9474, this.getBuffer(1.0F, 0.0F, 0.0F));
            GlStateManager.texGen(GlStateManager.TexGen.T, 9474, this.getBuffer(0.0F, 1.0F, 0.0F));
            GlStateManager.texGen(GlStateManager.TexGen.R, 9474, this.getBuffer(0.0F, 0.0F, 1.0F));
            GlStateManager.enableTexGenCoord(GlStateManager.TexGen.S);
            GlStateManager.enableTexGenCoord(GlStateManager.TexGen.T);
            GlStateManager.enableTexGenCoord(GlStateManager.TexGen.R);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(5890);
            GlStateManager.pushMatrix();
            GlStateManager.loadIdentity();
            GlStateManager.translate(0.5f, 0.5f, 0);
            GlStateManager.scale(0.5f, 0.5f, 1);
            float f2 = (float)(j + 1);
            GlStateManager.translate(17f / f2, (2f + f2 / 1.5f) * ((float)Minecraft.getSystemTime() % 800000f / 800000f), 0);
            GlStateManager.rotate((f2 * f2 * 4321 + f2 * 9f) * 2f, 0, 0, 1);
            GlStateManager.scale(4.5f - f2 / 4f, 4.5f - f2 / 4f, 1);
            GlStateManager.multMatrix(PROJECTION);
            GlStateManager.multMatrix(MODELVIEW);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
            float f3 = (random.nextFloat() * 0.5f + 0.1f) * f1;
            float f4 = (random.nextFloat() * 0.5f + 0.4f) * f1;
            float f5 = (random.nextFloat() * 0.5f + 0.5f) * f1;

            // SOUTH
            bufferbuilder.pos(0, 0, 1).color(f3, f4, f5, 1).endVertex();
            bufferbuilder.pos(1, 0, 1).color(f3, f4, f5, 1).endVertex();
            bufferbuilder.pos(1, 1, 1).color(f3, f4, f5, 1).endVertex();
            bufferbuilder.pos(0, 1, 1).color(f3, f4, f5, 1).endVertex();

            // NORTH
            bufferbuilder.pos(0, 1, 0).color(f3, f4, f5, 1).endVertex();
            bufferbuilder.pos(1, 1, 0).color(f3, f4, f5, 1).endVertex();
            bufferbuilder.pos(1, 0, 0).color(f3, f4, f5, 1).endVertex();
            bufferbuilder.pos(0, 0, 0).color(f3, f4, f5, 1).endVertex();

            // EAST
            bufferbuilder.pos(1, 1, 0).color(f3, f4, f5, 1).endVertex();
            bufferbuilder.pos(1, 1, 1).color(f3, f4, f5, 1).endVertex();
            bufferbuilder.pos(1, 0, 1).color(f3, f4, f5, 1).endVertex();
            bufferbuilder.pos(1, 0, 0).color(f3, f4, f5, 1).endVertex();

            // WEST
            bufferbuilder.pos(0, 0, 0).color(f3, f4, f5, 1).endVertex();
            bufferbuilder.pos(0, 0, 1).color(f3, f4, f5, 1).endVertex();
            bufferbuilder.pos(0, 1, 1).color(f3, f4, f5, 1).endVertex();
            bufferbuilder.pos(0, 1, 0).color(f3, f4, f5, 1).endVertex();

            // DOWN
            bufferbuilder.pos(0, 0, 0).color(f3, f4, f5, 1).endVertex();
            bufferbuilder.pos(1, 0, 0).color(f3, f4, f5, 1).endVertex();
            bufferbuilder.pos(1, 0, 1).color(f3, f4, f5, 1).endVertex();
            bufferbuilder.pos(0, 0, 1).color(f3, f4, f5, 1).endVertex();

            // UP
            bufferbuilder.pos(0, 1, 1).color(f3, f4, f5, 1).endVertex();
            bufferbuilder.pos(1, 1, 1).color(f3, f4, f5, 1).endVertex();
            bufferbuilder.pos(1, 1, 0).color(f3, f4, f5, 1).endVertex();
            bufferbuilder.pos(0, 1, 0).color(f3, f4, f5, 1).endVertex();

            tessellator.draw();
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(5888);
            ScreenUtils.bindTexture(END_SKY_TEXTURE);
        }

        GlStateManager.disableBlend();
        GlStateManager.disableTexGenCoord(GlStateManager.TexGen.S);
        GlStateManager.disableTexGenCoord(GlStateManager.TexGen.T);
        GlStateManager.disableTexGenCoord(GlStateManager.TexGen.R);
        GlStateManager.enableLighting();
        ClientUtils.getMinecraft().entityRenderer.setupFogColor(false);
    }

    private FloatBuffer getBuffer(float p_147525_1_, float p_147525_2_, float p_147525_3_){
        this.buffer.clear();
        this.buffer.put(p_147525_1_).put(p_147525_2_).put(p_147525_3_).put(0);
        this.buffer.flip();
        return this.buffer;
    }
}
