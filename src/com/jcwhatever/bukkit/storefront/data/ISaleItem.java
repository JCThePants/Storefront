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

import com.jcwhatever.generic.utils.items.ItemWrapper;
import com.jcwhatever.bukkit.storefront.Category;
import com.jcwhatever.bukkit.storefront.stores.IStore;

import org.bukkit.inventory.ItemStack;

import java.util.Date;
import java.util.List;
import java.util.UUID;


public interface ISaleItem {

    
    public boolean isRemoved ();
    
    public boolean isExpired();
    
    public Date getExpiration();

    public int getTotalSlots ();

    public UUID getItemId ();

    public UUID getSellerId ();

    public IStore getStore ();

    public Category getCategory ();

    public ItemStack getItemStack ();

    public ItemWrapper getWrapper ();

    public double getPricePerUnit ();
    
    /**
     * Get the quantity of the sale item instance
     * @return
     */
    public int getQty ();

    /**
     * Get the total quantity of sale items available
     * @return
     */
    public int getTotalItems ();

    public List<ISaleItem> getSaleItemStacks ();

    public SaleItem getParent();
    
    public void increment (int amount);    
}
