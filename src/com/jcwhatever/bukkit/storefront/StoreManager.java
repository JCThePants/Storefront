package com.jcwhatever.bukkit.storefront;

import com.jcwhatever.bukkit.generic.GenericsLib;
import com.jcwhatever.bukkit.generic.items.bank.ItemBankManager;
import com.jcwhatever.bukkit.generic.performance.SingleCache;
import com.jcwhatever.bukkit.generic.regions.ReadOnlyRegion;
import com.jcwhatever.bukkit.generic.storage.DataStorage;
import com.jcwhatever.bukkit.generic.storage.DataStorage.DataPath;
import com.jcwhatever.bukkit.generic.storage.IDataNode;
import com.jcwhatever.bukkit.generic.storage.StorageLoadHandler;
import com.jcwhatever.bukkit.generic.storage.StorageLoadResult;
import com.jcwhatever.bukkit.storefront.data.SaleItem;
import com.jcwhatever.bukkit.storefront.stores.IStore;
import com.jcwhatever.bukkit.storefront.stores.PlayerStore;
import com.jcwhatever.bukkit.storefront.stores.ServerStore;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StoreManager {

    private Map<String, IStore> _storeMap = new HashMap<>(40);
    private Map<String, IStore> _globalStoreMap = new HashMap<>(5);
    private Map<String, IStore> _playerStoreMap = new HashMap<>(35);

    private IDataNode _storeNode;
    private SingleCache<Block, IStore> _blockCache = new SingleCache<>();


    StoreManager(IDataNode storeNode) {

        _storeNode = storeNode;

        loadSettings();
    }


    public List<IStore> getStores () {

        return new ArrayList<IStore>(_storeMap.values());
    }


    public List<IStore> getServerStores () {

        return new ArrayList<IStore>(_globalStoreMap.values());
    }


    public List<IStore> getPlayerStores () {

        return new ArrayList<IStore>(_playerStoreMap.values());
    }


    public IStore getStore (Block block) {

        if (_blockCache.keyEquals(block))
            return _blockCache.getValue();

        Set<ReadOnlyRegion> regions = GenericsLib.getRegionManager().getRegions(block.getLocation());
        IStore result = null;

        for (ReadOnlyRegion region : regions) {
            result = region.getMeta(IStore.class.getName());
            if (result != null) {
                break;
            }
        }

        _blockCache.set(block, result);
        return result;
    }


    public IStore getStore (String name) {

        name = name.toLowerCase();

        return _storeMap.get(name);
    }


    public IStore getServerStore (String name) {

        name = name.toLowerCase();

        return _globalStoreMap.get(name);
    }


    public IStore getPlayerStore (String name) {

        name = name.toLowerCase();

        return _playerStoreMap.get(name);
    }


    public IStore addStore (String name, StoreType type) {

        name = name.toLowerCase();

        IStore store = getStore(name);
        if (store != null)
            return null;

        _storeNode.set(name + ".type", type);
        _storeNode.saveAsync(null);

        IDataNode storeNode = getStoreConfig(name);

        if (type == StoreType.SERVER) {
            store = new ServerStore(name, storeNode);
            _globalStoreMap.put(name, store);
        }
        else {
            store = new PlayerStore(name, storeNode);
            _playerStoreMap.put(name, store);
        }

        _storeMap.put(name, store);

        return store;
    }


    public boolean removeStore (String name) {

        name = name.toLowerCase();

        IStore store = getStore(name);
        if (store == null)
            return false;

        // return items to sellers
        List<SaleItem> saleItems = store.getSaleItems();
        for (SaleItem saleItem : saleItems) {

            ItemBankManager.deposit(saleItem.getSellerId(), saleItem.getItemStack(), saleItem.getQty());
            saleItem.setQty(0);
        }
        
        store.getStoreRegion().setEntryMessage(Storefront.getInstance(), null);
        store.getStoreRegion().setExitMessage(Storefront.getInstance(), null);

        DataStorage.removeStorage(Storefront.getInstance(), new DataPath("stores." + name));

        _globalStoreMap.remove(name);
        _playerStoreMap.remove(name);
        _storeMap.remove(name);

        return true;
    }


    private void loadSettings () {

        Set<String> storeNames = _storeNode.getSubNodeNames();
        if (storeNames != null && !storeNames.isEmpty()) {

            for (String storeName : storeNames) {

                final String name = storeName.toLowerCase();

                final IDataNode storeNode = getStoreConfig(name);

                storeNode.loadAsync(new StorageLoadHandler() {

                    @Override
                    public void onFinish(StorageLoadResult result) {

                        if (!result.isLoaded())
                            return;

                        StoreType type = _storeNode.getEnum(name + ".type", StoreType.SERVER, StoreType.class);

                        IStore store;

                        if (type == StoreType.SERVER) {
                            store = new ServerStore(name, storeNode);
                            _globalStoreMap.put(name, store);
                        } else {
                            store = new PlayerStore(name, storeNode);
                            _playerStoreMap.put(name, store);
                        }

                        _storeMap.put(name, store);

                    }

                });

            }
        }
    }

    private IDataNode getStoreConfig (String storeName) {

        return DataStorage.getStorage(Storefront.getInstance(), new DataPath("stores." + storeName));
    }

}
