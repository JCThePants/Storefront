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


package com.jcwhatever.bukkit.storefront.views.buy;

import com.jcwhatever.nucleus.providers.economy.IEconomyProvider.CurrencyNoun;
import com.jcwhatever.nucleus.utils.EconomyUtils;
import com.jcwhatever.nucleus.utils.inventory.InventoryUtils;
import com.jcwhatever.nucleus.utils.items.ItemStackUtils;
import com.jcwhatever.nucleus.utils.items.ItemStackUtils.DisplayNameResult;
import com.jcwhatever.nucleus.utils.MetaKey;
import com.jcwhatever.nucleus.views.IViewFactory;
import com.jcwhatever.nucleus.views.ViewSession;
import com.jcwhatever.nucleus.views.data.ViewArguments;
import com.jcwhatever.nucleus.views.data.ViewCloseReason;
import com.jcwhatever.nucleus.views.data.ViewOpenReason;
import com.jcwhatever.nucleus.views.data.ViewResults;
import com.jcwhatever.nucleus.views.menu.MenuItem;
import com.jcwhatever.nucleus.views.menu.PaginatorView;
import com.jcwhatever.bukkit.storefront.Category;
import com.jcwhatever.bukkit.storefront.Msg;
import com.jcwhatever.bukkit.storefront.data.ISaleItem;
import com.jcwhatever.bukkit.storefront.data.PaginatedItems;
import com.jcwhatever.bukkit.storefront.meta.ViewTaskMode;
import com.jcwhatever.bukkit.storefront.stores.IStore;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil.PriceType;
import com.jcwhatever.bukkit.storefront.views.AbstractMenuView;
import com.jcwhatever.bukkit.storefront.views.quantity.QuantityViewResult;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BuyView extends AbstractMenuView {

    private static final MetaKey<ISaleItem>
            SALE_ITEM = new MetaKey<>(ISaleItem.class);

    private IStore _store;

    public BuyView(ViewSession session, IViewFactory factory, ViewArguments arguments) {
        super(session, factory, arguments);

        _store = getStore();
    }

    @Override
    protected List<MenuItem> createMenuItems() {

        ViewTaskMode taskMode = getTaskMode();

        Integer page = getArguments().get(PaginatorView.SELECTED_PAGE);

        PaginatedItems pagin = (PaginatedItems)getArguments().get(PaginatorView.PAGINATOR);

        if (page == null) {
            page = 1;
        }

        if (pagin == null) {
            Category category = getCategory();
            pagin = new PaginatedItems(_store, category);
        }

        int totalPages = pagin.getTotalPages();
        if (totalPages < page)
            page = totalPages;

        setTitle(taskMode.getChatColor() + "Buy Items (Page " + page + ')');

        List<ISaleItem> saleItemStacks = pagin.getPage(page);

        List<MenuItem> menuItems = new ArrayList<>(saleItemStacks.size());

        for (int i = 0; i < saleItemStacks.size(); i++) {

            ISaleItem item = saleItemStacks.get(i);

            MenuItem menuItem = new MenuItem(i);
            menuItem.setMeta(SALE_ITEM, item);

            ItemStack stack = item.getItemStack();
            stack.setAmount(item.getQty());
            ItemStackUtil.setPriceLore(stack, item.getPricePerUnit(), PriceType.PER_ITEM);
            ItemStackUtil.setSellerLore(stack, item.getSellerId());

            menuItem.setItemStack(stack);

            menuItems.add(menuItem);
        }

        return menuItems;
    }

    @Override
    protected void onItemSelect(MenuItem menuItem) {

        ISaleItem saleItemStack = menuItem.getMeta(SALE_ITEM);
        if (saleItemStack == null)
            return;

        showQuantityView(saleItemStack, 1);
    }

    @Override
    protected void onShow(ViewOpenReason reason) {
        // do nothing
    }

    @Override
    protected void onPreShow(ViewOpenReason reason) {
        if (reason != ViewOpenReason.PREV)
            return;

        ViewResults viewResults = getViewSession().getNextView().getResults();
        if (!(viewResults instanceof QuantityViewResult))
            return;

        QuantityViewResult qtyResult = (QuantityViewResult) viewResults;

        ISaleItem saleItemStack = qtyResult.getSaleItem();
        if (saleItemStack == null)
            throw new IllegalStateException("SALE_ITEM in QuantityViewResult cannot be null.");

        int quantity = qtyResult.getQty();
        double amount = saleItemStack.getPricePerUnit() * quantity;

        double balance = EconomyUtils.getBalance(getPlayer().getUniqueId());

        // check buyer balance
        if (balance < amount) {
            Msg.tell(getPlayer(), "{RED}Problem: {WHITE}You don't have enough {0}.",
                    EconomyUtils.getCurrencyName(CurrencyNoun.PLURAL));
        }
        // check buyer available inventory room
        else if (!InventoryUtils.hasRoom(getPlayer().getInventory(), saleItemStack.getItemStack(), quantity)) {
            Msg.tell(getPlayer(), "{RED}Problem: {WHITE}There isn't enough space in your inventory.");
        }
        // check item is available
        else if (saleItemStack.getParent().getQty() < quantity) {
            Msg.tell(getPlayer(), "{RED}Problem: {WHITE}Not enough inventory. Someone may have purchased the item already.");
        }
        // buy items
        else if (!_store.buySaleItem(getPlayer(), saleItemStack, quantity, amount)) {
            Msg.tell(getPlayer(), "{RED}Problem: {WHITE}Failed to buy items. They may have been purchased by someone else already.");
        }
        else {
            Msg.tell(getPlayer(), "{GREEN}Sucess: {WHITE}Purchased {0} {1} for {2}.", quantity,
                    ItemStackUtils.getDisplayName(saleItemStack.getItemStack(), DisplayNameResult.REQUIRED),
                    EconomyUtils.formatAmount(amount));
        }
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
