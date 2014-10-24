package com.jcwhatever.bukkit.storefront.commands.admin.categories;

import com.jcwhatever.bukkit.generic.commands.AbstractCommand;
import com.jcwhatever.bukkit.generic.commands.ICommandInfo;

@ICommandInfo(
        command = "categories",
        description = "Manage item categories.")

public class CategoriesCommand extends AbstractCommand {

    public CategoriesCommand() {

        super();

        registerSubCommand(AddItemsSubCommand.class);
        registerSubCommand(AddSubCommand.class);
        registerSubCommand(ClearItemsSubCommand.class);
        registerSubCommand(DelItemsSubCommand.class);
        registerSubCommand(DelSubCommand.class);
        registerSubCommand(ListSubCommand.class);
        registerSubCommand(ListItemsSubCommand.class);
        registerSubCommand(SetDescSubCommand.class);
        registerSubCommand(SetFilterSubCommand.class);
        registerSubCommand(SetItemSubCommand.class);
        registerSubCommand(SetTitleSubCommand.class);
    }
}
