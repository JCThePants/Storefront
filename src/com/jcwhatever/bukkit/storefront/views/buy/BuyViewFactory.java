package com.jcwhatever.bukkit.storefront.views.buy;

import com.jcwhatever.bukkit.generic.views.IView;
import com.jcwhatever.bukkit.generic.views.IViewSession;
import com.jcwhatever.bukkit.generic.views.ViewFactory;
import com.jcwhatever.bukkit.generic.views.data.ViewArguments;
import com.jcwhatever.bukkit.generic.views.data.ViewOpenReason;

import javax.annotation.Nullable;

/*
 * 
 */
public class BuyViewFactory extends ViewFactory<BuyView> {

    public BuyViewFactory(String name) {
        super(name, BuyView.class);
    }

    @Override
    protected boolean onOpen(ViewOpenReason reason, BuyView view) {
        view.show(reason);
        return true;
    }

    @Override
    protected void onDispose() {

    }

    @Override
    public IView create(@Nullable String title, IViewSession session, ViewArguments arguments) {
        return new BuyView(session, this, arguments);
    }

}
