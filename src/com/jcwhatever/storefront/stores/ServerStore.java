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


package com.jcwhatever.storefront.stores;

import com.jcwhatever.nucleus.providers.bankitems.IBankItemsAccount;
import com.jcwhatever.nucleus.providers.economy.IEconomyTransaction;
import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.providers.bankitems.BankItems;
import com.jcwhatever.nucleus.providers.economy.Economy;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.Rand;
import com.jcwhatever.nucleus.managed.scheduler.Scheduler;
import com.jcwhatever.nucleus.utils.inventory.InventoryUtils;
import com.jcwhatever.nucleus.utils.language.Localizable;
import com.jcwhatever.nucleus.utils.observer.result.FutureResultAgent;
import com.jcwhatever.nucleus.utils.observer.result.FutureResultAgent.Future;
import com.jcwhatever.nucleus.utils.observer.result.FutureSubscriber;
import com.jcwhatever.nucleus.utils.observer.result.Result;
import com.jcwhatever.nucleus.views.ViewSession;
import com.jcwhatever.storefront.Lang;
import com.jcwhatever.storefront.Msg;
import com.jcwhatever.storefront.Storefront;
import com.jcwhatever.storefront.category.Category;
import com.jcwhatever.storefront.data.ISaleItem;
import com.jcwhatever.storefront.data.SaleItem;
import com.jcwhatever.storefront.data.SaleItemIDMap;
import com.jcwhatever.storefront.data.SaleItemMap;
import com.jcwhatever.storefront.utils.StoreStackMatcher;
import com.jcwhatever.storefront.views.mainmenu.MainMenuView;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;

/**
 * Server store implementation of {@link AbstractStore}
 * where any player can buy or sell items.
 */
public class ServerStore extends AbstractStore {

    @Localizable static final String _ITEMS_EXPIRED =
            "1 or more items you were selling at the store '{0: store title}' expired and " +
                    "were sent to your item bank account.";


    private static ExpireChecker _expireChecker;

    private Map<UUID, SaleItemMap> _playerMap;

    public ServerStore(String name, IDataNode storeNode) {

        super(name, storeNode);

        if (_expireChecker == null) {
            _expireChecker = new ExpireChecker();

            Scheduler.runTaskRepeat(
                    Storefront.getPlugin(), Rand.getInt(1, 20), 20 * 60, _expireChecker);
        }
    }

    @Override
    public StoreType getType() {
        return StoreType.SERVER;
    }

    @Override
    public void view (Player player, @Nullable Block sourceBlock) {
        PreCon.notNull(player);

        ViewSession session = ViewSession.get(player, sourceBlock);
        session.next(new MainMenuView(this));
    }

    @Override
    @Nullable
    public SaleItem getSaleItem(UUID sellerId, ItemStack itemStack) {
        PreCon.notNull(sellerId);
        PreCon.notNull(itemStack);

        SaleItemMap map = _playerMap.get(sellerId);
        if (map == null)
            return null;

        return (SaleItem)map.get(itemStack);
    }

    @Override
    public List<ISaleItem> getSaleItems(UUID sellerId) {
        PreCon.notNull(sellerId);

        SaleItemMap map = _playerMap.get(sellerId);
        if (map == null)
            return new ArrayList<>(0);

        return new ArrayList<>(map.values());
    }

    @Override
    @Nullable
    public SaleItem addSaleItem(Player player, ItemStack itemStack, int qty, double pricePerUnit) {
        PreCon.notNull(player);
        PreCon.notNull(itemStack);

        // make sure the item does not already exist
        SaleItem saleItem = getSaleItem(player.getUniqueId(), itemStack);
        if (saleItem != null) {
            // update item
            return updateAddSaleItem(saleItem, qty, pricePerUnit);
        }

        // get category for item
        Category category = Storefront.getCategoryManager().get(itemStack);
        if (category == null)
            return null;

        // create unique id
        UUID itemId = null;
        while (itemId == null) {
            itemId = UUID.randomUUID();
            if (getIDMap().containsKey(itemId))
                itemId = null;
        }

        // get data node for item
        IDataNode itemNode = getItemNode(itemId);

        // create new sale item, constructor saves info to itemNode
        SaleItem item = new SaleItem(this, player.getUniqueId(), itemId, itemStack, qty, pricePerUnit,
                itemNode);

        // put sale item into maps
        getIDMap().put(itemId, item);

        SaleItemIDMap categoryMap = getCategoryMap(category);
        categoryMap.put(itemId, item);

        SaleItemMap playerMap = getPlayerMap(player.getUniqueId());
        playerMap.put(itemStack, item);

        return item;
    }

    private SaleItem updateAddSaleItem(final SaleItem saleItem, final int qty,
                                       final double pricePerUnit) {
        PreCon.notNull(saleItem);

        int newQty = saleItem.getQty() + qty;

        saleItem.setPricePerUnit(pricePerUnit);
        saleItem.setQty(newQty);

        return saleItem;
    }

    @Override
    @Nullable
    public SaleItem removeSaleItem(UUID itemId) {
        PreCon.notNull(itemId);

        SaleItem item = (SaleItem)getIDMap().remove(itemId);
        if (item == null)
            return null;

        Category category = item.getCategory();
        if (category == null)
            return null;

        IDataNode itemNode = getItemNode(itemId);
        itemNode.remove();
        itemNode.save();

        SaleItemIDMap categoryMap = getCategoryMap(category);
        categoryMap.remove(itemId);

        SaleItemMap playerMap = getPlayerMap(item.getSellerId());
        playerMap.remove(item.getItemStack());

        return item;
    }

    @Override
    @Nullable
    public SaleItem removeSaleItem(UUID playerId, ItemStack itemStack) {
        PreCon.notNull(playerId);
        PreCon.notNull(itemStack);

        // get player item map
        SaleItemMap map = _playerMap.get(playerId);
        if (map == null)
            return null;

        // remove from map
        SaleItem saleItem = (SaleItem)map.remove(itemStack, StoreStackMatcher.getDurability());
        if (saleItem == null)
            return null;

        // remove from maps
        getIDMap().remove(saleItem.getId());
        SaleItemIDMap catMap = getCategoryMap(saleItem.getCategory());
        if (catMap != null) {
            catMap.remove(saleItem.getId());
        }

        // remove from data node
        IDataNode itemNode = getItemNode(saleItem.getId());
        itemNode.remove();
        itemNode.save();

        return saleItem;
    }

    @Override
    @Nullable
    public SaleItem removeSaleItem(UUID playerId, ItemStack itemStack, int qty) {
        PreCon.notNull(playerId);
        PreCon.notNull(itemStack);

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

    @Override
    public Future<IEconomyTransaction> buySaleItem(
            final Player buyer, final ISaleItem stack, final int qty, double price) {

        SaleItem saleItem = getSaleItem(stack.getId());
        if (saleItem == null || saleItem.getQty() < qty) {
            return new FutureResultAgent<IEconomyTransaction>().error(null,
                    "Couldn't find saleItem or not enough quantity to purchase.");
        }

        if (saleItem.isRemoved() || saleItem.isExpired()) {

            if (saleItem.isExpired()) {
                removeExpired(saleItem);
            }

            return new FutureResultAgent<IEconomyTransaction>().error(null,
                    "Player sale rejected because the item is removed or expired.");
        }

        final ItemStack purchasedStack = saleItem.getItemStack().clone();
        purchasedStack.setAmount(qty);

        // make sure player has room in chest
        if (!InventoryUtils.hasRoom(buyer.getInventory(), purchasedStack)) {
            return new FutureResultAgent<IEconomyTransaction>().error(null,
                    "Player sale rejected because not enough room in inventory.");
        }

        // make sure buyer can afford
        if (Economy.getBalance(buyer.getUniqueId()) < price) {
            return new FutureResultAgent<IEconomyTransaction>().error(null,
                    "Player sale rejected because player doesn't have enough money.");
        }

        return Economy.transfer(buyer.getUniqueId(), saleItem.getSellerId(), price)
                .onSuccess(new FutureSubscriber<IEconomyTransaction>() {
                    @Override
                    public void on(Result<IEconomyTransaction> result) {

                        stack.increment(-qty);

                        if (stack.getParent().getQty() == 0) {
                            removeSaleItem(stack.getId());
                        }

                        buyer.getInventory().addItem(purchasedStack);
                    }
                });
    }

    @Override
    public boolean clearSaleItems(final UUID playerId) {

        // get player item map
        final SaleItemMap map = _playerMap.get(playerId);
        if (map == null)
            return false;

        List<ISaleItem> items = new ArrayList<>(map.values());

        for (ISaleItem item : items) {
            removeSaleItem(playerId, item.getItemStack());
        }

        return true;
    }

    @Override
    public List<Category> getSellCategories() {
        return new ArrayList<>(Storefront.getCategoryManager().getAll());
    }

    @Override
    public List<Category> getBuyCategories() {
        return new ArrayList<>(Storefront.getCategoryManager().getAll());
    }

    @Override
    protected void onInit() {
        _playerMap = new HashMap<UUID, SaleItemMap>(50);
    }

    @Override
    protected void onSaleItemLoaded(SaleItem saleItem) {

        SaleItemIDMap categoryMap = getCategoryMap(saleItem.getCategory());
        categoryMap.put(saleItem.getId(), saleItem);

        SaleItemMap playerMap = getPlayerMap(saleItem.getSellerId());
        playerMap.put(saleItem.getItemStack(), saleItem);
    }

    @Override
    protected void onLoadSettings(IDataNode storeNode) {
        // do nothing
    }

    private SaleItemMap getPlayerMap(UUID playerId) {

        SaleItemMap saleItems = _playerMap.get(playerId);
        if (saleItems == null) {
            saleItems = new SaleItemMap();
            _playerMap.put(playerId, saleItems);
        }

        return saleItems;
    }

    private void removeExpired(SaleItem saleItem) {

        if (!saleItem.isExpired())
            return;

        IBankItemsAccount account = BankItems.getAccount(saleItem.getSellerId());
        account.deposit(saleItem.getItemStack(), saleItem.getQty());

        saleItem.setQty(0);

        removeSaleItem(saleItem.getId());

        Msg.tellImportant(saleItem.getSellerId(), "storefront-sale-expired-" + this.getName(),
                Lang.get(_ITEMS_EXPIRED, this.getTitle()));
    }

    private static class ExpireChecker implements Runnable {

        @Override
        public void run () {

            StoreManager storeManager = Storefront.getStoreManager();

            Collection<IStore> stores = storeManager.getAll();

            for (IStore store : stores) {

                if (!(store instanceof ServerStore))
                    continue;

                ServerStore serverStore = (ServerStore)store;

                List<ISaleItem> saleItems = serverStore.getSaleItems();

                for (ISaleItem saleItem : saleItems) {
                    if (saleItem.isExpired()) {
                        serverStore.removeExpired((SaleItem)saleItem);
                    }
                    else if (saleItem.isRemoved()) {
                        serverStore.removeSaleItem(saleItem.getId());
                    }
                }
            }
        }

    }
}
