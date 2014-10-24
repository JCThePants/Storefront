package com.jcwhatever.bukkit.storefront;

import java.util.Collection;
import java.util.UUID;

import com.jcwhatever.bukkit.generic.messaging.ChatPaginator;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.jcwhatever.bukkit.generic.messaging.Messenger;
import com.jcwhatever.bukkit.generic.messaging.ChatPaginator.PaginatorTemplate;
import com.jcwhatever.bukkit.generic.utils.TextUtils;

public class Msg {

    private Msg() {}


    public static void tell (CommandSender sender, String message, Object... params) {

        Messenger.tell(Storefront.getInstance(), sender, message, params);
    }


    public static void tell (Player p, String message, Object... params) {

        Messenger.tell(Storefront.getInstance(), p, message, params);
    }


    public static void tellNoSpam (Player p, String message, Object... params) {

        Messenger.tellNoSpam(Storefront.getInstance(), p, message, params);
    }


    public static void tellImportant (UUID playerId, String context, String message,
                                      Object... params) {

        Messenger.tellImportant(Storefront.getInstance(), playerId, context, message, params);
    }


    public static void info (String message, Object... params) {

        Messenger.info(Storefront.getInstance(), message, params);
    }


    public static void debug (String message, Object... params) {

        // if (!Storefront.getInstance().isDebugging())
        // return;
        Messenger.debug(Storefront.getInstance(), message, params);
    }


    public static void warning (String message, Object... params) {

        Messenger.warning(Storefront.getInstance(), message, params);
    }


    public static void severe (String message, Object... params) {

        Messenger.severe(Storefront.getInstance(), message, params);
    }


    public static void broadcast (String message, Object... params) {

        Messenger.broadcast(Storefront.getInstance(), message, params);
    }


    public static void broadcast (String message, Collection<Player> exclude, Object... params) {

        Messenger.broadcast(Storefront.getInstance(), message, exclude, params);
    }


    public static ChatPaginator getPaginator (String title, Object... params) {

        return new ChatPaginator(Storefront.getInstance(), 6, PaginatorTemplate.HEADER,
                PaginatorTemplate.FOOTER, TextUtils.format(title, params));
    }

}
