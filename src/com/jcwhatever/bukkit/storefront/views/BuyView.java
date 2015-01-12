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

import com.jcwhatever.bukkit.storefront.Msg;
import com.jcwhatever.bukkit.storefront.data.ISaleItem;
import com.jcwhatever.bukkit.storefront.data.PaginatedItems;
import com.jcwhatever.bukkit.storefront.meta.ViewSessionTask;
import com.jcwhatever.bukkit.storefront.stores.IStore;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil.PriceType;
import com.jcwhatever.nucleus.providers.economy.ICurrency.CurrencyNoun;
import com.jcwhatever.nucleus.utils.Economy;
import com.jcwhatever.nucleus.utils.MetaKey;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.inventory.InventoryUtils;
import com.jcwhatever.nucleus.utils.items.ItemStackUtils;
import com.jcwhatever.nucleus.utils.items.ItemStackUtils.DisplayNameResult;
import com.jcwhatever.nucleus.views.View;
import com.jcwhatever.nucleus.views.ViewCloseReason;
import com.jcwhatever.nucleus.views.ViewOpenReason;
import com.jcwhatever.nucleus.views.menu.MenuItem;
import com.jcwhatever.nucleus.views.menu.MenuItemBuilder;
import com.jcwhatever.nucleus.views.menu.PaginatorView;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BuyView extends AbstractMenuView {

    private static final MetaKey<ISaleItem>
            SALE_ITEM = new MetaKey<>(ISaleItem.class);

    private final IStore _store;
    private final PaginatedItems _pagin;

    private View _paginator;
    private int _page = 1;

    private ISaleItem _selectedSaleItem;

    public BuyView(PaginatedItems paginatedItems) {
        PreCon.notNull(paginatedItems);

        _store = getStore();
        _pagin = paginatedItems;
    }

    @Override
    public String getTitle() {
        ViewSessionTask taskMode = getSessionTask();
        String title = taskMode.getChatColor() + "Buy Items";

        if (_paginator != null) {
            title += " (Page " + _page + ')';
        }

        return title;
    }

    @Override
    protected List<MenuItem> createMenuItems() {

        _paginator = getViewSession().getPrevView();
        if (_paginator instanceof PaginatorView) {
            _page = ((PaginatorView) _paginator).getSelectedPage();
        }

        int totalPages = _pagin.getTotalPages();
        if (totalPages < _page)
            _page = totalPages;

        List<ISaleItem> saleItemStacks = _pagin.getPage(_page);

        List<MenuItem> menuItems = new ArrayList<>(saleItemStacks.size());

        for (int i = 0; i < saleItemStacks.size(); i++) {

            ISaleItem item = saleItemStacks.get(i);

            ItemStack stack = item.getItemStack();
            stack.setAmount(item.getQty());
            ItemStackUtil.setPriceLore(stack, item.getPricePerUnit(), PriceType.PER_ITEM);
            ItemStackUtil.setSellerLore(stack, item.getSellerId());

            MenuItem menuItem = new MenuItemBuilder(stack)
                    .meta(SALE_ITEM, item)
                    .build(i);

            menuItems.add(menuItem);
        }

        return menuItems;
    }

    @Override
    protected void onItemSelect(MenuItem menuItem) {

        ISaleItem saleItemStack = menuItem.getMeta(SALE_ITEM);
        if (saleItemStack == null)
            return;

        _selectedSaleItem = saleItemStack;

        getViewSession().next(new QuantityView(saleItemStack.getItemStack(), 1, 64, saleItemStack.getPricePerUnit()));
    }

    @Override
    protected void onShow(ViewOpenReason reason) {
        // do nothing
    }

    @Override
    protected boolean onPreShow(ViewOpenReason reason) {

        if (reason == ViewOpenReason.PREV) {

            int amount;

            View quantityView = getViewSession().getNextView();
            if (!(quantityView instanceof QuantityView)) {
                return true;
            }

            amount = ((QuantityView) quantityView).getSelectedAmount();

            double price = _selectedSaleItem.getPricePerUnit() * amount;
            double balance = Economy.getBalance(getPlayer().getUniqueId());

            // check buyer balance
            if (balance < price) {
                Msg.tell(getPlayer(), "{RED}Problem: {WHITE}You don't have enough {0}.",
                        Economy.getCurrency().getName(CurrencyNoun.PLURAL));
            }
            // check buyer available inventory room
            else if (!InventoryUtils.hasRoom(getPlayer().getInventory(), _selectedSaleItem.getItemStack(), amount)) {
                Msg.tell(getPlayer(), "{RED}Problem: {WHITE}There isn't enough space in your inventory.");
            }
            // check item is available
            else if (_selectedSaleItem.getParent().getQty() < amount) {
                Msg.tell(getPlayer(), "{RED}Problem: {WHITE}Not enough inventory. Someone may have purchased the item already.");
            }
            // buy items
            else if (!_store.buySaleItem(getPlayer(), _selectedSaleItem, amount, price)) {
                Msg.tell(getPlayer(), "{RED}Problem: {WHITE}Failed to buy items. They may have been purchased by someone else already.");
            } else {
                Msg.tell(getPlayer(), "{GREEN}Success: {WHITE}Purchased {0} {1} for {2}.", amount,
                        ItemStackUtils.getDisplayName(_selectedSaleItem.getItemStack(), DisplayNameResult.REQUIRED),
                        Economy.getCurrency().format(price));
            }

        } else {


            if (_store.getSaleItems().size() == 0) {

                Msg.tell(getPlayer(), "Out of Stock");
                return false;
            }

        }

        return true;
    }

    @Override
    protected void onClose(ViewCloseReason reason) {
        // do nothing
    }

    @Override
    protected int getSlotsRequired() {
        return MAX_SLOTS;
    }
}
