package com.jcwhatever.bukkit.storefront.views;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import com.jcwhatever.bukkit.generic.storage.IDataNode;
import com.jcwhatever.bukkit.generic.views.AbstractMenuInstance;
import com.jcwhatever.bukkit.generic.views.AbstractMenuView;
import com.jcwhatever.bukkit.generic.views.IView;
import com.jcwhatever.bukkit.generic.views.MenuItem;
import com.jcwhatever.bukkit.generic.views.ViewCloseReason;
import com.jcwhatever.bukkit.generic.views.ViewInstance;
import com.jcwhatever.bukkit.generic.views.ViewMeta;
import com.jcwhatever.bukkit.generic.views.ViewResult;
import com.jcwhatever.bukkit.storefront.Msg;
import com.jcwhatever.bukkit.storefront.StoreType;
import com.jcwhatever.bukkit.storefront.Storefront;
import com.jcwhatever.bukkit.storefront.data.PaginatedSaleItems;
import com.jcwhatever.bukkit.storefront.data.SaleItem;
import com.jcwhatever.bukkit.storefront.meta.SessionMetaKey;
import com.jcwhatever.bukkit.storefront.meta.ViewTaskMode;
import com.jcwhatever.bukkit.storefront.stores.IStore;

public class MainMenuView extends AbstractMenuView {

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
    protected ViewInstance onCreateInstance (Player p, ViewInstance previous,
                                             ViewMeta persistantMeta, ViewMeta meta) {

        MainMenuViewInstance instance = new MainMenuViewInstance(this, previous, p, persistantMeta,
                meta);
        return instance;
    }

    /**
     * 
     * @author JC The Pants
     *
     */
    public class MainMenuViewInstance extends AbstractMenuInstance {

        private IStore _store;
        private MenuItem _buyItem;
        private MenuItem _sellItem;


        public MainMenuViewInstance(IView view, ViewInstance previous, Player p,
                                    ViewMeta persistantMeta, ViewMeta initialMeta) {

            super(view, previous, p, persistantMeta, initialMeta);
        }


        @Override
        public ViewResult getResult () {

            return null;
        }


        @Override
        protected InventoryView onShow (ViewMeta meta) {

            _store = Storefront.getInstance().getStoreManager().getStore(getSourceBlock());
            if (_store == null)
                throw new IllegalStateException("Could not get store instance from source block.");

            // set persistent meta values
            getSessionMeta().setMeta(SessionMetaKey.STORE, _store);

            // set title
            setTitle("Store: " + _store.getTitle());

            // setup inventory
            Inventory inventory = Bukkit.createInventory(getPlayer(), 9, getTitle());

            boolean isPlayerManager = _store.getStoreType() == StoreType.PLAYER_OWNABLE
                    && getPlayer().getUniqueId().equals(_store.getOwnerId());

            _buyItem = new MenuItem(0, "buy", (AbstractMenuView) getView());
            _buyItem.setItemStack(new ItemStack(Material.CHEST));
            _buyItem.setDescription(isPlayerManager
                    ? "Click to manage items you're willing to buy."
                    : "Click to buy from the store.");
            _buyItem.setTitle(ChatColor.GREEN + (isPlayerManager
                    ? "WANTED"
                    : "BUY"));

            _sellItem = new MenuItem(1, "sell", (AbstractMenuView) getView());
            _sellItem.setItemStack(new ItemStack(Material.GOLD_BLOCK));
            _sellItem.setDescription(_store.hasOwner() 
                    ? "Click to sell items to the store."
                    : "Click to sell items from the store.");
            _sellItem.setTitle(ChatColor.BLUE + "SELL");

            inventory.setItem(0, _buyItem.getItemStack());
            
            boolean excludeSell = _store.getStoreType() == StoreType.PLAYER_OWNABLE &&
                                  !getPlayer().getUniqueId().equals(_store.getOwnerId()) &&
                                  _store.getWantedItems().getAll().size() == 0;
            
            if (!excludeSell)
                inventory.setItem(1, _sellItem.getItemStack());

            // show inventory to player
            return getPlayer().openInventory(inventory);
        }


        @Override
        protected InventoryView onShowAsPrev (ViewMeta instanceMeta, ViewResult meta) {

            return onShow(instanceMeta);
        }


        @Override
        protected MenuItem getMenuItem (int slot) {

            if (slot == 0)
                return _buyItem;

            if (slot == 1)
                return _sellItem;

            return null;
        }


        @Override
        protected void onItemSelect (MenuItem menuItem) {

            String viewName = null;
            ViewTaskMode taskMode = null;
            List<SaleItem> saleItems = null;
            int itemBuffer = 0;

            // player selected buy item
            if (menuItem == _buyItem) {
                
                switch (_store.getStoreType()) {

                    // server store buy
                    case SERVER:
                        
                        if (_store.getSaleItems().size() == 0) {
                            Msg.tell(getPlayer(), "Out of Stock");
                            return;
                        }
                        
                        viewName = Storefront.VIEW_CATEGORY;
                        taskMode = ViewTaskMode.SERVER_BUY;
                        break;

                    // player store buy
                    case PLAYER_OWNABLE:

                        // owner buy (wanted)
                        if (getPlayer().getUniqueId().equals(_store.getOwnerId())) {
                            taskMode = ViewTaskMode.OWNER_MANAGE_BUY;
                            viewName = Storefront.VIEW_WANTED;
                            saleItems = _store.getWantedItems().getAll();
                            itemBuffer = PaginatedSaleItems.MAX_PER_PAGE / 2;
                        }

                        // player buy
                        else {
                            viewName = Storefront.VIEW_CATEGORY;
                            taskMode = ViewTaskMode.PLAYER_BUY;
                        }
                        break;
                }
            }
            // player selected sell item
            else if (menuItem == _sellItem) {

                switch (_store.getStoreType()) {

                    // server store sell
                    case SERVER:
                        viewName = Storefront.VIEW_SELL;
                        taskMode = ViewTaskMode.SERVER_SELL;
                        break;

                    // player store sell
                    case PLAYER_OWNABLE:

                        // owner sell
                        if (getPlayer().getUniqueId().equals(_store.getOwnerId())) {
                            viewName = Storefront.VIEW_SELL;
                            taskMode = ViewTaskMode.OWNER_MANAGE_SELL;
                            saleItems = _store.getSaleItems();
                            itemBuffer = PaginatedSaleItems.MAX_PER_PAGE / 2;
                        }
                        // player sell
                        else {
                            viewName = Storefront.VIEW_CATEGORY;
                            taskMode = ViewTaskMode.PLAYER_SELL;
                        }
                        break;
                }
            }

            if (viewName == null)
                return;

            // set persistent task mode
            getSessionMeta().setMeta(SessionMetaKey.TASK_MODE, taskMode);

            // show next view
            if (saleItems != null)
                PaginatorView.showNextView(this, viewName, saleItems, menuItem, itemBuffer);
            else if (viewName.equals(Storefront.VIEW_CATEGORY))
                CategoryView.showNextView(this, _store, taskMode, menuItem);
            else
                getViewManager().show(getPlayer(), viewName, getSourceBlock(), menuItem);
        }


        @Override
        protected void onClose (ViewCloseReason reason) {

            // do nothing
        }

    }

}
