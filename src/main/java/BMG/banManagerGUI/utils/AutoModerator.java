package BMG.banManagerGUI.utils;

import BMG.banManagerGUI.BanManagerGUI;
import BMG.banManagerGUI.data.PunishmentData;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.regex.Pattern;

public class AutoModerator {
    private final BanManagerGUI plugin;
    private final PunishmentData punishmentData;
    private final Map<UUID, List<Long>> messageHistory = new HashMap<>();
    private final Map<UUID, String> lastMessage = new HashMap<>();
    private final Map<UUID, Integer> violations = new HashMap<>();
    
    private List<String> blockedWords = new ArrayList<>();
    private List<Pattern> blockedPatterns = new ArrayList<>();
    
    public AutoModerator(BanManagerGUI plugin, PunishmentData punishmentData) {
        this.plugin = plugin;
        this.punishmentData = punishmentData;
        loadConfiguration();
    }
    
    private void loadConfiguration() {
        blockedWords = plugin.getConfig().getStringList("auto-mod.blocked-words");
        for (String pattern : plugin.getConfig().getStringList("auto-mod.blocked-patterns")) {
            try {
                blockedPatterns.add(Pattern.compile(pattern, Pattern.CASE_INSENSITIVE));
            } catch (Exception e) {
                plugin.getLogger().warning("Invalid regex pattern: " + pattern);
            }
        }
    }
    
    public CheckResult checkMessage(Player player, String message) {
        UUID uuid = player.getUniqueId();
        String lowerMessage = message.toLowerCase();
        
        for (String word : blockedWords) {
            if (lowerMessage.contains(word.toLowerCase())) {
                incrementViolations(uuid);
                return new CheckResult(false, "Blocked word detected: " + word);
            }
        }
        
        for (Pattern pattern : blockedPatterns) {
            if (pattern.matcher(message).find()) {
                incrementViolations(uuid);
                return new CheckResult(false, "Message matches blocked pattern");
            }
        }
        
        if (isSpam(uuid, message)) {
            incrementViolations(uuid);
            return new CheckResult(false, "Spam detected");
        }
        
        if (isCapsLock(message)) {
            incrementViolations(uuid);
            return new CheckResult(false, "Excessive caps lock");
        }
        
        if (isAdvertising(message)) {
            incrementViolations(uuid);
            return new CheckResult(false, "Advertising detected");
        }
        
        updateMessageHistory(uuid, message);
        return new CheckResult(true, null);
    }
    
    private boolean isSpam(UUID uuid, String message) {
        List<Long> history = messageHistory.getOrDefault(uuid, new ArrayList<>());
        long currentTime = System.currentTimeMillis();
        
        history.removeIf(time -> currentTime - time > 10000);
        
        if (history.size() >= plugin.getConfig().getInt("auto-mod.spam-threshold", 5)) {
            return true;
        }
        
        String last = lastMessage.get(uuid);
        if (last != null && last.equalsIgnoreCase(message)) {
            return true;
        }
        
        return false;
    }
    
    private boolean isCapsLock(String message) {
        if (message.length() < 5) return false;
        
        int capsCount = 0;
        int letterCount = 0;
        
        for (char c : message.toCharArray()) {
            if (Character.isLetter(c)) {
                letterCount++;
                if (Character.isUpperCase(c)) {
                    capsCount++;
                }
            }
        }
        
        if (letterCount == 0) return false;
        
        double capsPercentage = (double) capsCount / letterCount;
        return capsPercentage > plugin.getConfig().getDouble("auto-mod.caps-threshold", 0.8);
    }
    
    private boolean isAdvertising(String message) {
        String[] adPatterns = {
            "\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b",
            "\\b[a-zA-Z0-9.-]+\\.(com|net|org|co|uk|de|fr)\\b",
            "join\\s+my\\s+server",
            "play\\s+on\\s+\\w+"
        };
        
        for (String pattern : adPatterns) {
            if (Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(message).find()) {
                return true;
            }
        }
        
        return false;
    }
    
    private void updateMessageHistory(UUID uuid, String message) {
        List<Long> history = messageHistory.computeIfAbsent(uuid, k -> new ArrayList<>());
        history.add(System.currentTimeMillis());
        lastMessage.put(uuid, message);
    }
    
    private void incrementViolations(UUID uuid) {
        int count = violations.getOrDefault(uuid, 0) + 1;
        violations.put(uuid, count);
        
        Player player = plugin.getServer().getPlayer(uuid);
        if (player != null) {
            int warnThreshold = plugin.getConfig().getInt("auto-mod.warn-threshold", 3);
            int muteThreshold = plugin.getConfig().getInt("auto-mod.mute-threshold", 5);
            int kickThreshold = plugin.getConfig().getInt("auto-mod.kick-threshold", 10);
            
            if (count == warnThreshold) {
                punishmentData.addWarning(player.getName(), "Auto-mod: Multiple violations", "AutoMod");
                player.sendMessage("§cWarning: You have violated chat rules multiple times!");
            } else if (count == muteThreshold) {
                punishmentData.addPunishment(player.getName(), "TEMPMUTE", "Auto-mod: Excessive violations", "AutoMod", 300000);
                player.sendMessage("§cYou have been temporarily muted for 5 minutes!");
            } else if (count >= kickThreshold) {
                player.kickPlayer("§cKicked by AutoMod: Excessive chat violations");
                punishmentData.addPunishment(player.getName(), "KICK", "Auto-mod: Excessive violations", "AutoMod", 0);
            }
        }
    }
    
    public void resetViolations(UUID uuid) {
        violations.remove(uuid);
        messageHistory.remove(uuid);
        lastMessage.remove(uuid);
    }
    
    public int getViolationCount(UUID uuid) {
        return violations.getOrDefault(uuid, 0);
    }
    
    public static class CheckResult {
        public final boolean allowed;
        public final String reason;
        
        public CheckResult(boolean allowed, String reason) {
            this.allowed = allowed;
            this.reason = reason;
        }
    }
}