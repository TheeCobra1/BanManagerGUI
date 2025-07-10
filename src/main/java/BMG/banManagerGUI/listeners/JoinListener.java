package BMG.banManagerGUI.listeners;

import BMG.banManagerGUI.BanManagerGUI;
import BMG.banManagerGUI.data.IPData;
import BMG.banManagerGUI.data.PunishmentData;
import BMG.banManagerGUI.utils.FreeVPNDetector;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

public class JoinListener implements Listener {
    private final BanManagerGUI plugin;
    private final PunishmentData punishmentData;
    
    public JoinListener(BanManagerGUI plugin, PunishmentData punishmentData) {
        this.plugin = plugin;
        this.punishmentData = punishmentData;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        String playerName = event.getPlayer().getName();
        String ip = event.getAddress().getHostAddress();
        
        if (punishmentData.isBanned(playerName)) {
            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, "§cYou are banned from this server!");
            return;
        }
        
        if (plugin.getConfig().getBoolean("vpn-detection.enabled", false)) {
            plugin.getVPNDetector().checkIP(ip).thenAccept(result -> {
                if ((result.isVPN || result.isProxy) && !event.getPlayer().hasPermission("banmanager.vpn.bypass")) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (event.getPlayer().isOnline()) {
                            event.getPlayer().kickPlayer("§cVPN/Proxy connections are not allowed!");
                            plugin.getAuditLog().log("AutoMod", "VPN_KICK", playerName, 
                                "VPN: " + result.isVPN + ", Proxy: " + result.isProxy + ", Country: " + result.country);
                        }
                    });
                }
            });
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String playerName = event.getPlayer().getName();
        String ip = event.getPlayer().getAddress().getAddress().getHostAddress();
        
        plugin.getIPData().recordPlayerIP(playerName, ip);
    }
}