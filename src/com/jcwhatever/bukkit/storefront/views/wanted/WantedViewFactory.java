package com.jcwhatever.bukkit.storefront.views.wanted;

import com.jcwhatever.bukkit.generic.views.IView;
import com.jcwhatever.bukkit.generic.views.IViewSession;
import com.jcwhatever.bukkit.generic.views.ViewFactory;
import com.jcwhatever.bukkit.generic.views.data.ViewArguments;
import com.jcwhatever.bukkit.generic.views.data.ViewOpenReason;

import javax.annotation.Nullable;

/*
 * 
 */
public class WantedViewFactory extends ViewFactory<WantedView> {

    public WantedViewFactory(String name) {
        super(name, WantedView.class);
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
    public IView create(@Nullable String title, IViewSession session, ViewArguments arguments) {
        return new WantedView(session, this, arguments);
    }
}
