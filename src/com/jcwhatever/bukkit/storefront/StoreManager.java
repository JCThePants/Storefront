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


package com.jcwhatever.bukkit.storefront;

import com.jcwhatever.bukkit.storefront.data.ISaleItem;
import com.jcwhatever.bukkit.storefront.regions.StoreRegion;
import com.jcwhatever.bukkit.storefront.stores.IStore;
import com.jcwhatever.bukkit.storefront.stores.PlayerStore;
import com.jcwhatever.bukkit.storefront.stores.ServerStore;
import com.jcwhatever.nucleus.Nucleus;
import com.jcwhatever.nucleus.providers.bankitems.IBankItemsAccount;
import com.jcwhatever.nucleus.regions.IRegion;
import com.jcwhatever.nucleus.storage.DataPath;
import com.jcwhatever.nucleus.storage.DataStorage;
import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.utils.BankItems;
import com.jcwhatever.nucleus.utils.observer.result.FutureSubscriber;
import com.jcwhatever.nucleus.utils.observer.result.Result;
import com.jcwhatever.nucleus.utils.performance.SingleCache;

import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

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


    @Nullable
    public IStore getStore (Block block) {

        if (_blockCache.keyEquals(block))
            return _blockCache.getValue();

        List<IRegion> regions = Nucleus.getRegionManager().getRegions(block.getLocation());
        IStore result = null;

        for (IRegion region : regions) {
            result = region.getMeta(StoreRegion.REGION_STORE);
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

    @Nullable
    public IStore addStore (String name, StoreType type) {

        name = name.toLowerCase();

        IStore store = getStore(name);
        if (store != null)
            return null;

        _storeNode.set(name + ".type", type);
        _storeNode.save();

        IDataNode storeNode = getStoreNode(name);

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
        List<ISaleItem> saleItems = store.getSaleItems();
        for (ISaleItem saleItem : saleItems) {

            IBankItemsAccount account = BankItems.getAccount(saleItem.getSellerId());
            account.deposit(saleItem.getItemStack(), saleItem.getQty());
        }

        store.getStoreRegion().setEntryMessage(null);
        store.getStoreRegion().setExitMessage(null);

        DataStorage.remove(Storefront.getInstance(), new DataPath("stores." + name));

        _globalStoreMap.remove(name);
        _playerStoreMap.remove(name);
        _storeMap.remove(name);

        return true;
    }


    private void loadSettings () {

        for (final IDataNode node : _storeNode) {

            final String name = node.getName().toLowerCase();
            final IDataNode storeNode = getStoreNode(node.getName());

            storeNode.loadAsync().onSuccess(new FutureSubscriber<IDataNode>() {
                @Override
                public void on(Result<IDataNode> result) {

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
            }).onError(new FutureSubscriber<IDataNode>() {
                @Override
                public void on(Result<IDataNode> result) {
                    Msg.warning("Failed to load store manager settings.");
                }
            });
        }
    }

    private IDataNode getStoreNode(String storeName) {

        return DataStorage.get(Storefront.getInstance(), new DataPath("stores." + storeName));
    }

}
