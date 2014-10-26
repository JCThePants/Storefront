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


package com.jcwhatever.bukkit.storefront;

import com.jcwhatever.bukkit.generic.GenericsPlugin;
import com.jcwhatever.bukkit.generic.permissions.IPermission;
import com.jcwhatever.bukkit.generic.permissions.Permissions;
import com.jcwhatever.bukkit.generic.views.ViewManager;
import com.jcwhatever.bukkit.storefront.commands.CommandHandler;
import com.jcwhatever.bukkit.storefront.events.GlobalListener;
import com.jcwhatever.bukkit.storefront.views.BuyView;
import com.jcwhatever.bukkit.storefront.views.CategoryView;
import com.jcwhatever.bukkit.storefront.views.ItemTaskMenuView;
import com.jcwhatever.bukkit.storefront.views.MainMenuView;
import com.jcwhatever.bukkit.storefront.views.PaginatorView;
import com.jcwhatever.bukkit.storefront.views.PriceView;
import com.jcwhatever.bukkit.storefront.views.QuantityView;
import com.jcwhatever.bukkit.storefront.views.SellView;
import com.jcwhatever.bukkit.storefront.views.SellWantedView;
import com.jcwhatever.bukkit.storefront.views.WantedView;
import org.bukkit.ChatColor;
import org.bukkit.permissions.PermissionDefault;

public class Storefront extends GenericsPlugin {

    private static final String CHAT_PREFIX = ChatColor.WHITE + "[" + ChatColor.BLUE + "Store" + ChatColor.WHITE + "] ";
    
    public static final String VIEW_MAIN_MENU = "__view_main_menu__";
    public static final String VIEW_CATEGORY = "__view_category__";
    public static final String VIEW_SELL = "__view_sell__";
    public static final String VIEW_PRICE = "__view_price__";
    public static final String VIEW_PAGINATOR = "__view_paginator__";
    public static final String VIEW_BUY = "__view_buy__";
    public static final String VIEW_QUANTITY = "__view_quantity__";
    public static final String VIEW_WANTED = "__view_wanted__";
    public static final String VIEW_ITEM_TASK_MENU = "__view_item_task_menu__";
    public static final String VIEW_SELL_WANTED = "__view_sell_wanted__";
    
    private static Storefront _singleton;

    public static Storefront getInstance () {

        return _singleton;
    }

    private ViewManager _viewManager;
    private CategoryManager _categoryManager;
    private StoreManager _storeManager;


    public Storefront() {

        super();

        _singleton = this;
    }


    public ViewManager getViewManager () {

        return _viewManager;
    }


    public CategoryManager getCategoryManager () {

        return _categoryManager;
    }


    public StoreManager getStoreManager () {

        return _storeManager;
    }

    @Override
    public String getChatPrefix () {
        return CHAT_PREFIX;
    }


    @Override
    public String getConsolePrefix () {

        return "[Storefront] ";
    }

    @Override
    protected void onEnablePlugin() {
        _viewManager = new ViewManager(this);
        _categoryManager = new CategoryManager(getDataNode().getNode("categories"));
        _storeManager = new StoreManager(getDataNode().getNode("stores"));

        registerViews();
        registerPermissions();

        this.registerCommands(new CommandHandler(this));
        this.registerEventListeners(new GlobalListener());
    }

    @Override
    protected void onDisablePlugin() {

    }


    private void registerViews () {

        _viewManager.addView(VIEW_MAIN_MENU, "Store", MainMenuView.class);
        _viewManager.addView(VIEW_CATEGORY, "Item Categories", CategoryView.class);
        _viewManager.addView(VIEW_SELL, "SELL Items", SellView.class);
        _viewManager.addView(VIEW_PRICE, "Set Price", PriceView.class);
        _viewManager.addView(VIEW_PAGINATOR, "Select Page", PaginatorView.class);
        _viewManager.addView(VIEW_BUY, "Items", BuyView.class);
        _viewManager.addView(VIEW_QUANTITY, "Select Quantity", QuantityView.class);
        _viewManager.addView(VIEW_WANTED, "Wanted", WantedView.class);
        _viewManager.addView(VIEW_ITEM_TASK_MENU, "Item Menu", ItemTaskMenuView.class);
        _viewManager.addView(VIEW_SELL_WANTED, "Sell Items to Store", SellWantedView.class);
    }


    private void registerPermissions () {

        Permissions.runBatchOperation(true, new Runnable() {

            @Override
            public void run () {

                Permissions.register("storefront.store.server", PermissionDefault.OP);
                
                IPermission permission = Permissions.register("storefront.sell.rows.*", PermissionDefault.TRUE);

                for (int i = 1; i <= 6; i++) {

                    IPermission rowPermission = Permissions.register("storefront.sell.rows." + i, PermissionDefault.TRUE);
                    Permissions.addParent(rowPermission, permission, true);
                }
            }
        });

    }

}
