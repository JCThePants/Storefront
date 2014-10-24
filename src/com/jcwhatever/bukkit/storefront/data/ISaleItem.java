package com.jcwhatever.bukkit.storefront.data;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;

import com.jcwhatever.bukkit.generic.items.ItemWrapper;
import com.jcwhatever.bukkit.storefront.Category;
import com.jcwhatever.bukkit.storefront.data.SaleItem.SaleItemStack;
import com.jcwhatever.bukkit.storefront.stores.IStore;


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

    public List<SaleItemStack> getSaleItemStacks ();
    
    public ISaleItem getParent();
    
    public void increment (int amount);    
}
