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
import com.jcwhatever.bukkit.storefront.stores.IStore;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil.PriceType;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.views.View;
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
 * A menu view used to perform tasks on an item.
 *
 * <p>Tasks include changing the quantity and price.</p>
 */
public class ItemTaskView extends AbstractMenuView {

    private static final int SLOT_ITEM = 0;
    private static final int SLOT_QTY = 3;
    private static final int SLOT_PRICE = 4;
    private static final int SLOT_REMOVE = 7;
    private static final int SLOT_CANCEL = 8;

    private MenuItem _qtyMenuItem;
    private MenuItem _priceMenuItem;
    private MenuItem _removeMenuItem;
    private MenuItem _cancelMenuItem;

    private MenuItem _menuItem;

    private List<MenuItem> _menuItems;

    private ItemStack _item;
    private double _price;
    private int _amount;
    private int _maxAmount;

    private Integer _selectedAmount;
    private Double _selectedPrice;

    /**
     * Constructor.
     *
     * @param store          The store the view is for.
     * @param item           The {@link org.bukkit.inventory.ItemStack} to change properties on.
     * @param initialPrice   The initial price of the item.
     * @param initialAmount  The initial amount of the item.
     * @param maxAmount      The max amount of the item.
     */
    public ItemTaskView(IStore store, ItemStack item, double initialPrice, int initialAmount, int maxAmount) {
        super(store);

        PreCon.notNull(item);

        _item = item;
        _price = initialPrice;
        _amount = initialAmount;
        _maxAmount = maxAmount;
    }

    /**
     * Get the amount selected by the player/viewer.
     *
     * @return  The amount or null if not selected.
     */
    @Nullable
    public Integer getSelectedAmount() {
        return _selectedAmount;
    }

    /**
     * Get the price selected by the player/viewer.
     *
     * @return  The price or null if not selected.
     */
    @Nullable
    public Double getSelectedPrice() {
        return _selectedPrice;
    }

    @Override
    public String getTitle() {
        ViewSessionTask taskMode = getSessionTask();
        return taskMode.getChatColor() + "Select Item Task";
    }

    @Override
    protected List<MenuItem> createMenuItems() {

        if (_menuItems != null)
            return _menuItems;

        _menuItems = new ArrayList<>(5);

        _qtyMenuItem = new MenuItemBuilder(Material.CHEST)
                .title("{LIGHT_PURPLE}QUANTITY")
                .description("Set the number of items in the stack.")
                .build(SLOT_QTY);
        _menuItems.add(_qtyMenuItem);

        _priceMenuItem = new MenuItemBuilder(Material.GOLD_NUGGET)
                .title("{YELLOW}PRICE")
                .description("Set the price per item in the stack.")
                .build(SLOT_PRICE);
        _menuItems.add(_priceMenuItem);

        _removeMenuItem = new MenuItemBuilder(Material.SHEARS)
                .title("{DARK_RED}REMOVE")
                .description("{RED}Click to remove item.")
                .build(SLOT_REMOVE);
        _menuItems.add(_removeMenuItem);

        _cancelMenuItem = new MenuItemBuilder(Material.REDSTONE_BLOCK)
                .title("{RED}CANCEL")
                .description("{RED}Click to cancel changes and return.")
                .build(SLOT_CANCEL);
        _menuItems.add(_cancelMenuItem);

        // setup menu item
        ItemStack itemClone = _item.clone();
        itemClone.setAmount(_amount);

        _menuItem = new MenuItemBuilder(itemClone).build(SLOT_ITEM);
        _menuItems.add(_menuItem);

        setLore(_menuItem);

        return _menuItems;
    }

    @Override
    protected void onItemSelect(MenuItem menuItem) {

        if (menuItem == _qtyMenuItem) {
            getViewSession().next(new QuantityView(getStore(), menuItem, _amount, _maxAmount, _price));
        }

        else if (menuItem == _priceMenuItem) {
            getViewSession().next(new PriceView(getStore(), menuItem, _price));
        }

        else if (menuItem == _removeMenuItem) {
            _selectedAmount = 0;
            getViewSession().previous();
        }
        else if (menuItem == _cancelMenuItem) {
            _selectedAmount = null;
            _selectedPrice = null;
            getViewSession().previous();
        }
        else {
            getViewSession().previous();
        }
    }

    @Override
    protected void onShow(ViewOpenReason reason) {

        if (reason != ViewOpenReason.PREV) {
            return;
        }

        View next = getViewSession().getNext();
        if (next == null)
            return;


        if (next instanceof QuantityView) {
            QuantityView quantityView = (QuantityView)next;

            Integer amount = quantityView.getSelectedQty();
            if (amount != null) {
                _amount = amount;
                _selectedAmount = amount;
            }
        }
        else if (next instanceof PriceView) {
            PriceView priceView = (PriceView)next;

            Double price = priceView.getSelectedPrice();
            if (price != null) {
                _price = price;
                _selectedPrice = price;
            }
        }

        _menuItem.setAmount(_amount);

        setLore(_menuItem);

        _menuItem.setVisible(this, true);
    }

    /*
     * Set an ItemStack's lore. (price, availability, instructions)
     */
    private void setLore(ItemStack itemStack) {
        ItemStackUtil.removeTempLore(itemStack);

        if (_price > 0)
            ItemStackUtil.setPriceLore(itemStack, _price * itemStack.getAmount(), PriceType.TOTAL);

        ItemStackUtil.addTempLore(itemStack, ChatColor.YELLOW + "Available: " + ChatColor.GRAY
                + (_maxAmount - itemStack.getAmount()));

        ItemStackUtil.addTempLore(itemStack, ChatColor.BLUE + "Click to confirm changes.");
    }
}
