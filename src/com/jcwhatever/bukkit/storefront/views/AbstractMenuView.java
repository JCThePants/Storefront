package com.jcwhatever.bukkit.storefront.views;

import com.jcwhatever.bukkit.storefront.Category;
import com.jcwhatever.bukkit.storefront.Storefront;
import com.jcwhatever.bukkit.storefront.meta.SessionMetaKey;
import com.jcwhatever.bukkit.storefront.meta.ViewSessionTask;
import com.jcwhatever.bukkit.storefront.stores.IStore;
import com.jcwhatever.bukkit.storefront.utils.StoreStackComparer;
import com.jcwhatever.nucleus.views.menu.MenuView;

/*
 * 
 */
public abstract class AbstractMenuView extends MenuView {

    protected AbstractMenuView() {
        super(Storefront.getInstance(), StoreStackComparer.getDefault());
    }

    protected IStore getStore() {
        IStore store = getViewSession().getMeta(SessionMetaKey.STORE);
        if (store == null)
            throw new RuntimeException("STORE session meta key not set.");

        return store;
    }

    protected ViewSessionTask getSessionTask() {
        ViewSessionTask taskMode = getViewSession().getMeta(SessionMetaKey.TASK_MODE);
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

    protected boolean hasCategory() {
        Category category = getViewSession().getMeta(SessionMetaKey.CATEGORY);
        return category != null;
    }
}
