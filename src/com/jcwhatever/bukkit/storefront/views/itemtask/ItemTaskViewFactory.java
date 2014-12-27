package com.jcwhatever.bukkit.storefront.views.itemtask;

import com.jcwhatever.nucleus.views.IView;
import com.jcwhatever.nucleus.views.ViewFactory;
import com.jcwhatever.nucleus.views.ViewSession;
import com.jcwhatever.nucleus.views.data.ViewArguments;
import com.jcwhatever.bukkit.storefront.Storefront;

import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;

/*
 * 
 */
public class ItemTaskViewFactory extends ViewFactory {

    public ItemTaskViewFactory(String name) {
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

        return new ItemTaskView(session, this, arguments);
    }
}
