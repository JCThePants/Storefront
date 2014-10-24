package com.jcwhatever.bukkit.storefront.meta;

/**
 * Meta Keys for view instances persistent meta
 * 
 * @author JC The Pants
 *
 */
public enum SessionMetaKey {
	
	/**
	 * The current Task Mode whose value is a <code>ViewTaskMode</code> enum constant.
	 */
	TASK_MODE,
	
	/**
	 * The current store whose value is an an instance that implements <code>IStore</code>.
	 */
	STORE,
	
	/**
	 * The current category
	 */
	CATEGORY
}
