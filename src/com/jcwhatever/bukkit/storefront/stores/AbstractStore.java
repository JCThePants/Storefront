package com.jcwhatever.bukkit.storefront.stores;

import com.jcwhatever.bukkit.generic.GenericsLib;
import com.jcwhatever.bukkit.generic.economy.EconomyHelper;
import com.jcwhatever.bukkit.generic.inventory.InventoryHelper;
import com.jcwhatever.bukkit.generic.items.ItemWrapper;
import com.jcwhatever.bukkit.generic.items.bank.ItemBankManager;
import com.jcwhatever.bukkit.generic.regions.BasicRegion;
import com.jcwhatever.bukkit.generic.regions.ReadOnlyRegion;
import com.jcwhatever.bukkit.generic.regions.Region;
import com.jcwhatever.bukkit.generic.storage.BatchOperation;
import com.jcwhatever.bukkit.generic.storage.IDataNode;
import com.jcwhatever.bukkit.generic.utils.Utils;
import com.jcwhatever.bukkit.storefront.Category;
import com.jcwhatever.bukkit.storefront.Msg;
import com.jcwhatever.bukkit.storefront.StoreType;
import com.jcwhatever.bukkit.storefront.Storefront;
import com.jcwhatever.bukkit.storefront.data.ISaleItem;
import com.jcwhatever.bukkit.storefront.data.PriceMap;
import com.jcwhatever.bukkit.storefront.data.QtyMap;
import com.jcwhatever.bukkit.storefront.data.SaleItem;
import com.jcwhatever.bukkit.storefront.data.SaleItemCategoryMap;
import com.jcwhatever.bukkit.storefront.data.SaleItemSnapshot;
import com.jcwhatever.bukkit.storefront.data.WantedItems;
import com.jcwhatever.bukkit.storefront.utils.StoreStackComparer;
import com.sun.istack.internal.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public abstract class AbstractStore implements IStore {

    private Map<Category, SaleItemCategoryMap> _categoryMap;

    private String _name;
    private String _title;

    private IDataNode _storeNode;

    private ReadOnlyRegion _externalRegion;
    private Region _internalRegion;

    private WantedItems _wantedItems;

    private String _regionName;
    private Location _regionP1;
    private Location _regionP2;

    public AbstractStore(String name, IDataNode storeNode) {

        _name = name;
        _storeNode = storeNode;
        _categoryMap = new HashMap<Category, SaleItemCategoryMap>(25);

        _internalRegion = new BasicRegion(Storefront.getInstance(), _name, _storeNode.getNode("region"));
        _internalRegion.setMeta(IStore.class.getName(), this);

        onInit();

        loadSettings();
    }


    @Override
    public final String getName () {

        return _name;
    }


    @Override
    public final String getTitle () {

        return _title;
    }


    @Override
    public final void setTitle (String title) {

        _title = title;
        _storeNode.set("title", title);
        _storeNode.saveAsync(null);
    }

    @Override
    public UUID getOwnerId () {

        ReadOnlyRegion region = getStoreRegion();
        if (region == null)
            return null;

        return region.getOwnerId();
    }


    @Override
    public void setOwnerId (UUID ownerId) {
        ReadOnlyRegion region = getStoreRegion();
        if (region == null)
            throw new IllegalStateException("Cannot set owner on a store that has no region.");

        if (region.getPlugin() != Storefront.getInstance())
            throw new IllegalStateException("Cannot set owner on a store with an external region.");

        region.setOwner(ownerId);

    }


    @Override
    public boolean hasOwner () {
        ReadOnlyRegion region = getStoreRegion();
        return region != null && region.hasOwner();
    }


    @Override
    public boolean hasOwnRegion () {
        return _externalRegion == null;
    }


    @Override
    public void setExternalRegion (ReadOnlyRegion region) {

        IDataNode ownRegionNode = _storeNode.getNode("region");

        disposeCurrentRegion();

        if (region == null) {

            clearExternalRegion();
            return;
        }

        boolean isOwn = region.getPlugin() == Storefront.getInstance();

        if (isOwn)
            throw new IllegalStateException("Can only set external regions.");

        if (!region.isDefined())
            throw new IllegalStateException("Region must be defined.");

        _storeNode.set("region-name", region.getName());
        _storeNode.set("region-p1", region.getP1());
        _storeNode.set("region-p2", region.getP2());

        ownRegionNode.remove();

        _externalRegion = region;
        _externalRegion.setMeta(IStore.class.getName(), this);

        _storeNode.saveAsync(null);
    }



    @Override
    public final ReadOnlyRegion getStoreRegion () {

        if (!_internalRegion.isDefined()) {

            // check if an external region is set.
            if (_regionName != null && _regionP1 != null && _regionP2 != null) {

                if (_externalRegion == null) {

                    // find external region
                    Set<ReadOnlyRegion> regions = GenericsLib.getRegionManager().getRegions(_regionP1);

                    for (ReadOnlyRegion region : regions) {
                        if (region.getP1().equals(_regionP1) &&
                                region.getP2().equals(_regionP2) &&
                                region.getName().equals(_regionName)) {

                            _internalRegion.setMeta(IStore.class.getName(), null);
                            _internalRegion.dispose();

                            _externalRegion = region;
                            _externalRegion.setMeta(IStore.class.getName(), this);
                            break;
                        }
                    }
                }
                else {
                    return _externalRegion;
                }
            }
        }

        return new ReadOnlyRegion(_internalRegion);
    }

    @Override
    @Nullable
    public Region getInternalRegion() {

        // load extenal region if any.
        getStoreRegion();

        if (_externalRegion != null)
            return null;

        return _internalRegion;
    }


    @Override
    public void setRegionCoords(Location p1, Location p2) {

        // remove external region
        if (!hasOwnRegion())
            clearExternalRegion();

        _internalRegion.setCoords(p1, p2);
    }

    @Override
    public IDataNode getDataNode () {

        return _storeNode;
    }


    @Override
    public abstract void view (Block sourceBlock, Player p);


    @Override
    public WantedItems getWantedItems () {

        if (getStoreType() == StoreType.SERVER) {
            Msg.debug("Attempted to get Wanted Items from a Server Store.");
            return null;
        }

        if (_wantedItems == null) {
            _wantedItems = new WantedItems(this, _storeNode.getNode("wanted-items"));
        }

        return _wantedItems;
    }


    @Override
    public boolean canAdd (UUID sellerId, ItemStack itemStack, int qty) {
        Category category = Storefront.getInstance().getCategoryManager().getCategory(itemStack);
        if (category == null)
            return false;

        SaleItemCategoryMap map = getCategoryMap(category);

        return map.canAdd(sellerId, itemStack, qty);
    }


    @Override
    public int getSpaceAvailable (UUID sellerId, ItemStack itemStack) {
        Category category = Storefront.getInstance().getCategoryManager().getCategory(itemStack);
        if (category == null)
            return 0;

        SaleItemCategoryMap map = getCategoryMap(category);

        return map.getSpace(sellerId, itemStack);
    }


    @Override
    public boolean sellToStore(Player seller, ISaleItem stack, int qty, double price) {
        if (getStoreType() == StoreType.SERVER) {
            Msg.debug("Cannot sell to a server store");
            return false;
        }

        if (!hasOwner()) {
            Msg.debug("Cannot sell to a store that has no owner.");
            return false;
        }


        SaleItem saleItem = getWantedItems().get(stack.getItemId());
        if (saleItem == null || saleItem.getQty() < qty) {
            Msg.debug("Tried to sell item to the store that it's not willing to accept.");
            return false;
        }


        Inventory playerInventory = seller.getInventory();

        if (!InventoryHelper.has(playerInventory, saleItem.getItemStack(), StoreStackComparer.getDefault(), qty)) {
            Msg.debug("Player doesn't have enough items to sell");
            return false;
        }

        if (!EconomyHelper.transferMoney(getOwnerId(), seller.getUniqueId(), price)) {
            Msg.debug("Failed to transfer money");

            return false;
        }

        InventoryHelper.remove(playerInventory, saleItem.getItemStack(), StoreStackComparer.getDefault(), qty);

        ItemBankManager.deposit(getOwnerId(), saleItem.getItemStack(), qty);

        // TODO

        return true;
    }


    @Override
    public boolean buySaleItem (Player buyer, ISaleItem stack, int qty, double price) {

        SaleItem saleItem = getSaleItem(stack.getItemId());
        if (saleItem == null || saleItem.getQty() < qty) {
            Msg.debug("Couldn't find saleItem or not enough quantity to purchase.");
            return false;
        }

        ItemStack purchasedStack = saleItem.getItemStack().clone();
        purchasedStack.setAmount(qty);

        // make sure player has room in inventory
        if (!InventoryHelper.hasRoom(buyer.getInventory(), purchasedStack)) {
            Msg.debug("Player sale rejected because not enough room in inventory.");
            return false;
        }

        // make sure buyer can afford
        if (EconomyHelper.getBalance(buyer) < price) {
            Msg.debug("Player sale rejected because player doesn't have enough money.");
            return false;
        }

        if (!EconomyHelper.transferMoney(buyer.getUniqueId(), saleItem.getSellerId(), price)) {
            return false;
        }

        stack.increment(-qty);

        buyer.getInventory().addItem(purchasedStack);

        return true;
    }


    @Override
    public void updateWantedFromInventory (final Player seller, final PriceMap priceMap, QtyMap qtyMap,
                                           final Inventory currentInventory,
                                           final SaleItemSnapshot startSnapshot) {

        updateFromInventory(true, seller, priceMap, qtyMap, currentInventory, startSnapshot);
    }


    @Override
    public void updateFromInventory (final Player seller, final PriceMap priceMap,
                                     final Inventory currentInventory,
                                     final SaleItemSnapshot startSnapshot) {

        updateFromInventory(false, seller, priceMap, null, currentInventory, startSnapshot);
    }


    protected SaleItemCategoryMap getCategoryMap (Category category) {

        SaleItemCategoryMap saleItems = _categoryMap.get(category);
        if (saleItems == null) {
            saleItems = new SaleItemCategoryMap();
            _categoryMap.put(category, saleItems);
        }

        return saleItems;
    }


    @Override
    public void updateRemovedFromInventory (final Player seller, final Inventory currentInventory,
                                             final SaleItemSnapshot startSnapshot) {

        getDataNode().runBatchOperation(new BatchOperation() {

            @Override
            public void run (IDataNode config) {

                List<ItemWrapper> originalItems = startSnapshot.getWrappers();
                
                Set<ItemWrapper> processed = new HashSet<ItemWrapper>(originalItems.size());
                
                // search for less than total amount items
                for (ItemWrapper startWrapper : originalItems) {
                    
                    if (processed.contains(startWrapper))
                        continue;
                    
                    processed.add(startWrapper);
                    
                    SaleItem saleItem = getSaleItem(seller.getUniqueId(), startWrapper.getItem());

                    if (saleItem == null)
                        continue;
                    
                    int startQty = InventoryHelper.count(startSnapshot.getItemStacks(), startWrapper.getItem(), StoreStackComparer.getDefault());
                    
                    int currQty = InventoryHelper.count(currentInventory.getContents(), startWrapper.getItem(), StoreStackComparer.getDefault());
                    
                    if (currQty >= startQty)
                        continue;
                    
                    int delta = Math.abs(startQty - currQty);
                    
                    int qty = saleItem.getQty();
                    qty -= delta;
                    saleItem.setQty(qty);
                }
              
            }

        });
    }

    private void updateFromInventory (final boolean isWanted, final Player seller,
                                      final PriceMap priceMap, final QtyMap qtyMap, final Inventory currentInventory,
                                      final SaleItemSnapshot startSnapshot) {

        getDataNode().runBatchOperation(new BatchOperation() {

            @Override
            public void run (IDataNode config) {

                SaleItemSnapshot currentSnapshot = new SaleItemSnapshot(currentInventory);

                List<ItemWrapper> originalItems = startSnapshot.getWrappers();
                List<ItemWrapper> currentItems = currentSnapshot.getWrappers();

                Set<ItemWrapper> processed = new HashSet<ItemWrapper>(originalItems.size());

                // modify original items
                for (ItemWrapper wrapper : originalItems) {

                    SaleItem saleItem = isWanted
                            ? getWantedItems().get(wrapper.getItem())
                                    : getSaleItem(seller.getUniqueId(), wrapper.getItem());

                            if (saleItem == null)
                                continue;

                            int originalAmount = startSnapshot.getAmount(wrapper);
                            int newAmount = currentSnapshot.getAmount(wrapper);
                            int delta = newAmount - originalAmount;

                            Double price = priceMap.getPrice(wrapper);
                            if (price != null) {
                                saleItem.setPricePerUnit(price);
                            }
                            
                            if (qtyMap != null) {
                                Integer qty = qtyMap.getQty(wrapper);
                                if (qty != null) {
                                    saleItem.setQty(qty);
                                }
                            }
                            else if (delta != 0) {
                                int qty = saleItem.getQty();
                                qty += delta;
                                saleItem.setQty(qty);
                            }

                            processed.add(wrapper);
                }

                // add new Items
                for (ItemWrapper wrapper : currentItems) {
                    if (processed.contains(wrapper))
                        continue;

                    Double price = priceMap.getPrice(wrapper);
                    if (price == null)
                        throw new IllegalStateException(
                                "Failed to get a price from the supplied price map.");

                    SaleItem saleItem = isWanted
                            ? getWantedItems().get(wrapper.getItem())
                            : getSaleItem(seller.getUniqueId(), wrapper.getItem());

                    // add new item
                    if (saleItem == null) {
                        
                        Integer qty = qtyMap != null
                                ? qtyMap.getQty(wrapper)
                                : wrapper.getItem().getAmount();
                                
                       if (qty == null)
                          throw new IllegalStateException(
                                    "Failed to get a quantity from the supplied quantity map.");

                        if (isWanted)
                            getWantedItems().add(wrapper.getItem(), qty, price);
                        else
                            addSaleItem(seller, wrapper.getItem(), qty, price);
                    }

                    // merge item with existing
                    else {
                        Integer qty = qtyMap != null
                                ? qtyMap.getQty(wrapper)
                                : currentSnapshot.getAmount(wrapper) + saleItem.getQty();
                                
                        if (qty == null)
                              throw new IllegalStateException(
                                      "Failed to get quantity from the supplied quantity map.");  
                                                
                        saleItem.setQty(qty);
                        saleItem.setPricePerUnit(price);
                    }
                }
            }

        });
    }


    private void disposeCurrentRegion() {
        if (_internalRegion != null) {
            _internalRegion.setMeta(IStore.class.getName(), null);
            
            _internalRegion.setEntryMessage(Storefront.getInstance(), null);
            _internalRegion.setExitMessage(Storefront.getInstance(), null);

            _internalRegion.dispose();
        }
    }

    private void clearExternalRegion() {

        _storeNode.set("region-name", null);
        _storeNode.set("region-p1", null);
        _storeNode.set("region-p2", null);

        _externalRegion = null;
        _regionName = null;
        _regionP1 = null;
        _regionP2 = null;

        IDataNode ownRegionNode = _storeNode.getNode("region");
        ownRegionNode.remove();

        _internalRegion = new BasicRegion(Storefront.getInstance(), _name, _storeNode.getNode("region"));
        _internalRegion.setMeta(IStore.class.getName(), this);

        _storeNode.saveAsync(null);
    }


    private void loadSettings () {

        _title = _storeNode.getString("title", _name);

        _regionName = _storeNode.getString("region-name");
        _regionP1 = _storeNode.getLocation("region-p1");
        _regionP2 = _storeNode.getLocation("region-p2");


        IDataNode itemsNode = _storeNode.getNode("sale-items");

        Set<String> rawItemIds = itemsNode.getSubNodeNames();

        if (rawItemIds != null && !rawItemIds.isEmpty()) {

            for (String rawItemId : rawItemIds) {

                UUID itemId = Utils.getId(rawItemId);
                if (itemId == null) {
                    Msg.debug("Failed to parse Item Id: {0}", rawItemId);
                    continue;
                }

                SaleItem saleItem = new SaleItem(this, itemId, itemsNode.getNode(rawItemId));

                if (saleItem.getItemStack() == null) {
                    Msg.debug("Failed to parse sale item stack.");
                    continue;
                }

                if (saleItem.getCategory() == null)
                    continue;

                onSaleItemLoaded(saleItem);

            }
        }

        Bukkit.getScheduler().runTaskLater(Storefront.getInstance(), new Runnable() {

            @Override
            public void run () {

                // load external region, if any
                getStoreRegion();
            }
        }, 30);

        onLoadSettings(_storeNode);
    }


    @Override
    public abstract StoreType getStoreType ();


    protected IDataNode getItemNode (UUID itemId) {

        return _storeNode.getNode("sale-items." + itemId);
    }


    protected abstract void onInit ();


    protected abstract void onSaleItemLoaded (SaleItem saleItem);


    protected abstract void onLoadSettings (IDataNode storeNode);






}
