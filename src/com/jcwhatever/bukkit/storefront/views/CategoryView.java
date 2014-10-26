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

import com.jcwhatever.bukkit.generic.storage.IDataNode;
import com.jcwhatever.bukkit.generic.views.AbstractMenuInstance;
import com.jcwhatever.bukkit.generic.views.AbstractMenuView;
import com.jcwhatever.bukkit.generic.views.MenuItem;
import com.jcwhatever.bukkit.generic.views.SlotMap;
import com.jcwhatever.bukkit.generic.views.ViewCloseReason;
import com.jcwhatever.bukkit.generic.views.ViewInstance;
import com.jcwhatever.bukkit.generic.views.ViewMeta;
import com.jcwhatever.bukkit.generic.views.ViewResult;
import com.jcwhatever.bukkit.storefront.Category;
import com.jcwhatever.bukkit.storefront.CategoryManager;
import com.jcwhatever.bukkit.storefront.StoreType;
import com.jcwhatever.bukkit.storefront.Storefront;
import com.jcwhatever.bukkit.storefront.data.SaleItem;
import com.jcwhatever.bukkit.storefront.meta.SessionMetaKey;
import com.jcwhatever.bukkit.storefront.meta.ViewTaskMode;
import com.jcwhatever.bukkit.storefront.meta.ViewTaskMode.BasicTask;
import com.jcwhatever.bukkit.storefront.stores.IStore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import java.util.List;

public class CategoryView extends AbstractMenuView {

    @Override
    protected void onInit () {
        // do nothing      
    }

    
    @Override
    protected void onLoadSettings (IDataNode dataNode) {

        // do nothing
    }


    @Override
    protected void buildInventory () {

        // do nothing
    }


    @Override
    protected ViewInstance onCreateInstance (Player p, ViewInstance previous, ViewMeta sessionMeta,
                                             ViewMeta instanceMeta) {

        CategoryViewInstance instance = new CategoryViewInstance(this, previous, p, sessionMeta,
                instanceMeta);
        return instance;
    }
    
    public static String getCategoryViewName(IStore store, ViewTaskMode currentMode) {
        String nextView = null;

        if (store.getStoreType() == StoreType.PLAYER_OWNABLE
                && currentMode.getBasicTask() == BasicTask.SELL) {

            nextView = currentMode.isOwnerManagerTask()
                    ? Storefront.VIEW_SELL
                    : Storefront.VIEW_SELL_WANTED;
        }
        else {
            nextView = currentMode.getBasicTask() == BasicTask.SELL
                    ? Storefront.VIEW_SELL
                    : Storefront.VIEW_BUY;
        }
        
        return nextView;
    }
    
    public static List<SaleItem> getCategorySaleItems(IStore store, ViewTaskMode currentMode, Category category) {
        List<SaleItem> saleItems = null;

        if (store.getStoreType() == StoreType.PLAYER_OWNABLE
                && currentMode.getBasicTask() == BasicTask.SELL) {
            
            saleItems = currentMode.isOwnerManagerTask()
                    ? store.getSaleItems(category)
                    : store.getWantedItems().get(category);
        }
        else {
            
            saleItems = store.getSaleItems(category);
        }
        
        return saleItems;
    }
    
    
    public static void showNextView(ViewInstance sender, IStore store, ViewTaskMode mode, ViewMeta instanceMeta) {
        

        List<Category> categories = getCategories(store, mode);
        
        // if there are less than a certain number of items
        List<SaleItem> saleItems = mode == ViewTaskMode.PLAYER_SELL 
                ? store.getWantedItems().getAll()
                : store.getSaleItems();
        
        int totalSlots = 0;
        for (SaleItem saleItem : saleItems)
            totalSlots += saleItem.getTotalSlots();
        
        if (totalSlots <= 6 * 9 * 3 || categories.size() <= 1) {
            String nextView = getCategoryViewName(store, mode);
            PaginatorView.showNextView(sender, nextView, saleItems);
            return;
        }

        // show categories
        sender.getView().getViewManager().show(sender.getPlayer(), Storefront.VIEW_CATEGORY, sender.getSourceBlock(), instanceMeta);
    }


    private static List<Category> getCategories(IStore store, ViewTaskMode mode) {
        List<Category> categories = null;

        if (mode.isOwnerManagerTask()) {
            CategoryManager manager = Storefront.getInstance().getCategoryManager();
            categories = manager.getCategories();
        }
        else {
            categories = mode.getBasicTask() == BasicTask.BUY
                    ? store.getBuyCategories()
                    : store.getSellCategories();
        }
        
        return categories;
    }

    /**
     * 
     * @author JC The Pants
     *
     */
    public class CategoryViewInstance extends AbstractMenuInstance {

        private IStore _store;
        private ViewTaskMode _taskMode;
        private Category _previousCategory;
        private SlotMap _slotMap;


        public CategoryViewInstance(AbstractMenuView view, ViewInstance previous, Player p,
                                    ViewMeta sessionMeta, ViewMeta instanceMeta) {

            super(view, previous, p, sessionMeta, instanceMeta);
        }


        @Override
        public ViewResult getResult () {

            return null;
        }


        @Override
        protected InventoryView onShow (ViewMeta meta) {

            // get store
            _store = getSessionMeta().getMeta(SessionMetaKey.STORE);
            if (_store == null)
                throw new IllegalStateException("Store not set in session meta.");

            _taskMode = getSessionMeta().getMeta(SessionMetaKey.TASK_MODE);
            if (_taskMode == null)
                throw new IllegalStateException("Task Mode not set in session meta.");

            _previousCategory = getSessionMeta().getMeta(SessionMetaKey.CATEGORY);

            // set title
            if (_taskMode.getBasicTask() == BasicTask.BUY)
                setTitle(_taskMode.getChatColor() + "Buy Item Categories");
            else
                setTitle(_taskMode.getChatColor() + "Sell Item Categories");

            // get inventory
            Inventory inventory = getInventory(_store);

            // show inventory to player
            return getPlayer().openInventory(inventory);
        }


        @Override
        protected InventoryView onShowAsPrev (ViewMeta instanceMeta, ViewResult meta) {

            return onShow(instanceMeta);
        }


        @Override
        protected MenuItem getMenuItem (int slot) {
            return _slotMap.get(slot);
        }


        @Override
        protected void onItemSelect (MenuItem menuItem) {

            Category category = menuItem.getMeta("__category__");
            if (category == null)
                return;

            getSessionMeta().setMeta(SessionMetaKey.CATEGORY, category);

            // get sale items in category

            List<SaleItem> saleItems = getCategorySaleItems(_store, _taskMode, category);
            String nextView = getCategoryViewName(_store, _taskMode);

            PaginatorView.showNextView(this, nextView, saleItems);
        }


        @Override
        protected void onClose (ViewCloseReason reason) {

            getSessionMeta().setMeta(SessionMetaKey.CATEGORY, _previousCategory);
        }


        protected Inventory getInventory (IStore store) {
            
            List<Category> categories = getCategories(store, _taskMode);

            _slotMap = new SlotMap();

            double itemSize = categories.size();
            int rows = (int) Math.ceil(itemSize / 9);

            _slotMap.setTotalSlots(rows * 9);

            Inventory inventory = Bukkit.createInventory(null, _slotMap.getTotalSlots(), getTitle());

            int size = Math.min(categories.size(), _slotMap.getTotalSlots());

            for (int i = 0; i < size; i++) {

                Category category = categories.get(i);
                
                List<SaleItem> saleItems = store.getSaleItems(category);
                int totalInCategory = 0;
                for (SaleItem saleItem : saleItems)
                    totalInCategory += saleItem.getQty();
                
                MenuItem item = new MenuItem(i, category.getName(), (AbstractMenuView) getView());
                item.setItemStack(category.getMenuItem());
                item.setTitle("" + ChatColor.YELLOW + ChatColor.ITALIC + category.getTitle().toUpperCase() + ChatColor.AQUA + " " + totalInCategory + " items  " );
                item.setDescription(category.getDescription());

                item.setMeta("__category__", category);

                inventory.setItem(i, item.getItemStack());
                _slotMap.put(i, item);
            }
            
            return inventory;
        }

    }

}
