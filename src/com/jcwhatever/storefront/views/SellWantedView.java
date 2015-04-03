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


package com.jcwhatever.storefront.views;


import com.jcwhatever.nucleus.providers.economy.IEconomyTransaction;
import com.jcwhatever.nucleus.utils.observer.result.FutureSubscriber;
import com.jcwhatever.nucleus.utils.observer.result.Result;
import com.jcwhatever.storefront.Lang;
import com.jcwhatever.storefront.Msg;
import com.jcwhatever.storefront.data.ISaleItem;
import com.jcwhatever.storefront.data.PaginatedItems;
import com.jcwhatever.storefront.data.PriceMap;
import com.jcwhatever.storefront.stores.IStore;
import com.jcwhatever.storefront.utils.ItemStackUtil;
import com.jcwhatever.storefront.utils.ItemStackUtil.PriceType;
import com.jcwhatever.storefront.utils.StoreStackMatcher;
import com.jcwhatever.nucleus.providers.economy.Economy;
import com.jcwhatever.nucleus.utils.MetaKey;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.inventory.InventoryUtils;
import com.jcwhatever.nucleus.utils.items.ItemStackUtils;
import com.jcwhatever.nucleus.utils.items.ItemStackUtils.DisplayNameOption;
import com.jcwhatever.nucleus.utils.language.Localizable;
import com.jcwhatever.nucleus.views.View;
import com.jcwhatever.nucleus.views.ViewOpenReason;
import com.jcwhatever.nucleus.views.menu.MenuItem;
import com.jcwhatever.nucleus.views.menu.MenuItemBuilder;
import com.jcwhatever.nucleus.views.menu.PaginatorView;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

//@ViewInfo(pageType=PaginatorPageType.SALE_ITEM)
public class SellWantedView extends AbstractMenuView {

    @Localizable static final String _VIEW_TITLE =
            "{DARK_GRAY}Sell Items to Store";

    @Localizable static final String _ITEM_NO_LONGER_ACCEPTED =
            "{RED}Problem: {WHITE}The store is no longer accepting that item.";

    @Localizable static final String _INSUFFICIENT_ITEMS =
            "{RED}Problem: {WHITE}You don't have the items you are trying to sell or do " +
                    "not have enough of them.";

    @Localizable static final String _STORE_CANNOT_AFFORD =
            "{RED}Problem: {WHITE}The store cannot afford to buy from you right now.";

    @Localizable static final String _ITEM_NOT_IN_INVENTORY =
            "{RED}Problem: {WHITE}You don't have that item in your inventory.";

    @Localizable static final String _SELL_FAILED =
            "{RED}Problem: {WHITE}Failed to sell items to store.";

    @Localizable static final String _SELL_SUCCESS =
            "{GREEN}Success: {WHITE}Sold {0: qty} {1: item sold} to the store for {GREEN}{2: amount}{WHITE}.";

    @Localizable static final String _WANTED_QTY_LORE =
            "{YELLOW}Wanted: {GRAY}{0: qty}";

    private static final MetaKey<ISaleItem> SALE_ITEM = new MetaKey<>(ISaleItem.class);

    private PriceMap _priceMap;

    private ISaleItem _selectedSaleItem;
    private MenuItem _selectedMenuItem;

    private PaginatedItems _pagin;
    private int _page = 1;

    public SellWantedView(IStore store, PaginatedItems paginatedItems) {
        super(store);

        PreCon.notNull(paginatedItems);

        _pagin = paginatedItems;
    }

    @Override
    public String getTitle() {
        return Lang.get(_VIEW_TITLE);
    }

    @Override
    protected boolean onPreShow(ViewOpenReason reason) {

        if (reason == ViewOpenReason.PREV) {

            View nextView = getViewSession().getNext();

            if (nextView instanceof PaginatorView) {
                _page = ((PaginatorView) nextView).getSelectedPage();
            }
        }

        return true;
    }

    @Override
    protected void onShow(ViewOpenReason reason) {

        if (reason != ViewOpenReason.PREV)
            return;

        View nextView = getViewSession().getNext();
        if (nextView instanceof QuantityView) {

            QuantityView quantityView = (QuantityView)nextView;

            final Integer amount = quantityView.getSelectedQty();
            if (amount == null)
                return;

            final MenuItem menuItem = _selectedMenuItem;

            if (_selectedSaleItem.getParent().getQty() == 0 || _selectedSaleItem.isRemoved()) {

                // item is not being accepted anymore
                Msg.tell(getPlayer(), Lang.get(_ITEM_NO_LONGER_ACCEPTED));
                return;
            }

            // make sure player has the items they are trying to sell
            if (!InventoryUtils.has(getPlayer().getInventory(), _selectedSaleItem.getItemStack(),
                    StoreStackMatcher.getDefault(), amount)) {

                Msg.tell(getPlayer(), Lang.get(_INSUFFICIENT_ITEMS));
                return;
            }

            final double totalCost = amount * _selectedSaleItem.getPricePerUnit();
            double storeBalance = Economy.getBalance(getStore().getOwnerId());

            if (storeBalance < totalCost) {
                Msg.tell(getPlayer(), Lang.get(_STORE_CANNOT_AFFORD));
                return;
            }

            getStore().sellToStore(getPlayer(), _selectedSaleItem, amount, totalCost)
                    .onError(new FutureSubscriber<IEconomyTransaction>() {
                        @Override
                        public void on(Result<IEconomyTransaction> result) {
                            Msg.tell(getPlayer(), Lang.get(_SELL_FAILED));
                        }
                    })
                    .onSuccess(new FutureSubscriber<IEconomyTransaction>() {
                        @Override
                        public void on(Result<IEconomyTransaction> result) {
                            // remove quantity from wanted items
                            _selectedSaleItem.increment(-amount);

                            if (menuItem != null) {
                                updateItem(menuItem, _selectedSaleItem.getQty(), _selectedSaleItem.getPricePerUnit());
                                menuItem.set(SellWantedView.this);
                                //_inventory.setItem(menuItem.getSlot(), menuItem.getItemStack());

                                Msg.tell(getPlayer(), Lang.get(_SELL_SUCCESS,
                                        amount,
                                        ItemStackUtils.getDisplayName(menuItem, DisplayNameOption.REQUIRED),
                                        Economy.getCurrency().format(totalCost)));
                            }
                        }
                    });

        }
    }

    @Override
    protected List<MenuItem> createMenuItems() {

        _priceMap = new PriceMap(getPlayer(), getStore());

        List<ISaleItem> saleItemStacks = _pagin.getPage(_page);

        List<MenuItem> menuItems = new ArrayList<>(saleItemStacks.size());

        for (int i=0; i < saleItemStacks.size(); i++) {

            ISaleItem saleItemStack = saleItemStacks.get(i);

            MenuItem menuItem = new MenuItemBuilder(saleItemStack.getItemStack())
                    .amount(saleItemStack.getQty())
                    .meta(SALE_ITEM, saleItemStack)
                    .build(i);

            updateItem(menuItem, saleItemStack.getQty(), saleItemStack.getPricePerUnit());

            menuItems.add(menuItem);
        }

        return menuItems;
    }


    @Override
    protected void onItemSelect(MenuItem menuItem) {

        MenuItem clone = new MenuItem(menuItem);
        ItemStackUtil.removeTempLore(clone);

        int playerQty = InventoryUtils.count(getPlayer().getInventory(), clone);

        if (playerQty == 0) {
            Msg.tell(getPlayer(), Lang.get(_ITEM_NOT_IN_INVENTORY));
            return;
        }

        ISaleItem selectedItem = menuItem.getMeta().get(SALE_ITEM);
        assert selectedItem != null;

        _selectedMenuItem = menuItem;

        Double price = _priceMap.get(clone);
        assert price != null;

        getViewSession().next(new QuantityView(getStore(),
                selectedItem.getItemStack(), 1, playerQty, price));
    }



    private void updateItem(ItemStack itemStack, int qty, double price) {

        itemStack.setAmount(qty);
        _priceMap.set(itemStack, price);

        ItemStackUtil.removeTempLore(itemStack);
        ItemStackUtil.setPriceLore(itemStack, price, PriceType.PER_ITEM);
        ItemStackUtil.addTempLore(itemStack, Lang.get(_WANTED_QTY_LORE, qty));
    }

}
