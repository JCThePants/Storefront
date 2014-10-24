package com.jcwhatever.bukkit.storefront.data;

import org.bukkit.inventory.Inventory;

import com.jcwhatever.bukkit.generic.inventory.InventorySnapshot;
import com.jcwhatever.bukkit.storefront.utils.StoreStackComparer;

public class SaleItemSnapshot extends InventorySnapshot {
    
    public SaleItemSnapshot(Inventory inventory) {
        super(inventory, StoreStackComparer.getDefault());
    }

    
}
