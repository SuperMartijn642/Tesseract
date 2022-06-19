package com.supermartijn642.tesseract.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.TileEntityBaseScreen;
import com.supermartijn642.core.gui.widget.TextFieldWidget;
import com.supermartijn642.tesseract.EnumChannelType;
import com.supermartijn642.tesseract.Tesseract;
import com.supermartijn642.tesseract.TesseractTile;
import com.supermartijn642.tesseract.manager.Channel;
import com.supermartijn642.tesseract.manager.TesseractChannelManager;
import com.supermartijn642.tesseract.packets.PacketScreenAddChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Locale;

/**
 * Created 5/28/2021 by SuperMartijn642
 */
public class TesseractAddChannelScreen extends TileEntityBaseScreen<TesseractTile> {

    private static final int CHANNEL_MIN_CHARACTERS = 3;
    private static final int CHANNEL_MAX_CHARACTERS = 19;

    private static final ResourceLocation BACKGROUND = new ResourceLocation("tesseract", "textures/gui/add_screen_background.png");

    private final EnumChannelType type;

    private TextFieldWidget nameField;
    private TesseractButton addButton;
    private LockButton lockButton;

    protected TesseractAddChannelScreen(BlockPos pos, EnumChannelType type){
        super(TextComponents.translation("gui.tesseract.add.title." + type.name().toLowerCase(Locale.ROOT)).get(), pos);
        this.type = type;
    }

    @Override
    protected float sizeX(TesseractTile tile){
        return 144;
    }

    @Override
    protected float sizeY(TesseractTile tile){
        return 65;
    }

    @Override
    protected void addWidgets(TesseractTile tile){
        this.nameField = this.addWidget(new TextFieldWidget(7, 21, 107, 18, "", CHANNEL_MAX_CHARACTERS, this::checkChannelName));
        this.nameField.setSuggestion(ClientUtils.translate("gui.tesseract.add.suggestion"));
        this.addButton = this.addWidget(new TesseractButton(8, 43, 61, 14, TextComponents.translation("gui.tesseract.add.add").get(), this::addChannel));
        this.addButton.active = false;
        TesseractButton cancelButton = this.addWidget(new TesseractButton(75, 43, 61, 14, TextComponents.translation("gui.tesseract.add.cancel").get(),
            () -> ClientUtils.displayScreen(new TesseractScreen(this.tilePos))));
        cancelButton.setRedBackground();
        this.lockButton = this.addWidget(new LockButton(117, 20));
        this.lockButton.setLocked(true);
    }

    @Override
    protected void render(PoseStack matrixStack, int mouseX, int mouseY, TesseractTile tile){
        ScreenUtils.bindTexture(BACKGROUND);
        ScreenUtils.drawTexture(matrixStack, 0, 0, this.sizeX(), this.sizeY());

        ScreenUtils.drawCenteredString(matrixStack, this.title, 72, 6, 0xffffffff);
    }

    @Override
    protected void renderTooltips(PoseStack matrixStack, int mouseX, int mouseY, TesseractTile tile){
    }

    private boolean checkChannelName(String name){
        name = name.trim();
        if(name.isEmpty() || name.length() < CHANNEL_MIN_CHARACTERS || name.length() > CHANNEL_MAX_CHARACTERS + 1){
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
        ClientUtils.displayScreen(new TesseractScreen(this.tilePos));
    }
}
