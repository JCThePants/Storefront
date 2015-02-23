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
import com.jcwhatever.bukkit.storefront.StoreManager;
import com.jcwhatever.bukkit.storefront.StoreType;
import com.jcwhatever.bukkit.storefront.Storefront;
import com.jcwhatever.bukkit.storefront.data.ISaleItem;
import com.jcwhatever.bukkit.storefront.data.SaleItem;
import com.jcwhatever.bukkit.storefront.data.SaleItemCategoryMap;
import com.jcwhatever.bukkit.storefront.data.SaleItemMap;
import com.jcwhatever.bukkit.storefront.utils.StoreStackMatcher;
import com.jcwhatever.bukkit.storefront.views.mainmenu.MainMenuView;
import com.jcwhatever.nucleus.providers.bankitems.IBankItemsAccount;
import com.jcwhatever.nucleus.providers.economy.TransactionFailException;
import com.jcwhatever.nucleus.storage.DataBatchOperation;
import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.utils.BankItems;
import com.jcwhatever.nucleus.utils.Economy;
import com.jcwhatever.nucleus.utils.inventory.InventoryUtils;
import com.jcwhatever.nucleus.views.ViewSession;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;

public class ServerStore extends AbstractStore {

    private static ExpireChecker _expireChecker;

    private Map<UUID, ISaleItem> _idMap;
    private Map<UUID, SaleItemMap> _playerMap;

    public ServerStore(String name, IDataNode storeNode) {

        super(name, storeNode);

        if (_expireChecker == null) {
            _expireChecker = new ExpireChecker();

            Bukkit.getScheduler().scheduleSyncRepeatingTask(Storefront.getInstance(), _expireChecker, 20, 20 * 60);
        }
    }

    @Override
    public StoreType getStoreType () {
        return StoreType.SERVER;
    }

    @Override
    public void view (Block sourceBlock, Player p) {

        ViewSession session = ViewSession.get(p, sourceBlock);

        session.next(new MainMenuView());
    }

    @Override
    public SaleItem getSaleItem (UUID itemId) {

        return (SaleItem)_idMap.get(itemId);
    }

    @Override
    @Nullable
    public SaleItem getSaleItem (UUID sellerId, ItemStack itemStack) {

        SaleItemMap map = _playerMap.get(sellerId);
        if (map == null)
            return null;

        return (SaleItem)map.get(itemStack);
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

        SaleItemMap map = _playerMap.get(sellerId);
        if (map == null)
            return new ArrayList<>(0);

        return new ArrayList<>(map.values());
    }

    @Override
    @Nullable
    public SaleItem addSaleItem (Player p, ItemStack itemStack, int qty, double pricePerUnit) {

        // make sure the item does not already exist
        SaleItem saleItem = getSaleItem(p.getUniqueId(), itemStack);
        if (saleItem != null) {
            // update item
            return updateAddSaleItem(saleItem, qty, pricePerUnit);
        }

        // get category for item
        Category category = Storefront.getInstance().getCategoryManager().get(itemStack);
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
        SaleItem item = new SaleItem(this, p.getUniqueId(), itemId, itemStack, qty, pricePerUnit,
                itemNode);

        // put sale item into maps
        _idMap.put(itemId, item);

        SaleItemCategoryMap categoryMap = getCategoryMap(category);
        categoryMap.put(itemId, item);

        SaleItemMap playerMap = getPlayerMap(p.getUniqueId());
        playerMap.put(itemStack, item);

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
    @Nullable
    public SaleItem removeSaleItem (UUID itemId) {

        SaleItem item = (SaleItem)_idMap.remove(itemId);
        if (item == null)
            return null;

        Category category = item.getCategory();
        if (category == null)
            return null;

        IDataNode itemNode = getItemNode(itemId);
        itemNode.remove();
        itemNode.save();

        SaleItemCategoryMap categoryMap = getCategoryMap(category);
        categoryMap.remove(itemId);

        SaleItemMap playerMap = getPlayerMap(item.getSellerId());
        playerMap.remove(item.getItemStack());

        return item;
    }

    @Override
    @Nullable
    public SaleItem removeSaleItem (UUID playerId, ItemStack itemStack) {

        // get player item map
        SaleItemMap map = _playerMap.get(playerId);
        if (map == null)
            return null;

        // remove from map
        SaleItem saleItem = (SaleItem)map.remove(itemStack, StoreStackMatcher.getDurability());
        if (saleItem == null)
            return null;

        // remove from maps
        _idMap.remove(saleItem.getItemId());
        SaleItemCategoryMap catMap = getCategoryMap(saleItem.getCategory());
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
    @Nullable
    public SaleItem removeSaleItem (UUID playerId, ItemStack itemStack, int qty) {

        // get player item map
        SaleItemMap map = _playerMap.get(playerId);
        if (map == null)
            return null;

        // get sale item from map
        SaleItem saleItem = (SaleItem)map.get(itemStack, StoreStackMatcher.getDurability());
        if (saleItem == null)
            return null;

        // check quantity to see if complete removal is required
        if (qty >= saleItem.getQty()) {
            return removeSaleItem(playerId, itemStack);
        }

        // update qty
        int newQty = saleItem.getQty() - qty;
        saleItem.setQty(newQty);

        return saleItem;
    }

    private void removeExpired(SaleItem saleItem) {

        if (!saleItem.isExpired())
            return;

        IBankItemsAccount account = BankItems.getAccount(saleItem.getSellerId());
        account.deposit(saleItem.getItemStack(), saleItem.getQty());

        saleItem.setQty(0);

        removeSaleItem(saleItem.getItemId());
        
        Msg.tellImportant(saleItem.getSellerId(), "storefront-sale-expired-" + this.getName(), 
                "1 or more items you were selling at the store '{0}' expired and were sent to your item bank account.",
                this.getTitle());
    }

    @Override
    public boolean buySaleItem (Player buyer, ISaleItem stack, int qty, double price) {

        SaleItem saleItem = getSaleItem(stack.getItemId());
        if (saleItem == null || saleItem.getQty() < qty) {
            Msg.debug("Couldn't find saleItem or not enough quantity to purchase.");
            return false;
        }

        if (saleItem.isRemoved() || saleItem.isExpired()) {
            Msg.debug("Player sale rejected because the item is removed or expired.");

            if (saleItem.isExpired()) {
                removeExpired(saleItem);
            }

            return false;
        }

        ItemStack purchasedStack = saleItem.getItemStack().clone();
        purchasedStack.setAmount(qty);

        // make sure player has room in chest
        if (!InventoryUtils.hasRoom(buyer.getInventory(), purchasedStack)) {
            Msg.debug("Player sale rejected because not enough room in chest.");
            return false;
        }

        // make sure buyer can afford
        if (Economy.getBalance(buyer.getUniqueId()) < price) {
            Msg.debug("Player sale rejected because player doesn't have enough money.");
            return false;
        }

        try {
            Economy.transfer(buyer.getUniqueId(), saleItem.getSellerId(), price);
        } catch (TransactionFailException e) {
            return false;
        }

        stack.increment(-qty);

        if (stack.getParent().getQty() == 0) {
            removeSaleItem(stack.getItemId());
        }

        buyer.getInventory().addItem(purchasedStack);

        return true;
    }

    @Override
    public boolean clearSaleItems (final UUID playerId) {

        // get player item map
        final SaleItemMap map = _playerMap.get(playerId);
        if (map == null)
            return false;

        getDataNode().runBatchOperation(new DataBatchOperation() {

            @Override
            public void run (IDataNode dataNode) {

                List<ISaleItem> items = new ArrayList<>(map.values());

                for (ISaleItem item : items) {
                    removeSaleItem(playerId, item.getItemStack());
                }
            }

        });

        return true;
    }

    @Override
    public List<Category> getSellCategories () {
        return new ArrayList<>(Storefront.getInstance().getCategoryManager().getAll());
    }

    @Override
    public List<Category> getBuyCategories () {
        return new ArrayList<>(Storefront.getInstance().getCategoryManager().getAll());
    }

    @Override
    protected void onInit () {

        _idMap = new HashMap<UUID, ISaleItem>(50);
        _playerMap = new HashMap<UUID, SaleItemMap>(50);
    }

    @Override
    protected void onSaleItemLoaded (SaleItem saleItem) {

        _idMap.put(saleItem.getItemId(), saleItem);

        SaleItemCategoryMap categoryMap = getCategoryMap(saleItem.getCategory());
        categoryMap.put(saleItem.getItemId(), saleItem);

        SaleItemMap playerMap = getPlayerMap(saleItem.getSellerId());
        playerMap.put(saleItem.getItemStack(), saleItem);
    }

    @Override
    protected void onLoadSettings (IDataNode storeNode) {
        // do nothing
    }

    private SaleItemMap getPlayerMap (UUID playerId) {

        SaleItemMap saleItems = _playerMap.get(playerId);
        if (saleItems == null) {
            saleItems = new SaleItemMap();
            _playerMap.put(playerId, saleItems);
        }

        return saleItems;
    }

    private static class ExpireChecker implements Runnable {

        @Override
        public void run () {

            StoreManager storeManager = Storefront.getInstance().getStoreManager();

            List<IStore> stores = storeManager.getServerStores();

            for (IStore store : stores) {
                
                if (!(store instanceof ServerStore)) {
                    continue;
                }
                
                ServerStore serverStore = (ServerStore)store;

                List<ISaleItem> saleItems = serverStore.getSaleItems();

                for (ISaleItem saleItem : saleItems) {
                    if (saleItem.isExpired()) {
                        serverStore.removeExpired((SaleItem)saleItem);
                    }
                    else if (saleItem.isRemoved()) {
                        serverStore.removeSaleItem(saleItem.getItemId());
                    }
                }
            }
        }

    }
}
