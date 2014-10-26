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

public class PriceView extends AbstractMenuView {

    private static final int SLOT_ITEM = 0;
    private static final int SLOT_SUBTRACT_1 = 3;
    private static final int SLOT_SUBTRACT_10 = 4;
    private static final int SLOT_SUBTRACT_50 = 5;
    private static final int SLOT_ADD_50 = 6;
    private static final int SLOT_ADD_10 = 7;
    private static final int SLOT_ADD_1 = 8;

    private static final String META_PRICE_INCREMENT = "price";

    public enum PriceViewMeta {
        /**
         * Initial price of the item to set a price on
         */
        PRICE,

        /**
         * The item stack that represents the item
         * to be priced.
         */
        ITEMSTACK
    }

    @Override
    protected void onInit () {
        // do nothing
    }


    @Override
    protected void buildInventory () {

        // do nothing

    }


    @Override
    protected ViewInstance onCreateInstance (Player p, ViewInstance previous,
                                             ViewMeta persistantMeta, ViewMeta meta) {

        PriceViewInstance instance = new PriceViewInstance(this, previous, p, persistantMeta, meta);
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
    public class PriceViewInstance extends AbstractMenuInstance {

        private MenuItem _itemToPrice;
        private double _price = 1.0D;
        private Inventory _inventory;

        private PriceViewResult _result;
        
        private MenuItem _minus1;
        private MenuItem _minus10;
        private MenuItem _minus50;
        private MenuItem _add50;
        private MenuItem _add10;
        private MenuItem _add1;



        public PriceViewInstance(IView view, ViewInstance previous, Player p,
                                 ViewMeta persistantMeta, ViewMeta initialMeta) {

            super(view, previous, p, persistantMeta, initialMeta);
            
            AbstractMenuView menuView = (AbstractMenuView)getView();
            
            // -1 btn
            _minus1 = new MenuItem(SLOT_SUBTRACT_1, "minus1", menuView);
            _minus1.setItemStack(new ItemStack(Material.STONE));
            _minus1.setTitle(ChatColor.RED + "Subtract 1");
            _minus1.setDescription("Subtract 1.00 from the price of the item.");
            _minus1.setMeta(META_PRICE_INCREMENT, -1.0D);

            // -10 btn
            _minus10 = new MenuItem(SLOT_SUBTRACT_10, "minus10", menuView);
            _minus10.setItemStack(new ItemStack(Material.DIRT));
            _minus10.setTitle(ChatColor.RED + "Subtract 10");
            _minus10.setDescription("Subtract 10.00 from the price of the item.");
            _minus10.setMeta(META_PRICE_INCREMENT, -10.0D);

            // -50 btn
            _minus50 = new MenuItem(SLOT_SUBTRACT_50, "minus50", menuView);
            _minus50.setItemStack(new ItemStack(Material.GRASS));
            _minus50.setTitle(ChatColor.RED + "Subtract 50");
            _minus50.setDescription("Subtract 50.00 from the price of the item.");
            _minus50.setMeta(META_PRICE_INCREMENT, -50.0D);

            // +50 btn
            _add50 = new MenuItem(SLOT_ADD_50, "add50", menuView);
            _add50.setItemStack(new ItemStack(Material.DIAMOND_BLOCK));
            _add50.setTitle(ChatColor.GREEN + "Add 50");
            _add50.setDescription("Add 50.00 to the price of the item.");
            _add50.setMeta(META_PRICE_INCREMENT, 50.0D);

            // +10 btn
            _add10 = new MenuItem(SLOT_ADD_10, "add10", menuView);
            _add10.setItemStack(new ItemStack(Material.GOLD_BLOCK));
            _add10.setTitle(ChatColor.GREEN + "Add 10");
            _add10.setDescription("Add 10.00 to the price of the item.");
            _add10.setMeta(META_PRICE_INCREMENT, 10.0D);

            // +1 btn
            _add1 = new MenuItem(SLOT_ADD_1, "add1", menuView);
            _add1.setItemStack(new ItemStack(Material.IRON_BLOCK));
            _add1.setTitle(ChatColor.GREEN + "Add 1");
            _add1.setDescription("Add 1.00 to the price of the item.");
            _add1.setMeta(META_PRICE_INCREMENT, 1.0D);

        }


        @Override
        public ViewResult getResult () {

            return _result;
        }


        @Override
        protected InventoryView onShow (ViewMeta instanceMeta) {

            PreCon.notNull(instanceMeta);

            ViewTaskMode taskMode = getSessionMeta().getMeta(SessionMetaKey.TASK_MODE);
            setTitle(taskMode.getChatColor() + "Set price per item");

            ItemStack itemToPrice = instanceMeta.getMeta(PriceViewMeta.ITEMSTACK);
            if (itemToPrice == null)
                throw new IllegalStateException("ITEMSTACK PriceViewMeta meta cannot be null.");

            Double price = instanceMeta.getMeta(PriceViewMeta.PRICE);
            if (price != null)
                _price = price;

            _itemToPrice = new MenuItem(SLOT_ITEM, "itemToPrice", (AbstractMenuView) getView());
            _itemToPrice.setItemStack(itemToPrice.clone());
            _itemToPrice.getItemStack().setAmount(1);

            _result = new PriceViewResult(this, itemToPrice, _price);
            _result.setIsCancelled(true);

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
                    _result.setIsCancelled(false);
                    getPlayer().closeInventory();
                    return null;
                case SLOT_SUBTRACT_1:
                    return _minus1;
                case SLOT_SUBTRACT_10:
                    return _minus10;
                case SLOT_SUBTRACT_50:
                    return _minus50;
                case SLOT_ADD_50:
                    return _add50;
                case SLOT_ADD_10:
                    return _add10;
                case SLOT_ADD_1:
                    return _add1;
                default:
                    return null;
            }
        }


        @Override
        protected void onItemSelect (MenuItem menuItem) {

            Double increment = menuItem.getMeta(META_PRICE_INCREMENT);
            if (increment == null)
                return;

            incrementPrice(increment);
            updateItemVisibility();
        }


        @Override
        protected void onClose (ViewCloseReason reason) {

            // do nothing
        }


        private void incrementPrice (double increment) {

            _price += increment;

            // prevent values below 1.00
            _price = Math.max(1.0D, _price);

            setLore(_itemToPrice.getItemStack());
            
            _inventory.setItem(0, _itemToPrice.getItemStack());

            _result.setPrice(_price);

            ItemStackUtil.setPriceLore(_add1.getItemStack(), _price, PriceType.PER_ITEM);
            ItemStackUtil.setPriceLore(_add10.getItemStack(), _price, PriceType.PER_ITEM);
            ItemStackUtil.setPriceLore(_add50.getItemStack(), _price, PriceType.PER_ITEM);
            ItemStackUtil.setPriceLore(_minus1.getItemStack(), _price, PriceType.PER_ITEM);
            ItemStackUtil.setPriceLore(_minus10.getItemStack(), _price, PriceType.PER_ITEM);
            ItemStackUtil.setPriceLore(_minus50.getItemStack(), _price, PriceType.PER_ITEM);
            
        }

        
        private void updateItemVisibility() {
            
            if (!setItemVisible(SLOT_SUBTRACT_1, _inventory, _price > 1)) {
                
                if (!isSlotEmpty(SLOT_SUBTRACT_1, _inventory)) {
                    _inventory.setItem(SLOT_SUBTRACT_1, _minus1.getItemStack());
                }
            }
        
            if (!setItemVisible(SLOT_SUBTRACT_10, _inventory, _price > 1)) {

                if (!isSlotEmpty(SLOT_SUBTRACT_10, _inventory)) {
                    _inventory.setItem(SLOT_SUBTRACT_10, _minus10.getItemStack());
                }
            }
            
            if (!setItemVisible(SLOT_SUBTRACT_50, _inventory, _price > 1)) {
                
                if (!isSlotEmpty(SLOT_SUBTRACT_50, _inventory)) {
                    _inventory.setItem(SLOT_SUBTRACT_50, _minus50.getItemStack());
                }
            }
            
            _inventory.setItem(SLOT_ADD_50, _add50.getItemStack());
            _inventory.setItem(SLOT_ADD_10, _add10.getItemStack());
            _inventory.setItem(SLOT_ADD_1, _add1.getItemStack());
        }

        private void buildInventory () {

            _inventory = Bukkit.createInventory(getPlayer(), 9, getTitle());

            _inventory.setItem(0, _itemToPrice.getItemStack());
            
            // update price on "buttons"
            incrementPrice(0);
            
            updateItemVisibility();

        }
        
        private void setLore(ItemStack itemStack) {
            ItemStackUtil.removeTempLore(itemStack);
            ItemStackUtil.setPriceLore(itemStack, _price, PriceType.PER_ITEM);
            ItemStackUtil.addTempLore(itemStack, ChatColor.GREEN + "Click to confirm changes.");
        }

        public class PriceViewResult extends ViewResult {

            PriceViewResult(ViewInstance parent, ItemStack itemStack, double price) {

                super(parent);

                setMeta("itemStack", itemStack);
                setMeta("price", price);
            }


            void setPrice (double price) {

                setMeta("price", price);
            }


            public double getPrice () {

                return getMeta("price");
            }


            public ItemStack getItemStack () {

                return getMeta("itemStack");
            }
        }

    }

}
