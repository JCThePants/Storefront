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

import com.jcwhatever.nucleus.commands.AbstractCommand;
import com.jcwhatever.nucleus.commands.CommandInfo;

@CommandInfo(
        command = "categories",
        description = "Manage item categories.")

public class CategoriesCommand extends AbstractCommand {

    public CategoriesCommand() {

        super();

        registerCommand(AddItemsSubCommand.class);
        registerCommand(AddSubCommand.class);
        registerCommand(ClearItemsSubCommand.class);
        registerCommand(DelItemsSubCommand.class);
        registerCommand(DelSubCommand.class);
        registerCommand(ListSubCommand.class);
        registerCommand(ListItemsSubCommand.class);
        registerCommand(SetDescSubCommand.class);
        registerCommand(SetFilterSubCommand.class);
        registerCommand(SetItemSubCommand.class);
        registerCommand(SetTitleSubCommand.class);
    }
}
