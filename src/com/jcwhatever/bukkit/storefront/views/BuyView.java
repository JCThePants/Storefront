package com.jcwhatever.bukkit.storefront.views;

import com.jcwhatever.bukkit.generic.economy.EconomyHelper;
import com.jcwhatever.bukkit.generic.economy.EconomyHelper.CurrencyNoun;
import com.jcwhatever.bukkit.generic.inventory.InventoryHelper;
import com.jcwhatever.bukkit.generic.items.ItemStackHelper;
import com.jcwhatever.bukkit.generic.items.ItemStackHelper.DisplayNameResult;
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
import com.jcwhatever.bukkit.storefront.Category;
import com.jcwhatever.bukkit.storefront.Msg;
import com.jcwhatever.bukkit.storefront.Storefront;
import com.jcwhatever.bukkit.storefront.data.ISaleItem;
import com.jcwhatever.bukkit.storefront.data.PaginatedSaleItems;
import com.jcwhatever.bukkit.storefront.data.PaginatedSaleItems.PaginatorPageType;
import com.jcwhatever.bukkit.storefront.data.SaleItem;
import com.jcwhatever.bukkit.storefront.meta.SessionMetaKey;
import com.jcwhatever.bukkit.storefront.meta.ViewTaskMode;
import com.jcwhatever.bukkit.storefront.stores.IStore;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil.PriceType;
import com.jcwhatever.bukkit.storefront.views.PaginatorView.PaginatorMetaKey;
import com.jcwhatever.bukkit.storefront.views.QuantityView.QuantityMetaKey;
import com.jcwhatever.bukkit.storefront.views.QuantityView.QuantityViewInstance.QuantityViewResult;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class BuyView extends AbstractMenuView {

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

        BuyViewInstance instance = new BuyViewInstance(this, previous, p, persistantMeta, meta);
        return instance;
    }


    @Override
    protected void onLoadSettings (IDataNode dataNode) {

        // do nothing
    }

    public class BuyViewInstance extends AbstractMenuInstance {

        private SlotMap _slotMap = new SlotMap();
        private IStore _store;
        private ViewResult _result;
        private Category _category;


        public BuyViewInstance(IView view, ViewInstance previous, Player p,
                               ViewMeta persistantMeta, ViewMeta initialMeta) {

            super(view, previous, p, persistantMeta, initialMeta);
        }


        @Override
        public ViewResult getResult () {

            return _result;
        }


        @Override
        protected InventoryView onShow (ViewMeta meta) {

            PreCon.notNull(meta);

            
            ViewTaskMode taskMode = getSessionMeta().getMeta(SessionMetaKey.TASK_MODE);
            _store = getSessionMeta().getMeta(SessionMetaKey.STORE);
            _category = getSessionMeta().getMeta(SessionMetaKey.CATEGORY);
            _result = new ViewResult(this);

            Integer page = meta.getMeta(PaginatorMetaKey.SELECTED_PAGE);
            PaginatedSaleItems pagin = meta.getMeta(PaginatorMetaKey.PAGINATOR);

            if (page == null) {
                page = 1;
            }

            if (pagin == null) {
                List<SaleItem> saleItems = _store.getSaleItems(_category);
                pagin = new PaginatedSaleItems(saleItems);
            }

            int totalPages = pagin.getTotalPages(getPlayer(), PaginatorPageType.SALE_ITEM_STACK);
            if (totalPages < page)
                page = totalPages;
            
            
            
            setTitle(taskMode.getChatColor() + "Buy Items (Page " + page + ")");

            List<ISaleItem> saleItemStacks = pagin.getPage(page, getPlayer(), PaginatorPageType.SALE_ITEM_STACK);

            Inventory inventory = Bukkit.createInventory(getPlayer(), PaginatedSaleItems.MAX_PER_PAGE, getTitle());

            for (int i = 0; i < saleItemStacks.size(); i++) {

                ISaleItem item = saleItemStacks.get(i);

                MenuItem menuItem = new MenuItem(i, "stack" + i, (AbstractMenuView) getView());
                menuItem.setMeta("__menuItemStack__", item);

                _slotMap.put(i, menuItem);

                ItemStack stack = item.getItemStack();
                stack.setAmount(item.getQty());
                ItemStackUtil.setPriceLore(stack, item.getPricePerUnit(), PriceType.PER_ITEM);
                ItemStackUtil.setSellerLore(stack, item.getSellerId());

                inventory.setItem(i, stack);
            }

            return getPlayer().openInventory(inventory);
        }


        @Override
        protected InventoryView onShowAsPrev (ViewMeta instanceMeta, ViewResult result) {

            if (result != null && !result.isCancelled()) {

                if (result instanceof QuantityViewResult) {

                    QuantityViewResult qtyResult = (QuantityViewResult) result;
                    
                    ISaleItem saleItemStack = qtyResult.getInstanceMeta().getMeta("__saleItemStack__");
                    if (saleItemStack == null)
                        throw new IllegalStateException("__saleItemStack__ in QuantityResult's instance meta cannot be null.");

                    int quantity = qtyResult.getQty();
                    double amount = saleItemStack.getPricePerUnit() * quantity;

                    double balance = EconomyHelper.getBalance(getPlayer());

                    // check buyer balance
                    if (balance < amount) {
                        Msg.tell(getPlayer(), "{RED}Problem: {WHITE}You don't have enough {0}.",
                                EconomyHelper.getCurrencyName(CurrencyNoun.PLURAL));
                    }
                    // check buyer available inventory room
                    else if (!InventoryHelper.hasRoom(getPlayer().getInventory(), saleItemStack.getItemStack(), quantity)) {
                        Msg.tell(getPlayer(), "{RED}Problem: {WHITE}There isn't enough space in your inventory.");
                    }
                    // check item is available
                    else if (saleItemStack.getParent().getQty() < quantity) {
                        Msg.tell(getPlayer(), "{RED}Problem: {WHITE}Not enough inventory. Someone may have purchased the item already.");
                    }
                    // buy items
                    else if (!_store.buySaleItem(getPlayer(), saleItemStack, quantity, amount)) {
                        Msg.tell(getPlayer(), "{RED}Problem: {WHITE}Failed to buy items. They may have been purchased by someone else already.");
                    }
                    else {
                        Msg.tell(getPlayer(), "{GREEN}Sucess: {WHITE}Purchased {0} {1} for {2}.", quantity,
                                ItemStackHelper.getDisplayName(saleItemStack.getItemStack(), DisplayNameResult.REQUIRED),
                                EconomyHelper.formatAmount(amount));
                    }
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

            ISaleItem saleItemStack = menuItem.getMeta("__menuItemStack__");
            if (saleItemStack == null)
                return;

            ViewMeta meta = new ViewMeta()
            .setMeta(QuantityMetaKey.ITEMSTACK, saleItemStack.getItemStack())
            .setMeta(QuantityMetaKey.MAX_QTY, saleItemStack.getQty())
            .setMeta(QuantityMetaKey.QTY, 1)
            .setMeta(QuantityMetaKey.PRICE, saleItemStack.getPricePerUnit())
            .setMeta("__saleItemStack__", saleItemStack)
            ;

            getViewManager().show(getPlayer(), Storefront.VIEW_QUANTITY, getSourceBlock(), meta);
        }


        @Override
        protected void onClose (ViewCloseReason reason) {

            // TODO - Make sure below is useful
            
            if (reason == ViewCloseReason.GOING_BACK) {
                List<SaleItem> saleItems = _store.getSaleItems(_category);
                PaginatedSaleItems pagin = new PaginatedSaleItems(saleItems);

                _result.setMeta(PaginatorMetaKey.TOTAL_PAGES, pagin.getTotalPages(getPlayer(), PaginatorPageType.SALE_ITEM_STACK));
            }
        }

    }

}
