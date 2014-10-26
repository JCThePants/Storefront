/* This file is part of Storefront for Bukkit, licensed under the MIT License (MIT).
 *
 * Copyright (c) JCThePants (www.jcwhatever.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */


package com.jcwhatever.bukkit.storefront;

import com.jcwhatever.bukkit.generic.messaging.ChatPaginator;
import com.jcwhatever.bukkit.generic.messaging.ChatPaginator.PaginatorTemplate;
import com.jcwhatever.bukkit.generic.messaging.Messenger;
import com.jcwhatever.bukkit.generic.utils.TextUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

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
