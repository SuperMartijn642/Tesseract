package com.supermartijn642.tesseract.screen;

import com.supermartijn642.core.gui.widget.IHoverTextWidget;
import com.supermartijn642.tesseract.RedstoneState;
import com.supermartijn642.tesseract.Tesseract;
import com.supermartijn642.tesseract.TesseractTile;
import com.supermartijn642.tesseract.packets.PacketScreenCycleRedstoneState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * Created 7/5/2020 by SuperMartijn642
 */
public class RedstoneButton extends CycleButton implements IHoverTextWidget {

    public RedstoneState state;
    private BlockPos pos;

    public RedstoneButton(int x, int y){
        super(x, y, 60);
    }

    public void update(TesseractTile tile){
        this.state = tile.getRedstoneState();
        this.pos = tile.getPos();
    }

    @Override
    protected int getCycleIndex(){
        return this.state == RedstoneState.DISABLED ? 0 : this.state == RedstoneState.HIGH ? 1 : 2;
    }

    @Override
    public void onPress(){
        super.onPress();
        if(this.pos != null)
            Tesseract.CHANNEL.sendToServer(new PacketScreenCycleRedstoneState(this.pos));
    }

    @Override
    protected ITextComponent getNarrationMessage(){
        return this.getHoverText();
    }

    @Override
    public ITextComponent getHoverText(){
        return new TranslationTextComponent("gui.tesseract.redstone.speech", this.state.translate());
    }
}
