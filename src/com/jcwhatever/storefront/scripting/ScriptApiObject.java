package com.jcwhatever.storefront.scripting;

import com.jcwhatever.nucleus.mixins.IDisposable;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.player.PlayerUtils;
import com.jcwhatever.storefront.Storefront;
import com.jcwhatever.storefront.stores.IStore;
import com.jcwhatever.storefront.stores.StoreType;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;

public class ScriptApiObject implements IDisposable {

    private boolean _isDisposed;

    /**
     * Open a store menu view.
     *
     * @param player  The player who will see the view.
     * @param store   The store object or store name.
     */
    public void openMenu(Object player, Object store) {
        PreCon.notNull(player, "player");
        PreCon.notNull(store, "store");

        Player p = PlayerUtils.getPlayer(player);
        PreCon.isValid(p != null, "Invalid player object.");

        IStore s = getStore(store);
        PreCon.isValid(s != null, "Invalid store object.");

        s.view(p, null);
    }

    /**
     * Get a store by name, {@link com.jcwhatever.nucleus.utils.coords.NamedLocation},
     * {@link org.bukkit.Location} or {@link org.bukkit.block.Block}.
     *
     * @param store  The object to reference the store with.
     *
     * @return  The store or null if not found or invalid object provided.
     */
    @Nullable
    public IStore getStore(Object store) {
        PreCon.notNull(store);

        if (store instanceof IStore) {
            return (IStore) store;
        }
        else if (store instanceof String) {
            return Storefront.getStoreManager().get((String)store);
        }
        else if (store instanceof Location) {
            Block block = ((Location)store).getBlock();
            return Storefront.getStoreManager().get(block);
        }
        else if (store instanceof Block) {
            return Storefront.getStoreManager().get((Block)store);
        }

        return null;
    }

    /**
     * Get a list of all stores.
     */
    public List<IStore> getStores() {
        Collection<IStore> stores = Storefront.getStoreManager().getAll();
        List<IStore> results = new ArrayList<>(stores.size());

        for (IStore store : stores)
            results.add(store);

        return results;
    }

    /**
     * Get a list of all server stores.
     */
    public List<IStore> getServerStores() {
        Collection<IStore> stores = Storefront.getStoreManager().getAll();
        List<IStore> results = new ArrayList<>(stores.size());

        for (IStore store : stores) {
            if (store.getType() == StoreType.SERVER)
                results.add(store);
        }

        return results;
    }

    /**
     * Get a list of all player stores.
     */
    public List<IStore> getPlayerStores() {
        Collection<IStore> stores = Storefront.getStoreManager().getAll();
        List<IStore> results = new ArrayList<>(stores.size());

        for (IStore store : stores) {
            if (store.getType() == StoreType.PLAYER_OWNABLE)
                results.add(store);
        }

        return results;
    }

    @Override
    public boolean isDisposed() {
        return _isDisposed;
    }

    @Override
    public void dispose() {
        _isDisposed = true;
    }
}
