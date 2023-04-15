package com.supermartijn642.tesseract.screen;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.BlockEntityBaseWidget;
import com.supermartijn642.core.gui.widget.premade.TextFieldWidget;
import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.Tesseract;
import com.supermartijn642.tesseract.TesseractBlockEntity;
import com.supermartijn642.tesseract.TesseractClient;
import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractChannelManager;
import com.supermartijn642.tesseract.packets.PacketScreenAddChannel;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import java.util.List;
import java.util.Locale;

/**
 * Created 5/28/2021 by SuperMartijn642
 */
public class TesseractAddChannelScreen extends BlockEntityBaseWidget<TesseractBlockEntity> {

    private static final int CHANNEL_MIN_CHARACTERS = 3;
    private static final int CHANNEL_MAX_CHARACTERS = 19;

    private static final ResourceLocation BACKGROUND = new ResourceLocation("tesseract", "textures/gui/add_screen_background.png");

    private final EnumChannelType type;

    private TextFieldWidget nameField;
    private TesseractButton addButton;
    private LockButton lockButton;

    protected TesseractAddChannelScreen(World level, BlockPos pos, EnumChannelType type){
        super(0, 0, 144, 65, level, pos);
        this.type = type;
    }

    @Override
    protected void addWidgets(TesseractBlockEntity entity){
        this.nameField = this.addWidget(new TextFieldWidget(7, 21, 107, 18, "", CHANNEL_MAX_CHARACTERS, this::checkChannelName));
        this.nameField.setSuggestion(ClientUtils.translate("gui.tesseract.add.suggestion"));
        this.addButton = this.addWidget(new TesseractButton(8, 43, 61, 14, TextComponents.translation("gui.tesseract.add.add").get(), this::addChannel));
        this.addButton.active = false;
        TesseractButton cancelButton = this.addWidget(new TesseractButton(75, 43, 61, 14, TextComponents.translation("gui.tesseract.add.cancel").get(),
            () -> TesseractClient.openScreen(this.blockEntityPos)));
        cancelButton.setRedBackground();
        this.lockButton = this.addWidget(new LockButton(117, 20));
        this.lockButton.setLocked(true);

        super.addWidgets(entity);
    }

    @Override
    protected void render(int mouseX, int mouseY, TesseractBlockEntity entity){
        ScreenUtils.bindTexture(BACKGROUND);
        ScreenUtils.drawTexture(0, 0, this.width(), this.height());

        ScreenUtils.drawCenteredString(TextComponents.translation("gui.tesseract.add.title." + this.type.name().toLowerCase(Locale.ROOT)).get(), 72, 6, 0xffffffff);

        super.render(mouseX, mouseY, entity);
    }

    private boolean checkChannelName(String name){
        name = name.trim();
        if(name.length() < CHANNEL_MIN_CHARACTERS || name.length() > CHANNEL_MAX_CHARACTERS + 1){
            this.addButton.active = false;
            return false;
        }

        List<Channel> channels = TesseractChannelManager.CLIENT.getChannelsCreatedBy(this.type, ClientUtils.getPlayer().getUUID());
        boolean isUnique = true;
        for(Channel channel : channels){
            if(channel.name.equals(name)){
                isUnique = false;
                break;
            }
        }
        this.addButton.active = isUnique;
        return isUnique;
    }

    /**
     * Called when the add button is clicked
     */
    private void addChannel(){
        String name = this.nameField.getText().trim();
        if(!this.checkChannelName(name))
            return;

        Tesseract.CHANNEL.sendToServer(new PacketScreenAddChannel(this.type, name, this.lockButton.isLocked()));
        TesseractClient.openScreen(this.blockEntityPos);
    }

    @Override
    protected ITextComponent getNarrationMessage(TesseractBlockEntity object){
        return TextComponents.translation("gui.tesseract.add.title." + this.type.name().toLowerCase(Locale.ROOT)).get();
    }
}
