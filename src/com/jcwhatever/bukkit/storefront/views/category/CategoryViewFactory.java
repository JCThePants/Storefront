package com.jcwhatever.bukkit.storefront.views.category;

import com.jcwhatever.bukkit.generic.views.IView;
import com.jcwhatever.bukkit.generic.views.IViewSession;
import com.jcwhatever.bukkit.generic.views.ViewFactory;
import com.jcwhatever.bukkit.generic.views.data.ViewArguments;
import com.jcwhatever.bukkit.generic.views.data.ViewOpenReason;

import javax.annotation.Nullable;

/*
 * 
 */
public class CategoryViewFactory extends ViewFactory<CategoryView> {

    public CategoryViewFactory(String name) {
        super(name, CategoryView.class);
    }

    @Override
    protected boolean onOpen(ViewOpenReason reason, CategoryView view) {
        view.show(reason);
        return true;
    }

    @Override
    protected void onDispose() {
        // do nothing
    }

    @Override
    public IView create(@Nullable String title, IViewSession session, ViewArguments arguments) {
        return new CategoryView(session, this, arguments);
    }

}
