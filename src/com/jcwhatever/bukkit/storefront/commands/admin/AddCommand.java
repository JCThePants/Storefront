package com.jcwhatever.bukkit.storefront.commands.admin;

import org.bukkit.command.CommandSender;

import com.jcwhatever.bukkit.generic.commands.AbstractCommand;
import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.storefront.StoreManager;
import com.jcwhatever.bukkit.storefront.StoreType;
import com.jcwhatever.bukkit.storefront.Storefront;
import com.jcwhatever.bukkit.storefront.stores.IStore;

@ICommandInfo(
        command = "add",
        staticParams = {
                "storeName", "server|player_ownable"
        },
        usage = "/stores add <storeName> <storeType>",
        description = "Add a store.")

public class AddCommand extends AbstractCommand {

    @Override
    public void execute (CommandSender sender, CommandArguments args) throws InvalidValueException {

        String storeName = args.getName("storeName");
        StoreType type = args.getEnum("server|player_ownable", StoreType.class);

        StoreManager storeManager = Storefront.getInstance().getStoreManager();

        IStore store = storeManager.getStore(storeName);
        if (store != null) {
            tellError(sender, "A store with the name '{0}' already exists.", store.getName());
            return; // finished
        }

        store = storeManager.addStore(storeName, type);

        if (store == null) {
            tellError(sender, "Failed to create store.");
            return; // finished
        }

        tellSuccess(sender, "{0} store '{1}' added.", type.name(), store.getName());
    }
}
