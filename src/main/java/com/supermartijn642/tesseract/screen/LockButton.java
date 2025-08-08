package com.supermartijn642.tesseract.screen;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.GuiGraphicsHelper;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.core.gui.widget.premade.AbstractButtonWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

/**
 * Created 4/13/2021 by SuperMartijn642
 */
public class LockButton extends AbstractButtonWidget {

    private boolean locked;
    private boolean active = true;

    public LockButton(int x, int y){
        super(x, y, 20, 20, null);
    }

    @Override
    public void onPress(){
        super.onPress();
        this.locked = !this.locked;
    }

    @Override
    public Component getNarrationMessage(){
        return TextComponents.translation("gui.narrate.button", TextComponents.translation("narrator.button.difficulty_lock").get()).string(". ").translation(this.isLocked() ? "narrator.button.difficulty_lock.locked" : "narrator.button.difficulty_lock.unlocked").get();
    }

    @Override
    public void render(WidgetRenderContext context, GuiGraphicsHelper graphics, int x, int y){
        Icon icon;
        if(!this.active)
            icon = this.locked ? Icon.LOCKED_DISABLED : Icon.UNLOCKED_DISABLED;
        else if(this.isFocused())
            icon = this.locked ? Icon.LOCKED_HOVER : Icon.UNLOCKED_HOVER;
        else
            icon = this.locked ? Icon.LOCKED : Icon.UNLOCKED;

        graphics.submitSprite(icon.location, this.x, this.y, this.width, this.height);
    }

    public boolean isLocked(){
        return this.locked;
    }

    public void setLocked(boolean lockedIn){
        this.locked = lockedIn;
    }

    @Override
    protected void getTooltips(Consumer<Component> tooltips){
        tooltips.accept(TextComponents.translation("gui.tesseract.channel." + (this.locked ? "private" : "public")).get());
    }

    public enum Icon {
        LOCKED(ResourceLocation.withDefaultNamespace("gui/sprites/widget/locked_button")),
        LOCKED_HOVER(ResourceLocation.withDefaultNamespace("gui/sprites/widget/locked_button_highlighted")),
        LOCKED_DISABLED(ResourceLocation.withDefaultNamespace("gui/sprites/widget/locked_button_disabled")),
        UNLOCKED(ResourceLocation.withDefaultNamespace("gui/sprites/widget/unlocked_button")),
        UNLOCKED_HOVER(ResourceLocation.withDefaultNamespace("gui/sprites/widget/unlocked_button_highlighted")),
        UNLOCKED_DISABLED(ResourceLocation.withDefaultNamespace("gui/sprites/widget/unlocked_button_disabled"));

        private final ResourceLocation location;

        Icon(ResourceLocation location){
            this.location = location;
        }

        public ResourceLocation location(){
            return this.location;
        }
    }
}
