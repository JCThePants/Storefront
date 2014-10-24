package com.jcwhatever.bukkit.storefront.commands.admin.categories;

import java.util.List;

import com.jcwhatever.bukkit.generic.messaging.ChatPaginator;
import org.bukkit.command.CommandSender;

import com.jcwhatever.bukkit.generic.commands.AbstractCommand;
import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.storefront.Lang;
import com.jcwhatever.bukkit.generic.utils.TextUtils.FormatTemplate;
import com.jcwhatever.bukkit.storefront.Category;
import com.jcwhatever.bukkit.storefront.CategoryManager;
import com.jcwhatever.bukkit.storefront.Msg;
import com.jcwhatever.bukkit.storefront.Storefront;

@ICommandInfo(
        parent = "categories",
        command = "list",
        staticParams = {
            "page=1"
        },
        usage = "/stores categories list [page]",
        description = "List item categories.")

public class ListSubCommand extends AbstractCommand {

    @Override
    public void execute (CommandSender sender, CommandArguments args) throws InvalidValueException {

        int page = args.getInt("page");

        CategoryManager manager = Storefront.getInstance().getCategoryManager();

        List<Category> categories = manager.getCategories();

        String paginTitle = Lang.get("Item Categories");
        ChatPaginator pagin = Msg.getPaginator(paginTitle);

        for (Category category : categories) {

            String desc = "";

            if (!category.getName().equals(category.getTitle())) {
                desc += category.getTitle();
            }

            if (category.getDescription() != null && !category.getDescription().isEmpty()) {

                if (!desc.isEmpty())
                    desc += ": ";
                desc += category.getDescription();

            }

            pagin.add(category.getName(), desc);
        }

        pagin.show(sender, page, FormatTemplate.ITEM_DESCRIPTION);
    }
}
