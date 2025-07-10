package BMG.banManagerGUI.utils;

import BMG.banManagerGUI.BanManagerGUI;
import BMG.banManagerGUI.data.PunishmentData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class BulkActions {
    private final BanManagerGUI plugin;
    private final PunishmentData punishmentData;
    private final Map<UUID, List<String>> selectedPlayers = new HashMap<>();
    
    public BulkActions(BanManagerGUI plugin) {
        this.plugin = plugin;
        this.punishmentData = plugin.getPunishmentData();
    }
    
    public void addSelectedPlayer(Player selector, String targetName) {
        selectedPlayers.computeIfAbsent(selector.getUniqueId(), k -> new ArrayList<>()).add(targetName);
    }
    
    public void removeSelectedPlayer(Player selector, String targetName) {
        List<String> selected = selectedPlayers.get(selector.getUniqueId());
        if (selected != null) {
            selected.remove(targetName);
        }
    }
    
    public List<String> getSelectedPlayers(Player selector) {
        return selectedPlayers.getOrDefault(selector.getUniqueId(), new ArrayList<>());
    }
    
    public void clearSelection(Player selector) {
        selectedPlayers.remove(selector.getUniqueId());
    }
    
    public CompletableFuture<BulkActionResult> executeBulkBan(Player executor, String reason) {
        return CompletableFuture.supplyAsync(() -> {
            List<String> selected = getSelectedPlayers(executor);
            BulkActionResult result = new BulkActionResult();
            
            for (String playerName : selected) {
                try {
                    Player target = Bukkit.getPlayer(playerName);
                    if (target != null) {
                        Bukkit.getScheduler().runTask(plugin, () -> 
                            target.kickPlayer("§cYou have been banned!\n§7Reason: " + reason + "\n§7Banned by: " + executor.getName())
                        );
                    }
                    
                    Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(playerName, reason, null, executor.getName());
                    punishmentData.addPunishment(playerName, "BAN", reason, executor.getName(), 0);
                    result.addSuccess(playerName);
                } catch (Exception e) {
                    result.addFailure(playerName, e.getMessage());
                }
            }
            
            return result;
        });
    }
    
    public CompletableFuture<BulkActionResult> executeBulkKick(Player executor, String reason) {
        return CompletableFuture.supplyAsync(() -> {
            List<String> selected = getSelectedPlayers(executor);
            BulkActionResult result = new BulkActionResult();
            
            for (String playerName : selected) {
                Player target = Bukkit.getPlayer(playerName);
                if (target != null) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        target.kickPlayer("§cYou have been kicked!\n§7Reason: " + reason + "\n§7Kicked by: " + executor.getName());
                        punishmentData.addPunishment(playerName, "KICK", reason, executor.getName(), 0);
                    });
                    result.addSuccess(playerName);
                } else {
                    result.addFailure(playerName, "Player not online");
                }
            }
            
            return result;
        });
    }
    
    public CompletableFuture<BulkActionResult> executeBulkMute(Player executor, String reason) {
        return CompletableFuture.supplyAsync(() -> {
            List<String> selected = getSelectedPlayers(executor);
            BulkActionResult result = new BulkActionResult();
            
            for (String playerName : selected) {
                try {
                    punishmentData.addPunishment(playerName, "MUTE", reason, executor.getName(), 0);
                    Player target = Bukkit.getPlayer(playerName);
                    if (target != null) {
                        Bukkit.getScheduler().runTask(plugin, () -> 
                            target.sendMessage("§cYou have been muted!\n§7Reason: " + reason + "\n§7Muted by: " + executor.getName())
                        );
                    }
                    result.addSuccess(playerName);
                } catch (Exception e) {
                    result.addFailure(playerName, e.getMessage());
                }
            }
            
            return result;
        });
    }
    
    public static class BulkActionResult {
        private final List<String> successful = new ArrayList<>();
        private final Map<String, String> failed = new HashMap<>();
        
        public void addSuccess(String player) {
            successful.add(player);
        }
        
        public void addFailure(String player, String reason) {
            failed.put(player, reason);
        }
        
        public List<String> getSuccessful() {
            return successful;
        }
        
        public Map<String, String> getFailed() {
            return failed;
        }
        
        public int getTotalProcessed() {
            return successful.size() + failed.size();
        }
    }
}