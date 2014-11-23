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

import com.jcwhatever.bukkit.generic.extended.MaterialExt;
import com.jcwhatever.bukkit.generic.items.ItemStackHelper;
import com.jcwhatever.bukkit.generic.storage.IDataNode;
import com.jcwhatever.bukkit.generic.views.AbstractMenuInstance;
import com.jcwhatever.bukkit.generic.views.AbstractMenuView;
import com.jcwhatever.bukkit.generic.views.IView;
import com.jcwhatever.bukkit.generic.views.InventoryActionInfoHandler.InventoryActionInfo;
import com.jcwhatever.bukkit.generic.views.InventoryActionInfoHandler.ViewActionOrder;
import com.jcwhatever.bukkit.generic.views.MenuItem;
import com.jcwhatever.bukkit.generic.views.ViewCloseReason;
import com.jcwhatever.bukkit.generic.views.ViewInstance;
import com.jcwhatever.bukkit.generic.views.ViewMeta;
import com.jcwhatever.bukkit.generic.views.ViewResult;
import com.jcwhatever.bukkit.storefront.Category;
import com.jcwhatever.bukkit.storefront.Storefront;
import com.jcwhatever.bukkit.storefront.data.ISaleItem;
import com.jcwhatever.bukkit.storefront.data.ViewInfo;
import com.jcwhatever.bukkit.storefront.data.PaginatedSaleItems;
import com.jcwhatever.bukkit.storefront.data.PaginatedSaleItems.PaginatorPageType;
import com.jcwhatever.bukkit.storefront.data.PriceMap;
import com.jcwhatever.bukkit.storefront.data.QtyMap;
import com.jcwhatever.bukkit.storefront.data.SaleItem;
import com.jcwhatever.bukkit.storefront.data.SaleItemSnapshot;
import com.jcwhatever.bukkit.storefront.data.WantedItems;
import com.jcwhatever.bukkit.storefront.meta.SessionMetaKey;
import com.jcwhatever.bukkit.storefront.meta.ViewTaskMode;
import com.jcwhatever.bukkit.storefront.stores.IStore;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil.AddToInventoryResult;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil.AddToInventoryResult.SlotInfo;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil.PriceType;
import com.jcwhatever.bukkit.storefront.views.ItemTaskMenuView.ItemTaskMenuInstance.ItemTaskMenuResult;
import com.jcwhatever.bukkit.storefront.views.ItemTaskMenuView.ItemTaskMenuMeta;
import com.jcwhatever.bukkit.storefront.views.PaginatorView.PaginatorMetaKey;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * 
 * @author JC The Pants
 *
 */
@ViewInfo(pageType=PaginatorPageType.SALE_ITEM)
public class WantedView extends AbstractMenuView {

    @Override
    protected void onInit () {

        // do nothing
    }


    @Override
    protected void buildInventory () {

        // do nothing
    }


    @Override
    protected ViewInstance onCreateInstance (Player p, ViewInstance previous, ViewMeta sessionMeta,
                                             ViewMeta instanceMeta) {

        WantedViewInstance instance = new WantedViewInstance(this, previous, p, sessionMeta,
                instanceMeta);
        return instance;
    }


    @Override
    protected void onLoadSettings (IDataNode dataNode) {

        // do nothing
    }

    /**
     * 
     * @author JC The Pants
     *
     */
    public class WantedViewInstance extends AbstractMenuInstance {

        private IStore _store;
        private Inventory _inventory;
        private SaleItemSnapshot _snapshot;
        private PriceMap _priceMap;
        private QtyMap _qtyMap;

        public WantedViewInstance(IView view, ViewInstance previous, Player p,
                                  ViewMeta sessionMeta, ViewMeta instanceMeta) {

            super(view, previous, p, sessionMeta, instanceMeta);
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

            ViewTaskMode taskMode = getSessionMeta().getMeta(SessionMetaKey.TASK_MODE);
            setTitle(taskMode.getChatColor() + "Wanted");

            _priceMap = new PriceMap(getPlayer(), _store);
            _qtyMap = new QtyMap(getPlayer(), _store);

            setupInventory();

            return getPlayer().openInventory(_inventory);
        }


        @Override
        protected InventoryView onShowAsPrev (ViewMeta instanceMeta, ViewResult result) {

            if (result instanceof ItemTaskMenuResult) {
                ItemTaskMenuResult itemResult = (ItemTaskMenuResult) result;

                MenuItem menuItem = itemResult.getInstanceMeta().getMeta("__menuItem__");
                if (menuItem == null)
                    throw new IllegalStateException("__menuItem__ in ItemTaskMenuResult instance meta cannot be null.");

                if (!itemResult.isCancelled()) {

                    if (itemResult.getQty() == 0) {
                        _inventory.setItem(menuItem.getSlot(), ItemStackHelper.AIR);
                    }
                    else {

                        ItemStack itemStack = menuItem.getItemStack();

                        updateItem(itemStack, itemResult.getQty(), itemResult.getPrice());

                    }

                }
            }

            if (_inventory != null)
                return getPlayer().openInventory(_inventory);

            return onShow(instanceMeta);
        }


        @Override
        protected void onClose (ViewCloseReason reason) {

            if (reason == ViewCloseReason.GOING_BACK) {

                _store.updateWantedFromInventory(getPlayer(), _priceMap, _qtyMap, _inventory, _snapshot);

                // update meta pagination, if any
                PaginatedSaleItems pagin = getInstanceMeta().getMeta(PaginatorMetaKey.PAGINATOR);
                if (pagin != null) {

                    List<SaleItem> saleItems = _store.getWantedItems().getAll();

                    pagin.clear();
                    pagin.addAll(saleItems);
                }

            }

        }


        @Override
        protected MenuItem getMenuItem (int slot) {

            ItemStack itemStack = _inventory.getItem(slot);
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                return null;
            }

            MenuItem menuItem = new MenuItem(slot, "__temp__", (AbstractMenuView) getView());
            menuItem.setItemStack(itemStack);

            return menuItem;
        }


        @Override
        protected void onItemSelect (MenuItem menuItem) {
            showItemTaskMenu(menuItem);
        }
        
        @Override
        protected boolean onLowerItemsPlaced (InventoryActionInfo actionInfo, ViewActionOrder actionOrder) {
            return onLowerInventoryClick(actionInfo);
        }


        @Override
        protected boolean onLowerItemsPickup (InventoryActionInfo actionInfo, ViewActionOrder actionOrder) {
            return onLowerInventoryClick(actionInfo);
        }

        private boolean onLowerInventoryClick (InventoryActionInfo actionInfo) {

            ItemStack selectedStack = actionInfo.getSlotStack();
            if (selectedStack == null || selectedStack.getType() == Material.AIR)
                return false;
            
            // clone and repair selected stack
            selectedStack = selectedStack.clone();
            MaterialExt material = MaterialExt.from(selectedStack.getType());
            
            if (material.isRepairable()) {
                ItemStackHelper.repair(selectedStack);
            }
            
            SaleItemSnapshot snapshot = new SaleItemSnapshot(_inventory);
            boolean hasItem = snapshot.getAmount(selectedStack) > 0;
            selectedStack.setAmount(1);
            
            Category category = Storefront.getInstance().getCategoryManager().getCategory(selectedStack);
            if (category == null)
                return false;

            AddToInventoryResult result = ItemStackUtil.addToInventory(selectedStack, _inventory);    
            if (result.getLeftOver() == 1)
                return false;

            SlotInfo slotInfo = result.getSlotsInfo().get(0);
            int slot = slotInfo.getSlot();
            
            // get item to sell
            ItemStack itemStack = _inventory.getItem(slot);
            itemStack.setAmount(1);

            // open price view
            MenuItem menuItem = new MenuItem(slotInfo.getSlot(), "__temp__", (AbstractMenuView)getView());
            menuItem.setItemStack(itemStack);
            if (!hasItem) {
                _priceMap.setPrice(selectedStack, 1.0D);
                _qtyMap.setQty(selectedStack, 1);
            }

            showItemTaskMenu(menuItem);

            return false; // cancel underlying event
        }

        private void showItemTaskMenu(MenuItem menuItem) {
            ItemStack itemStack = menuItem.getItemStack();

            ViewMeta instanceMeta = new ViewMeta()
            .setMeta(ItemTaskMenuMeta.ITEMSTACK, itemStack)
            .setMeta(ItemTaskMenuMeta.MAX_QTY, 64)
            .setMeta(ItemTaskMenuMeta.QTY, _qtyMap.getQty(itemStack))
            .setMeta(ItemTaskMenuMeta.PRICE, _priceMap.getPrice(itemStack))
            .setMeta("__menuItem__", menuItem)
            ;

            getViewManager().show(getPlayer(), Storefront.VIEW_ITEM_TASK_MENU, getSourceBlock(), instanceMeta);
        }

        private void updateItem(ItemStack itemStack, int qty, double price) {

            itemStack.setAmount(qty);
            _priceMap.setPrice(itemStack, price);
            _qtyMap.setQty(itemStack, qty);

            ItemStackUtil.removeTempLore(itemStack);
            ItemStackUtil.setPriceLore(itemStack, price, PriceType.PER_ITEM);
            ItemStackUtil.addTempLore(itemStack, ChatColor.YELLOW + "Wanted: " + ChatColor.GRAY + qty);
        }

        private void setupInventory () {

            Integer page = getInstanceMeta().getMeta(PaginatorMetaKey.SELECTED_PAGE);
            PaginatedSaleItems pagin = getInstanceMeta().getMeta(PaginatorMetaKey.PAGINATOR);

            if (page == null) {
                page = 1;
            }

            if (pagin == null) {
                WantedItems wanted = _store.getWantedItems();
                List<SaleItem> saleItems = wanted.getAll();
                pagin = new PaginatedSaleItems(saleItems);
            }

            _inventory = Bukkit.createInventory(getPlayer(), PaginatedSaleItems.MAX_PER_PAGE, getTitle());

            List<ISaleItem> items = pagin.getPage(page, PaginatorPageType.SALE_ITEM);

            // add items to inventory
            for (ISaleItem item : items) {

                ItemStack clone = item.getItemStack().clone();

                updateItem(clone, item.getQty(), item.getPricePerUnit());

                _inventory.addItem(clone);
            }

            // take snapshot of inventory
            _snapshot = new SaleItemSnapshot(_inventory);

        }

    }

}
