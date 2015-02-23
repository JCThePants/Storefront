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


package com.jcwhatever.bukkit.storefront.views;

import com.jcwhatever.bukkit.storefront.Category;
import com.jcwhatever.bukkit.storefront.CategoryManager;
import com.jcwhatever.bukkit.storefront.Msg;
import com.jcwhatever.bukkit.storefront.StoreType;
import com.jcwhatever.bukkit.storefront.Storefront;
import com.jcwhatever.bukkit.storefront.data.ISaleItem;
import com.jcwhatever.bukkit.storefront.data.PaginatedItems;
import com.jcwhatever.bukkit.storefront.data.PriceMap;
import com.jcwhatever.bukkit.storefront.data.SaleItem;
import com.jcwhatever.bukkit.storefront.data.SaleItemSnapshot;
import com.jcwhatever.bukkit.storefront.meta.SessionMetaKey;
import com.jcwhatever.bukkit.storefront.meta.ViewSessionTask;
import com.jcwhatever.bukkit.storefront.stores.IStore;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil.PriceType;
import com.jcwhatever.bukkit.storefront.utils.StoreStackMatcher;
import com.jcwhatever.nucleus.utils.Permissions;
import com.jcwhatever.nucleus.utils.scheduler.ScheduledTask;
import com.jcwhatever.nucleus.utils.scheduler.TaskHandler;
import com.jcwhatever.nucleus.utils.MetaKey;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.Scheduler;
import com.jcwhatever.nucleus.utils.inventory.InventoryUtils;
import com.jcwhatever.nucleus.utils.items.ItemStackUtils;
import com.jcwhatever.nucleus.utils.items.MatchableItem;
import com.jcwhatever.nucleus.views.View;
import com.jcwhatever.nucleus.views.chest.ChestEventAction;
import com.jcwhatever.nucleus.views.chest.ChestEventInfo;
import com.jcwhatever.nucleus.views.chest.ChestView;
import com.jcwhatever.nucleus.views.chest.InventoryItemAction.InventoryPosition;
import com.jcwhatever.nucleus.views.ViewCloseReason;
import com.jcwhatever.nucleus.views.ViewOpenReason;
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

public class SellView extends ChestView {

    private static MetaKey<ItemStack>
            PRICED_ITEM_STACK = new MetaKey<ItemStack>(ItemStack.class);

    private IStore _store;
    private PriceMap _priceMap;
    private ScheduledTask _sledgehammer = null;
    private List<ISaleItem> _saleItemStacks;
    private SaleItemSnapshot _snapshot;
    private Inventory _inventory;

    private int _page = 1;
    private PaginatedItems _pagin;

    public SellView(PaginatedItems paginatedItems) {
        super(Storefront.getInstance(), null);

        PreCon.notNull(paginatedItems);

        _pagin = paginatedItems;
    }

    @Override
    public String getTitle() {
        ViewSessionTask taskMode = getViewSession().getMeta(SessionMetaKey.TASK_MODE);
        if (taskMode == null)
            throw new AssertionError();

        return taskMode.getChatColor() + "Place items to sell";
    }

    @Override
    protected void onShow(ViewOpenReason reason) {

        if (reason == ViewOpenReason.PREV) {

            View nextView = getViewSession().getNextView();
            if (nextView instanceof PriceView) {


                PriceView priceView = (PriceView) nextView;

                ItemStack item = priceView.getItemToPrice();

                Double price = priceView.getSelectedPrice();
                if (price == null && _priceMap.getPrice(item) != null)
                    return;

                price = price != null ? price : 1.0D;

                ItemStackUtil.removeTempLore(item);

                _priceMap.setPrice(item.clone(), price);

                ItemStackUtil.setPriceLore(_inventory, item,
                        price, PriceType.PER_ITEM, false);
            }
        }
        else {

            View paginatorView = getViewSession().getPrevView();
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

            _store.updateFromInventory(getPlayer(), _priceMap, _inventory, _snapshot);
        }
    }

    @Override
    protected Inventory createInventory() {

        if (_inventory != null)
            return _inventory;

        _store = getViewSession().getMeta(SessionMetaKey.STORE);
        if (_store == null)
            throw new IllegalStateException("Store not set in session meta.");

        if (_priceMap == null)
            _priceMap = new PriceMap(getPlayer(), _store);

        ViewSessionTask taskMode = getViewSession().getMeta(SessionMetaKey.TASK_MODE);
        PreCon.notNull(taskMode);

        // create new chest
        int maxSaleItems = getMaxSaleItems(getPlayer());

        MenuInventory inventory = new MenuInventory(getPlayer(), maxSaleItems, getTitle());

        PaginatedItems pagin = new PaginatedItems(_store, getPlayer().getUniqueId());

        _saleItemStacks = pagin.getPage(_page);

        if (_saleItemStacks == null)
            return _inventory = inventory;

        for (ISaleItem saleItem : _saleItemStacks) {

            ItemStack stack = saleItem.getItemStack().clone();
            stack.setAmount(saleItem.getQty());

            inventory.addItem(stack);

            _priceMap.setPrice(saleItem.getWrapper(), saleItem.getPricePerUnit());
        }

        _snapshot = new SaleItemSnapshot(inventory);

        if (_sledgehammer != null)
            _sledgehammer.cancel();

        _sledgehammer = Scheduler.runTaskRepeat(Storefront.getInstance(), 1, 15, new Sledgehammer());

        return _inventory = inventory;
    }

    @Override
    protected ChestEventAction onItemsPlaced(ChestEventInfo eventInfo) {

        if (eventInfo.getInventoryPosition() == InventoryPosition.TOP) {
            return onUpperItemsPlaced(eventInfo);
        }

        ItemStackUtil.removeTempLore(getPlayer().getInventory(), true);

        ItemStack item = eventInfo.getItemStack().clone();

        ItemStackUtil.removeTempLore(item);

        int startQty = _snapshot.getAmount(item);

        if (startQty > 0) { // check if item was present at beginning

            // prevent sale of removed item
            _store.updateRemovedFromInventory(getPlayer(), _inventory, _snapshot);
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

                Msg.tell(getPlayer(), "Item already purchased. Inventory updated to reflect changes.");

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


    private ChestEventAction onUpperItemsPlaced(ChestEventInfo eventInfo) {
        ItemStack cursorStack = eventInfo.getCursorStack();
        CategoryManager categoryManager = Storefront.getInstance().getCategoryManager();
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
            Msg.tell(getPlayer(), "{RED}Problem: {WHITE}You cannot sell that item.");
            return ChestEventAction.DENY;
        }

        int availableSpace = _store.getSpaceAvailable(getPlayer().getUniqueId(), itemToAdd);

        int added = InventoryUtils.count(_inventory, itemToAdd, StoreStackMatcher.getDefault());

        if (availableSpace - added < itemToAdd.getAmount()) {

            Msg.tell(getPlayer(), "{RED}Problem: {WHITE}There is not enough room left in the store for the item you are trying to sell.");

            return ChestEventAction.DENY; // prevent placing item
        }

        openPriceMenu(itemToAdd, false);

        return ChestEventAction.ALLOW;
    }


    private int getMaxSaleItems (Player seller) {

        if (_store.getStoreType() == StoreType.PLAYER_OWNABLE)
            return 6 * 9;

        for (int i = 6; i > 0; i--) {

            if (Permissions.has(seller, "storefront.sell.rows." + i)) {
                return i * 9;
            }
        }

        return 9;
    }

    private void updateQuantities() {

        Scheduler.runTaskLater(Storefront.getInstance(), 5, new Runnable() {

            @Override
            public void run() {

                List<MatchableItem> originalItems = _snapshot.getWrappers();

                Set<MatchableItem> processed = new HashSet<>(6 * 9);

                // modify original items
                for (MatchableItem startWrapper : originalItems) {

                    if (processed.contains(startWrapper))
                        continue;

                    processed.add(startWrapper);

                    SaleItem saleItem = _store.getSaleItem(getPlayer().getUniqueId(), startWrapper.getItem());

                    if (saleItem == null) {

                        int total = InventoryUtils.count(_inventory, startWrapper.getItem(), StoreStackMatcher.getDefault());

                        InventoryUtils.removeAmount(_inventory, startWrapper.getItem(), StoreStackMatcher.getDefault(), total);

                        continue;
                    }

                    int currQty = saleItem.getQty();

                    int inventoryQty = InventoryUtils.count(_inventory.getContents(), startWrapper.getItem(), StoreStackMatcher.getDefault());

                    if (inventoryQty <= currQty)
                        continue;

                    int delta = Math.abs(inventoryQty - currQty);

                    InventoryUtils.removeAmount(_inventory, startWrapper.getItem(), StoreStackMatcher.getDefault(), delta);

                }
            }
        });
    }

    private boolean openPriceMenu (ItemStack item, boolean force) {

        IStore store = Storefront.getInstance().getStoreManager().getStore(getViewSession().getSessionBlock());
        if (store == null)
            throw new IllegalStateException("Could not get store instance from source block.");

        final ItemStack itemClone = item.clone();

        ItemStackUtil.removeTempLore(itemClone);

        MatchableItem matchableItem = new MatchableItem(itemClone, StoreStackMatcher.getDefault());

        final Double price = _priceMap.getPrice(matchableItem);
        if (price == null || force) {

            // prevent sledge hammer from running during the delay to show
            // next view
            if (_sledgehammer != null) {
                _sledgehammer.cancel();
                _sledgehammer = null;
            }

            getViewSession().next(new PriceView(itemClone, price != null
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

            if (getViewSession().getCurrentView() != SellView.this) {
                cancelTask();
                return;
            }

            ItemStack[] contents = _inventory.getContents();
            CategoryManager categoryManager = Storefront.getInstance().getCategoryManager();

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

                Double price = _priceMap.getPrice(wrapper);
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
