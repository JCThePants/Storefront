package com.jcwhatever.bukkit.storefront.commands.user;

import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import com.jcwhatever.bukkit.generic.commands.AbstractCommand;
import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.storefront.StoreManager;
import com.jcwhatever.bukkit.storefront.StoreType;
import com.jcwhatever.bukkit.storefront.Storefront;
import com.jcwhatever.bukkit.storefront.stores.IStore;


@ICommandInfo(
        command = "exitmsg",
        staticParams = {
                "storeName", "message="
        },
        usage = "/stores exitmsg <storeName> <message>",
        description = "Set the region exit message of a store. Omit message to remove.",
        permissionDefault = PermissionDefault.TRUE)

public class ExitMsgCommand extends AbstractCommand {

    @Override
    public void execute (CommandSender sender, CommandArguments args) throws InvalidValueException {

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
                tellError(sender, "You don't have permission to change the exit message of a Server store.");
                return; // finished
            }
        }
        else if (sender instanceof Player && (!store.hasOwner() || !store.getOwnerId().equals(playerId))) {
            
            if (!sender.isOp()) {
                tellError(sender, "You can only change the exit message of your own stores.");
                return; // finished
            }
            
            tell(sender, "Changing exit message as OP.");
        }
        
        
        store.getStoreRegion().setExitMessage(Storefront.getInstance(), message.isEmpty() ? null : message);

        if (message.isEmpty())
            tellSuccess(sender, "Store '{0}' exit message removed.", store.getName());
        else
            tellSuccess(sender, "Store '{0}' exit message set to '{1}'.", store.getName(), message);
    }
}
