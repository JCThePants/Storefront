/* This file is part of Storefront for Bukkit, licensed under the MIT License (MIT).
 *
 * Copyright (c) JCThePants (www.jcwhatever.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */


package com.jcwhatever.bukkit.storefront.data;

import com.jcwhatever.nucleus.utils.items.ItemWrapper;
import com.jcwhatever.bukkit.storefront.stores.IStore;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil;
import com.jcwhatever.bukkit.storefront.utils.StoreStackComparer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

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
