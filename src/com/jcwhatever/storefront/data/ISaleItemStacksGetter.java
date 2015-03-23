package com.jcwhatever.storefront.data;

import java.util.List;

/**
 * A type that can give a list of {@link ISaleItem}'s that
 * each represent am {@link org.bukkit.inventory.ItemStack} up to its max stack size.
 */
public interface ISaleItemStacksGetter {

    /**
     * Get a list that represents a {@link ISaleItem} divided
     * into child sale items based on the max quantity the sale item can fit into an
     * {@link org.bukkit.inventory.ItemStack}.
     *
     * <p>Each {@link ISaleItem} in the returned list has a quantity that is the max
     * stack size of the represented {@link org.bukkit.inventory.ItemStack} with the
     * last {@link ISaleItem} containing the remainder.</p>
     */
    List<ISaleItem> getStacks();
}
