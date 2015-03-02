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
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.managers.NamedInsensitiveDataManager;

import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import javax.annotation.Nullable;

/**
 * Manages all item categories.
 */
public class CategoryManager extends NamedInsensitiveDataManager<Category> {

    /**
     * Constructor.
     *
     * @param categoryNode  The category managers data node.
     */
    CategoryManager(IDataNode categoryNode) {
        super(categoryNode, true);
    }

    /**
     * Get the category an {@link ItemStack} falls under.
     *
     * @param itemStack  The {@link ItemStack} to check.
     *
     * @return  The {@link Category} or null if the {@link ItemStack} has no category.
     */
    @Nullable
    public Category get(ItemStack itemStack) {

        itemStack = itemStack.clone();

        ItemStackUtil.removeTempLore(itemStack);

        Collection<Category> categories = getAll();

        for (Category category : categories) {
            if (category.getFilterManager().isValid(itemStack))
                return category;
        }

        return null;
    }

    /**
     * Add a category.
     *
     * @param name  The name of the category. Must be a valid data node name.
     *
     * @return The added category or null if the name is already in use.
     */
    @Nullable
    public Category add(String name) {
        PreCon.validNodeName(name);

        if (contains(name))
            return null;

        IDataNode catNode = getNode(name);
        catNode.set("title", name);

        Category category = new Category(name, catNode);

        add(category);

        return category;
    }

    @Nullable
    @Override
    protected Category load(String name, IDataNode itemNode) {
        return new Category(name, itemNode);
    }

    @Override
    protected void save(Category item, IDataNode itemNode) {
        // do nothing
    }
}
