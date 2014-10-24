package com.jcwhatever.bukkit.storefront.commands;

import org.bukkit.plugin.Plugin;

import com.jcwhatever.bukkit.generic.commands.AbstractCommandHandler;
import com.jcwhatever.bukkit.storefront.commands.admin.AddCommand;
import com.jcwhatever.bukkit.storefront.commands.admin.DelCommand;
import com.jcwhatever.bukkit.storefront.commands.admin.ListCommand;
import com.jcwhatever.bukkit.storefront.commands.admin.SetOwnerCommand;
import com.jcwhatever.bukkit.storefront.commands.admin.categories.CategoriesCommand;
import com.jcwhatever.bukkit.storefront.commands.admin.regions.RegionsCommand;
import com.jcwhatever.bukkit.storefront.commands.user.EntryMsgCommand;
import com.jcwhatever.bukkit.storefront.commands.user.ExitMsgCommand;
import com.jcwhatever.bukkit.storefront.commands.user.SetTitleCommand;

public class CommandHandler extends AbstractCommandHandler {

    public CommandHandler(Plugin plugin) {

        super(plugin);
    }


    @Override
    protected void registerCommands () {

        registerCommand(AddCommand.class);
        registerCommand(DelCommand.class);
        registerCommand(ListCommand.class);
        registerCommand(SetOwnerCommand.class);
        registerCommand(CategoriesCommand.class);
        registerCommand(RegionsCommand.class);

        registerCommand(SetTitleCommand.class);
        registerCommand(EntryMsgCommand.class);
        registerCommand(ExitMsgCommand.class);

    }

}
