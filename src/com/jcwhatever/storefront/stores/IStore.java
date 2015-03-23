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


package com.jcwhatever.storefront.stores;

import com.jcwhatever.storefront.category.Category;
import com.jcwhatever.storefront.data.ISaleItem;
import com.jcwhatever.storefront.data.ISaleItemGetter;
import com.jcwhatever.storefront.data.SaleItem;
import com.jcwhatever.storefront.data.WantedItems;
import com.jcwhatever.storefront.regions.StoreRegion;
import com.jcwhatever.nucleus.mixins.INamedInsensitive;
import com.jcwhatever.nucleus.regions.IRegion;
import com.jcwhatever.nucleus.storage.IDataNode;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

/**
 * Interface for a store.
 */
public interface IStore extends INamedInsensitive, ISaleItemGetter {

    /**
     * Get the stores display title.
     */
    String getTitle ();

    /**
     * Set the stores display title.
     *
     * @param title  The title text.
     */
    void setTitle (String title);

    /**
     * Get the player owner of the store, if any.
     */
    @Nullable
    UUID getOwnerId ();

    /**
     * Set the id of the player that owns the store.
     *
     * @param ownerId The id of the player owner.
     */
    void setOwnerId (@Nullable UUID ownerId);

    /**
     * Determine if the store has an owner.
     */
    boolean hasOwner ();

    /**
     * Get the stores region, external or internal.
     */
    IRegion getRegion();

    /**
     * Get the stores region wrapper.
     */
    StoreRegion getStoreRegion();

    /**
     * Determine if the store defines its own region or uses an external region.
     */
    boolean hasOwnRegion();

    /**
     * Set an external region for the store. Null to remove.
     *
     * @param region  External region to set
     */
    void setExternalRegion(IRegion region);

    /**
     * Get the type of store.
     */
    StoreType getType();

    /**
     * Open the stores main menu to a player.
     *
     * @param player       The player to show the stores main menu to.
     * @param sourceBlock  The block clicked on to open the store, if any.
     */
    void view (Player player, @Nullable Block sourceBlock);

    /**
     * Get a sale item from the store by item id.
     *
     * @param itemId  The id of the sale item.
     */
    SaleItem getSaleItem (UUID itemId);

    /**
     * Get a sale item from the store being sold by the specified seller 
     * and that matches the provided item stack.
     *
     * @param sellerId   The id of the seller
     * @param itemStack  The item stack
     */
    SaleItem getSaleItem (UUID sellerId, ItemStack itemStack);

    /**
     * Get all sale items in the specified category from the store.
     *
     * @param category  The category to search in
     */
    List<ISaleItem> getSaleItems (Category category);

    /**
     * Get sale items to be viewed by the specified seller.
     *
     * @param sellerId  The id of the seller
     */
    List<ISaleItem> getSaleItems (UUID sellerId);

    /**
     * Determine if there is room to add a sale item represented by the 
     * provided item stack in the amount specified.
     *
     * @param sellerId   The id of the seller trying to add the item.
     * @param itemStack  The item stack
     * @param qty        The amount that needs to fit
     */
    boolean canAdd(UUID sellerId, ItemStack itemStack, int qty);

    /**
     * Get the amount of space available to add the specified item.
     *
     * @param sellerId   The id of the seller who wants to add a sale item.
     * @param itemStack  The item stack.
     */
    int getSpaceAvailable(UUID sellerId, ItemStack itemStack);

    /**
     * Add a sale item to the store.
     *
     * @param seller        The player that is selling the item to/from the store. 
     * @param itemStack     The item stack that represents the item to be sold.
     * @param qty           The number of items that will be sold.
     * @param pricePerUnit  The price per unit of the items to be sold.
     */
    SaleItem addSaleItem (Player seller, ItemStack itemStack, int qty, double pricePerUnit);

    /**
     * Remove a sale item from the store.
     *
     * @param itemId  The id of the item to remove.
     */
    SaleItem removeSaleItem (UUID itemId);

    /**
     * Remove a sale item from the store.
     *
     * @param sellerId   The id of the seller whose item is being removed.
     * @param itemStack  An item stack that represents the sale item to be removed.
     */
    SaleItem removeSaleItem (UUID sellerId, ItemStack itemStack);

    /**
     * Remove a sale item from the store.
     *
     * @param sellerId   The id of the seller whose item is being removed.   
     * @param itemStack  An item stack that represents the sale item to be removed.
     * @param qty        The number of items to remove.
     */
    SaleItem removeSaleItem (UUID sellerId, ItemStack itemStack, int qty);

    /**
     * Removes items from player and transfers money from store owner to player.
     *
     * @param seller  The player selling items to the store.
     * @param stack   An item stack that represents the items to be sold.
     * @param qty     The number of items to sell.
     * @param price   The total price of the transaction.
     */
    boolean sellToStore(Player seller, ISaleItem stack, int qty, double price);

    /**
     * Remove item from store and give to the specified buyer. Creates economy transaction
     * between buyer and seller, and transfers items to buyers inventory.
     *
     * @param buyer  The player whose is buying the item
     * @param stack  The SaleItemStack that is being purchased.
     * @param qty    The number of items to be purchased.
     * @param price  The total price of the transaction.
     *
     * @return  True if successful
     */
    boolean buySaleItem (Player buyer, ISaleItem stack, int qty, double price);

    /**
     * Remove all sale items from the specified seller.
     *
     * @param sellerId  The id of the seller.
     *
     * @return  True if successful
     */
    boolean clearSaleItems (UUID sellerId);

    /**
     * Get Categories that players can sell to in the store.
     */
    List<Category> getSellCategories ();

    /**
     * Get Categories that have items for sale in the store.
     */
    List<Category> getBuyCategories ();

    /**
     * Get items the store owner wants to buy.
     */
    WantedItems getWantedItems ();

    /**
     * Get the stores data storage node.
     */
    IDataNode getDataNode ();
}
