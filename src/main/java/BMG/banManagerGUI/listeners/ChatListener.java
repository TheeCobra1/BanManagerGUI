package BMG.banManagerGUI.listeners;

import BMG.banManagerGUI.BanManagerGUI;
import BMG.banManagerGUI.data.PunishmentData;
import BMG.banManagerGUI.utils.AutoModerator;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
    private final BanManagerGUI plugin;
    private final PunishmentData punishmentData;
    
    public ChatListener(BanManagerGUI plugin, PunishmentData punishmentData) {
        this.plugin = plugin;
        this.punishmentData = punishmentData;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        
        if (punishmentData.isMuted(player.getName())) {
            event.setCancelled(true);
            player.sendMessage("§cYou are currently muted and cannot speak!");
            return;
        }
        
        if (plugin.getConfig().getBoolean("auto-mod.enabled", false) && 
            !player.hasPermission("banmanager.automod.bypass")) {
            AutoModerator.CheckResult result = plugin.getAutoModerator().checkMessage(player, event.getMessage());
            if (!result.allowed) {
                event.setCancelled(true);
                player.sendMessage("§cYour message was blocked: " + result.reason);
                plugin.getAuditLog().log("AutoMod", "MESSAGE_BLOCKED", player.getName(), result.reason);
            }
        }
    }
}