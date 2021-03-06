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


package com.jcwhatever.storefront;

import com.jcwhatever.storefront.data.ISaleItem;
import com.jcwhatever.storefront.regions.StoreRegion;
import com.jcwhatever.storefront.stores.IStore;
import com.jcwhatever.storefront.stores.StoreManager;
import com.jcwhatever.storefront.stores.StoreType;
import com.jcwhatever.nucleus.Nucleus;
import com.jcwhatever.nucleus.events.regions.RegionOwnerChangedEvent;
import com.jcwhatever.nucleus.providers.bankitems.IBankItemsAccount;
import com.jcwhatever.nucleus.regions.IRegion;
import com.jcwhatever.nucleus.providers.bankitems.BankItems;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Collection;
import java.util.List;

public class BukkitListener implements Listener {

    @EventHandler
    private void onRegionOwnerChanged(RegionOwnerChangedEvent event) {

        // return if there was no previous owner
        if (event.getOldOwnerId() == null)
            return;
        
        StoreManager storeManager = Storefront.getStoreManager();
        
        Collection<IStore> stores = storeManager.getAll();

        // find store with event region
        for (IStore store : stores) {
            if (store.getRegion().equals(event.getRegion())) {

                // store must be player ownable
                if (store.getType() != StoreType.PLAYER_OWNABLE)
                    return;

                // deposit items into bank of old owner
                List<ISaleItem> saleItems = store.getSaleItems();

                for (ISaleItem saleItem : saleItems) {

                    IBankItemsAccount account = BankItems.getAccount(event.getOldOwnerId());
                    account.deposit(saleItem.getItemStack(), saleItem.getQty());
                }

                // remove items from store
                store.clearSaleItems(event.getOldOwnerId());

                if (event.getNewOwnerId() == null) {
                    // remove region entry and exit messages
                    store.getStoreRegion().setEntryMessage(null);
                    store.getStoreRegion().setExitMessage(null);
                }
                
                return;
            }
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    private void onPlayerInteract (PlayerInteractEvent event) {

        if (!event.hasBlock() || event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if (event.getClickedBlock().getType() != Material.CHEST)
            return;

        List<IRegion> regions = Nucleus.getRegionManager().getRegions(
                event.getClickedBlock().getLocation());

        for (IRegion region : regions) {
            IStore store = region.getMeta().get(StoreRegion.REGION_STORE);
            if (store != null) {
                event.setCancelled(true);
                store.view(event.getPlayer(), event.getClickedBlock());
                break;
            }
        }
    }
}
