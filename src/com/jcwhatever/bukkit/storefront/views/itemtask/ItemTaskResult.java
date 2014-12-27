package com.jcwhatever.bukkit.storefront.views.itemtask;

import com.jcwhatever.nucleus.mixins.ICancellable;
import com.jcwhatever.nucleus.views.data.ViewArguments;
import com.jcwhatever.nucleus.views.data.ViewResultKey;
import com.jcwhatever.nucleus.views.data.ViewResults;
import javax.annotation.Nullable;

/*
 * 
 */
public class ItemTaskResult extends ViewResults implements ICancellable {

    public static ViewResultKey<Double>
            PRICE = new ViewResultKey<Double>(Double.class);

    public static ViewResultKey<Integer>
            QUANTITY = new ViewResultKey<Integer>(Integer.class);

    public static ViewResultKey<Boolean>
            IS_CANCELLED = new ViewResultKey<>(Boolean.class);



    public ItemTaskResult(double price, int qty) {
        super(new ViewResult(PRICE, price),
              new ViewResult(QUANTITY, qty));
    }

    public ItemTaskResult(ViewArguments merge, double price, int qty) {
        super(merge, new ViewResult(PRICE, price),
                     new ViewResult(QUANTITY, qty));
    }

    @Nullable
    public Double getPrice () {

        return get(PRICE);
    }

    @Nullable
    public Integer getQty () {

        return get(QUANTITY);
    }

    void setPrice (double price) {

        set(PRICE, price);
    }

    void setQty (int qty) {

        set(QUANTITY, qty);
    }

    @Override
    public boolean isCancelled() {
        Boolean isCancelled = get(IS_CANCELLED);
        if (isCancelled == null)
            return false;

        return isCancelled;
    }

    @Override
    public void setCancelled(boolean isCancelled) {

        set(IS_CANCELLED, isCancelled);
    }
}
