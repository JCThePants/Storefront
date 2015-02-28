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

import com.jcwhatever.bukkit.storefront.meta.SessionMetaKey;
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
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * A menu view used to change the quantity of an item.
 */
public class QuantityView extends AbstractMenuView {

    private static final MetaKey<Integer>
            QUANTITY_INCREMENT = new MetaKey<Integer>(Integer.class);

    private static final int SLOT_ITEM = 0;
    private static final int SLOT_SUBTRACT_1 = 3;
    private static final int SLOT_SUBTRACT_10 = 4;
    private static final int SLOT_ADD_10 = 5;
    private static final int SLOT_ADD_1 = 6;
    private static final int SLOT_CANCEL = 8;

    private MenuItem _menuCancel;
    private MenuItem _menuSubtract1;
    private MenuItem _menuSubtract10;
    private MenuItem _menuAdd1;
    private MenuItem _menuAdd10;

    private MenuItem _itemToQuantify;
    private List<MenuItem> _menuItems;

    private ItemStack _item;
    private int _qty;
    private int _maxQty;
    private Integer _selectedAmount;
    private double _price;

    /**
     * Constructor.
     *
     * @param item        The {@link ItemStack} whose quantity is being changed.
     * @param initialQty  The initial quantity.
     * @param maxQty      The max quantity allowed.
     * @param price       The price of the item. Used to display total price given selected quantity.
     */
    public QuantityView(ItemStack item, int initialQty, int maxQty, double price) {
        PreCon.notNull(item);

        _item = item;
        _qty = initialQty;
        _maxQty = Math.min(64, maxQty);
        _price = price;
    }

    /**
     * Get the {@link org.bukkit.inventory.ItemStack} whose quantity is being changed.
     */
    public ItemStack getItemStack() {
        return _item;
    }

    /**
     * Get the selected quantity.
     *
     * @return  The selected quantity or null if not selected.
     */
    public Integer getSelectedQty() {
        return _selectedAmount;
    }

    @Override
    public String getTitle() {
        return "Select Amount";
    }

    @Override
    protected List<MenuItem> createMenuItems() {

        if (_menuItems != null)
            return _menuItems;

        _selectedAmount = null;

        ViewSessionTask taskMode = getViewSession().getMeta(SessionMetaKey.TASK_MODE);
        PreCon.notNull(taskMode);

        _menuItems = new ArrayList<>(6);

        _menuCancel = new MenuItemBuilder(Material.REDSTONE_BLOCK)
                .title("{RED}Cancel")
                .description("{RED}Click to cancel and return.")
                .build(SLOT_CANCEL);
        _menuItems.add(_menuCancel);

        _menuSubtract1 = new MenuItemBuilder(Material.STONE)
                .title("{RED}Subtract 1")
                .description("Click to Subtract 1 item from the quantity.")
                .meta(QUANTITY_INCREMENT, -1)
                .build(SLOT_SUBTRACT_1);
        _menuItems.add(_menuSubtract1);

        _menuSubtract10 = new MenuItemBuilder(Material.DIRT)
                .title("{RED}Subtract 10")
                .description("Click to Subtract 10 items from the quantity.")
                .meta(QUANTITY_INCREMENT, -10)
                .build(SLOT_SUBTRACT_10);
        _menuItems.add(_menuSubtract10);

        _menuAdd1 = new MenuItemBuilder(Material.IRON_BLOCK)
                .title("{GREEN}Add 1")
                .description("Click to Add 1 item to the quantity.")
                .meta(QUANTITY_INCREMENT, 1)
                .build(SLOT_ADD_1);
        _menuItems.add(_menuAdd1);

        _menuAdd10 = new MenuItemBuilder(Material.GOLD_BLOCK)
                .title("{GREEN}Add 10")
                .description("Click to Add 10 item to the quantity.")
                .meta(QUANTITY_INCREMENT, 10)
                .build(SLOT_ADD_10);
        _menuItems.add(_menuAdd10);

        _itemToQuantify = new MenuItemBuilder(_item)
                .amount(_qty)
                .build(SLOT_ITEM);
        setLore(_itemToQuantify);
        _menuItems.add(_itemToQuantify);

        return _menuItems;
    }

    @Override
    protected void onItemSelect(MenuItem menuItem) {

        if (menuItem == _itemToQuantify) {
            getViewSession().previous();
            return;
        }
        else if (menuItem == _menuCancel) {
            // escape
            getPlayer().closeInventory();
            return;
        }

        if (_selectedAmount == null)
            _selectedAmount = 0;

        Integer increment = menuItem.getMeta(QUANTITY_INCREMENT);
        if (increment == null)
            return;

        int qty = Math.max(1, _selectedAmount + increment);
        int max = Math.min(64, _maxQty);
        qty = Math.min(qty, max);

        InventoryView view = getInventoryView();
        assert view != null;

        ItemStack item = view.getTopInventory().getItem(0);

        item.setAmount(qty);
        _selectedAmount = qty;

        setLore(item);

        updateItemVisibility();
    }

    @Override
    protected void onShow(ViewOpenReason reason) {
        updateItemVisibility();
    }

    /*
     * Update the visibility of the quantity controls.
     */
    private void updateItemVisibility() {

        InventoryView view = getInventoryView();
        assert view != null;

        ItemStack item = view.getTopInventory().getItem(0);

        int qty = item.getAmount();

        _menuSubtract1.setVisible(this, qty > 1);
        _menuSubtract10.setVisible(this, qty > 1);

        _menuAdd1.setVisible(this, qty < _maxQty);
        _menuAdd10.setVisible(this, qty < _maxQty);
    }

    /*
     * Update the item lore. (qty, availability, instructions)
     */
    private ItemStack setLore(ItemStack itemStack) {

        ItemStackUtil.removeTempLore(itemStack);

        if (_price > 0)
            ItemStackUtil.setPriceLore(itemStack, _price * itemStack.getAmount(), PriceType.TOTAL);

        ItemStackUtil.addTempLore(itemStack, ChatColor.YELLOW + "Qty: " + ChatColor.GRAY
                + itemStack.getAmount());

        ItemStackUtil.addTempLore(itemStack, ChatColor.YELLOW + "Available: " + ChatColor.GRAY
                + _maxQty);

        ViewSessionTask taskMode = getSessionTask();

        if (taskMode == ViewSessionTask.SERVER_BUY || taskMode == ViewSessionTask.PLAYER_BUY)
            ItemStackUtil.addTempLore(itemStack, ChatColor.BLUE + "Click to purchase.");
        else
            ItemStackUtil.addTempLore(itemStack, ChatColor.BLUE + "Click to confirm.");

        return itemStack;
    }
}
