package com.supermartijn642.tesseract.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.core.gui.widget.premade.ButtonWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Created 5/13/2021 by SuperMartijn642
 */
public class TesseractButton extends ButtonWidget {

    private static final ResourceLocation BUTTON_BACKGROUND = ResourceLocation.fromNamespaceAndPath("tesseract", "textures/gui/default_buttons.png");
    private static final ResourceLocation RED_BUTTON_BACKGROUND = ResourceLocation.fromNamespaceAndPath("tesseract", "textures/gui/red_buttons.png");

    private Component text;
    private ResourceLocation background = BUTTON_BACKGROUND;
    public boolean active = true;

    public TesseractButton(int x, int y, int width, int height, Component text, Runnable onPress){
        super(x, y, width, height, text, onPress);
        this.text = text;
    }

    public void setRedBackground(){
        this.background = RED_BUTTON_BACKGROUND;
    }

    @Override
    public void setText(Component text){
        super.setText(text);
        this.text = text;
    }

    @Override
    public void render(WidgetRenderContext context, int mouseX, int mouseY){
        this.drawButtonBackground(context.poseStack(), (float)this.x, (float)this.y, (float)this.width, (float)this.height, (float)(this.active ? (this.isFocused() ? 5 : 0) : 10) / 15.0F);
        float textX = (float)this.x + (float)this.width / 2.0F;
        float textY = (float)this.y + (float)this.height / 2.0F - 4.0F;
        ScreenUtils.drawCenteredStringWithShadow(context.poseStack(), Minecraft.getInstance().font, this.text, textX, textY, this.active ? -1 : 2147483647);
    }

    private void drawButtonBackground(PoseStack matrixStack, float x, float y, float width, float height, float yOffset){
        ScreenUtils.bindTexture(this.background);
        ScreenUtils.drawTexture(matrixStack, x, y, 2.0F, 2.0F, 0.0F, yOffset, 0.4F, 0.13333334F);
        ScreenUtils.drawTexture(matrixStack, x + width - 2.0F, y, 2.0F, 2.0F, 0.6F, yOffset, 0.4F, 0.13333334F);
        ScreenUtils.drawTexture(matrixStack, x + width - 2.0F, y + height - 2.0F, 2.0F, 2.0F, 0.6F, yOffset + 0.2F, 0.4F, 0.13333334F);
        ScreenUtils.drawTexture(matrixStack, x, y + height - 2.0F, 2.0F, 2.0F, 0.0F, yOffset + 0.2F, 0.4F, 0.13333334F);
        ScreenUtils.drawTexture(matrixStack, x + 2.0F, y, width - 4.0F, 2.0F, 0.4F, yOffset, 0.2F, 0.13333334F);
        ScreenUtils.drawTexture(matrixStack, x + 2.0F, y + height - 2.0F, width - 4.0F, 2.0F, 0.4F, yOffset + 0.2F, 0.2F, 0.13333334F);
        ScreenUtils.drawTexture(matrixStack, x, y + 2.0F, 2.0F, height - 4.0F, 0.0F, yOffset + 0.13333334F, 0.4F, 0.06666667F);
        ScreenUtils.drawTexture(matrixStack, x + width - 2.0F, y + 2.0F, 2.0F, height - 4.0F, 0.6F, yOffset + 0.13333334F, 0.4F, 0.06666667F);
        ScreenUtils.drawTexture(matrixStack, x + 2.0F, y + 2.0F, width - 4.0F, height - 4.0F, 0.4F, yOffset + 0.13333334F, 0.2F, 0.06666667F);
    }
}
