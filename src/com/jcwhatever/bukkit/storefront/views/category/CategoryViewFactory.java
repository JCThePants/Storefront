package com.jcwhatever.bukkit.storefront.views.category;

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
public class CategoryViewFactory extends ViewFactory {

    public CategoryViewFactory(String name) {
        super(null, name);
    }

    @Override
    public Plugin getPlugin() {
        return Storefront.getInstance();
    }

    @Override
    protected void onDispose() {
        // do nothing
    }

    @Override
    public IView create(@Nullable String title, ViewSession session, ViewArguments arguments) {
        return new CategoryView(session, this, arguments);
    }

}
