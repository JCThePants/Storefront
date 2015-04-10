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
import com.jcwhatever.nucleus.utils.observer.future.FutureResultSubscriber;
import com.jcwhatever.nucleus.utils.observer.future.Result;
import com.jcwhatever.storefront.Lang;
import com.jcwhatever.storefront.Msg;
import com.jcwhatever.storefront.data.ISaleItem;
import com.jcwhatever.storefront.data.PaginatedItems;
import com.jcwhatever.storefront.meta.ViewSessionTask;
import com.jcwhatever.storefront.stores.IStore;
import com.jcwhatever.storefront.utils.ItemStackUtil;
import com.jcwhatever.storefront.utils.ItemStackUtil.PriceType;
import com.jcwhatever.nucleus.providers.economy.ICurrency.CurrencyNoun;
import com.jcwhatever.nucleus.providers.economy.Economy;
import com.jcwhatever.nucleus.utils.MetaKey;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.inventory.InventoryUtils;
import com.jcwhatever.nucleus.utils.items.ItemStackUtils;
import com.jcwhatever.nucleus.utils.items.ItemStackUtils.DisplayNameOption;
import com.jcwhatever.nucleus.managed.language.Localizable;
import com.jcwhatever.nucleus.views.View;
import com.jcwhatever.nucleus.views.ViewOpenReason;
import com.jcwhatever.nucleus.views.menu.MenuItem;
import com.jcwhatever.nucleus.views.menu.MenuItemBuilder;
import com.jcwhatever.nucleus.views.menu.PaginatorView;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * A menu view that is displayed in order to buy items.
 *
 * <p>Allows player to select items from an inventory view to purchase.</p>
 */
public class BuyView extends AbstractMenuView {

    @Localizable static final String _VIEW_TITLE =
            "Buy Items";

    @Localizable static final String _VIEW_TITLE_MULTI_PAGE =
            "Buy Items (Page {0: page number})";

    @Localizable static final String _INSUFFICIENT_FUNDS =
            "{RED}Problem: {WHITE}You don't have enough {0: currency name}.";

    @Localizable static final String _INSUFFICENT_INVENTORY_SPACE =
            "{RED}Problem: {WHITE}There isn't enough space in your inventory.";

    @Localizable static final String _NOT_ENOUGH_AVAILABLE =
            "{RED}Problem: {WHITE}Not enough available. Someone may have purchased the item already.";

    @Localizable static final String _BUY_FAILED =
            "{RED}Problem: {WHITE}Failed to buy items. They may have been purchased by " +
                    "someone else already.";

    @Localizable static final String _BUY_SUCCESS =
            "{GREEN}Success: {WHITE}Purchased {0: qty} {1: item} for {2: price}.";

    @Localizable static final String _OUT_OF_STOCK =
            "Out of Stock";

    private static final MetaKey<ISaleItem>
            SALE_ITEM = new MetaKey<>(ISaleItem.class);

    private final PaginatedItems _pagin;

    private View _paginator;
    private int _page = 1;

    private ISaleItem _selectedSaleItem;

    /**
     * Constructor.
     *
     * @param paginatedItems  {@link PaginatedItems} that contain the items to display in the view.
     */
    public BuyView(IStore store, PaginatedItems paginatedItems) {
        super(store);
        PreCon.notNull(paginatedItems);

        _pagin = paginatedItems;
    }

    @Override
    public String getTitle() {
        ViewSessionTask taskMode = getSessionTask();
        String title = taskMode.getChatColor().toString();

        title += _paginator != null
                ? Lang.get(_VIEW_TITLE_MULTI_PAGE, _page)
                : Lang.get(_VIEW_TITLE);

        return title;
    }

    @Override
    protected List<MenuItem> createMenuItems() {

        _paginator = getViewSession().getPrev();
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

        ISaleItem saleItemStack = menuItem.getMeta().get(SALE_ITEM);
        if (saleItemStack == null)
            return;

        _selectedSaleItem = saleItemStack;

        getViewSession().next(new QuantityView(getStore(),
                saleItemStack.getItemStack(), 1, 64, saleItemStack.getPricePerUnit()));
    }

    @Override
    protected void onShow(ViewOpenReason reason) {
        // do nothing
    }

    @Override
    protected boolean onPreShow(ViewOpenReason reason) {

        if (reason == ViewOpenReason.PREV) {

            View quantityView = getViewSession().getNext();
            if (!(quantityView instanceof QuantityView)) {
                return true;
            }

            final int amount = ((QuantityView) quantityView).getSelectedQty();

            final double price = _selectedSaleItem.getPricePerUnit() * amount;
            double balance = Economy.getBalance(getPlayer().getUniqueId());

            // check buyer balance
            if (balance < price) {
                Msg.tell(getPlayer(), Lang.get(_INSUFFICIENT_FUNDS,
                        Economy.getCurrency().getName(CurrencyNoun.PLURAL)));
            }
            // check buyer available inventory room
            else if (!InventoryUtils.hasRoom(getPlayer().getInventory(), _selectedSaleItem.getItemStack(), amount)) {
                Msg.tell(getPlayer(), Lang.get(_INSUFFICENT_INVENTORY_SPACE));
            }
            // check item is available
            else if (_selectedSaleItem.getParent().getQty() < amount) {
                Msg.tell(getPlayer(), Lang.get(_NOT_ENOUGH_AVAILABLE));
            }

            getStore().buySaleItem(getPlayer(), _selectedSaleItem, amount, price)
                    .onError(new FutureResultSubscriber<IEconomyTransaction>() {
                        @Override
                        public void on(Result<IEconomyTransaction> result) {

                            Msg.tell(getPlayer(), Lang.get(_BUY_FAILED));
                        }
                    })
                    .onSuccess(new FutureResultSubscriber<IEconomyTransaction>() {
                        @Override
                        public void on(Result<IEconomyTransaction> result) {

                            Msg.tell(getPlayer(), Lang.get(_BUY_SUCCESS, amount,
                                    ItemStackUtils.getDisplayName(
                                            _selectedSaleItem.getItemStack(), DisplayNameOption.REQUIRED),
                                    Economy.getCurrency().format(price)));
                        }
                    });

        } else {

            if (getStore().getSaleItems().size() == 0) {

                Msg.tell(getPlayer(), Lang.get(_OUT_OF_STOCK));
                return false;
            }
        }

        return true;
    }
}
