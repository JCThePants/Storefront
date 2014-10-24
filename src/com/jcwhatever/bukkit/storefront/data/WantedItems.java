package com.jcwhatever.bukkit.storefront.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;

import com.jcwhatever.bukkit.generic.items.ItemWrapper;
import com.jcwhatever.bukkit.generic.storage.IDataNode;
import com.jcwhatever.bukkit.generic.utils.PreCon;
import com.jcwhatever.bukkit.generic.utils.Utils;
import com.jcwhatever.bukkit.storefront.Category;
import com.jcwhatever.bukkit.storefront.Msg;
import com.jcwhatever.bukkit.storefront.Storefront;
import com.jcwhatever.bukkit.storefront.stores.IStore;
import com.jcwhatever.bukkit.storefront.utils.StoreStackComparer;

public class WantedItems {

    private Map<Category, SaleItemCategoryMap> _wantedCategoryMap = new HashMap<Category, SaleItemCategoryMap>();
    private Map<UUID, WantedItem> _wantedIdMap = new HashMap<UUID, WantedItem>();
    private Map<ItemWrapper, WantedItem> _wantedMap = new HashMap<ItemWrapper, WantedItem>();

    private IDataNode _wantedNode;
    private IStore _store;


    public WantedItems(IStore store, IDataNode wantedNode) {

        _wantedNode = wantedNode;
        _store = store;

        loadSettings();
    }


    public List<SaleItem> getAll () {

        return new ArrayList<SaleItem>(_wantedIdMap.values());
    }


    public List<SaleItem> get (Category category) {

        SaleItemCategoryMap map = _wantedCategoryMap.get(category);
        if (map == null)
            return new ArrayList<SaleItem>(0);

        return new ArrayList<SaleItem>(map.values());
    }


    public SaleItem get (UUID itemId) {

        return _wantedIdMap.get(itemId);
    }


    public SaleItem get (ItemStack item) {

        PreCon.notNull(item);

        ItemWrapper wrapper = new ItemWrapper(item, StoreStackComparer.getDefault());

        return _wantedMap.get(wrapper);
    }


    public SaleItem add (ItemStack itemStack, int qty, double pricePerUnit) {

        PreCon.notNull(itemStack);

        Category category = Storefront.getInstance().getCategoryManager().getCategory(itemStack);
        if (category == null)
            return null;

        UUID itemId = null;

        while (itemId == null) {
            itemId = UUID.randomUUID();
            if (_wantedIdMap.containsKey(itemId))
                itemId = null;
        }

        IDataNode itemNode = _wantedNode.getNode(itemId.toString());

        // constructor saves to itemNode
        WantedItem item = new WantedItem(_store, _store.getOwnerId(), itemId, itemStack, qty,
                pricePerUnit, itemNode);

        _wantedIdMap.put(itemId, item);
        _wantedMap.put(item.getWrapper(), item);

        SaleItemCategoryMap categoryMap = getCategoryMap(category);
        categoryMap.put(itemId, item);

        return item;
    }


    public SaleItem remove (UUID itemId) {

        PreCon.notNull(itemId);

        WantedItem item = _wantedIdMap.remove(itemId);
        if (item == null)
            return null;

        Category category = item.getCategory();
        if (category == null)
            return null;

        _wantedNode.remove(itemId.toString());

        _wantedMap.remove(item.getWrapper());

        SaleItemCategoryMap categoryMap = getCategoryMap(category);
        categoryMap.remove(itemId);

        return item;
    }


    private SaleItemCategoryMap getCategoryMap (Category category) {

        PreCon.notNull(category);

        SaleItemCategoryMap saleItems = _wantedCategoryMap.get(category);
        if (saleItems == null) {
            saleItems = new SaleItemCategoryMap();
            _wantedCategoryMap.put(category, saleItems);
        }

        return saleItems;
    }


    private void loadSettings () {

        Set<String> rawItemIds = _wantedNode.getSubNodeNames();

        if (rawItemIds != null && !rawItemIds.isEmpty()) {

            for (String rawItemId : rawItemIds) {

                UUID itemId = Utils.getId(rawItemId);
                if (itemId == null) {
                    Msg.debug("Failed to parse Item Id: {0}", rawItemId);
                    continue;
                }

                WantedItem saleItem = new WantedItem(_store, itemId, _wantedNode.getNode(rawItemId));

                if (saleItem.getItemStack() == null) {
                    Msg.debug("Failed to parse sale item stack.");
                    continue;
                }

                if (saleItem.getCategory() == null)
                    continue;

                _wantedMap.put(saleItem.getWrapper(), saleItem);
                _wantedIdMap.put(saleItem.getItemId(), saleItem);

                SaleItemCategoryMap categoryMap = getCategoryMap(saleItem.getCategory());
                categoryMap.put(saleItem.getItemId(), saleItem);

            }
        }

    }

}
