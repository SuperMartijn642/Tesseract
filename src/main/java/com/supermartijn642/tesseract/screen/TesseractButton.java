package com.supermartijn642.tesseract.screen;

import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.ButtonWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

/**
 * Created 5/13/2021 by SuperMartijn642
 */
public class TesseractButton extends ButtonWidget {

    private static final ResourceLocation BUTTON_BACKGROUND = new ResourceLocation("tesseract", "textures/gui/default_buttons.png");
    private static final ResourceLocation RED_BUTTON_BACKGROUND = new ResourceLocation("tesseract", "textures/gui/red_buttons.png");

    private ITextComponent text;
    private ResourceLocation background = BUTTON_BACKGROUND;

    public TesseractButton(int x, int y, int width, int height, ITextComponent text, Runnable onPress){
        super(x, y, width, height, text, onPress);
        this.text = text;
    }

    public void setRedBackground(){
        this.background = RED_BUTTON_BACKGROUND;
    }

    @Override
    public void setText(ITextComponent text){
        super.setText(text);
        this.text = text;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks){
        this.drawButtonBackground((float)this.x, (float)this.y, (float)this.width, (float)this.height, (float)(this.active ? (this.isHovered() ? 5 : 0) : 10) / 15.0F);
        float textX = (float)this.x + (float)this.width / 2.0F;
        float textY = (float)this.y + (float)this.height / 2.0F - 4.0F;
        ScreenUtils.drawCenteredStringWithShadow(Minecraft.getInstance().font, this.text, textX, textY, this.active ? -1 : 2147483647);
    }

    private void drawButtonBackground(float x, float y, float width, float height, float yOffset){
        ScreenUtils.bindTexture(this.background);
        ScreenUtils.drawTexture(x, y, 2.0F, 2.0F, 0.0F, yOffset, 0.4F, 0.13333334F);
        ScreenUtils.drawTexture(x + width - 2.0F, y, 2.0F, 2.0F, 0.6F, yOffset, 0.4F, 0.13333334F);
        ScreenUtils.drawTexture(x + width - 2.0F, y + height - 2.0F, 2.0F, 2.0F, 0.6F, yOffset + 0.2F, 0.4F, 0.13333334F);
        ScreenUtils.drawTexture(x, y + height - 2.0F, 2.0F, 2.0F, 0.0F, yOffset + 0.2F, 0.4F, 0.13333334F);
        ScreenUtils.drawTexture(x + 2.0F, y, width - 4.0F, 2.0F, 0.4F, yOffset, 0.2F, 0.13333334F);
        ScreenUtils.drawTexture(x + 2.0F, y + height - 2.0F, width - 4.0F, 2.0F, 0.4F, yOffset + 0.2F, 0.2F, 0.13333334F);
        ScreenUtils.drawTexture(x, y + 2.0F, 2.0F, height - 4.0F, 0.0F, yOffset + 0.13333334F, 0.4F, 0.06666667F);
        ScreenUtils.drawTexture(x + width - 2.0F, y + 2.0F, 2.0F, height - 4.0F, 0.6F, yOffset + 0.13333334F, 0.4F, 0.06666667F);
        ScreenUtils.drawTexture(x + 2.0F, y + 2.0F, width - 4.0F, height - 4.0F, 0.4F, yOffset + 0.13333334F, 0.2F, 0.06666667F);
    }
}
