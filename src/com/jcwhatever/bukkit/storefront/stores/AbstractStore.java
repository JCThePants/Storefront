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
import com.jcwhatever.bukkit.storefront.data.PriceMap;
import com.jcwhatever.bukkit.storefront.data.QtyMap;
import com.jcwhatever.bukkit.storefront.data.SaleItem;
import com.jcwhatever.bukkit.storefront.data.SaleItemCategoryMap;
import com.jcwhatever.bukkit.storefront.data.SaleItemSnapshot;
import com.jcwhatever.bukkit.storefront.data.WantedItems;
import com.jcwhatever.bukkit.storefront.regions.StoreRegion;
import com.jcwhatever.bukkit.storefront.utils.StoreStackMatcher;
import com.jcwhatever.nucleus.Nucleus;
import com.jcwhatever.nucleus.providers.bankitems.IBankItemsAccount;
import com.jcwhatever.nucleus.providers.economy.TransactionFailException;
import com.jcwhatever.nucleus.regions.IRegion;
import com.jcwhatever.nucleus.storage.DataBatchOperation;
import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.utils.BankItems;
import com.jcwhatever.nucleus.utils.Economy;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.Scheduler;
import com.jcwhatever.nucleus.utils.inventory.InventoryUtils;
import com.jcwhatever.nucleus.utils.items.MatchableItem;
import com.jcwhatever.nucleus.utils.text.TextUtils;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

public abstract class AbstractStore implements IStore {

    private Map<Category, SaleItemCategoryMap> _categoryMap;

    private String _name;
    private String _searchName;
    private String _title;

    private IDataNode _storeNode;

    private StoreRegion _region;

    private WantedItems _wantedItems;

    private String _regionName;
    private Location _regionP1;
    private Location _regionP2;

    public AbstractStore(String name, IDataNode storeNode) {
        PreCon.notNullOrEmpty(name);
        PreCon.notNull(storeNode);

        _name = name;
        _searchName = name.toLowerCase();
        _storeNode = storeNode;
        _categoryMap = new HashMap<Category, SaleItemCategoryMap>(25);

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

        _title = title;
        _storeNode.set("title", title);
        _storeNode.save();
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

        IDataNode ownRegionNode = _storeNode.getNode("region");

        boolean isOwn = region.getPlugin() == Storefront.getPlugin();

        if (isOwn)
            throw new IllegalStateException("Can only set external regions.");

        if (!region.isDefined())
            throw new IllegalStateException("Region must be defined.");

        _storeNode.set("region-name", region.getName());
        _storeNode.set("region-p1", region.getP1());
        _storeNode.set("region-p2", region.getP2());

        ownRegionNode.remove();

        _region.setRegion(region);

        _storeNode.save();
    }



    @Override
    public final IRegion getRegion() {

        IRegion currentRegion = _region.getRegion();

        if (!currentRegion.isDefined()) {

            // check if an external region is set.
            if (_regionName != null && _regionP1 != null && _regionP2 != null) {

                // find external region
                List<IRegion> regions = Nucleus.getRegionManager().getRegions(_regionP1);

                for (IRegion region : regions) {
                    //noinspection ConstantConditions
                    if (region.getP1().equals(_regionP1) &&
                            region.getP2().equals(_regionP2) &&
                            region.getName().equals(_regionName)) {

                        _region.setRegion(region);
                        break;
                    }
                }
            }
        }

        return currentRegion;
    }

    @Override
    public final StoreRegion getStoreRegion() {
        return _region;
    }

    @Override
    public void setRegionCoords(Location p1, Location p2) {
        _region.setCoords(p1, p2);
    }

    @Override
    public IDataNode getDataNode () {

        return _storeNode;
    }


    @Override
    public abstract void view (Block sourceBlock, Player p);


    @Override
    public WantedItems getWantedItems () {

        if (getStoreType() == StoreType.SERVER) {
            throw new RuntimeException("Cannot get Wanted items from a server store.");
        }

        if (_wantedItems == null) {
            _wantedItems = new WantedItems(this, _storeNode.getNode("wanted-items"));
        }

        return _wantedItems;
    }


    @Override
    public boolean canAdd (UUID sellerId, ItemStack itemStack, int qty) {
        Category category = Storefront.getCategoryManager().get(itemStack);
        if (category == null)
            return false;

        SaleItemCategoryMap map = getCategoryMap(category);

        return map.canAdd(sellerId, itemStack, qty);
    }


    @Override
    public int getSpaceAvailable (UUID sellerId, ItemStack itemStack) {
        Category category = Storefront.getCategoryManager().get(itemStack);
        if (category == null)
            return 0;

        SaleItemCategoryMap map = getCategoryMap(category);

        return map.getSpace(sellerId, itemStack);
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

        // TODO

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


    @Override
    public void updateWantedFromInventory (Player seller, PriceMap priceMap, QtyMap qtyMap,
                                           Inventory currentInventory,
                                           SaleItemSnapshot startSnapshot) {

        updateFromInventory(true, seller, priceMap, qtyMap, currentInventory, startSnapshot);
    }


    @Override
    public void updateFromInventory (Player seller, PriceMap priceMap,
                                     Inventory currentInventory,
                                     SaleItemSnapshot startSnapshot) {

        updateFromInventory(false, seller, priceMap, null, currentInventory, startSnapshot);
    }


    protected SaleItemCategoryMap getCategoryMap (Category category) {

        SaleItemCategoryMap saleItems = _categoryMap.get(category);
        if (saleItems == null) {
            saleItems = new SaleItemCategoryMap();
            _categoryMap.put(category, saleItems);
        }

        return saleItems;
    }


    @Override
    public void updateRemovedFromInventory (final Player seller, final Inventory currentInventory,
                                            final SaleItemSnapshot startSnapshot) {

        getDataNode().runBatchOperation(new DataBatchOperation() {

            @Override
            public void run (IDataNode dataNode) {

                List<MatchableItem> originalItems = startSnapshot.getWrappers();

                Set<MatchableItem> processed = new HashSet<MatchableItem>(originalItems.size());

                // search for less than total amount items
                for (MatchableItem startWrapper : originalItems) {

                    if (processed.contains(startWrapper))
                        continue;

                    processed.add(startWrapper);

                    SaleItem saleItem = getSaleItem(seller.getUniqueId(), startWrapper.getItem());

                    if (saleItem == null)
                        continue;

                    int startQty = InventoryUtils.count(startSnapshot.getItemStacks(), startWrapper.getItem(), StoreStackMatcher.getDefault());

                    int currQty = InventoryUtils.count(currentInventory.getContents(), startWrapper.getItem(), StoreStackMatcher.getDefault());

                    if (currQty >= startQty)
                        continue;

                    int delta = Math.abs(startQty - currQty);

                    int qty = saleItem.getQty();
                    qty -= delta;
                    saleItem.setQty(qty);
                }

            }

        });
    }

    private void updateFromInventory (final boolean isWanted, final Player seller,
                                      final PriceMap priceMap, final QtyMap qtyMap, final Inventory currentInventory,
                                      final SaleItemSnapshot startSnapshot) {

        getDataNode().runBatchOperation(new DataBatchOperation() {

            @Override
            public void run (IDataNode dataNode) {

                SaleItemSnapshot currentSnapshot = new SaleItemSnapshot(currentInventory);

                List<MatchableItem> originalItems = startSnapshot.getWrappers();
                List<MatchableItem> currentItems = currentSnapshot.getWrappers();

                Set<MatchableItem> processed = new HashSet<MatchableItem>(originalItems.size());

                // modify original items
                for (MatchableItem wrapper : originalItems) {

                    SaleItem saleItem = (SaleItem)(isWanted
                            ? getWantedItems().get(wrapper.getItem())
                            : getSaleItem(seller.getUniqueId(), wrapper.getItem()));

                    if (saleItem == null)
                        continue;

                    int originalAmount = startSnapshot.getAmount(wrapper);
                    int newAmount = currentSnapshot.getAmount(wrapper);
                    int delta = newAmount - originalAmount;

                    Double price = priceMap.get(wrapper);
                    if (price != null) {
                        saleItem.setPricePerUnit(price);
                    }

                    if (qtyMap != null) {
                        Integer qty = qtyMap.get(wrapper);
                        if (qty != null) {
                            saleItem.setQty(qty);
                        }
                    }
                    else if (delta != 0) {
                        int qty = saleItem.getQty();
                        qty += delta;
                        saleItem.setQty(qty);
                    }

                    processed.add(wrapper);
                }

                // add new Items
                for (MatchableItem wrapper : currentItems) {
                    if (processed.contains(wrapper))
                        continue;

                    Double price = priceMap.get(wrapper);
                    if (price == null)
                        throw new IllegalStateException(
                                "Failed to get a price from the supplied price map.");

                    SaleItem saleItem = (SaleItem)(isWanted
                            ? getWantedItems().get(wrapper.getItem())
                            : getSaleItem(seller.getUniqueId(), wrapper.getItem()));

                    // add new item
                    if (saleItem == null) {

                        Integer qty = qtyMap != null
                                ? qtyMap.get(wrapper)
                                : wrapper.getItem().getAmount();

                        if (qty == null)
                            throw new IllegalStateException(
                                    "Failed to get a quantity from the supplied quantity map.");

                        if (isWanted)
                            getWantedItems().add(wrapper.getItem(), qty, price);
                        else
                            addSaleItem(seller, wrapper.getItem(), qty, price);
                    }

                    // merge item with existing
                    else {
                        Integer qty = qtyMap != null
                                ? qtyMap.get(wrapper)
                                : currentSnapshot.getAmount(wrapper) + saleItem.getQty();

                        if (qty == null)
                            throw new IllegalStateException(
                                    "Failed to get quantity from the supplied quantity map.");

                        saleItem.setQty(qty);
                        saleItem.setPricePerUnit(price);
                    }
                }
            }

        });
    }

    private void clearExternalRegion() {

        _storeNode.set("region-name", null);
        _storeNode.set("region-p1", null);
        _storeNode.set("region-p2", null);

        _regionName = null;
        _regionP1 = null;
        _regionP2 = null;

        IDataNode ownRegionNode = _storeNode.getNode("region");
        ownRegionNode.remove();

        _region.setOwnRegion();

        _storeNode.save();
    }


    private void loadSettings () {

        _title = _storeNode.getString("title", _name);

        _regionName = _storeNode.getString("region-name");
        _regionP1 = _storeNode.getLocation("region-p1");
        _regionP2 = _storeNode.getLocation("region-p2");

        IDataNode itemsNode = _storeNode.getNode("sale-items");

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

            onSaleItemLoaded(saleItem);
        }

        Scheduler.runTaskLater(Storefront.getPlugin(), 30, new Runnable() {

            @Override
            public void run() {

                // load external region, if any
                getRegion();
            }
        });

        onLoadSettings(_storeNode);
    }

    @Override
    public abstract StoreType getStoreType ();

    protected IDataNode getItemNode (UUID itemId) {

        return _storeNode.getNode("sale-items." + itemId);
    }

    protected abstract void onInit ();


    protected abstract void onSaleItemLoaded (SaleItem saleItem);


    protected abstract void onLoadSettings (IDataNode storeNode);
}
