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


package com.jcwhatever.storefront.data;

import com.jcwhatever.storefront.stores.IStore;
import com.jcwhatever.nucleus.storage.IDataNode;

import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import javax.annotation.Nullable;

/**
 * A {@link SaleItem} that represents an item that is wanted for purchase
 * by a store owner.
 */
public class WantedItem extends SaleItem {

    /**
     * Constructor.
     *
     * <p>Used for creating a new transient item.</p>
     *
     * @param store         The store the item is wanted for.
     * @param sellerId      The ID of the seller that wants the item.
     * @param itemId        The item ID.
     * @param itemStack     The {@link org.bukkit.inventory.ItemStack} the seller wants.
     * @param qty           The quantity desired.
     * @param pricePerUnit  The price per unit.
     */
    public WantedItem(IStore store, UUID sellerId, UUID itemId, ItemStack itemStack, int qty,
                      double pricePerUnit) {

        this(store, sellerId, itemId, itemStack, qty, pricePerUnit, null);
    }

    /**
     * Constructor.
     *
     * <p>Used for create a new item that is stored to data node.</p>
     *
     * @param store         The store the item is wanted for.
     * @param sellerId      The ID of the seller that wants the item.
     * @param itemId        The item ID.
     * @param itemStack     The {@link org.bukkit.inventory.ItemStack} the seller wants.
     * @param qty           The quantity desired.
     * @param pricePerUnit  The price per unit.
     * @param dataNode      The items data node.
     */
    public WantedItem(IStore store, UUID sellerId, UUID itemId, ItemStack itemStack, int qty,
                      double pricePerUnit, @Nullable IDataNode dataNode) {

        super(store, sellerId, itemId, itemStack, qty, pricePerUnit, dataNode);
    }

    /**
     * Constructor.
     *
     * <p>Used to load an existing item from a data node.</p>
     *
     * @param store     The store the item is wanted for.
     * @param itemId    The ID of the seller that wants the item.
     * @param dataNode  The items data node.
     */
    public WantedItem(IStore store, UUID itemId, IDataNode dataNode) {
        super(store, itemId, dataNode);
    }

    @Override
    protected void onRemove (UUID itemId) {
        getStore().getWantedItems().remove(itemId);
    }
}
