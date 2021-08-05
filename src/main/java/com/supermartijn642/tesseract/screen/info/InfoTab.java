package com.supermartijn642.tesseract.screen.info;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.tesseract.Tesseract;
import com.supermartijn642.tesseract.screen.info.gui.GuiPage;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * Created 7/17/2021 by SuperMartijn642
 */
public enum InfoTab {

    GUI("gui.tesseract.info.tab.gui", () -> Tesseract.tesseract.asItem(),
        new GuiPage(72, 0, 172, 56, "gui.tesseract.info.gui.page1", true, false)),
    ITEMS("gui.tesseract.info.tab.items", () -> Items.CHEST),
    FLUID("gui.tesseract.info.tab.fluid", () -> Items.BUCKET),
    ENERGY("gui.tesseract.info.tab.energy", () -> Items.REDSTONE);

    private final String translationKey;
    private final Supplier<Item> iconItem;
    private final List<Page> pages = new ArrayList<>();
    public int currentPageIndex = 0;

    InfoTab(String translationKey, Supplier<Item> iconItem, Page... pages){
        this.translationKey = translationKey;
        this.iconItem = iconItem;
        this.pages.addAll(Arrays.asList(pages));
    }

    public Component getTranslation(){
        return TextComponents.translation(this.translationKey).get();
    }

    public ItemStack getIconItem(){
        return new ItemStack(this.iconItem.get());
    }

    public int getNumberOfPages(){
        return this.pages.size();
    }

    public Page getPage(int index){
        return this.pages.get(index);
    }

    public Page getCurrentPage(){
        return this.pages.get(this.currentPageIndex);
    }

}
