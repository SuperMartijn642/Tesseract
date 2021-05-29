package com.supermartijn642.tesseract.screen;

import com.supermartijn642.core.gui.widget.IHoverTextWidget;
import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.Tesseract;
import com.supermartijn642.tesseract.TesseractTile;
import com.supermartijn642.tesseract.TransferState;
import com.supermartijn642.tesseract.packets.PacketScreenCycleTransferState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * Created 7/6/2020 by SuperMartijn642
 */
public class TransferButton extends CycleButton implements IHoverTextWidget {

    public TransferState state;
    private BlockPos pos;
    private EnumChannelType type;

    public TransferButton(int x, int y){
        super(x, y, 0);
    }

    public void update(TesseractTile tile, EnumChannelType type){
        this.state = tile.getTransferState(type);
        this.pos = tile.getPos();
        this.type = type;
    }

    @Override
    protected int getCycleIndex(){
        return this.state == TransferState.RECEIVE ? 0 : this.state == TransferState.SEND ? 1 : 2;
    }

    @Override
    public void onPress(){
        super.onPress();
        if(this.pos != null)
            Tesseract.CHANNEL.sendToServer(new PacketScreenCycleTransferState(this.pos, this.type));
    }

    @Override
    protected ITextComponent getNarrationMessage(){
        return this.getHoverText();
    }

    @Override
    public ITextComponent getHoverText(){
        return new TranslationTextComponent("gui.tesseract.transfer.speech", this.state.translate());
    }
}
