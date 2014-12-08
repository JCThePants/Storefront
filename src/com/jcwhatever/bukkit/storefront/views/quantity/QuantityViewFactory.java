package com.jcwhatever.bukkit.storefront.views.quantity;

import com.jcwhatever.bukkit.generic.views.IView;
import com.jcwhatever.bukkit.generic.views.IViewSession;
import com.jcwhatever.bukkit.generic.views.ViewFactory;
import com.jcwhatever.bukkit.generic.views.data.ViewArguments;
import com.jcwhatever.bukkit.generic.views.data.ViewOpenReason;

import javax.annotation.Nullable;

/*
 * 
 */
public class QuantityViewFactory extends ViewFactory<QuantityView> {

    public QuantityViewFactory(String name) {
        super(name, QuantityView.class);
    }

    @Override
    protected boolean onOpen(ViewOpenReason reason, QuantityView view) {
        view.show(reason);
        return true;
    }

    @Override
    protected void onDispose() {

    }

    @Override
    public IView create(@Nullable String title, IViewSession session, ViewArguments arguments) {
        return new QuantityView(session, this, arguments);
    }
}
