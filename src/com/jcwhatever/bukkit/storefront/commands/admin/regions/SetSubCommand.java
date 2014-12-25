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


package com.jcwhatever.bukkit.storefront.commands.admin.regions;

import com.jcwhatever.generic.commands.AbstractCommand;
import com.jcwhatever.generic.commands.CommandInfo;
import com.jcwhatever.generic.commands.arguments.CommandArguments;
import com.jcwhatever.generic.commands.exceptions.CommandException;
import com.jcwhatever.generic.regions.selection.IRegionSelection;
import com.jcwhatever.bukkit.storefront.StoreManager;
import com.jcwhatever.bukkit.storefront.Storefront;
import com.jcwhatever.bukkit.storefront.stores.IStore;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandInfo(
        parent = "regions",
        command = "set",
        staticParams = { "storeName" },
        description = "Set the specified store region to your current region selection.")

public class SetSubCommand extends AbstractCommand {

    @Override
    public void execute (CommandSender sender, CommandArguments args) throws CommandException {

        CommandException.assertNotConsole(this, sender);

        String storeName = args.getName("storeName");

        StoreManager storeManager = Storefront.getInstance().getStoreManager();

        IStore store = storeManager.getStore(storeName);
        if (store == null) {
            tellError(sender, "A store with the name '{0}' was not found.", storeName);
            return; // finished
        }
        
        IRegionSelection selection = getRegionSelection((Player) sender);
        if (selection == null)
            return; // finished

        store.setRegionCoords(selection.getP1(), selection.getP2());

        tellSuccess(sender, "Store '{0}' region set to current region selection.", store.getName());
    }
}
