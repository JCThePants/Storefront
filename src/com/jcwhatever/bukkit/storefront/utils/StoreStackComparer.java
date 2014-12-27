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


package com.jcwhatever.bukkit.storefront.utils;

import com.jcwhatever.nucleus.utils.items.ItemStackComparer;

import org.bukkit.inventory.ItemStack;

/**
 * Extends {@code ItemStackComparer} functionality by
 * removing temporary lore from item stacks before comparing them.
 */
public class StoreStackComparer extends ItemStackComparer {
    
    private static StoreStackComparer _default;
    private static StoreStackComparer _durability;

    /**
     * Get the default {@code StoreStackComparer} singleton instance
     * which compares based on type and meta data.
     */
    public static StoreStackComparer getDefault() {
        if (_default == null)
            _default = new StoreStackComparer(ItemStackComparer.DEFAULT_COMPARE);
        
        return _default;
    }

    /**
     * Get the {@code StoreStackComparer} singleton instance which
     * compares based on type, meta data, and durability.
     */
    public static StoreStackComparer getDurability() {
        if (_durability == null) {
            _durability = new StoreStackComparer(ItemStackComparer.DURABILITY_COMPARE);
        }
        
        return _durability;
    }

    /**
     * Constructor.
     *
     * @param compareOperations  {@code ItemStackComparer} compare operations bit flags.
     */
    public StoreStackComparer(byte compareOperations) {
        super(compareOperations);
    }


    /**
     * Compare two item stacks using the defined compare operations.
     *
     * @param itemStack1  The first item stack
     * @param itemStack2  The second item stack
     */
    @Override
    public boolean isSame(ItemStack itemStack1, ItemStack itemStack2) {

        if (itemStack1 == null || itemStack2 == itemStack1)
            return false;

        ItemStack clone1 = itemStack1.clone();
        ItemStack clone2 = itemStack2.clone();
        
        ItemStackUtil.removeTempLore(clone1);
        ItemStackUtil.removeTempLore(clone2);
        
        return super.isSame(clone1, clone2);
    }
    
}
