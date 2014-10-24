package com.jcwhatever.bukkit.storefront;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.jcwhatever.bukkit.generic.items.ItemFilterManager;
import com.jcwhatever.bukkit.generic.items.ItemStackComparer;
import com.jcwhatever.bukkit.generic.storage.IDataNode;

public class Category {

    private String _name;
    private String _title;
    private String _description = "Click to view items in category.";

    private IDataNode _categoryNode;
    private ItemFilterManager _filterManager;

    private ItemStack _menuItem;

    private static final ItemStack DEFAULT_MENU_ITEM = new ItemStack(Material.STONE);


    Category(String name, IDataNode categoryNode) {

        _name = name;
        _categoryNode = categoryNode;
        _filterManager = new ItemFilterManager(Storefront.getInstance(),
                categoryNode.getNode("item-filter"), ItemStackComparer.COMPARE_TYPE);

        loadSettings();
    }


    public String getName () {

        return _name;
    }


    public String getTitle () {

        return _title;
    }


    public void setTitle (String title) {

        _title = title;
        _categoryNode.set("title", title);
        _categoryNode.saveAsync(null);
        onCategoryChange();
    }


    public String getDescription () {

        return _description;
    }


    public void setDescription (String description) {

        _description = description;
        _categoryNode.set("description", description);
        _categoryNode.saveAsync(null);
        onCategoryChange();
    }


    public ItemFilterManager getFilterManager () {

        return _filterManager;
    }


    public ItemStack getMenuItem () {

        return _menuItem != null
                ? _menuItem
                : DEFAULT_MENU_ITEM;
    }


    public void setMenuItem (ItemStack item) {

        _menuItem = item;
        _categoryNode.set("menu-item", item);
        _categoryNode.saveAsync(null);
        onCategoryChange();
    }


    private void loadSettings () {

        _title = _categoryNode.getString("title", _name);
        _description = _categoryNode.getString("description", _description);

        ItemStack[] stacks = _categoryNode.getItemStacks("menu-item");

        if (stacks != null && stacks.length > 0) {
            _menuItem = stacks[0];
        }
    }


    private void onCategoryChange () {

        Storefront.getInstance().getCategoryManager().onCategoryChange();
    }

}
