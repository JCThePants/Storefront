package com.jcwhatever.bukkit.storefront.views.mainmenu;

import com.jcwhatever.bukkit.generic.views.IViewFactory;
import com.jcwhatever.bukkit.generic.views.data.ViewArguments;
import com.jcwhatever.bukkit.generic.views.menu.MenuItem;
import com.jcwhatever.bukkit.storefront.data.ISaleItemGetter;
import com.jcwhatever.bukkit.storefront.data.PaginatedItems;
import com.jcwhatever.bukkit.storefront.meta.ViewTaskMode;
import com.sun.istack.internal.Nullable;

/*
 * 
 */
public class MainMenuItem extends MenuItem {

    private ViewTaskMode _taskMode;
    private IViewFactory _viewFactory;
    private PaginatedItems _saleItems;
    private ViewArguments _arguments;
    private boolean _isCategorized;

    public MainMenuItem(int slot) {
        super(slot);
    }

    public ViewTaskMode getTaskMode() {
        if (_taskMode == null)
            throw new RuntimeException("Task mode not set.");

        return _taskMode;
    }

    public void setTaskMode(ViewTaskMode viewTaskMode) {
        _taskMode = viewTaskMode;
    }

    public IViewFactory getViewFactory() {
        return _viewFactory;
    }

    public void setViewFactory(IViewFactory factory) {
        _viewFactory = factory;
    }

    @Nullable
    public PaginatedItems getSaleItems() {
        return _saleItems;
    }

    public void setSaleItems(ISaleItemGetter getter) {
        _saleItems = new PaginatedItems(getter);
    }

    public void setSaleItems(PaginatedItems items) {
        _saleItems = items;
    }

    public boolean isCategorized() {
        return _isCategorized;
    }

    public void setCategorized(boolean isCategorized) {
        _isCategorized = isCategorized;
    }

    public ViewArguments getArguments() {
        if (_arguments == null)
            return new ViewArguments();

        return _arguments;
    }

    public void setArguments(ViewArguments arguments) {
        _arguments = arguments;
    }


}
