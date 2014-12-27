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


package com.jcwhatever.bukkit.storefront.views.sellwanted;


import com.jcwhatever.nucleus.mixins.IPaginator;
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
import com.jcwhatever.bukkit.storefront.data.ISaleItemGetter;
import com.jcwhatever.bukkit.storefront.data.PaginatedItems;
import com.jcwhatever.bukkit.storefront.data.PriceMap;
import com.jcwhatever.bukkit.storefront.meta.ViewTaskMode;
import com.jcwhatever.bukkit.storefront.stores.IStore;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil.PriceType;
import com.jcwhatever.bukkit.storefront.utils.StoreStackComparer;
import com.jcwhatever.bukkit.storefront.views.AbstractMenuView;
import com.jcwhatever.bukkit.storefront.views.quantity.QuantityViewResult;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

//@ViewInfo(pageType=PaginatorPageType.SALE_ITEM)
public class SellWantedView extends AbstractMenuView {

    private static final MetaKey<ISaleItem> SALE_ITEM = new MetaKey<>(ISaleItem.class);

    private IStore _store;
    private Category _category;
    private IPaginator _pagin;
    private PriceMap _priceMap;

    private ISaleItem _selectedItem;
    private MenuItem _selectedMenuItem;


    protected SellWantedView(ViewSession session, IViewFactory factory, ViewArguments arguments) {
        super(session, factory, arguments);

        _store = getStore();
        _category = hasCategory() ? getCategory() : null;
    }

    @Override
    protected List<MenuItem> createMenuItems() {

        ViewTaskMode taskMode = getTaskMode();

        setTitle(taskMode.getChatColor() + "Sell Items to Store");

        _priceMap = new PriceMap(getPlayer(), _store);

        _pagin = getArguments().get(PaginatorView.PAGINATOR);
        if (_pagin == null) {
            _pagin = new PaginatedItems(new ISaleItemGetter() {
                @Override
                public List<ISaleItem> getSaleItems() {
                    return _category != null
                            ? _store.getWantedItems().get(_category)
                            : _store.getWantedItems().getAll();
                }
            });
        }

        Integer page = getArguments().get(PaginatorView.SELECTED_PAGE);
        if (page == null) {
            page = 1;
        }

        List<ISaleItem> saleItemStacks = _pagin.getPage(page);//, PaginatorPageType.SALE_ITEM);

        List<MenuItem> menuItems = new ArrayList<>(saleItemStacks.size());

        for (int i=0; i < saleItemStacks.size(); i++) {

            ISaleItem saleItemStack = saleItemStacks.get(i);

            MenuItem menuItem = new MenuItem(i);
            menuItem.setItemStack(saleItemStack.getItemStack().clone());
            menuItem.getItemStack().setAmount(saleItemStack.getQty());
            menuItem.setMeta(SALE_ITEM, saleItemStack);


            updateItem(menuItem.getItemStack(), saleItemStack.getQty(), saleItemStack.getPricePerUnit());

            menuItems.add(menuItem);
        }

        return menuItems;
    }


    @Override
    protected void onItemSelect(MenuItem menuItem) {

        ItemStack itemStack = menuItem.getItemStack().clone();
        ItemStackUtil.removeTempLore(itemStack);

        int playerQty = InventoryUtils.count(getPlayer().getInventory(), itemStack);

        if (playerQty == 0) {
            Msg.tell(getPlayer(), "{RED}Problem: {WHITE}You don't have that item in your chest.");
            return;
        }

        _selectedItem = menuItem.getMeta(SALE_ITEM);
        _selectedMenuItem = menuItem;

        showQuantityView(itemStack, 1, playerQty, _priceMap.getPrice(itemStack));
    }

    @Override
    protected void onShow(ViewOpenReason reason) {

        if (reason != ViewOpenReason.PREV)
            return;

        ViewResults result = getViewSession().getNextView().getResults();

        if (result instanceof QuantityViewResult) {

            QuantityViewResult qtyResult = (QuantityViewResult)result;

            if (!qtyResult.isCancelled()) {

                ISaleItem saleItemStack = _selectedItem;
                MenuItem menuItem = _selectedMenuItem;

                if (saleItemStack.getTotalItems() == 0 || saleItemStack.isRemoved()) {

                    // item is not being accepted anymore
                    Msg.tell(getPlayer(), "{RED}Problem: {WHITE}The store is no longer accepting that item.");
                    return;
                }

                // make sure player has the items they are trying to sell
                if (!InventoryUtils.has(getPlayer().getInventory(), saleItemStack.getItemStack(),
                        StoreStackComparer.getDefault(), qtyResult.getQty())) {

                    Msg.tell(getPlayer(), "{RED}Problem: {WHITE}You don't have the items you are trying to sell or do not have enough of them.");
                    return;
                }

                double totalCost = qtyResult.getQty() * saleItemStack.getPricePerUnit();
                double storeBalance = EconomyUtils.getBalance(_store.getOwnerId());

                if (storeBalance < totalCost) {
                    Msg.tell(getPlayer(), "{RED}Problem: {WHITE}The store cannot afford to buy from you right now.");
                    return;
                }

                if (!_store.sellToStore(getPlayer(), saleItemStack, qtyResult.getQty(), totalCost)) {
                    Msg.tell(getPlayer(), "{RED}Problem: {WHITE}Failed to sell items to store.");

                    return;
                }


                // remove quantity from wanted items
                saleItemStack.increment(-qtyResult.getQty());

                if (menuItem != null) {
                    updateItem(menuItem.getItemStack(), saleItemStack.getQty(), saleItemStack.getPricePerUnit());
                    menuItem.set(this);
                    //_inventory.setItem(menuItem.getSlot(), menuItem.getItemStack());

                    Msg.tell(getPlayer(), "{GREEN}Success: {WHITE}Sold {0} {1} to the store for {GREEN}{2}{WHITE}.",
                            qtyResult.getQty(),
                            ItemStackUtils.getDisplayName(menuItem.getItemStack(), DisplayNameResult.REQUIRED),
                            EconomyUtils.formatAmount(totalCost));
                }



            }
        }

    }

    @Override
    protected void onClose(ViewCloseReason reason) {

    }

    private void updateItem(ItemStack itemStack, int qty, double price) {

        itemStack.setAmount(qty);
        _priceMap.setPrice(itemStack, price);

        ItemStackUtil.removeTempLore(itemStack);
        ItemStackUtil.setPriceLore(itemStack, price, PriceType.PER_ITEM);
        ItemStackUtil.addTempLore(itemStack, ChatColor.YELLOW + "Wanted: " + ChatColor.GRAY + qty);
    }

}
