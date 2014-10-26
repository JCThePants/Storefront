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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import com.jcwhatever.bukkit.generic.storage.IDataNode;
import com.jcwhatever.bukkit.generic.utils.PreCon;
import com.jcwhatever.bukkit.generic.views.AbstractMenuInstance;
import com.jcwhatever.bukkit.generic.views.AbstractMenuView;
import com.jcwhatever.bukkit.generic.views.IView;
import com.jcwhatever.bukkit.generic.views.MenuItem;
import com.jcwhatever.bukkit.generic.views.ViewCloseReason;
import com.jcwhatever.bukkit.generic.views.ViewInstance;
import com.jcwhatever.bukkit.generic.views.ViewMeta;
import com.jcwhatever.bukkit.generic.views.ViewResult;
import com.jcwhatever.bukkit.storefront.meta.SessionMetaKey;
import com.jcwhatever.bukkit.storefront.meta.ViewTaskMode;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil.PriceType;

public class QuantityView extends AbstractMenuView {

    private static final String META_QTY_INCREMENT = "__qty_increment__";

    private static final int SLOT_ITEM = 0;
    private static final int SLOT_SUBTRACT_1 = 3;
    private static final int SLOT_SUBTRACT_10 = 4;
    private static final int SLOT_ADD_10 = 5;
    private static final int SLOT_ADD_1 = 6;
    private static final int SLOT_CANCEL = 8;

    private MenuItem _menuCancel;
    private MenuItem _menuSubtract1;
    private MenuItem _menuSubtract10;
    private MenuItem _menuAdd1;
    private MenuItem _menuAdd10;

    public enum QuantityMetaKey {
        /**
         * The ItemStack instance to get a quantity for.
         * Required.
         */
        ITEMSTACK,
        
        /**
         * The initial quantity
         * Default is 1.
         */
        QTY,
        
        /**
         * The maximum quantity that can be set.
         * Default is 64.
         * 64 is absolute maximum.
         */
        MAX_QTY,
        
        /**
         * Price per unit.
         * Not used if not set.
         */
        PRICE
    }


    @Override
    protected void onInit () {

        _menuCancel = new MenuItem(SLOT_CANCEL, "menuCancel", this);
        _menuCancel.setItemStack(new ItemStack(Material.REDSTONE_BLOCK));
        _menuCancel.setTitle(ChatColor.RED + "Cancel");
        _menuCancel.setDescription(ChatColor.RED + "Click to cancel and return.");

        _menuSubtract1 = new MenuItem(SLOT_SUBTRACT_1, "menuSubtract1", this);
        _menuSubtract1.setItemStack(new ItemStack(Material.STONE));
        _menuSubtract1.setTitle(ChatColor.RED + "Subtract 1");
        _menuSubtract1.setDescription("Click to Subtract 1 item from the quantity.");
        _menuSubtract1.setMeta(META_QTY_INCREMENT, -1);

        _menuSubtract10 = new MenuItem(SLOT_SUBTRACT_10, "menuSubtract10", this);
        _menuSubtract10.setItemStack(new ItemStack(Material.DIRT));
        _menuSubtract10.setTitle(ChatColor.RED + "Subtract 10");
        _menuSubtract10.setDescription("Click to Subtract 10 items from the quantity.");
        _menuSubtract10.setMeta(META_QTY_INCREMENT, -10);

        _menuAdd1 = new MenuItem(SLOT_SUBTRACT_1, "menuAdd1", this);
        _menuAdd1.setItemStack(new ItemStack(Material.IRON_BLOCK));
        _menuAdd1.setTitle(ChatColor.GREEN + "Add 1");
        _menuAdd1.setDescription("Click to Add 1 item to the quantity.");
        _menuAdd1.setMeta(META_QTY_INCREMENT, 1);

        _menuAdd10 = new MenuItem(SLOT_ADD_10, "menuAdd10", this);
        _menuAdd10.setItemStack(new ItemStack(Material.GOLD_BLOCK));
        _menuAdd10.setTitle(ChatColor.GREEN + "Add 10");
        _menuAdd10.setDescription("Click to Add 10 item to the quantity.");
        _menuAdd10.setMeta(META_QTY_INCREMENT, 10);
    }


    @Override
    protected void buildInventory () {

        // do nothing
    }


    @Override
    protected ViewInstance onCreateInstance (Player p, ViewInstance previous,
                                             ViewMeta persistantMeta, ViewMeta meta) {

        QuantityViewInstance instance = new QuantityViewInstance(this, previous, p, persistantMeta,
                meta);
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
    public class QuantityViewInstance extends AbstractMenuInstance {

        private double _price = 0.0D;
        private int _maxQty = 64;
        private MenuItem _itemToQuantify;
        private Inventory _inventory;
        private QuantityViewResult _result;
        
        
        public QuantityViewInstance(IView view, ViewInstance previous, Player p,
                                    ViewMeta persistantMeta, ViewMeta initialMeta) {

            super(view, previous, p, persistantMeta, initialMeta);
        }


        public ViewResult getResult () {

            return _result;
        }


        @Override
        protected InventoryView onShow (ViewMeta instanceMeta) {
            PreCon.notNull(instanceMeta);

            ViewTaskMode taskMode = getSessionMeta().getMeta(SessionMetaKey.TASK_MODE);
            setTitle(taskMode.getChatColor() + "Select Quantity");

            ItemStack itemStack = instanceMeta.getMeta(QuantityMetaKey.ITEMSTACK);
            if (itemStack == null)
                throw new IllegalStateException("QuantityViewMeta.ITEMSTACK instance meta cannot be null.");

            Integer maxQty = instanceMeta.getMeta(QuantityMetaKey.MAX_QTY);
            _maxQty = maxQty != null ? maxQty : 64;
            
            Integer qty = instanceMeta.getMeta(QuantityMetaKey.QTY);
            if (qty == null) {
                qty = 1;
            }
            
            Double price = instanceMeta.getMeta(QuantityMetaKey.PRICE);
            if (price != null) {
                _price = price;
            }
            
            _result = new QuantityViewResult(this, itemStack, qty);
            _result.setIsCancelled(true);

            _itemToQuantify = new MenuItem(SLOT_ITEM, "itemToQuantify", (AbstractMenuView) getView());
            _itemToQuantify.setItemStack(itemStack.clone());
            _itemToQuantify.getItemStack().setAmount(qty);
            
            buildInventory();

            return getPlayer().openInventory(_inventory);
        }


        @Override
        protected InventoryView onShowAsPrev (ViewMeta instanceMeta, ViewResult meta) {

            return onShow(instanceMeta);
        }


        @Override
        protected MenuItem getMenuItem (int slot) {

            switch (slot) {
                case 0:
                    return _itemToQuantify;
                case SLOT_SUBTRACT_1:
                    return _menuSubtract1;
                case SLOT_SUBTRACT_10:
                    return _menuSubtract10;
                case SLOT_ADD_1:
                    return _menuAdd1;
                case SLOT_ADD_10:
                    return _menuAdd10;
                case SLOT_CANCEL:
                    return _menuCancel;
                default:
                    return null;
            }
        }


        @Override
        protected void onItemSelect (MenuItem menuItem) {

            if (menuItem == _itemToQuantify) {
                _result.setIsCancelled(false);
                getPlayer().closeInventory();
                return;
            }
            else if (menuItem == _menuCancel) {
                _result.setIsCancelled(true);
                getPlayer().closeInventory();
                return;
            }

            Integer increment = menuItem.getMeta(META_QTY_INCREMENT);
            if (increment == null)
                return;

            int qty = Math.max(1, _result.getQty() + increment);
            int max = Math.min(64, _maxQty);
            qty = Math.min(qty, max);

            _result.setQty(qty);

            ItemStack item = _inventory.getItem(0);

            item.setAmount(qty);
            
            setLore(item);
            
            updateItemVisibility();
        }
        
        
        @Override
        protected void onClose (ViewCloseReason reason) {

            // do nothing
        }
        
        private void updateItemVisibility() {
            
            ItemStack item = _inventory.getItem(0);
            
            int qty = item.getAmount();
            
            setItemVisible(SLOT_SUBTRACT_1, _inventory, qty > 1);
            setItemVisible(SLOT_SUBTRACT_10, _inventory, qty > 1);
            
            setItemVisible(SLOT_ADD_1, _inventory, qty < _maxQty);
            setItemVisible(SLOT_ADD_10, _inventory, qty < _maxQty);
        }
       

        private void buildInventory () {

            _inventory = Bukkit.createInventory(getPlayer(), 9, getTitle());

            ItemStack item = _itemToQuantify.getItemStack();
            item.setAmount(_result.getQty());
            
            setLore(item);
            
            _inventory.setItem(0, item);
            
            updateItemVisibility();
            
            _inventory.setItem(SLOT_CANCEL, _menuCancel.getItemStack());

        }
        
        private void setLore(ItemStack itemStack) {
            
            ItemStackUtil.removeTempLore(itemStack);
            
            if (_price > 0)
                ItemStackUtil.setPriceLore(itemStack, _price * itemStack.getAmount(), PriceType.TOTAL);
            
            ItemStackUtil.addTempLore(itemStack, ChatColor.YELLOW + "Qty: " + ChatColor.GRAY
                    + itemStack.getAmount());
            
            ItemStackUtil.addTempLore(itemStack, ChatColor.YELLOW + "Available: " + ChatColor.GRAY
                    + _maxQty);
            
            ViewTaskMode taskMode = getSessionMeta().getMeta(SessionMetaKey.TASK_MODE);

            if (taskMode == ViewTaskMode.SERVER_BUY || taskMode == ViewTaskMode.PLAYER_BUY)
                ItemStackUtil.addTempLore(itemStack, ChatColor.BLUE + "Click to purchase.");
            else
                ItemStackUtil.addTempLore(itemStack, ChatColor.BLUE + "Click to confirm.");
        }

        public class QuantityViewResult extends ViewResult {

            QuantityViewResult(ViewInstance parent, ItemStack itemStack, int qty) {

                super(parent);
                setMeta("itemStack", itemStack);
                setMeta("qty", qty);
            }

            public ItemStack getItemStack () {

                return getMeta("itemStack");
            }


            public int getQty () {

                Integer integer = getMeta("qty");
                if (integer == null)
                    return 1;

                return integer;
            }


            void setQty (int qty) {

                setMeta("qty", qty);
            }

        }

    }

}
