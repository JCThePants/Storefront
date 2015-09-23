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


package com.jcwhatever.storefront.utils;

import com.jcwhatever.nucleus.managed.scheduler.Scheduler;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.utils.items.ItemStackUtils;
import com.jcwhatever.nucleus.utils.items.MatchableItem;
import com.jcwhatever.nucleus.utils.player.PlayerUtils;
import com.jcwhatever.storefront.Storefront;
import com.jcwhatever.storefront.data.SaleItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Static {@link org.bukkit.inventory.ItemStack} utilities.
 */
public class ItemStackUtil {

    private ItemStackUtil() {}

    private static final String TEMP_INDICATOR = ChatColor.BLACK.toString()
            + ChatColor.GRAY.toString();
    
    private static NumberFormat _format = DecimalFormat.getCurrencyInstance();
    private static StoreStackMatcher _durabilityComparer = StoreStackMatcher.getDurability();

    /**
     * Specifies what type of price to display.
     */
    public enum PriceType {
        /**
         * The price is per item.
         */
        PER_ITEM,
        /**
         * The price is the sum price of the quantity.
         */
        TOTAL
    }

    /**
     * Remove all temporary lore from an {@link org.bukkit.inventory.ItemStack}.
     *
     * @param itemStack  The {@link org.bukkit.inventory.ItemStack}.
     *
     * @return  A list of removed lore.
     */
    public static List<String> removeTempLore(ItemStack itemStack) {
        PreCon.notNull(itemStack);

        List<String> lore = ItemStackUtils.getLore(itemStack);
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

        ItemStackUtils.setLore(itemStack, newLore);

        return removed;
    }

    /**
     * Remove temp lore from all {@link org.bukkit.inventory.ItemStack}'s in
     * the specified {@link org.bukkit.inventory.Inventory}.
     *
     * @param inventory  The inventory to remove all item stack lore.
     * @param runLater   True to run the task on the next tick, false to run immediately..
     */
    public static void removeTempLore(final Inventory inventory, boolean runLater) {
        PreCon.notNull(inventory);

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

        if (runLater) {
            Scheduler.runTaskLater(Storefront.getPlugin(), runnable);
        } else {
            runnable.run();
        }
    }

    /**
     * Append temporary lore line to an {@link org.bukkit.inventory.ItemStack}.
     *
     * @param itemStack  The item stack to add the temp lore to.
     * @param text       The lore text to add.
     */
    public static void addTempLore(ItemStack itemStack, CharSequence text) {
        PreCon.notNull(itemStack);
        PreCon.notNull(text);

        List<String> lore = ItemStackUtils.getLore(itemStack);

        if (lore == null)
            lore = new ArrayList<>(5);

        int insertAt = 0;

        while (insertAt < lore.size()) {
            if (lore.get(insertAt).indexOf(TEMP_INDICATOR) != 0)
                break;

            insertAt++;
        }
        lore.add(insertAt, TEMP_INDICATOR + text);

        ItemStackUtils.setLore(itemStack, lore);
    }

    /**
     * Append a list of temporary lore to an {@link org.bukkit.inventory.ItemStack}.
     *
     * @param itemStack  The item stack to add the temp lore to.
     * @param newLore    The new lore text to add.
     */
    public static void addTempLore(ItemStack itemStack, List<? extends CharSequence> newLore) {
        PreCon.notNull(itemStack);
        PreCon.notNull(newLore);

        List<String> lore = ItemStackUtils.getLore(itemStack);

        if (lore == null)
            lore = new ArrayList<>(5);

        int insertAt = hasPriceLore(itemStack)
                ? 1
                : 0;

        for (int i = insertAt, j = 0; i < newLore.size() + insertAt; i++, j++) {
            lore.add(i, TEMP_INDICATOR + newLore.get(j));
        }

        ItemStackUtils.setLore(itemStack, lore);
    }

    /**
     * Append temporary lore to an {@link org.bukkit.inventory.ItemStack} that
     * indicates the items seller.
     *
     * @param itemStack  The item stack to append temp lore to.
     * @param sellerId   The ID of the seller.
     */
    public static void setSellerLore (ItemStack itemStack, UUID sellerId) {
        PreCon.notNull(itemStack);
        PreCon.notNull(sellerId);

        String playerName = PlayerUtils.getPlayerName(sellerId);
        if (playerName == null)
            playerName = "?";

        addTempLore(itemStack, ChatColor.YELLOW + "Seller: " + ChatColor.GRAY + playerName);
    }

    /**
     * Append or replace the temp lore on an {@link org.bukkit.inventory.ItemStack} that
     * indicates the price of the item.
     *
     * @param itemStack  The item stack to append temp lore to.
     * @param price      The price of the item.
     * @param priceType  The price type.
     */
    public static void setPriceLore (ItemStack itemStack, double price, PriceType priceType) {
        PreCon.notNull(itemStack);
        PreCon.notNull(priceType);

        List<String> lore = ItemStackUtils.getLore(itemStack);
        if (lore == null)
            lore = new ArrayList<>(5);


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
     * Change all {@link org.bukkit.inventory.ItemStack}'s in an {@link org.bukkit.inventory.Inventory} that
     * match the provided item stack type to the specified price.
     *
     * @param inventory  The inventory.
     * @param itemStack  The item stack.
     * @param price      The price to set.
     * @param runLater   True to run on the next tick, false to run immediately.
     */
    public static void setPriceLore (final Inventory inventory, final ItemStack itemStack,
                                     final double price, final PriceType priceType, boolean runLater) {
        PreCon.notNull(inventory);
        PreCon.notNull(itemStack);
        PreCon.notNull(priceType);

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

                    //noinspection EqualsBetweenInconvertibleTypes
                    if (wrapper.equals(item)) {
                        setPriceLore(item, price, priceType);
                    }
                    else if (priceLine != null) {
                        setPriceLoreLine(item, priceLine);
                    }

                    inventory.setItem(i, item);
                }

                setPriceLore(itemStack, price, priceType);

            }
        };

        if (runLater) {
            Scheduler.runTaskLater(Storefront.getPlugin(), runnable);
        } else {
            runnable.run();
        }
    }

    /**
     * Get the price lore set on an {@link org.bukkit.inventory.ItemStack}.
     *
     * @param itemStack  The item stack to check.
     *
     * @return The price lore text or null if not present.
     */
    @Nullable
    public static String getPriceLore (ItemStack itemStack) {
        PreCon.notNull(itemStack);

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

    /**
     * Determine if an {@link org.bukkit.inventory.ItemStack} has its price lore set.
     *
     * @param itemStack  The item stack to check.
     */
    public static boolean hasPriceLore (ItemStack itemStack) {
        PreCon.notNull(itemStack);

        List<String> lore = ItemStackUtils.getLore(itemStack);
        if (lore == null)
            return false;

        if (lore.size() == 0)
            return false;

        String line1 = lore.get(0);

        return line1.indexOf(TEMP_INDICATOR + ChatColor.YELLOW + "Price: ") == 0;
    }

    /**
     * Remove price lore from an {@link org.bukkit.inventory.ItemStack}.
     *
     * @param itemStack  The item stack to remove price lore from.
     */
    public static void removePriceLore (ItemStack itemStack) {
        PreCon.notNull(itemStack);

        List<String> lore = ItemStackUtils.getLore(itemStack);
        if (lore == null)
            return;

        if (lore.size() == 0)
            return;

        String line1 = lore.get(0);

        if (line1.indexOf(TEMP_INDICATOR + ChatColor.YELLOW + "Price: ") != 0)
            return;

        lore.remove(0);

        ItemStackUtils.setLore(itemStack, lore);
    }

    /**
     * Remove price lore from all {@link org.bukkit.inventory.ItemStack}'s in
     * the specified {@link org.bukkit.inventory.Inventory}.
     *
     * @param inventory  The inventory.
     * @param runLater   True to run the task on the next tick, false to run immediately.
     */
    public static void removePriceLore (final Inventory inventory, boolean runLater) {
        PreCon.notNull(inventory);

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

        if (runLater) {
            Scheduler.runTaskLater(Storefront.getPlugin(), runnable);
        } else {
            runnable.run();
        }
    }

    /**
     * Add an {@link org.bukkit.inventory.ItemStack} to an {@link org.bukkit.inventory.Inventory}.
     *
     * <p>The amount of the {@link org.bukkit.inventory.ItemStack} is considered
     * when adding to the inventory..</p>
     *
     * @param itemStack  The item stack to add.
     * @param inventory  The inventory to add the item to.
     *
     * @return  The results of the operation.
     */
    public static AddToInventoryResult addToInventory (ItemStack itemStack, Inventory inventory) {
        PreCon.notNull(itemStack);
        PreCon.notNull(inventory);

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

    /*
     * Append or set the price lore of an item stack.
     */
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


    public static int getTotalSlots (SaleItem saleItem) {

        Material material = saleItem.getItemStack().getType();
        
        int qty = saleItem.getQty();
        if (qty == 0)
            return 0;
        
        int maxStackSize = material != null
                ? material.getMaxStackSize()
                : 64;
                        
        return (int) Math.ceil((double) qty / maxStackSize);
    }

    /**
     * Results object when adding an item to an inventory.
     */
    public static class AddToInventoryResult {

        int leftover;
        final List<SlotInfo> slotsInfo = new ArrayList<>(6 * 9);

        /**
         * Get the number of items that were not added.
         */
        public int getLeftOver () {
            return leftover;
        }

        /**
         * Get info about the slots modified in the inventory.
         */
        public List<SlotInfo> getSlotsInfo () {
            return slotsInfo;
        }

        void setLeftOver (int leftover) {
            this.leftover = leftover;
        }

        void addSlotInfo (int slot, int added) {
            slotsInfo.add(new SlotInfo(slot, added));
        }

        /**
         * Contains information about a slot modified in an inventory.
         */
        public static class SlotInfo {

            private int slot;
            private int itemsAdded;

            SlotInfo(int slot, int itemsAdded) {

                this.slot = slot;
                this.itemsAdded = itemsAdded;
            }

            /**
             * Get the slot index.
             */
            public int getSlot() {
                return slot;
            }

            /**
             * Get the number of items added to the slot.
             */
            public int getItemsAdded() {
                return itemsAdded;
            }
        }
    }

}
