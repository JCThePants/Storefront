package com.jcwhatever.bukkit.storefront.views;

import com.jcwhatever.generic.views.IViewFactory;
import com.jcwhatever.generic.views.ViewSession;
import com.jcwhatever.generic.views.data.ViewArgumentKey;
import com.jcwhatever.generic.views.data.ViewArguments;
import com.jcwhatever.generic.views.data.ViewArguments.ViewArgument;
import com.jcwhatever.generic.views.data.ViewResults;
import com.jcwhatever.generic.views.menu.MenuView;
import com.jcwhatever.generic.views.menu.PaginatorView;
import com.jcwhatever.bukkit.storefront.Category;
import com.jcwhatever.bukkit.storefront.CategoryManager;
import com.jcwhatever.bukkit.storefront.Storefront;
import com.jcwhatever.bukkit.storefront.data.ISaleItem;
import com.jcwhatever.bukkit.storefront.data.PaginatedItems;
import com.jcwhatever.bukkit.storefront.meta.GlobalArgumentKey;
import com.jcwhatever.bukkit.storefront.meta.SessionMetaKey;
import com.jcwhatever.bukkit.storefront.meta.ViewTaskMode;
import com.jcwhatever.bukkit.storefront.meta.ViewTaskMode.BasicTask;
import com.jcwhatever.bukkit.storefront.stores.IStore;
import com.jcwhatever.bukkit.storefront.utils.StoreStackComparer;
import com.jcwhatever.bukkit.storefront.views.category.CategoryView;
import com.jcwhatever.bukkit.storefront.views.itemtask.ItemTaskView;
import com.jcwhatever.bukkit.storefront.views.price.PriceView;
import com.jcwhatever.bukkit.storefront.views.quantity.QuantityView;

import org.bukkit.inventory.ItemStack;

import java.util.List;
import javax.annotation.Nullable;

/*
 * 
 */
public abstract class AbstractMenuView extends MenuView {

    private ViewResults _results;

    protected AbstractMenuView(ViewSession session,
                               IViewFactory factory, ViewArguments arguments) {
        super(null, session, factory, arguments, StoreStackComparer.getDefault());
    }

    @Nullable
    @Override
    public final ViewResults getResults() {
        return _results;
    }

    protected void setResults(ViewResults results) {
        _results = results;
    }

    protected IStore getStore() {
        IStore store = getViewSession().getMeta(SessionMetaKey.STORE);
        if (store == null)
            throw new RuntimeException("STORE session meta key not set.");

        return store;
    }

    protected ViewTaskMode getTaskMode() {
        ViewTaskMode taskMode = getViewSession().getMeta(SessionMetaKey.TASK_MODE);
        if (taskMode == null)
            throw new RuntimeException("TASK_MODE session meta key not set.");

        return taskMode;
    }

    protected Category getCategory() {
        Category category = getViewSession().getMeta(SessionMetaKey.CATEGORY);
        if (category == null)
            throw new RuntimeException("CATEGORY session meta key not set.");

        return category;
    }

    protected <T> T getRequiredArgument(ViewArguments arguments, ViewArgumentKey<T> key) {

        T result = arguments.get(key);
        if (result == null)
            throw new RuntimeException("Missing required view argument.");

        return result;
    }

    protected boolean hasCategory() {
        Category category = getViewSession().getMeta(SessionMetaKey.CATEGORY);
        return category != null;
    }

    protected void showQuantityView(ISaleItem saleItem) {
        showQuantityView(saleItem, 1, saleItem.getQty(),
                saleItem.getPricePerUnit(), new ViewArguments());
    }

    protected void showQuantityView(ISaleItem saleItem, ViewArguments merge) {
        showQuantityView(saleItem, 1, saleItem.getQty(),
                saleItem.getPricePerUnit(), merge);
    }

    protected void showQuantityView(ISaleItem saleItem, int initialQty) {
        showQuantityView(saleItem, initialQty, saleItem.getQty(),
                saleItem.getPricePerUnit(), new ViewArguments());
    }

    protected void showQuantityView(ISaleItem saleItem, int initialQty, ViewArguments merge) {
        showQuantityView(saleItem, initialQty, saleItem.getQty(),
                saleItem.getPricePerUnit(), merge);
    }

    protected void showQuantityView(ISaleItem saleItem, int initialQty,
                                    int maxQty, double pricePerUnit, ViewArguments merge) {

        ViewArguments qtyArgs = new ViewArguments(merge,
                new ViewArgument(QuantityView.SALE_ITEM, saleItem),
                new ViewArgument(QuantityView.MAX_QUANTITY, maxQty),
                new ViewArgument(QuantityView.INITIAL_QUANTITY, initialQty),
                new ViewArgument(QuantityView.PRICE, pricePerUnit)
        );

        getViewSession().next(Storefront.VIEW_QUANTITY, qtyArgs);
    }

    protected void showQuantityView(ItemStack itemStack, int initialQty,
                                    int maxQty, double pricePerUnit) {

        ViewArguments qtyArgs = new ViewArguments(
                new ViewArgument(QuantityView.ITEM_STACK, itemStack),
                new ViewArgument(QuantityView.MAX_QUANTITY, maxQty),
                new ViewArgument(QuantityView.INITIAL_QUANTITY, initialQty),
                new ViewArgument(QuantityView.PRICE, pricePerUnit)
        );

        getViewSession().next(Storefront.VIEW_QUANTITY, qtyArgs);
    }

    protected void showPriceView (ItemStack itemToPrice, double initialPrice) {

        ViewArguments arguments = new ViewArguments(
                new ViewArgument(PriceView.ITEM_STACK, itemToPrice),
                new ViewArgument(PriceView.INITIAL_PRICE, initialPrice));

        getViewSession().next(Storefront.VIEW_PRICE, arguments);
    }

    protected void showCategoryViewOrNext(IViewFactory nextFactory, PaginatedItems pagin, ViewArguments arguments) {

        ViewSession session = getViewSession();
        IStore store = session.getMeta(SessionMetaKey.STORE);
        if (store == null)
            throw new AssertionError();

        List<Category> categories = getCategories();

        int totalSlots = pagin.getTotalItems();

        if (totalSlots <= 6 * 9 * 3 || categories.size() <= 1) {

            ViewArguments paginArguments = new ViewArguments(arguments,
                    new ViewArgument(PaginatorView.PAGINATOR, pagin),
                    new ViewArgument(PaginatorView.NEXT_VIEW, nextFactory));

            showPaginViewOrNext(nextFactory, pagin, paginArguments);
            return;
        }

        // show categories
        session.next(Storefront.VIEW_CATEGORY, new ViewArguments(
                new ViewArgument(CategoryView.NEXT_VIEW, nextFactory)
        ));
    }

    protected void showPaginViewOrNext (IViewFactory nextView,
                             PaginatedItems pagin, ViewArguments nextArguments) {

        ViewSession session = getViewSession();

        IStore store = session.getMeta(SessionMetaKey.STORE);
        if (store == null)
            throw new AssertionError();

        if (pagin.getTotalPages() < 2) {

            ViewArguments arguments = new ViewArguments(nextArguments,
                    new ViewArgument(GlobalArgumentKey.SALE_ITEMS, pagin),
                    new ViewArgument(PaginatorView.SELECTED_PAGE, 1)
            );

            session.next(nextView, arguments);
        }
        else {

            ViewArguments paginArgs = new ViewArguments(
                    new ViewArgument(PaginatorView.PAGINATOR, pagin),
                    new ViewArgument(PaginatorView.NEXT_VIEW, nextView)
            );

            session.next(Storefront.VIEW_PAGINATOR, paginArgs);
        }

        /*
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
        */


    }

    protected List<Category> getCategories() {
        List<Category> categories;

        IStore store = getStore();
        ViewTaskMode mode = getTaskMode();

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

    protected void showItemTaskMenu(ItemStack itemStack, int initialQty, double initialPrice) {
        showItemTaskMenu(itemStack, initialQty, initialPrice, 64);
    }

    protected void showItemTaskMenu(ItemStack itemStack, int initialQty, double initialPrice, int maxQty) {

        ViewArguments arguments = new ViewArguments(
                new ViewArgument(ItemTaskView.ITEM_STACK, itemStack),
                new ViewArgument(ItemTaskView.MAX_QUANTITY, maxQty),
                new ViewArgument(ItemTaskView.INITIAL_QUANTITY, initialQty),
                new ViewArgument(ItemTaskView.INITIAL_PRICE, initialPrice));

        getViewSession().next(Storefront.VIEW_ITEM_TASK_MENU, arguments);
    }


}
