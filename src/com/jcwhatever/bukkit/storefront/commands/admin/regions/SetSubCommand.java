package com.jcwhatever.bukkit.storefront.commands.admin.regions;

import com.jcwhatever.bukkit.generic.commands.AbstractCommand;
import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidCommandSenderException;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidCommandSenderException.CommandSenderType;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.generic.regions.RegionSelection;
import com.jcwhatever.bukkit.storefront.StoreManager;
import com.jcwhatever.bukkit.storefront.Storefront;
import com.jcwhatever.bukkit.storefront.stores.IStore;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@ICommandInfo(
        parent = "regions",
        command = "set",
        staticParams = {
            "storeName"
        },
        usage = "/stores regions set <storeName>",
        description = "Set the specified store region to the current WorldEdit selection.")

public class SetSubCommand extends AbstractCommand {

    @Override
    public void execute (CommandSender sender, CommandArguments args)
            throws InvalidValueException, InvalidCommandSenderException {

        InvalidCommandSenderException.check(sender, CommandSenderType.PLAYER, 
                "Console cannot select regions.");

        if (!isWorldEditInstalled(sender))
            return; // finished

        String storeName = args.getName("storeName");

        StoreManager storeManager = Storefront.getInstance().getStoreManager();

        IStore store = storeManager.getStore(storeName);
        if (store == null) {
            tellError(sender, "A store with the name '{0}' was not found.", storeName);
            return; // finished
        }
        
        RegionSelection selection = getWorldEditSelection((Player) sender);
        if (selection == null)
            return; // finished

        store.setRegionCoords(selection.getP1(), selection.getP2());

        tellSuccess(sender, "Store '{0}' region set to current WorldEdit selection.", store.getName());
    }
}
