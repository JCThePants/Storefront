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

import com.jcwhatever.bukkit.storefront.utils.StoreStackMatcher;
import com.jcwhatever.nucleus.utils.PreCon;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

/**
 * A {@link java.util.HashMap} intended to to store {@link ISaleItem}'s
 * by a {@link java.util.UUID}.
 */
@SuppressWarnings("serial")
public class SaleItemIDMap extends HashMap<UUID, ISaleItem> {
    
    private static final int MAX_SLOTS = 2916; // (6 *9) * (6 * 9)

    /**
     * Determine if there is enough room to add a specified amount
     * of an {@link org.bukkit.inventory.ItemStack} into a paginated inventory
     * menu view that holds a maximum of 2916 slots.
     *
     * <p>Assumes the items in the map are the items already in the paginated inventory view.</p>
     *
     * @param viewerId   The ID of the inventory menu viewer. The viewers items are excluded
     *                   from the result since they are not visible.
     * @param itemStack  The {@link org.bukkit.inventory.ItemStack} to check.
     * @param amount     The amount.
     */
    public boolean canAdd(@Nullable UUID viewerId, ItemStack itemStack, int amount) {
        PreCon.notNull(itemStack);

        int room = getAvailableSpace(viewerId, itemStack, amount);

        return room >= amount;
    }

    /**
     * Determine how many more of a specified {@link org.bukkit.inventory.ItemStack}
     * can be added to a paginated inventory menu view that holds a maximum of 2916 slots.
     *
     * <p>Assumes the items in the map are the items already in the paginated inventory view.</p>
     *
     * @param viewerId   The ID of the inventory menu viewer. The viewers items are excluded
     *                   from the result since they are not visible.
     * @param itemStack  The {@link org.bukkit.inventory.ItemStack} to check.
     * @return
     */
    public int getAvailableSpace(@Nullable UUID viewerId, ItemStack itemStack) {
        return getAvailableSpace(viewerId, itemStack, -1);
    }
    
    private int getAvailableSpace(@Nullable UUID viewerId, ItemStack itemStack, int amount) {
        PreCon.notNull(itemStack);

        List<ISaleItem> saleItems = new ArrayList<>(this.values());
        
        int slotsUsed = 0;
        int partialStackSpace = 0;

        // iterate all sale items
        for (ISaleItem saleItem : saleItems) {
            
            Material saleMaterial = saleItem.getItemStack().getType();
            
            int maxStackSize = saleMaterial.getMaxStackSize();            
            int qty = saleItem.getQty();

            // increment slots used
            slotsUsed += (int)Math.ceil((double)qty / maxStackSize);

            boolean isMatch = StoreStackMatcher.getDefault()
                    .isMatch(itemStack, saleItem.getItemStack());

            // exclude the sellers items since the seller cannot see their own items
            if (isMatch && saleItem.getSellerId().equals(viewerId)) {
                
                partialStackSpace += qty >= maxStackSize ? qty % maxStackSize : maxStackSize - qty;
                
                if (amount >= 0 && partialStackSpace >= amount)
                    return partialStackSpace;
            }

            // check if max slots is exceeded
            if (amount >= 0 && slotsUsed >= MAX_SLOTS)
                return 0;
        }
        
        int emptySlots = MAX_SLOTS - slotsUsed;
        return (itemStack.getType().getMaxStackSize() * emptySlots) + partialStackSpace;
    }
}
