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


import com.jcwhatever.bukkit.storefront.Lang;
import com.jcwhatever.bukkit.storefront.Storefront;
import com.jcwhatever.bukkit.storefront.category.Category;
import com.jcwhatever.bukkit.storefront.category.CategoryManager;
import com.jcwhatever.bukkit.storefront.data.ISaleItem;
import com.jcwhatever.bukkit.storefront.data.PaginatedItems;
import com.jcwhatever.bukkit.storefront.meta.SessionMetaKey;
import com.jcwhatever.bukkit.storefront.meta.ViewSessionTask;
import com.jcwhatever.bukkit.storefront.meta.ViewSessionTask.BasicTask;
import com.jcwhatever.bukkit.storefront.stores.IStore;
import com.jcwhatever.bukkit.storefront.utils.StoreStackMatcher;
import com.jcwhatever.nucleus.utils.MetaKey;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.language.Localizable;
import com.jcwhatever.nucleus.views.View;
import com.jcwhatever.nucleus.views.ViewOpenReason;
import com.jcwhatever.nucleus.views.ViewSession;
import com.jcwhatever.nucleus.views.menu.MenuItem;
import com.jcwhatever.nucleus.views.menu.MenuItemBuilder;
import com.jcwhatever.nucleus.views.menu.PaginatorView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;

/**
 * A menu view used to select an item category.
 */
public class CategoryView extends AbstractMenuView {

    @Localizable static final String _VIEW_TITLE_BUY =
            "Buy Item Categories";

    @Localizable static final String _VIEW_TITLE_SELL =
            "Sell Item Categories";

    @Localizable static final String _CATEGORY_NAME =
            "{YELLOW}{ITALIC}{0: category name}{AQUA} {1: total items} items  ";


    private static MetaKey<Category>
            ITEM_CATEGORY = new MetaKey<>(Category.class);

    /**
     * Shows the next view and opens a {@link CategoryView} first if needed. If not, the next view
     * or a {@link com.jcwhatever.nucleus.views.menu.PaginatorView} to the next view is shown.
     *
     * @param store     The view session store.
     * @param session   The current view session.
     * @param nextView  The next view to show. Null to use the previous view (relative to the category view).
     * @param pagin     The paginated items that will be shown in the view.
     */
    public static void categoryNext(IStore store, ViewSession session, @Nullable View nextView, PaginatedItems pagin) {
        PreCon.notNull(session);
        PreCon.notNull(pagin);

        ViewSessionTask task = session.getMeta(SessionMetaKey.TASK_MODE);

        Collection<Category> categories = getCategories(store, task);

        int totalSlots = pagin.size();

        // determine if showing the category selection view is even necessary.
        if (totalSlots <= 6 * 9 * 3 || categories.size() <= 1) {

            PaginatorView.paginateNext(session, nextView, pagin, StoreStackMatcher.getDurability());
            return;
        }

        // show categories
        session.next(new CategoryView(store, nextView));
    }

    /*
     * Get all categories  for the given IStore and ViewSessionTask.
     */
    private static Collection<Category> getCategories(IStore store, ViewSessionTask task) {
        Collection<Category> categories;

        if (task.isOwnerManagerTask()) {
            CategoryManager manager = Storefront.getCategoryManager();
            categories = manager.getAll();
        }
        else {
            categories = task.getBasicTask() == BasicTask.BUY
                    ? store.getBuyCategories()
                    : store.getSellCategories();
        }

        return categories;
    }


    private View _nextView;

    /**
     * Constructor.
     *
     * @param nextView  The next view to show after the category view.
     *                  Null to use the previous view.
     */
    public CategoryView(IStore store, @Nullable View nextView) {
        super(store);

        _nextView = nextView;
    }

    @Override
    public String getTitle() {
        // set title
        ViewSessionTask taskMode = getSessionTask();
        return taskMode.getBasicTask() == BasicTask.BUY
                ? taskMode.getChatColor() + Lang.get(_VIEW_TITLE_BUY)
                : taskMode.getChatColor() + Lang.get(_VIEW_TITLE_SELL);
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

            List<ISaleItem> saleItems = getStore().getSaleItems(category);
            int totalInCategory = 0;
            for (ISaleItem saleItem : saleItems)
                totalInCategory += saleItem.getQty();

            MenuItem item = new MenuItemBuilder(category.getMenuItem())
                    .description(category.getDescription())
                    .title(Lang.get(_CATEGORY_NAME,
                            category.getTitle().toUpperCase(),
                            totalInCategory))
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

        if (_nextView != null)
            getViewSession().next(_nextView);
        else
            getViewSession().previous();
    }
}
