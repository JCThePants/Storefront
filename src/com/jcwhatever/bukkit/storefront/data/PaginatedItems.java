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

import com.jcwhatever.nucleus.utils.TimeScale;
import com.jcwhatever.nucleus.mixins.IPaginator;
import com.jcwhatever.nucleus.utils.performance.SingleCache;
import com.jcwhatever.nucleus.views.chest.ChestView;
import com.jcwhatever.bukkit.storefront.Category;
import com.jcwhatever.bukkit.storefront.stores.IStore;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

/**
 * An implementation of {@link com.jcwhatever.nucleus.mixins.IPaginator} for use with
 * {@link ISaleItem}'s.
 */
public class PaginatedItems implements IPaginator<ISaleItem>, ISaleItemStacksGetter {

    private ISaleItemGetter _getter;
    private int _itemsPerPage = ChestView.MAX_SLOTS;
    private SingleCache<List<ISaleItem>, Void> _listCache = new SingleCache<>(1, TimeScale.TICKS);

    /**
     * Constructor.
     *
     * @param getter  A getter used to retrieve sale items for the paginator.
     */
    public PaginatedItems(ISaleItemGetter getter) {
        _getter = getter;
    }

    /**
     * Constructor.
     *
     * @param store     The store to retrieve sale items from.
     * @param category  The category of the items to retrieve.
     */
    public PaginatedItems(final IStore store, final Category category) {
        _getter = new ISaleItemGetter() {
            @Override
            public List<ISaleItem> getSaleItems() {
                return store.getSaleItems(category);
            }
        };
    }

    /**
     * Constructor.
     *
     * @param store     The store to retrieve sale items from.
     * @param sellerId  The ID of the seller of the items to retrieve.
     */
    public PaginatedItems(final IStore store, final UUID sellerId) {
        _getter = new ISaleItemGetter() {
            @Override
            public List<ISaleItem> getSaleItems() {
                return store.getSaleItems(sellerId);
            }
        };
    }

    @Override
    public PageStartIndex getPageStartIndex() {
        return PageStartIndex.ONE;
    }

    @Override
    public int size() {
        return getStacks().size();
    }

    @Override
    public List<ISaleItem> getStacks() {
        List<ISaleItem> saleItems = getList();

        List<ISaleItem> result = new ArrayList<>(saleItems.size() * 5);

        for (ISaleItem saleItem : saleItems) {
            if (saleItem instanceof SaleItem) {
                result.addAll(saleItem.getStacks());
            }
            else {
                result.add(saleItem);
            }
        }

        return result;
    }

    @Override
    public int getTotalPages() {
        int totalItems = size();
        return (int)Math.ceil((double)totalItems / getItemsPerPage());
    }

    @Override
    public int getItemsPerPage() {
        return _itemsPerPage;
    }

    @Override
    public void setItemsPerPage(int itemsPerPage) {
        _itemsPerPage = itemsPerPage;
    }

    @Override
    public List<ISaleItem> getPage(int page) {

        List<ISaleItem> saleItems = getStacks();

        if (saleItems.size() == 0)
            return new ArrayList<>(0);

        int start = getStartIndex(page);
        int end = getEndIndex(page, saleItems.size());

        if (end < start) {
            return new ArrayList<>(0);
        }

        try {
            return saleItems.subList(start, end);
        }
        catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            return new ArrayList<>(0);
        }
    }

    @Override
    public ListIterator<ISaleItem> iterator(int page) {
        return new PaginatorIterator(page);
    }

    private int getStartIndex(int page) {
        return (page - 1) * _itemsPerPage;
    }

    private int getEndIndex(int page, int max) {
        return Math.min(max, getStartIndex(page) + _itemsPerPage);
    }

    private List<ISaleItem> getList() {
        List<ISaleItem> result = _listCache.getKey();
        if (result != null)
            return result;

        result = _getter.getSaleItems();
        _listCache.set(result, null);
        return result;
    }

    /**
     * List iterator for a specific page.
     */
    public class PaginatorIterator implements ListIterator<ISaleItem> {

        int _index;
        final int _startIndex;
        final int _endIndex;
        final List<ISaleItem> _list = getStacks();

        PaginatorIterator(int page) {
            _startIndex = getStartIndex(page);
            _endIndex = getEndIndex(page, _list.size());
            _index = _startIndex;
        }

        @Override
        public boolean hasNext() {
            return _index <= _endIndex && _index <= _list.size();
        }

        @Override
        public ISaleItem next() {
            ISaleItem entry = _list.get(_index);
            _index++;
            return entry;
        }

        @Override
        public boolean hasPrevious() {
            return _index > _startIndex;
        }

        @Override
        public ISaleItem previous() {
            _index--;
            return _list.get(_index);
        }

        @Override
        public int nextIndex() {
            return _index + 1;
        }

        @Override
        public int previousIndex() {
            return _index - 1;
        }

        @Override
        public void remove() {
            _list.remove(_index);
        }

        @Override
        public void set(ISaleItem e) {
            _list.set(_index, e);
        }

        @Override
        public void add(ISaleItem e) {
            _list.add(_index, e);
        }
    }
}
