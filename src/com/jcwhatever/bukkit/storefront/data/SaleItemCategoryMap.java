package com.jcwhatever.bukkit.storefront.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;

import com.jcwhatever.bukkit.generic.extended.MaterialExt;
import com.jcwhatever.bukkit.generic.utils.PreCon;
import com.jcwhatever.bukkit.storefront.utils.StoreStackComparer;

@SuppressWarnings("serial")
public class SaleItemCategoryMap extends HashMap<UUID, SaleItem> {
    
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
        
        List<SaleItem> saleItems = new ArrayList<SaleItem>(this.values());
        
        int slotsUsed = 0;
        int partialStackSpace = 0;
                        
        for (SaleItem saleItem : saleItems) {
            
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
