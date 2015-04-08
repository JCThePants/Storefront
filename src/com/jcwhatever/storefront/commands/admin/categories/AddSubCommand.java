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

import com.jcwhatever.nucleus.managed.commands.CommandInfo;
import com.jcwhatever.nucleus.managed.commands.arguments.ICommandArguments;
import com.jcwhatever.nucleus.managed.commands.exceptions.CommandException;
import com.jcwhatever.nucleus.managed.commands.mixins.IExecutableCommand;
import com.jcwhatever.nucleus.managed.commands.utils.AbstractCommand;
import com.jcwhatever.storefront.Storefront;
import com.jcwhatever.storefront.category.Category;
import com.jcwhatever.storefront.category.CategoryManager;

import org.bukkit.command.CommandSender;

@CommandInfo(
        parent = "categories",
        command = "add",
        staticParams = { "categoryName" },
        description = "Add an item category.")

public class AddSubCommand extends AbstractCommand implements IExecutableCommand {

    @Override
    public void execute (CommandSender sender, ICommandArguments args)
            throws CommandException {

        String categoryName = args.getName("categoryName");

        CategoryManager catManager = Storefront.getCategoryManager();

        Category category = catManager.get(categoryName);
        if (category != null)
            throw new CommandException("An item category with the name '{0}' already exists.", category.getName());

        category = catManager.add(categoryName);
        if (category == null)
            throw new CommandException("Failed to create category.");

        tellSuccess(sender, "Category '{0}' added.", category.getName());
    }
}
