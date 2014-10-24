package com.jcwhatever.bukkit.storefront;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.inventory.ItemStack;

import com.jcwhatever.bukkit.generic.storage.IDataNode;
import com.jcwhatever.bukkit.storefront.utils.ItemStackUtil;

public class CategoryManager {

    private IDataNode _categoryNode;
    private Map<String, Category> _categoryMap = new HashMap<String, Category>();
    private List<Runnable> _onCategoryChangeSubscribers = new ArrayList<Runnable>();


    CategoryManager(IDataNode categoryNode) {

        _categoryNode = categoryNode;

        loadSettings();
    }


    public Category getCategory (ItemStack itemStack) {
        
        itemStack = itemStack.clone();
        
        ItemStackUtil.removeTempLore(itemStack);
        
        List<Category> categories = getCategories();

        for (Category category : categories) {
            if (category.getFilterManager().isValidItem(itemStack))
                return category;
        }

        return null;
    }


    public Category getCategory (String name) {

        name = name.toLowerCase();

        return _categoryMap.get(name);
    }


    public List<Category> getCategories () {

        return new ArrayList<Category>(_categoryMap.values());
    }


    public Category addCategory (String name) {

        name = name.toLowerCase();

        Category category = _categoryMap.get(name);
        if (category != null) {
            return null;
        }

        IDataNode catNode = _categoryNode.getNode(name);
        catNode.set("title", name);
        catNode.saveAsync(null);

        category = new Category(name, catNode);

        _categoryMap.put(name, category);

        onCategoryChange();

        return category;
    }


    public Category removeCategory (String name) {

        name = name.toLowerCase();

        Category category = _categoryMap.remove(name);

        if (category != null)
            _categoryNode.remove(name);

        onCategoryChange();

        return category;
    }


    public void addOnCategoryChangeSubscriber (Runnable runnable) {

        _onCategoryChangeSubscribers.add(runnable);
    }


    public void removeOnCategoryChangeSubscriber (Runnable runnable) {

        _onCategoryChangeSubscribers.remove(runnable);
    }


    void onCategoryChange () {

        for (Runnable runnable : _onCategoryChangeSubscribers) {
            runnable.run();
        }
    }


    private void loadSettings () {

        Set<String> categoryNames = _categoryNode.getSubNodeNames();

        if (categoryNames != null && !categoryNames.isEmpty()) {

            for (String categoryName : categoryNames) {

                IDataNode catNode = _categoryNode.getNode(categoryName);

                Category category = new Category(categoryName, catNode);

                _categoryMap.put(categoryName.toLowerCase(), category);

            }
        }
    }

}
