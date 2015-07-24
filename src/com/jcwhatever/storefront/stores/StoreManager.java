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

import com.jcwhatever.nucleus.Nucleus;
import com.jcwhatever.nucleus.providers.bankitems.BankItems;
import com.jcwhatever.nucleus.providers.bankitems.IBankItemsAccount;
import com.jcwhatever.nucleus.regions.IRegion;
import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.managers.NamedInsensitiveDataManager;
import com.jcwhatever.nucleus.utils.performance.EntryCache;
import com.jcwhatever.storefront.data.ISaleItem;
import com.jcwhatever.storefront.regions.StoreRegion;
import org.bukkit.block.Block;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Manages all stores.
 */
public class StoreManager extends NamedInsensitiveDataManager<IStore> {

    private final EntryCache<Block, IStore> _blockCache = new EntryCache<>();

    /**
     * Constructor.
     *
     * @param storeNode  The store manager data node.
     */
    public StoreManager(IDataNode storeNode) {
        super(storeNode, true);
    }

    /**
     * Determine what store, if any, a {@link org.bukkit.block.Block}
     * belongs to.
     *
     * @param block  The {@link org.bukkit.block.Block} to check.
     *
     * @return  The {@link IStore} or null if the block is not part of a store.
     */
    @Nullable
    public IStore get(Block block) {
        PreCon.notNull(block);

        if (_blockCache.keyEquals(block))
            return _blockCache.getValue();

        List<IRegion> regions = Nucleus.getRegionManager().getRegions(block.getLocation());
        IStore result = null;

        for (IRegion region : regions) {
            result = region.getMeta().get(StoreRegion.REGION_STORE);
            if (result != null) {
                break;
            }
        }

        _blockCache.set(block, result);
        return result;
    }

    /**
     * Add a new store.
     *
     * @param name  The node name of the store.
     * @param type  The store type.
     *
     * @return  The new {@link IStore} instance or null if the name is already in use.
     */
    @Nullable
    public IStore add(String name, StoreType type) {
        PreCon.validNodeName(name);
        PreCon.notNull(type);

        name = name.toLowerCase();

        IStore store = get(name);
        if (store != null)
            return null;

        IDataNode storeNode = getNode(name);
        storeNode.set("type", type);

        switch (type) {
            case SERVER:
                store = new ServerStore(name, storeNode);
                break;
            case PLAYER_OWNABLE:
                store = new PlayerStore(name, storeNode);
                break;
            default:
                throw new AssertionError();
        }

        add(store);

        return store;
    }

    @Override
    protected void onRemove(IStore removed) {
        super.onRemove(removed);

        // return items to sellers
        List<ISaleItem> saleItems = removed.getSaleItems();
        for (ISaleItem saleItem : saleItems) {

            IBankItemsAccount account = BankItems.getAccount(saleItem.getSellerId());
            account.deposit(saleItem.getItemStack(), saleItem.getQty());
        }

        removed.getStoreRegion().setEntryMessage(null);
        removed.getStoreRegion().setExitMessage(null);
    }

    @Nullable
    @Override
    protected IStore load(String name, IDataNode itemNode) {

        StoreType type = itemNode.getEnum("type", StoreType.SERVER, StoreType.class);
        if (type == null)
            return null;

        switch (type) {
            case SERVER:
                return new ServerStore(name, itemNode);
            case PLAYER_OWNABLE:
                return new PlayerStore(name, itemNode);
            default:
                throw new AssertionError();
        }
    }

    @Override
    protected void save(IStore item, IDataNode itemNode) {
        // do nothing
    }
}
