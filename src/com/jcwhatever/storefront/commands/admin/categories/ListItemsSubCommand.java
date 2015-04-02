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


package com.jcwhatever.storefront.commands.admin.categories;

import com.jcwhatever.nucleus.commands.AbstractCommand;
import com.jcwhatever.nucleus.commands.CommandInfo;
import com.jcwhatever.nucleus.commands.arguments.CommandArguments;
import com.jcwhatever.nucleus.commands.exceptions.InvalidArgumentException;
import com.jcwhatever.nucleus.utils.items.ItemStackUtils;
import com.jcwhatever.nucleus.utils.items.MatchableItem;
import com.jcwhatever.nucleus.utils.items.serializer.ItemStackSerializer.SerializerOutputType;
import com.jcwhatever.nucleus.messaging.ChatPaginator;
import com.jcwhatever.nucleus.utils.text.TextUtils.FormatTemplate;
import com.jcwhatever.storefront.category.Category;
import com.jcwhatever.storefront.category.CategoryManager;
import com.jcwhatever.storefront.Lang;
import com.jcwhatever.storefront.Msg;
import com.jcwhatever.storefront.Storefront;

import org.bukkit.command.CommandSender;

import java.util.Set;

@CommandInfo(
        parent = "categories",
        command = "listitems",
        staticParams = { "categoryName", "page=1" },
        floatingParams = { "search=" },
        description = "List a categories filter items.")

public class ListItemsSubCommand extends AbstractCommand {

    @Override
    public void execute (CommandSender sender, CommandArguments args) throws InvalidArgumentException {

        int page = args.getInteger("page");
        String categoryName = args.getName("categoryName");

        CategoryManager manager = Storefront.getCategoryManager();

        Category category = manager.get(categoryName);
        if (category == null) {
            String message = Lang.get("An item category with the name '{0}' was not found.", categoryName);
            tellError(sender, message);
            return; // finished
        }

        Set<MatchableItem> wrappers = category.getFilterManager().getMatchable();

        String paginTitle = Lang.get("Filtered Items in Category '{0}'", category.getName());
        ChatPaginator pagin = Msg.getPaginator(paginTitle);

        String filterLabel = Lang.get("FILTER MODE");
        pagin.addFormatted(FormatTemplate.CONSTANT_DEFINITION, filterLabel, category.getFilterManager().getPolicy().name());

        for (MatchableItem wrapper : wrappers) {

            pagin.add(ItemStackUtils.serialize(wrapper.getItem(), SerializerOutputType.COLOR));
        }

        if (!args.isDefaultValue("search"))
            pagin.setSearchTerm(args.getString("search"));

        pagin.show(sender, page, FormatTemplate.RAW);
    }
}
