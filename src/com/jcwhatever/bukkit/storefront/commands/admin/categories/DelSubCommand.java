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
        command = "del",
        staticParams = {
            "categoryName"
        },
        usage = "/stores categories del <categoryName>",
        description = "Removes an item category.")

public class DelSubCommand extends AbstractCommand {

    @Override
    public void execute (CommandSender sender, CommandArguments args) throws InvalidValueException {

        String categoryName = args.getName("categoryName");

        CategoryManager catManager = Storefront.getInstance().getCategoryManager();

        Category category = catManager.getCategory(categoryName);
        if (category == null) {
            tellError(sender, "An item category with the name '{0}' was not found.", categoryName);
            return; // finished
        }

        category = catManager.removeCategory(categoryName);
        if (category == null) {
            tellError(sender, "Failed to remove category.");
            return; // finished
        }

        tellSuccess(sender, "Category '{0}' removed.", category.getName());
    }
}
