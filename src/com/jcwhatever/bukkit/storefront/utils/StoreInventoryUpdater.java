package com.jcwhatever.bukkit.storefront.utils;

import com.jcwhatever.bukkit.storefront.data.PriceMap;
import com.jcwhatever.bukkit.storefront.data.QtyMap;
import com.jcwhatever.bukkit.storefront.data.SaleItem;
import com.jcwhatever.bukkit.storefront.stores.IStore;
import com.jcwhatever.nucleus.storage.DataBatchOperation;
import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.inventory.InventorySnapshot;
import com.jcwhatever.nucleus.utils.inventory.InventoryUtils;
import com.jcwhatever.nucleus.utils.items.MatchableItem;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Updates a stores inventory based on a starting snapshot of an inventory
 * and the current updated snapshot.
 *
 * <p>Used to update inventory based on changes to a portion of the inventory
 * caused by changing an inventory views contents.
 */
public class StoreInventoryUpdater {

    private final IStore _store;
    private boolean _isWantedItems;
    private Player _seller;
    private UUID _sellerId;
    private PriceMap _priceMap;
    private QtyMap _qtyMap;
    private Inventory _currentInventory;
    private InventorySnapshot _snapshot;

    public StoreInventoryUpdater(IStore store) {
        PreCon.notNull(store);

        _store = store;
    }

    /**
     * Set the seller that the inventory changes are for.
     *
     * <p>Required.</p>
     *
     * @param seller  The seller.
     *
     * @return  Self for chaining.
     */
    public StoreInventoryUpdater seller(Player seller) {
        PreCon.notNull(seller);

        _seller = seller;
        _sellerId = seller.getUniqueId();

        return this;
    }

    /**
     * Set the {@link PriceMap} that contains prices for all items in
     * the inventory.
     *
     * <p>Required if using {@link #update}.</p>
     *
     * @param priceMap  The {@link PriceMap}.
     *
     * @return  Self for chaining.
     */
    public StoreInventoryUpdater priceMap(PriceMap priceMap) {
        PreCon.notNull(priceMap);

        _priceMap = priceMap;

        return this;
    }

    /**
     * Set the optional {@link QtyMap} whose quantities are used instead of
     * the quantities from the snapshot.
     *
     * <p>Use when the quantities in the inventory do not represent the desired
     * quantity.</p>
     *
     * @param qtyMap  The {@link QtyMap}.
     *
     * @return  Self for chaining.
     */
    public StoreInventoryUpdater qtyMap(QtyMap qtyMap) {
        PreCon.notNull(qtyMap);

        _qtyMap = qtyMap;

        return this;
    }

    /**
     * Set the {@link Inventory} that is the current state of the inventory
     * to update.
     *
     * <p>Required.</p>
     *
     * @param inventory  The current {@link Inventory}.
     *
     * @return  Self for chaining.
     */
    public StoreInventoryUpdater inventory(Inventory inventory) {
        PreCon.notNull(inventory);

        _currentInventory = inventory;

        return this;
    }

    /**
     * Set the {@link InventorySnapshot} that contains the inventory before it was
     * modified.
     *
     * <p>Required.</p>
     *
     * @param snapshot  The {@link InventorySnapshot}.
     *
     * @return  Self for chaining.
     */
    public StoreInventoryUpdater snapshot(InventorySnapshot snapshot) {
        PreCon.notNull(snapshot);

        _snapshot = snapshot;

        return this;
    }

    /**
     * Set the wanted flag to true.
     *
     * <p>Wanted flag indicates if the wanted items inventory should be updated (true) or
     * the sale items inventory should be updated (false).</p>
     *
     * @return  Self for chaining.
     */
    public StoreInventoryUpdater wanted() {
        _isWantedItems = true;

        return this;
    }

    /**
     * Update the inventory.
     */
    public void update() {

        checkAllValuesPresent();

        if (_priceMap == null)
            throw new IllegalStateException("StoreInventoryUpdater: PriceMap is required.");

        _store.getDataNode().runBatchOperation(new DataBatchOperation() {

            @Override
            public void run (IDataNode dataNode) {

                InventorySnapshot currentSnapshot = new InventorySnapshot(
                        _currentInventory, StoreStackMatcher.getDefault());

                List<MatchableItem> originalItems = _snapshot.getMatchable();
                List<MatchableItem> currentItems = currentSnapshot.getMatchable();

                Set<MatchableItem> processed = new HashSet<MatchableItem>(originalItems.size());

                // modify original items
                modifyExisting(currentSnapshot, originalItems, processed);

                // add new Items
                addNew(currentSnapshot, currentItems, processed);
            }

        });
    }

    /**
     * Update for removed items only.
     */
    public void updateRemoved() {
        _store.getDataNode().runBatchOperation(new DataBatchOperation() {

            @Override
            public void run (IDataNode dataNode) {
                removeMissing();
            }
        });
    }

    private void checkAllValuesPresent() {
        if (_sellerId == null)
            throw new IllegalStateException("StoreInventoryUpdater: SellerID is required.");

        if (_currentInventory == null)
            throw new IllegalStateException("StoreInventoryUpdater: Inventory is required.");

        if (_snapshot == null)
            throw new IllegalStateException("StoreInventoryUpdater: Snapshot is required.");
    }

    private void modifyExisting(InventorySnapshot currentSnapshot,
                                List<MatchableItem> originalItems,
                                Set<MatchableItem> processed) {

        for (MatchableItem wrapper : originalItems) {

            SaleItem saleItem = (SaleItem)(_isWantedItems
                    ? _store.getWantedItems().get(wrapper.getItem())
                    : _store.getSaleItem(_sellerId, wrapper.getItem()));

            if (saleItem == null)
                continue;

            int originalAmount = _snapshot.getAmount(wrapper);
            int newAmount = currentSnapshot.getAmount(wrapper);
            int delta = newAmount - originalAmount;

            Double price = _priceMap.get(wrapper);
            if (price != null) {
                saleItem.setPricePerUnit(price);
            }

            if (_qtyMap != null) {
                Integer qty = _qtyMap.get(wrapper);
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
    }

    private void addNew(InventorySnapshot currentSnapshot,
                        List<MatchableItem> currentItems,
                        Set<MatchableItem> processed) {

        for (MatchableItem wrapper : currentItems) {
            if (processed.contains(wrapper))
                continue;

            Double price = _priceMap.get(wrapper);
            if (price == null)
                throw new IllegalStateException(
                        "Failed to get a price from the supplied price map.");

            SaleItem saleItem = (SaleItem)(_isWantedItems
                    ? _store.getWantedItems().get(wrapper.getItem())
                    : _store.getSaleItem(_sellerId, wrapper.getItem()));

            // add new item
            if (saleItem == null) {

                //noinspection ConstantConditions
                Integer qty = _qtyMap != null
                        ? _qtyMap.get(wrapper)
                        : wrapper.getItem().getAmount();

                if (_isWantedItems)
                    _store.getWantedItems().add(wrapper.getItem(), qty, price);
                else
                    _store.addSaleItem(_seller, wrapper.getItem(), qty, price);
            }

            // merge item with existing
            else {

                //noinspection ConstantConditions
                Integer qty = _qtyMap != null
                        ? _qtyMap.get(wrapper)
                        : currentSnapshot.getAmount(wrapper) + saleItem.getQty();

                saleItem.setQty(qty);
                saleItem.setPricePerUnit(price);
            }
        }
    }

    private void removeMissing() {
        List<MatchableItem> originalItems = _snapshot.getMatchable();

        Set<MatchableItem> processed = new HashSet<MatchableItem>(originalItems.size());

        // search for less than total amount items
        for (MatchableItem startWrapper : originalItems) {

            if (processed.contains(startWrapper))
                continue;

            processed.add(startWrapper);

            SaleItem saleItem = _store.getSaleItem(_sellerId, startWrapper.getItem());

            if (saleItem == null)
                continue;

            int startQty = InventoryUtils.count(
                    _snapshot.getItemStacks(), startWrapper.getItem(),
                    StoreStackMatcher.getDefault());

            int currQty = InventoryUtils.count(
                    _currentInventory.getContents(), startWrapper.getItem(),
                    StoreStackMatcher.getDefault());

            if (currQty >= startQty)
                continue;

            int delta = Math.abs(startQty - currQty);

            int qty = saleItem.getQty();
            qty -= delta;
            saleItem.setQty(qty);
        }
    }
}
