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

import com.jcwhatever.nucleus.providers.bankitems.BankItems;
import com.jcwhatever.nucleus.providers.bankitems.IBankItemsAccount;
import com.jcwhatever.nucleus.providers.economy.Economy;
import com.jcwhatever.nucleus.providers.economy.IEconomyTransaction;
import com.jcwhatever.nucleus.regions.IRegion;
import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.inventory.InventoryUtils;
import com.jcwhatever.nucleus.utils.observer.future.FutureResultAgent;
import com.jcwhatever.nucleus.utils.observer.future.FutureResultSubscriber;
import com.jcwhatever.nucleus.utils.observer.future.IFutureResult;
import com.jcwhatever.nucleus.utils.observer.future.Result;
import com.jcwhatever.nucleus.utils.text.TextUtils;
import com.jcwhatever.storefront.Msg;
import com.jcwhatever.storefront.Storefront;
import com.jcwhatever.storefront.category.Category;
import com.jcwhatever.storefront.data.ISaleItem;
import com.jcwhatever.storefront.data.SaleItem;
import com.jcwhatever.storefront.data.SaleItemIDMap;
import com.jcwhatever.storefront.data.WantedItems;
import com.jcwhatever.storefront.regions.StoreRegion;
import com.jcwhatever.storefront.utils.StoreStackMatcher;

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

        _region = new StoreRegion(this, storeNode.getNode("region"));

        onInit();

        load();
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
        PreCon.notNull(region);

        boolean isOwn = region.getPlugin() == Storefront.getPlugin();

        if (isOwn)
            throw new IllegalStateException("Can only set external regions.");

        if (!region.isDefined())
            throw new IllegalStateException("Region must be defined.");

        _region.setRegion(region);
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

        if (getType() == StoreType.SERVER) {
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
    public IFutureResult<IEconomyTransaction> sellToStore(Player seller, ISaleItem stack, final int qty, double price) {

        if (getType() == StoreType.SERVER) {
            return new FutureResultAgent<IEconomyTransaction>()
                    .error(null, "Cannot sell to a server store");
        }

        if (!hasOwner()) {
            return new FutureResultAgent<IEconomyTransaction>()
                    .error(null, "Cannot sell to a store that has no owner.");
        }

        final ISaleItem saleItem = getWantedItems().get(stack.getId());
        if (saleItem == null || saleItem.getQty() < qty) {
            return new FutureResultAgent<IEconomyTransaction>()
                    .error(null, "Tried to sell item to the store that it's not willing to accept.");
        }

        final Inventory playerInventory = seller.getInventory();

        if (!InventoryUtils.has(playerInventory, saleItem.getItemStack(), StoreStackMatcher.getDefault(), qty)) {
            return new FutureResultAgent<IEconomyTransaction>()
                    .error(null, "Player doesn't have enough items to sell");
        }


        return Economy.transfer(getOwnerId(), seller.getUniqueId(), price)
                .onSuccess(new FutureResultSubscriber<IEconomyTransaction>() {
                    @Override
                    public void on(Result<IEconomyTransaction> result) {
                        InventoryUtils.removeAmount(
                                playerInventory, saleItem.getItemStack(), StoreStackMatcher.getDefault(), qty);

                        IBankItemsAccount account = BankItems.getAccount(getOwnerId());
                        account.deposit(saleItem.getItemStack(), qty);

                    }
                });
    }

    @Override
    public IFutureResult<IEconomyTransaction> buySaleItem (
            final Player buyer, final ISaleItem stack, final int qty, double price) {

        SaleItem saleItem = getSaleItem(stack.getId());
        if (saleItem == null || saleItem.getQty() < qty) {
            return new FutureResultAgent<IEconomyTransaction>()
                    .error(null, "Couldn't find saleItem or not enough quantity to purchase.");
        }

        final ItemStack purchasedStack = saleItem.getItemStack().clone();
        purchasedStack.setAmount(qty);

        // make sure player has room in chest
        if (!InventoryUtils.hasRoom(buyer.getInventory(), purchasedStack)) {
            return new FutureResultAgent<IEconomyTransaction>()
                    .error(null, "Player sale rejected because not enough room in chest.");
        }

        // make sure buyer can afford
        if (Economy.getBalance(buyer.getUniqueId()) < price) {
            return new FutureResultAgent<IEconomyTransaction>()
                    .error(null, "Player sale rejected because player doesn't have enough money.");
        }

        return Economy.transfer(buyer.getUniqueId(), saleItem.getSellerId(), price)
                .onSuccess(new FutureResultSubscriber<IEconomyTransaction>() {
                    @Override
                    public void on(Result<IEconomyTransaction> result) {
                        stack.increment(-qty);
                        buyer.getInventory().addItem(purchasedStack);
                    }
                });
    }

    protected SaleItemIDMap getCategoryMap (Category category) {

        SaleItemIDMap saleItems = _categoryMap.get(category);
        if (saleItems == null) {
            saleItems = new SaleItemIDMap();
            _categoryMap.put(category, saleItems);
        }

        return saleItems;
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

    private void load() {

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

        onLoadSettings(_dataNode);
    }

}
