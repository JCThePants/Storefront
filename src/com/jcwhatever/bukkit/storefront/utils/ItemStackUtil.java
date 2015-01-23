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


package com.jcwhatever.bukkit.storefront.utils;

import com.jcwhatever.nucleus.utils.extended.MaterialExt;
import com.jcwhatever.nucleus.utils.items.MatchableItem;
import com.jcwhatever.nucleus.utils.items.ItemStackUtils;
import com.jcwhatever.nucleus.utils.player.PlayerUtils;
import com.jcwhatever.bukkit.storefront.Storefront;
import com.jcwhatever.bukkit.storefront.data.SaleItem;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

public class ItemStackUtil {

    
    private static final String TEMP_INDICATOR = ChatColor.BLACK.toString()
            + ChatColor.GRAY.toString();
    
    private static NumberFormat _format = DecimalFormat.getCurrencyInstance();
    private static StoreStackMatcher _durabilityComparer = StoreStackMatcher.getDurability();

    public enum PriceType {
        PER_ITEM,
        TOTAL
    }

    public static List<String> removeTempLore (ItemStack stack) {

        List<String> lore = ItemStackUtils.getLore(stack);
        if (lore == null)
            return new ArrayList<String>(0);

        List<String> newLore = new ArrayList<String>(5);
        List<String> removed = new ArrayList<String>(5);

        for (String line : lore) {
            if (line.indexOf(TEMP_INDICATOR) == 0) {
                removed.add(line.substring(TEMP_INDICATOR.length()));
                continue;
            }

            newLore.add(line);
        }

        ItemStackUtils.setLore(stack, newLore);

        return removed;
    }


    public static void removeTempLore (final Inventory inventory, boolean runLater) {

        Runnable runnable = new Runnable() {

            @Override
            public void run () {

                ItemStack[] stacks = inventory.getContents();
                for (int i = 0; i < stacks.length; i++) {

                    ItemStack stack = stacks[i];

                    if (stack != null)
                        removeTempLore(stack);

                    inventory.setItem(i, stack);
                }
            }
        };

        if (runLater)
            Bukkit.getScheduler().runTaskLater(Storefront.getInstance(), runnable, 1);
        else
            runnable.run();

    }


    public static void addTempLore (ItemStack stack, String text) {

        List<String> lore = ItemStackUtils.getLore(stack);

        if (lore == null)
            lore = new ArrayList<>(5);

        int insertAt = 0;

        while (insertAt < lore.size()) {
            if (lore.get(insertAt).indexOf(TEMP_INDICATOR) != 0)
                break;

            insertAt++;
        }
        lore.add(insertAt, TEMP_INDICATOR + text);

        ItemStackUtils.setLore(stack, lore);
    }


    public static void addTempLore (ItemStack stack, List<String> newLore) {

        List<String> lore = ItemStackUtils.getLore(stack);

        if (lore == null)
            lore = new ArrayList<>(5);

        int insertAt = hasPriceLore(stack)
                ? 1
                : 0;

        for (int i = insertAt, j = 0; i < newLore.size() + insertAt; i++, j++) {
            lore.add(i, TEMP_INDICATOR + newLore.get(j));
        }

        ItemStackUtils.setLore(stack, lore);
    }


    public static void setSellerLore (ItemStack stack, UUID sellerId) {

        String playerName = PlayerUtils.getPlayerName(sellerId);
        if (playerName == null)
            playerName = "?";

        addTempLore(stack, ChatColor.YELLOW + "Seller: " + ChatColor.GRAY + playerName);
    }


    public static void setPriceLore (ItemStack itemStack, double price, PriceType priceType) {

        List<String> lore = ItemStackUtils.getLore(itemStack);
        if (lore == null) {
            lore = new ArrayList<>(5);
        }

        if (lore.size() > 0) {
            String line1 = lore.get(0);

            if (line1.indexOf(TEMP_INDICATOR + ChatColor.YELLOW + "Price: ") == 0)
                lore.remove(0);
        }

        String suffix = priceType == PriceType.PER_ITEM
                ? " per item"
                : " total";

        lore.add(0, TEMP_INDICATOR + ChatColor.YELLOW + "Price: " + ChatColor.GREEN
                + _format.format(price) + ChatColor.GRAY + suffix);

        ItemStackUtils.setLore(itemStack, lore);
    }
    
    


    /**
     * Change all item stacks in an chest that match the provided item stack type to the specified price.
     * Note: Only use in views that contain chest for a single player
     * @param inventory
     * @param itemStack
     * @param price
     * @param runLater
     */
    public static void setPriceLore (final Inventory inventory, final ItemStack itemStack,
                                     final double price, final PriceType priceType, boolean runLater) {

        Runnable runnable = new Runnable() {

            @Override
            public void run () {

                removePriceLore(itemStack);
                MatchableItem wrapper = new MatchableItem(itemStack, _durabilityComparer);

                ItemStack[] items = inventory.getContents();

                for (int i = 0; i < items.length; i++) {

                    ItemStack item = items[i];

                    if (item == null)
                        continue;

                    String priceLine = getPriceLore(item);
                    if (priceLine != null) {
                        removePriceLore(item);
                    }

                    if (wrapper.equals(item))
                        setPriceLore(item, price, priceType);
                    else if (priceLine != null)
                        setPriceLoreLine(item, priceLine);

                    inventory.setItem(i, item);
                }

                setPriceLore(itemStack, price, priceType);

            }
        };

        if (runLater)
            Bukkit.getScheduler().runTaskLater(Storefront.getInstance(), runnable, 1);
        else
            runnable.run();

    }


    @Nullable
    public static String getPriceLore (ItemStack itemStack) {

        List<String> lore = ItemStackUtils.getLore(itemStack);
        if (lore == null)
            return null;

        if (lore.size() == 0)
            return null;

        String line1 = lore.get(0);

        if (line1.indexOf(TEMP_INDICATOR + ChatColor.YELLOW + "Price: ") == 0)
            return line1;

        return null;
    }


    private static void setPriceLoreLine (ItemStack itemStack, String str) {

        List<String> lore = ItemStackUtils.getLore(itemStack);
        if (lore == null) {
            lore = new ArrayList<>(5);
        }

        for (int i = 0; i < lore.size(); i++) {
            String line = lore.get(i);

            if (line.indexOf(TEMP_INDICATOR + ChatColor.YELLOW + "Price: ") == 0) {
                lore.remove(i);
                lore.add(i, str);
                break;
            }
        }

        ItemStackUtils.setLore(itemStack, lore);
    }


    public static boolean hasPriceLore (ItemStack itemStack) {

        List<String> lore = ItemStackUtils.getLore(itemStack);
        if (lore == null) {
            return false;
        }

        if (lore.size() == 0)
            return false;

        String line1 = lore.get(0);

        return line1.indexOf(TEMP_INDICATOR + ChatColor.YELLOW + "Price: ") == 0;
    }


    public static void removePriceLore (ItemStack itemStack) {

        List<String> lore = ItemStackUtils.getLore(itemStack);
        if (lore == null) {
            return;
        }

        if (lore.size() == 0)
            return;

        String line1 = lore.get(0);

        if (line1.indexOf(TEMP_INDICATOR + ChatColor.YELLOW + "Price: ") != 0)
            return;

        lore.remove(0);

        ItemStackUtils.setLore(itemStack, lore);
    }


    public static void removePriceLore (final Inventory inventory, boolean runLater) {

        Runnable runnable = new Runnable() {

            @Override
            public void run () {

                ItemStack[] stacks = inventory.getContents();
                for (int i = 0; i < stacks.length; i++) {

                    ItemStack stack = stacks[i];

                    if (stack != null)
                        removePriceLore(stack);

                    inventory.setItem(i, stack);
                }
            }
        };

        if (runLater)
            Bukkit.getScheduler().runTaskLater(Storefront.getInstance(), runnable, 1);
        else
            runnable.run();

    }


    public static int addToInventory (SaleItem saleItem, Inventory inventory) {

        int qty = saleItem.getQty();
        if (qty == 0)
            return 0;

        int stacks = (int) Math.ceil((double) qty / 64);

        for (int i = 0; i < stacks; i++) {

            ItemStack stack = saleItem.getItemStack().clone();
            stack.setAmount(qty >= 64
                    ? 64
                    : qty);

            inventory.addItem(stack);

            qty -= 64;
        }

        return stacks;
    }


    public static AddToInventoryResult addToInventory (ItemStack itemStack, Inventory inventory) {

        ItemStack clone = itemStack.clone();
        removeTempLore(clone);

        ItemStack[] contents = inventory.getContents();

        int cloneQty = itemStack.getAmount();

        AddToInventoryResult result = new AddToInventoryResult();

        // find other stacks the item will fit into
        for (int slot = 0; slot < contents.length; slot++) {

            ItemStack item = contents[slot];

            if (item == null || item.getType() == Material.AIR)
                continue;

            ItemStack itemClone = item.clone();

            removeTempLore(itemClone);

            if (StoreStackMatcher.getDefault().isMatch(clone, itemClone)) {

                int itemQty = item.getAmount();
                int space = 64 - itemQty;

                if (space > 0) {

                    if (cloneQty < space) {
                        result.addSlotInfo(slot, cloneQty);
                        itemQty += cloneQty;
                        cloneQty = 0;
                    }
                    else {
                        result.addSlotInfo(slot, space);
                        itemQty = 64;
                        cloneQty -= space;
                    }

                    item.setAmount(itemQty);
                    inventory.setItem(slot, item);
                }
            }

            if (cloneQty <= 0)
                break;
        }

        // place remaining items in empty slots
        for (int slot = 0; slot < contents.length; slot++) {

            ItemStack item = contents[slot];

            if (item != null && item.getType() != Material.AIR)
                continue;

            ItemStack slotClone = itemStack.clone();

            if (cloneQty <= 64) {
                result.addSlotInfo(slot, cloneQty);
                slotClone.setAmount(cloneQty);
                cloneQty = 0;
            }
            else {
                result.addSlotInfo(slot, 64);
                cloneQty -= 64;
                slotClone.setAmount(64);
            }

            inventory.setItem(slot, slotClone);

            if (cloneQty <= 0)
                break;
        }

        result.setLeftOver(cloneQty);

        return result;
    }


    public static int getTotalSlots (SaleItem saleItem) {

        MaterialExt material = MaterialExt.from(saleItem.getItemStack().getType());
        
        int qty = saleItem.getQty();
        if (qty == 0)
            return 0;
        
        int maxStackSize = material != null
                ? material.getMaxStackSize()
                : 64;
                        
        return (int) Math.ceil((double) qty / maxStackSize);
    }


    public static int getTotalSlots (List<SaleItem> saleItems) {

        int total = 0;

        for (SaleItem saleItem : saleItems) {
            total += saleItem.getTotalSlots();
        }

        return total;
    }

    
    /**
     * 
     * @author JC The Pants
     *
     */
    public static class AddToInventoryResult {

        private int _leftover;
        private final List<SlotInfo> _slotsInfo = new ArrayList<>(6 * 9);


        void setLeftOver (int leftover) {

            _leftover = leftover;
        }


        void addSlotInfo (int slot, int added) {

            _slotsInfo.add(new SlotInfo(slot, added));
        }


        public int getLeftOver () {

            return _leftover;
        }


        public List<SlotInfo> getSlotsInfo () {

            return _slotsInfo;
        }

        public static class SlotInfo {

            private int _slot;
            private int _itemsAdded;


            SlotInfo(int slot, int itemsAdded) {

                _slot = slot;
                _itemsAdded = itemsAdded;
            }


            public int getSlot () {

                return _slot;
            }


            public int getItemsAdded () {

                return _itemsAdded;
            }
        }
    }

}
