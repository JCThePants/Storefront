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


package com.jcwhatever.bukkit.storefront.meta;

import org.bukkit.ChatColor;

public enum ViewTaskMode {
	SERVER_BUY (false, ChatColor.DARK_BLUE, BasicTask.BUY),
	SERVER_SELL (false, ChatColor.DARK_GRAY, BasicTask.SELL),

	PLAYER_BUY (false, ChatColor.DARK_BLUE, BasicTask.BUY),
	PLAYER_SELL (false, ChatColor.DARK_GRAY, BasicTask.SELL),

	OWNER_MANAGE_BUY (true, ChatColor.DARK_GREEN, BasicTask.BUY),
	OWNER_MANAGE_SELL (true, ChatColor.DARK_GRAY, BasicTask.SELL);

	public enum BasicTask {
		BUY,
		SELL
	}

	private final BasicTask _basicTask;
	private final boolean _isOwnerManagerTask;
	private ChatColor _chatColor;

	ViewTaskMode (boolean isOwnerManagerTask, ChatColor chatColor, BasicTask pureTask) {
		_basicTask = pureTask;
		_isOwnerManagerTask = isOwnerManagerTask;
		_chatColor = chatColor;
	}

	public BasicTask getBasicTask() {
		return _basicTask;
	}

	public boolean isOwnerManagerTask() {
		return _isOwnerManagerTask;
	}
	
	public ChatColor getChatColor() {
	    return _chatColor;
	}
	
	public void setChatColor(ChatColor chatColor) {
	    _chatColor = chatColor;
	}
}