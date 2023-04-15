package com.supermartijn642.tesseract.screen;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.Tesseract;
import com.supermartijn642.tesseract.TesseractBlockEntity;
import com.supermartijn642.tesseract.TransferState;
import com.supermartijn642.tesseract.packets.PacketScreenCycleTransferState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;

import java.util.function.Consumer;

/**
 * Created 7/6/2020 by SuperMartijn642
 */
public class TransferButton extends CycleButton {

    public TransferState state;
    private BlockPos pos;
    private EnumChannelType type;

    public TransferButton(int x, int y){
        super(x, y, 0);
    }

    public void update(TesseractBlockEntity entity, EnumChannelType type){
        this.state = entity.getTransferState(type);
        this.pos = entity.getBlockPos();
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
    public ITextComponent getNarrationMessage(){
        return TextComponents.translation("gui.tesseract.transfer.speech", this.state.translate()).get();
    }

    @Override
    protected void getTooltips(Consumer<ITextComponent> tooltips){
        tooltips.accept(TextComponents.translation("gui.tesseract.transfer.speech", this.state.translate()).get());
    }
}
