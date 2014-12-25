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


package com.jcwhatever.bukkit.storefront.views.sell;

import com.jcwhatever.generic.utils.items.ItemWrapper;
import com.jcwhatever.generic.permissions.Permissions;
import com.jcwhatever.generic.scheduler.ScheduledTask;
import com.jcwhatever.generic.scheduler.TaskHandler;
import com.jcwhatever.generic.utils.InventoryUtils;
import com.jcwhatever.generic.utils.items.ItemStackUtils;
import com.jcwhatever.generic.utils.MetaKey;
import com.jcwhatever.generic.utils.PreCon;
import com.jcwhatever.generic.utils.Scheduler;
import com.jcwhatever.generic.views.IView;
import com.jcwhatever.generic.views.IViewFactory;
import com.jcwhatever.generic.views.ViewSession;
import com.jcwhatever.generic.views.chest.ChestEventAction;
import com.jcwhatever.generic.views.chest.ChestEventInfo;
import com.jcwhatever.generic.views.chest.ChestView;
import com.jcwhatever.generic.views.chest.InventoryItemAction.InventoryPosition;
import com.jcwhatever.generic.views.data.ViewArguments;
import com.jcwhatever.generic.views.data.ViewArguments.ViewArgument;
import com.jcwhatever.generic.views.data.ViewCloseReason;
import com.jcwhatever.generic.views.data.ViewOpenReason;
import com.jcwhatever.generic.views.data.ViewResults;
import com.jcwhatever.generic.views.menu.PaginatorView;
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
import com.jcwhatever.bukkit.storefront.meta.ViewTaskMode;
import com.jcwhatever.bukkit.storefront.stores.IStore;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil.PriceType;
import com.jcwhatever.bukkit.storefront.utils.StoreStackComparer;
import com.jcwhatever.bukkit.storefront.views.price.PriceView;
import com.jcwhatever.bukkit.storefront.views.price.PriceViewResult;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

public class SellView extends ChestView {

    private static MetaKey<ItemStack>
            PRICED_ITEM_STACK = new MetaKey<ItemStack>(ItemStack.class);

    private IStore _store;
    private PriceMap _priceMap;
    private ScheduledTask _sledgehammer = null;
    private List<ISaleItem> _saleItemStacks;
    private SaleItemSnapshot _snapshot;
    private Inventory _inventory;

    protected SellView(@Nullable String title, ViewSession session, IViewFactory factory, ViewArguments arguments) {
        super(title, session, factory, arguments, StoreStackComparer.getDefault());
    }

    @Override
    protected void onShow(ViewOpenReason reason) {

        if (reason == ViewOpenReason.PREV) {

            IView nextView = getViewSession().getNextView();
            if (nextView == null)
                return; // finished

            ViewResults results = nextView.getResults();

            if (results instanceof PriceViewResult) {
                PriceViewResult result = (PriceViewResult) results;

                if (!result.isCancelled() || _priceMap.getPrice(result.getItemStack()) == null)
                    updatePrice(result);
            }
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

        ViewTaskMode taskMode = getViewSession().getMeta(SessionMetaKey.TASK_MODE);
        PreCon.notNull(taskMode);

        setTitle(taskMode.getChatColor() + "Place items to sell");

        // create new chest
        int maxSaleItems = getMaxSaleItems(getPlayer());

        Inventory inventory = getTitle() != null
                ? Bukkit.createInventory(getPlayer(), maxSaleItems, getTitle())
                : Bukkit.createInventory(getPlayer(), maxSaleItems);

        Integer page = getArguments().get(PaginatorView.SELECTED_PAGE);
        if (page == null)
            page = 1;

        PaginatedItems pagin = new PaginatedItems(_store, getPlayer().getUniqueId());

        _saleItemStacks = pagin.getPage(page);//, PaginatorPageType.SALE_ITEM_STACK);

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
        Category category = categoryManager.getCategory(itemToAdd);
        if (category == null) {
            Msg.tell(getPlayer(), "{RED}Problem: {WHITE}You cannot sell that item.");
            return ChestEventAction.DENY;
        }

        int availableSpace = _store.getSpaceAvailable(getPlayer().getUniqueId(), itemToAdd);

        int added = InventoryUtils.count(_inventory, itemToAdd, StoreStackComparer.getDefault());

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

                List<ItemWrapper> originalItems = _snapshot.getWrappers();

                Set<ItemWrapper> processed = new HashSet<>(6 * 9);

                // modify original items
                for (ItemWrapper startWrapper : originalItems) {

                    if (processed.contains(startWrapper))
                        continue;

                    processed.add(startWrapper);

                    SaleItem saleItem = _store.getSaleItem(getPlayer().getUniqueId(), startWrapper.getItem());

                    if (saleItem == null) {

                        int total = InventoryUtils.count(_inventory, startWrapper.getItem(), StoreStackComparer.getDefault());

                        InventoryUtils.remove(_inventory, startWrapper.getItem(), StoreStackComparer.getDefault(), total);

                        continue;
                    }

                    int currQty = saleItem.getQty();

                    int inventoryQty = InventoryUtils.count(_inventory.getContents(), startWrapper.getItem(), StoreStackComparer.getDefault());

                    if (inventoryQty <= currQty)
                        continue;

                    int delta = Math.abs(inventoryQty - currQty);

                    InventoryUtils.remove(_inventory, startWrapper.getItem(), StoreStackComparer.getDefault(), delta);

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

        ItemWrapper itemWrapper = new ItemWrapper(itemClone, StoreStackComparer.getDefault());

        final Double price = _priceMap.getPrice(itemWrapper);
        if (price == null || force) {

            // prevent sledge hammer from running during the delay to show
            // next view
            if (_sledgehammer != null) {
                _sledgehammer.cancel();
                _sledgehammer = null;
            }

            ViewArguments arguments = new ViewArguments(
                    new ViewArgument(PRICED_ITEM_STACK, item),
                    new ViewArgument(PriceView.ITEM_STACK, itemClone),
                    new ViewArgument(PriceView.INITIAL_PRICE, price != null
                            ? price
                            : 1.0D)
            );

            getViewSession().next(Storefront.VIEW_PRICE, arguments);
            return true;
        }
        else {
            ItemStackUtil.setPriceLore(_inventory, item, price, PriceType.PER_ITEM, true);
            return false;
        }
    }

    @Nullable
    @Override
    public ViewResults getResults() {
        return null;
    }

    private void updatePrice (PriceViewResult priceResult) {

        ItemStack item = getViewSession().getNextView().getArguments().get(PRICED_ITEM_STACK);

        ItemStackUtil.setPriceLore(_inventory, item,
                priceResult.getPrice(1), PriceType.PER_ITEM, false);

        ItemStackUtil.removeTempLore(priceResult.getItemStack());

        _priceMap.setPrice(priceResult.getItemStack(), priceResult.getPrice(1));
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
                Category category = categoryManager.getCategory(stack);
                if (category == null) {
                    ItemStackUtil.removeTempLore(stack);
                    _inventory.setItem(i, ItemStackUtils.AIR);
                    getPlayer().getInventory().addItem(stack);

                    continue;
                }

                // make sure upper chest has proper prices set
                boolean hasPrice = ItemStackUtil.hasPriceLore(stack);

                List<String> removed = ItemStackUtil.removeTempLore(stack);

                ItemWrapper wrapper = new ItemWrapper(stack, StoreStackComparer.getDefault());

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
