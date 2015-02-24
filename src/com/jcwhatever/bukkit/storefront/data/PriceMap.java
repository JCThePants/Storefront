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

import com.jcwhatever.bukkit.storefront.stores.IStore;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil;
import com.jcwhatever.bukkit.storefront.utils.StoreStackMatcher;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.items.MatchableItem;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Maps {@link ItemStack}'s to a per unit price.
 *
 * <p>Does not set price for specific {@link org.bukkit.inventory.ItemStack} instances. By default,
 * stores price based on type and meta but item price can be retrieved based on other properties
 * using {@link com.jcwhatever.nucleus.utils.items.MatchableItem}.</p>
 */
public class PriceMap {

    private final IStore _store;
    private final Player _seller;
    private final Map<MatchableItem, Double> _priceMap = new HashMap<MatchableItem, Double>(7 * 9);

    /**
     * Constructor.
     *
     * @param seller  The seller of the items in the price map.
     * @param store   The store the price map is for.
     */
    public PriceMap(Player seller, IStore store) {
        PreCon.notNull(seller);
        PreCon.notNull(store);

        _store = store;
        _seller = seller;
    }

    /**
     * Get the per unit price of an {@link org.bukkit.inventory.ItemStack}.
     *
     * <p>The quantity/amount of the {@link org.bukkit.inventory.ItemStack} is not considered</p>
     *
     * <p>The {@link org.bukkit.inventory.ItemStack} is matched based on type and meta.</p>
     *
     * @param itemStack  The {@link org.bukkit.inventory.ItemStack} to check.
     *
     * @return  The price of the item or null if no price set.
     */
    @Nullable
    public Double get(ItemStack itemStack) {
        PreCon.notNull(itemStack);

        return get(getMatchable(itemStack));
    }

    /**
     * Get the per unit price of an {@link org.bukkit.inventory.ItemStack} represented
     * with a {@link com.jcwhatever.nucleus.utils.items.MatchableItem} used to match the
     * item to an item in the price map based on certain properties.
     *
     * @param matchable  The {@link com.jcwhatever.nucleus.utils.items.MatchableItem} to check.
     *
     * @return  The price of the item or null if no price set.
     */
    @Nullable
    public Double get(MatchableItem matchable) {
        PreCon.notNull(matchable);

        Double price = _priceMap.get(matchable);

        if (price == null) {
            SaleItem saleItem = _store.getSaleItem(_seller.getUniqueId(), matchable.getItem());

            if (saleItem != null) {
                price = saleItem.getPricePerUnit();
                _priceMap.put(matchable, price);
            }
        }

        return price;
    }

    /**
     * Set the per unit price of an {@link org.bukkit.inventory.ItemStack}.
     *
     * <p>The quantity/amount of the {@link org.bukkit.inventory.ItemStack} is not considered</p>
     *
     * <p>The {@link org.bukkit.inventory.ItemStack} price is stored based on the items
     * type and meta.</p>
     *
     * @param itemStack  The {@link org.bukkit.inventory.ItemStack} to set a price on.
     * @param price      The per unit price.
     */
    public void set(ItemStack itemStack, double price) {
        PreCon.notNull(itemStack);

        set(getMatchable(itemStack), price);
    }

    /**
     * Set the per unit price of an {@link org.bukkit.inventory.ItemStack} represented
     * with a {@link com.jcwhatever.nucleus.utils.items.MatchableItem} used to match the
     * item to an item in the price map based on certain properties.
     *
     * @param matchable  The {@link com.jcwhatever.nucleus.utils.items.MatchableItem} used to
     *                   set the {@link org.bukkit.inventory.ItemStack} price.
     * @param price      The per unit price.
     */
    public void set(MatchableItem matchable, double price) {
        PreCon.notNull(matchable);

        _priceMap.put(matchable, price);
    }

    /**
     * Clear price of the specified {@link org.bukkit.inventory.ItemStack}.
     *
     * <p>The quantity/amount of the {@link org.bukkit.inventory.ItemStack} is not considered.</p>
     *
     * <p>The {@link org.bukkit.inventory.ItemStack} whose price is cleared is determined based on
     * the specified items type and meta.</p>
     *
     * @param itemStack  The {@link org.bukkit.inventory.ItemStack} that represents the item to be cleared.
     */
    public void clear(ItemStack itemStack) {
        PreCon.notNull(itemStack);

        clear(getMatchable(itemStack));
    }

    /**
     * Clear the price of an {@link org.bukkit.inventory.ItemStack} represented
     * with a {@link com.jcwhatever.nucleus.utils.items.MatchableItem} used to match the
     * item to an item in the price map based on certain properties.
     *
     * @param matchable  The {@link com.jcwhatever.nucleus.utils.items.MatchableItem} used to
     *                   match the {@link ItemStack} whose price is to be cleared.
     */
    public void clear(MatchableItem matchable) {
        PreCon.notNull(matchable);

        _priceMap.remove(matchable);
    }

    private MatchableItem getMatchable(ItemStack itemStack) {

        itemStack = itemStack.clone();
        ItemStackUtil.removeTempLore(itemStack);
        return new MatchableItem(itemStack, StoreStackMatcher.getDefault());
    }
}
