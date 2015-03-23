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


package com.jcwhatever.storefront.views;

import com.jcwhatever.storefront.Lang;
import com.jcwhatever.storefront.Msg;
import com.jcwhatever.storefront.Storefront;
import com.jcwhatever.storefront.category.Category;
import com.jcwhatever.storefront.category.CategoryManager;
import com.jcwhatever.storefront.data.ISaleItem;
import com.jcwhatever.storefront.data.PaginatedItems;
import com.jcwhatever.storefront.data.PriceMap;
import com.jcwhatever.storefront.data.SaleItem;
import com.jcwhatever.storefront.meta.SessionMetaKey;
import com.jcwhatever.storefront.meta.ViewSessionTask;
import com.jcwhatever.storefront.stores.IStore;
import com.jcwhatever.storefront.stores.StoreType;
import com.jcwhatever.storefront.utils.ItemStackUtil;
import com.jcwhatever.storefront.utils.ItemStackUtil.PriceType;
import com.jcwhatever.storefront.utils.StoreInventoryUpdater;
import com.jcwhatever.storefront.utils.StoreStackMatcher;
import com.jcwhatever.nucleus.utils.Permissions;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.Scheduler;
import com.jcwhatever.nucleus.utils.inventory.InventorySnapshot;
import com.jcwhatever.nucleus.utils.inventory.InventoryUtils;
import com.jcwhatever.nucleus.utils.items.ItemStackUtils;
import com.jcwhatever.nucleus.utils.items.MatchableItem;
import com.jcwhatever.nucleus.utils.language.Localizable;
import com.jcwhatever.nucleus.utils.scheduler.IScheduledTask;
import com.jcwhatever.nucleus.utils.scheduler.TaskHandler;
import com.jcwhatever.nucleus.views.View;
import com.jcwhatever.nucleus.views.ViewCloseReason;
import com.jcwhatever.nucleus.views.ViewOpenReason;
import com.jcwhatever.nucleus.views.chest.ChestEventAction;
import com.jcwhatever.nucleus.views.chest.ChestEventInfo;
import com.jcwhatever.nucleus.views.chest.ChestView;
import com.jcwhatever.nucleus.views.chest.InventoryItemAction.InventoryPosition;
import com.jcwhatever.nucleus.views.menu.MenuInventory;
import com.jcwhatever.nucleus.views.menu.PaginatorView;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A {@link ChestView} used to place items to be sold.
 */
public class SellView extends ChestView {

    @Localizable static final String _VIEW_TITLE =
            "{DARK_GRAY}Place items to sell";

    @Localizable static final String _ALREADY_PURCHASED =
            "Item already purchased. Inventory updated to reflect changes.";

    @Localizable static final String _CANNOT_SELL =
            "{RED}Problem: {WHITE}You cannot sell that item.";

    @Localizable static final String _NO_ROOM_IN_INVENTORY =
            "{RED}Problem: {WHITE}There is not enough room left in the store for the item you " +
                    "are trying to sell.";

    private IStore _store;
    private PriceMap _priceMap;
    private IScheduledTask _sledgehammer = null;
    private InventorySnapshot _snapshot;
    private MenuInventory _inventory;

    private int _page = 1;

    /**
     * Constructor.
     */
    public SellView(IStore store) {
        super(Storefront.getPlugin(), null);

        PreCon.notNull(store);

        _store = store;
    }

    @Override
    public String getTitle() {
        return Lang.get(_VIEW_TITLE);
    }

    @Override
    protected void onShow(ViewOpenReason reason) {

        // check if opening as a previous view.
        if (reason == ViewOpenReason.PREV) {

            // get the view that was closed, check for price change
            View nextView = getViewSession().getNext();
            if (nextView instanceof PriceView) {

                PriceView priceView = (PriceView) nextView;

                ItemStack item = priceView.getItemToPrice();

                Double price = priceView.getSelectedPrice();
                if (price == null && _priceMap.get(item) != null)
                    return;

                price = price != null ? price : 1.0D;

                ItemStackUtil.removeTempLore(item);

                _priceMap.set(item.clone(), price);

                ItemStackUtil.setPriceLore(_inventory, item,
                        price, PriceType.PER_ITEM, false);
            }
        }
        else {

            View paginatorView = getViewSession().getPrev();
            if (paginatorView instanceof PaginatorView)
                _page = ((PaginatorView) paginatorView).getSelectedPage();
        }
    }

    @Override
    protected void onClose(ViewCloseReason reason) {

        if (_sledgehammer != null) {
            _sledgehammer.cancel();
            _sledgehammer = null;

            // do final checks
            new Sledgehammer().run();
        }

        if (reason == ViewCloseReason.PREV ||
                reason == ViewCloseReason.ESCAPE) {

            new StoreInventoryUpdater(_store)
                    .seller(getPlayer())
                    .priceMap(_priceMap)
                    .inventory(_inventory)
                    .snapshot(_snapshot)
                    .update();
        }
    }

    @Override
    protected Inventory createInventory() {

        if (_inventory != null)
            return _inventory;

        // get session task
        ViewSessionTask task = getViewSession().getMeta().get(SessionMetaKey.TASK_MODE);
        if (task == null)
            throw new IllegalStateException("ViewSessionTask not set in session meta.");

        if (_priceMap == null)
            _priceMap = new PriceMap(getPlayer(), _store);

        // create new chest
        int maxSaleItems = getMaxSaleItems(getPlayer());

        MenuInventory inventory = new MenuInventory(getPlayer(), maxSaleItems, getTitle());

        PaginatedItems pagin = new PaginatedItems(_store, getPlayer().getUniqueId());

        List<ISaleItem> saleItemStacks = pagin.getPage(_page);

        if (saleItemStacks == null)
            return _inventory = inventory;

        // add items to inventory, fill price map
        for (ISaleItem saleItem : saleItemStacks) {

            ItemStack stack = saleItem.getItemStack().clone();
            stack.setAmount(saleItem.getQty());

            inventory.addItem(stack);

            _priceMap.set(saleItem.getMatchable(), saleItem.getPricePerUnit());
        }

        // take an inventory starting snapshot
        _snapshot = new InventorySnapshot(inventory, StoreStackMatcher.getDefault());

        if (_sledgehammer != null)
            _sledgehammer.cancel();

        _sledgehammer = Scheduler.runTaskRepeat(Storefront.getPlugin(), 1, 15, new Sledgehammer());

        return _inventory = inventory;
    }

    @Override
    protected ChestEventAction onItemsPlaced(ChestEventInfo eventInfo) {

        if (eventInfo.getInventoryPosition() == InventoryPosition.TOP)
            return onUpperItemsPlaced(eventInfo);

        ItemStackUtil.removeTempLore(getPlayer().getInventory(), true);

        ItemStack item = eventInfo.getItemStack().clone();

        ItemStackUtil.removeTempLore(item);

        int startQty = _snapshot.getAmount(item);

        if (startQty > 0) { // check if item was present at beginning

            // prevent sale of removed item
            new StoreInventoryUpdater(_store)
                    .seller(getPlayer())
                    .inventory(_inventory)
                    .snapshot(_snapshot)
                    .updateRemoved();
        }

        return ChestEventAction.ALLOW;
    }

    @Override
    protected ChestEventAction onItemsPickup(ChestEventInfo eventInfo) {

        if (eventInfo.getInventoryAction() == InventoryAction.PICKUP_HALF) { // right click
            openPriceMenu(eventInfo.getItemStack(), true);

            return ChestEventAction.DENY;
        }

        ItemStack item = eventInfo.getItemStack().clone();

        ItemStackUtil.removeTempLore(item);

        int startQty = _snapshot.getAmount(item);

        if (startQty > 0) { // check if item was present at beginning

            SaleItem saleItem = _store.getSaleItem(getPlayer().getUniqueId(), item);

            // see if item is still available to be removed (may have been purchased already)
            if (saleItem == null || saleItem.getQty() == 0 || saleItem.getQty() < item.getAmount()) {

                updateQuantities();

                Msg.tell(getPlayer(), Lang.get(_ALREADY_PURCHASED));

                // cancel event, prevent moving item out of store and into player chest
                return ChestEventAction.DENY;
            }
        }

        return ChestEventAction.ALLOW;
    }

    @Override
    protected ChestEventAction onItemsDropped(ChestEventInfo eventInfo) {
        return ChestEventAction.DENY;
    }

    /*
     * Handle adding items to sell.
     */
    private ChestEventAction onUpperItemsPlaced(ChestEventInfo eventInfo) {
        ItemStack cursorStack = eventInfo.getCursorStack();
        CategoryManager categoryManager = Storefront.getCategoryManager();
        ItemStack itemToAdd = eventInfo.getItemStack();

        switch (eventInfo.getInventoryAction()) {
            case PLACE_ONE:
                // put cursor items back into player inventory
                if (cursorStack != null && cursorStack.getAmount() != 1) {
                    return ChestEventAction.DENY;
                }
                break;

            case PLACE_SOME:
                return ChestEventAction.DENY;
        }

        // make sure the item is part of a valid category
        Category category = categoryManager.get(itemToAdd);
        if (category == null) {
            Msg.tell(getPlayer(), Lang.get(_CANNOT_SELL));
            return ChestEventAction.DENY;
        }

        int availableSpace = _store.getSpaceAvailable(getPlayer().getUniqueId(), itemToAdd);
        int added = InventoryUtils.count(_inventory, itemToAdd, StoreStackMatcher.getDefault());

        if (availableSpace - added < itemToAdd.getAmount()) {

            Msg.tell(getPlayer(), _NO_ROOM_IN_INVENTORY);
            return ChestEventAction.DENY; // prevent placing item
        }

        openPriceMenu(itemToAdd, false);

        return ChestEventAction.ALLOW;
    }

    /*
     * Get the max number of items a player is allowed to sell.
     */
    private int getMaxSaleItems (Player seller) {

        if (_store.getType() == StoreType.PLAYER_OWNABLE)
            return 6 * 9;

        for (int i = 6; i > 0; i--) {

            if (Permissions.has(seller, "storefront.sell.rows." + i)) {
                return i * 9;
            }
        }

        return 9;
    }

    /**
     * Update quantities in the inventory based on quantities reported by the store.
     */
    private void updateQuantities() {

        Scheduler.runTaskLater(Storefront.getPlugin(), 5, new Runnable() {

            @Override
            public void run() {

                List<MatchableItem> originalItems = _snapshot.getMatchable();

                Set<MatchableItem> processed = new HashSet<>(6 * 9);

                // modify original items
                for (MatchableItem startWrapper : originalItems) {

                    if (processed.contains(startWrapper))
                        continue;

                    processed.add(startWrapper);

                    SaleItem saleItem = _store.getSaleItem(getPlayer().getUniqueId(), startWrapper.getItem());

                    if (saleItem == null) {

                        int total = InventoryUtils.count(_inventory,
                                startWrapper.getItem(),
                                StoreStackMatcher.getDefault());

                        InventoryUtils.removeAmount(_inventory,
                                startWrapper.getItem(),
                                StoreStackMatcher.getDefault(),
                                total);

                        continue;
                    }

                    int currQty = saleItem.getQty();

                    int inventoryQty = InventoryUtils.count(
                            _inventory.getContents(),
                            startWrapper.getItem(),
                            StoreStackMatcher.getDefault());

                    if (inventoryQty <= currQty)
                        continue;

                    int delta = Math.abs(inventoryQty - currQty);

                    InventoryUtils.removeAmount(_inventory,
                            startWrapper.getItem(),
                            StoreStackMatcher.getDefault(),
                            delta);

                }
            }
        });
    }

    /*
     * Open a price menu view to change the sale price of an item.
     */
    private boolean openPriceMenu (ItemStack item, boolean force) {

        IStore store = Storefront.getStoreManager().get(getViewSession().getSessionBlock());
        if (store == null)
            throw new IllegalStateException("Could not get store instance from source block.");

        final ItemStack itemClone = item.clone();

        ItemStackUtil.removeTempLore(itemClone);

        MatchableItem matchableItem = new MatchableItem(itemClone, StoreStackMatcher.getDefault());

        final Double price = _priceMap.get(matchableItem);
        if (price == null || force) {

            // prevent sledge hammer from running during the delay to show
            // next view
            if (_sledgehammer != null) {
                _sledgehammer.cancel();
                _sledgehammer = null;
            }

            getViewSession().next(new PriceView(_store, itemClone, price != null
                    ? price
                    : 1.0D));
            return true;
        }
        else {
            ItemStackUtil.setPriceLore(_inventory, item, price, PriceType.PER_ITEM, true);
            return false;
        }
    }

    /**
     * Bukkit doesn't fire chest click events consistently,
     * so check periodically for inconsistencies caused by
     * an unfired event.
     */
    private class Sledgehammer extends TaskHandler {

        @Override
        public void run () {

            if (getViewSession().getCurrent() != SellView.this) {
                cancelTask();
                return;
            }

            ItemStack[] contents = _inventory.getContents();
            CategoryManager categoryManager = Storefront.getCategoryManager();

            for (int i = 0; i < contents.length; i++) {

                ItemStack stack = contents[i];
                if (stack == null || stack.getType() == Material.AIR)
                    continue;

                // make sure item has a category
                Category category = categoryManager.get(stack);
                if (category == null) {
                    ItemStackUtil.removeTempLore(stack);
                    _inventory.setItem(i, ItemStackUtils.AIR);
                    getPlayer().getInventory().addItem(stack);

                    continue;
                }

                // make sure upper chest has proper prices set
                boolean hasPrice = ItemStackUtil.hasPriceLore(stack);

                List<String> removed = ItemStackUtil.removeTempLore(stack);

                MatchableItem wrapper = new MatchableItem(stack, StoreStackMatcher.getDefault());

                Double price = _priceMap.get(wrapper);
                if (price == null) {
                    openPriceMenu(stack, false);
                    return;
                }
                else {
                    if (!hasPrice)
                        ItemStackUtil.setPriceLore(stack, price, PriceType.PER_ITEM);

                    ItemStackUtil.addTempLore(stack, removed);
                }
            }

            // Make sure player chest does not have prices set
            contents = getPlayer().getInventory().getContents();
            for (ItemStack stack : contents) {
                if (stack == null)
                    continue;

                ItemStackUtil.removeTempLore(stack);
            }
        }
    }
}
