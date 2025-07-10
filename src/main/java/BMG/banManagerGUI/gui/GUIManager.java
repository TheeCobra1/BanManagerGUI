package BMG.banManagerGUI.gui;

import BMG.banManagerGUI.BanManagerGUI;
import BMG.banManagerGUI.data.AuditLog;
import BMG.banManagerGUI.data.BanCache;
import BMG.banManagerGUI.data.PunishmentData;
import BMG.banManagerGUI.utils.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class GUIManager {
    private final BanManagerGUI plugin;
    private final PunishmentData punishmentData;
    private final BanCache banCache;
    private final Map<Player, String> playerActions = new HashMap<>();
    private final Map<Player, Integer> playerPages = new HashMap<>();
    
    public GUIManager(BanManagerGUI plugin) {
        this.plugin = plugin;
        this.punishmentData = plugin.getPunishmentData();
        this.banCache = new BanCache();
    }
    
    public void openHomeGUI(Player player) {
        Inventory gui = Bukkit.createInventory(new GUIHolder("home", "§c§lBan Manager §8- §7Home"), 54, "§c§lBan Manager §8- §7Home");
        
        for (int i = 0; i < 9; i++) {
            gui.setItem(i, createItem(Material.RED_STAINED_GLASS_PANE, " ", Arrays.asList()));
        }
        for (int i = 45; i < 54; i++) {
            gui.setItem(i, createItem(Material.RED_STAINED_GLASS_PANE, " ", Arrays.asList()));
        }
        
        gui.setItem(11, createGlowingItem(Material.REDSTONE_BLOCK, "§c§lBan Player", Arrays.asList("§7Permanently ban a player", "§7from the server", "", "§e» Click to select player")));
        gui.setItem(12, createItem(Material.ORANGE_CONCRETE, "§6§lTemp Ban", Arrays.asList("§7Temporarily ban a player", "§7for a specific duration", "", "§e» Click to select player")));
        gui.setItem(13, createGlowingItem(Material.GOLD_BLOCK, "§e§lMute Player", Arrays.asList("§7Mute a player in chat", "§7permanently", "", "§e» Click to select player")));
        gui.setItem(14, createItem(Material.YELLOW_CONCRETE, "§6§lTemp Mute", Arrays.asList("§7Temporarily mute a player", "§7for a specific duration", "", "§e» Click to select player")));
        gui.setItem(15, createGlowingItem(Material.IRON_SWORD, "§c§lKick Player", Arrays.asList("§7Remove a player from", "§7the server", "", "§e» Click to select player")));
        
        gui.setItem(20, createGlowingItem(Material.EMERALD_BLOCK, "§a§lUnban Player", Arrays.asList("§7Remove a ban from", "§7a player", "", "§e» Click to view banned")));
        gui.setItem(21, createItem(Material.LIME_CONCRETE, "§a§lUnmute Player", Arrays.asList("§7Remove a mute from", "§7a player", "", "§e» Click to view muted")));
        gui.setItem(22, createGlowingItem(Material.WRITABLE_BOOK, "§b§lView History", Arrays.asList("§7View punishment history", "§7of a player", "", "§e» Click to select player")));
        gui.setItem(23, createItem(Material.BEACON, "§d§lStatistics", Arrays.asList("§7View server punishment", "§7statistics and analytics", "", "§e» Click to view stats")));
        gui.setItem(24, createGlowingItem(Material.ANVIL, "§5§lAdvanced Tools", Arrays.asList("§7Access advanced admin", "§7moderation tools", "", "§e» Click to open")));
        
        gui.setItem(29, createItem(Material.BELL, "§e§lWarnings", Arrays.asList("§7Manage player warnings", "§7before punishments", "", "§e» Click to manage")));
        gui.setItem(30, createItem(Material.COMMAND_BLOCK, "§c§lIP Manager", Arrays.asList("§7Manage IP bans and", "§7IP-based punishments", "", "§e» Click to manage")));
        gui.setItem(31, createItem(Material.NETHER_STAR, "§b§lTemplates", Arrays.asList("§7Quick punishment templates", "§7for common offenses", "", "§e» Click to manage")));
        gui.setItem(32, createItem(Material.COMPASS, "§a§lSearch Player", Arrays.asList("§7Search for specific", "§7players quickly", "", "§e» Click to search")));
        gui.setItem(33, createItem(Material.REDSTONE, "§c§lReload", Arrays.asList("§7Reload plugin", "§7configuration", "", "§e» Click to reload")));
        
        gui.setItem(49, createItem(Material.BARRIER, "§c§lClose", Arrays.asList("§7Close this menu")));
        
        fillEmptyWithPattern(gui);
        player.openInventory(gui);
    }
    
    public void openPlayerSelectionGUI(Player player, String action) {
        playerActions.put(player, action);
        String title = "§8§lSelect Player §8- §c" + action;
        Inventory gui = Bukkit.createInventory(new GUIHolder("player_select", title), 54, title);
        
        for (int i = 0; i < 9; i++) {
            gui.setItem(i, createItem(Material.GRAY_STAINED_GLASS_PANE, " ", Arrays.asList()));
        }
        for (int i = 45; i < 54; i++) {
            gui.setItem(i, createItem(Material.GRAY_STAINED_GLASS_PANE, " ", Arrays.asList()));
        }
        
        List<Player> onlinePlayers = Arrays.asList(Bukkit.getOnlinePlayers().toArray(new Player[0]));
        int slot = 10;
        
        for (int i = 0; i < Math.min(onlinePlayers.size(), 28); i++) {
            Player target = onlinePlayers.get(i);
            ItemStack head = createEnhancedPlayerHead(target, action);
            gui.setItem(slot, head);
            
            slot++;
            if ((slot + 1) % 9 == 0) slot += 2;
            if (slot > 43) break;
        }
        
        gui.setItem(48, createItem(Material.ARROW, "§7Back", Arrays.asList("§7Return to main menu")));
        gui.setItem(49, createItem(Material.BARRIER, "§c§lClose", Arrays.asList("§7Close this menu")));
        gui.setItem(50, createGlowingItem(Material.NAME_TAG, "§e§lSearch Offline", Arrays.asList("§7Search for an offline player", "", "§e» Click to search")));
        
        fillEmptyWithPattern(gui);
        player.openInventory(gui);
    }
    
    public void openPunishmentGUI(Player player, String targetName, String action) {
        String title = "§c" + action + " - " + targetName;
        Inventory gui = Bukkit.createInventory(new GUIHolder("punishment", title), 54, title);
        
        if (action.contains("Ban") || action.contains("Mute") || action.contains("Kick")) {
            if (action.contains("Kick")) {
                gui.setItem(10, createItem(Material.PAPER, "§7AFK/Idle", Arrays.asList("§7Reason: AFK/Idle")));
                gui.setItem(11, createItem(Material.PAPER, "§7Server Full", Arrays.asList("§7Reason: Server Full")));
                gui.setItem(12, createItem(Material.PAPER, "§7Inappropriate Behavior", Arrays.asList("§7Reason: Inappropriate Behavior")));
                gui.setItem(13, createItem(Material.PAPER, "§7Spam/Flood", Arrays.asList("§7Reason: Spam/Flood")));
                gui.setItem(14, createItem(Material.PAPER, "§7Wrong Server", Arrays.asList("§7Reason: Wrong Server")));
                gui.setItem(15, createItem(Material.PAPER, "§7Maintenance", Arrays.asList("§7Reason: Maintenance")));
                gui.setItem(16, createItem(Material.PAPER, "§7Other", Arrays.asList("§7Reason: Other")));
            } else {
                gui.setItem(10, createItem(Material.PAPER, "§7Griefing", Arrays.asList("§7Reason: Griefing")));
                gui.setItem(11, createItem(Material.PAPER, "§7Cheating/Hacking", Arrays.asList("§7Reason: Cheating/Hacking")));
                gui.setItem(12, createItem(Material.PAPER, "§7Inappropriate Chat", Arrays.asList("§7Reason: Inappropriate Chat")));
                gui.setItem(13, createItem(Material.PAPER, "§7Spam", Arrays.asList("§7Reason: Spam")));
                gui.setItem(14, createItem(Material.PAPER, "§7Advertising", Arrays.asList("§7Reason: Advertising")));
                gui.setItem(15, createItem(Material.PAPER, "§7Disrespect", Arrays.asList("§7Reason: Disrespect")));
                gui.setItem(16, createItem(Material.PAPER, "§7Other", Arrays.asList("§7Reason: Other")));
            }
            
            if (action.contains("Temp")) {
                gui.setItem(28, createItem(Material.CLOCK, "§e1 Hour", Arrays.asList("§7Duration: 1 Hour")));
                gui.setItem(29, createItem(Material.CLOCK, "§e1 Day", Arrays.asList("§7Duration: 1 Day")));
                gui.setItem(30, createItem(Material.CLOCK, "§e1 Week", Arrays.asList("§7Duration: 1 Week")));
                gui.setItem(31, createItem(Material.CLOCK, "§e1 Month", Arrays.asList("§7Duration: 1 Month")));
                gui.setItem(32, createItem(Material.CLOCK, "§e3 Months", Arrays.asList("§7Duration: 3 Months")));
                gui.setItem(33, createItem(Material.CLOCK, "§e6 Months", Arrays.asList("§7Duration: 6 Months")));
                gui.setItem(34, createItem(Material.CLOCK, "§eCustom", Arrays.asList("§7Set custom duration")));
            }
        }
        
        gui.setItem(48, createItem(Material.ARROW, "§7Back", Arrays.asList("§7Return to player selection")));
        gui.setItem(49, createItem(Material.BARRIER, "§cCancel", Arrays.asList("§7Cancel this action")));
        gui.setItem(50, createItem(Material.LIME_DYE, "§aConfirm", Arrays.asList("§7Confirm " + action)));
        
        fillEmpty(gui);
        player.openInventory(gui);
    }
    
    public void openHistoryGUI(Player player, String targetName) {
        String title = "§cHistory - " + targetName;
        Inventory gui = Bukkit.createInventory(new GUIHolder("history", title), 54, title);
        
        gui.setItem(49, createItem(Material.BARRIER, "§cClose", Arrays.asList("§7Close this menu")));
        
        fillEmpty(gui);
        player.openInventory(gui);
    }
    
    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createPlayerHead(Player player) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = head.getItemMeta();
        meta.setDisplayName("§e" + player.getName());
        meta.setLore(Arrays.asList("§7Click to select this player"));
        head.setItemMeta(meta);
        return head;
    }
    
    private ItemStack createEnhancedPlayerHead(Player player, String action) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(player);
        meta.setDisplayName("§e§l" + player.getName());
        
        List<String> lore = new ArrayList<>();
        lore.add("§7Action: §c" + action);
        lore.add("");
        
        if (player.isOp()) {
            lore.add("§cOperator");
        }
        
        int warnings = punishmentData.getWarningCount(player.getName());
        if (warnings > 0) {
            lore.add("§eWarnings: §6" + warnings);
        }
        
        lore.add("");
        lore.add("§e» Click to select");
        
        meta.setLore(lore);
        head.setItemMeta(meta);
        return head;
    }
    
    private void fillEmpty(Inventory gui) {
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        meta.setDisplayName(" ");
        glass.setItemMeta(meta);
        
        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, glass);
            }
        }
    }
    
    private void fillEmptyWithPattern(Inventory gui) {
        ItemStack blackGlass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta blackMeta = blackGlass.getItemMeta();
        blackMeta.setDisplayName(" ");
        blackGlass.setItemMeta(blackMeta);
        
        ItemStack grayGlass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta grayMeta = grayGlass.getItemMeta();
        grayMeta.setDisplayName(" ");
        grayGlass.setItemMeta(grayMeta);
        
        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null) {
                if ((i / 9 + i % 9) % 2 == 0) {
                    gui.setItem(i, blackGlass);
                } else {
                    gui.setItem(i, grayGlass);
                }
            }
        }
    }
    
    private ItemStack createGlowingItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        meta.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }
    
    private void addGlow(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
    }
    
    private ItemStack createBulkSelectionPlayerHead(Player player, boolean isSelected) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(player);
        meta.setDisplayName("§e§l" + player.getName());
        
        List<String> lore = new ArrayList<>();
        if (isSelected) {
            lore.add("§a§lSELECTED");
            lore.add("");
            lore.add("§cClick to deselect");
        } else {
            lore.add("§7Not selected");
            lore.add("");
            lore.add("§aClick to select");
        }
        
        meta.setLore(lore);
        head.setItemMeta(meta);
        
        if (isSelected) {
            addGlow(head);
        }
        
        return head;
    }
    
    public String getPlayerAction(Player player) {
        return playerActions.get(player);
    }
    
    public void setPlayerAction(Player player, String action) {
        playerActions.put(player, action);
    }
    
    public void removePlayerAction(Player player) {
        playerActions.remove(player);
        playerPages.remove(player);
    }
    
    public void openUnbanGUI(Player player) {
        openUnbanGUI(player, 0);
    }
    
    public void openUnbanGUI(Player player, int page) {
        playerPages.put(player, page);
        String title = "§cUnban Player - Page " + (page + 1);
        Inventory gui = Bukkit.createInventory(new GUIHolder("unban", title), 54, title);
        
        gui.setItem(22, createItem(Material.CLOCK, "§eLoading...", Arrays.asList("§7Fetching banned players")));
        gui.setItem(49, createItem(Material.BARRIER, "§cBack", Arrays.asList("§7Return to main menu")));
        fillEmpty(gui);
        player.openInventory(gui);
        
        banCache.getBannedPlayersAsync().thenAccept(bannedPlayers -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (player.getOpenInventory().getTopInventory().getHolder() == null || 
                    !(player.getOpenInventory().getTopInventory().getHolder() instanceof GUIHolder)) return;
                
                gui.clear();
                
                if (bannedPlayers.isEmpty()) {
                    gui.setItem(22, createItem(Material.BARRIER, "§cNo banned players", Arrays.asList("§7There are no banned players")));
                } else {
                    int startIndex = page * 28;
                    int slot = 10;
                    
                    for (int i = startIndex; i < Math.min(startIndex + 28, bannedPlayers.size()); i++) {
                        BanCache.BannedPlayerInfo banInfo = bannedPlayers.get(i);
                        ItemStack head = createBannedPlayerHeadFromInfo(banInfo);
                        gui.setItem(slot, head);
                        
                        slot++;
                        if ((slot + 1) % 9 == 0) slot += 2;
                        if (slot > 43) break;
                    }
                    
                    if (page > 0) {
                        gui.setItem(48, createItem(Material.ARROW, "§7Previous Page", Arrays.asList("§7Go to page " + page)));
                    }
                    
                    if (startIndex + 28 < bannedPlayers.size()) {
                        gui.setItem(50, createItem(Material.ARROW, "§7Next Page", Arrays.asList("§7Go to page " + (page + 2))));
                    }
                }
                
                gui.setItem(49, createItem(Material.BARRIER, "§cBack", Arrays.asList("§7Return to main menu")));
                fillEmpty(gui);
                player.updateInventory();
            });
        });
    }
    
    public void openUnmuteGUI(Player player) {
        openUnmuteGUI(player, 0);
    }
    
    public void openUnmuteGUI(Player player, int page) {
        playerPages.put(player, page);
        String title = "§cUnmute Player - Page " + (page + 1);
        Inventory gui = Bukkit.createInventory(new GUIHolder("unmute", title), 54, title);
        
        gui.setItem(22, createItem(Material.CLOCK, "§eLoading...", Arrays.asList("§7Fetching muted players")));
        gui.setItem(49, createItem(Material.BARRIER, "§cBack", Arrays.asList("§7Return to main menu")));
        fillEmpty(gui);
        player.openInventory(gui);
        
        CompletableFuture.supplyAsync(() -> punishmentData.getMutedPlayers()).thenAccept(mutedPlayers -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (player.getOpenInventory().getTopInventory().getHolder() == null || 
                    !(player.getOpenInventory().getTopInventory().getHolder() instanceof GUIHolder)) return;
                
                gui.clear();
                
                if (mutedPlayers.isEmpty()) {
                    gui.setItem(22, createItem(Material.BARRIER, "§cNo muted players", Arrays.asList("§7There are no muted players")));
                } else {
                    int startIndex = page * 28;
                    int slot = 10;
                    
                    for (int i = startIndex; i < Math.min(startIndex + 28, mutedPlayers.size()); i++) {
                        String mutedName = mutedPlayers.get(i);
                        OfflinePlayer muted = Bukkit.getOfflinePlayer(mutedName);
                        ItemStack head = createMutedPlayerHead(muted);
                        gui.setItem(slot, head);
                        
                        slot++;
                        if ((slot + 1) % 9 == 0) slot += 2;
                        if (slot > 43) break;
                    }
                    
                    if (page > 0) {
                        gui.setItem(48, createItem(Material.ARROW, "§7Previous Page", Arrays.asList("§7Go to page " + page)));
                    }
                    
                    if (startIndex + 28 < mutedPlayers.size()) {
                        gui.setItem(50, createItem(Material.ARROW, "§7Next Page", Arrays.asList("§7Go to page " + (page + 2))));
                    }
                }
                
                gui.setItem(49, createItem(Material.BARRIER, "§cBack", Arrays.asList("§7Return to main menu")));
                fillEmpty(gui);
                player.updateInventory();
            });
        });
    }
    
    private ItemStack createBannedPlayerHeadFromInfo(BanCache.BannedPlayerInfo banInfo) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(banInfo.name));
        meta.setDisplayName("§c" + banInfo.name);
        
        meta.setLore(Arrays.asList(
            "§7Reason: " + (banInfo.reason != null && !banInfo.reason.isEmpty() ? banInfo.reason : "No reason"),
            "§7Banned by: " + (banInfo.source != null ? banInfo.source : "Unknown"),
            "§7",
            "§aClick to unban"
        ));
        
        head.setItemMeta(meta);
        return head;
    }
    
    private ItemStack createMutedPlayerHead(OfflinePlayer player) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(player);
        meta.setDisplayName("§e" + player.getName());
        
        meta.setLore(Arrays.asList(
            "§7Status: Muted",
            "§7",
            "§aClick to unmute"
        ));
        
        head.setItemMeta(meta);
        return head;
    }
    
    public int getPlayerPage(Player player) {
        return playerPages.getOrDefault(player, 0);
    }
    
    public void invalidateBanCache() {
        banCache.invalidateCache();
    }
    
    
    public void openAdvancedToolsGUI(Player player) {
        Inventory gui = Bukkit.createInventory(new GUIHolder("advanced", "§5§lAdvanced Tools"), 54, "§5§lAdvanced Tools");
        
        for (int i = 0; i < 9; i++) {
            gui.setItem(i, createItem(Material.PURPLE_STAINED_GLASS_PANE, " ", Arrays.asList()));
        }
        
        gui.setItem(11, createGlowingItem(Material.HOPPER, "§e§lBulk Actions", Arrays.asList("§7Perform actions on", "§7multiple players", "", "§e» Click to open")));
        gui.setItem(13, createGlowingItem(Material.ENDER_CHEST, "§b§lBan Import/Export", Arrays.asList("§7Import or export", "§7ban data", "", "§e» Click to manage")));
        gui.setItem(15, createGlowingItem(Material.REPEATER, "§c§lAuto Moderation", Arrays.asList("§7Configure automatic", "§7moderation rules", "", "§e» Click to configure")));
        
        gui.setItem(29, createGlowingItem(Material.COMPARATOR, "§a§lPlugin Integration", Arrays.asList("§7Configure integrations", "§7with other plugins", "", "§e» Click to manage")));
        gui.setItem(31, createGlowingItem(Material.OBSERVER, "§d§lAudit Log", Arrays.asList("§7View all moderator", "§7actions and changes", "", "§e» Click to view")));
        gui.setItem(33, createGlowingItem(Material.STRUCTURE_BLOCK, "§6§lBackup & Restore", Arrays.asList("§7Backup and restore", "§7punishment data", "", "§e» Click to manage")));
        
        gui.setItem(49, createItem(Material.ARROW, "§7Back", Arrays.asList("§7Return to main menu")));
        
        fillEmptyWithPattern(gui);
        player.openInventory(gui);
    }
    
    public void openStatisticsGUI(Player player) {
        Inventory gui = Bukkit.createInventory(new GUIHolder("statistics", "§d§lStatistics"), 54, "§d§lStatistics");
        
        long totalBans = punishmentData.getTotalBans();
        long totalMutes = punishmentData.getTotalMutes();
        long totalKicks = punishmentData.getTotalKicks();
        long totalWarnings = punishmentData.getTotalWarnings();
        
        gui.setItem(11, createGlowingItem(Material.REDSTONE_BLOCK, "§c§lTotal Bans", Arrays.asList("§7Total bans issued:", "§f" + totalBans, "", "§7Active bans:", "§f" + Bukkit.getBanList(org.bukkit.BanList.Type.NAME).getBanEntries().size())));
        gui.setItem(13, createGlowingItem(Material.GOLD_BLOCK, "§e§lTotal Mutes", Arrays.asList("§7Total mutes issued:", "§f" + totalMutes, "", "§7Active mutes:", "§f" + punishmentData.getMutedPlayers().size())));
        gui.setItem(15, createGlowingItem(Material.IRON_SWORD, "§c§lTotal Kicks", Arrays.asList("§7Total kicks issued:", "§f" + totalKicks)));
        
        gui.setItem(29, createGlowingItem(Material.BELL, "§e§lTotal Warnings", Arrays.asList("§7Total warnings issued:", "§f" + totalWarnings)));
        gui.setItem(31, createGlowingItem(Material.CLOCK, "§b§lRecent Activity", Arrays.asList("§7View recent punishment", "§7activity", "", "§e» Click to view")));
        gui.setItem(33, createGlowingItem(Material.BOOK, "§a§lTop Offenders", Arrays.asList("§7Players with most", "§7punishments", "", "§e» Click to view")));
        
        gui.setItem(49, createItem(Material.ARROW, "§7Back", Arrays.asList("§7Return to main menu")));
        
        fillEmptyWithPattern(gui);
        player.openInventory(gui);
    }
    
    public void openWarningsGUI(Player player) {
        Inventory gui = Bukkit.createInventory(new GUIHolder("warnings", "§e§lWarnings Management"), 54, "§e§lWarnings Management");
        
        gui.setItem(11, createGlowingItem(Material.YELLOW_DYE, "§e§lIssue Warning", Arrays.asList("§7Give a warning to", "§7a player", "", "§e» Click to select player")));
        gui.setItem(13, createGlowingItem(Material.BOOK, "§6§lView Warnings", Arrays.asList("§7View all warnings", "§7for a player", "", "§e» Click to select player")));
        gui.setItem(15, createGlowingItem(Material.LIME_DYE, "§a§lClear Warnings", Arrays.asList("§7Clear warnings for", "§7a player", "", "§e» Click to select player")));
        
        gui.setItem(31, createGlowingItem(Material.REDSTONE_TORCH, "§c§lWarning Settings", Arrays.asList("§7Configure warning", "§7thresholds and actions", "", "§e» Click to configure")));
        
        gui.setItem(49, createItem(Material.ARROW, "§7Back", Arrays.asList("§7Return to main menu")));
        
        fillEmptyWithPattern(gui);
        player.openInventory(gui);
    }
    
    public void openIPManagerGUI(Player player) {
        Inventory gui = Bukkit.createInventory(new GUIHolder("ipmanager", "§c§lIP Manager"), 54, "§c§lIP Manager");
        
        boolean vpnEnabled = plugin.getConfig().getBoolean("vpn-detection.enabled", false);
        
        gui.setItem(11, createGlowingItem(Material.REDSTONE_BLOCK, "§c§lIP Ban", Arrays.asList("§7Ban an IP address", "§7from the server", "", "§e» Click to ban IP")));
        gui.setItem(13, createGlowingItem(Material.EMERALD_BLOCK, "§a§lIP Unban", Arrays.asList("§7Remove an IP ban", "", "§7Banned IPs: §c" + plugin.getIPData().getBannedIPs().size(), "", "§e» Click to view banned IPs")));
        gui.setItem(15, createGlowingItem(Material.COMPASS, "§b§lIP Lookup", Arrays.asList("§7Find all players", "§7using an IP", "", "§e» Click to search")));
        
        gui.setItem(29, createGlowingItem(Material.PAPER, "§e§lIP History", Arrays.asList("§7View IP history", "§7for a player", "", "§e» Click to select player")));
        gui.setItem(31, createGlowingItem(Material.OBSERVER, "§d§lVPN Whitelist", Arrays.asList("§7Manage VPN whitelist", "§7for allowed VPNs", "", "§7Whitelisted: §a" + plugin.getVPNDetector().getWhitelist().size(), "", "§e» Click to manage")));
        gui.setItem(33, createGlowingItem(Material.SHIELD, "§6§lVPN Detection", Arrays.asList("§7Configure VPN/Proxy", "§7detection settings", "", "§7Status: " + (vpnEnabled ? "§aEnabled" : "§cDisabled"), "", "§e» Click to toggle")));
        
        gui.setItem(49, createItem(Material.ARROW, "§7Back", Arrays.asList("§7Return to main menu")));
        
        fillEmptyWithPattern(gui);
        player.openInventory(gui);
    }
    
    public void openVPNSettingsGUI(Player player) {
        Inventory gui = Bukkit.createInventory(new GUIHolder("vpnsettings", "§6§lVPN Detection Settings"), 54, "§6§lVPN Detection Settings");
        
        boolean enabled = plugin.getConfig().getBoolean("vpn-detection.enabled", false);
        boolean kickOnDetect = plugin.getConfig().getBoolean("vpn-detection.kick-on-detect", true);
        boolean notifyStaff = plugin.getConfig().getBoolean("vpn-detection.notify-staff", true);
        
        gui.setItem(11, createGlowingItem(enabled ? Material.LIME_DYE : Material.GRAY_DYE, 
            enabled ? "§a§lDetection Enabled" : "§c§lDetection Disabled", 
            Arrays.asList("§7Current status: " + (enabled ? "§aEnabled" : "§cDisabled"), "", "§e» Click to toggle")));
        
        gui.setItem(13, createGlowingItem(kickOnDetect ? Material.REDSTONE : Material.GUNPOWDER, 
            kickOnDetect ? "§c§lAuto-Kick Enabled" : "§7§lAuto-Kick Disabled", 
            Arrays.asList("§7Kick VPN users: " + (kickOnDetect ? "§aYes" : "§cNo"), "", "§e» Click to toggle")));
        
        gui.setItem(15, createGlowingItem(notifyStaff ? Material.BELL : Material.GRAY_DYE, 
            notifyStaff ? "§e§lNotifications Enabled" : "§7§lNotifications Disabled", 
            Arrays.asList("§7Notify staff: " + (notifyStaff ? "§aYes" : "§cNo"), "", "§e» Click to toggle")));
        
        gui.setItem(31, createGlowingItem(Material.PAPER, "§b§lCheck Player IP", 
            Arrays.asList("§7Manually check if a", "§7player is using VPN", "", "§e» Click to check")));
        
        gui.setItem(49, createItem(Material.ARROW, "§7Back", Arrays.asList("§7Return to IP Manager")));
        
        fillEmptyWithPattern(gui);
        player.openInventory(gui);
    }
    
    public void openIPBanListGUI(Player player, int page) {
        Inventory gui = Bukkit.createInventory(new GUIHolder("ipbanlist", "§c§lBanned IPs - Page " + (page + 1)), 54, "§c§lBanned IPs - Page " + (page + 1));
        
        List<String> bannedIPs = plugin.getIPData().getBannedIPs();
        int startIndex = page * 28;
        int slot = 10;
        
        for (int i = startIndex; i < Math.min(startIndex + 28, bannedIPs.size()); i++) {
            String ip = bannedIPs.get(i);
            List<String> players = plugin.getIPData().getPlayersWithIP(ip);
            
            ItemStack item = createGlowingItem(Material.REDSTONE_BLOCK, "§c" + ip, 
                Arrays.asList("§7Associated players:", 
                    players.isEmpty() ? "§7None" : "§e" + String.join(", ", players.subList(0, Math.min(3, players.size()))),
                    players.size() > 3 ? "§7... and " + (players.size() - 3) + " more" : "",
                    "",
                    "§a» Click to unban"));
            gui.setItem(slot, item);
            
            slot++;
            if ((slot + 1) % 9 == 0) slot += 2;
            if (slot > 43) break;
        }
        
        if (page > 0) {
            gui.setItem(48, createItem(Material.ARROW, "§7Previous Page", Arrays.asList("§7Go to page " + page)));
        }
        
        if (startIndex + 28 < bannedIPs.size()) {
            gui.setItem(50, createItem(Material.ARROW, "§7Next Page", Arrays.asList("§7Go to page " + (page + 2))));
        }
        
        gui.setItem(49, createItem(Material.BARRIER, "§cBack", Arrays.asList("§7Return to IP Manager")));
        
        fillEmptyWithPattern(gui);
        player.openInventory(gui);
    }
    
    public void openVPNWhitelistGUI(Player player) {
        Inventory gui = Bukkit.createInventory(new GUIHolder("vpnwhitelist", "§d§lVPN Whitelist"), 54, "§d§lVPN Whitelist");
        
        List<String> whitelist = plugin.getVPNDetector().getWhitelist();
        int slot = 10;
        
        for (int i = 0; i < Math.min(whitelist.size(), 28); i++) {
            String ip = whitelist.get(i);
            ItemStack item = createItem(Material.EMERALD, "§a" + ip, 
                Arrays.asList("§7Whitelisted IP", "", "§c» Click to remove"));
            gui.setItem(slot, item);
            
            slot++;
            if ((slot + 1) % 9 == 0) slot += 2;
            if (slot > 43) break;
        }
        
        gui.setItem(49, createItem(Material.ARROW, "§7Back", Arrays.asList("§7Return to IP Manager")));
        gui.setItem(50, createGlowingItem(Material.LIME_DYE, "§a§lAdd IP", Arrays.asList("§7Add an IP to", "§7the whitelist", "", "§e» Click to add")));
        
        fillEmptyWithPattern(gui);
        player.openInventory(gui);
    }
    
    public void openTemplatesGUI(Player player) {
        Inventory gui = Bukkit.createInventory(new GUIHolder("templates", "§b§lPunishment Templates"), 54, "§b§lPunishment Templates");
        
        gui.setItem(10, createGlowingItem(Material.PAPER, "§c§lHacking", Arrays.asList("§7Template: Hacking/Cheating", "§7Duration: Permanent Ban", "", "§e» Click to use")));
        gui.setItem(11, createGlowingItem(Material.PAPER, "§c§lGriefing", Arrays.asList("§7Template: Griefing", "§7Duration: 30 Day Ban", "", "§e» Click to use")));
        gui.setItem(12, createGlowingItem(Material.PAPER, "§6§lChat Abuse", Arrays.asList("§7Template: Chat Abuse", "§7Duration: 7 Day Mute", "", "§e» Click to use")));
        gui.setItem(13, createGlowingItem(Material.PAPER, "§e§lSpamming", Arrays.asList("§7Template: Spamming", "§7Duration: 1 Hour Mute", "", "§e» Click to use")));
        gui.setItem(14, createGlowingItem(Material.PAPER, "§6§lAdvertising", Arrays.asList("§7Template: Advertising", "§7Duration: Permanent Mute", "", "§e» Click to use")));
        gui.setItem(15, createGlowingItem(Material.PAPER, "§c§lBan Evasion", Arrays.asList("§7Template: Ban Evasion", "§7Duration: IP Ban", "", "§e» Click to use")));
        gui.setItem(16, createGlowingItem(Material.PAPER, "§e§lMinor Offense", Arrays.asList("§7Template: Minor Offense", "§7Duration: Warning", "", "§e» Click to use")));
        
        gui.setItem(31, createGlowingItem(Material.WRITABLE_BOOK, "§a§lCreate Template", Arrays.asList("§7Create a custom", "§7punishment template", "", "§e» Click to create")));
        
        gui.setItem(49, createItem(Material.ARROW, "§7Back", Arrays.asList("§7Return to main menu")));
        
        fillEmptyWithPattern(gui);
        player.openInventory(gui);
    }
    
    public void openBulkActionsGUI(Player player) {
        Inventory gui = Bukkit.createInventory(new GUIHolder("bulkactions", "§e§lBulk Actions"), 54, "§e§lBulk Actions");
        
        gui.setItem(10, createGlowingItem(Material.CHEST, "§a§lSelect Players", 
            Arrays.asList("§7Select multiple players", "§7for bulk actions", "", "§e» Click to select")));
        
        List<String> selected = plugin.getBulkActions().getSelectedPlayers(player);
        if (!selected.isEmpty()) {
            gui.setItem(12, createGlowingItem(Material.REDSTONE_BLOCK, "§c§lBulk Ban", 
                Arrays.asList("§7Ban all selected players", "§7Selected: §e" + selected.size(), "", "§c» Click to ban all")));
            gui.setItem(13, createGlowingItem(Material.GOLD_BLOCK, "§e§lBulk Mute", 
                Arrays.asList("§7Mute all selected players", "§7Selected: §e" + selected.size(), "", "§e» Click to mute all")));
            gui.setItem(14, createGlowingItem(Material.IRON_SWORD, "§c§lBulk Kick", 
                Arrays.asList("§7Kick all selected players", "§7Selected: §e" + selected.size(), "", "§c» Click to kick all")));
            gui.setItem(16, createItem(Material.BARRIER, "§c§lClear Selection", 
                Arrays.asList("§7Clear all selected", "§7players", "", "§e» Click to clear")));
        }
        
        gui.setItem(49, createItem(Material.ARROW, "§7Back", Arrays.asList("§7Return to advanced tools")));
        
        fillEmptyWithPattern(gui);
        player.openInventory(gui);
    }
    
    public void openBulkPlayerSelectionGUI(Player player, int page) {
        playerPages.put(player, page);
        String title = "§e§lSelect Players - Page " + (page + 1);
        Inventory gui = Bukkit.createInventory(new GUIHolder("bulkselect", title), 54, title);
        
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        List<String> selected = plugin.getBulkActions().getSelectedPlayers(player);
        
        int startIndex = page * 28;
        int slot = 10;
        
        for (int i = startIndex; i < Math.min(startIndex + 28, onlinePlayers.size()); i++) {
            Player target = onlinePlayers.get(i);
            boolean isSelected = selected.contains(target.getName());
            
            ItemStack head = createBulkSelectionPlayerHead(target, isSelected);
            gui.setItem(slot, head);
            
            slot++;
            if ((slot + 1) % 9 == 0) slot += 2;
            if (slot > 43) break;
        }
        
        if (page > 0) {
            gui.setItem(48, createItem(Material.ARROW, "§7Previous Page", Arrays.asList("§7Go to page " + page)));
        }
        
        if (startIndex + 28 < onlinePlayers.size()) {
            gui.setItem(50, createItem(Material.ARROW, "§7Next Page", Arrays.asList("§7Go to page " + (page + 2))));
        }
        
        gui.setItem(49, createItem(Material.EMERALD_BLOCK, "§a§lConfirm Selection", 
            Arrays.asList("§7Selected: §e" + selected.size() + " players", "", "§a» Click to continue")));
        
        fillEmptyWithPattern(gui);
        player.openInventory(gui);
    }
    
    public void openImportExportGUI(Player player) {
        Inventory gui = Bukkit.createInventory(new GUIHolder("importexport", "§b§lImport/Export"), 54, "§b§lImport/Export");
        
        gui.setItem(12, createGlowingItem(Material.CHEST, "§a§lExport Bans", 
            Arrays.asList("§7Export all bans and", "§7mutes to a file", "", "§e» Click to export")));
        
        gui.setItem(14, createGlowingItem(Material.HOPPER, "§b§lImport Bans", 
            Arrays.asList("§7Import bans from", "§7a file", "", "§e» Click to select file")));
        
        List<File> exportFiles = plugin.getImportExport().getExportFiles();
        if (!exportFiles.isEmpty()) {
            gui.setItem(31, createGlowingItem(Material.BOOK, "§e§lRecent Exports", 
                Arrays.asList("§7Found §e" + exportFiles.size() + " §7export files", 
                    "§7Latest: §f" + exportFiles.get(exportFiles.size() - 1).getName(), 
                    "", "§e» Click to view")));
        }
        
        gui.setItem(49, createItem(Material.ARROW, "§7Back", Arrays.asList("§7Return to advanced tools")));
        
        fillEmptyWithPattern(gui);
        player.openInventory(gui);
    }
    
    public void openAutoModSettingsGUI(Player player) {
        Inventory gui = Bukkit.createInventory(new GUIHolder("automod", "§c§lAuto Moderation"), 54, "§c§lAuto Moderation");
        
        boolean enabled = plugin.getConfig().getBoolean("auto-mod.enabled", false);
        
        gui.setItem(10, createGlowingItem(enabled ? Material.LIME_DYE : Material.GRAY_DYE, 
            enabled ? "§a§lEnabled" : "§c§lDisabled", 
            Arrays.asList("§7Auto moderation is", "§7currently " + (enabled ? "§aenabled" : "§cdisabled"), 
                "", "§e» Click to toggle")));
        
        gui.setItem(12, createItem(Material.PAPER, "§e§lSpam Detection", 
            Arrays.asList("§7Current: " + (plugin.getConfig().getBoolean("auto-mod.detect-spam", true) ? "§aEnabled" : "§cDisabled"),
                "§7Detects spam messages", "", "§e» Click to toggle")));
        
        gui.setItem(13, createItem(Material.IRON_BARS, "§c§lCaps Detection", 
            Arrays.asList("§7Current: " + (plugin.getConfig().getBoolean("auto-mod.detect-caps", true) ? "§aEnabled" : "§cDisabled"),
                "§7Max caps: §e" + plugin.getConfig().getInt("auto-mod.max-caps-percent", 70) + "%", 
                "", "§e» Click to toggle")));
        
        gui.setItem(14, createItem(Material.BARRIER, "§6§lBlocked Words", 
            Arrays.asList("§7Manage blocked", "§7words list", 
                "§7Current: §e" + plugin.getConfig().getStringList("auto-mod.blocked-words").size() + " words",
                "", "§e» Click to manage")));
        
        gui.setItem(15, createItem(Material.NAME_TAG, "§b§lAdvertising Detection", 
            Arrays.asList("§7Current: " + (plugin.getConfig().getBoolean("auto-mod.detect-advertising", true) ? "§aEnabled" : "§cDisabled"),
                "§7Detects IPs and URLs", "", "§e» Click to toggle")));
        
        gui.setItem(31, createGlowingItem(Material.REDSTONE, "§c§lPunishment Settings", 
            Arrays.asList("§7Configure auto-mod", "§7punishments", "", "§e» Click to configure")));
        
        gui.setItem(49, createItem(Material.ARROW, "§7Back", Arrays.asList("§7Return to advanced tools")));
        
        fillEmptyWithPattern(gui);
        player.openInventory(gui);
    }
    
    public void openAuditLogGUI(Player player, int page) {
        Inventory gui = Bukkit.createInventory(new GUIHolder("auditlog", "§d§lAudit Log - Page " + (page + 1)), 54, "§d§lAudit Log - Page " + (page + 1));
        
        List<AuditLog.LogEntry> entries = plugin.getAuditLog().getRecentEntries(page * 28, 28);
        int slot = 10;
        
        for (AuditLog.LogEntry entry : entries) {
            String timeAgo = TimeUtil.getTimeAgo(entry.timestamp);
            ItemStack item = createItem(Material.WRITABLE_BOOK, "§e" + entry.action, 
                Arrays.asList("§7Moderator: §f" + entry.moderator,
                    "§7Target: §f" + entry.target,
                    "§7Details: §f" + entry.details,
                    "§7Time: §f" + timeAgo));
            gui.setItem(slot, item);
            
            slot++;
            if ((slot + 1) % 9 == 0) slot += 2;
            if (slot > 43) break;
        }
        
        if (page > 0) {
            gui.setItem(48, createItem(Material.ARROW, "§7Previous Page", Arrays.asList("§7Go to page " + page)));
        }
        
        if (entries.size() == 28) {
            gui.setItem(50, createItem(Material.ARROW, "§7Next Page", Arrays.asList("§7Go to page " + (page + 2))));
        }
        
        gui.setItem(49, createItem(Material.ARROW, "§7Back", Arrays.asList("§7Return to advanced tools")));
        gui.setItem(53, createGlowingItem(Material.COMPASS, "§e§lSearch", 
            Arrays.asList("§7Search audit logs", "§7by player or action", "", "§e» Click to search")));
        
        fillEmptyWithPattern(gui);
        player.openInventory(gui);
    }
    
    public void openBackupRestoreGUI(Player player) {
        Inventory gui = Bukkit.createInventory(new GUIHolder("backup", "§6§lBackup & Restore"), 54, "§6§lBackup & Restore");
        
        gui.setItem(12, createGlowingItem(Material.CHEST, "§a§lCreate Backup", 
            Arrays.asList("§7Create a backup of all", "§7punishment data", "", "§e» Click to backup")));
        
        gui.setItem(14, createGlowingItem(Material.ENDER_CHEST, "§b§lRestore Backup", 
            Arrays.asList("§7Restore from a", "§7previous backup", "", "§e» Click to select")));
        
        File backupDir = new File(plugin.getDataFolder(), "backups");
        if (backupDir.exists()) {
            File[] backups = backupDir.listFiles((dir, name) -> name.endsWith(".yml"));
            if (backups != null && backups.length > 0) {
                gui.setItem(31, createItem(Material.BOOK, "§e§lRecent Backups", 
                    Arrays.asList("§7Found §e" + backups.length + " §7backups",
                        "§7Latest: §f" + backups[backups.length - 1].getName(),
                        "", "§e» Click to view")));
            }
        }
        
        gui.setItem(49, createItem(Material.ARROW, "§7Back", Arrays.asList("§7Return to advanced tools")));
        
        fillEmptyWithPattern(gui);
        player.openInventory(gui);
    }
}