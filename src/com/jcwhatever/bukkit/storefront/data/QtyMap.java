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
 * Maps {@link ItemStack}'s to a quantity value.
 *
 * <p>Does not set quantity for specific {@link org.bukkit.inventory.ItemStack} instances. By default,
 * stores quantity based on type and meta but item quantities can be retrieved based on other properties
 * using {@link com.jcwhatever.nucleus.utils.items.MatchableItem}.</p>
 */
public class QtyMap {

    private IStore _store;
    private Player _seller;
    private Map<MatchableItem, Integer> _qtyMap = new HashMap<MatchableItem, Integer>(7 * 9);

    /**
     * Constructor.
     *
     * @param seller  The seller of the items in the quantity map.
     * @param store   The store the items are for/from.
     */
    public QtyMap(Player seller, IStore store) {
        PreCon.notNull(seller);
        PreCon.notNull(store);

        _store = store;
        _seller = seller;
    }

    /**
     * Get the quantity of the specified {@link org.bukkit.inventory.ItemStack}.
     *
     * <p>The quantity/amount of the specified  {@link org.bukkit.inventory.ItemStack}
     * is not considered</p>
     *
     * <p>The {@link org.bukkit.inventory.ItemStack} is matched based on type and meta.</p>
     *
     * @param itemStack  The {@link ItemStack} to check.
     */
    @Nullable
    public int get(ItemStack itemStack) {
        return get(getMatchable(itemStack));
    }

    /**
     * Get the quantity of the {@link org.bukkit.inventory.ItemStack} that matches the specified
     * {@link com.jcwhatever.nucleus.utils.items.MatchableItem}.
     *
     * @param matchable  The {@link com.jcwhatever.nucleus.utils.items.MatchableItem} used to match
     *                   against stored {@link org.bukkit.inventory.ItemStack} quantities.
     */
    public int get(MatchableItem matchable) {

        Integer qty = _qtyMap.get(matchable);

        if (qty == null) {
            SaleItem saleItem = _store.getSaleItem(_seller.getUniqueId(), matchable.getItem());

            if (saleItem != null) {
                qty = saleItem.getQty();
                _qtyMap.put(matchable, qty);
            }
            else {
                return 0;
            }

        }
        return qty;
    }

    /**
     * Set the quantity of an {@link org.bukkit.inventory.ItemStack}.
     *
     * <p>The quantity/amount of the specified {@link org.bukkit.inventory.ItemStack}
     * is not considered</p>
     *
     * <p>The specified {@link org.bukkit.inventory.ItemStack} is matched based on type and meta.</p>
     *
     * @param itemStack  The {@link org.bukkit.inventory.ItemStack} to set quantity on.
     * @param qty        The quantity to set.
     */
    public void set(ItemStack itemStack, int qty) {
        PreCon.notNull(itemStack);

        set(getMatchable(itemStack), qty);
    }

    /**
     * Set the quantity of an {@link org.bukkit.inventory.ItemStack} matched using a
     * {@link com.jcwhatever.nucleus.utils.items.MatchableItem}.
     *
     * @param matchable  The {@link com.jcwhatever.nucleus.utils.items.MatchableItem} used to
     *                   match against the stored {@link org.bukkit.inventory.ItemStack} quantity.
     * @param qty        The quantity to set.
     */
    public void set(MatchableItem matchable, int qty) {
        PreCon.notNull(matchable);

        _qtyMap.put(matchable, qty);
    }

    /**
     * Clear quantity for the specified {@link ItemStack}.
     *
     * <p>The quantity/amount of the specified {@link org.bukkit.inventory.ItemStack}
     * is not considered</p>
     *
     * <p>The specified {@link org.bukkit.inventory.ItemStack} is matched based on type and meta.</p>
     *
     * @param itemStack  The {@link org.bukkit.inventory.ItemStack} whose quantity is to be cleared.
     */
    public void clear(ItemStack itemStack) {
        PreCon.notNull(itemStack);

        clear(getMatchable(itemStack));
    }

    /**
     * Clear quantity for the specified {@link ItemStack} that matches
     * the specified {@link com.jcwhatever.nucleus.utils.items.MatchableItem}.
     *
     * @param matchable  The {@link com.jcwhatever.nucleus.utils.items.MatchableItem} used to match
     *                   the {@link org.bukkit.inventory.ItemStack} to clear.
     */
    public void clear(MatchableItem matchable) {
        _qtyMap.remove(matchable);
    }

    private MatchableItem getMatchable(ItemStack itemStack) {

        itemStack = itemStack.clone();
        ItemStackUtil.removeTempLore(itemStack);
        return new MatchableItem(itemStack, StoreStackMatcher.getDefault());
    }

}

