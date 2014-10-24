package com.jcwhatever.bukkit.storefront.views;

import com.jcwhatever.bukkit.generic.storage.IDataNode;
import com.jcwhatever.bukkit.generic.utils.PreCon;
import com.jcwhatever.bukkit.generic.views.AbstractMenuInstance;
import com.jcwhatever.bukkit.generic.views.AbstractMenuView;
import com.jcwhatever.bukkit.generic.views.IView;
import com.jcwhatever.bukkit.generic.views.MenuItem;
import com.jcwhatever.bukkit.generic.views.SlotMap;
import com.jcwhatever.bukkit.generic.views.ViewCloseReason;
import com.jcwhatever.bukkit.generic.views.ViewInstance;
import com.jcwhatever.bukkit.generic.views.ViewMeta;
import com.jcwhatever.bukkit.generic.views.ViewResult;
import com.jcwhatever.bukkit.storefront.StoreType;
import com.jcwhatever.bukkit.storefront.Storefront;
import com.jcwhatever.bukkit.storefront.data.IViewInfo;
import com.jcwhatever.bukkit.storefront.data.PaginatedSaleItems;
import com.jcwhatever.bukkit.storefront.data.PaginatedSaleItems.PaginatorPageType;
import com.jcwhatever.bukkit.storefront.data.SaleItem;
import com.jcwhatever.bukkit.storefront.meta.SessionMetaKey;
import com.jcwhatever.bukkit.storefront.meta.ViewTaskMode;
import com.jcwhatever.bukkit.storefront.stores.IStore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class PaginatorView extends AbstractMenuView {
    
    public enum PaginatorMetaKey {
        /**
         * The total number of pages to show in the paginator
         */
        TOTAL_PAGES,

        /**
         * The name of the view shown when a page is selected
         */
        NEXT_VIEW_NAME,

        /**
         * The ViewMeta object passed to the view shown when a page is selected
         */
        NEXT_VIEW_META,

        /**
         * Passed to next view, the integer number of the page selected
         */
        SELECTED_PAGE,

        /**
         * Passes the calculated PaginatedSaleItems instance to the next view
         */
        PAGINATOR,
        
        /**
         * Specifies the cached paginator page type
         */
        PAGINATOR_PAGE_TYPE

    }
    
    public static PaginatorPageType getPageType(String viewName) {
        
        IView view = Storefront.getInstance().getViewManager().getView(viewName);
        if (view == null)
            throw new NullPointerException("A view named " + viewName + " was not found.");
        
        IViewInfo viewInfo = view.getClass().getAnnotation(IViewInfo.class);
        
        return viewInfo != null 
            ? viewInfo.pageType() 
            : PaginatorPageType.SALE_ITEM_STACK; 
    }


    public static void showNextView (ViewInstance sender, String nextView, List<SaleItem> saleItems) {

        ViewMeta meta = new ViewMeta();
        showNextView(sender, nextView, saleItems, meta, 0);
    }


    public static void showNextView (ViewInstance sender, String nextView,
                                     List<SaleItem> saleItems, ViewMeta instanceMeta) {

        showNextView(sender, nextView, saleItems, instanceMeta, 0);
    }


    public static void showNextView (ViewInstance sender, String nextView,
                                     List<SaleItem> saleItems, ViewMeta instanceMeta, int itemBuffer) {

        PaginatedSaleItems pagin = new PaginatedSaleItems(saleItems, itemBuffer);
        IStore store = sender.getSessionMeta().getMeta(SessionMetaKey.STORE);

        instanceMeta.setMeta(PaginatorMetaKey.SELECTED_PAGE, 1);
        instanceMeta.setMeta(PaginatorMetaKey.PAGINATOR, pagin);
        
        PaginatorPageType pageType = getPageType(nextView); 

        Player seller = store.getStoreType() == StoreType.SERVER
                ? sender.getPlayer()
                : null;
            
        int totalPages = pagin.getTotalPages(seller, pageType);

        if (totalPages <= 1) {
            // show page view
            sender.getView().getViewManager().show(sender.getPlayer(), nextView, sender.getSourceBlock(), instanceMeta);
        }
        else {
            // show paginator view
            ViewMeta meta = new ViewMeta()
            .setMeta(PaginatorMetaKey.TOTAL_PAGES, totalPages)
                    .setMeta(PaginatorMetaKey.NEXT_VIEW_NAME, nextView)
                    .setMeta(PaginatorMetaKey.NEXT_VIEW_META, instanceMeta)
                    .setMeta(PaginatorMetaKey.PAGINATOR, pagin)
                    .setMeta(PaginatorMetaKey.PAGINATOR_PAGE_TYPE, pageType);
            ;

            sender.getView().getViewManager().show(sender.getPlayer(), Storefront.VIEW_PAGINATOR, sender.getSourceBlock(), meta);
        }
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

        ItemPaginatorInstance instance = new ItemPaginatorInstance(this, previous, p,
                persistantMeta, meta);

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
    public class ItemPaginatorInstance extends AbstractMenuInstance {

        private SlotMap _slotMap = new SlotMap();
        private String _nextViewName;
        private ViewMeta _nextViewMeta;
        private IStore _store;


        public ItemPaginatorInstance(IView view, ViewInstance previous, Player p,
                                     ViewMeta persistantMeta, ViewMeta initialMeta) {

            super(view, previous, p, persistantMeta, initialMeta);
        }


        @Override
        public ViewResult getResult () {

            return null;
        }


        @Override
        protected InventoryView onShow (ViewMeta meta) {

            PreCon.notNull(meta);

            ViewTaskMode taskMode = getSessionMeta().getMeta(SessionMetaKey.TASK_MODE);
            setTitle(taskMode.getChatColor() + "Select page");

            _store = getSessionMeta().getMeta(SessionMetaKey.STORE);

            // get total pages to display
            Integer totalPages = meta.getMeta(PaginatorMetaKey.TOTAL_PAGES);
            if (totalPages == null)
                throw new IllegalStateException("TOTAL_PAGES meta cannot be null.");

            // get name of the view to display when a page is selected
            _nextViewName = meta.getMeta(PaginatorMetaKey.NEXT_VIEW_NAME);
            if (_nextViewName == null)
                throw new IllegalStateException("NEXT_VIEW_NAME meta cannot be null.");

            // get the meta to be passed to the view displayed when a page is
            // selected
            _nextViewMeta = meta.getMeta(PaginatorMetaKey.NEXT_VIEW_META);

            // Add page menu items to inventory
            int rows = (int) Math.ceil((double) totalPages / 9);
            Inventory inventory = Bukkit.createInventory(getPlayer(), rows * 9, getTitle());

            for (int i = 1; i <= totalPages; i++) {
                MenuItem menuItem = new MenuItem(i - 1, "page" + i, (AbstractMenuView) getView());
                menuItem.setItemStack(new ItemStack(Material.PAPER));
                menuItem.setTitle("" + ChatColor.AQUA + ChatColor.ITALIC + "Page " + i);
                menuItem.setDescription("Click to view page " + i + " of the results.");
                menuItem.setMeta("__page__", i);

                _slotMap.put(i - 1, menuItem);

                inventory.addItem(menuItem.getItemStack());
            }

            // show inventory view to player
            return getPlayer().openInventory(inventory);
        }


        @Override
        protected InventoryView onShowAsPrev (ViewMeta instanceMeta, ViewResult result) {

            if (result != null) {
                // get total pages to display
                //Integer totalPages = result.getMeta(PaginatorMetaKey.TOTAL_PAGES);
                //if (totalPages != null)
                  //  instanceMeta.setMeta(PaginatorMetaKey.TOTAL_PAGES, totalPages);
            }
            if (result == null) {

                PaginatedSaleItems pagin = instanceMeta.getMeta(PaginatorMetaKey.PAGINATOR);
                if (pagin != null) {
                    
                    PaginatorPageType pageType = instanceMeta.getMeta(PaginatorMetaKey.PAGINATOR_PAGE_TYPE);
                    if (pageType == null)
                        throw new IllegalStateException("Could not find Paginator page type in instance meta.");

                    Player exclude = _store.getStoreType() == StoreType.SERVER
                            ? getPlayer()
                            : null;

                    instanceMeta.setMeta(PaginatorMetaKey.TOTAL_PAGES, pagin.getTotalPages(exclude, pageType));
                }
            }

            return onShow(instanceMeta);
        }


        @Override
        protected MenuItem getMenuItem (int slot) {

            return _slotMap.get(slot);
        }


        @Override
        protected void onItemSelect (MenuItem menuItem) {

            if (menuItem == null)
                return;

            Integer selectedPage = menuItem.getMeta("__page__");

            // show next View
            if (_nextViewMeta == null)
                _nextViewMeta = new ViewMeta();

            _nextViewMeta.setMeta(PaginatorMetaKey.SELECTED_PAGE, selectedPage);

            getViewManager().show(getPlayer(), _nextViewName, getSourceBlock(), _nextViewMeta);
        }


        @Override
        protected void onClose (ViewCloseReason reason) {

            // do nothing
        }

    }

}
