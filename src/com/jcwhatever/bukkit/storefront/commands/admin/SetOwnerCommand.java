package com.jcwhatever.bukkit.storefront.commands.admin;

import java.util.UUID;

import org.bukkit.command.CommandSender;

import com.jcwhatever.bukkit.generic.commands.AbstractCommand;
import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.generic.player.PlayerHelper;
import com.jcwhatever.bukkit.storefront.StoreManager;
import com.jcwhatever.bukkit.storefront.StoreType;
import com.jcwhatever.bukkit.storefront.Storefront;
import com.jcwhatever.bukkit.storefront.stores.IStore;

@ICommandInfo(
        command = "setowner",
        staticParams = {
                "storeName", "ownerName"
        },
        usage = "/stores setowner <storeName> <ownerName>",
        description = "Set the owner of the store")

public class SetOwnerCommand extends AbstractCommand {

    @Override
    public void execute (CommandSender sender, CommandArguments args) throws InvalidValueException {

        String storeName = args.getName("storeName");
        String ownerName = args.getName("ownerName");

        StoreManager storeManager = Storefront.getInstance().getStoreManager();

        IStore store = storeManager.getStore(storeName);
        if (store == null) {
            tellError(sender, "A store with the name '{0}' was not found.", storeName);
            return; // finished
        }

        if (store.getStoreType() == StoreType.SERVER) {
            tellError(sender, "The store named '{0}' is a Server store and cannot have an owner.", store.getName());
            return; // finished
        }

        UUID ownerId = PlayerHelper.getPlayerId(ownerName);
        if (ownerId == null) {
            tellError(sender, "A player named '{0}' could not be found or has never logged into the server before.", ownerName);
            return; // finished
        }

        store.setOwnerId(ownerId);

        tellSuccess(sender, "Store '{0}' owner set to player '{1}'.", store.getName(), ownerName);
    }
}
