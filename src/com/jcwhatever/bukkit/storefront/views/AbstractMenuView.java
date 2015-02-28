package com.jcwhatever.bukkit.storefront.views;

import com.jcwhatever.bukkit.storefront.Category;
import com.jcwhatever.bukkit.storefront.Storefront;
import com.jcwhatever.bukkit.storefront.meta.SessionMetaKey;
import com.jcwhatever.bukkit.storefront.meta.ViewSessionTask;
import com.jcwhatever.bukkit.storefront.stores.IStore;
import com.jcwhatever.bukkit.storefront.utils.StoreStackMatcher;
import com.jcwhatever.nucleus.views.menu.MenuView;

/**
 * Abstract implementation of {@link com.jcwhatever.nucleus.views.menu.MenuView}.
 *
 * <p>Provides protected utility methods to all Storefront menu views.</p>
 */
public abstract class AbstractMenuView extends MenuView {

    /**
     * Constructor.
     */
    protected AbstractMenuView() {
        super(Storefront.getPlugin(), StoreStackMatcher.getDefault());
    }

    /**
     * Get the {@link IStore} for the current view session.
     */
    protected IStore getStore() {
        IStore store = getViewSession().getMeta(SessionMetaKey.STORE);
        if (store == null)
            throw new RuntimeException("STORE session meta key not set.");

        return store;
    }

    /**
     * Get the {@link ViewSessionTask} for the current view session.
     */
    protected ViewSessionTask getSessionTask() {
        ViewSessionTask taskMode = getViewSession().getMeta(SessionMetaKey.TASK_MODE);
        if (taskMode == null)
            throw new RuntimeException("TASK_MODE session meta key not set.");

        return taskMode;
    }

    /**
     * Get the {@link Category} for the current view session.
     */
    protected Category getCategory() {
        Category category = getViewSession().getMeta(SessionMetaKey.CATEGORY);
        if (category == null)
            throw new RuntimeException("CATEGORY session meta key not set.");

        return category;
    }

    /**
     * Determine if the current view session has a {@link Category}.
     */
    protected boolean hasCategory() {
        Category category = getViewSession().getMeta(SessionMetaKey.CATEGORY);
        return category != null;
    }
}
