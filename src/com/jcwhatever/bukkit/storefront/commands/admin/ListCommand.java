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


package com.jcwhatever.bukkit.storefront.commands.admin;

import com.jcwhatever.generic.commands.AbstractCommand;
import com.jcwhatever.generic.commands.CommandInfo;
import com.jcwhatever.generic.commands.arguments.CommandArguments;
import com.jcwhatever.generic.commands.exceptions.InvalidArgumentException;
import com.jcwhatever.generic.messaging.ChatPaginator;
import com.jcwhatever.bukkit.storefront.Lang;
import com.jcwhatever.generic.language.Localizable;
import com.jcwhatever.generic.utils.text.TextUtils.FormatTemplate;
import com.jcwhatever.bukkit.storefront.Msg;
import com.jcwhatever.bukkit.storefront.StoreManager;
import com.jcwhatever.bukkit.storefront.Storefront;
import com.jcwhatever.bukkit.storefront.stores.IStore;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandInfo(
        command = "list",
        staticParams = { "page=1" },
        description = "List stores.")

public class ListCommand extends AbstractCommand {

    @Localizable static final String _PAGINATOR_TITLE = "Stores";

    @Override
    public void execute (CommandSender sender, CommandArguments args) throws InvalidArgumentException {

        int page = args.getInteger("page");

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

        pagin.show(sender, page, FormatTemplate.LIST_ITEM_DESCRIPTION);
    }
}
