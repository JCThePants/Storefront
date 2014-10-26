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


package com.jcwhatever.bukkit.storefront.data;

import com.jcwhatever.bukkit.generic.items.ItemStackComparer;
import com.jcwhatever.bukkit.generic.items.ItemWrapper;
import com.jcwhatever.bukkit.storefront.utils.StoreStackComparer;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class SaleItemMap extends HashMap<ItemWrapper, SaleItem> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    public SaleItemMap() {
    }


    public SaleItem get (ItemStack key) {
        return get(key, StoreStackComparer.getDefault());
    }
    
    public SaleItem get (ItemStack key, ItemStackComparer comparer) {
        ItemWrapper wrapper = new ItemWrapper(key, comparer);
        return super.get(wrapper);
    }

    public SaleItem put (ItemStack key, SaleItem value) {

        ItemWrapper wrapper = new ItemWrapper(key);
        return super.put(wrapper, value);
    }

    public SaleItem remove (ItemStack key) {
        return remove(key, StoreStackComparer.getDefault());
    }
    
    public SaleItem remove (ItemStack key, ItemStackComparer comparer) {
        ItemWrapper wrapper = new ItemWrapper(key);
        return super.remove(wrapper);
    }

    public boolean containsKey (ItemStack key) {
        return containsKey(key, StoreStackComparer.getDefault());
    }
        
    public boolean containsKey (ItemStack key, ItemStackComparer comparer) {

        ItemWrapper wrapper = new ItemWrapper(key, comparer);
        return super.containsKey(wrapper);
    }

}
