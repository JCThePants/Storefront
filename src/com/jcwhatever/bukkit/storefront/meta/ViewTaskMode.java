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