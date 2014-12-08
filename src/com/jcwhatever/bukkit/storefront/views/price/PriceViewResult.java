package com.jcwhatever.bukkit.storefront.views.price;

import com.jcwhatever.bukkit.generic.mixins.ICancellable;
import com.jcwhatever.bukkit.generic.views.data.ViewArguments;
import com.jcwhatever.bukkit.generic.views.data.ViewResultKey;
import com.jcwhatever.bukkit.generic.views.data.ViewResults;

import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

public class PriceViewResult extends ViewResults implements ICancellable {

    public static final ViewResultKey<ItemStack>
            ITEM_STACK = new ViewResultKey<>(ItemStack.class);

    public static final ViewResultKey<Double>
            PRICE = new ViewResultKey<>(Double.class);

    public static final ViewResultKey<Boolean>
            IS_CANCELLED = new ViewResultKey<>(Boolean.class);

    public PriceViewResult(ItemStack itemStack, double price) {
        super(new ViewResult(ITEM_STACK, itemStack),
              new ViewResult(PRICE, price));
    }

    public PriceViewResult(ItemStack itemStack, double price, ViewArguments merge) {
        super(merge,
                new ViewResult(ITEM_STACK, itemStack),
                new ViewResult(PRICE, price));
    }

    @Nullable
    public double getPrice (double defValue) {
        Double price = get(PRICE);
        if (price == null)
            return defValue;

        return price;
    }

    @Nullable
    public ItemStack getItemStack () {
        return get(ITEM_STACK);
    }

    void setPrice (double price) {
        set(PRICE, price);
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