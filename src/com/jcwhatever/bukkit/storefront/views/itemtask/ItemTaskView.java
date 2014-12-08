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


package com.jcwhatever.bukkit.storefront.views.itemtask;

import com.jcwhatever.bukkit.generic.views.IView;
import com.jcwhatever.bukkit.generic.views.IViewFactory;
import com.jcwhatever.bukkit.generic.views.IViewSession;
import com.jcwhatever.bukkit.generic.views.data.ViewArgumentKey;
import com.jcwhatever.bukkit.generic.views.data.ViewArguments;
import com.jcwhatever.bukkit.generic.views.data.ViewCloseReason;
import com.jcwhatever.bukkit.generic.views.data.ViewOpenReason;
import com.jcwhatever.bukkit.generic.views.data.ViewResults;
import com.jcwhatever.bukkit.generic.views.menu.MenuItem;
import com.jcwhatever.bukkit.storefront.meta.ViewTaskMode;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil.PriceType;
import com.jcwhatever.bukkit.storefront.views.AbstractMenuView;
import com.jcwhatever.bukkit.storefront.views.price.PriceViewResult;
import com.jcwhatever.bukkit.storefront.views.quantity.QuantityViewResult;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemTaskView extends AbstractMenuView {

    public static final ViewArgumentKey<ItemStack>
            ITEM_STACK = new ViewArgumentKey<>(ItemStack.class);

    public static final ViewArgumentKey<Integer>
            INITIAL_QUANTITY = new ViewArgumentKey<Integer>(Integer.class);

    public static final ViewArgumentKey<Integer>
            MAX_QUANTITY = new ViewArgumentKey<Integer>(Integer.class);

    public static final ViewArgumentKey<Double>
            INITIAL_PRICE = new ViewArgumentKey<Double>(Double.class);

    private static final int SLOT_ITEM = 0;
    private static final int SLOT_QTY = 3;
    private static final int SLOT_PRICE = 4;
    private static final int SLOT_REMOVE = 7;
    private static final int SLOT_CANCEL = 8;

    private MenuItem _qtyMenuItem;
    private MenuItem _priceMenuItem;
    private MenuItem _removeMenuItem;
    private MenuItem _cancelMenuItem;

    private ItemTaskResult _result;
    private ItemStack _itemStack;
    private MenuItem _menuItem;
    private int _maxQty;
    private int _qty;
    private double _price;

    private List<MenuItem> _menuItems;

    protected ItemTaskView(IViewSession session,
                           IViewFactory factory, ViewArguments arguments) {

        super(session, factory, arguments);

        ViewTaskMode taskMode = getTaskMode();

        setTitle(taskMode.getChatColor() + "Select Item Task");

        _itemStack = getRequiredArgument(arguments, ITEM_STACK);

        Double price = arguments.get(INITIAL_PRICE);
        _price = price != null
                ? price
                : 1.0D;

        Integer qty = arguments.get(INITIAL_QUANTITY);
        _qty = qty != null
                ? qty
                : 1;

        Integer maxQty = arguments.get(MAX_QUANTITY);
        _maxQty = maxQty != null
                ? maxQty
                : 64;

        // setup results
        _result = new ItemTaskResult(_price, _qty);
        setResults(_result);
    }

    @Override
    protected List<MenuItem> createMenuItems() {

        if (_menuItems != null)
            return _menuItems;

        _menuItems = new ArrayList<>(5);

        _qtyMenuItem = new MenuItem(SLOT_QTY, this);
        _qtyMenuItem.setItemStack(new ItemStack(Material.CHEST));
        _qtyMenuItem.setTitle(ChatColor.LIGHT_PURPLE + "QUANTITY");
        _qtyMenuItem.setDescription("Set the number of items in the stack.");
        _menuItems.add(_qtyMenuItem);

        _priceMenuItem = new MenuItem(SLOT_PRICE, this);
        _priceMenuItem.setItemStack(new ItemStack(Material.GOLD_NUGGET));
        _priceMenuItem.setTitle(ChatColor.YELLOW + "PRICE");
        _priceMenuItem.setDescription("Set the price per item in the stack.");
        _menuItems.add(_priceMenuItem);

        _removeMenuItem = new MenuItem(SLOT_REMOVE, this);
        _removeMenuItem.setItemStack(new ItemStack(Material.SHEARS));
        _removeMenuItem.setTitle(ChatColor.DARK_RED + "REMOVE");
        _removeMenuItem.setDescription(ChatColor.RED + "Click to remove item.");
        _menuItems.add(_removeMenuItem);

        _cancelMenuItem = new MenuItem(SLOT_CANCEL, this);
        _cancelMenuItem.setItemStack(new ItemStack(Material.REDSTONE_BLOCK));
        _cancelMenuItem.setTitle(ChatColor.RED + "CANCEL");
        _cancelMenuItem.setDescription(ChatColor.RED + "Click to cancel changes and return.");
        _menuItems.add(_cancelMenuItem);

        // setup menu item
        ItemStack itemClone = _itemStack.clone();
        itemClone.setAmount(_qty);

        _menuItem = new MenuItem(SLOT_ITEM, this);
        _menuItem.setItemStack(itemClone);
        _menuItems.add(_menuItem);

        setLore(itemClone);

        return _menuItems;
    }

    @Override
    protected void onItemSelect(MenuItem menuItem) {

        if (menuItem == _qtyMenuItem) {
            showQuantityView(_itemStack, _qty, _maxQty, _price);
        }
        else if (menuItem == _priceMenuItem) {
            showPriceView(_itemStack, _price);
        }

        else if (menuItem == _removeMenuItem) {
            _result.setQty(0);
            _result.setCancelled(false);
            close(ViewCloseReason.PREV);
        }
        else if (menuItem == _cancelMenuItem) {
            _result.setCancelled(true);
            close(ViewCloseReason.PREV);
        }
        else {
            _result.setCancelled(false);
            close(ViewCloseReason.PREV);
        }
    }

    @Override
    protected void onShow(ViewOpenReason reason) {

        if (reason != ViewOpenReason.PREV) {
            return;
        }

        IView next = getViewSession().getNextView();
        if (next == null)
            return;

        ViewResults result = next.getResults();
        if (result == null)
            return;

        if (result instanceof QuantityViewResult) {
            QuantityViewResult quantityResult = (QuantityViewResult)result;

            _qty = quantityResult.getQty();
            _result.setQty(_qty);
        }
        else if (result instanceof PriceViewResult) {
            PriceViewResult priceResult = (PriceViewResult)result;

            _price = priceResult.getPrice(_price);
            _result.setPrice(_price);
        }

        _menuItem.getItemStack().setAmount(_qty);

        setLore(_menuItem.getItemStack());

        _menuItem.set();
    }

    @Override
    protected void onClose(ViewCloseReason reason) {

    }

    private void setLore(ItemStack itemStack) {
        ItemStackUtil.removeTempLore(itemStack);

        if (_price > 0)
            ItemStackUtil.setPriceLore(itemStack, _price * itemStack.getAmount(), PriceType.TOTAL);

        ItemStackUtil.addTempLore(itemStack, ChatColor.YELLOW + "Available: " + ChatColor.GRAY
                + (_maxQty - itemStack.getAmount()));

        ItemStackUtil.addTempLore(itemStack, ChatColor.BLUE + "Click to confirm changes.");
    }
}
