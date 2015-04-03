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


package com.jcwhatever.storefront;

import com.jcwhatever.nucleus.Nucleus;
import com.jcwhatever.nucleus.NucleusPlugin;
import com.jcwhatever.nucleus.mixins.IDisposable;
import com.jcwhatever.nucleus.providers.permissions.IPermission;
import com.jcwhatever.nucleus.managed.scripting.IEvaluatedScript;
import com.jcwhatever.nucleus.managed.scripting.IScriptApi;
import com.jcwhatever.nucleus.managed.scripting.SimpleScriptApi;
import com.jcwhatever.nucleus.managed.scripting.SimpleScriptApi.IApiObjectCreator;
import com.jcwhatever.nucleus.providers.permissions.Permissions;
import com.jcwhatever.nucleus.utils.text.TextUtils;
import com.jcwhatever.storefront.category.CategoryManager;
import com.jcwhatever.storefront.commands.StorefrontCommandDispatcher;
import com.jcwhatever.storefront.scripting.ScriptApiObject;
import com.jcwhatever.storefront.stores.StoreManager;

import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;

/**
 * Region and menu based {@link org.bukkit.inventory.ItemStack} store plugin.
 */
public class Storefront extends NucleusPlugin {

    private static final String CHAT_PREFIX = TextUtils.format("{WHITE}[{BLUE}Store{WHITE}] ");
    private static Storefront _instance;

    /**
     * Get the current {@link Storefront} plugin instance.
     */
    public static Storefront getPlugin() {
        return _instance;
    }

    /**
     * Get the category manager.
     */
    public static CategoryManager getCategoryManager () {
        return _instance._categoryManager;
    }

    /**
     * Get the store manager.
     */
    public static StoreManager getStoreManager () {
        return _instance._storeManager;
    }


    private CategoryManager _categoryManager;
    private StoreManager _storeManager;
    private IScriptApi _scriptApi;

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

        _instance = this;

        _categoryManager = new CategoryManager(getDataNode().getNode("categories"));
        _storeManager = new StoreManager(getDataNode().getNode("stores"));

        _scriptApi = new SimpleScriptApi(this, "storefront", new IApiObjectCreator() {
            @Override
            public IDisposable create(Plugin plugin, IEvaluatedScript script) {
                return new ScriptApiObject();
            }
        });

        Nucleus.getScriptApiRepo().registerApi(_scriptApi);

        registerPermissions();

        this.registerCommands(new StorefrontCommandDispatcher(this));
        this.registerEventListeners(new BukkitListener());
    }

    @Override
    protected void onDisablePlugin() {

        Nucleus.getScriptApiRepo().unregisterApi(_scriptApi);
    }

    private void registerPermissions () {

        Permissions.runBatchOperation(new Runnable() {

            @Override
            public void run () {

                Permissions.register("storefront.store.server", PermissionDefault.OP);
                
                IPermission permission = Permissions.register("storefront.sell.rows.*", PermissionDefault.TRUE);

                for (int i = 1; i <= 6; i++) {

                    Permissions.register("storefront.sell.rows." + i, PermissionDefault.TRUE)
                        .addParent(permission, true);
                }
            }
        });
    }
}
