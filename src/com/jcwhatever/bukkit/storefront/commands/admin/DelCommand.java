package com.jcwhatever.bukkit.storefront.commands.admin;

import org.bukkit.command.CommandSender;

import com.jcwhatever.bukkit.generic.commands.AbstractCommand;
import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.storefront.StoreManager;
import com.jcwhatever.bukkit.storefront.Storefront;
import com.jcwhatever.bukkit.storefront.stores.IStore;

@ICommandInfo(
        command = "del",
        staticParams = {
            "storeName"
        },
        usage = "/stores del <storeName>",
        description = "Remove a store.")

public class DelCommand extends AbstractCommand {

    @Override
    public void execute (CommandSender sender, CommandArguments args) throws InvalidValueException {

        String storeName = args.getName("storeName");

        StoreManager storeManager = Storefront.getInstance().getStoreManager();

        IStore store = storeManager.getStore(storeName);
        if (store == null) {
            tellError(sender, "A store with the name '{0}' was not found.", storeName);
            return; // finished
        }

        if (!storeManager.removeStore(storeName)) {
            tellError(sender, "Failed to remove store.");
            return; // finished
        }

        tellSuccess(sender, "Store '{0}' removed.", storeName);
    }
}
