package com.jcwhatever.bukkit.storefront.data;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.jcwhatever.bukkit.generic.items.ItemWrapper;
import com.jcwhatever.bukkit.storefront.stores.IStore;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil;
import com.jcwhatever.bukkit.storefront.utils.StoreStackComparer;


public class QtyMap {

    private IStore _store;
    private Player _seller;
    private Map<ItemWrapper, Integer> _qtyMap = new HashMap<ItemWrapper, Integer>(7 * 9);


    public QtyMap(Player seller, IStore store) {

        _store = store;
        _seller = seller;
    }


    public Integer getQty (ItemStack itemStack) {

        return getQty(getWrapper(itemStack));
    }


    public Integer getQty (ItemWrapper wrapper) {

        Integer qty = _qtyMap.get(wrapper);

        if (qty == null) {
            SaleItem saleItem = _store.getSaleItem(_seller.getUniqueId(), wrapper.getItem());

            if (saleItem != null) {
                qty = saleItem.getQty();
                _qtyMap.put(wrapper, qty);
            }
        }

        return qty;
    }


    public void setQty (ItemStack itemStack, int qty) {

        setQty(getWrapper(itemStack), qty);
    }


    public void setQty (ItemWrapper wrapper, int qty) {

        _qtyMap.put(wrapper, qty);
    }


    public void clearQty (ItemStack itemStack) {

        clearQty(getWrapper(itemStack));
    }


    public void clearQty (ItemWrapper wrapper) {

        _qtyMap.remove(wrapper);
    }


    private ItemWrapper getWrapper (ItemStack itemStack) {

        itemStack = itemStack.clone();

        ItemStackUtil.removeTempLore(itemStack);

        ItemWrapper wrapper = new ItemWrapper(itemStack, StoreStackComparer.getDefault());

        return wrapper;
    }

}

