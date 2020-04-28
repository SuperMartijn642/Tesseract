package com.supermartijn642.tesseract;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * Created 3/19/2020 by SuperMartijn642
 */
public class TesseractTileRenderer extends TileEntityRenderer<TesseractTile> {

    private static final Random RANDOM = new Random(31100L);
    private static final List<RenderType> RENDER_TYPES = IntStream.range(0, 16).mapToObj((p_228882_0_) -> RenderType.getEndPortal(p_228882_0_ + 1)).collect(ImmutableList.toImmutableList());

    public TesseractTileRenderer(TileEntityRendererDispatcher dispatcher){
        super(dispatcher);
    }

    @Override
    public void render(TesseractTile tile, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay){
        if(!tile.renderOn())
            return;

        matrixStack.push();

        matrixStack.translate(0.5, 0.5, 0.5);
        matrixStack.scale(0.65f, 0.65f, 0.65f);
        matrixStack.translate(-0.5, -0.5, -0.5);

        RANDOM.setSeed(31100L);
        int i = 15;
        Matrix4f matrix4f = matrixStack.getLast().getMatrix();
        this.renderCube(0.15F, matrix4f, buffer.getBuffer(RENDER_TYPES.get(0)));

        for(int j = 1; j < i; ++j){
            this.renderCube(2.0F / (18 - j), matrix4f, buffer.getBuffer(RENDER_TYPES.get(j)));
        }

        matrixStack.pop();
    }

    private void renderCube(float p_228883_3_, Matrix4f matrix4f, IVertexBuilder builder){
        float f = (RANDOM.nextFloat() * 0.5F + 0.1F) * p_228883_3_;
        float f1 = (RANDOM.nextFloat() * 0.5F + 0.4F) * p_228883_3_;
        float f2 = (RANDOM.nextFloat() * 0.5F + 0.5F) * p_228883_3_;
        this.renderFace(matrix4f, builder, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, f, f1, f2);
        this.renderFace(matrix4f, builder, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, f, f1, f2);
        this.renderFace(matrix4f, builder, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, f, f1, f2);
        this.renderFace(matrix4f, builder, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, f, f1, f2);
        this.renderFace(matrix4f, builder, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, f, f1, f2);
        this.renderFace(matrix4f, builder, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, f, f1, f2);
    }

    private void renderFace(Matrix4f matrix4f, IVertexBuilder builder, float x1, float x2, float y1, float y2, float p_228884_8_, float p_228884_9_, float p_228884_10_, float p_228884_11_, float red, float green, float blue){
        builder.pos(matrix4f, x1, y1, p_228884_8_).color(red, green, blue, 1.0F).endVertex();
        builder.pos(matrix4f, x2, y1, p_228884_9_).color(red, green, blue, 1.0F).endVertex();
        builder.pos(matrix4f, x2, y2, p_228884_10_).color(red, green, blue, 1.0F).endVertex();
        builder.pos(matrix4f, x1, y2, p_228884_11_).color(red, green, blue, 1.0F).endVertex();
    }
}
