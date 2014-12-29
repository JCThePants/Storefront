package com.jcwhatever.bukkit.storefront.views.mainmenu;

import com.jcwhatever.bukkit.storefront.data.ISaleItemGetter;
import com.jcwhatever.bukkit.storefront.data.PaginatedItems;
import com.jcwhatever.bukkit.storefront.meta.ViewSessionTask;
import com.jcwhatever.nucleus.views.menu.MenuItem;

import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/*
 * 
 */
public class MainMenuItem extends MenuItem {

    private ViewSessionTask _taskMode;
    private boolean _isCategorized;
    private ISaleItemGetter _getter;

    public MainMenuItem(int slot, ItemStack itemStack,
                        @Nullable Map<Object, Object> meta,
                        @Nullable List<Runnable> onClick) {
        super(slot, itemStack, meta, onClick);
    }

    public ViewSessionTask getTask() {
        if (_taskMode == null)
            throw new RuntimeException("Task mode not set.");

        return _taskMode;
    }

    public void setTask(ViewSessionTask viewTaskMode) {
        _taskMode = viewTaskMode;
    }

    @Nullable
    public PaginatedItems getSaleItems() {
        return new PaginatedItems(_getter);
    }

    public void setSaleItems(ISaleItemGetter getter) {
        _getter = getter;
    }

    public boolean isCategorized() {
        return _isCategorized;
    }

    public void setCategorized(boolean isCategorized) {
        _isCategorized = isCategorized;
    }
}
