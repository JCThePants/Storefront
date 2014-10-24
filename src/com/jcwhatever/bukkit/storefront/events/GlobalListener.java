package com.jcwhatever.bukkit.storefront.events;

import com.jcwhatever.bukkit.generic.GenericsLib;
import com.jcwhatever.bukkit.generic.events.bukkit.regions.RegionOwnerChangedEvent;
import com.jcwhatever.bukkit.generic.items.bank.ItemBankManager;
import com.jcwhatever.bukkit.generic.regions.ReadOnlyRegion;
import com.jcwhatever.bukkit.storefront.Msg;
import com.jcwhatever.bukkit.storefront.StoreManager;
import com.jcwhatever.bukkit.storefront.StoreType;
import com.jcwhatever.bukkit.storefront.Storefront;
import com.jcwhatever.bukkit.storefront.data.SaleItem;
import com.jcwhatever.bukkit.storefront.stores.IStore;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;
import java.util.Set;

public class GlobalListener implements Listener {

    @EventHandler
    private void onRegionOwnerChanged(RegionOwnerChangedEvent event) {
        
        Msg.debug("Region owner changed.");
        
        // return if there was no previous owner
        if (event.getOldOwnerId() == null)
            return;
        
        StoreManager storeManager = Storefront.getInstance().getStoreManager();
        
        List<IStore> stores = storeManager.getStores();
        
        Msg.debug("find store with region");
        
        // find store with event region
        for (IStore store : stores) {
            if (store.getStoreRegion().getHandleClass().equals(event.getRegion().getHandleClass())) {

                // store must be player ownable
                if (store.getStoreType() != StoreType.PLAYER_OWNABLE)
                    return;

                // deposit items into bank of old owner
                List<SaleItem> saleItems = store.getSaleItems();

                for (SaleItem saleItem : saleItems) {
                    ItemBankManager.deposit(event.getOldOwnerId(), saleItem.getItemStack(), saleItem.getQty());
                }

                Msg.debug("Remove store items");
                // remove items from store
                store.clearSaleItems(event.getOldOwnerId());

                if (event.getNewOwnerId() == null) {
                    // remove region entry and exit messages
                    store.getStoreRegion().setEntryMessage(Storefront.getInstance(), null);
                    store.getStoreRegion().setExitMessage(Storefront.getInstance(), null);
                }
                
                return;
            }
        }
    }
    
    @EventHandler
    private void onPlayerInteract (PlayerInteractEvent event) {

        if (!event.hasBlock() || event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if (event.getClickedBlock().getType() != Material.CHEST)
            return;

        Set<ReadOnlyRegion> regions = GenericsLib.getRegionManager().getRegions(event.getClickedBlock().getLocation());

        for (ReadOnlyRegion region : regions) {
            IStore store = region.getMeta(IStore.class.getName());
            if (store != null) {
                event.setCancelled(true);
                store.view(event.getClickedBlock(), event.getPlayer());
                break;
            }
        }

    }

}
