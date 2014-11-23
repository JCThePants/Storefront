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

import com.jcwhatever.bukkit.generic.economy.EconomyHelper;
import com.jcwhatever.bukkit.generic.inventory.InventoryHelper;
import com.jcwhatever.bukkit.generic.items.ItemStackHelper;
import com.jcwhatever.bukkit.generic.items.ItemStackHelper.DisplayNameResult;
import com.jcwhatever.bukkit.generic.storage.IDataNode;
import com.jcwhatever.bukkit.generic.views.AbstractMenuInstance;
import com.jcwhatever.bukkit.generic.views.AbstractMenuView;
import com.jcwhatever.bukkit.generic.views.IView;
import com.jcwhatever.bukkit.generic.views.MenuItem;
import com.jcwhatever.bukkit.generic.views.SlotMap;
import com.jcwhatever.bukkit.generic.views.ViewCloseReason;
import com.jcwhatever.bukkit.generic.views.ViewInstance;
import com.jcwhatever.bukkit.generic.views.ViewMeta;
import com.jcwhatever.bukkit.generic.views.ViewResult;
import com.jcwhatever.bukkit.storefront.Category;
import com.jcwhatever.bukkit.storefront.Msg;
import com.jcwhatever.bukkit.storefront.Storefront;
import com.jcwhatever.bukkit.storefront.data.ISaleItem;
import com.jcwhatever.bukkit.storefront.data.ViewInfo;
import com.jcwhatever.bukkit.storefront.data.PaginatedSaleItems;
import com.jcwhatever.bukkit.storefront.data.PaginatedSaleItems.PaginatorPageType;
import com.jcwhatever.bukkit.storefront.data.PriceMap;
import com.jcwhatever.bukkit.storefront.data.SaleItem;
import com.jcwhatever.bukkit.storefront.meta.SessionMetaKey;
import com.jcwhatever.bukkit.storefront.meta.ViewTaskMode;
import com.jcwhatever.bukkit.storefront.stores.IStore;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil.PriceType;
import com.jcwhatever.bukkit.storefront.utils.StoreStackComparer;
import com.jcwhatever.bukkit.storefront.views.PaginatorView.PaginatorMetaKey;
import com.jcwhatever.bukkit.storefront.views.QuantityView.QuantityMetaKey;
import com.jcwhatever.bukkit.storefront.views.QuantityView.QuantityViewInstance.QuantityViewResult;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@ViewInfo(pageType=PaginatorPageType.SALE_ITEM)
public class SellWantedView extends AbstractMenuView {
 
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

        SellWantedInstance instance = new SellWantedInstance(this, previous, p, sessionMeta, instanceMeta);
        return instance;
    }

    @Override
    protected void onLoadSettings (IDataNode dataNode) {
        // do nothing
    }

    public class SellWantedInstance extends AbstractMenuInstance {

        private IStore _store;
        private Category _category;
        private Inventory _inventory;
        private PaginatedSaleItems _pagin;
        private SlotMap _slotMap;
        private PriceMap _priceMap;

        public SellWantedInstance(IView view, ViewInstance previous, Player p,
                                  ViewMeta sessionMeta, ViewMeta instanceMeta) {

            super(view, previous, p, sessionMeta, instanceMeta);
        }

        @Override
        protected InventoryView onShow (ViewMeta instanceMeta) {

            if (_inventory == null) {
                _store = getSessionMeta().getMeta(SessionMetaKey.STORE);
                if (_store == null)
                    throw new IllegalStateException("Missing Store object from session meta.");

                _category = getSessionMeta().getMeta(SessionMetaKey.CATEGORY);
            }

            setupInventory();

            return getPlayer().openInventory(_inventory);
        }

        @Override
        protected InventoryView onShowAsPrev (ViewMeta instanceMeta, ViewResult result) {

            if (result instanceof QuantityViewResult) {

                QuantityViewResult qtyResult = (QuantityViewResult)result;

                if (!qtyResult.isCancelled()) {

                    ISaleItem saleItemStack = result.getInstanceMeta().getMeta("__saleItemStack__");
                    MenuItem menuItem = result.getInstanceMeta().getMeta("__menuItem__");

                    if (saleItemStack.getTotalItems() == 0 || saleItemStack.isRemoved()) {
                        
                        // item is not being accepted anymore
                        Msg.tell(getPlayer(), "{RED}Problem: {WHITE}The store is no longer accepting that item.");

                        return onShow(instanceMeta);
                    }

                    // make sure player has the items they are trying to sell
                    if (!InventoryHelper.has(getPlayer().getInventory(), saleItemStack.getItemStack(), 
                            StoreStackComparer.getDefault(), qtyResult.getQty())) {

                        Msg.tell(getPlayer(), "{RED}Problem: {WHITE}You don't have the items you are trying to sell or do not have enough of them.");

                        return onShow(instanceMeta);
                    }

                    double totalCost = qtyResult.getQty() * saleItemStack.getPricePerUnit();
                    double storeBalance = EconomyHelper.getBalance(_store.getOwnerId());

                    if (storeBalance < totalCost) {
                        Msg.tell(getPlayer(), "{RED}Problem: {WHITE}The store cannot afford to buy from you right now.");
                        
                        return onShow(instanceMeta);
                    }

                    if (!_store.sellToStore(getPlayer(), saleItemStack, qtyResult.getQty(), totalCost)) {
                        Msg.tell(getPlayer(), "{RED}Problem: {WHITE}Failed to sell items to store.");
                        
                        return onShow(instanceMeta);
                    }
                    
                    
                    // remove quantity from wanted items
                    saleItemStack.increment(-qtyResult.getQty());
                    
                    if (menuItem != null) {
                        updateItem(menuItem.getItemStack(), saleItemStack.getQty(), saleItemStack.getPricePerUnit());
                        _inventory.setItem(menuItem.getSlot(), menuItem.getItemStack());
                        
                        Msg.tell(getPlayer(), "{GREEN}Success: {WHITE}Sold {0} {1} to the store for {GREEN}{2}{WHITE}.", 
                                qtyResult.getQty(),
                                ItemStackHelper.getDisplayName(menuItem.getItemStack(), DisplayNameResult.REQUIRED),
                                EconomyHelper.formatAmount(totalCost));
                    }
                    
                    
                       
                }
            }

            return onShow(instanceMeta);
        }

        @Override
        protected void onClose (ViewCloseReason reason) {
            // do nothing
        }

        @Override
        protected MenuItem getMenuItem (int slot) {
            return _slotMap.get(slot);
        }

        @Override
        protected void onItemSelect (MenuItem menuItem) {
            
            ItemStack itemStack = menuItem.getItemStack().clone();
            ItemStackUtil.removeTempLore(itemStack);
            
            int playerQty = InventoryHelper.count(getPlayer().getInventory(), itemStack);
                        
            if (playerQty == 0) {
                Msg.tell(getPlayer(), "{RED}Problem: {WHITE}You don't have that item in your inventory.");
                return;
            }
                
            ISaleItem saleItemStack = menuItem.getMeta("__saleItemStack__");
            
            ViewMeta instanceMeta = new ViewMeta()
            .setMeta(QuantityMetaKey.ITEMSTACK, itemStack)
            .setMeta(QuantityMetaKey.MAX_QTY, playerQty)
            .setMeta(QuantityMetaKey.PRICE, _priceMap.getPrice(itemStack))
            .setMeta(QuantityMetaKey.QTY, 1)
            .setMeta("__saleItemStack__", saleItemStack)
            .setMeta("__menuItem__", menuItem)
            ;

            getViewManager().show(getPlayer(), Storefront.VIEW_QUANTITY, getSourceBlock(), instanceMeta);
        }

        @Override
        public ViewResult getResult () {
            return null;
        }
        
        
        private void updateItem(ItemStack itemStack, int qty, double price) {

            itemStack.setAmount(qty);
            _priceMap.setPrice(itemStack, price);

            ItemStackUtil.removeTempLore(itemStack);
            ItemStackUtil.setPriceLore(itemStack, price, PriceType.PER_ITEM);
            ItemStackUtil.addTempLore(itemStack, ChatColor.YELLOW + "Wanted: " + ChatColor.GRAY + qty);
        }
        
        private void setupInventory () {

            ViewTaskMode taskMode = getSessionMeta().getMeta(SessionMetaKey.TASK_MODE);
            setTitle(taskMode.getChatColor() + "Sell Items to Store");

            _inventory = Bukkit.createInventory(getPlayer(), PaginatedSaleItems.MAX_PER_PAGE, getTitle());
            _slotMap = new SlotMap();
            _priceMap = new PriceMap(getPlayer(), _store);

            _pagin = getInstanceMeta().getMeta(PaginatorMetaKey.PAGINATOR);
            if (_pagin == null) {

                List<SaleItem> saleItems = _category != null
                        ? _store.getWantedItems().get(_category)
                        : _store.getWantedItems().getAll();
                        
                Msg.debug("Total Items: {0}", saleItems.size());


                _pagin = new PaginatedSaleItems(saleItems);
            }

            Integer page = getInstanceMeta().getMeta(PaginatorMetaKey.SELECTED_PAGE);
            if (page == null) {
                page = 1;
            }

            List<ISaleItem> saleItemStacks = _pagin.getPage(page, PaginatorPageType.SALE_ITEM);
            
            for (int i=0; i < saleItemStacks.size(); i++) {

                ISaleItem saleItemStack = saleItemStacks.get(i);

                MenuItem menuItem = new MenuItem(i, "item" + i, (AbstractMenuView)getView());
                menuItem.setItemStack(saleItemStack.getItemStack().clone());
                menuItem.getItemStack().setAmount(saleItemStack.getQty());
                menuItem.setMeta("__saleItemStack__", saleItemStack);
                
                _slotMap.put(i, menuItem);
                updateItem(menuItem.getItemStack(), saleItemStack.getQty(), saleItemStack.getPricePerUnit());
                
                _inventory.setItem(i, menuItem.getItemStack());
            }

        }

    }

}
