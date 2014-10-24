package com.jcwhatever.bukkit.storefront;

import com.jcwhatever.bukkit.generic.language.LanguageManager;
import com.jcwhatever.bukkit.generic.language.Localized;

public class Lang {

    private Lang() {}

    private static LanguageManager _languageManager = new LanguageManager();


    @Localized
    public static String get(String text, Object... params) {
        return _languageManager.get(Storefront.getInstance(), text, params);
    }

}
