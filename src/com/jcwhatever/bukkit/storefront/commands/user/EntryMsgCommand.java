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


package com.jcwhatever.bukkit.storefront.commands.user;

import com.jcwhatever.generic.commands.AbstractCommand;
import com.jcwhatever.generic.commands.CommandInfo;
import com.jcwhatever.generic.commands.arguments.CommandArguments;
import com.jcwhatever.generic.commands.exceptions.InvalidArgumentException;
import com.jcwhatever.bukkit.storefront.StoreManager;
import com.jcwhatever.bukkit.storefront.StoreType;
import com.jcwhatever.bukkit.storefront.Storefront;
import com.jcwhatever.bukkit.storefront.stores.IStore;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.UUID;

@CommandInfo(
        command = "entrymsg",
        staticParams = { "storeName", "message=" },
        description = "Set the region entry message of a store. Omit message to remove.",
        permissionDefault = PermissionDefault.TRUE)

public class EntryMsgCommand extends AbstractCommand {

    @Override
    public void execute (CommandSender sender, CommandArguments args) throws InvalidArgumentException {

        String storeName = args.getName("storeName");
        String message = args.getString("message");

        StoreManager storeManager = Storefront.getInstance().getStoreManager();

        IStore store = storeManager.getStore(storeName);
        if (store == null) {
            tellError(sender, "A store with the name '{0}' was not found.", storeName);
            return; // finished
        }

        UUID playerId = null;
        
        if (sender instanceof Player) {
            playerId = ((Player) sender).getUniqueId();
        }
        
        if (store.getStoreType() == StoreType.SERVER) {
            
            if (!sender.hasPermission("storefront.store.server")) {
                tellError(sender, "You don't have permission to change the entry message of a Server store.");
                return; // finished
            }
        }
        else if (sender instanceof Player && (!store.hasOwner() || !store.getOwnerId().equals(playerId))) {
            
            if (!sender.isOp()) {
                tellError(sender, "You can only change the entry message of your own stores.");
                return; // finished
            }
            
            tell(sender, "Changing entry message as OP.");
        }
        
        store.getStoreRegion().setEntryMessage(message.isEmpty() ? null : message);

        if (message.isEmpty())
            tellSuccess(sender, "Store '{0}' entry message removed.", store.getName());
        else
            tellSuccess(sender, "Store '{0}' entry message set to '{1}'.", store.getName(), message);
    }
}
