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


package com.jcwhatever.bukkit.storefront.views.price;

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

public class PriceView extends AbstractMenuView {

    public static final ViewArgumentKey<ItemStack>
            ITEM_STACK = new ViewArgumentKey<>(ItemStack.class);

    public static final ViewArgumentKey<Double>
            INITIAL_PRICE = new ViewArgumentKey<>(Double.class);

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
    private double _price = 1.0D;

    private PriceViewResult _result;

    private MenuItem _minus1;
    private MenuItem _minus10;
    private MenuItem _minus50;
    private MenuItem _add50;
    private MenuItem _add10;
    private MenuItem _add1;

    public PriceView(ViewSession session, IViewFactory factory, ViewArguments arguments) {
        super(session, factory, arguments);
    }

    @Override
    protected List<MenuItem> createMenuItems() {

        List<MenuItem> menuItems = new ArrayList<>(10);

        // -1 btn
        _minus1 = new MenuItem(SLOT_SUBTRACT_1);
        _minus1.setItemStack(new ItemStack(Material.STONE));
        _minus1.setTitle(ChatColor.RED + "Subtract 1");
        _minus1.setDescription("Subtract 1.00 from the price of the item.");
        _minus1.setMeta(PRICE_INCREMENT, -1.0D);
        menuItems.add(_minus1);

        // -10 btn
        _minus10 = new MenuItem(SLOT_SUBTRACT_10);
        _minus10.setItemStack(new ItemStack(Material.DIRT));
        _minus10.setTitle(ChatColor.RED + "Subtract 10");
        _minus10.setDescription("Subtract 10.00 from the price of the item.");
        _minus10.setMeta(PRICE_INCREMENT, -10.0D);
        menuItems.add(_minus10);

        // -50 btn
        _minus50 = new MenuItem(SLOT_SUBTRACT_50);
        _minus50.setItemStack(new ItemStack(Material.GRASS));
        _minus50.setTitle(ChatColor.RED + "Subtract 50");
        _minus50.setDescription("Subtract 50.00 from the price of the item.");
        _minus50.setMeta(PRICE_INCREMENT, -50.0D);
        menuItems.add(_minus50);

        // +50 btn
        _add50 = new MenuItem(SLOT_ADD_50);
        _add50.setItemStack(new ItemStack(Material.DIAMOND_BLOCK));
        _add50.setTitle(ChatColor.GREEN + "Add 50");
        _add50.setDescription("Add 50.00 to the price of the item.");
        _add50.setMeta(PRICE_INCREMENT, 50.0D);
        menuItems.add(_add50);

        // +10 btn
        _add10 = new MenuItem(SLOT_ADD_10);
        _add10.setItemStack(new ItemStack(Material.GOLD_BLOCK));
        _add10.setTitle(ChatColor.GREEN + "Add 10");
        _add10.setDescription("Add 10.00 to the price of the item.");
        _add10.setMeta(PRICE_INCREMENT, 10.0D);
        menuItems.add(_add10);

        // +1 btn
        _add1 = new MenuItem(SLOT_ADD_1);
        _add1.setItemStack(new ItemStack(Material.IRON_BLOCK));
        _add1.setTitle(ChatColor.GREEN + "Add 1");
        _add1.setDescription("Add 1.00 to the price of the item.");
        _add1.setMeta(PRICE_INCREMENT, 1.0D);
        menuItems.add(_add1);


        ViewTaskMode taskMode = getViewSession().getMeta(SessionMetaKey.TASK_MODE);
        PreCon.notNull(taskMode);

        setTitle(taskMode.getChatColor() + "Set price per item");

        ItemStack itemToPrice = getArguments().get(ITEM_STACK);
        if (itemToPrice == null)
            throw new IllegalStateException("ITEM_STACK view argument is required.");

        Double price = getArguments().get(INITIAL_PRICE);
        if (price != null)
            _price = price;

        _itemToPrice = new MenuItem(SLOT_ITEM);
        _itemToPrice.setItemStack(itemToPrice.clone());
        _itemToPrice.getItemStack().setAmount(1);
        menuItems.add(_itemToPrice);

        _result = new PriceViewResult(itemToPrice, _price);
        _result.setCancelled(true);

        return menuItems;
    }

    @Override
    protected void onItemSelect(MenuItem menuItem) {

        Double increment = menuItem.getMeta(PRICE_INCREMENT);
        if (increment == null)
            return;

        incrementPrice(increment);
        updateItemVisibility();
    }

    @Override
    protected void onShow(ViewOpenReason reason) {

    }

    @Override
    protected void onClose(ViewCloseReason reason) {

    }

    private void incrementPrice (double increment) {

        _price += increment;

        // prevent values below 1.00
        _price = Math.max(1.0D, _price);

        setLore(_itemToPrice.getItemStack());

        _itemToPrice.set();

        _result.setPrice(_price);

        ItemStackUtil.setPriceLore(_add1.getItemStack(), _price, PriceType.PER_ITEM);
        ItemStackUtil.setPriceLore(_add10.getItemStack(), _price, PriceType.PER_ITEM);
        ItemStackUtil.setPriceLore(_add50.getItemStack(), _price, PriceType.PER_ITEM);
        ItemStackUtil.setPriceLore(_minus1.getItemStack(), _price, PriceType.PER_ITEM);
        ItemStackUtil.setPriceLore(_minus10.getItemStack(), _price, PriceType.PER_ITEM);
        ItemStackUtil.setPriceLore(_minus50.getItemStack(), _price, PriceType.PER_ITEM);
    }

    private boolean setItemVisible(MenuItem item, boolean isVisible) {
        if (item.isVisible() == isVisible)
            return false;

        item.setVisible(isVisible);
        return true;
    }

    private void updateItemVisibility() {

        _minus1.setVisible(_price > 1);
        _minus10.setVisible(_price > 1);
        _minus50.setVisible(_price > 1);

        _add1.setVisible(true);
        _add10.setVisible(true);
        _add50.setVisible(true);
    }

    private void setLore(ItemStack itemStack) {
        ItemStackUtil.removeTempLore(itemStack);
        ItemStackUtil.setPriceLore(itemStack, _price, PriceType.PER_ITEM);
        ItemStackUtil.addTempLore(itemStack, ChatColor.GREEN + "Click to confirm changes.");
    }

}
