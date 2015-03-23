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


package com.jcwhatever.storefront.commands;

import org.bukkit.plugin.Plugin;

import com.jcwhatever.nucleus.commands.CommandDispatcher;
import com.jcwhatever.storefront.commands.admin.AddCommand;
import com.jcwhatever.storefront.commands.admin.DelCommand;
import com.jcwhatever.storefront.commands.admin.ListCommand;
import com.jcwhatever.storefront.commands.admin.SetOwnerCommand;
import com.jcwhatever.storefront.commands.admin.categories.CategoriesCommand;
import com.jcwhatever.storefront.commands.admin.regions.RegionsCommand;
import com.jcwhatever.storefront.commands.user.EntryMsgCommand;
import com.jcwhatever.storefront.commands.user.ExitMsgCommand;
import com.jcwhatever.storefront.commands.user.SetTitleCommand;

public class StorefrontCommandDispatcher extends CommandDispatcher {

    public StorefrontCommandDispatcher(Plugin plugin) {

        super(plugin);
    }


    @Override
    protected void registerCommands () {

        registerCommand(AddCommand.class);
        registerCommand(DelCommand.class);
        registerCommand(ListCommand.class);
        registerCommand(SetOwnerCommand.class);
        registerCommand(CategoriesCommand.class);
        registerCommand(RegionsCommand.class);

        registerCommand(SetTitleCommand.class);
        registerCommand(EntryMsgCommand.class);
        registerCommand(ExitMsgCommand.class);

    }

}
