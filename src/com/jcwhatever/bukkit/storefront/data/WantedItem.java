package com.jcwhatever.bukkit.storefront.data;

import java.util.UUID;

import org.bukkit.inventory.ItemStack;

import com.jcwhatever.bukkit.generic.storage.IDataNode;
import com.jcwhatever.bukkit.storefront.stores.IStore;

public class WantedItem extends SaleItem {

    public WantedItem(IStore store, UUID sellerId, UUID itemId, ItemStack itemStack, int qty,
                      double pricePerUnit) {

        this(store, sellerId, itemId, itemStack, qty, pricePerUnit, null);
    }


    public WantedItem(IStore store, UUID sellerId, UUID itemId, ItemStack itemStack, int qty,
                      double pricePerUnit, IDataNode dataNode) {

        super(store, sellerId, itemId, itemStack, qty, pricePerUnit, dataNode);
    }


    public WantedItem(IStore store, UUID itemId, IDataNode dataNode) {

        super(store, itemId, dataNode);
    }


    @Override
    protected void onRemove (UUID itemId) {

        getStore().getWantedItems().remove(itemId);
    }

}
