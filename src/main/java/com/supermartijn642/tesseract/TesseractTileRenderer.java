package com.supermartijn642.tesseract;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;

/**
 * Created 3/19/2020 by SuperMartijn642
 */
public class TesseractTileRenderer implements BlockEntityRenderer<TesseractTile> {

    @Override
    public void render(TesseractTile tile, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay){
        if(!tile.renderOn())
            return;

        matrixStack.pushPose();

        matrixStack.translate(0.5, 0.5, 0.5);
        matrixStack.scale(0.65f, 0.65f, 0.65f);
        matrixStack.translate(-0.5, -0.5, -0.5);

        Matrix4f matrix4f = matrixStack.last().pose();
        this.renderCube(matrix4f, buffer.getBuffer(RenderType.endPortal()));

        matrixStack.popPose();
    }

    private void renderCube(Matrix4f matrix4f, VertexConsumer builder){
        this.renderFace(matrix4f, builder, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F);
        this.renderFace(matrix4f, builder, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        this.renderFace(matrix4f, builder, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F);
        this.renderFace(matrix4f, builder, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F);
        this.renderFace(matrix4f, builder, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F);
        this.renderFace(matrix4f, builder, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F);
    }

    private void renderFace(Matrix4f matrix4f, VertexConsumer builder, float x1, float x2, float y1, float y2, float z1, float z2, float p_228884_10_, float p_228884_11_){
        builder.vertex(matrix4f, x1, y1, z1).endVertex();
        builder.vertex(matrix4f, x2, y1, z2).endVertex();
        builder.vertex(matrix4f, x2, y2, p_228884_10_).endVertex();
        builder.vertex(matrix4f, x1, y2, p_228884_11_).endVertex();
    }
}
