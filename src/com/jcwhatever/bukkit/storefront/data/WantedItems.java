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

import com.jcwhatever.bukkit.storefront.category.Category;
import com.jcwhatever.bukkit.storefront.Msg;
import com.jcwhatever.bukkit.storefront.Storefront;
import com.jcwhatever.bukkit.storefront.stores.IStore;
import com.jcwhatever.bukkit.storefront.utils.StoreStackMatcher;
import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.items.MatchableItem;
import com.jcwhatever.nucleus.utils.text.TextUtils;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;

/**
 * Manages {@link WantedItem}'s for a specific {@link IStore}.
 */
public class WantedItems {

    private final IStore _store;
    private final IDataNode _wantedNode;

    private final Map<Category, SaleItemIDMap> _wantedCategoryMap = new HashMap<Category, SaleItemIDMap>(25);
    private final Map<UUID, ISaleItem> _wantedIdMap = new HashMap<>(25);
    private final Map<MatchableItem, ISaleItem> _wantedMap = new HashMap<>(25);

    /**
     * Constructor.
     *
     * @param store       The store the instance is for.
     * @param wantedNode  The data storage node.
     */
    public WantedItems(IStore store, IDataNode wantedNode) {
        PreCon.notNull(store);
        PreCon.notNull(wantedNode);

        _wantedNode = wantedNode;
        _store = store;

        loadSettings();
    }

    /**
     * Get all wanted items as {@link ISaleItem}'s.
     */
    public List<ISaleItem> getAll() {
        return new ArrayList<>(_wantedIdMap.values());
    }

    /**
     * Get all wanted items for the specified category as
     * {@link ISaleItem}'s.
     *
     * @param category  The {@link Category} of the items to get.
     */
    public List<ISaleItem> get(Category category) {

        SaleItemIDMap map = _wantedCategoryMap.get(category);
        if (map == null)
            return new ArrayList<>(0);

        return new ArrayList<>(map.values());
    }

    /**
     * Get a wanted item by item ID.
     *
     * @param itemId  The ID of the item.
     *
     * @return  The wanted item as a {@link ISaleItem} or null if not found.
     */
    @Nullable
    public ISaleItem get(UUID itemId) {
        PreCon.notNull(itemId);

        return _wantedIdMap.get(itemId);
    }

    /**
     * Get a wanted item by {@link org.bukkit.inventory.ItemStack}.
     *
     * <p>The {@link org.bukkit.inventory.ItemStack} is matched by type and meta.</p>
     *
     * @param item  The {@link org.bukkit.inventory.ItemStack}.
     *
     * @return  The wanted item as a {@link ISaleItem} or null if not found.
     */
    @Nullable
    public ISaleItem get(ItemStack item) {
        PreCon.notNull(item);

        MatchableItem matchable = new MatchableItem(item, StoreStackMatcher.getDefault());
        return _wantedMap.get(matchable);
    }

    /**
     * Add a wanted item.
     *
     * @param itemStack     The {@link ItemStack} that represents the item wanted.
     * @param qty           The quantity of items wanted.
     * @param pricePerUnit  The price to be payed per item.
     *
     * @return  The new wanted item as a {@link ISaleItem} or null if no category was
     * found for the item.
     */
    @Nullable
    public ISaleItem add(ItemStack itemStack, int qty, double pricePerUnit) {
        PreCon.notNull(itemStack);
        PreCon.greaterThanZero(qty);
        PreCon.greaterThanZero(pricePerUnit);

        Category category = Storefront.getCategoryManager().get(itemStack);
        if (category == null)
            return null;

        UUID itemId = null;

        while (itemId == null) {
            itemId = UUID.randomUUID();
            if (_wantedIdMap.containsKey(itemId))
                itemId = null;
        }

        IDataNode itemNode = _wantedNode.getNode(itemId.toString());

        // constructor saves to itemNode
        WantedItem item = new WantedItem(_store, _store.getOwnerId(), itemId, itemStack, qty,
                pricePerUnit, itemNode);

        _wantedIdMap.put(itemId, item);
        _wantedMap.put(item.getMatchable(), item);

        SaleItemIDMap categoryMap = getCategoryMap(category);
        categoryMap.put(itemId, item);

        return item;
    }

    /**
     * Remove a wanted item by item ID.
     *
     * @param itemId  The ID of the item to remove.
     *
     * @return  The removed item or null if not found.
     */
    @Nullable
    public ISaleItem remove(UUID itemId) {
        PreCon.notNull(itemId);

        ISaleItem item = _wantedIdMap.remove(itemId);
        if (item == null)
            return null;

        Category category = item.getCategory();
        if (category == null)
            return null;

        _wantedNode.remove(itemId.toString());

        _wantedMap.remove(item.getMatchable());

        SaleItemIDMap categoryMap = getCategoryMap(category);
        categoryMap.remove(itemId);

        return item;
    }

    private SaleItemIDMap getCategoryMap (Category category) {

        SaleItemIDMap saleItems = _wantedCategoryMap.get(category);
        if (saleItems == null) {
            saleItems = new SaleItemIDMap();
            _wantedCategoryMap.put(category, saleItems);
        }

        return saleItems;
    }

    private void loadSettings () {

        for (IDataNode node : _wantedNode) {

            UUID itemId = TextUtils.parseUUID(node.getName());
            if (itemId == null) {
                Msg.debug("Failed to parse Item Id: {0}", node.getName());
                continue;
            }

            WantedItem saleItem = new WantedItem(_store, itemId, node);

            if (saleItem.getItemStack() == null) {
                Msg.debug("Failed to parse sale item stack.");
                continue;
            }

            if (saleItem.getCategory() == null)
                continue;

            _wantedMap.put(saleItem.getMatchable(), saleItem);
            _wantedIdMap.put(saleItem.getId(), saleItem);

            SaleItemIDMap categoryMap = getCategoryMap(saleItem.getCategory());
            categoryMap.put(saleItem.getId(), saleItem);
        }
    }
}
