package com.supermartijn642.tesseract.screen;

import com.supermartijn642.core.gui.GuiGraphicsHelper;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.core.gui.widget.premade.ButtonWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * Created 5/13/2021 by SuperMartijn642
 */
public class TesseractButton extends ButtonWidget {

    public static final Identifier BUTTON_BACKGROUND = Identifier.fromNamespaceAndPath("tesseract", "gui/default_buttons");
    public static final Identifier RED_BUTTON_BACKGROUND = Identifier.fromNamespaceAndPath("tesseract", "gui/red_buttons");

    private Component text;
    private Identifier background = BUTTON_BACKGROUND;
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
    public void render(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY){
        this.drawButtonBackground(graphics, (float)this.x, (float)this.y, (float)this.width, (float)this.height, (float)(this.active ? (this.isFocused() ? 5 : 0) : 10) / 15.0F);
        float textX = (float)this.x + (float)this.width / 2.0F;
        float textY = (float)this.y + (float)this.height / 2.0F - 4.0F;
        graphics.submitText(this.text, textX, textY, p -> p.color(this.active ? -1 : 2147483647).shadow().centerHorizontally());
    }

    private void drawButtonBackground(GuiGraphicsHelper graphics, float x, float y, float width, float height, float yOffset){
        graphics.submitSprite(this.background, x, y, 2.0F, 2.0F, p -> p.uv(0.0F, yOffset, 0.4F, 0.13333334F));
        graphics.submitSprite(this.background, x + width - 2.0F, y, 2.0F, 2.0F, p -> p.uv(0.6F, yOffset, 0.4F, 0.13333334F));
        graphics.submitSprite(this.background, x + width - 2.0F, y + height - 2.0F, 2.0F, 2.0F, p -> p.uv(0.6F, yOffset + 0.2F, 0.4F, 0.13333334F));
        graphics.submitSprite(this.background, x, y + height - 2.0F, 2.0F, 2.0F, p -> p.uv(0.0F, yOffset + 0.2F, 0.4F, 0.13333334F));
        graphics.submitSprite(this.background, x + 2.0F, y, width - 4.0F, 2.0F, p -> p.uv(0.4F, yOffset, 0.2F, 0.13333334F));
        graphics.submitSprite(this.background, x + 2.0F, y + height - 2.0F, width - 4.0F, 2.0F, p -> p.uv(0.4F, yOffset + 0.2F, 0.2F, 0.13333334F));
        graphics.submitSprite(this.background, x, y + 2.0F, 2.0F, height - 4.0F, p -> p.uv(0.0F, yOffset + 0.13333334F, 0.4F, 0.06666667F));
        graphics.submitSprite(this.background, x + width - 2.0F, y + 2.0F, 2.0F, height - 4.0F, p -> p.uv(0.6F, yOffset + 0.13333334F, 0.4F, 0.06666667F));
        graphics.submitSprite(this.background, x + 2.0F, y + 2.0F, width - 4.0F, height - 4.0F, p -> p.uv(0.4F, yOffset + 0.13333334F, 0.2F, 0.06666667F));
    }
}
