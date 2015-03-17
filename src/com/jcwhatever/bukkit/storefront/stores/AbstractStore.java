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

import com.jcwhatever.bukkit.storefront.category.Category;
import com.jcwhatever.bukkit.storefront.Msg;
import com.jcwhatever.bukkit.storefront.Storefront;
import com.jcwhatever.bukkit.storefront.data.ISaleItem;
import com.jcwhatever.bukkit.storefront.data.SaleItem;
import com.jcwhatever.bukkit.storefront.data.SaleItemIDMap;
import com.jcwhatever.bukkit.storefront.data.WantedItems;
import com.jcwhatever.bukkit.storefront.regions.StoreRegion;
import com.jcwhatever.bukkit.storefront.utils.StoreStackMatcher;
import com.jcwhatever.nucleus.providers.bankitems.IBankItemsAccount;
import com.jcwhatever.nucleus.providers.economy.TransactionFailException;
import com.jcwhatever.nucleus.regions.IRegion;
import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.utils.BankItems;
import com.jcwhatever.nucleus.utils.Economy;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.Scheduler;
import com.jcwhatever.nucleus.utils.inventory.InventoryUtils;
import com.jcwhatever.nucleus.utils.text.TextUtils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;

/**
 * Abstract implementation of a store.
 */
public abstract class AbstractStore implements IStore {

    private final String _name;
    private final String _searchName;
    private final Map<Category, SaleItemIDMap> _categoryMap;
    private final IDataNode _dataNode;
    private final StoreRegion _region;

    private String _title;
    private WantedItems _wantedItems;
    private final Map<UUID, ISaleItem> _idMap = new HashMap<>(50);

    /**
     * Constructor.
     *
     * @param name       The node name of the store.
     * @param storeNode  The stores data node.
     */
    public AbstractStore(String name, IDataNode storeNode) {
        PreCon.notNullOrEmpty(name);
        PreCon.notNull(storeNode);

        _name = name;
        _searchName = name.toLowerCase();
        _dataNode = storeNode;
        _categoryMap = new HashMap<Category, SaleItemIDMap>(25);

        _region = new StoreRegion(this);

        onInit();

        loadSettings();
    }

    @Override
    public final String getName () {
        return _name;
    }

    @Override
    public String getSearchName() {
        return _searchName;
    }

    @Override
    public final String getTitle () {
        return _title;
    }

    @Override
    public final void setTitle (String title) {
        PreCon.notNull(title);

        _title = title;
        _dataNode.set("title", title);
        _dataNode.save();
    }

    @Override
    @Nullable
    public UUID getOwnerId () {

        IRegion region = getRegion();
        if (region == null)
            return null;

        return region.getOwnerId();
    }

    @Override
    public void setOwnerId (UUID ownerId) {
        IRegion region = getRegion();
        if (region == null)
            throw new IllegalStateException("Cannot set owner on a store that has no region.");

        if (region.getPlugin() != Storefront.getPlugin())
            throw new IllegalStateException("Cannot set owner on a store with an external region.");

        region.setOwner(ownerId);
    }

    @Override
    public boolean hasOwner () {
        IRegion region = getRegion();
        return region != null && region.hasOwner();
    }

    @Override
    public boolean hasOwnRegion () {
        return _region.hasOwnRegion();
    }

    @Override
    public void setExternalRegion (IRegion region) {
        IDataNode ownRegionNode = _dataNode.getNode("region");

        boolean isOwn = region.getPlugin() == Storefront.getPlugin();

        if (isOwn)
            throw new IllegalStateException("Can only set external regions.");

        if (!region.isDefined())
            throw new IllegalStateException("Region must be defined.");

        _dataNode.set("region-name", region.getName());
        _dataNode.set("region-p1", region.getP1());
        _dataNode.set("region-p2", region.getP2());

        ownRegionNode.remove();

        _region.setRegion(region);

        _dataNode.save();
    }

    @Override
    public final IRegion getRegion() {
        return _region.getRegion();
    }

    @Override
    public final StoreRegion getStoreRegion() {
        return _region;
    }

    @Override
    public IDataNode getDataNode () {
        return _dataNode;
    }

    @Nullable
    @Override
    public SaleItem getSaleItem (UUID itemId) {
        PreCon.notNull(itemId);

        return (SaleItem)_idMap.get(itemId);
    }

    @Override
    public List<ISaleItem> getSaleItems () {
        return new ArrayList<>(_idMap.values());
    }

    @Override
    public List<ISaleItem> getSaleItems (Category category) {

        SaleItemIDMap map = getCategoryMap(category);
        if (map == null)
            return new ArrayList<>(0);

        return new ArrayList<>(map.values());
    }

    @Override
    public WantedItems getWantedItems () {

        if (getStoreType() == StoreType.SERVER) {
            throw new RuntimeException("Cannot get Wanted items from a server store.");
        }

        if (_wantedItems == null) {
            _wantedItems = new WantedItems(this, _dataNode.getNode("wanted-items"));
        }

        return _wantedItems;
    }

    @Override
    public boolean canAdd (UUID sellerId, ItemStack itemStack, int qty) {
        Category category = Storefront.getCategoryManager().get(itemStack);
        if (category == null)
            return false;

        SaleItemIDMap map = getCategoryMap(category);

        return map.canAdd(sellerId, itemStack, qty);
    }

    @Override
    public int getSpaceAvailable (UUID sellerId, ItemStack itemStack) {
        Category category = Storefront.getCategoryManager().get(itemStack);
        if (category == null)
            return 0;

        SaleItemIDMap map = getCategoryMap(category);

        return map.getAvailableSpace(sellerId, itemStack);
    }

    @Override
    public boolean sellToStore(Player seller, ISaleItem stack, int qty, double price) {
        if (getStoreType() == StoreType.SERVER) {
            Msg.debug("Cannot sell to a server store");
            return false;
        }

        if (!hasOwner()) {
            Msg.debug("Cannot sell to a store that has no owner.");
            return false;
        }


        ISaleItem saleItem = getWantedItems().get(stack.getId());
        if (saleItem == null || saleItem.getQty() < qty) {
            Msg.debug("Tried to sell item to the store that it's not willing to accept.");
            return false;
        }


        Inventory playerInventory = seller.getInventory();

        if (!InventoryUtils.has(playerInventory, saleItem.getItemStack(), StoreStackMatcher.getDefault(), qty)) {
            Msg.debug("Player doesn't have enough items to sell");
            return false;
        }

        try {
            Economy.transfer(getOwnerId(), seller.getUniqueId(), price);
        } catch (TransactionFailException e) {
            Msg.debug("Failed to transfer money");
            return false;
        }

        InventoryUtils.removeAmount(playerInventory, saleItem.getItemStack(), StoreStackMatcher.getDefault(), qty);

        IBankItemsAccount account = BankItems.getAccount(getOwnerId());
        account.deposit(saleItem.getItemStack(), qty);

        return true;
    }

    @Override
    public boolean buySaleItem (Player buyer, ISaleItem stack, int qty, double price) {

        SaleItem saleItem = getSaleItem(stack.getId());
        if (saleItem == null || saleItem.getQty() < qty) {
            Msg.debug("Couldn't find saleItem or not enough quantity to purchase.");
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
        buyer.getInventory().addItem(purchasedStack);

        return true;
    }

    protected SaleItemIDMap getCategoryMap (Category category) {

        SaleItemIDMap saleItems = _categoryMap.get(category);
        if (saleItems == null) {
            saleItems = new SaleItemIDMap();
            _categoryMap.put(category, saleItems);
        }

        return saleItems;
    }

    private void clearExternalRegion() {

        _dataNode.set("region-name", null);
        _dataNode.set("region-p1", null);
        _dataNode.set("region-p2", null);

        IDataNode ownRegionNode = _dataNode.getNode("region");
        ownRegionNode.remove();

        _region.setOwnRegion();

        _dataNode.save();
    }

    private void loadSettings () {

        _title = _dataNode.getString("title", _name);

        IDataNode itemsNode = _dataNode.getNode("sale-items");

        for (IDataNode node : itemsNode) {

            UUID itemId = TextUtils.parseUUID(node.getName());
            if (itemId == null) {
                Msg.debug("Failed to parse Item Id: {0}", node.getName());
                continue;
            }

            SaleItem saleItem = new SaleItem(this, itemId, node);

            if (saleItem.getItemStack() == null) {
                Msg.debug("Failed to parse sale item stack.");
                continue;
            }

            if (saleItem.getCategory() == null)
                continue;

            getIDMap().put(saleItem.getId(), saleItem);

            onSaleItemLoaded(saleItem);
        }

        Scheduler.runTaskLater(Storefront.getPlugin(), 30, new Runnable() {

            @Override
            public void run() {

                // load external region, if any
                getRegion();
            }
        });

        onLoadSettings(_dataNode);
    }

    protected IDataNode getItemNode (UUID itemId) {
        return _dataNode.getNode("sale-items." + itemId);
    }

    protected Map<UUID, ISaleItem> getIDMap() {
        return _idMap;
    }

    protected abstract void onInit ();

    protected abstract void onSaleItemLoaded (SaleItem saleItem);

    protected abstract void onLoadSettings (IDataNode storeNode);

}
