package BMG.banManagerGUI.utils;

import org.bukkit.ChatColor;

public class ColorUtil {
    
    public static String stripColors(String text) {
        if (text == null) return null;
        return ChatColor.stripColor(text);
    }
    
    public static String translateColorCodes(String text) {
        if (text == null) return null;
        return ChatColor.translateAlternateColorCodes('&', text);
    }
    
    public static String stripForConsole(String text) {
        if (text == null) return null;
        return text.replaceAll("§[0-9a-fk-or]", "");
    }
}