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


package com.jcwhatever.bukkit.storefront.stores;

import com.jcwhatever.bukkit.generic.regions.IRegion;
import com.jcwhatever.bukkit.generic.storage.IDataNode;
import com.jcwhatever.bukkit.storefront.Category;
import com.jcwhatever.bukkit.storefront.StoreType;
import com.jcwhatever.bukkit.storefront.data.ISaleItem;
import com.jcwhatever.bukkit.storefront.data.PaginatedItems;
import com.jcwhatever.bukkit.storefront.data.PriceMap;
import com.jcwhatever.bukkit.storefront.data.QtyMap;
import com.jcwhatever.bukkit.storefront.data.SaleItem;
import com.jcwhatever.bukkit.storefront.data.SaleItemSnapshot;
import com.jcwhatever.bukkit.storefront.data.WantedItems;
import com.jcwhatever.bukkit.storefront.regions.StoreRegion;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

public interface IStore {

    /**
     * Get the configuration name of the store.
     */
    public String getName ();

    /**
     * Get the stores display title.
     */
    public String getTitle ();

    /**
     * Set the stores display title.
     *
     * @param title  The title text.
     */
    public void setTitle (String title);

    /**
     * Get the player owner of the store, if any.
     */
    @Nullable
    public UUID getOwnerId ();

    /**
     * Set the id of the player that owns the store.
     *
     * @param ownerId The id of the player owner.
     */
    public void setOwnerId (@Nullable UUID ownerId);

    /**
     * Determine if the store has an owner.
     */
    public boolean hasOwner ();

    /**
     * Get the stores region, external or internal.
     */
    public IRegion getRegion();

    /**
     * Get the stores region wrapper.
     */
    public StoreRegion getStoreRegion();

    /**
     * Sets the region coordinates and sets the store
     * to use an internal region instance.
     *
     * @param p1  The first region point location.
     * @param p2  The seconds region point location.
     */
    public void setRegionCoords(Location p1, Location p2);

    /**
     * Determine if the store defines its own region or uses an external region.
     */
    public boolean hasOwnRegion();

    /**
     * Set an external region for the store. Null to remove.
     *
     * @param region  External region to set
     */
    public void setExternalRegion(IRegion region);

    /**
     * Get the type of store.
     */
    public StoreType getStoreType ();

    /**
     * Open the stores main menu to a player.
     *
     * @param sourceBlock  The block clicked on to open the store, if any.
     * @param player       The player to show the stores main menu to.
     */
    public void view (Block sourceBlock, Player player);

    /**
     * Get a sale item from the store by item id.
     *
     * @param itemId  The id of the sale item.
     */
    public SaleItem getSaleItem (UUID itemId);

    /**
     * Get a sale item from the store being sold by the specified seller 
     * and that matches the provided item stack.
     *
     * @param sellerId   The id of the seller
     * @param itemStack  The item stack
     */
    public SaleItem getSaleItem (UUID sellerId, ItemStack itemStack);

    /**
     * Get all sale items from the store.
     */
    public PaginatedItems getSaleItems ();

    /**
     * Get all sale items in the specified category from the store.
     *
     * @param category  The category to search in
     */
    public PaginatedItems getSaleItems (Category category);

    /**
     * Get sale items to be viewed by the specified seller.
     *
     * @param sellerId  The id of the seller
     */
    public PaginatedItems getSaleItems (UUID sellerId);

    /**
     * Determine if there is room to add a sale item represented by the 
     * provided item stack in the amount specified.
     *
     * @param sellerId   The id of the seller trying to add the item.
     * @param itemStack  The item stack
     * @param qty        The amount that needs to fit
     */
    public boolean canAdd(UUID sellerId, ItemStack itemStack, int qty);

    /**
     * Get the amount of space available to add the specified item.
     *
     * @param sellerId   The id of the seller who wants to add a sale item.
     * @param itemStack  The item stack.
     */
    public int getSpaceAvailable(UUID sellerId, ItemStack itemStack);

    /**
     * Add a sale item to the store.
     *
     * @param seller        The player that is selling the item to/from the store. 
     * @param itemStack     The item stack that represents the item to be sold.
     * @param qty           The number of items that will be sold.
     * @param pricePerUnit  The price per unit of the items to be sold.
     */
    public SaleItem addSaleItem (Player seller, ItemStack itemStack, int qty, double pricePerUnit);


    /**
     * Remove a sale item from the store.
     *
     * @param itemId  The id of the item to remove.
     */
    public SaleItem removeSaleItem (UUID itemId);


    /**
     * Remove a sale item from the store.
     *
     * @param sellerId   The id of the seller whose item is being removed.
     * @param itemStack  An item stack that represents the sale item to be removed.
     */
    public SaleItem removeSaleItem (UUID sellerId, ItemStack itemStack);


    /**
     * Remove a sale item from the store.
     *
     * @param sellerId   The id of the seller whose item is being removed.   
     * @param itemStack  An item stack that represents the sale item to be removed.
     * @param qty        The number of items to remove.
     */
    public SaleItem removeSaleItem (UUID sellerId, ItemStack itemStack, int qty);


    /**
     * Removes items from player and transfers money from store owner to player.
     *
     * @param seller  The player selling items to the store.
     * @param stack   An item stack that represents the items to be sold.
     * @param qty     The number of items to sell.
     * @param price   The total price of the transaction.
     */
    public boolean sellToStore(Player seller, ISaleItem stack, int qty, double price);


    /**
     * Remove item from store and give to the specified buyer. Creates economy transaction
     * between buyer and seller, and transfers items to buyers chest.
     *
     * @param buyer  The player whose is buying the item
     * @param stack  The SaleItemStack that is being purchased.
     * @param qty    The number of items to be purchased.
     * @param price  The total price of the transaction.
     *
     * @return  True if successful
     */
    public boolean buySaleItem (Player buyer, ISaleItem stack, int qty, double price);


    /**
     * Update quantities and prices on items using a current chest and a snapshot
     * of the starting chest.
     *
     * @param seller            The player who is selling the items
     * @param priceMap          The price map containing prices for the chest items.
     * @param currentInventory  The current chest.
     * @param startSnapshot     A snapshot of the chest before it was modified.
     */
    public void updateFromInventory (Player seller, PriceMap priceMap, Inventory currentInventory,
                                     SaleItemSnapshot startSnapshot);


    /**
     * Update quantities and prices on wanted items using a current chest and a snapshot
     * of the starting chest.
     *
     * @param seller            The player who is selling the items
     * @param priceMap          The price map containing prices for the chest items.
     * @param qtyMap            The quantity map containing item quantities for the chest items.
     * @param currentInventory  The current chest.
     * @param startSnapshot     A snapshot of the chest before it was modified.
     */
    public void updateWantedFromInventory (Player seller, PriceMap priceMap, QtyMap qtyMap,
                                           Inventory currentInventory,
                                           SaleItemSnapshot startSnapshot);


    /**
     * update quantities of removed items using a current chest and a snapshot of the
     * starting chest.
     *
     * @param seller            The player who is selling the items
     * @param currentInventory  The current chest.
     * @param startSnapshot     A snapshot of the chest begore it was modified.
     */
    public void updateRemovedFromInventory (final Player seller, final Inventory currentInventory,
                                            final SaleItemSnapshot startSnapshot);

    /**
     * Remove all sale items from the specified seller.
     *
     * @param sellerId  The id of the seller.
     *
     * @return  True if successful
     */
    public boolean clearSaleItems (UUID sellerId);


    /**
     * Get Categories that players can sell to in the store.
     */
    public List<Category> getSellCategories ();


    /**
     * Get Categories that have items for sale in the store.
     */
    public List<Category> getBuyCategories ();


    /**
     * Get items the store owner wants to buy.
     */
    public WantedItems getWantedItems ();


    /**
     * Get the stores data storage node.
     */
    public IDataNode getDataNode ();

}
