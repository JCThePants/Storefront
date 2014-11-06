/* This file is part of Storefront for Bukkit, licensed under the MIT License (MIT).
 *
 * Copyright (c) JCThePants (www.jcwhatever.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */


package com.jcwhatever.bukkit.storefront.commands.admin.categories;

import com.jcwhatever.bukkit.generic.commands.AbstractCommand;
import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.generic.items.ItemStackHelper;
import com.jcwhatever.bukkit.generic.items.ItemStackSerializer.SerializerOutputType;
import com.jcwhatever.bukkit.generic.items.ItemWrapper;
import com.jcwhatever.bukkit.generic.messaging.ChatPaginator;
import com.jcwhatever.bukkit.generic.utils.TextUtils.FormatTemplate;
import com.jcwhatever.bukkit.storefront.Category;
import com.jcwhatever.bukkit.storefront.CategoryManager;
import com.jcwhatever.bukkit.storefront.Lang;
import com.jcwhatever.bukkit.storefront.Msg;
import com.jcwhatever.bukkit.storefront.Storefront;
import org.bukkit.command.CommandSender;

import java.util.Set;

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

        int page = args.getInteger("page");
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
