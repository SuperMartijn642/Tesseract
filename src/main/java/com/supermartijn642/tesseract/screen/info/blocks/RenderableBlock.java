package com.supermartijn642.tesseract.screen.info.blocks;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.supermartijn642.core.ClientUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Created 7/16/2021 by SuperMartijn642
 */
public abstract class RenderableBlock {

    private static final MultiBufferSource.BufferSource MODEL_BUFFER = MultiBufferSource.immediate(new BufferBuilder(128));

    private final float rotation;
    private final int x, y, z;

    public RenderableBlock(float rotation, int x, int y, int z){
        this.rotation = rotation;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void render(PoseStack matrixStack){
        matrixStack.pushPose();
        matrixStack.translate(this.x, this.y, this.z);
        if(this.rotation != 0){
            matrixStack.translate(0.5, 0.5, 0.5);
            matrixStack.mulPose(new Quaternion(0, this.rotation, 0, true));
            matrixStack.translate(-0.5, -0.5, -0.5);
        }
        this.renderInternal(matrixStack);
        matrixStack.popPose();
    }

    protected abstract void renderInternal(PoseStack matrixStack);

    public static RenderableBlock of(ResourceLocation modelLocation, float rotation, int x, int y, int z){
        return new RenderableBlock(rotation, x, y, z) {
            @Override
            public void renderInternal(PoseStack matrixStack){
                BakedModel model = ClientUtils.getBlockRenderer().getBlockModelShaper().getModelManager().getModel(modelLocation);
//                ClientUtils.getBlockRenderer().getModelRenderer().renderModel(matrixStack.last(), MODEL_BUFFER.getBuffer(RenderType.solid()), null, model, 1, 1, 1, 0, 0, ModelData.EMPTY);
            }
        };
    }

    public static RenderableBlock of(Block block, float rotation, int x, int y, int z){
        return new RenderableBlock(rotation, x, y, z) {
            @Override
            public void renderInternal(PoseStack matrixStack){
                BakedModel model = ClientUtils.getBlockRenderer().getBlockModel(block.defaultBlockState());
//                ClientUtils.getBlockRenderer().getModelRenderer().renderModel(matrixStack.last(), MODEL_BUFFER.getBuffer(RenderType.solid()), null, model, 1, 1, 1, 0, 0, ModelData.EMPTY);
            }
        };
    }

    public static RenderableBlock of(BlockState state, float rotation, int x, int y, int z){
        return new RenderableBlock(rotation, x, y, z) {
            @Override
            public void renderInternal(PoseStack matrixStack){
                BakedModel model = ClientUtils.getBlockRenderer().getBlockModel(state);
//                ClientUtils.getBlockRenderer().getModelRenderer().renderModel(matrixStack.last(), MODEL_BUFFER.getBuffer(RenderType.solid()), null, model, 1, 1, 1, 0, 0, ModelData.EMPTY);
            }
        };
    }

}
