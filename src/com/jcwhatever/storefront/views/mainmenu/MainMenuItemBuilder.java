package com.jcwhatever.storefront.views.mainmenu;

import com.jcwhatever.storefront.data.ISaleItemGetter;
import com.jcwhatever.storefront.meta.ViewSessionTask;
import com.jcwhatever.nucleus.utils.PreCon;
import com.jcwhatever.nucleus.views.menu.MenuItemBuilder;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Utility class to create new instances of {@link MainMenuItem}.
 */
public class MainMenuItemBuilder extends MenuItemBuilder {

    private ViewSessionTask _task;
    private ISaleItemGetter _saleItemGetter;
    private boolean _isCategorized;

    /**
     * Constructor.
     *
     * @param itemStack  The {@link org.bukkit.inventory.ItemStack} that represents
     *                   the menu item.
     */
    public MainMenuItemBuilder(ItemStack itemStack) {
        super(itemStack);
    }

    /**
     * Constructor.
     *
     * @param material  The menu item {@link org.bukkit.Material}.
     */
    public MainMenuItemBuilder(Material material) {
        super(material);
    }

    /**
     * Constructor.
     *
     * @param materialData  The menu item {@link org.bukkit.material.MaterialData}.
     */
    public MainMenuItemBuilder(MaterialData materialData) {
        super(materialData);
    }

    /**
     * Constructor.
     *
     * @param materialName  The name of the menu item material.
     */
    public MainMenuItemBuilder(String materialName) {
        super(materialName);
    }

    /**
     * Set the task mode.
     *
     * @param task  The view session task.
     *
     * @return  Self for chaining.
     */
    public MainMenuItemBuilder task(ViewSessionTask task) {
        PreCon.notNull(task);

        _task = task;

        return this;
    }

    /**
     * Set the sale item getter.
     *
     * @param getter  The {@link ISaleItemGetter} used to get sale items
     *                to show.
     *
     * @return  Self for chaining.
     */
    public MainMenuItemBuilder saleItems(ISaleItemGetter getter) {
        PreCon.notNull(getter);

        _saleItemGetter = getter;

        return this;
    }

    /**
     * Flag the {@link com.jcwhatever.storefront.data.ISaleItem}'s shown
     * as categorized.
     *
     * @return  Self for chaining.
     */
    public MainMenuItemBuilder categorized() {
        _isCategorized = true;

        return this;
    }

    /**
     * Build and return a new {@link MainMenuItem}.
     *
     * @param slot  The inventory slot the menu item will be placed in.
     */
    @Override
    public MainMenuItem build(int slot) {

        MainMenuItem menuItem = (MainMenuItem)super.build(slot);

        if (_task != null)
            menuItem.setTask(_task);

        if (_saleItemGetter != null)
            menuItem.setSaleItems(_saleItemGetter);

        if (_isCategorized)
            menuItem.setCategorized(true);

        return menuItem;
    }

    @Override
    protected MainMenuItem createMenuItem(int slot, ItemStack itemStack,
                                      @Nullable Map<Object, Object> meta,
                                      @Nullable List<Runnable> onClick) {

        MainMenuItem item = new MainMenuItem(slot, itemStack);

        if (meta != null)
            item.getMeta().copyAll(meta);

        if (onClick != null) {
            for (Runnable runnable : onClick)
                item.onClick(runnable);
        }

        return item;
    }
}
