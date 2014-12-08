package com.jcwhatever.bukkit.storefront.views.sell;

import com.jcwhatever.bukkit.generic.views.IView;
import com.jcwhatever.bukkit.generic.views.IViewSession;
import com.jcwhatever.bukkit.generic.views.ViewFactory;
import com.jcwhatever.bukkit.generic.views.data.ViewArguments;
import com.jcwhatever.bukkit.generic.views.data.ViewOpenReason;

import javax.annotation.Nullable;

/*
 * 
 */
public class SellViewFactory extends ViewFactory<SellView> {

    public SellViewFactory(String name) {
        super(name, SellView.class);
    }

    @Override
    protected boolean onOpen(ViewOpenReason reason, SellView view) {
        view.show(reason);
        return true;
    }

    @Override
    protected void onDispose() {

    }

    @Override
    public IView create(@Nullable String title, IViewSession session, ViewArguments arguments) {
        return new SellView(title, session, this, arguments);
    }

}
