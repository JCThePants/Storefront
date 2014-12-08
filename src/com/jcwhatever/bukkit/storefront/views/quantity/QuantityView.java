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


package com.jcwhatever.bukkit.storefront.views.quantity;

import com.jcwhatever.bukkit.generic.utils.MetaKey;
import com.jcwhatever.bukkit.generic.utils.PreCon;
import com.jcwhatever.bukkit.generic.views.IViewFactory;
import com.jcwhatever.bukkit.generic.views.ViewSession;
import com.jcwhatever.bukkit.generic.views.data.ViewArgumentKey;
import com.jcwhatever.bukkit.generic.views.data.ViewArguments;
import com.jcwhatever.bukkit.generic.views.data.ViewCloseReason;
import com.jcwhatever.bukkit.generic.views.data.ViewOpenReason;
import com.jcwhatever.bukkit.generic.views.menu.MenuItem;
import com.jcwhatever.bukkit.storefront.meta.SessionMetaKey;
import com.jcwhatever.bukkit.storefront.meta.ViewTaskMode;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil.PriceType;
import com.jcwhatever.bukkit.storefront.views.AbstractMenuView;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class QuantityView extends AbstractMenuView {

    public static final ViewArgumentKey<ItemStack>
            ITEM_STACK = new ViewArgumentKey<>(ItemStack.class);

    public static final ViewArgumentKey<Integer>
            INITIAL_QUANTITY = new ViewArgumentKey<>(Integer.class);

    public static final ViewArgumentKey<Integer>
            MAX_QUANTITY = new ViewArgumentKey<>(Integer.class);

    public static final ViewArgumentKey<Double>
            PRICE = new ViewArgumentKey<>(Double.class);

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

    private double _price = 0.0D;
    private int _maxQty = 64;
    private MenuItem _itemToQuantify;
    private QuantityViewResult _result;

    private List<MenuItem> _menuItems;

    protected QuantityView(ViewSession session,
                           IViewFactory factory, ViewArguments arguments) {
        super(session, factory, arguments);

    }

    @Override
    protected List<MenuItem> createMenuItems() {

        if (_menuItems != null)
            return _menuItems;

        ViewTaskMode taskMode = getViewSession().getMeta(SessionMetaKey.TASK_MODE);
        PreCon.notNull(taskMode);

        setTitle(taskMode.getChatColor() + "Select Quantity");
        ItemStack itemStack = getArguments().get(ITEM_STACK);
        if (itemStack == null)
            throw new IllegalStateException("ITEM_STACK argument is required.");

        Integer maxQty = getArguments().get(MAX_QUANTITY);
        _maxQty = maxQty != null ? maxQty : 64;

        Integer qty = getArguments().get(INITIAL_QUANTITY);
        if (qty == null) {
            qty = 1;
        }

        Double price = getArguments().get(PRICE);
        if (price != null) {
            _price = price;
        }

        _result = new QuantityViewResult(itemStack, qty);
        _result.setCancelled(true);


        _menuItems = new ArrayList<>(6);

        _menuCancel = new MenuItem(SLOT_CANCEL);
        _menuCancel.setItemStack(new ItemStack(Material.REDSTONE_BLOCK));
        _menuCancel.setTitle(ChatColor.RED + "Cancel");
        _menuCancel.setDescription(ChatColor.RED + "Click to cancel and return.");
        _menuItems.add(_menuCancel);

        _menuSubtract1 = new MenuItem(SLOT_SUBTRACT_1);
        _menuSubtract1.setItemStack(new ItemStack(Material.STONE));
        _menuSubtract1.setTitle(ChatColor.RED + "Subtract 1");
        _menuSubtract1.setDescription("Click to Subtract 1 item from the quantity.");
        _menuSubtract1.setMeta(QUANTITY_INCREMENT, -1);
        _menuItems.add(_menuSubtract1);

        _menuSubtract10 = new MenuItem(SLOT_SUBTRACT_10);
        _menuSubtract10.setItemStack(new ItemStack(Material.DIRT));
        _menuSubtract10.setTitle(ChatColor.RED + "Subtract 10");
        _menuSubtract10.setDescription("Click to Subtract 10 items from the quantity.");
        _menuSubtract10.setMeta(QUANTITY_INCREMENT, -10);
        _menuItems.add(_menuSubtract10);

        _menuAdd1 = new MenuItem(SLOT_ADD_1);
        _menuAdd1.setItemStack(new ItemStack(Material.IRON_BLOCK));
        _menuAdd1.setTitle(ChatColor.GREEN + "Add 1");
        _menuAdd1.setDescription("Click to Add 1 item to the quantity.");
        _menuAdd1.setMeta(QUANTITY_INCREMENT, 1);
        _menuItems.add(_menuAdd1);

        _menuAdd10 = new MenuItem(SLOT_ADD_10);
        _menuAdd10.setItemStack(new ItemStack(Material.GOLD_BLOCK));
        _menuAdd10.setTitle(ChatColor.GREEN + "Add 10");
        _menuAdd10.setDescription("Click to Add 10 item to the quantity.");
        _menuAdd10.setMeta(QUANTITY_INCREMENT, 10);
        _menuItems.add(_menuAdd10);

        _itemToQuantify = new MenuItem(SLOT_ITEM);
        _itemToQuantify.setItemStack(itemStack.clone());
        _itemToQuantify.getItemStack().setAmount(qty);
        _menuItems.add(_itemToQuantify);

        return _menuItems;
    }

    @Override
    protected void onItemSelect(MenuItem menuItem) {

        if (menuItem == _itemToQuantify) {
            _result.setCancelled(false);
            getPlayer().closeInventory();
            return;
        }
        else if (menuItem == _menuCancel) {
            _result.setCancelled(true);
            getPlayer().closeInventory();
            return;
        }

        Integer increment = menuItem.getMeta(QUANTITY_INCREMENT);
        if (increment == null)
            return;

        int qty = Math.max(1, _result.getQty() + increment);
        int max = Math.min(64, _maxQty);
        qty = Math.min(qty, max);

        _result.setQty(qty);

        ItemStack item = getInventoryView().getTopInventory().getItem(0);

        item.setAmount(qty);

        setLore(item);

        updateItemVisibility();
    }

    @Override
    protected void onShow(ViewOpenReason reason) {

    }

    @Override
    protected void onClose(ViewCloseReason reason) {

    }

    private void updateItemVisibility() {

        ItemStack item = getInventoryView().getTopInventory().getItem(0);

        int qty = item.getAmount();

        _menuSubtract1.setVisible(qty > 1);
        _menuSubtract10.setVisible(qty > 1);

        _menuAdd1.setVisible(qty < _maxQty);
        _menuAdd10.setVisible(qty < _maxQty);
    }

    private void setLore(ItemStack itemStack) {

        ItemStackUtil.removeTempLore(itemStack);

        if (_price > 0)
            ItemStackUtil.setPriceLore(itemStack, _price * itemStack.getAmount(), PriceType.TOTAL);

        ItemStackUtil.addTempLore(itemStack, ChatColor.YELLOW + "Qty: " + ChatColor.GRAY
                + itemStack.getAmount());

        ItemStackUtil.addTempLore(itemStack, ChatColor.YELLOW + "Available: " + ChatColor.GRAY
                + _maxQty);

        ViewTaskMode taskMode = getTaskMode();

        if (taskMode == ViewTaskMode.SERVER_BUY || taskMode == ViewTaskMode.PLAYER_BUY)
            ItemStackUtil.addTempLore(itemStack, ChatColor.BLUE + "Click to purchase.");
        else
            ItemStackUtil.addTempLore(itemStack, ChatColor.BLUE + "Click to confirm.");
    }

}
