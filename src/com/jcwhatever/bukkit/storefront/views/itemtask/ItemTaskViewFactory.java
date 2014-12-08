package com.jcwhatever.bukkit.storefront.views.itemtask;

import com.jcwhatever.bukkit.generic.views.IView;
import com.jcwhatever.bukkit.generic.views.IViewSession;
import com.jcwhatever.bukkit.generic.views.ViewFactory;
import com.jcwhatever.bukkit.generic.views.data.ViewArguments;
import com.jcwhatever.bukkit.generic.views.data.ViewOpenReason;

import javax.annotation.Nullable;

/*
 * 
 */
public class ItemTaskViewFactory extends ViewFactory<ItemTaskView> {

    public ItemTaskViewFactory(String name) {
        super(name, ItemTaskView.class);
    }

    @Override
    protected boolean onOpen(ViewOpenReason reason, ItemTaskView view) {
        view.show(reason);
        return true;
    }

    @Override
    protected void onDispose() {

    }

    @Override
    public IView create(@Nullable String title, IViewSession session, ViewArguments arguments) {

        return new ItemTaskView(session, this, arguments);
    }
}
