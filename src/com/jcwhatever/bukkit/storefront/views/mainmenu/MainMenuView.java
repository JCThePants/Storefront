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


package com.jcwhatever.bukkit.storefront.views.mainmenu;

import com.jcwhatever.bukkit.generic.views.IViewFactory;
import com.jcwhatever.bukkit.generic.views.ViewSession;
import com.jcwhatever.bukkit.generic.views.data.ViewArguments;
import com.jcwhatever.bukkit.generic.views.data.ViewCloseReason;
import com.jcwhatever.bukkit.generic.views.data.ViewOpenReason;
import com.jcwhatever.bukkit.generic.views.menu.MenuItem;
import com.jcwhatever.bukkit.storefront.Msg;
import com.jcwhatever.bukkit.storefront.StoreType;
import com.jcwhatever.bukkit.storefront.Storefront;
import com.jcwhatever.bukkit.storefront.data.PaginatedItems;
import com.jcwhatever.bukkit.storefront.meta.SessionMetaKey;
import com.jcwhatever.bukkit.storefront.meta.ViewTaskMode;
import com.jcwhatever.bukkit.storefront.stores.IStore;
import com.jcwhatever.bukkit.storefront.views.AbstractMenuView;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MainMenuView extends AbstractMenuView {

    private IStore _store;
    private boolean _isStoreOwner;
    private boolean _canSell;

    public MainMenuView(ViewSession session, IViewFactory factory, ViewArguments arguments) {
        super(session, factory, arguments);

        _store = getStore();

        // set session store
        getViewSession().setMeta(SessionMetaKey.STORE, _store);
        setTitle("Store: " + _store.getTitle());

        _isStoreOwner = _store.getStoreType() == StoreType.PLAYER_OWNABLE
                && getPlayer().getUniqueId().equals(_store.getOwnerId());

        _canSell = !(_store.getStoreType() == StoreType.PLAYER_OWNABLE &&
                !getPlayer().getUniqueId().equals(_store.getOwnerId()) &&
                _store.getWantedItems().getAll().size() == 0);
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

        ViewTaskMode taskMode = menuItem.getTaskMode();
        if (taskMode == null)
            throw new AssertionError();

        IViewFactory factory = menuItem.getViewFactory();
        if (factory == null)
            throw new AssertionError();

        ViewArguments arguments = menuItem.getArguments();

        // set persistent task mode
        getViewSession().setMeta(SessionMetaKey.TASK_MODE, taskMode);

        if (menuItem.isCategorized()) {
            showCategoryViewOrNext(factory, new ViewArguments());
        }
        else {
            showPaginViewOrNext(factory, menuItem.getSaleItems(), arguments);
        }
    }

    @Override
    protected void onShow(ViewOpenReason reason) {

    }

    @Override
    protected void onClose(ViewCloseReason reason) {

    }

    @Override
    protected IStore getStore() {
        Block block = getViewSession().getSessionBlock();
        if (block == null) {
            throw new RuntimeException("A session block is required in order to get a store instance.");
        }

        IStore store = Storefront.getInstance().getStoreManager().getStore(block);
        if (store == null)
            throw new IllegalStateException("Could not get store instance.");

        return store;
    }

    /**
     * Get a new Sell Menu Item
     */
    private MenuItem getSellItem() {

        final MainMenuItem sellItem = new MainMenuItem(1, this);

        sellItem.setItemStack(new ItemStack(Material.GOLD_BLOCK));
        sellItem.setTitle(ChatColor.BLUE + "SELL");

        if (_store.hasOwner()) {
            sellItem.setDescription("Click to sell items to the store.");
        }
        else {
            sellItem.setDescription("Click to sell items from the store.");
        }

        switch (_store.getStoreType()) {

            case SERVER:
                sellItem.setViewFactory  (Storefront.VIEW_SELL);
                sellItem.setTaskMode     (ViewTaskMode.SERVER_SELL);
                sellItem.setCategorized  (true);
                sellItem.setOnClick      (new Runnable() {
                    @Override
                    public void run() {
                        sellItem.setSaleItems(_store.getSaleItems(getPlayer().getUniqueId()));
                    }
                });
                break;

            case PLAYER_OWNABLE:
                // owner sell
                if (_isStoreOwner) {
                    sellItem.setViewFactory (Storefront.VIEW_SELL);
                    sellItem.setTaskMode    (ViewTaskMode.OWNER_MANAGE_SELL);
                    sellItem.setSaleItems   (_store.getSaleItems());
                    sellItem.setOnClick     (new Runnable() {
                        @Override
                        public void run() {
                            sellItem.setSaleItems(_store.getSaleItems());
                        }
                    });
                }
                // player sell
                else {
                    sellItem.setViewFactory (Storefront.VIEW_SELL_WANTED);
                    sellItem.setTaskMode    (ViewTaskMode.PLAYER_SELL);
                    sellItem.setCategorized (true);
                    sellItem.setOnClick     (new Runnable() {
                        @Override
                        public void run() {
                            sellItem.setSaleItems(_store.getWantedItems().getAll());
                        }
                    });
                }
                break;
        }

        return sellItem;
    }

    /**
     * Get a new Buy menu item.
     */
    private MenuItem getBuyItem() {

        switch (_store.getStoreType()) {

            case SERVER:
                return getServerBuyItem();

            case PLAYER_OWNABLE:
                return getPlayerBuyItem();

            default:
                throw new AssertionError();
        }
    }

    private MenuItem getServerBuyItem() {
        final MainMenuItem item = new MainMenuItem(0, this);

        item.setTitle       ("BUY");
        item.setDescription ("Click to buy from the store.");
        item.setViewFactory (Storefront.VIEW_BUY);
        item.setTaskMode    (ViewTaskMode.SERVER_BUY);
        item.setItemStack   (new ItemStack(Material.CHEST));
        item.setCategorized (true);
        item.setOnClick     (new Runnable() {
            @Override
            public void run() {

                if (_store.getSaleItems().size() == 0) {
                    Msg.tell(getPlayer(), "Out of Stock");
                    item.setCancelled(true);
                }
                else {
                    item.setSaleItems(new PaginatedItems(_store.getSaleItems()));
                }
            }
        });

        return item;
    }

    private MenuItem getPlayerBuyItem() {
        final MainMenuItem item = new MainMenuItem(0, this);
        item.setItemStack(new ItemStack(Material.CHEST));

        if (_isStoreOwner) {
            item.setTitle       (ChatColor.GREEN + "WANTED");
            item.setDescription ("Click to manage items you're willing to buy.");
            item.setViewFactory (Storefront.VIEW_WANTED);
            item.setTaskMode    (ViewTaskMode.OWNER_MANAGE_BUY);
            item.setOnClick     (new Runnable() {
                @Override
                public void run() {
                    item.setSaleItems(_store.getWantedItems().getAll());
                }
            });
        }
        else {
            item.setTitle       ("BUY");
            item.setDescription ("Click to buy from the store.");
            item.setViewFactory (Storefront.VIEW_BUY);
            item.setTaskMode    (ViewTaskMode.PLAYER_BUY);
            item.setCategorized (true);
            item.setOnClick     (new Runnable() {
                @Override
                public void run() {
                    item.setSaleItems(_store.getSaleItems());
                }
            });
        }
        return item;
    }

}
