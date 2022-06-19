package com.supermartijn642.tesseract.screen;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.widget.IHoverTextWidget;
import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.Tesseract;
import com.supermartijn642.tesseract.TesseractTile;
import com.supermartijn642.tesseract.TransferState;
import com.supermartijn642.tesseract.packets.PacketScreenCycleTransferState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

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
        this.pos = tile.getBlockPos();
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
    protected Component getNarrationMessage(){
        return this.getHoverText();
    }

    @Override
    public Component getHoverText(){
        return TextComponents.translation("gui.tesseract.transfer.speech", this.state.translate()).get();
    }
}
