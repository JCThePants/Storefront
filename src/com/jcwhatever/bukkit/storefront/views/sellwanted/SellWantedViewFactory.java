package com.jcwhatever.bukkit.storefront.views.sellwanted;

import com.jcwhatever.bukkit.generic.views.IView;
import com.jcwhatever.bukkit.generic.views.IViewSession;
import com.jcwhatever.bukkit.generic.views.ViewFactory;
import com.jcwhatever.bukkit.generic.views.data.ViewArguments;
import com.jcwhatever.bukkit.generic.views.data.ViewOpenReason;

import javax.annotation.Nullable;

/*
 * 
 */
public class SellWantedViewFactory extends ViewFactory<SellWantedView> {

    public SellWantedViewFactory(String name) {
        super(name, SellWantedView.class);
    }

    @Override
    protected boolean onOpen(ViewOpenReason reason, SellWantedView view) {
        view.show(reason);
        return true;
    }

    @Override
    protected void onDispose() {

    }

    @Override
    public IView create(@Nullable String title, IViewSession session, ViewArguments arguments) {
        return new SellWantedView(session, this, arguments);
    }
}
