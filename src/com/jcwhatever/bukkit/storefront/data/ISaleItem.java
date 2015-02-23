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

import com.jcwhatever.bukkit.storefront.Category;
import com.jcwhatever.bukkit.storefront.stores.IStore;
import com.jcwhatever.nucleus.utils.items.MatchableItem;

import org.bukkit.inventory.ItemStack;

import java.util.Date;
import java.util.UUID;

/**
 * Represents a quantity of {@link ItemStack}'s for sale by a specific seller
 * in a specific {@link IStore}.
 */
public interface ISaleItem extends ISaleItemStacksGetter {

    /**
     * Get the sale item ID.
     */
    UUID getId();

    /**
     * Get the ID of the player that is selling the item.
     */
    UUID getSellerId();

    /**
     * Get the parent sale item. When a sale item has a parent, it means
     * the sale item represents a portion of the quantity of the parent.
     *
     * @return  The parent {@link ISaleItem} or self if the sale item is the parent.
     */
    ISaleItem getParent();

    /**
     * Determine if the sale item is removed.
     */
    boolean isRemoved();

    /**
     * Determine if the sale item is expired.
     */
    boolean isExpired();

    /**
     * Get the date the sale item expires.
     */
    Date getExpiration();

    /**
     * Get the store the sale item is being sold at.
     */
    IStore getStore();

    /**
     * Get the category of the sale item.
     */
    Category getCategory();

    /**
     * Get an {@link org.bukkit.inventory.ItemStack} representing the sale item.
     */
    ItemStack getItemStack();

    /**
     * Get a {@link com.jcwhatever.nucleus.utils.items.MatchableItem} used to
     * match the sale item {@link org.bukkit.inventory.ItemStack}.
     */
    MatchableItem getMatchable();

    /**
     * Get the price per unit of the sale item.
     */
    double getPricePerUnit();

    /**
     * Get the quantity of the sale item instance.
     *
     * <p>If the instance is a portion of a parent instance, this returns the
     * quantity of the portion. Otherwise the total quantity is returned.</p>
     */
    int getQty();

    /**
     * Increment the number of items in the stack by the specified amount.
     *
     * @param amount  The amount to increment. Negative numbers are allowed.
     */
    void increment(int amount);
}
