/* This file is part of Storefront for Bukkit, licensed under the MIT License (MIT).
 *
 * Copyright (c) JCThePants (www.jcwhatever.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */


package com.jcwhatever.bukkit.storefront.views;

import com.jcwhatever.bukkit.storefront.meta.ViewSessionTask;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil.PriceType;
import com.jcwhatever.nucleus.utils.MetaKey;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.views.ViewOpenReason;
import com.jcwhatever.nucleus.views.menu.MenuItem;
import com.jcwhatever.nucleus.views.menu.MenuItemBuilder;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

/**
 * A menu view used to select the price of an item.
 */
public class PriceView extends AbstractMenuView {

    private static final MetaKey<Double>
            PRICE_INCREMENT = new MetaKey<Double>(Double.class);

    private static final int SLOT_ITEM = 0;
    private static final int SLOT_SUBTRACT_1 = 3;
    private static final int SLOT_SUBTRACT_10 = 4;
    private static final int SLOT_SUBTRACT_50 = 5;
    private static final int SLOT_ADD_50 = 6;
    private static final int SLOT_ADD_10 = 7;
    private static final int SLOT_ADD_1 = 8;

    private MenuItem _itemToPrice;
    private MenuItem _minus1;
    private MenuItem _minus10;
    private MenuItem _minus50;
    private MenuItem _add50;
    private MenuItem _add10;
    private MenuItem _add1;

    private ItemStack _item;
    private double _price;
    private Double _selectedPrice;

    /**
     * Constructor.
     *
     * @param itemToPrice   The {@link org.bukkit.inventory.ItemStack} to be priced.
     *
     * @param initialPrice  The initial price.
     */
    public PriceView(ItemStack itemToPrice, double initialPrice) {
        PreCon.notNull(itemToPrice);

        _item = itemToPrice;
        _price = initialPrice;
    }

    /**
     * Get the {@link org.bukkit.inventory.ItemStack} that is being priced.
     */
    public ItemStack getItemToPrice() {
        return _item;
    }

    /**
     * Get the selected price.
     *
     * @return  The selected price or null if not selected.
     */
    @Nullable
    public Double getSelectedPrice() {
        return _selectedPrice;
    }

    @Override
    public String getTitle() {
        ViewSessionTask task = getSessionTask();
        return task.getChatColor() + "Set price per item";
    }

    @Override
    protected List<MenuItem> createMenuItems() {

        List<MenuItem> menuItems = new ArrayList<>(10);

        // -1 btn
        _minus1 = new MenuItemBuilder(Material.STONE)
                .title("{RED}Subtract 1")
                .description("Subtract 1.00 from the price of the item.")
                .meta(PRICE_INCREMENT, -1.0D)
                .build(SLOT_SUBTRACT_1);
        menuItems.add(_minus1);

        // -10 btn
        _minus10 = new MenuItemBuilder(Material.DIRT)
                .title("{RED}Subtract 10")
                .description("Subtract 10.00 from the price of the item.")
                .meta(PRICE_INCREMENT, -10.0D)
                .build(SLOT_SUBTRACT_10);
        menuItems.add(_minus10);

        // -50 btn
        _minus50 = new MenuItemBuilder(Material.GRASS)
                .title("{RED}Subtract 50")
                .description("Subtract 50.00 from the price of the item.")
                .meta(PRICE_INCREMENT, -50.0D)
                .build(SLOT_SUBTRACT_50);
        menuItems.add(_minus50);

        // +50 btn
        _add50 = new MenuItemBuilder(Material.DIAMOND_BLOCK)
                .title("{GREEN}Add 50")
                .description("Add 50.00 to the price of the item.")
                .meta(PRICE_INCREMENT, 50.0D)
                .build(SLOT_ADD_50);
        menuItems.add(_add50);

        // +10 btn
        _add10 = new MenuItemBuilder(Material.GOLD_BLOCK)
                .title("{GREEN}Add 10")
                .description("Add 10.00 to the price of the item.")
                .meta(PRICE_INCREMENT, 10.0D)
                .build(SLOT_ADD_10);
        menuItems.add(_add10);

        // +1 btn
        _add1 = new MenuItemBuilder(Material.IRON_BLOCK)
                .title("{GREEN}Add 1")
                .description("Add 1.00 to the price of the item.")
                .meta(PRICE_INCREMENT, 1.0D)
                .build(SLOT_ADD_1);
        menuItems.add(_add1);

        _itemToPrice = new MenuItemBuilder(_item)
                .amount(1)
                .build(SLOT_ITEM);
        menuItems.add(_itemToPrice);

        return menuItems;
    }

    @Override
    protected void onItemSelect(MenuItem menuItem) {

        if (menuItem == _itemToPrice) {
            getViewSession().previous();
            return;
        }

        Double increment = menuItem.getMeta(PRICE_INCREMENT);
        if (increment == null)
            return;

        incrementPrice(increment);
        updateItemVisibility();
    }

    @Override
    protected void onShow(ViewOpenReason reason) {
        updateItemVisibility();
    }

    /*
     * Increment the price of the item by the specified amount (can be negative).
     * Updates variables and item lore.
     */
    private void incrementPrice (double increment) {

        _price += increment;

        // prevent values below 1.00
        _price = Math.max(1.0D, _price);

        setLore(_itemToPrice);

        _itemToPrice.set(this);

        _selectedPrice = _price;

        ItemStackUtil.setPriceLore(_add1, _price, PriceType.PER_ITEM);
        ItemStackUtil.setPriceLore(_add10, _price, PriceType.PER_ITEM);
        ItemStackUtil.setPriceLore(_add50, _price, PriceType.PER_ITEM);
        ItemStackUtil.setPriceLore(_minus1, _price, PriceType.PER_ITEM);
        ItemStackUtil.setPriceLore(_minus10, _price, PriceType.PER_ITEM);
        ItemStackUtil.setPriceLore(_minus50, _price, PriceType.PER_ITEM);

        _add1.set(this);
        _add10.set(this);
        _add50.set(this);
        _minus1.set(this);
        _minus10.set(this);
        _minus50.set(this);
    }

    /*
     * Update the visibility of the price controls.
     */
    private void updateItemVisibility() {

        _minus1.setVisible(this, _price > 1);
        _minus10.setVisible(this, _price > 1);
        _minus50.setVisible(this, _price > 1);

        _add1.setVisible(this, true);
        _add10.setVisible(this, true);
        _add50.setVisible(this, true);
    }

    /*
     * Set the item lore. (price, instructions)
     */
    private void setLore(ItemStack itemStack) {
        ItemStackUtil.removeTempLore(itemStack);
        ItemStackUtil.setPriceLore(itemStack, _price, PriceType.PER_ITEM);
        ItemStackUtil.addTempLore(itemStack, ChatColor.GREEN + "Click to confirm changes.");
    }
}
