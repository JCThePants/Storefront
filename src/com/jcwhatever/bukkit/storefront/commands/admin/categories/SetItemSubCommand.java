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
        command = "setitem",
        staticParams = {
                "categoryName", "item"
        },
        usage = "/stores categories setitem <categoryName> <item>",
        description = "Set item used to represent category in inventory menus.")

public class SetItemSubCommand extends AbstractCommand {

    @Override
    public void execute (CommandSender sender, CommandArguments args) throws InvalidValueException {

        String categoryName = args.getName("categoryName");
        ItemStack[] items = args.getItemStack(sender, "item");

        CategoryManager catManager = Storefront.getInstance().getCategoryManager();

        Category category = catManager.getCategory(categoryName);
        if (category == null) {
            tellError(sender, "An item category with the name '{0}' was not found.", categoryName);
            return; // finished
        }

        if (items.length != 1) {
            tellError(sender, "Only 1 item can be set.");
            return; // finished
        }

        category.setMenuItem(items[0]);

        tellSuccess(sender, "Category '{0}' item changed.", category.getName());
    }
}
