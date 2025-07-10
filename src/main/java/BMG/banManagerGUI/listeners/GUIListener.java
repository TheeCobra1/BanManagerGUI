package BMG.banManagerGUI.listeners;

import BMG.banManagerGUI.BanManagerGUI;
import BMG.banManagerGUI.data.PunishmentData;
import BMG.banManagerGUI.gui.GUIHolder;
import BMG.banManagerGUI.gui.GUIManager;
import BMG.banManagerGUI.utils.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GUIListener implements Listener {
    private final BanManagerGUI plugin;
    private final GUIManager guiManager;
    private final PunishmentData punishmentData;
    private final Map<Player, String> selectedReasons = new HashMap<>();
    private final Map<Player, Long> selectedDurations = new HashMap<>();
    
    public GUIListener(BanManagerGUI plugin, GUIManager guiManager, PunishmentData punishmentData) {
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.punishmentData = punishmentData;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof GUIHolder)) return;
        
        GUIHolder guiHolder = (GUIHolder) holder;
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        if (clicked.getItemMeta() == null || !clicked.getItemMeta().hasDisplayName()) return;
        String itemName = clicked.getItemMeta().getDisplayName();
        String guiType = guiHolder.getGuiType();
        String title = guiHolder.getGuiTitle();
        
        switch (guiType) {
            case "home":
                handleHomeClick(player, itemName);
                break;
            case "player_select":
                handlePlayerSelectionClick(player, clicked, itemName);
                break;
            case "unban":
                handleUnbanClick(player, clicked, itemName);
                break;
            case "unmute":
                handleUnmuteClick(player, clicked, itemName);
                break;
            case "punishment":
                handlePunishmentClick(player, title, itemName);
                break;
            case "history":
                if (itemName.equals("§cClose")) {
                    player.closeInventory();
                }
                break;
            case "advanced":
                handleAdvancedClick(player, itemName);
                break;
            case "statistics":
                handleStatisticsClick(player, itemName);
                break;
            case "warnings":
                handleWarningsClick(player, itemName);
                break;
            case "ipmanager":
                handleIPManagerClick(player, itemName);
                break;
            case "templates":
                handleTemplatesClick(player, clicked, itemName);
                break;
            case "vpnsettings":
                handleVPNSettingsClick(player, itemName);
                break;
            case "ipbanlist":
                handleIPBanListClick(player, clicked, itemName);
                break;
            case "vpnwhitelist":
                handleVPNWhitelistClick(player, clicked, itemName);
                break;
            case "bulkactions":
                handleBulkActionsClick(player, itemName);
                break;
            case "bulkselect":
                handleBulkSelectClick(player, clicked, itemName);
                break;
            case "importexport":
                handleImportExportClick(player, itemName);
                break;
            case "automod":
                handleAutoModClick(player, itemName);
                break;
            case "auditlog":
                handleAuditLogClick(player, itemName);
                break;
            case "backup":
                handleBackupClick(player, itemName);
                break;
        }
    }
    
    private void handleHomeClick(Player player, String itemName) {
        if (itemName.equals("§c§lBan Player")) {
            guiManager.openPlayerSelectionGUI(player, "Ban");
        } else if (itemName.equals("§6§lTemp Ban")) {
            guiManager.openPlayerSelectionGUI(player, "Temp Ban");
        } else if (itemName.equals("§e§lMute Player")) {
            guiManager.openPlayerSelectionGUI(player, "Mute");
        } else if (itemName.equals("§6§lTemp Mute")) {
            guiManager.openPlayerSelectionGUI(player, "Temp Mute");
        } else if (itemName.equals("§c§lKick Player")) {
            guiManager.openPlayerSelectionGUI(player, "Kick");
        } else if (itemName.equals("§a§lUnban Player")) {
            guiManager.openUnbanGUI(player);
        } else if (itemName.equals("§a§lUnmute Player")) {
            guiManager.openUnmuteGUI(player);
        } else if (itemName.equals("§b§lView History")) {
            guiManager.openPlayerSelectionGUI(player, "History");
        } else if (itemName.equals("§d§lStatistics")) {
            guiManager.openStatisticsGUI(player);
        } else if (itemName.equals("§5§lAdvanced Tools")) {
            if (player.hasPermission("banmanager.advanced")) {
                guiManager.openAdvancedToolsGUI(player);
            } else {
                player.sendMessage("§cYou don't have permission to access advanced tools!");
            }
        } else if (itemName.equals("§e§lWarnings")) {
            guiManager.openWarningsGUI(player);
        } else if (itemName.equals("§c§lIP Manager")) {
            if (player.hasPermission("banmanager.ipmanage")) {
                guiManager.openIPManagerGUI(player);
            } else {
                player.sendMessage("§cYou don't have permission to manage IPs!");
            }
        } else if (itemName.equals("§b§lTemplates")) {
            guiManager.openTemplatesGUI(player);
        } else if (itemName.equals("§a§lSearch Player")) {
            player.closeInventory();
            player.sendMessage("§ePlease type the player name in chat to search:");
        } else if (itemName.equals("§c§lReload")) {
            if (player.hasPermission("banmanager.reload")) {
                plugin.reloadConfig();
                player.sendMessage("§aConfiguration reloaded successfully!");
            } else {
                player.sendMessage("§cYou don't have permission to reload!");
            }
        } else if (itemName.equals("§c§lClose")) {
            player.closeInventory();
        }
    }
    
    private void handlePlayerSelectionClick(Player player, ItemStack clicked, String itemName) {
        if (itemName.equals("§7Back")) {
            guiManager.openHomeGUI(player);
        } else if (itemName.equals("§cClose")) {
            player.closeInventory();
            guiManager.removePlayerAction(player);
        } else if (itemName.equals("§eSearch Offline")) {
            player.closeInventory();
            player.sendMessage("§ePlease type the player name in chat:");
        } else if (clicked.getType() == Material.PLAYER_HEAD && itemName.startsWith("§e")) {
            String targetName = itemName.startsWith("§e§l") ? itemName.substring(4) : itemName.substring(2);
            String action = guiManager.getPlayerAction(player);
            
            if (action.equals("History")) {
                guiManager.openHistoryGUI(player, targetName);
            } else if (action.equals("Warning")) {
                player.closeInventory();
                punishmentData.addWarning(targetName, "Manual warning", player.getName());
                player.sendMessage("§aSuccessfully warned " + targetName);
                player.sendMessage("§eThey now have §6" + punishmentData.getWarningCount(targetName) + " §ewarnings");
            } else if (action.equals("ViewWarnings")) {
                player.closeInventory();
                int warnings = punishmentData.getWarningCount(targetName);
                player.sendMessage("§e" + targetName + " has §6" + warnings + " §ewarnings");
            } else if (action.equals("ClearWarnings")) {
                player.closeInventory();
                player.sendMessage("§aCleared warnings for " + targetName);
            } else if (action.equals("IPHistory")) {
                player.closeInventory();
                List<String> ips = plugin.getIPData().getPlayerIPs(targetName);
                player.sendMessage("§e§lIP History for " + targetName + ":");
                if (ips.isEmpty()) {
                    player.sendMessage("§7No IP history found");
                } else {
                    for (String ip : ips) {
                        player.sendMessage("§7- §f" + ip);
                    }
                }
            } else if (action.equals("VPNCheck")) {
                player.closeInventory();
                String lastIP = plugin.getIPData().getPlayerLastIP(targetName);
                if (lastIP.equals("Unknown")) {
                    player.sendMessage("§cNo IP data found for " + targetName);
                } else {
                    player.sendMessage("§eChecking IP " + lastIP + " for " + targetName + "...");
                    plugin.getVPNDetector().checkIP(lastIP).thenAccept(result -> {
                        player.sendMessage("§e§lVPN Check Results:");
                        player.sendMessage("§7VPN: " + (result.isVPN ? "§cYes" : "§aNo"));
                        player.sendMessage("§7Proxy: " + (result.isProxy ? "§cYes" : "§aNo"));
                        player.sendMessage("§7Country: §f" + result.country);
                        player.sendMessage("§7ISP: §f" + result.isp);
                        player.sendMessage("§7Risk Score: §f" + result.risk + "%");
                    });
                }
            } else {
                guiManager.openPunishmentGUI(player, targetName, action);
            }
        }
    }
    
    private void handlePunishmentClick(Player player, String title, String itemName) {
        String[] parts = title.split(" - ");
        String action = parts[0].substring(2);
        String targetName = parts[1];
        
        if (itemName.equals("§7Back")) {
            guiManager.openPlayerSelectionGUI(player, action);
            selectedReasons.remove(player);
            selectedDurations.remove(player);
        } else if (itemName.equals("§cCancel")) {
            player.closeInventory();
            guiManager.removePlayerAction(player);
            selectedReasons.remove(player);
            selectedDurations.remove(player);
        } else if (itemName.equals("§aConfirm")) {
            String reason = selectedReasons.getOrDefault(player, "No reason specified");
            long duration = selectedDurations.getOrDefault(player, 0L);
            executePunishment(player, targetName, action, reason, duration);
            player.closeInventory();
            guiManager.removePlayerAction(player);
            selectedReasons.remove(player);
            selectedDurations.remove(player);
        } else if (itemName.startsWith("§7")) {
            String reasonText = itemName.substring(2);
            selectedReasons.put(player, reasonText);
            player.sendMessage("§aSelected reason: " + reasonText);
        } else if (itemName.startsWith("§e") && (itemName.contains("Hour") || itemName.contains("Day") || 
                   itemName.contains("Week") || itemName.contains("Month"))) {
            String durationText = itemName.substring(2);
            long duration = TimeUtil.getDurationMillis(durationText);
            selectedDurations.put(player, duration);
            player.sendMessage("§aSelected duration: " + durationText);
        }
    }
    
    private void executePunishment(Player player, String targetName, String action, String reason, long duration) {
        Player target = Bukkit.getPlayer(targetName);
        
        switch (action) {
            case "Ban":
                if (target != null) {
                    target.kickPlayer("§cYou have been banned!\n§7Reason: " + reason + "\n§7Banned by: " + player.getName());
                }
                Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(targetName, reason, null, player.getName());
                punishmentData.addPunishment(targetName, "BAN", reason, player.getName(), 0);
                guiManager.invalidateBanCache();
                player.sendMessage("§aSuccessfully banned " + targetName);
                broadcastPunishment(targetName, "banned", reason, player.getName(), 0);
                break;
            case "Temp Ban":
                if (duration <= 0) {
                    player.sendMessage("§cPlease select a duration!");
                    return;
                }
                if (target != null) {
                    target.kickPlayer("§cYou have been temporarily banned!\n§7Reason: " + reason + 
                                    "\n§7Duration: " + TimeUtil.formatDuration(duration) + 
                                    "\n§7Banned by: " + player.getName());
                }
                Date expiry = new Date(System.currentTimeMillis() + duration);
                Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(targetName, reason, expiry, player.getName());
                punishmentData.addPunishment(targetName, "TEMPBAN", reason, player.getName(), duration);
                guiManager.invalidateBanCache();
                player.sendMessage("§aSuccessfully temp banned " + targetName + " for " + TimeUtil.formatDuration(duration));
                broadcastPunishment(targetName, "temporarily banned", reason, player.getName(), duration);
                break;
            case "Mute":
                punishmentData.addPunishment(targetName, "MUTE", reason, player.getName(), 0);
                if (target != null) {
                    target.sendMessage("§cYou have been muted!\n§7Reason: " + reason + "\n§7Muted by: " + player.getName());
                }
                player.sendMessage("§aSuccessfully muted " + targetName);
                broadcastPunishment(targetName, "muted", reason, player.getName(), 0);
                break;
            case "Temp Mute":
                if (duration <= 0) {
                    player.sendMessage("§cPlease select a duration!");
                    return;
                }
                punishmentData.addPunishment(targetName, "TEMPMUTE", reason, player.getName(), duration);
                if (target != null) {
                    target.sendMessage("§cYou have been temporarily muted!\n§7Reason: " + reason + 
                                     "\n§7Duration: " + TimeUtil.formatDuration(duration) + 
                                     "\n§7Muted by: " + player.getName());
                }
                player.sendMessage("§aSuccessfully temp muted " + targetName + " for " + TimeUtil.formatDuration(duration));
                broadcastPunishment(targetName, "temporarily muted", reason, player.getName(), duration);
                break;
            case "Kick":
                if (target != null) {
                    target.kickPlayer("§cYou have been kicked!\n§7Reason: " + reason + "\n§7Kicked by: " + player.getName());
                    punishmentData.addPunishment(targetName, "KICK", reason, player.getName(), 0);
                    player.sendMessage("§aSuccessfully kicked " + targetName);
                    broadcastPunishment(targetName, "kicked", reason, player.getName(), 0);
                } else {
                    player.sendMessage("§cPlayer not online!");
                }
                break;
        }
    }
    
    private void broadcastPunishment(String target, String action, String reason, String issuer, long duration) {
        String message;
        if (duration > 0) {
            message = "§c" + target + " has been " + action + " by " + issuer + " for " + 
                      TimeUtil.formatDuration(duration) + ". Reason: " + reason;
        } else {
            message = "§c" + target + " has been " + action + " by " + issuer + " for: " + reason;
        }
        
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("banmanager.notify")) {
                p.sendMessage(message);
            }
        }
        
        String consoleMessage = message.replaceAll("§[0-9a-fk-or]", "");
        plugin.getLogger().info(consoleMessage);
    }
    
    private void handleUnbanClick(Player player, ItemStack clicked, String itemName) {
        if (itemName.equals("§cBack")) {
            guiManager.openHomeGUI(player);
        } else if (itemName.equals("§7Previous Page")) {
            int currentPage = guiManager.getPlayerPage(player);
            guiManager.openUnbanGUI(player, currentPage - 1);
        } else if (itemName.equals("§7Next Page")) {
            int currentPage = guiManager.getPlayerPage(player);
            guiManager.openUnbanGUI(player, currentPage + 1);
        } else if (clicked.getType() == Material.PLAYER_HEAD && itemName.startsWith("§c")) {
            String targetName = itemName.substring(2);
            player.closeInventory();
            
            Bukkit.getBanList(org.bukkit.BanList.Type.NAME).pardon(targetName);
            punishmentData.removeBan(targetName);
            punishmentData.addPunishment(targetName, "UNBAN", "Unbanned", player.getName(), 0);
            guiManager.invalidateBanCache();
            
            player.sendMessage("§aSuccessfully unbanned " + targetName);
            
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.hasPermission("banmanager.notify")) {
                    p.sendMessage("§a" + targetName + " has been unbanned by " + player.getName());
                }
            }
        }
    }
    
    private void handleUnmuteClick(Player player, ItemStack clicked, String itemName) {
        if (itemName.equals("§cBack")) {
            guiManager.openHomeGUI(player);
        } else if (itemName.equals("§7Previous Page")) {
            int currentPage = guiManager.getPlayerPage(player);
            guiManager.openUnmuteGUI(player, currentPage - 1);
        } else if (itemName.equals("§7Next Page")) {
            int currentPage = guiManager.getPlayerPage(player);
            guiManager.openUnmuteGUI(player, currentPage + 1);
        } else if (clicked.getType() == Material.PLAYER_HEAD && itemName.startsWith("§e")) {
            String targetName = itemName.startsWith("§e§l") ? itemName.substring(4) : itemName.substring(2);
            player.closeInventory();
            
            punishmentData.removeMute(targetName);
            punishmentData.addPunishment(targetName, "UNMUTE", "Unmuted", player.getName(), 0);
            
            Player target = Bukkit.getPlayer(targetName);
            if (target != null) {
                target.sendMessage("§aYou have been unmuted!");
            }
            
            player.sendMessage("§aSuccessfully unmuted " + targetName);
            
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.hasPermission("banmanager.notify")) {
                    p.sendMessage("§a" + targetName + " has been unmuted by " + player.getName());
                }
            }
        }
    }
    
    private void handleAdvancedClick(Player player, String itemName) {
        if (itemName.equals("§7Back")) {
            guiManager.openHomeGUI(player);
        } else if (itemName.equals("§e§lBulk Actions")) {
            guiManager.openBulkActionsGUI(player);
        } else if (itemName.equals("§b§lBan Import/Export")) {
            guiManager.openImportExportGUI(player);
        } else if (itemName.equals("§c§lAuto Moderation")) {
            guiManager.openAutoModSettingsGUI(player);
        } else if (itemName.equals("§a§lPlugin Integration")) {
            player.sendMessage("§aPlugin integration coming soon!");
        } else if (itemName.equals("§d§lAudit Log")) {
            guiManager.openAuditLogGUI(player, 0);
        } else if (itemName.equals("§6§lBackup & Restore")) {
            guiManager.openBackupRestoreGUI(player);
        }
    }
    
    private void handleStatisticsClick(Player player, String itemName) {
        if (itemName.equals("§7Back")) {
            guiManager.openHomeGUI(player);
        } else if (itemName.equals("§b§lRecent Activity")) {
            player.sendMessage("§bRecent activity viewer coming soon!");
        } else if (itemName.equals("§a§lTop Offenders")) {
            player.sendMessage("§aTop offenders list coming soon!");
        }
    }
    
    private void handleWarningsClick(Player player, String itemName) {
        if (itemName.equals("§7Back")) {
            guiManager.openHomeGUI(player);
        } else if (itemName.equals("§e§lIssue Warning")) {
            guiManager.openPlayerSelectionGUI(player, "Warning");
        } else if (itemName.equals("§6§lView Warnings")) {
            guiManager.openPlayerSelectionGUI(player, "ViewWarnings");
        } else if (itemName.equals("§a§lClear Warnings")) {
            guiManager.openPlayerSelectionGUI(player, "ClearWarnings");
        } else if (itemName.equals("§c§lWarning Settings")) {
            player.sendMessage("§cWarning settings coming soon!");
        }
    }
    
    private void handleIPManagerClick(Player player, String itemName) {
        if (itemName.equals("§7Back")) {
            guiManager.openHomeGUI(player);
        } else if (itemName.equals("§c§lIP Ban")) {
            player.closeInventory();
            player.sendMessage("§cPlease type the IP address to ban:");
        } else if (itemName.equals("§a§lIP Unban")) {
            guiManager.openIPBanListGUI(player, 0);
        } else if (itemName.equals("§b§lIP Lookup")) {
            player.closeInventory();
            player.sendMessage("§bPlease type the IP to lookup:");
        } else if (itemName.equals("§e§lIP History")) {
            guiManager.openPlayerSelectionGUI(player, "IPHistory");
        } else if (itemName.equals("§d§lVPN Whitelist")) {
            guiManager.openVPNWhitelistGUI(player);
        } else if (itemName.equals("§6§lVPN Detection")) {
            guiManager.openVPNSettingsGUI(player);
        }
    }
    
    private void handleTemplatesClick(Player player, ItemStack clicked, String itemName) {
        if (itemName.equals("§7Back")) {
            guiManager.openHomeGUI(player);
        } else if (itemName.equals("§a§lCreate Template")) {
            player.sendMessage("§aTemplate creation coming soon!");
        } else if (clicked.getType() == Material.PAPER) {
            String templateName = itemName.substring(4);
            guiManager.openPlayerSelectionGUI(player, "Template:" + templateName);
        }
    }
    
    private void handleVPNSettingsClick(Player player, String itemName) {
        if (itemName.equals("§7Back")) {
            guiManager.openIPManagerGUI(player);
        } else if (itemName.equals("§a§lDetection Enabled") || itemName.equals("§c§lDetection Disabled")) {
            boolean current = plugin.getConfig().getBoolean("vpn-detection.enabled", false);
            plugin.getConfig().set("vpn-detection.enabled", !current);
            plugin.saveConfig();
            guiManager.openVPNSettingsGUI(player);
            player.sendMessage("§aVPN detection " + (!current ? "enabled" : "disabled") + "!");
        } else if (itemName.equals("§c§lAuto-Kick Enabled") || itemName.equals("§7§lAuto-Kick Disabled")) {
            boolean current = plugin.getConfig().getBoolean("vpn-detection.kick-on-detect", true);
            plugin.getConfig().set("vpn-detection.kick-on-detect", !current);
            plugin.saveConfig();
            guiManager.openVPNSettingsGUI(player);
        } else if (itemName.equals("§e§lNotifications Enabled") || itemName.equals("§7§lNotifications Disabled")) {
            boolean current = plugin.getConfig().getBoolean("vpn-detection.notify-staff", true);
            plugin.getConfig().set("vpn-detection.notify-staff", !current);
            plugin.saveConfig();
            guiManager.openVPNSettingsGUI(player);
        } else if (itemName.equals("§b§lCheck Player IP")) {
            guiManager.openPlayerSelectionGUI(player, "VPNCheck");
        }
    }
    
    private void handleIPBanListClick(Player player, ItemStack clicked, String itemName) {
        if (itemName.equals("§cBack")) {
            guiManager.openIPManagerGUI(player);
        } else if (itemName.equals("§7Previous Page") || itemName.equals("§7Next Page")) {
            int currentPage = guiManager.getPlayerPage(player);
            int newPage = itemName.contains("Previous") ? currentPage - 1 : currentPage + 1;
            guiManager.openIPBanListGUI(player, newPage);
        } else if (clicked.getType() == Material.REDSTONE_BLOCK && itemName.startsWith("§c")) {
            String ip = itemName.substring(2);
            plugin.getIPData().unbanIP(ip);
            plugin.getAuditLog().log(player.getName(), "IP_UNBAN", ip, "Unbanned via GUI");
            player.sendMessage("§aSuccessfully unbanned IP: " + ip);
            guiManager.openIPBanListGUI(player, guiManager.getPlayerPage(player));
        }
    }
    
    private void handleVPNWhitelistClick(Player player, ItemStack clicked, String itemName) {
        if (itemName.equals("§7Back")) {
            guiManager.openIPManagerGUI(player);
        } else if (itemName.equals("§a§lAdd IP")) {
            player.closeInventory();
            player.sendMessage("§aPlease type the IP address to whitelist:");
        } else if (clicked.getType() == Material.EMERALD && itemName.startsWith("§a")) {
            String ip = itemName.substring(2);
            plugin.getVPNDetector().removeFromWhitelist(ip);
            player.sendMessage("§cRemoved " + ip + " from VPN whitelist");
            guiManager.openVPNWhitelistGUI(player);
        }
    }
    
    private void handleBulkActionsClick(Player player, String itemName) {
        if (itemName.equals("§7Back")) {
            guiManager.openAdvancedToolsGUI(player);
        } else if (itemName.equals("§a§lSelect Players")) {
            guiManager.openBulkPlayerSelectionGUI(player, 0);
        } else if (itemName.equals("§c§lBulk Ban")) {
            player.closeInventory();
            player.sendMessage("§cPlease type the ban reason:");
            guiManager.setPlayerAction(player, "BulkBan");
        } else if (itemName.equals("§e§lBulk Mute")) {
            player.closeInventory();
            player.sendMessage("§ePlease type the mute reason:");
            guiManager.setPlayerAction(player, "BulkMute");
        } else if (itemName.equals("§c§lBulk Kick")) {
            player.closeInventory();
            player.sendMessage("§cPlease type the kick reason:");
            guiManager.setPlayerAction(player, "BulkKick");
        } else if (itemName.equals("§c§lClear Selection")) {
            plugin.getBulkActions().clearSelection(player);
            player.sendMessage("§aSelection cleared!");
            guiManager.openBulkActionsGUI(player);
        }
    }
    
    private void handleBulkSelectClick(Player player, ItemStack clicked, String itemName) {
        if (itemName.equals("§7Previous Page") || itemName.equals("§7Next Page")) {
            int currentPage = guiManager.getPlayerPage(player);
            int newPage = itemName.contains("Previous") ? currentPage - 1 : currentPage + 1;
            guiManager.openBulkPlayerSelectionGUI(player, newPage);
        } else if (itemName.equals("§a§lConfirm Selection")) {
            guiManager.openBulkActionsGUI(player);
        } else if (clicked.getType() == Material.PLAYER_HEAD) {
            String targetName = itemName.startsWith("§e§l") ? itemName.substring(4) : itemName.substring(2);
            List<String> selected = plugin.getBulkActions().getSelectedPlayers(player);
            
            if (selected.contains(targetName)) {
                plugin.getBulkActions().removeSelectedPlayer(player, targetName);
            } else {
                plugin.getBulkActions().addSelectedPlayer(player, targetName);
            }
            guiManager.openBulkPlayerSelectionGUI(player, guiManager.getPlayerPage(player));
        }
    }
    
    private void handleImportExportClick(Player player, String itemName) {
        if (itemName.equals("§7Back")) {
            guiManager.openAdvancedToolsGUI(player);
        } else if (itemName.equals("§a§lExport Bans")) {
            player.closeInventory();
            plugin.getImportExport().exportBans().thenAccept(result -> {
                if (result.success) {
                    player.sendMessage("§aSuccessfully exported " + result.exportedBans + " bans and " + 
                                     result.exportedMutes + " mutes!");
                    player.sendMessage("§7File: §f" + result.filePath);
                    plugin.getAuditLog().log(player.getName(), "EXPORT", "All Bans", 
                                           "Exported " + result.exportedBans + " bans, " + result.exportedMutes + " mutes");
                } else {
                    player.sendMessage("§cExport failed: " + result.error);
                }
            });
        } else if (itemName.equals("§b§lImport Bans")) {
            player.closeInventory();
            player.sendMessage("§bPlease type the filename to import (or 'cancel' to cancel):");
            guiManager.setPlayerAction(player, "ImportFile");
        } else if (itemName.equals("§e§lRecent Exports")) {
            player.sendMessage("§eAvailable export files:");
            for (File file : plugin.getImportExport().getExportFiles()) {
                player.sendMessage("§7- §f" + file.getName());
            }
        }
    }
    
    private void handleAutoModClick(Player player, String itemName) {
        if (itemName.equals("§7Back")) {
            guiManager.openAdvancedToolsGUI(player);
        } else if (itemName.equals("§a§lEnabled") || itemName.equals("§c§lDisabled")) {
            boolean current = plugin.getConfig().getBoolean("auto-mod.enabled", false);
            plugin.getConfig().set("auto-mod.enabled", !current);
            plugin.saveConfig();
            player.sendMessage("§aAuto moderation " + (!current ? "enabled" : "disabled") + "!");
            guiManager.openAutoModSettingsGUI(player);
        } else if (itemName.equals("§e§lSpam Detection")) {
            boolean current = plugin.getConfig().getBoolean("auto-mod.detect-spam", true);
            plugin.getConfig().set("auto-mod.detect-spam", !current);
            plugin.saveConfig();
            guiManager.openAutoModSettingsGUI(player);
        } else if (itemName.equals("§c§lCaps Detection")) {
            boolean current = plugin.getConfig().getBoolean("auto-mod.detect-caps", true);
            plugin.getConfig().set("auto-mod.detect-caps", !current);
            plugin.saveConfig();
            guiManager.openAutoModSettingsGUI(player);
        } else if (itemName.equals("§b§lAdvertising Detection")) {
            boolean current = plugin.getConfig().getBoolean("auto-mod.detect-advertising", true);
            plugin.getConfig().set("auto-mod.detect-advertising", !current);
            plugin.saveConfig();
            guiManager.openAutoModSettingsGUI(player);
        } else if (itemName.equals("§6§lBlocked Words")) {
            player.closeInventory();
            player.sendMessage("§6Current blocked words:");
            for (String word : plugin.getConfig().getStringList("auto-mod.blocked-words")) {
                player.sendMessage("§7- §f" + word);
            }
            player.sendMessage("§eTo add a word: §f/banmanager automod addword <word>");
            player.sendMessage("§eTo remove a word: §f/banmanager automod removeword <word>");
        } else if (itemName.equals("§c§lPunishment Settings")) {
            player.sendMessage("§cAuto-mod punishment configuration coming soon!");
        }
    }
    
    private void handleAuditLogClick(Player player, String itemName) {
        if (itemName.equals("§7Back")) {
            guiManager.openAdvancedToolsGUI(player);
        } else if (itemName.equals("§7Previous Page") || itemName.equals("§7Next Page")) {
            int currentPage = guiManager.getPlayerPage(player);
            int newPage = itemName.contains("Previous") ? currentPage - 1 : currentPage + 1;
            guiManager.openAuditLogGUI(player, newPage);
        } else if (itemName.equals("§e§lSearch")) {
            player.closeInventory();
            player.sendMessage("§ePlease type a player name or action type to search:");
            guiManager.setPlayerAction(player, "AuditSearch");
        }
    }
    
    private void handleBackupClick(Player player, String itemName) {
        if (itemName.equals("§7Back")) {
            guiManager.openAdvancedToolsGUI(player);
        } else if (itemName.equals("§a§lCreate Backup")) {
            player.closeInventory();
            File backupDir = new File(plugin.getDataFolder(), "backups");
            backupDir.mkdirs();
            
            File punishmentFile = new File(plugin.getDataFolder(), "punishments.yml");
            File backupFile = new File(backupDir, "backup_" + System.currentTimeMillis() + ".yml");
            
            try {
                java.nio.file.Files.copy(punishmentFile.toPath(), backupFile.toPath());
                player.sendMessage("§aBackup created successfully!");
                player.sendMessage("§7File: §f" + backupFile.getName());
                plugin.getAuditLog().log(player.getName(), "BACKUP_CREATE", "System", backupFile.getName());
            } catch (Exception e) {
                player.sendMessage("§cBackup failed: " + e.getMessage());
            }
        } else if (itemName.equals("§b§lRestore Backup")) {
            player.closeInventory();
            player.sendMessage("§bAvailable backups:");
            
            File backupDir = new File(plugin.getDataFolder(), "backups");
            if (backupDir.exists()) {
                File[] backups = backupDir.listFiles((dir, name) -> name.endsWith(".yml"));
                if (backups != null && backups.length > 0) {
                    for (File backup : backups) {
                        player.sendMessage("§7- §f" + backup.getName());
                    }
                    player.sendMessage("§eTo restore: §f/banmanager restore <filename>");
                } else {
                    player.sendMessage("§cNo backups found!");
                }
            }
        } else if (itemName.equals("§e§lRecent Backups")) {
            player.sendMessage("§eRecent backup files:");
            File backupDir = new File(plugin.getDataFolder(), "backups");
            if (backupDir.exists()) {
                File[] backups = backupDir.listFiles((dir, name) -> name.endsWith(".yml"));
                if (backups != null) {
                    Arrays.sort(backups, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
                    for (int i = 0; i < Math.min(5, backups.length); i++) {
                        player.sendMessage("§7- §f" + backups[i].getName());
                    }
                }
            }
        }
    }
}