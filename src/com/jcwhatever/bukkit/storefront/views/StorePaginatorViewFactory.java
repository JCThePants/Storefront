package com.jcwhatever.bukkit.storefront.views;

import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.views.IView;
import com.jcwhatever.nucleus.views.ViewFactory;
import com.jcwhatever.nucleus.views.ViewSession;
import com.jcwhatever.nucleus.views.data.ViewArguments;
import com.jcwhatever.nucleus.views.menu.PaginatorView;
import com.jcwhatever.bukkit.storefront.Storefront;
import com.jcwhatever.bukkit.storefront.utils.StoreStackComparer;

import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;

public class StorePaginatorViewFactory extends ViewFactory {

    public StorePaginatorViewFactory(String name) {
        super(null, name);
    }

    @Override
    public Plugin getPlugin() {
        return Storefront.getInstance();
    }

    @Override
    public IView create(@Nullable String title, ViewSession session, ViewArguments arguments) {
        PreCon.notNull(session);
        PreCon.notNull(arguments);

        return new PaginatorView(title, session, this, arguments, StoreStackComparer.getDefault());
    }

    @Override
    protected void onDispose() {
        // do nothing
    }
}
