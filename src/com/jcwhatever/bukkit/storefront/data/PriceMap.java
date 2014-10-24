package com.jcwhatever.bukkit.storefront.data;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.jcwhatever.bukkit.generic.items.ItemWrapper;
import com.jcwhatever.bukkit.storefront.stores.IStore;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil;
import com.jcwhatever.bukkit.storefront.utils.StoreStackComparer;

public class PriceMap {

    private IStore _store;
    private Player _seller;
    private Map<ItemWrapper, Double> _priceMap = new HashMap<ItemWrapper, Double>(7 * 9);


    public PriceMap(Player seller, IStore store) {

        _store = store;
        _seller = seller;
    }


    public Double getPrice (ItemStack itemStack) {

        return getPrice(getWrapper(itemStack));
    }


    public Double getPrice (ItemWrapper wrapper) {

        Double price = _priceMap.get(wrapper);

        if (price == null) {
            SaleItem saleItem = _store.getSaleItem(_seller.getUniqueId(), wrapper.getItem());

            if (saleItem != null) {
                price = saleItem.getPricePerUnit();
                _priceMap.put(wrapper, price);
            }
        }

        return price;
    }


    public void setPrice (ItemStack itemStack, double price) {

        setPrice(getWrapper(itemStack), price);
    }


    public void setPrice (ItemWrapper wrapper, double price) {

        _priceMap.put(wrapper, price);
    }


    public void clearPrice (ItemStack itemStack) {

        clearPrice(getWrapper(itemStack));
    }


    public void clearPrice (ItemWrapper wrapper) {

        _priceMap.remove(wrapper);
    }


    private ItemWrapper getWrapper (ItemStack itemStack) {

        itemStack = itemStack.clone();

        ItemStackUtil.removeTempLore(itemStack);

        ItemWrapper wrapper = new ItemWrapper(itemStack, StoreStackComparer.getDefault());

        return wrapper;
    }

}
