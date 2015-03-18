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


package com.jcwhatever.bukkit.storefront.views;

import com.jcwhatever.bukkit.storefront.category.Category;
import com.jcwhatever.bukkit.storefront.Storefront;
import com.jcwhatever.bukkit.storefront.data.ISaleItem;
import com.jcwhatever.bukkit.storefront.data.PaginatedItems;
import com.jcwhatever.bukkit.storefront.data.PriceMap;
import com.jcwhatever.bukkit.storefront.data.QtyMap;
import com.jcwhatever.bukkit.storefront.meta.SessionMetaKey;
import com.jcwhatever.bukkit.storefront.meta.ViewSessionTask;
import com.jcwhatever.bukkit.storefront.stores.IStore;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil.AddToInventoryResult;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil.AddToInventoryResult.SlotInfo;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil.PriceType;
import com.jcwhatever.bukkit.storefront.utils.StoreStackMatcher;
import com.jcwhatever.nucleus.utils.MetaKey;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.inventory.InventorySnapshot;
import com.jcwhatever.nucleus.utils.items.ItemStackUtils;
import com.jcwhatever.nucleus.utils.materials.Materials;
import com.jcwhatever.nucleus.views.View;
import com.jcwhatever.nucleus.views.ViewOpenReason;
import com.jcwhatever.nucleus.views.chest.ChestEventInfo;
import com.jcwhatever.nucleus.views.menu.MenuItem;
import com.jcwhatever.nucleus.views.menu.MenuItemBuilder;
import com.jcwhatever.nucleus.views.menu.PaginatorView;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * A menu view used to manage items that a store owner wants to buy.
 */
public class WantedView extends AbstractMenuView {

    private static final MetaKey<MenuItem> ITEM_TASKED_MENU_ITEM = new MetaKey<>(MenuItem.class);

    private InventorySnapshot _snapshot;
    private PriceMap _priceMap;
    private QtyMap _qtyMap;

    private int _page = 1;
    private PaginatedItems _pagin;

    /**
     * Constructor.
     *
     * @param store           The store the view is for.
     * @param paginatedItems  The current items wanted.
     */
    public WantedView(IStore store, PaginatedItems paginatedItems) {
        super(store);

        PreCon.notNull(paginatedItems);

        _pagin = paginatedItems;
    }

    @Override
    public String getTitle() {
        ViewSessionTask taskMode = getSessionTask();
        return taskMode.getChatColor() + "Wanted";
    }

    @Override
    protected List<MenuItem> createMenuItems() {

        IStore store = getStore();

        ViewSessionTask taskMode = getViewSession().getMeta(SessionMetaKey.TASK_MODE);
        PreCon.notNull(taskMode);

        _priceMap = new PriceMap(getPlayer(), store);
        _qtyMap = new QtyMap(getPlayer(), store);

        List<ISaleItem> items = _pagin.getPage(_page);//, PaginatorPageType.SALE_ITEM);

        List<MenuItem> menuItems = new ArrayList<>(items.size());

        // add items to chest
        for (int i=0; i < items.size(); i++) {
            ISaleItem item = items.get(i);

            ItemStack clone = item.getItemStack();

            MenuItem menuItem = new MenuItemBuilder(clone).build(i);

            updateItem(menuItem, item.getQty(), item.getPricePerUnit());

            menuItems.add(menuItem);
        }

        return menuItems;
    }

    @Override
    protected void onItemSelect(MenuItem menuItem) {
        // do nothing
    }

    @Override
    protected boolean onPreShow(ViewOpenReason reason) {

        if (reason != ViewOpenReason.PREV && reason != ViewOpenReason.REFRESH) {

            if (_pagin.getTotalPages() > 1) {
                getViewSession().next(new PaginatorView(Storefront.getPlugin(), _pagin,
                        StoreStackMatcher.getDurability()));
                return false;
            }
        }

        else if (reason == ViewOpenReason.PREV) {

            View nextView = getViewSession().getNext();

            if (nextView instanceof PaginatorView) {
                _page = ((PaginatorView) nextView).getSelectedPage();
            }
        }

        return true;
    }

    @Override
    protected void onShow(ViewOpenReason reason) {

        InventoryView view = getInventoryView();
        assert view != null;

        // take snapshot of chest
        _snapshot = new InventorySnapshot(view.getTopInventory(), StoreStackMatcher.getDefault());
    }

    /*
     * Update qty and price on an item and update its lore.
     */
    private void updateItem(MenuItem menuItem, int qty, double price) {

        menuItem.setAmount(qty);
        _priceMap.set(menuItem, price);
        _qtyMap.set(menuItem, qty);

        ItemStackUtil.removeTempLore(menuItem);
        ItemStackUtil.setPriceLore(menuItem, price, PriceType.PER_ITEM);
        ItemStackUtil.addTempLore(menuItem, ChatColor.YELLOW + "Wanted: " + ChatColor.GRAY + qty);

        menuItem.set(this);
    }

    /*
     * Handle items being added from lower inventory.
     */
    private boolean onLowerInventoryClick (ChestEventInfo eventInfo) {

        ItemStack selectedStack = eventInfo.getSlotStack();
        if (selectedStack == null || selectedStack.getType() == Material.AIR)
            return false;

        // clone and repair selected stack
        selectedStack = selectedStack.clone();

        if (Materials.isRepairable(selectedStack.getType())) {
            ItemStackUtils.repair(selectedStack);
        }

        InventoryView view = getInventoryView();
        assert view != null;

        InventorySnapshot snapshot = new InventorySnapshot(
                view.getTopInventory(), StoreStackMatcher.getDefault());

        boolean hasItem = snapshot.getAmount(selectedStack) > 0;
        selectedStack.setAmount(1);

        Category category = Storefront.getCategoryManager().get(selectedStack);
        if (category == null)
            return false;

        AddToInventoryResult result = ItemStackUtil.addToInventory(selectedStack, getInventoryView().getTopInventory());
        if (result.getLeftOver() == 1)
            return false;

        SlotInfo slotInfo = result.getSlotsInfo().get(0);
        int slot = slotInfo.getSlot();

        // get item to sell
        ItemStack itemStack = getInventoryView().getTopInventory().getItem(slot);
        itemStack.setAmount(1);

        // open price view
        MenuItem menuItem = new MenuItemBuilder(itemStack)
            .build(slotInfo.getSlot());

        if (!hasItem) {
            _priceMap.set(selectedStack, 1.0D);
            _qtyMap.set(selectedStack, 1);
        }

        getViewSession().setMeta(ITEM_TASKED_MENU_ITEM, menuItem);

        Double price = _priceMap.get(itemStack);
        assert price != null;

        Integer qty = _qtyMap.get(itemStack);
        assert qty != null;

        getViewSession().next(new ItemTaskView(getStore(), itemStack, price, qty, 64));

        return false; // cancel underlying event
    }
}
