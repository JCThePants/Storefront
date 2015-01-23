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


package com.jcwhatever.bukkit.storefront.stores;

import com.jcwhatever.bukkit.storefront.Category;
import com.jcwhatever.bukkit.storefront.Msg;
import com.jcwhatever.bukkit.storefront.StoreType;
import com.jcwhatever.bukkit.storefront.Storefront;
import com.jcwhatever.bukkit.storefront.data.ISaleItem;
import com.jcwhatever.bukkit.storefront.data.SaleItem;
import com.jcwhatever.bukkit.storefront.data.SaleItemCategoryMap;
import com.jcwhatever.bukkit.storefront.data.WantedItems;
import com.jcwhatever.bukkit.storefront.utils.StoreStackMatcher;
import com.jcwhatever.bukkit.storefront.views.mainmenu.MainMenuView;
import com.jcwhatever.nucleus.storage.DataBatchOperation;
import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.utils.items.MatchableItem;
import com.jcwhatever.nucleus.views.ViewSession;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerStore extends AbstractStore {

    private Map<UUID, ISaleItem> _idMap;
    private Map<MatchableItem, ISaleItem> _stackMap;


    public PlayerStore(String name, IDataNode storeNode) {

        super(name, storeNode);
    }

    @Override
    public StoreType getStoreType () {
        return StoreType.PLAYER_OWNABLE;
    }

    @Override
    public void view (Block sourceBlock, Player p) {

        assert getOwnerId() != null;
        
        if (!hasOwner()) {

            Msg.tell(p, "This store is out of business.");
            return;
        }

        if (!getOwnerId().equals(p.getUniqueId()) &&
                getSaleItems().size() == 0 &&
                getWantedItems().getAll().size() == 0) {
            
            Msg.tell(p, "Out of stock.");
            return;
        }

        ViewSession session = ViewSession.get(p, sourceBlock);
        session.next(new MainMenuView());
    }

    @Override
    public SaleItem getSaleItem (UUID itemId) {
        return (SaleItem)_idMap.get(itemId);
    }

    @Override
    public SaleItem getSaleItem (UUID sellerId, ItemStack itemStack) {

        if (!sellerId.equals(getOwnerId()))
            throw new RuntimeException("The seller is not the owner of the store.");

        MatchableItem wrapper = new MatchableItem(itemStack, StoreStackMatcher.getDefault());

        return (SaleItem)_stackMap.get(wrapper);
    }

    @Override
    public List<ISaleItem> getSaleItems () {
        return new ArrayList<>(_idMap.values());
    }

    @Override
    public List<ISaleItem> getSaleItems (Category category) {

        SaleItemCategoryMap map = getCategoryMap(category);
        if (map == null)
            return new ArrayList<>(0);

        return new ArrayList<>(map.values());
    }

    @Override
    public List<ISaleItem> getSaleItems (UUID sellerId) {

        if (!sellerId.equals(getOwnerId()))
            return new ArrayList<>(0);

        return new ArrayList<>(_idMap.values());
    }

    @Override
    public SaleItem addSaleItem (Player seller, ItemStack itemStack, int qty, double pricePerUnit) {

        // make sure the item does not already exist
        SaleItem saleItem = getSaleItem(getOwnerId(), itemStack);
        if (saleItem != null) {
            // update item
            return updateAddSaleItem(saleItem, qty, pricePerUnit);
        }

        // get category for item
        Category category = Storefront.getInstance().getCategoryManager().getCategory(itemStack);
        if (category == null)
            return null;

        // create unique id
        UUID itemId = null;
        while (itemId == null) {
            itemId = UUID.randomUUID();
            if (_idMap.containsKey(itemId))
                itemId = null;
        }

        // get data node for item
        IDataNode itemNode = getItemNode(itemId);

        // create new sale item, constructor saves info to itemNode
        SaleItem item = new SaleItem(this, getOwnerId(), itemId, itemStack, qty, pricePerUnit,
                itemNode);

        // put sale item into maps
        _idMap.put(itemId, item);
        _stackMap.put(item.getWrapper(), item);

        SaleItemCategoryMap categoryMap = getCategoryMap(category);
        categoryMap.put(itemId, item);

        return item;
    }

    private SaleItem updateAddSaleItem (final SaleItem saleItem, final int qty,
                                        final double pricePerUnit) {

        getDataNode().runBatchOperation(new DataBatchOperation() {

            @Override
            public void run (IDataNode dataNode) {

                int newQty = saleItem.getQty() + qty;

                saleItem.setPricePerUnit(pricePerUnit);
                saleItem.setQty(newQty);
            }

        });

        return saleItem;
    }

    @Override
    public SaleItem removeSaleItem (UUID itemId) {

        SaleItem item = (SaleItem)_idMap.remove(itemId);
        if (item == null)
            return null;

        Category category = item.getCategory();
        if (category == null)
            return null;

        _stackMap.remove(item.getWrapper());

        IDataNode itemNode = getItemNode(itemId);
        itemNode.remove();
        itemNode.save();

        SaleItemCategoryMap categoryMap = getCategoryMap(category);
        categoryMap.remove(itemId);

        return item;
    }


    @Override
    public SaleItem removeSaleItem (UUID sellerId, ItemStack itemStack) {

        if (!sellerId.equals(getOwnerId()))
            return null;

        MatchableItem wrapper = new MatchableItem(itemStack, StoreStackMatcher.getDefault());

        // remove from map
        SaleItem saleItem = (SaleItem)_stackMap.remove(wrapper);
        if (saleItem == null)
            return null;

        // remove from maps
        _idMap.remove(saleItem.getItemId());
        SaleItemCategoryMap catMap = this.getCategoryMap(saleItem.getCategory());
        if (catMap != null) {
            catMap.remove(saleItem.getItemId());
        }

        // remove from data node
        IDataNode itemNode = getItemNode(saleItem.getItemId());
        itemNode.remove();
        itemNode.save();

        return saleItem;
    }


    @Override
    public SaleItem removeSaleItem (UUID sellerId, ItemStack itemStack, int qty) {

        if (!sellerId.equals(getOwnerId()))
            return null;

        MatchableItem wrapper = new MatchableItem(itemStack, StoreStackMatcher.getDefault());

        // get sale item from map
        SaleItem saleItem = (SaleItem)_stackMap.get(wrapper);
        if (saleItem == null)
            return null;

        // check quantity to see if complete removal is required
        if (qty >= saleItem.getQty()) {
            return removeSaleItem(getOwnerId(), itemStack);
        }

        // update quantity
        int newQty = saleItem.getQty() - qty;
        saleItem.setQty(newQty);

        return saleItem;
    }


    @Override
    public boolean clearSaleItems (final UUID sellerId) {

        if (!sellerId.equals(getOwnerId()))
            return false;

        getDataNode().runBatchOperation(new DataBatchOperation() {

            @Override
            public void run (IDataNode dataNode) {

                List<ISaleItem> items = getSaleItems();

                for (ISaleItem item : items) {
                    removeSaleItem(getOwnerId(), item.getItemStack());
                }
            }

        });

        return true;

    }

    @Override
    protected void onLoadSettings (IDataNode storeNode) {

        // do nothing
    }

    @Override
    public List<Category> getSellCategories () {

        WantedItems wantedItems = getWantedItems();
        List<ISaleItem> saleItems = wantedItems.getAll();

        Set<Category> categories = new HashSet<>(20);

        for (ISaleItem saleItem : saleItems) {
            categories.add(saleItem.getCategory());
        }

        return new ArrayList<Category>(categories);
    }


    @Override
    public List<Category> getBuyCategories () {

        List<ISaleItem> saleItems = this.getSaleItems();

        Set<Category> categories = new HashSet<>(20);

        for (ISaleItem saleItem : saleItems) {
            categories.add(saleItem.getCategory());
        }

        return new ArrayList<Category>(categories);
    }


    @Override
    protected void onInit () {

        _idMap = new HashMap<>(100);
        _stackMap = new HashMap<>(100);
    }


    @Override
    protected void onSaleItemLoaded (SaleItem saleItem) {

        _idMap.put(saleItem.getItemId(), saleItem);
        _stackMap.put(saleItem.getWrapper(), saleItem);

        SaleItemCategoryMap categoryMap = getCategoryMap(saleItem.getCategory());
        categoryMap.put(saleItem.getItemId(), saleItem);
    }
    
}
