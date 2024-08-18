package com.supermartijn642.tesseract;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.supermartijn642.core.render.CustomBlockEntityRenderer;
import com.supermartijn642.core.render.RenderConfiguration;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;

/**
 * Created 3/19/2020 by SuperMartijn642
 */
public class TesseractBlockEntityRenderer implements CustomBlockEntityRenderer<TesseractBlockEntity> {

    @Override
    public void render(TesseractBlockEntity entity, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay){
        if(!entity.renderOn())
            return;

        poseStack.pushPose();

        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.scale(0.65f, 0.65f, 0.65f);
        poseStack.translate(-0.5, -0.5, -0.5);

        Matrix4f matrix4f = poseStack.last().pose();
        this.renderCube(matrix4f, bufferSource.getBuffer(RenderConfiguration.endPortal()));

        poseStack.popPose();
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
        builder.addVertex(matrix4f, x1, y1, z1);
        builder.addVertex(matrix4f, x2, y1, z2);
        builder.addVertex(matrix4f, x2, y2, p_228884_10_);
        builder.addVertex(matrix4f, x1, y2, p_228884_11_);
    }
}
