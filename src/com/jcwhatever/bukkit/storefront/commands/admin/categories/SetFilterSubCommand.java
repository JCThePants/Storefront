package com.jcwhatever.bukkit.storefront.commands.admin.categories;

import org.bukkit.command.CommandSender;

import com.jcwhatever.bukkit.generic.commands.AbstractCommand;
import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.generic.items.ItemFilterManager.FilterMode;
import com.jcwhatever.bukkit.storefront.Category;
import com.jcwhatever.bukkit.storefront.CategoryManager;
import com.jcwhatever.bukkit.storefront.Storefront;

@ICommandInfo(
        parent = "categories",
        command = "setfilter",
        staticParams = {
                "categoryName", "whitelist|blacklist"
        },
        usage = "/stores categories setfilter <categoryName> <whitelist|blacklist>",
        description = "Set category title.")

public class SetFilterSubCommand extends AbstractCommand {

    @Override
    public void execute (CommandSender sender, CommandArguments args) throws InvalidValueException {

        String categoryName = args.getName("categoryName");
        FilterMode filter = args.getEnum("whitelist|blacklist", FilterMode.class);

        CategoryManager catManager = Storefront.getInstance().getCategoryManager();

        Category category = catManager.getCategory(categoryName);
        if (category == null) {
            tellError(sender, "An item category with the name '{0}' was not found.", categoryName);
            return; // finished
        }

        category.getFilterManager().setMode(filter);

        tellSuccess(sender, "Category '{0}' filter mode changed to {1}.", category.getName(), filter.name());
    }
}
