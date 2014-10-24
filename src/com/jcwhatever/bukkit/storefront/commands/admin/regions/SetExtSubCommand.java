package com.jcwhatever.bukkit.storefront.commands.admin.regions;

import com.jcwhatever.bukkit.generic.GenericsLib;
import com.jcwhatever.bukkit.generic.commands.AbstractCommand;
import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidCommandSenderException;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidCommandSenderException.CommandSenderType;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.generic.regions.ReadOnlyRegion;
import com.jcwhatever.bukkit.generic.utils.TextUtils;
import com.jcwhatever.bukkit.storefront.StoreManager;
import com.jcwhatever.bukkit.storefront.Storefront;
import com.jcwhatever.bukkit.storefront.stores.IStore;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@ICommandInfo(
        parent = "regions",
        command = "setext",
        staticParams = {
            "storeName", "regionName="
        },
        usage = "/stores regions setext <storeName> [regionName]",
        description = "Set the specified store region to the region you are standing in. If there is more than 1, specify with [regionName].")

public class SetExtSubCommand extends AbstractCommand {

    @Override
    public void execute (CommandSender sender, CommandArguments args)
            throws InvalidValueException, InvalidCommandSenderException {

        InvalidCommandSenderException.check(sender, CommandSenderType.PLAYER, 
                "Console has no location.");
        
        String storeName = args.getName("storeName");
        String regionName = args.getString("regionName");
        
        Player p = (Player)sender;
        
        StoreManager storeManager = Storefront.getInstance().getStoreManager();

        IStore store = storeManager.getStore(storeName);
        if (store == null) {
            tellError(sender, "A store with the name '{0}' was not found.", storeName);
            return; // finished
        }
        
        
        Set<ReadOnlyRegion> regions = GenericsLib.getRegionManager().getRegions(p.getLocation());
        
        if (regions.isEmpty()) {
            tellError(sender, "No jcGenerics region was found where you are standing.");
            return; // finished
        }
        
        if (regions.size() > 1 && regionName.isEmpty()) {
            tellError(sender, "More than one region was found. Please specify with one of the following region names:");
            
            List<String> regionNames = new ArrayList<String>(regions.size());
            for (ReadOnlyRegion region : regions) {
                regionNames.add(region.getName() + '(' + region.getPlugin().getName() + ')');
            }

            tell(sender, TextUtils.concat(regionNames, ", "));
            return; // finished
        }
        
        
        ReadOnlyRegion extRegion = null;
        
        for (ReadOnlyRegion region : regions) {
            
            if ((regions.size() == 1 && regionName.isEmpty()) ||
                    region.getName().equalsIgnoreCase(regionName)) {
                
            
                extRegion = region;
                break;
            }
        }
        
        if (extRegion == null) {
            tellError(sender, regionName.isEmpty()
                    ? "Could not find a region."
                    : "Could not fina a region where you are standing named '{0}'.", regionName);
            
            return; // finished
        }
        
        IStore extStore = extRegion.getMeta(IStore.class.getName()); 
        if (extStore != null) {
            tellError(sender, "Region '{0}' is already assigned to store named '{1}'.", extRegion.getName(), extStore.getName());
            return; // finished
        }

        store.setExternalRegion(extRegion);
        
        tellSuccess(sender, "Store '{0}' region set to region named '{1}' from plugin named '{2}'.", store.getName(), extRegion.getName(), extRegion.getPlugin().getName());
    }
}
