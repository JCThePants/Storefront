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


package com.jcwhatever.bukkit.storefront.views.category;


import com.jcwhatever.bukkit.generic.utils.text.TextUtils;
import com.jcwhatever.bukkit.generic.views.IViewFactory;
import com.jcwhatever.bukkit.generic.views.ViewSession;
import com.jcwhatever.bukkit.generic.views.data.ViewArgumentKey;
import com.jcwhatever.bukkit.generic.views.data.ViewArguments;
import com.jcwhatever.bukkit.generic.views.data.ViewCloseReason;
import com.jcwhatever.bukkit.generic.views.data.ViewOpenReason;
import com.jcwhatever.bukkit.generic.views.menu.MenuItem;
import com.jcwhatever.bukkit.storefront.Category;
import com.jcwhatever.bukkit.storefront.StoreType;
import com.jcwhatever.bukkit.storefront.data.ISaleItem;
import com.jcwhatever.bukkit.storefront.data.ISaleItemGetter;
import com.jcwhatever.bukkit.storefront.data.PaginatedItems;
import com.jcwhatever.bukkit.storefront.meta.SessionMetaKey;
import com.jcwhatever.bukkit.storefront.meta.ViewTaskMode;
import com.jcwhatever.bukkit.storefront.meta.ViewTaskMode.BasicTask;
import com.jcwhatever.bukkit.storefront.stores.IStore;
import com.jcwhatever.bukkit.storefront.views.AbstractMenuView;

import java.util.ArrayList;
import java.util.List;

public class CategoryView extends AbstractMenuView {

    public static ViewArgumentKey<IViewFactory>
            NEXT_VIEW = new ViewArgumentKey<>(IViewFactory.class);

    public static ViewArgumentKey<ViewArguments>
            NEXT_VIEW_ARGUMENTS = new ViewArgumentKey<>(ViewArguments.class);

    private static ViewArgumentKey<Category>
            ITEM_CATEGORY = new ViewArgumentKey<>(Category.class);

    private IStore _store;
    private ViewTaskMode _taskMode;
    private Category _previousCategory;


    public CategoryView(ViewSession session, IViewFactory factory, ViewArguments arguments) {
        super(session, factory, arguments);

        _store = getStore();
        _taskMode = getTaskMode();
        _previousCategory = hasCategory() ? getCategory() : null;

        // set title
        if (_taskMode.getBasicTask() == BasicTask.BUY)
            setTitle(_taskMode.getChatColor() + "Buy Item Categories");
        else
            setTitle(_taskMode.getChatColor() + "Sell Item Categories");
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

        List<Category> categories = getCategories();

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

            MenuItem item = new MenuItem(i)
                    .setItemStack(category.getMenuItem())
                    .setDescription(category.getDescription())
                    .setTitle(TextUtils.format("{YELLOW}{ITALIC}" + category.getTitle().toUpperCase() +
                            "{AQUA} " + totalInCategory + " items  "));

            item.setMeta(ITEM_CATEGORY, category);

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

        PaginatedItems saleItems = getCategorySaleItems(_store, _taskMode, category);
        IViewFactory nextView = getArguments().get(NEXT_VIEW);
        if (nextView == null)
            throw new RuntimeException("NEXT_VIEW argument is required.");

        ViewArguments nextArguments = getArguments().get(NEXT_VIEW_ARGUMENTS);
        if (nextArguments == null)
            nextArguments = new ViewArguments();

        showPaginViewOrNext(nextView, saleItems, nextArguments);
    }

    public static PaginatedItems getCategorySaleItems(final IStore store, ViewTaskMode currentMode,
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
