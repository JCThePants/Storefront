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

import com.jcwhatever.bukkit.storefront.Category;
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

public class WantedItems {

    private Map<Category, SaleItemCategoryMap> _wantedCategoryMap = new HashMap<Category, SaleItemCategoryMap>(25);
    private Map<UUID, ISaleItem> _wantedIdMap = new HashMap<>(25);
    private Map<MatchableItem, ISaleItem> _wantedMap = new HashMap<>(25);

    private IDataNode _wantedNode;
    private IStore _store;


    public WantedItems(IStore store, IDataNode wantedNode) {

        _wantedNode = wantedNode;
        _store = store;

        loadSettings();
    }

    public List<ISaleItem> getAll () {
        return new ArrayList<>(_wantedIdMap.values());
    }

    public List<ISaleItem> get (Category category) {

        SaleItemCategoryMap map = _wantedCategoryMap.get(category);
        if (map == null)
            return new ArrayList<>(0);

        return new ArrayList<>(map.values());
    }

    public ISaleItem get (UUID itemId) {

        return _wantedIdMap.get(itemId);
    }

    public ISaleItem get (ItemStack item) {

        PreCon.notNull(item);

        MatchableItem wrapper = new MatchableItem(item, StoreStackMatcher.getDefault());

        return _wantedMap.get(wrapper);
    }


    @Nullable
    public SaleItem add (ItemStack itemStack, int qty, double pricePerUnit) {

        PreCon.notNull(itemStack);

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
        _wantedMap.put(item.getWrapper(), item);

        SaleItemCategoryMap categoryMap = getCategoryMap(category);
        categoryMap.put(itemId, item);

        return item;
    }

    @Nullable
    public ISaleItem remove (UUID itemId) {

        PreCon.notNull(itemId);

        ISaleItem item = _wantedIdMap.remove(itemId);
        if (item == null)
            return null;

        Category category = item.getCategory();
        if (category == null)
            return null;

        _wantedNode.remove(itemId.toString());

        _wantedMap.remove(item.getWrapper());

        SaleItemCategoryMap categoryMap = getCategoryMap(category);
        categoryMap.remove(itemId);

        return item;
    }


    private SaleItemCategoryMap getCategoryMap (Category category) {

        PreCon.notNull(category);

        SaleItemCategoryMap saleItems = _wantedCategoryMap.get(category);
        if (saleItems == null) {
            saleItems = new SaleItemCategoryMap();
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

            _wantedMap.put(saleItem.getWrapper(), saleItem);
            _wantedIdMap.put(saleItem.getItemId(), saleItem);

            SaleItemCategoryMap categoryMap = getCategoryMap(saleItem.getCategory());
            categoryMap.put(saleItem.getItemId(), saleItem);

        }
    }
}
