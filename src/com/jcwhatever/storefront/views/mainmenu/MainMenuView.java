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


package com.jcwhatever.storefront.views.mainmenu;

import com.jcwhatever.storefront.Lang;
import com.jcwhatever.storefront.data.ISaleItem;
import com.jcwhatever.storefront.data.ISaleItemGetter;
import com.jcwhatever.storefront.meta.SessionMetaKey;
import com.jcwhatever.storefront.meta.ViewSessionTask;
import com.jcwhatever.storefront.stores.IStore;
import com.jcwhatever.storefront.stores.StoreType;
import com.jcwhatever.storefront.utils.StoreStackMatcher;
import com.jcwhatever.storefront.views.AbstractMenuView;
import com.jcwhatever.storefront.views.BuyView;
import com.jcwhatever.storefront.views.CategoryView;
import com.jcwhatever.storefront.views.SellView;
import com.jcwhatever.storefront.views.SellWantedView;
import com.jcwhatever.storefront.views.WantedView;
import com.jcwhatever.nucleus.managed.language.Localizable;
import com.jcwhatever.nucleus.views.View;
import com.jcwhatever.nucleus.views.ViewCloseReason;
import com.jcwhatever.nucleus.views.ViewOpenReason;
import com.jcwhatever.nucleus.views.ViewSession;
import com.jcwhatever.nucleus.views.menu.MenuItem;
import com.jcwhatever.nucleus.views.menu.PaginatorView;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of {@link AbstractMenuView} that displays
 * the main store menu view.
 */
public class MainMenuView extends AbstractMenuView {

    @Localizable static final String _SELL_TITLE =
            "{BLUE}SELL";

    @Localizable static final String _SELL_TO_DESCR =
            "Click to sell items to the store.";

    @Localizable static final String _SELL_FROM_DESCR =
            "Click to sell items from the store.";

    @Localizable static final String _BUY_TITLE =
            "{GREEN}BUY";

    @Localizable static final String _BUY_DESCR =
            "Click to buy from the store.";

    @Localizable static final String _WANTED_TITLE =
            "{YELLOW}WANTED";

    @Localizable static final String _WANTED_DESCR =
            "Click to manage items you're willing to buy.";

    private boolean _isStoreOwner;
    private boolean _canSell;

    /**
     * Constructor.
     */
    public MainMenuView(IStore store) {
        super(store);
    }

    @Override
    protected void onViewSessionSet(ViewSession session) {

        // Determine if the viewer is the store owner
        _isStoreOwner = getStore().getType() == StoreType.PLAYER_OWNABLE
                && getPlayer().getUniqueId().equals(getStore().getOwnerId());

        // Determine if the viewer can sell items in the store
        _canSell = !(getStore().getType() == StoreType.PLAYER_OWNABLE &&
                !getPlayer().getUniqueId().equals(getStore().getOwnerId()) &&
                getStore().getWantedItems().getAll().size() == 0);
    }

    @Override
    protected List<MenuItem> createMenuItems() {

        List<MenuItem> menuItems = new ArrayList<>(2);

        menuItems.add(getBuyItem());

        if (_canSell) {
            menuItems.add(getSellItem());
        }

        return menuItems;
    }

    @Override
    protected void onItemSelect(MenuItem item) {

        if (!(item instanceof MainMenuItem))
            throw new AssertionError();

        MainMenuItem menuItem = (MainMenuItem)item;

        ViewSessionTask taskMode = menuItem.getTask();
        if (taskMode == null)
            throw new AssertionError();

        // set persistent task mode
        getViewSession().getMeta().setKey(SessionMetaKey.TASK_MODE, taskMode);

        View view = null;

        switch (taskMode) {
            case SERVER_BUY:
                // fall through
            case PLAYER_BUY:
                view = new BuyView(getStore(), menuItem.getSaleItems());
                break;

            case SERVER_SELL:
                // fall through
            case OWNER_MANAGE_SELL:
                view = new SellView(getStore());
                break;

            case PLAYER_SELL:
                view = new SellWantedView(getStore(), menuItem.getSaleItems());
                break;

            case OWNER_MANAGE_BUY:
                view = new WantedView(getStore(), menuItem.getSaleItems());
                break;
        }

        if (menuItem.isCategorized()) {
            CategoryView.categoryNext(getStore(), getViewSession(),
                    view,
                    menuItem.getSaleItems());
        }
        else {
            PaginatorView.paginateNext(getViewSession(),
                    view,
                    menuItem.getSaleItems(),
                    StoreStackMatcher.getDurability());
        }
    }

    @Override
    protected void onShow(ViewOpenReason reason) {
        // do nothing
    }

    @Override
    public String getTitle() {
        return getStore().getTitle();
    }

    @Override
    protected void onClose(ViewCloseReason reason) {
        // do nothing
    }

    /*
     * Get a new "Sell" menu item.
     */
    private MenuItem getSellItem() {

        MainMenuItemBuilder builder = (MainMenuItemBuilder)new MainMenuItemBuilder(Material.GOLD_BLOCK)
                .title(Lang.get(_SELL_TITLE))
                .description(getStore().hasOwner()
                        ? Lang.get(_SELL_TO_DESCR).toString()
                        : Lang.get(_SELL_FROM_DESCR).toString());

        switch (getStore().getType()) {

            case SERVER:
                builder
                        .task(ViewSessionTask.SERVER_SELL)
                        .categorized()
                        .saleItems(new ISaleItemGetter() {
                            @Override
                            public List<ISaleItem> getSaleItems() {
                                return getStore().getSaleItems(getPlayer().getUniqueId());
                            }
                        });
                break;

            case PLAYER_OWNABLE:
                // owner sell
                if (_isStoreOwner) {
                    builder
                            .task(ViewSessionTask.OWNER_MANAGE_SELL)
                            .saleItems(new ISaleItemGetter() {
                                @Override
                                public List<ISaleItem> getSaleItems() {
                                    return getStore().getSaleItems();
                                }
                            });

                }
                // player sell
                else {
                    builder
                            .task(ViewSessionTask.PLAYER_SELL)
                            .categorized()
                            .saleItems(new ISaleItemGetter() {
                                @Override
                                public List<ISaleItem> getSaleItems() {
                                    return hasCategory()
                                            ? getStore().getWantedItems().get(getCategory())
                                            : getStore().getWantedItems().getAll();
                                }
                            });
                }
                break;
        }

        return builder.build(1);
    }

    /**
     * Get a new "Buy" menu item.
     */
    private MenuItem getBuyItem() {

        switch (getStore().getType()) {

            case SERVER:
                return getServerBuyItem();

            case PLAYER_OWNABLE:
                return getPlayerBuyItem();

            default:
                throw new AssertionError();
        }
    }

    /*
     * Get a new "Buy" menu item for a server store.
     */
    private MenuItem getServerBuyItem() {

        MainMenuItemBuilder builder = (MainMenuItemBuilder)new MainMenuItemBuilder(Material.CHEST)
                .task(ViewSessionTask.SERVER_BUY)
                .categorized()
                .saleItems(new ISaleItemGetter() {
                    @Override
                    public List<ISaleItem> getSaleItems() {

                        List<ISaleItem> items = getStore().getSaleItems();

                        // remove players items from the list
                        List<ISaleItem> results = new ArrayList<ISaleItem>(items.size());
                        for (ISaleItem item : items) {
                            if (!item.getSellerId().equals(getPlayer().getUniqueId())) {
                                results.add(item);
                            }
                        }

                        return results;
                    }
                })
                .title(_BUY_TITLE)
                .description(_BUY_DESCR)
                ;

        return builder.build(0);
    }

    /*
     * Get a new "Buy" menu item for a player owned store.
     */
    private MenuItem getPlayerBuyItem() {
        MainMenuItemBuilder builder = new MainMenuItemBuilder(Material.CHEST);

        if (_isStoreOwner) {
            // Build menu item for store owner
            builder
                    .task(ViewSessionTask.OWNER_MANAGE_BUY)
                    .saleItems(new ISaleItemGetter() {

                        @Override
                        public List<ISaleItem> getSaleItems() {
                            return getStore().getWantedItems().getAll();
                        }
                    })
                    .title(Lang.get(_WANTED_TITLE))
                    .description(Lang.get(_WANTED_DESCR).toString());

        }
        else {
            // Build menu item for players that do not own the store
            builder
                    .task(ViewSessionTask.PLAYER_BUY)
                    .categorized()
                    .saleItems(new ISaleItemGetter() {
                        @Override
                        public List<ISaleItem> getSaleItems() {
                            return getStore().getSaleItems();
                        }
                    })
                    .title(Lang.get(_BUY_TITLE))
                    .description(Lang.get(_BUY_DESCR).toString());

        }
        return builder.build(0);
    }
}
