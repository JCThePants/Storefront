package com.jcwhatever.bukkit.storefront.views.wanted;

import com.jcwhatever.bukkit.generic.views.IView;
import com.jcwhatever.bukkit.generic.views.ViewFactory;
import com.jcwhatever.bukkit.generic.views.ViewSession;
import com.jcwhatever.bukkit.generic.views.data.ViewArguments;
import com.jcwhatever.bukkit.generic.views.data.ViewOpenReason;
import com.jcwhatever.bukkit.storefront.Storefront;

import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;

/*
 * 
 */
public class WantedViewFactory extends ViewFactory<WantedView> {

    public WantedViewFactory(String name) {
        super(null, name, WantedView.class);
    }

    @Override
    public Plugin getPlugin() {
        return Storefront.getInstance();
    }

    @Override
    protected boolean onOpen(ViewOpenReason reason, WantedView view) {
        view.show(reason);
        return true;
    }

    @Override
    protected void onDispose() {

    }

    @Override
    public IView create(@Nullable String title, ViewSession session, ViewArguments arguments) {
        return new WantedView(session, this, arguments);
    }
}
