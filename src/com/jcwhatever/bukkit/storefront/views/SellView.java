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

import com.jcwhatever.bukkit.generic.inventory.InventoryHelper;
import com.jcwhatever.bukkit.generic.utils.ItemStackUtils;
import com.jcwhatever.bukkit.generic.items.ItemWrapper;
import com.jcwhatever.bukkit.generic.permissions.Permissions;
import com.jcwhatever.bukkit.generic.storage.IDataNode;
import com.jcwhatever.bukkit.generic.views.AbstractView;
import com.jcwhatever.bukkit.generic.views.IView;
import com.jcwhatever.bukkit.generic.views.InventoryActionInfoHandler.InventoryActionInfo;
import com.jcwhatever.bukkit.generic.views.InventoryActionInfoHandler.ViewActionOrder;
import com.jcwhatever.bukkit.generic.views.ViewCloseReason;
import com.jcwhatever.bukkit.generic.views.ViewInstance;
import com.jcwhatever.bukkit.generic.views.ViewManager;
import com.jcwhatever.bukkit.generic.views.ViewMeta;
import com.jcwhatever.bukkit.generic.views.ViewResult;
import com.jcwhatever.bukkit.generic.views.ViewType;
import com.jcwhatever.bukkit.storefront.Category;
import com.jcwhatever.bukkit.storefront.CategoryManager;
import com.jcwhatever.bukkit.storefront.Msg;
import com.jcwhatever.bukkit.storefront.StoreType;
import com.jcwhatever.bukkit.storefront.Storefront;
import com.jcwhatever.bukkit.storefront.data.ISaleItem;
import com.jcwhatever.bukkit.storefront.data.PaginatedSaleItems;
import com.jcwhatever.bukkit.storefront.data.PaginatedSaleItems.PaginatorPageType;
import com.jcwhatever.bukkit.storefront.data.PriceMap;
import com.jcwhatever.bukkit.storefront.data.SaleItem;
import com.jcwhatever.bukkit.storefront.data.SaleItemSnapshot;
import com.jcwhatever.bukkit.storefront.meta.SessionMetaKey;
import com.jcwhatever.bukkit.storefront.meta.ViewTaskMode;
import com.jcwhatever.bukkit.storefront.stores.IStore;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil.PriceType;
import com.jcwhatever.bukkit.storefront.utils.StoreStackComparer;
import com.jcwhatever.bukkit.storefront.views.PaginatorView.PaginatorMetaKey;
import com.jcwhatever.bukkit.storefront.views.PriceView.PriceViewInstance.PriceViewResult;
import com.jcwhatever.bukkit.storefront.views.PriceView.PriceViewMeta;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SellView extends AbstractView {

    @Override
    protected void onInit (String name, IDataNode dataNode, ViewManager viewManager) {

        // do nothing
    }


    @Override
    public InventoryType getInventoryType () {

        return InventoryType.CHEST;
    }


    @Override
    public ViewType getViewType () {

        return ViewType.INVENTORY;
    }


    @Override
    public void dispose () {

        // do nothing
    }


    @Override
    protected void onLoadSettings (IDataNode dataNode) {

        // do nothing
    }


    @Override
    protected ViewInstance onCreateInstance (Player p, ViewInstance previous,
                                             ViewMeta persistantMeta, ViewMeta meta) {

        SellViewInstance instance = new SellViewInstance(this, previous, p, persistantMeta, meta);
        return instance;
    }

    /**
     * 
     * @author JC The Pants
     *
     */
    public class SellViewInstance extends ViewInstance {

        private Inventory _inventory;
        private PriceMap _priceMap;
        private Integer _sledgehammer = null;
        private IStore _store;
        private List<ISaleItem> _saleItemStacks;
        private SaleItemSnapshot _snapshot;


        public SellViewInstance(IView view, ViewInstance previous, Player p,
                                ViewMeta persistantMeta, ViewMeta initialMeta) {

            super(view, previous, p, persistantMeta, initialMeta);
        }


        @Override
        public ViewResult getResult () {

            return null;
        }


        @Override
        protected InventoryView onShow (ViewMeta meta) {

            _store = getSessionMeta().getMeta(SessionMetaKey.STORE);
            if (_store == null)
                throw new IllegalStateException("Store not set in session meta.");

            if (_priceMap == null)
                _priceMap = new PriceMap(getPlayer(), _store);

            ViewTaskMode taskMode = getSessionMeta().getMeta(SessionMetaKey.TASK_MODE);
            setTitle(taskMode.getChatColor() + "Place items to sell");

            _inventory = getInventory();

            if (_sledgehammer != null)
                Bukkit.getScheduler().cancelTask(_sledgehammer);

            _sledgehammer = Bukkit.getScheduler().scheduleSyncRepeatingTask(Storefront.getInstance(), new Sledgehammer(), 1, 15);

            return getPlayer().openInventory(_inventory);
        }

        @Override
        protected InventoryView onShowAsPrev (ViewMeta instanceMeta, ViewResult meta) {

            if (meta instanceof PriceViewResult) {
                PriceViewResult result = (PriceViewResult) meta;

                if (!result.isCancelled() || _priceMap.getPrice(result.getItemStack()) == null)
                    updatePrice(result);
            }

            return onShow(instanceMeta);
        }


        @Override
        protected void onClose (ViewCloseReason reason) {

            if (_sledgehammer != null) {
                Bukkit.getScheduler().cancelTask(_sledgehammer);
                _sledgehammer = null;

                // do final checks
                new Sledgehammer().run();
            }

            if (reason == ViewCloseReason.GOING_BACK) {

                _store.updateFromInventory(getPlayer(), _priceMap, _inventory, _snapshot);

                // update meta pagination, if any
                PaginatedSaleItems pagin = getInstanceMeta().getMeta(PaginatorMetaKey.PAGINATOR);
                if (pagin != null) {

                    List<SaleItem> saleItems = _store.getSaleItems(getPlayer().getUniqueId());

                    pagin.clear();
                    pagin.addAll(saleItems);
                }

            }
        }


        @Override
        protected boolean onItemsPlaced (final InventoryActionInfo actionInfo, ViewActionOrder actionOrder) {

            // put cursor items back into player inventory
            if (actionInfo.getInventoryAction() == InventoryAction.PLACE_ONE
                    && actionInfo.getCursorStack() != null
                    && actionInfo.getCursorStack().getAmount() != 1) {

                return false;
            }
            else if (actionInfo.getInventoryAction() == InventoryAction.PLACE_SOME) {
                return false;
            }

            Category category = Storefront.getInstance().getCategoryManager().getCategory(actionInfo.getStack());

            if (category == null) {
                Msg.tell(getPlayer(), "{RED}Problem: {WHITE}You cannot sell that item.");
                return false;
            }

            ItemStack itemToAdd = actionInfo.getStack();
            int availableSpace = _store.getSpaceAvailable(getPlayer().getUniqueId(), itemToAdd);

            int added = InventoryHelper.count(_inventory, itemToAdd, StoreStackComparer.getDefault());

            if (availableSpace - added < itemToAdd.getAmount()) {

                Msg.tell(getPlayer(), "{RED}Problem: {WHITE}There is not enough room left in the store for the item you are trying to sell.");

                return false; // prevent placing item
            }

            openPriceMenu(itemToAdd, false);

            return true;
        }


        @Override
        protected boolean onItemsPickup (InventoryActionInfo actionInfo, ViewActionOrder actionOrder) {

            if (actionInfo.getInventoryAction() == InventoryAction.PICKUP_HALF) { // right click
                openPriceMenu(actionInfo.getStack(), true);

                return false;
            }

            ItemStack item = actionInfo.getStack().clone();

            ItemStackUtil.removeTempLore(item);

            int startQty = _snapshot.getAmount(item);

            if (startQty > 0) { // check if item was present at beginning

                SaleItem saleItem = _store.getSaleItem(getPlayer().getUniqueId(), item);

                // see if item is still available to be removed (may have been purchased already)
                if (saleItem == null || saleItem.getQty() == 0 || saleItem.getQty() < item.getAmount()) { 

                    updateQuantities();

                    Msg.tell(getPlayer(), "Item already purchased. Inventory updated to reflect changes.");

                    // cancel event, prevent moving item out of store and into player inventory
                    return false;
                }
            }


            return true;
        }


        @Override
        protected boolean onItemsDropped (InventoryActionInfo actionInfo, ViewActionOrder actionOrder) {

            return false;
        }


        @Override
        protected boolean onLowerItemsPlaced (InventoryActionInfo actionInfo, ViewActionOrder actionOrder) {

            ItemStackUtil.removeTempLore(getPlayer().getInventory(), true);

            ItemStack item = actionInfo.getStack().clone();

            ItemStackUtil.removeTempLore(item);

            int startQty = _snapshot.getAmount(item);

            if (startQty > 0) { // check if item was present at beginning

                // prevent sale of removed item
                _store.updateRemovedFromInventory(getPlayer(), _inventory, _snapshot);
            }

            return true;
        }


        @Override
        protected boolean onLowerItemsPickup (InventoryActionInfo actionInfo, ViewActionOrder actionOrder) {
            return true;
        }


        private void updateQuantities() {
            Bukkit.getScheduler().runTaskLater(Storefront.getInstance(), new Runnable() {

                @Override
                public void run () {

                    List<ItemWrapper> originalItems = _snapshot.getWrappers();

                    Set<ItemWrapper> processed = new HashSet<ItemWrapper>();

                    // modify original items
                    for (ItemWrapper startWrapper : originalItems) {

                        if (processed.contains(startWrapper))
                            continue;

                        processed.add(startWrapper);

                        SaleItem saleItem = _store.getSaleItem(getPlayer().getUniqueId(), startWrapper.getItem());

                        if (saleItem == null) {

                            int total = InventoryHelper.count(_inventory, startWrapper.getItem(), StoreStackComparer.getDefault());

                            InventoryHelper.remove(_inventory, startWrapper.getItem(), StoreStackComparer.getDefault(), total);

                            continue;
                        }

                        int currQty = saleItem.getQty();

                        int inventoryQty = InventoryHelper.count(_inventory.getContents(), startWrapper.getItem(), StoreStackComparer.getDefault());

                        if (inventoryQty <= currQty)
                            continue;

                        int delta = Math.abs(inventoryQty - currQty);

                        InventoryHelper.remove(_inventory, startWrapper.getItem(), StoreStackComparer.getDefault(), delta);

                    }
                }
            }, 5);
        }


        private boolean openPriceMenu (ItemStack item, boolean force) {

            IStore store = Storefront.getInstance().getStoreManager().getStore(getSourceBlock());
            if (store == null)
                throw new IllegalStateException("Could not get store instance from source block.");

            final ItemStack itemClone = item.clone();

            ItemStackUtil.removeTempLore(itemClone);
            ;

            ItemWrapper itemWrapper = new ItemWrapper(itemClone, StoreStackComparer.getDefault());

            final Double price = _priceMap.getPrice(itemWrapper);
            if (price == null || force) {

                // prevent sledge hammer from running during the delay to show
                // next view
                if (_sledgehammer != null) {
                    Bukkit.getScheduler().cancelTask(_sledgehammer);
                    _sledgehammer = null;
                }

                ViewMeta meta = new ViewMeta()
                .setMeta(PriceViewMeta.ITEMSTACK, itemClone)
                .setMeta(PriceViewMeta.PRICE, price != null
                ? price
                        : 1.0D);

                getViewManager().show(getPlayer(), Storefront.VIEW_PRICE, getSourceBlock(), meta);
                return true;
            }
            else {
                ItemStackUtil.setPriceLore(_inventory, item, price, PriceType.PER_ITEM, true);
                return false;
            }
        }


        private int getMaxSaleItems (Player seller) {

            if (_store.getStoreType() == StoreType.PLAYER_OWNABLE)
                return PaginatedSaleItems.MAX_PER_PAGE;

            for (int i = 6; i > 0; i--) {

                if (Permissions.has(seller, "storefront.sell.rows." + i)) {
                    return i * 9;
                }
            }

            return 9;
        }


        private Inventory getInventory () {

            if (_inventory != null)
                return _inventory;

            // create new inventory
            int maxSaleItems = getMaxSaleItems(getPlayer());

            Inventory inventory = Bukkit.createInventory(getPlayer(), maxSaleItems, getTitle());

            Integer page = getInstanceMeta().getMeta(PaginatorMetaKey.SELECTED_PAGE);
            if (page == null)
                page = 1;

            List<SaleItem> saleItems = _store.getSaleItems(getPlayer().getUniqueId());
            PaginatedSaleItems pagin = new PaginatedSaleItems(saleItems);

            _saleItemStacks = pagin.getPage(page, PaginatorPageType.SALE_ITEM_STACK);

            if (_saleItemStacks == null)
                return inventory;

            for (ISaleItem saleItem : _saleItemStacks) {

                ItemStack stack = saleItem.getItemStack().clone();
                stack.setAmount(saleItem.getQty());

                inventory.addItem(stack);

                _priceMap.setPrice(saleItem.getWrapper(), saleItem.getPricePerUnit());
            }

            _snapshot = new SaleItemSnapshot(inventory);

            return inventory;
        }


        private void updatePrice (PriceViewResult priceResult) {

            ItemStackUtil.setPriceLore(_inventory, priceResult.getItemStack().clone(), priceResult.getPrice(), PriceType.PER_ITEM, false);

            _priceMap.setPrice(priceResult.getItemStack(), priceResult.getPrice());
        }

        /**
         * Bukkit doesn't fire inventory click events consistently,
         * so check periodically for inconsistencies caused by
         * an unfired event. 
         * 
         * @author JC The Pants
         *
         */
        private class Sledgehammer implements Runnable {

            @Override
            public void run () {

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

                    // make sure upper inventory has proper prices set
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

                // Make sure player inventory does not have prices set
                contents = getPlayer().getInventory().getContents();
                for (int i = 0; i < contents.length; i++) {
                    ItemStack stack = contents[i];
                    if (stack == null)
                        continue;

                    ItemStackUtil.removeTempLore(stack);
                }

            }

        }

    }

}
