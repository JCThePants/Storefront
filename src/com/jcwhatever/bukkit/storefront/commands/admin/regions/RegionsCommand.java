package com.jcwhatever.bukkit.storefront.commands.admin.regions;

import com.jcwhatever.bukkit.generic.commands.AbstractCommand;
import com.jcwhatever.bukkit.generic.commands.ICommandInfo;

@ICommandInfo(
        command = "regions",
        description = "Manage store regions.")

public class RegionsCommand extends AbstractCommand {

    public RegionsCommand() {

        super();

        registerSubCommand(SetSubCommand.class);
        registerSubCommand(SetExtSubCommand.class);
    }
}
