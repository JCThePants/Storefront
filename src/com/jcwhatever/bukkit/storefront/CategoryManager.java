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

import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil;
import com.jcwhatever.nucleus.storage.IDataNode;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryManager {

    private IDataNode _categoryNode;
    private Map<String, Category> _categoryMap = new HashMap<String, Category>();
    private List<Runnable> _onCategoryChangeSubscribers = new ArrayList<Runnable>();


    CategoryManager(IDataNode categoryNode) {

        _categoryNode = categoryNode;

        loadSettings();
    }


    public Category getCategory (ItemStack itemStack) {

        itemStack = itemStack.clone();

        ItemStackUtil.removeTempLore(itemStack);

        List<Category> categories = getCategories();

        for (Category category : categories) {
            if (category.getFilterManager().isValidItem(itemStack))
                return category;
        }

        return null;
    }


    public Category getCategory (String name) {

        name = name.toLowerCase();

        return _categoryMap.get(name);
    }


    public List<Category> getCategories () {

        return new ArrayList<Category>(_categoryMap.values());
    }


    public Category addCategory (String name) {

        name = name.toLowerCase();

        Category category = _categoryMap.get(name);
        if (category != null) {
            return null;
        }

        IDataNode catNode = _categoryNode.getNode(name);
        catNode.set("title", name);
        catNode.save();

        category = new Category(name, catNode);

        _categoryMap.put(name, category);

        onCategoryChange();

        return category;
    }


    public Category removeCategory (String name) {

        name = name.toLowerCase();

        Category category = _categoryMap.remove(name);

        if (category != null)
            _categoryNode.remove(name);

        onCategoryChange();

        return category;
    }


    public void addOnCategoryChangeSubscriber (Runnable runnable) {

        _onCategoryChangeSubscribers.add(runnable);
    }


    public void removeOnCategoryChangeSubscriber (Runnable runnable) {

        _onCategoryChangeSubscribers.remove(runnable);
    }


    void onCategoryChange () {

        for (Runnable runnable : _onCategoryChangeSubscribers) {
            runnable.run();
        }
    }


    private void loadSettings () {

        for (IDataNode catNode : _categoryNode) {

            Category category = new Category(catNode.getName(), catNode);

            _categoryMap.put(category.getName().toLowerCase(), category);
        }
    }
}
