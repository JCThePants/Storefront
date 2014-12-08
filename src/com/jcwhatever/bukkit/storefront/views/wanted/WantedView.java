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


package com.jcwhatever.bukkit.storefront.views.wanted;

import com.jcwhatever.bukkit.generic.extended.MaterialExt;
import com.jcwhatever.bukkit.generic.utils.ItemStackUtils;
import com.jcwhatever.bukkit.generic.utils.MetaKey;
import com.jcwhatever.bukkit.generic.utils.PreCon;
import com.jcwhatever.bukkit.generic.views.IViewFactory;
import com.jcwhatever.bukkit.generic.views.ViewSession;
import com.jcwhatever.bukkit.generic.views.chest.ChestEventInfo;
import com.jcwhatever.bukkit.generic.views.data.ViewArguments;
import com.jcwhatever.bukkit.generic.views.data.ViewCloseReason;
import com.jcwhatever.bukkit.generic.views.data.ViewOpenReason;
import com.jcwhatever.bukkit.generic.views.menu.MenuItem;
import com.jcwhatever.bukkit.generic.views.menu.PaginatorView;
import com.jcwhatever.bukkit.storefront.Category;
import com.jcwhatever.bukkit.storefront.Storefront;
import com.jcwhatever.bukkit.storefront.data.PaginatedItems;
import com.jcwhatever.bukkit.storefront.data.PriceMap;
import com.jcwhatever.bukkit.storefront.data.QtyMap;
import com.jcwhatever.bukkit.storefront.data.SaleItem;
import com.jcwhatever.bukkit.storefront.data.SaleItemSnapshot;
import com.jcwhatever.bukkit.storefront.data.WantedItems;
import com.jcwhatever.bukkit.storefront.meta.SessionMetaKey;
import com.jcwhatever.bukkit.storefront.meta.ViewTaskMode;
import com.jcwhatever.bukkit.storefront.stores.IStore;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil.AddToInventoryResult;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil.AddToInventoryResult.SlotInfo;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil.PriceType;
import com.jcwhatever.bukkit.storefront.views.AbstractMenuView;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author JC The Pants
 *
 */
//@ViewInfo(pageType=PaginatorPageType.SALE_ITEM)
public class WantedView extends AbstractMenuView {

    private static final MetaKey<MenuItem> ITEM_TASKED_MENU_ITEM = new MetaKey<>(MenuItem.class);

    private IStore _store;
    private SaleItemSnapshot _snapshot;
    private PriceMap _priceMap;
    private QtyMap _qtyMap;

    protected WantedView(ViewSession session,
                         IViewFactory factory, ViewArguments arguments) {
        super(session, factory, arguments);
    }

    @Override
    protected List<MenuItem> createMenuItems() {

        _store = getViewSession().getMeta(SessionMetaKey.STORE);
        if (_store == null)
            throw new IllegalStateException("Store not set in session meta.");

        ViewTaskMode taskMode = getViewSession().getMeta(SessionMetaKey.TASK_MODE);
        PreCon.notNull(taskMode);

        setTitle(taskMode.getChatColor() + "Wanted");

        _priceMap = new PriceMap(getPlayer(), _store);
        _qtyMap = new QtyMap(getPlayer(), _store);

        Integer page = getArguments().get(PaginatorView.SELECTED_PAGE);
        PaginatedItems pagin = (PaginatedItems)getArguments().get(PaginatorView.PAGINATOR);

        if (page == null) {
            page = 1;
        }

        if (pagin == null) {
            WantedItems wanted = _store.getWantedItems();
            List<SaleItem> saleItems = wanted.getAll();
            pagin = new PaginatedItems(saleItems);
        }

        List<SaleItem> items = pagin.getPage(page);//, PaginatorPageType.SALE_ITEM);

        List<MenuItem> menuItems = new ArrayList<>(items.size());

        // add items to chest
        for (int i=0; i < items.size(); i++) {
            SaleItem item = items.get(i);

            ItemStack clone = item.getItemStack();

            MenuItem menuItem = new MenuItem(i);
            menuItem.setItemStack(clone);

            updateItem(menuItem, item.getQty(), item.getPricePerUnit());

            menuItems.add(menuItem);
        }

        return menuItems;
    }

    @Override
    protected void onItemSelect(MenuItem menuItem) {

    }

    @Override
    protected void onShow(ViewOpenReason reason) {

        // take snapshot of chest
        _snapshot = new SaleItemSnapshot(getInventoryView().getTopInventory());
    }

    @Override
    protected void onClose(ViewCloseReason reason) {

    }

    private void updateItem(MenuItem menuItem, int qty, double price) {

        ItemStack itemStack = menuItem.getItemStack();

        itemStack.setAmount(qty);
        _priceMap.setPrice(itemStack, price);
        _qtyMap.setQty(itemStack, qty);

        ItemStackUtil.removeTempLore(itemStack);
        ItemStackUtil.setPriceLore(itemStack, price, PriceType.PER_ITEM);
        ItemStackUtil.addTempLore(itemStack, ChatColor.YELLOW + "Wanted: " + ChatColor.GRAY + qty);

        menuItem.set(this);
    }


    private boolean onLowerInventoryClick (ChestEventInfo eventInfo) {

        ItemStack selectedStack = eventInfo.getSlotStack();
        if (selectedStack == null || selectedStack.getType() == Material.AIR)
            return false;

        // clone and repair selected stack
        selectedStack = selectedStack.clone();
        MaterialExt material = MaterialExt.from(selectedStack.getType());

        if (material.isRepairable()) {
            ItemStackUtils.repair(selectedStack);
        }

        SaleItemSnapshot snapshot = new SaleItemSnapshot(getInventoryView().getTopInventory());
        boolean hasItem = snapshot.getAmount(selectedStack) > 0;
        selectedStack.setAmount(1);

        Category category = Storefront.getInstance().getCategoryManager().getCategory(selectedStack);
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
        MenuItem menuItem = new MenuItem(slotInfo.getSlot());
        menuItem.setItemStack(itemStack);
        if (!hasItem) {
            _priceMap.setPrice(selectedStack, 1.0D);
            _qtyMap.setQty(selectedStack, 1);
        }

        getViewSession().setMeta(ITEM_TASKED_MENU_ITEM, menuItem);

        showItemTaskMenu(itemStack, _qtyMap.getQty(itemStack), _priceMap.getPrice(itemStack));

        return false; // cancel underlying event
    }

}
