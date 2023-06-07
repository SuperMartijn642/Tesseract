package com.supermartijn642.tesseract.screen;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.core.gui.widget.premade.AbstractButtonWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

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
        return TextComponents.translation("gui.narrate.button", TextComponents.translation("narrator.button.difficulty_lock")).string(". ").translation(this.isLocked() ? "narrator.button.difficulty_lock.locked" : "narrator.button.difficulty_lock.unlocked").get();
    }

    @Override
    public void render(WidgetRenderContext context, int x, int y){
        ScreenUtils.bindTexture(Button.WIDGETS_LOCATION);

        Icon icon;
        if(!this.active)
            icon = this.locked ? Icon.LOCKED_DISABLED : Icon.UNLOCKED_DISABLED;
        else if(this.isFocused())
            icon = this.locked ? Icon.LOCKED_HOVER : Icon.UNLOCKED_HOVER;
        else
            icon = this.locked ? Icon.LOCKED : Icon.UNLOCKED;

        ScreenUtils.drawTexture(context.poseStack(), this.x, this.y, this.width, this.height, icon.getX() / 256f, icon.getY() / 256f, 20 / 256f, 20 / 256f);
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

    enum Icon {
        LOCKED(0, 146),
        LOCKED_HOVER(0, 166),
        LOCKED_DISABLED(0, 186),
        UNLOCKED(20, 146),
        UNLOCKED_HOVER(20, 166),
        UNLOCKED_DISABLED(20, 186);

        private final int x;
        private final int y;

        Icon(int xIn, int yIn){
            this.x = xIn;
            this.y = yIn;
        }

        public int getX(){
            return this.x;
        }

        public int getY(){
            return this.y;
        }
    }
}
