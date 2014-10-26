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

import com.jcwhatever.bukkit.generic.storage.IDataNode;
import com.jcwhatever.bukkit.storefront.stores.IStore;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class WantedItem extends SaleItem {

    public WantedItem(IStore store, UUID sellerId, UUID itemId, ItemStack itemStack, int qty,
                      double pricePerUnit) {

        this(store, sellerId, itemId, itemStack, qty, pricePerUnit, null);
    }


    public WantedItem(IStore store, UUID sellerId, UUID itemId, ItemStack itemStack, int qty,
                      double pricePerUnit, IDataNode dataNode) {

        super(store, sellerId, itemId, itemStack, qty, pricePerUnit, dataNode);
    }


    public WantedItem(IStore store, UUID itemId, IDataNode dataNode) {

        super(store, itemId, dataNode);
    }


    @Override
    protected void onRemove (UUID itemId) {

        getStore().getWantedItems().remove(itemId);
    }

}
