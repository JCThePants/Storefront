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


package com.jcwhatever.storefront.commands.user;

import com.jcwhatever.nucleus.managed.commands.CommandInfo;
import com.jcwhatever.nucleus.managed.commands.arguments.ICommandArguments;
import com.jcwhatever.nucleus.managed.commands.exceptions.CommandException;
import com.jcwhatever.nucleus.managed.commands.mixins.IExecutableCommand;
import com.jcwhatever.nucleus.managed.commands.utils.AbstractCommand;
import com.jcwhatever.storefront.Storefront;
import com.jcwhatever.storefront.stores.IStore;
import com.jcwhatever.storefront.stores.StoreManager;
import com.jcwhatever.storefront.stores.StoreType;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.UUID;


@CommandInfo(
        command = "exitmsg",
        staticParams = { "storeName", "message=" },
        description = "Set the region exit message of a store. Omit message to remove.",
        permissionDefault = PermissionDefault.TRUE)

public class ExitMsgCommand extends AbstractCommand implements IExecutableCommand {

    @Override
    public void execute (CommandSender sender, ICommandArguments args) throws CommandException {

        String storeName = args.getName("storeName");
        String message = args.getString("message");

        StoreManager storeManager = Storefront.getStoreManager();

        IStore store = storeManager.get(storeName);
        if (store == null)
            throw new CommandException("A store with the name '{0}' was not found.", storeName);

        UUID playerId = null;
        
        if (sender instanceof Player) {
            playerId = ((Player) sender).getUniqueId();
        }
        
        if (store.getType() == StoreType.SERVER) {
            
            if (!sender.hasPermission("storefront.store.server"))
                throw new CommandException("You don't have permission to change the exit " +
                        "message of a Server store.");
        }
        else if (sender instanceof Player && (!store.hasOwner() || !store.getOwnerId().equals(playerId))) {
            
            if (!sender.isOp())
                throw new CommandException("You can only change the exit message of your own stores.");

            tell(sender, "Changing exit message as OP.");
        }

        store.getStoreRegion().setExitMessage(message.isEmpty() ? null : message);

        if (message.isEmpty())
            tellSuccess(sender, "Store '{0}' exit message removed.", store.getName());
        else
            tellSuccess(sender, "Store '{0}' exit message set to '{1}'.", store.getName(), message);
    }
}
