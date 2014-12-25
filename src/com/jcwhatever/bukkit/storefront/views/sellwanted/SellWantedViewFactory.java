package com.jcwhatever.bukkit.storefront.views.sellwanted;

import com.jcwhatever.generic.views.IView;
import com.jcwhatever.generic.views.ViewFactory;
import com.jcwhatever.generic.views.ViewSession;
import com.jcwhatever.generic.views.data.ViewArguments;
import com.jcwhatever.bukkit.storefront.Storefront;

import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;

/*
 * 
 */
public class SellWantedViewFactory extends ViewFactory {

    public SellWantedViewFactory(String name) {
        super(null, name);
    }

    @Override
    public Plugin getPlugin() {
        return Storefront.getInstance();
    }

    @Override
    protected void onDispose() {

    }

    @Override
    public IView create(@Nullable String title, ViewSession session, ViewArguments arguments) {
        return new SellWantedView(session, this, arguments);
    }
}
