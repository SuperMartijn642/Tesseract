package com.supermartijn642.tesseract.screen.info.blocks;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.math.vector.Quaternion;

/**
 * Created 7/16/2021 by SuperMartijn642
 */
public class BlockRenderer {

    public static void renderBlocks(MatrixStack matrixStack, float xRotation, float yRotation, float zRotation, float scale, RenderableBlock... blocks){
        matrixStack.push();
        matrixStack.rotate(new Quaternion(xRotation, yRotation, zRotation, true));
        matrixStack.scale(scale,scale,scale);
        for(RenderableBlock block : blocks)
            block.render(matrixStack);
        matrixStack.pop();
    }

}
