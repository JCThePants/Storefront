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
        command = "settitle",
        staticParams = {
                "categoryName", "title"
        },
        usage = "/stores categories settitle <categoryName> <title>",
        description = "Set category title.")

public class SetTitleSubCommand extends AbstractCommand {

    @Override
    public void execute (CommandSender sender, CommandArguments args) throws InvalidValueException {

        String categoryName = args.getName("categoryName");
        String title = args.getString("title");

        CategoryManager catManager = Storefront.getInstance().getCategoryManager();

        Category category = catManager.getCategory(categoryName);
        if (category == null) {
            tellError(sender, "An item category with the name '{0}' was not found.", categoryName);
            return; // finished
        }

        category.setTitle(title);

        tellSuccess(sender, "Category '{0}' title changed to '{1}'.", category.getName(), title);
    }
}
