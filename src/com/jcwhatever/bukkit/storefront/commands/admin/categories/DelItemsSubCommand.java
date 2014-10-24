package com.jcwhatever.bukkit.storefront.commands.admin.categories;

import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import com.jcwhatever.bukkit.generic.commands.AbstractCommand;
import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.storefront.Category;
import com.jcwhatever.bukkit.storefront.CategoryManager;
import com.jcwhatever.bukkit.storefront.Storefront;

@ICommandInfo(
        parent = "categories",
        command = "delitems",
        staticParams = {
                "categoryName", "items"
        },
        usage = "/stores categories delitems <categoryName> <items>",
        description = "Remove filter items from a category.")

public class DelItemsSubCommand extends AbstractCommand {

    @Override
    public void execute (CommandSender sender, CommandArguments args) throws InvalidValueException {

        String categoryName = args.getName("categoryName");
        ItemStack[] items = args.getItemStack(sender, "items");

        CategoryManager catManager = Storefront.getInstance().getCategoryManager();

        Category category = catManager.getCategory(categoryName);
        if (category == null) {
            tellError(sender, "An item category with the name '{0}' was not found.", categoryName);
            return; // finished
        }

        if (!category.getFilterManager().removeItems(items)) {
            tellError(sender, "Failed to remove items from category.");
            return; // finished
        }

        tellSuccess(sender, "Filter items removed from category '{0}'.", category.getName());
    }
}
