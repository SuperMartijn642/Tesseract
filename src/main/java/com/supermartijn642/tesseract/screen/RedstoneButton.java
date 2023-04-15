package com.supermartijn642.tesseract.screen;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.tesseract.RedstoneState;
import com.supermartijn642.tesseract.Tesseract;
import com.supermartijn642.tesseract.TesseractBlockEntity;
import com.supermartijn642.tesseract.packets.PacketScreenCycleRedstoneState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;

import java.util.function.Consumer;

/**
 * Created 7/5/2020 by SuperMartijn642
 */
public class RedstoneButton extends CycleButton {

    public RedstoneState state;
    private BlockPos pos;

    public RedstoneButton(int x, int y){
        super(x, y, 60);
    }

    public void update(TesseractBlockEntity entity){
        this.state = entity.getRedstoneState();
        this.pos = entity.getBlockPos();
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
    public ITextComponent getNarrationMessage(){
        return TextComponents.translation("gui.tesseract.redstone.speech", this.state.translate()).get();
    }

    @Override
    protected void getTooltips(Consumer<ITextComponent> tooltips){
        tooltips.accept(TextComponents.translation("gui.tesseract.redstone.speech", this.state.translate()).get());
    }
}
