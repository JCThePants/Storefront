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

import com.jcwhatever.bukkit.storefront.utils.StoreStackMatcher;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.items.ItemStackMatcher;
import com.jcwhatever.nucleus.utils.items.MatchableItem;

import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import javax.annotation.Nullable;

/**
 * Maps {@link org.bukkit.inventory.ItemStack}'s to {@link ISaleItem}'s
 * using {@link com.jcwhatever.nucleus.utils.items.MatchableItem}'s as key.
 */
@SuppressWarnings("serial")
public class SaleItemMap extends HashMap<MatchableItem, ISaleItem> {

    /**
     * Get an {@link ISaleItem} from the map using an
     * {@link org.bukkit.inventory.ItemStack}.
     *
     * <p>The {@link org.bukkit.inventory.ItemStack} is matched based
     * on type and meta.</p>
     *
     * @param key  The {@link org.bukkit.inventory.ItemStack}.
     *
     * @return  The {@link ISaleItem} or null if not found.
     */
    @Nullable
    public ISaleItem get(ItemStack key) {
        return get(key, StoreStackMatcher.getDefault());
    }

    /**
     * Get an {@link ISaleItem} from the map using an
     * {@link org.bukkit.inventory.ItemStack} that is matched using
     * the specified {@link com.jcwhatever.nucleus.utils.items.ItemStackMatcher}.
     *
     * @param key      The {@link org.bukkit.inventory.ItemStack}.
     * @param matcher  The {@link com.jcwhatever.nucleus.utils.items.ItemStackMatcher}.
     *
     * @return  The {@link ISaleItem} or null if not found.
     */
    @Nullable
    public ISaleItem get(ItemStack key, ItemStackMatcher matcher) {
        PreCon.notNull(key);
        PreCon.notNull(matcher);

        MatchableItem wrapper = new MatchableItem(key, matcher);
        return super.get(wrapper);
    }

    /**
     * Put an {@link ISaleItem} into the map keyed to the specified
     * {@link org.bukkit.inventory.ItemStack}.
     *
     * <p>The provided item is wrapped in a {@link com.jcwhatever.nucleus.utils.items.MatchableItem}</p>
     *
     * @param key    The {@link org.bukkit.inventory.ItemStack} key.
     * @param value  The {@link ISaleItem} value.
     *
     * @return  The previous value.
     */
    @Nullable
    public ISaleItem put(ItemStack key, SaleItem value) {
        PreCon.notNull(key);
        PreCon.notNull(value);

        MatchableItem matchable = new MatchableItem(key);
        return super.put(matchable, value);
    }

    /**
     * Remove a {@link ISaleItem} from the map using the specified
     * {@link org.bukkit.inventory.ItemStack} as key.
     *
     * <p>The {@link org.bukkit.inventory.ItemStack} is matched based
     * on type and meta.</p>
     *
     * @param key  The {@link org.bukkit.inventory.ItemStack} key.
     *
     * @return  The removed value or null if not found.
     */
    @Nullable
    public ISaleItem remove(ItemStack key) {
        PreCon.notNull(key);

        return remove(key, StoreStackMatcher.getDefault());
    }

    /**
     * Remove a {@link ISaleItem} from the map using the specified
     * {@link org.bukkit.inventory.ItemStack} that is matched using
     * the specified {@link com.jcwhatever.nucleus.utils.items.ItemStackMatcher}.
     *
     * @param key      The {@link org.bukkit.inventory.ItemStack} key.
     * @param matcher  The {@link com.jcwhatever.nucleus.utils.items.ItemStackMatcher}.
     *
     * @return  The removed value or null if not found.
     */
    @Nullable
    public ISaleItem remove(ItemStack key, ItemStackMatcher matcher) {
        PreCon.notNull(key);
        PreCon.notNull(matcher);

        MatchableItem matchable = new MatchableItem(key, matcher);
        return super.remove(matchable);
    }

    /**
     * Determine if the map contains a {@link ISaleItem} of the
     * specified {@link org.bukkit.inventory.ItemStack}.
     *
     * <p>The {@link org.bukkit.inventory.ItemStack} is matched based
     * on type and meta.</p>
     *
     * @param key  The {@link org.bukkit.inventory.ItemStack} key.
     */
    public boolean containsKey (ItemStack key) {
        return containsKey(key, StoreStackMatcher.getDefault());
    }

    /**
     * Determine if the map contains an {@link ISaleItem} of the
     * specified {@link org.bukkit.inventory.ItemStack} matched using
     * the specified {@link com.jcwhatever.nucleus.utils.items.ItemStackMatcher}.
     *
     * @param key      The {@link org.bukkit.inventory.ItemStack} key.
     * @param matcher  The {@link com.jcwhatever.nucleus.utils.items.ItemStackMatcher}.
     */
    public boolean containsKey (ItemStack key, ItemStackMatcher matcher) {
        PreCon.notNull(key);
        PreCon.notNull(matcher);

        MatchableItem matchable = new MatchableItem(key, matcher);
        return super.containsKey(matchable);
    }
}
