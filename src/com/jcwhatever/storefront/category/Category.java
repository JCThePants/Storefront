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


package com.jcwhatever.storefront.category;

import com.jcwhatever.storefront.Lang;
import com.jcwhatever.storefront.Storefront;
import com.jcwhatever.nucleus.mixins.INamedInsensitive;
import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.items.ItemFilterManager;
import com.jcwhatever.nucleus.utils.items.ItemStackMatcher;
import com.jcwhatever.nucleus.utils.language.Localizable;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * An item category.
 */
public class Category implements INamedInsensitive {

    @Localizable static final String _DESCRIPTION =
            "Click to view items in category.";

    private String _name;
    private String _searchName;
    private String _title;
    private String _description = Lang.get(_DESCRIPTION);

    private IDataNode _categoryNode;
    private ItemFilterManager _filterManager;

    private ItemStack _menuItem;

    private static final ItemStack DEFAULT_MENU_ITEM = new ItemStack(Material.STONE);

    /**
     * Constructor.
     *
     * @param name          The category name.
     * @param categoryNode
     */
    Category(String name, IDataNode categoryNode) {

        _name = name;
        _searchName = name.toLowerCase();
        _categoryNode = categoryNode;
        _filterManager = new ItemFilterManager(Storefront.getPlugin(),
                categoryNode.getNode("item-filter"),
                ItemStackMatcher.get(ItemStackMatcher.MATCH_TYPE));

        loadSettings();
    }

    @Override
    public String getName () {
        return _name;
    }

    @Override
    public String getSearchName() {
        return _searchName;
    }

    /**
     * Get the display title of the category.
     */
    public String getTitle () {
        return _title;
    }

    /**
     * Set the display title of the category.
     *
     * @param title  The display title.
     */
    public void setTitle (String title) {
        PreCon.notNullOrEmpty(title);

        _title = title;
        _categoryNode.set("title", title);
        _categoryNode.save();
    }

    /**
     * Get the category description.
     */
    public String getDescription () {
        return _description;
    }

    /**
     * Set the category description.
     *
     * @param description  The description.
     */
    public void setDescription (String description) {
        _description = description;
        _categoryNode.set("description", description);
        _categoryNode.save();
    }

    /**
     * Get the category item filter manager.
     *
     * <p>Defines what items the category encompasses.</p>
     */
    public ItemFilterManager getFilterManager () {
        return _filterManager;
    }

    /**
     * Get the {@link ItemStack} used in inventory menus.
     */
    public ItemStack getMenuItem () {
        return _menuItem != null
                ? _menuItem
                : DEFAULT_MENU_ITEM;
    }

    /**
     * Set the {@link ItemStack} used in inventory menus.
     *
     * @param item  The {@link ItemStack}.
     */
    public void setMenuItem (ItemStack item) {
        PreCon.notNull(item);

        _menuItem = item;
        _categoryNode.set("menu-item", item);
        _categoryNode.save();
    }

    private void loadSettings () {

        _title = _categoryNode.getString("title", _name);
        _description = _categoryNode.getString("description", _description);

        ItemStack[] stacks = _categoryNode.getItemStacks("menu-item");

        if (stacks != null && stacks.length > 0) {
            _menuItem = stacks[0];
        }
    }
}
