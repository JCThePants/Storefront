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

import com.jcwhatever.bukkit.generic.utils.PreCon;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PaginatedSaleItems {

    public static final int MAX_PER_PAGE = 6 * 9;
    public static final int MAX_PAGES = 6 * 9;

    private List<ISaleItem> _saleItems = new ArrayList<ISaleItem>();
    private int _itemBuffer = 0;
    
    public enum PaginatorPageType {

        /**
         * Paginator should calculate pages based on sale items.
         */
        SALE_ITEM,
        
        /**
         * Paginator should calculate pages based on sale item stacks.
         */
        SALE_ITEM_STACK
    }



    public PaginatedSaleItems() {

    }


    public PaginatedSaleItems(Collection<? extends ISaleItem> saleItems) {

        addAll(saleItems);
    }


    public PaginatedSaleItems(Collection<? extends ISaleItem> saleItems, int itemBuffer) {

        addAll(saleItems);
        _itemBuffer = itemBuffer;
    }


    public boolean add (ISaleItem saleItem) {
        
        if (saleItem.getParent() != null)
            return false;

        return _saleItems.add(saleItem);
    }


    public void addAll (Collection<? extends ISaleItem> saleItems) {
        
        for (ISaleItem item : saleItems)
            add(item);
    }


    public void clear () {

        _saleItems.clear();
    }


    public int getTotalPages (PaginatorPageType pageType) {

        
        int size = pageType == PaginatorPageType.SALE_ITEM_STACK
                ? getSaleItemStacks().size()
                : _saleItems.size();

        int total = size + _itemBuffer;

        int totalPages = (int) Math.ceil((double) total / MAX_PER_PAGE);

        totalPages = Math.max(totalPages, 1);

        return Math.min(totalPages, MAX_PAGES);
    }


    public int getTotalPages (Player excludedSeller, PaginatorPageType pageType) {

        if (excludedSeller == null)
            return getTotalPages(pageType);

        int size = pageType == PaginatorPageType.SALE_ITEM_STACK
                ? getSaleItemStacks(excludedSeller).size()
                : getSaleItems(excludedSeller).size();

        int total = size + _itemBuffer;

        int totalPages = (int) Math.ceil((double) total / MAX_PER_PAGE);

        totalPages = Math.max(totalPages, 1);

        return Math.min(totalPages, MAX_PAGES);
    }


    public List<ISaleItem> getPage (int page, PaginatorPageType pageType) {

        return getPage(page, null, pageType);
    }


    public List<ISaleItem> getPage (int page, Player excludeSeller, PaginatorPageType pageType) {
        PreCon.notNull(pageType);
                
        if (page == 0)
            throw new IndexOutOfBoundsException("Cannot retrieve a page less than 1.");

        if (page > MAX_PAGES)
            throw new IndexOutOfBoundsException("Cannot retrieve a page greater than " + MAX_PAGES);

        List<ISaleItem> saleItemStacks = pageType == PaginatorPageType.SALE_ITEM_STACK
                ? getSaleItemStacks(excludeSeller)
                : getSaleItems(excludeSeller);

        int total = saleItemStacks.size();
        int firstItem = page * MAX_PER_PAGE - MAX_PER_PAGE;
        int lastItem = Math.min(firstItem + MAX_PER_PAGE - 1, total - 1);

        List<ISaleItem> result = new ArrayList<ISaleItem>();

        if (firstItem < total) {

            for (int i = firstItem; i <= lastItem; i++) {

                ISaleItem item = saleItemStacks.get(i);

                result.add(item);
            }

        }

        return result;
    }


    public List<ISaleItem> getSaleItemStacks () {

        return getSaleItemStacks(null);
    }


    public List<ISaleItem> getSaleItemStacks (Player excludedSeller) {

        List<ISaleItem> result = new ArrayList<ISaleItem>();

        for (ISaleItem saleItem : _saleItems) {
            if (saleItem.getTotalItems() < 1)
                continue;

            if (excludedSeller != null
                    && saleItem.getSellerId().equals(excludedSeller.getUniqueId()))
                continue;

            result.addAll(saleItem.getSaleItemStacks());
        }

        return result;
    }
    
    public List<ISaleItem> getSaleItems (Player excludedSeller) {

        List<ISaleItem> result = new ArrayList<ISaleItem>();

        for (ISaleItem saleItem : _saleItems) {
            
            if (saleItem.getTotalItems() < 1)
                continue;

            if (excludedSeller != null
                    && saleItem.getSellerId().equals(excludedSeller.getUniqueId()))
                continue;

            result.add(saleItem);
        }

        return result;
        
    }

}
