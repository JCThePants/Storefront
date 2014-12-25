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

import com.jcwhatever.generic.extended.MaterialExt;
import com.jcwhatever.generic.utils.PreCon;
import com.jcwhatever.bukkit.storefront.utils.StoreStackComparer;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("serial")
public class SaleItemCategoryMap extends HashMap<UUID, ISaleItem> {
    
    private static final int MAX_SLOTS = 2916; // (6 *9) * (6 * 9)
        
    
    public boolean canAdd(UUID sellerId, ItemStack itemStack, int amount) {
        PreCon.notNull(itemStack);

        int room = getSpace(sellerId, itemStack, amount);
        
        return room >= amount;
    }
        
    public int getSpace(UUID sellerId, ItemStack itemStack) {
        return getSpace(sellerId, itemStack, -1);
    }
    
    private int getSpace(UUID sellerId, ItemStack itemStack, int amount) {
        PreCon.notNull(itemStack);

        MaterialExt materialExt = MaterialExt.from(itemStack.getType());
        
        List<ISaleItem> saleItems = new ArrayList<>(this.values());
        
        int slotsUsed = 0;
        int partialStackSpace = 0;
                        
        for (ISaleItem saleItem : saleItems) {
            
            MaterialExt saleMaterial = MaterialExt.from(saleItem.getItemStack().getType());
            
            int maxStackSize = saleMaterial.getMaxStackSize();            
            int qty = saleItem.getQty();
            
            slotsUsed += (int)Math.ceil((double)qty / maxStackSize);
            
            if (StoreStackComparer.getDefault().isSame(itemStack, saleItem.getItemStack()) &&
                    saleItem.getSellerId().equals(sellerId)) {
                
                partialStackSpace += qty >= maxStackSize ? qty % maxStackSize : maxStackSize - qty;
                
                if (amount >= 0 && partialStackSpace >= amount)
                    return partialStackSpace;
            }
            
            if (amount >= 0 && slotsUsed >= MAX_SLOTS)
                return 0;
        }
        
        int emptySlots = MAX_SLOTS - slotsUsed;
        int totalRoom = (materialExt.getMaxStackSize() * emptySlots) + partialStackSpace;
        
        return totalRoom;
    }

}
