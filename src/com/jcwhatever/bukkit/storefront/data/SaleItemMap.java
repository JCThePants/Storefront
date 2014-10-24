package com.jcwhatever.bukkit.storefront.data;

import java.util.HashMap;

import org.bukkit.inventory.ItemStack;

import com.jcwhatever.bukkit.generic.items.ItemStackComparer;
import com.jcwhatever.bukkit.generic.items.ItemWrapper;
import com.jcwhatever.bukkit.storefront.utils.StoreStackComparer;

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
