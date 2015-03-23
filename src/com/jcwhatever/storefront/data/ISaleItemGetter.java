package com.jcwhatever.storefront.data;

import java.util.List;

/**
 * Used to retrieve a sale items list.
 */
public interface ISaleItemGetter {

    /**
     * Get all sale items from the store.
     */
    List<ISaleItem> getSaleItems();
}
