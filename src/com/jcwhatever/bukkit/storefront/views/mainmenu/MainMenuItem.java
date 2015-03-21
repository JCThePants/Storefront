package com.jcwhatever.bukkit.storefront.views.mainmenu;

import com.jcwhatever.bukkit.storefront.data.ISaleItemGetter;
import com.jcwhatever.bukkit.storefront.data.PaginatedItems;
import com.jcwhatever.bukkit.storefront.meta.ViewSessionTask;
import com.jcwhatever.nucleus.views.menu.MenuItem;

import org.bukkit.inventory.ItemStack;

/**
 * Implementation of a {@link com.jcwhatever.nucleus.views.menu.MenuItem} designed
 * for Storefronts main menu.
 */
public class MainMenuItem extends MenuItem {

    private ViewSessionTask _taskMode;
    private boolean _isCategorized;
    private ISaleItemGetter _getter;

    /**
     * Constructor.
     *
     * @param slot       The inventory slot the item is for.
     * @param itemStack  The {@link org.bukkit.inventory.ItemStack} that is used in
     *                   the inventory slot.
     */
    public MainMenuItem(int slot, ItemStack itemStack) {
        super(slot, itemStack);
    }

    /**
     * Get the {@link ViewSessionTask} associated with the menu item.
     *
     * <p>When the menu item is clicked, the value becomes the current
     * task.</p>
     */
    public ViewSessionTask getTask() {
        if (_taskMode == null)
            throw new RuntimeException("Task mode not set.");

        return _taskMode;
    }

    /**
     * Set the {@link ViewSessionTask} associated with the menu item.
     *
     * @param task  The {@link ViewSessionTask}.
     */
    public void setTask(ViewSessionTask task) {
        _taskMode = task;
    }

    /**
     * Get the {@link PaginatedItems} that represent sale items associated
     * with the task for the current {@link org.bukkit.entity.Player} who is
     * viewing the menu.
     */
    public PaginatedItems getSaleItems() {
        return new PaginatedItems(_getter);
    }

    /**
     * Set the {@link ISaleItemGetter} used to get sale items to show.
     *
     * @param getter  The {@link ISaleItemGetter}.
     */
    public void setSaleItems(ISaleItemGetter getter) {
        _getter = getter;
    }

    /**
     * Determine if the view of the sale items are by category.
     */
    public boolean isCategorized() {
        return _isCategorized;
    }

    /**
     * Set the sale items viewed are by category (or not).
     *
     * @param isCategorized  True for categorized, otherwise false.
     */
    public void setCategorized(boolean isCategorized) {
        _isCategorized = isCategorized;
    }
}
