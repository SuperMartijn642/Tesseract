package com.supermartijn642.tesseract.screen;

import com.supermartijn642.core.gui.GuiGraphicsHelper;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.core.gui.widget.premade.AbstractButtonWidget;
import net.minecraft.resources.Identifier;

/**
 * Created 7/5/2020 by SuperMartijn642
 */
public abstract class CycleButton extends AbstractButtonWidget {

    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("tesseract", "gui/buttons");

    private final int textureX;
    private boolean active = true;

    public CycleButton(int x, int y, int textureX){
        super(x, y, 20, 20, null);
        this.textureX = textureX;
    }

    protected abstract int getCycleIndex();

    @Override
    public void render(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY){
        graphics.submitSprite(TEXTURE, this.x, this.y, this.width, this.height, p -> p.uv((this.textureX + this.getCycleIndex() * 20) / 120f, (this.active ? this.isFocused() ? 1 : 0 : 2) / 3f, 1 / 6f, 1 / 3f));
    }
}
