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
        command = "settitle",
        staticParams = {
                "storeName", "title"
        },
        usage = "/stores settitle <storeName> <ownerName>",
        description = "Set the title of a store.",
        permissionDefault = PermissionDefault.TRUE)

public class SetTitleCommand extends AbstractCommand {

    @Override
    public void execute (CommandSender sender, CommandArguments args) throws InvalidValueException {

        String storeName = args.getName("storeName");
        String title = args.getString("title");

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
                tellError(sender, "You don't have permission to change the title of a Server store.");
                return; // finished
            }
        }
        else if (sender instanceof Player && (!store.hasOwner() || !store.getOwnerId().equals(playerId))) {
            
            if (!sender.isOp()) {
                tellError(sender, "You can only change the title of your own stores.");
                return; // finished
            }
            
            tell(sender, "Changing title as OP.");
        }
        
        store.setTitle(title);

        tellSuccess(sender, "Store '{0}' title set to '{1}'.", store.getName(), title);
    }
}