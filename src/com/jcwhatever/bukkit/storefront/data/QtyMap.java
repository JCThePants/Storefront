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

import com.jcwhatever.generic.utils.items.ItemWrapper;
import com.jcwhatever.bukkit.storefront.stores.IStore;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil;
import com.jcwhatever.bukkit.storefront.utils.StoreStackComparer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;


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

