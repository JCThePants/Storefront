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


package com.jcwhatever.bukkit.storefront.data;

import com.jcwhatever.nucleus.utils.items.ItemWrapper;
import com.jcwhatever.nucleus.storage.IDataNode;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.bukkit.storefront.Category;
import com.jcwhatever.bukkit.storefront.Storefront;
import com.jcwhatever.bukkit.storefront.stores.IStore;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil;
import com.jcwhatever.bukkit.storefront.utils.StoreStackComparer;
import org.apache.commons.lang.time.DateUtils;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class SaleItem implements ISaleItem {

    private UUID _itemId;
    private UUID _sellerId;
    private IStore _store;
    private Category _category;
    private ItemStack _itemStack;
    private double _pricePerUnit = 1.0D;
    private int _qty;

    private IDataNode _dataNode;
    private ItemWrapper _wrapper;

    private boolean _removed;
    private Date _expires;
    

    public SaleItem(IStore store, UUID sellerId, UUID itemId, ItemStack itemStack, int qty,
                    double pricePerUnit) {

        this(store, sellerId, itemId, itemStack, qty, pricePerUnit, null);
    }


    public SaleItem(IStore store, UUID sellerId, UUID itemId, ItemStack itemStack, int qty,
                    double pricePerUnit, IDataNode dataNode) {

        PreCon.notNull(store);
        PreCon.notNull(itemId);
        PreCon.notNull(itemStack);

        _itemId = itemId;
        _sellerId = sellerId;
        _store = store;
        _itemStack = itemStack;
        _qty = qty;
        _pricePerUnit = pricePerUnit;
        _dataNode = dataNode;
        _wrapper = new ItemWrapper(itemStack, StoreStackComparer.getDurability());
        _expires = DateUtils.addDays(new Date(), 5);

        saveSettings();
    }


    public SaleItem(IStore store, UUID itemId, IDataNode dataNode) {

        _itemId = itemId;
        _store = store;
        _dataNode = dataNode;

        loadSettings();
    }

    @Override
    public boolean isRemoved () {

        return _removed;
    }
    
    @Override
    public boolean isExpired() {
        return _expires.compareTo(new Date()) <= 0;
    }
    
    @Override
    public Date getExpiration() {
        return _expires;
    }
        

    @Override
    public int getTotalSlots () {
        return ItemStackUtil.getTotalSlots(this);
    }

    @Override
    public UUID getItemId () {

        return _itemId;
    }

    @Override
    public UUID getSellerId () {

        return _sellerId;
    }

    @Override
    public IStore getStore () {
        return _store;
    }

    @Override
    public Category getCategory () {

        if (_category == null) {
            _category = Storefront.getInstance().getCategoryManager().getCategory(_itemStack);
        }
        return _category;
    }

    @Override
    public ItemStack getItemStack () {
        return _itemStack.clone();
    }

    @Override
    public ItemWrapper getWrapper () {
        return _wrapper;
    }

    @Override
    public int getQty () {
        return _qty;
    }

    public void setQty (int qty) {

        _qty = Math.max(qty, 0);

        if (_removed)
            return;

        if (qty > 0) {
            _dataNode.set("qty", qty);
        }
        else {
            onRemove(getItemId());
            _dataNode.remove();
            _removed = true;
        }

        _dataNode.saveAsync(null);
    }

    @Override
    public double getPricePerUnit () {
        return _pricePerUnit;
    }

    public void setPricePerUnit (double pricePerUnit) {

        _pricePerUnit = pricePerUnit;

        if (_removed)
            return;

        _dataNode.set("price", pricePerUnit);
        _dataNode.saveAsync(null);
    }

    @Override
    public int getTotalItems () {
        return _qty;
    }

    @Override
    public SaleItem getParent () {
        return this;
    }
    
    @Override
    public void increment (int amount) {
        int qty = _qty + amount;
        setQty(qty);
    }

    private void loadSettings () {

        if (_dataNode == null)
            return;

        ItemStack[] itemStacks = _dataNode.getItemStacks("item");
        if (itemStacks != null && itemStacks.length > 0) {
            _itemStack = itemStacks[0];
            _wrapper = new ItemWrapper(_itemStack, StoreStackComparer.getDurability());
        }

        _sellerId = _dataNode.getUUID("seller-id", _sellerId);
        _pricePerUnit = _dataNode.getDouble("price", _pricePerUnit);
        _qty = _dataNode.getInteger("qty", _qty);
        
        long expireLong = _dataNode.getLong("expires", -1);

        _expires = expireLong == -1
                ? DateUtils.addDays(new Date(), 5)
                : new Date(expireLong);
    }


    private void saveSettings () {

        if (_dataNode == null || _removed)
            return;

        _dataNode.set("item", _itemStack);
        _dataNode.set("qty", _qty);
        _dataNode.set("price", _pricePerUnit);
        _dataNode.set("seller-id", _sellerId);
        _dataNode.set("expires", _expires.getTime());

        _dataNode.saveAsync(null);
    }

    @Override
    public List<ISaleItem> getSaleItemStacks () {

        int maxPerStack = _wrapper.getMaterialExt().getMaxStackSize();
        if (maxPerStack == 0)
            return new ArrayList<>(0);

        int totalStacks = (int) Math.ceil((double) _qty / maxPerStack);

        if (_qty < 1) {
            onRemove(this.getItemId());
            return new ArrayList<>(0);
        }

        List<ISaleItem> result = new ArrayList<>(totalStacks);

        int itemsLeft = _qty;
        for (int i = 0; i < totalStacks; i++) {

            int qty = Math.min(maxPerStack, itemsLeft);

            SaleItemStack stack = new SaleItemStack(this, qty);

            result.add(stack);

            itemsLeft -= qty;
        }
        return result;
    }

    protected void onRemove (UUID itemId) {
        getStore().removeSaleItem(itemId);
    }

    public class SaleItemStack implements ISaleItem {

        private SaleItem _parent;
        private int _qty;

        SaleItemStack(SaleItem parent, int qty) {
            _parent = parent;
            _qty = qty;
        }

        @Override
        public UUID getItemId () {
            return _parent._itemId;
        }

        @Override
        public UUID getSellerId () {
            return _parent._sellerId;
        }

        @Override
        public Category getCategory () {
            return _parent._category;
        }

        @Override
        public int getQty () {
            return _qty;
        }

        @Override
        public void increment (int amount) {
            _qty += amount;
            int newParentQty = _parent._qty + amount;
            _parent.setQty(newParentQty);
        }

        @Override
        public ItemStack getItemStack () {
            return _parent.getItemStack().clone();
        }

        @Override
        public ItemWrapper getWrapper () {
            return _wrapper;
        }

        public void remove () {
            increment(-_qty);
        }
        
        @Override
        public SaleItem getParent() {
            return _parent;
        }

        @Override
        public boolean isRemoved () {
            return _parent.isRemoved();
        }
        
        @Override
        public boolean isExpired() {
            return _parent.isExpired();
        }
        
        @Override
        public Date getExpiration() {
            return _parent.getExpiration();
        }

        @Override
        public int getTotalSlots () {
            return _parent.getTotalSlots();
        }

        @Override
        public IStore getStore () {
            return _parent.getStore();
        }

        @Override
        public double getPricePerUnit () {
            return _parent.getPricePerUnit();
        }

        @Override
        public int getTotalItems () {
            return _parent.getTotalItems();
        }

        @Override
        public List<ISaleItem> getSaleItemStacks () {
            return _parent.getSaleItemStacks();
        }
    }
}
