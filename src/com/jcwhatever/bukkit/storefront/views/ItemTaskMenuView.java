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
import com.jcwhatever.bukkit.storefront.Storefront;
import com.jcwhatever.bukkit.storefront.meta.SessionMetaKey;
import com.jcwhatever.bukkit.storefront.meta.ViewTaskMode;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil.PriceType;
import com.jcwhatever.bukkit.storefront.views.PriceView.PriceViewInstance.PriceViewResult;
import com.jcwhatever.bukkit.storefront.views.PriceView.PriceViewMeta;
import com.jcwhatever.bukkit.storefront.views.QuantityView.QuantityViewInstance.QuantityViewResult;
import com.jcwhatever.bukkit.storefront.views.QuantityView.QuantityMetaKey;

public class ItemTaskMenuView extends AbstractMenuView {

    private static final int SLOT_ITEM = 0;
    private static final int SLOT_QTY = 3;
    private static final int SLOT_PRICE = 4;
    private static final int SLOT_REMOVE = 7;
    private static final int SLOT_CANCEL = 8;

    private MenuItem _qtyMenuItem;
    private MenuItem _priceMenuItem;
    private MenuItem _removeMenuItem;
    private MenuItem _cancelMenuItem;

    public enum ItemTaskMenuMeta {
        /**
         * The ItemStack to get quantity and price for.
         * Required.
         */
        ITEMSTACK,

        /**
         * Initial quantity to use.
         * Default is 1.
         */
        QTY,

        /**
         * Max quantity that can be set.
         * Default is 64.
         * Absolute max is 64.
         */
        MAX_QTY,

        /**
         * Initial price to use.
         * Default is 1.00
         */
        PRICE
    }


    @Override
    protected void onInit () {

        _qtyMenuItem = new MenuItem(SLOT_QTY, "__qty__", this);
        _qtyMenuItem.setItemStack(new ItemStack(Material.CHEST));
        _qtyMenuItem.setTitle(ChatColor.LIGHT_PURPLE + "QUANTITY");
        _qtyMenuItem.setDescription("Set the number of items in the stack.");

        _priceMenuItem = new MenuItem(SLOT_PRICE, "__price__", this);
        _priceMenuItem.setItemStack(new ItemStack(Material.GOLD_NUGGET));
        _priceMenuItem.setTitle(ChatColor.YELLOW + "PRICE");
        _priceMenuItem.setDescription("Set the price per item in the stack.");

        _removeMenuItem = new MenuItem(SLOT_REMOVE, "__remove__", this);
        _removeMenuItem.setItemStack(new ItemStack(Material.SHEARS));
        _removeMenuItem.setTitle(ChatColor.DARK_RED + "REMOVE");
        _removeMenuItem.setDescription(ChatColor.RED + "Click to remove item.");
        
        _cancelMenuItem = new MenuItem(SLOT_CANCEL, "__cancel__", this);
        _cancelMenuItem.setItemStack(new ItemStack(Material.REDSTONE_BLOCK));
        _cancelMenuItem.setTitle(ChatColor.RED + "CANCEL");
        _cancelMenuItem.setDescription(ChatColor.RED + "Click to cancel changes and return.");
        
    }


    @Override
    protected void buildInventory () {

        // do nothing

    }


    @Override
    protected ViewInstance onCreateInstance (Player p, ViewInstance previous, ViewMeta sessionMeta,
                                             ViewMeta instanceMeta) {

        ItemTaskMenuInstance instance = new ItemTaskMenuInstance(this, previous, p, sessionMeta,
                instanceMeta);
        return instance;
    }


    @Override
    protected void onLoadSettings (IDataNode dataNode) {

        // do nothing

    }

    public class ItemTaskMenuInstance extends AbstractMenuInstance {

        private Inventory _inventory;
        private ItemTaskMenuResult _result;
        private ItemStack _itemStack;
        private MenuItem _menuItem;
        private int _maxQty;
        private int _qty;
        private double _price;


        public ItemTaskMenuInstance(IView view, ViewInstance previous, Player p,
                                    ViewMeta sessionMeta, ViewMeta instanceMeta) {

            super(view, previous, p, sessionMeta, instanceMeta);
        }


        @Override
        protected InventoryView onShow (ViewMeta instanceMeta) {

            PreCon.notNull(instanceMeta);

            if (_inventory == null) {
                
                ViewTaskMode taskMode = getSessionMeta().getMeta(SessionMetaKey.TASK_MODE);
                setTitle(taskMode.getChatColor() + "Select Item Task");
                
                _inventory = Bukkit.createInventory(getPlayer(), 9, getTitle());

                _inventory.setItem(SLOT_QTY, _qtyMenuItem.getItemStack());
                _inventory.setItem(SLOT_PRICE, _priceMenuItem.getItemStack());
                _inventory.setItem(SLOT_REMOVE, _removeMenuItem.getItemStack());
                _inventory.setItem(SLOT_CANCEL, _cancelMenuItem.getItemStack());

                _itemStack = instanceMeta.getMeta(ItemTaskMenuMeta.ITEMSTACK);
                if (_itemStack == null)
                    throw new IllegalStateException(
                            "ItemTaskMenuMeta.ITEM_STACK instance meta is required.");

                Double price = instanceMeta.getMeta(ItemTaskMenuMeta.PRICE);
                _price = price != null
                        ? price
                        : 1.0D;

                Integer qty = instanceMeta.getMeta(ItemTaskMenuMeta.QTY);
                _qty = qty != null
                        ? qty
                        : 1;

                Integer maxQty = instanceMeta.getMeta(ItemTaskMenuMeta.MAX_QTY);
                _maxQty = maxQty != null
                        ? maxQty
                        : 64;

                // setup results
                _result = new ItemTaskMenuResult(this);
                _result.setQty(_qty);
                _result.setPrice(_price);
                
                // setup menu item
                ItemStack itemClone = _itemStack.clone();
                itemClone.setAmount(_qty);
                             
                _menuItem = new MenuItem(SLOT_ITEM, "__item__", (AbstractMenuView)getView());
                _menuItem.setItemStack(itemClone);
                
                setLore(itemClone);
                
                _inventory.setItem(SLOT_ITEM, itemClone);
            }

            return getPlayer().openInventory(_inventory);
        }


        @Override
        protected InventoryView onShowAsPrev (ViewMeta instanceMeta, ViewResult result) {

            if (result instanceof QuantityViewResult) {
                QuantityViewResult quantityResult = (QuantityViewResult)result;
                
                _qty = quantityResult.getQty();
                _result.setQty(_qty);
            }
            else if (result instanceof PriceViewResult) {
                PriceViewResult priceResult = (PriceViewResult)result;
                
                _price = priceResult.getPrice();
                _result.setPrice(_price);
            }
            
            _menuItem.getItemStack().setAmount(_qty);
           
            setLore(_menuItem.getItemStack());
            
            _inventory.setItem(SLOT_ITEM, _menuItem.getItemStack());
            
            return onShow(instanceMeta);
        }


        @Override
        protected void onClose (ViewCloseReason reason) {

            // do nothing
        }


        @Override
        protected MenuItem getMenuItem (int slot) {

            switch (slot) {
                case SLOT_ITEM:
                    return _menuItem;
                case SLOT_QTY:
                    return _qtyMenuItem;
                case SLOT_PRICE:
                    return _priceMenuItem;
                case SLOT_REMOVE:
                    return _removeMenuItem;
                case SLOT_CANCEL:
                    return _cancelMenuItem;
                default:
                    return null;
            }
        }


        @Override
        protected void onItemSelect (MenuItem menuItem) {

            if (menuItem == _qtyMenuItem) {
                showQtyView();
            }
            else if (menuItem == _priceMenuItem) {
                showPriceView();
            }
            else if (menuItem == _removeMenuItem) {
                _result.setQty(0);
                _result.setIsCancelled(false);
                getPlayer().closeInventory();
            }
            else if (menuItem == _cancelMenuItem) {
                _result.setIsCancelled(true);
                getPlayer().closeInventory();
            }
            else {
                _result.setIsCancelled(false);
                getPlayer().closeInventory();
            }
        }


        @Override
        public ViewResult getResult () {

            return _result;
        }


        private void showQtyView () {

            ViewMeta instanceMeta = new ViewMeta()
            .setMeta(QuantityMetaKey.ITEMSTACK, _itemStack)
            .setMeta(QuantityMetaKey.MAX_QTY, _maxQty)
            .setMeta(QuantityMetaKey.QTY, _qty)
            .setMeta(QuantityMetaKey.PRICE, _price)
            ;

            getViewManager().show(getPlayer(), Storefront.VIEW_QUANTITY, getSourceBlock(), instanceMeta);
        }


        private void showPriceView () {

            ViewMeta instanceMeta = new ViewMeta()
            .setMeta(PriceViewMeta.ITEMSTACK, _itemStack)
            .setMeta(PriceViewMeta.PRICE, _price)
            ;

            getViewManager().show(getPlayer(), Storefront.VIEW_PRICE, getSourceBlock(), instanceMeta);
        }
        
        private void setLore(ItemStack itemStack) {
            ItemStackUtil.removeTempLore(itemStack);
            
            if (_price > 0)
                ItemStackUtil.setPriceLore(itemStack, _price * itemStack.getAmount(), PriceType.TOTAL);
            
            ItemStackUtil.addTempLore(itemStack, ChatColor.YELLOW + "Available: " + ChatColor.GRAY
                    + (_maxQty - itemStack.getAmount()));
            
            ItemStackUtil.addTempLore(itemStack, ChatColor.BLUE + "Click to confirm changes.");
        }

        
        public class ItemTaskMenuResult extends ViewResult {

            public ItemTaskMenuResult(ViewInstance viewInstance) {

                super(viewInstance);
            }


            public Double getPrice () {

                return getMeta("__price__");
            }


            public int getQty () {

                return getMeta("__qty__");
            }


            void setPrice (double price) {

                setMeta("__price__", price);
            }


            void setQty (int qty) {

                setMeta("__qty__", qty);
            }

        }

    }

}
