package com.jcwhatever.bukkit.storefront.commands.admin;

import com.jcwhatever.bukkit.generic.commands.AbstractCommand;
import com.jcwhatever.bukkit.generic.commands.ICommandInfo;
import com.jcwhatever.bukkit.generic.commands.arguments.CommandArguments;
import com.jcwhatever.bukkit.generic.commands.exceptions.InvalidValueException;
import com.jcwhatever.bukkit.generic.messaging.ChatPaginator;
import com.jcwhatever.bukkit.storefront.Lang;
import com.jcwhatever.bukkit.generic.language.Localizable;
import com.jcwhatever.bukkit.generic.utils.TextUtils.FormatTemplate;
import com.jcwhatever.bukkit.storefront.Msg;
import com.jcwhatever.bukkit.storefront.StoreManager;
import com.jcwhatever.bukkit.storefront.Storefront;
import com.jcwhatever.bukkit.storefront.stores.IStore;
import org.bukkit.command.CommandSender;

import java.util.List;

@ICommandInfo(
        command = "list",
        staticParams = {
            "page=1"
        },
        usage = "/stores list [page]",
        description = "List stores.")

public class ListCommand extends AbstractCommand {

    @Localizable static final String _PAGINATOR_TITLE = "Stores";

    @Override
    public void execute (CommandSender sender, CommandArguments args) throws InvalidValueException {

        int page = args.getInt("page");

        StoreManager manager = Storefront.getInstance().getStoreManager();

        List<IStore> stores = manager.getStores();

        ChatPaginator pagin = Msg.getPaginator(Lang.get(_PAGINATOR_TITLE));

        for (IStore store : stores) {

            String desc = store.getStoreType().name();

            if (!store.getName().equals(store.getTitle())) {
                desc += ", " + store.getTitle();
            }

            pagin.add(store.getName(), desc);
        }

        pagin.show(sender, page, FormatTemplate.ITEM_DESCRIPTION);
    }
}
