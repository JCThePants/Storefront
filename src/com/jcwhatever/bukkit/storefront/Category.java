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

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.jcwhatever.bukkit.generic.items.ItemFilterManager;
import com.jcwhatever.bukkit.generic.items.ItemStackComparer;
import com.jcwhatever.bukkit.generic.storage.IDataNode;

public class Category {

    private String _name;
    private String _title;
    private String _description = "Click to view items in category.";

    private IDataNode _categoryNode;
    private ItemFilterManager _filterManager;

    private ItemStack _menuItem;

    private static final ItemStack DEFAULT_MENU_ITEM = new ItemStack(Material.STONE);


    Category(String name, IDataNode categoryNode) {

        _name = name;
        _categoryNode = categoryNode;
        _filterManager = new ItemFilterManager(Storefront.getInstance(),
                categoryNode.getNode("item-filter"), ItemStackComparer.COMPARE_TYPE);

        loadSettings();
    }


    public String getName () {

        return _name;
    }


    public String getTitle () {

        return _title;
    }


    public void setTitle (String title) {

        _title = title;
        _categoryNode.set("title", title);
        _categoryNode.saveAsync(null);
        onCategoryChange();
    }


    public String getDescription () {

        return _description;
    }


    public void setDescription (String description) {

        _description = description;
        _categoryNode.set("description", description);
        _categoryNode.saveAsync(null);
        onCategoryChange();
    }


    public ItemFilterManager getFilterManager () {

        return _filterManager;
    }


    public ItemStack getMenuItem () {

        return _menuItem != null
                ? _menuItem
                : DEFAULT_MENU_ITEM;
    }


    public void setMenuItem (ItemStack item) {

        _menuItem = item;
        _categoryNode.set("menu-item", item);
        _categoryNode.saveAsync(null);
        onCategoryChange();
    }


    private void loadSettings () {

        _title = _categoryNode.getString("title", _name);
        _description = _categoryNode.getString("description", _description);

        ItemStack[] stacks = _categoryNode.getItemStacks("menu-item");

        if (stacks != null && stacks.length > 0) {
            _menuItem = stacks[0];
        }
    }


    private void onCategoryChange () {

        Storefront.getInstance().getCategoryManager().onCategoryChange();
    }

}
