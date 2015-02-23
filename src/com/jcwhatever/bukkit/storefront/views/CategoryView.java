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


import com.jcwhatever.bukkit.storefront.Category;
import com.jcwhatever.bukkit.storefront.CategoryManager;
import com.jcwhatever.bukkit.storefront.StoreType;
import com.jcwhatever.bukkit.storefront.Storefront;
import com.jcwhatever.bukkit.storefront.data.ISaleItem;
import com.jcwhatever.bukkit.storefront.data.ISaleItemGetter;
import com.jcwhatever.bukkit.storefront.data.PaginatedItems;
import com.jcwhatever.bukkit.storefront.meta.SessionMetaKey;
import com.jcwhatever.bukkit.storefront.meta.ViewSessionTask;
import com.jcwhatever.bukkit.storefront.meta.ViewSessionTask.BasicTask;
import com.jcwhatever.bukkit.storefront.stores.IStore;
import com.jcwhatever.bukkit.storefront.utils.StoreStackMatcher;
import com.jcwhatever.nucleus.utils.MetaKey;
import com.jcwhatever.nucleus.views.View;
import com.jcwhatever.nucleus.views.ViewCloseReason;
import com.jcwhatever.nucleus.views.ViewOpenReason;
import com.jcwhatever.nucleus.views.ViewSession;
import com.jcwhatever.nucleus.views.menu.MenuItem;
import com.jcwhatever.nucleus.views.menu.MenuItemBuilder;
import com.jcwhatever.nucleus.views.menu.PaginatorView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;

public class CategoryView extends AbstractMenuView {

    public static MetaKey<Category>
            ITEM_CATEGORY = new MetaKey<>(Category.class);

    public static void categoryNext(ViewSession session, View nextView, PaginatedItems pagin) {

        IStore store = session.getMeta(SessionMetaKey.STORE);
        if (store == null)
            throw new AssertionError();

        ViewSessionTask task = session.getMeta(SessionMetaKey.TASK_MODE);

        Collection<Category> categories = getCategories(store, task);

        int totalSlots = pagin.getTotalItems();

        if (totalSlots <= 6 * 9 * 3 || categories.size() <= 1) {

            PaginatorView.paginateNext(session, nextView, pagin, StoreStackMatcher.getDurability());
            return;
        }

        // show categories
        session.next(new CategoryView(nextView));
    }

    private static Collection<Category> getCategories(IStore store, ViewSessionTask mode) {
        Collection<Category> categories;

        if (mode.isOwnerManagerTask()) {
            CategoryManager manager = Storefront.getCategoryManager();
            categories = manager.getAll();
        }
        else {
            categories = mode.getBasicTask() == BasicTask.BUY
                    ? store.getBuyCategories()
                    : store.getSellCategories();
        }

        return categories;
    }


    private IStore _store;
    private View _nextView;

    public CategoryView(@Nullable View nextView) {

        _nextView = nextView;
        _store = getStore();
    }

    @Override
    public String getTitle() {
        // set title
        ViewSessionTask taskMode = getSessionTask();
        return taskMode.getBasicTask() == BasicTask.BUY
                ? taskMode.getChatColor() + "Buy Item Categories"
                : taskMode.getChatColor() + "Sell Item Categories";
    }

    @Override
    protected void onClose(ViewCloseReason reason) {
        // do nothing
    }

    @Override
    protected void onShow(ViewOpenReason reason) {
        // do nothing
    }

    @Override
    protected List<MenuItem> createMenuItems() {

        List<Category> categories = new ArrayList<>(getCategories(getStore(), getSessionTask()));

        double itemSize = categories.size();
        int rows = (int) Math.ceil(itemSize / 9);
        int totalSlots = rows * 9;

        int size = Math.min(categories.size(), totalSlots);

        List<MenuItem> menuItems = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {

            Category category = categories.get(i);

            List<ISaleItem> saleItems = _store.getSaleItems(category);
            int totalInCategory = 0;
            for (ISaleItem saleItem : saleItems)
                totalInCategory += saleItem.getQty();

            MenuItem item = new MenuItemBuilder(category.getMenuItem())
                    .description(category.getDescription())
                    .title("{YELLOW}{ITALIC}{0}{AQUA} {1} items  ",
                            category.getTitle().toUpperCase(),
                            totalInCategory)
                    .meta(ITEM_CATEGORY, category)
                    .build(i);

            menuItems.add(item);
        }

        return menuItems;
    }

    @Override
    protected void onItemSelect(MenuItem menuItem) {

        Category category = menuItem.getMeta(ITEM_CATEGORY);
        if (category == null)
            throw new AssertionError();

        getViewSession().setMeta(SessionMetaKey.CATEGORY, category);

        // get sale items in category

        //PaginatedItems saleItems = getCategorySaleItems(_store, _taskMode, category);

        if (_nextView != null)
            getViewSession().next(_nextView);
        else
            getViewSession().previous();
    }

    public static PaginatedItems getCategorySaleItems(final IStore store, ViewSessionTask currentMode,
                                                      final Category category) {
        PaginatedItems saleItems;

        if (store.getStoreType() == StoreType.PLAYER_OWNABLE
                && currentMode.getBasicTask() == BasicTask.SELL) {

            saleItems = currentMode.isOwnerManagerTask()
                    ? new PaginatedItems(store, category)
                    : new PaginatedItems(new ISaleItemGetter() {
                @Override
                public List<ISaleItem> getSaleItems() {
                    return store.getWantedItems().get(category);
                }
            });
        }
        else {

            saleItems = new PaginatedItems(store, category);
        }

        return saleItems;
    }

}
