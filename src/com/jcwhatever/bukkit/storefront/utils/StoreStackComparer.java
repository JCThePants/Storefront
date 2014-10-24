package com.jcwhatever.bukkit.storefront.utils;

import com.jcwhatever.bukkit.generic.utils.PreCon;
import org.bukkit.inventory.ItemStack;

import com.jcwhatever.bukkit.generic.items.ItemStackComparer;

/**
 * Extends {@code ItemStackComparer} functionality by
 * removing temporary lore from item stacks before comparing them.
 */
public class StoreStackComparer extends ItemStackComparer {
    
    private static StoreStackComparer _default;
    private static StoreStackComparer _durability;

    /**
     * Get the default {@code StoreStackComparer} singleton instance
     * which compares based on type and meta data.
     */
    public static StoreStackComparer getDefault() {
        if (_default == null)
            _default = new StoreStackComparer(ItemStackComparer.DEFAULT_COMPARE);
        
        return _default;
    }

    /**
     * Get the {@code StoreStackComparer} singleton instance which
     * compares based on type, meta data, and durability.
     */
    public static StoreStackComparer getDurability() {
        if (_durability == null) {
            _durability = new StoreStackComparer(ItemStackComparer.DURABILITY_COMPARE);
        }
        
        return _durability;
    }

    /**
     * Constructor.
     *
     * @param compareOperations  {@code ItemStackComparer} compare operations bit flags.
     */
    public StoreStackComparer(byte compareOperations) {
        super(compareOperations);
    }


    /**
     * Compare two item stacks using the defined compare operations.
     *
     * @param itemStack1  The first item stack
     * @param itemStack2  The second item stack
     */
    @Override
    public boolean isSame(ItemStack itemStack1, ItemStack itemStack2) {
        PreCon.notNull(itemStack1);
        PreCon.notNull(itemStack2);
        
        ItemStack clone1 = itemStack1.clone();
        ItemStack clone2 = itemStack2.clone();
        
        ItemStackUtil.removeTempLore(clone1);
        ItemStackUtil.removeTempLore(clone2);
        
        return super.isSame(clone1, clone2);
    }
    
}
