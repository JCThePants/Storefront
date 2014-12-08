package com.jcwhatever.bukkit.storefront.views.price;

import com.jcwhatever.bukkit.generic.views.IView;
import com.jcwhatever.bukkit.generic.views.IViewSession;
import com.jcwhatever.bukkit.generic.views.ViewFactory;
import com.jcwhatever.bukkit.generic.views.data.ViewArguments;
import com.jcwhatever.bukkit.generic.views.data.ViewOpenReason;

import javax.annotation.Nullable;

/*
 * 
 */
public class PriceViewFactory extends ViewFactory<PriceView> {

    public PriceViewFactory(String name) {
        super(name, PriceView.class);
    }

    @Override
    protected boolean onOpen(ViewOpenReason reason, PriceView view) {
        view.show(reason);
        return true;
    }

    @Override
    protected void onDispose() {

    }

    @Override
    public IView create(@Nullable String title, IViewSession session, ViewArguments arguments) {
        return new PriceView(session, this, arguments);
    }
}
