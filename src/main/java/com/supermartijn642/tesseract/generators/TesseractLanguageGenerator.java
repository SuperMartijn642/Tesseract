package com.supermartijn642.tesseract.generators;

import com.supermartijn642.core.generator.LanguageGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.tesseract.Tesseract;

/**
 * Created 23/12/2022 by SuperMartijn642
 */
public class TesseractLanguageGenerator extends LanguageGenerator {

    public TesseractLanguageGenerator(ResourceCache cache){
        super("tesseract", cache, "en_us");
    }

    @Override
    public void generate(){
        // Tesseract item
        this.block(Tesseract.tesseract, "Tesseract");
        this.translation("tesseract.tesseract.info", "Items, fluids, and energy can be transferred between multiple tesseracts");

        // Screen
        this.translation("gui.tesseract.title", "Tesseract");
        this.translation("gui.tesseract.items", "TESSERACT - ITEMS");
        this.translation("gui.tesseract.fluid", "TESSERACT - FLUID");
        this.translation("gui.tesseract.energy", "TESSERACT - ENERGY");
        this.translation("gui.tesseract.type.items", "Items");
        this.translation("gui.tesseract.type.fluid", "Fluids");
        this.translation("gui.tesseract.type.energy", "Energy");
        this.translation("gui.tesseract.set", "Activate");
        this.translation("gui.tesseract.unset", "Deactivate");
        this.translation("gui.tesseract.remove", "Remove");
        this.translation("gui.tesseract.add", "Create");
        this.translation("gui.tesseract.transfer.send", "SEND ONLY");
        this.translation("gui.tesseract.transfer.receive", "RECEIVE ONLY");
        this.translation("gui.tesseract.transfer.both", "SEND AND RECEIVE");
        this.translation("gui.tesseract.transfer.speech", "Transfer Mode: %s");
        this.translation("gui.tesseract.redstone.disabled", "DISABLED");
        this.translation("gui.tesseract.redstone.high", "HIGH");
        this.translation("gui.tesseract.redstone.low", "LOW");
        this.translation("gui.tesseract.redstone.speech", "Redstone: %s");
        this.translation("gui.tesseract.channel.private", "Private");
        this.translation("gui.tesseract.channel.public", "Public");
        this.translation("gui.tesseract.add.title.items", "Create Channel - ITEMS");
        this.translation("gui.tesseract.add.title.fluid", "Create Channel - FLUID");
        this.translation("gui.tesseract.add.title.energy", "Create Channel - ENERGY");
        this.translation("gui.tesseract.add.suggestion", "Channel name");
        this.translation("gui.tesseract.add.add", "Create");
        this.translation("gui.tesseract.add.cancel", "Cancel");
        this.translation("gui.tesseract.info_button", "Information (Coming soon)");
        this.translation("gui.tesseract.info.title", "Information");
        this.translation("gui.tesseract.info.back", "Back");
        this.translation("gui.tesseract.info.forward", "Next");
        this.translation("gui.tesseract.info.narrate_page", "Page %s");
        this.translation("gui.tesseract.info.tab.gui", "Tesseract Screen");
        this.translation("gui.tesseract.info.tab.items", "Item Transport");
        this.translation("gui.tesseract.info.tab.fluid", "Fluid Transport");
        this.translation("gui.tesseract.info.tab.energy", "Energy Transport");
    }
}
