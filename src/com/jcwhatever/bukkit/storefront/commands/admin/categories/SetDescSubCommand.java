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

import com.jcwhatever.generic.commands.AbstractCommand;
import com.jcwhatever.generic.commands.CommandInfo;
import com.jcwhatever.generic.commands.arguments.CommandArguments;
import com.jcwhatever.generic.commands.exceptions.InvalidArgumentException;
import com.jcwhatever.bukkit.storefront.Category;
import com.jcwhatever.bukkit.storefront.CategoryManager;
import com.jcwhatever.bukkit.storefront.Storefront;

import org.bukkit.command.CommandSender;

@CommandInfo(
        parent = "categories",
        command = "setdesc",
        staticParams = { "categoryName", "description" },
        usage = "/stores categories setdesc <categoryName> <description>",
        description = "Set category description.")

public class SetDescSubCommand extends AbstractCommand {

    @Override
    public void execute (CommandSender sender, CommandArguments args) throws InvalidArgumentException {

        String categoryName = args.getName("categoryName");
        String description = args.getString("description");

        CategoryManager catManager = Storefront.getInstance().getCategoryManager();

        Category category = catManager.getCategory(categoryName);
        if (category == null) {
            tellError(sender, "An item category with the name '{0}' was not found.", categoryName);
            return; // finished
        }

        category.setDescription(description);

        tellSuccess(sender, "Category '{0}' description changed to '{1}'.", category.getName(), description);
    }
}
