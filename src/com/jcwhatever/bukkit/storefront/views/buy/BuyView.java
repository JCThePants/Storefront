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

import com.jcwhatever.bukkit.generic.utils.MetaKey;
import com.jcwhatever.bukkit.generic.views.IViewFactory;
import com.jcwhatever.bukkit.generic.views.ViewSession;
import com.jcwhatever.bukkit.generic.views.data.ViewArguments;
import com.jcwhatever.bukkit.generic.views.data.ViewCloseReason;
import com.jcwhatever.bukkit.generic.views.data.ViewOpenReason;
import com.jcwhatever.bukkit.generic.views.menu.MenuItem;
import com.jcwhatever.bukkit.generic.views.menu.PaginatorView;
import com.jcwhatever.bukkit.storefront.Category;
import com.jcwhatever.bukkit.storefront.data.PaginatedItems;
import com.jcwhatever.bukkit.storefront.data.SaleItem;
import com.jcwhatever.bukkit.storefront.meta.ViewTaskMode;
import com.jcwhatever.bukkit.storefront.stores.IStore;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil.PriceType;
import com.jcwhatever.bukkit.storefront.views.AbstractMenuView;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BuyView extends AbstractMenuView {

    private static final MetaKey<SaleItem>
            SALE_ITEM = new MetaKey<>(SaleItem.class);

    public BuyView(ViewSession session, IViewFactory factory, ViewArguments arguments) {
        super(session, factory, arguments);
    }

    @Override
    protected List<MenuItem> createMenuItems() {

        ViewTaskMode taskMode = getTaskMode();
        IStore store = getStore();
        Category category = getCategory();

        Integer page = getArguments().get(PaginatorView.SELECTED_PAGE);

        PaginatedItems pagin = (PaginatedItems)getArguments().get(PaginatorView.PAGINATOR);

        if (page == null) {
            page = 1;
        }

        if (pagin == null) {
            List<SaleItem> saleItems = store.getSaleItems(category);
            pagin = new PaginatedItems(saleItems);
        }

        int totalPages = pagin.getTotalPages();
        if (totalPages < page)
            page = totalPages;

        setTitle(taskMode.getChatColor() + "Buy Items (Page " + page + ')');

        List<SaleItem> saleItemStacks = pagin.getPage(page);

        List<MenuItem> menuItems = new ArrayList<>(saleItemStacks.size());

        for (int i = 0; i < saleItemStacks.size(); i++) {

            SaleItem item = saleItemStacks.get(i);

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

        SaleItem saleItemStack = menuItem.getMeta(SALE_ITEM);
        if (saleItemStack == null)
            return;

        showQuantityView(saleItemStack, 1);
    }

    @Override
    protected void onShow(ViewOpenReason reason) {

    }

    @Override
    protected void onClose(ViewCloseReason reason) {

    }
}
