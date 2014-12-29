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

import com.jcwhatever.bukkit.storefront.commands.StorefrontCommandDispatcher;
import com.jcwhatever.bukkit.storefront.events.GlobalListener;
import com.jcwhatever.nucleus.NucleusPlugin;
import com.jcwhatever.nucleus.providers.permissions.IPermission;
import com.jcwhatever.nucleus.utils.Permissions;

import org.bukkit.ChatColor;
import org.bukkit.permissions.PermissionDefault;

public class Storefront extends NucleusPlugin {

    private static final String CHAT_PREFIX = ChatColor.WHITE + "[" + ChatColor.BLUE + "Store" + ChatColor.WHITE + "] ";

    private static Storefront _singleton;

    public static Storefront getInstance () {

        return _singleton;
    }

    private CategoryManager _categoryManager;
    private StoreManager _storeManager;


    public Storefront() {

        super();

        _singleton = this;
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


        _categoryManager = new CategoryManager(getDataNode().getNode("categories"));
        _storeManager = new StoreManager(getDataNode().getNode("stores"));

        registerPermissions();

        this.registerCommands(new StorefrontCommandDispatcher(this));
        this.registerEventListeners(new GlobalListener());
    }

    @Override
    protected void onDisablePlugin() {

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
