package com.jcwhatever.bukkit.storefront.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.entity.Player;

import com.jcwhatever.bukkit.generic.utils.PreCon;

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
