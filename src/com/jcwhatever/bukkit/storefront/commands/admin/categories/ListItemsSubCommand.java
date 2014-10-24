package com.jcwhatever.bukkit.storefront.commands.admin.categories;

import java.util.Set;

import com.jcwhatever.bukkit.generic.items.ItemStackHelper;
import com.jcwhatever.bukkit.generic.items.ItemStackSerializer.SerializerOutputType;
import com.jcwhatever.bukkit.generic.messaging.ChatPaginator;
import org.bukkit.command.CommandSender;

import com.jcwhatever.bukkit.generic.commands.AbstractCommand;
import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.generic.items.ItemWrapper;
import com.jcwhatever.bukkit.storefront.Lang;
import com.jcwhatever.bukkit.generic.utils.TextUtils.FormatTemplate;
import com.jcwhatever.bukkit.storefront.Category;
import com.jcwhatever.bukkit.storefront.CategoryManager;
import com.jcwhatever.bukkit.storefront.Msg;
import com.jcwhatever.bukkit.storefront.Storefront;

@ICommandInfo(
        parent = "categories",
        command = "listitems",
        staticParams = {
                "categoryName", "page=1"
        },
        usage = "/stores categories listitems <categoryName> [page]",
        description = "List a categories filter items.")

public class ListItemsSubCommand extends AbstractCommand {

    @Override
    public void execute (CommandSender sender, CommandArguments args) throws InvalidValueException {

        int page = args.getInt("page");
        String categoryName = args.getName("categoryName");

        CategoryManager manager = Storefront.getInstance().getCategoryManager();

        Category category = manager.getCategory(categoryName);
        if (category == null) {
            String message = Lang.get("An item category with the name '{0}' was not found.", categoryName);
            tellError(sender, message);
            return; // finished
        }

        Set<ItemWrapper> wrappers = category.getFilterManager().getItems();

        String paginTitle = Lang.get("Filtered Items in Category '{0}'", category.getName());
        ChatPaginator pagin = Msg.getPaginator(paginTitle);

        String filterLabel = Lang.get("FILTER MODE");
        pagin.addFormatted(FormatTemplate.DEFINITION, filterLabel, category.getFilterManager().getMode().name());

        for (ItemWrapper wrapper : wrappers) {

            pagin.add(ItemStackHelper.serializeToString(wrapper.getItem(), SerializerOutputType.COLOR));
        }

        pagin.show(sender, page, FormatTemplate.RAW);
    }
}
