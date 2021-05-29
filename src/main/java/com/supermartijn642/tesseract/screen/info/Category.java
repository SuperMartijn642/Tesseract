package com.supermartijn642.tesseract.screen.info;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 4/18/2021 by SuperMartijn642
 */
public class Category {

    public static final List<Category> CATEGORIES = new ArrayList<>();
    static {
        // TODO create categories and pages
    }

    private final List<Page> pages = new ArrayList<>();

    public int getPageCount(){
        return this.pages.size();
    }

    public Page getPage(int index){
        return index >= 0 && index < this.pages.size() ? this.pages.get(index) : null;
    }

}
