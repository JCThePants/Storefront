package com.jcwhatever.bukkit.storefront.views.quantity;

import com.jcwhatever.nucleus.mixins.ICancellable;
import com.jcwhatever.nucleus.views.data.ViewArguments;
import com.jcwhatever.nucleus.views.data.ViewResultKey;
import com.jcwhatever.nucleus.views.data.ViewResults;
import com.jcwhatever.bukkit.storefront.data.ISaleItem;

import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

/*
 * 
 */
public class QuantityViewResult extends ViewResults implements ICancellable {

    public static final ViewResultKey<ISaleItem>
            SALE_ITEM = new ViewResultKey<>(ISaleItem.class);

    public static final ViewResultKey<ItemStack>
            ITEM_STACK = new ViewResultKey<>(ItemStack.class);

    public static final ViewResultKey<Integer>
            QUANTITY = new ViewResultKey<>(Integer.class);

    public static final ViewResultKey<Boolean>
            IS_CANCELLED = new ViewResultKey<>(Boolean.class);

    public QuantityViewResult(@Nullable ISaleItem item, ItemStack itemStack, int qty) {
        super(new ViewResult(SALE_ITEM, item),
                new ViewResult(ITEM_STACK, item.getItemStack()),
                new ViewResult(QUANTITY, qty));
    }

    public QuantityViewResult(ViewArguments merge, @Nullable ISaleItem item, ItemStack itemStack, int qty) {
        super(merge, new ViewResult(SALE_ITEM, item),
                new ViewResult(ITEM_STACK, itemStack),
                new ViewResult(QUANTITY, qty));
    }

    public ISaleItem getSaleItem() {
        return get(SALE_ITEM);
    }

    @Nullable
    public ItemStack getItemStack () {

        return get(ITEM_STACK);
    }

    public int getQty () {

        Integer integer = get(QUANTITY);
        if (integer == null)
            return 1;

        return integer;
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
