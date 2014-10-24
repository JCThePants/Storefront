package com.jcwhatever.bukkit.storefront.commands.admin.categories;

import org.bukkit.command.CommandSender;

import com.jcwhatever.bukkit.generic.commands.AbstractCommand;
import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.storefront.Category;
import com.jcwhatever.bukkit.storefront.CategoryManager;
import com.jcwhatever.bukkit.storefront.Storefront;

@ICommandInfo(
        parent = "categories",
        command = "add",
        staticParams = {
            "categoryName"
        },
        usage = "/stores categories add <categoryName>",
        description = "Add an item category.")

public class AddSubCommand extends AbstractCommand {

    @Override
    public void execute (CommandSender sender, CommandArguments args)
            throws InvalidValueException {

        String categoryName = args.getName("categoryName");

        CategoryManager catManager = Storefront.getInstance().getCategoryManager();

        Category category = catManager.getCategory(categoryName);
        if (category != null) {
            tellError(sender, "An item category with the name '{0}' already exists.", category.getName());
            return; // finished
        }

        category = catManager.addCategory(categoryName);
        if (category == null) {
            tellError(sender, "Failed to create category.");
            return; // finished
        }

        tellSuccess(sender, "Category '{0}' added.", category.getName());
    }
}
