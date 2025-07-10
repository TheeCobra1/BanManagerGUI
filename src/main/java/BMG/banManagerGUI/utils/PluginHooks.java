package BMG.banManagerGUI.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class PluginHooks {
    private boolean vaultEnabled = false;
    private boolean luckPermsEnabled = false;
    private boolean essentialsEnabled = false;
    private boolean placeholderAPIEnabled = false;
    private boolean discordSRVEnabled = false;
    
    public PluginHooks() {
        checkPlugins();
    }
    
    private void checkPlugins() {
        vaultEnabled = isPluginEnabled("Vault");
        luckPermsEnabled = isPluginEnabled("LuckPerms");
        essentialsEnabled = isPluginEnabled("Essentials") || isPluginEnabled("EssentialsX");
        placeholderAPIEnabled = isPluginEnabled("PlaceholderAPI");
        discordSRVEnabled = isPluginEnabled("DiscordSRV");
    }
    
    private boolean isPluginEnabled(String pluginName) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        return plugin != null && plugin.isEnabled();
    }
    
    public boolean hasVault() {
        return vaultEnabled;
    }
    
    public boolean hasLuckPerms() {
        return luckPermsEnabled;
    }
    
    public boolean hasEssentials() {
        return essentialsEnabled;
    }
    
    public boolean hasPlaceholderAPI() {
        return placeholderAPIEnabled;
    }
    
    public boolean hasDiscordSRV() {
        return discordSRVEnabled;
    }
    
    public String getIntegrationStatus() {
        StringBuilder status = new StringBuilder();
        
        String reset = "\u001B[0m";
        String bold = "\u001B[1m";
        String green = "\u001B[92m";
        String red = "\u001B[91m";
        String yellow = "\u001B[93m";
        String white = "\u001B[97m";
        String gray = "\u001B[90m";
        
        status.append("\n");
        status.append(bold).append(yellow).append("  Plugin Integrations").append(reset).append("\n");
        status.append(gray).append("  ─────────────────────").append(reset).append("\n");
        
        status.append("  ").append(white).append("Vault").append(gray).append(" ............... ");
        status.append(vaultEnabled ? green + bold + "Active" : red + "Inactive").append(reset).append("\n");
        
        status.append("  ").append(white).append("LuckPerms").append(gray).append(" ........... ");
        status.append(luckPermsEnabled ? green + bold + "Active" : red + "Inactive").append(reset).append("\n");
        
        status.append("  ").append(white).append("Essentials").append(gray).append(" .......... ");
        status.append(essentialsEnabled ? green + bold + "Active" : red + "Inactive").append(reset).append("\n");
        
        status.append("  ").append(white).append("PlaceholderAPI").append(gray).append(" ...... ");
        status.append(placeholderAPIEnabled ? green + bold + "Active" : red + "Inactive").append(reset).append("\n");
        
        status.append("  ").append(white).append("DiscordSRV").append(gray).append(" .......... ");
        status.append(discordSRVEnabled ? green + bold + "Active" : red + "Inactive").append(reset);
        
        return status.toString();
    }
}