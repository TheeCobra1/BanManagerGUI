package BMG.banManagerGUI.data;

import BMG.banManagerGUI.BanManagerGUI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PunishmentData {
    private final BanManagerGUI plugin;
    private File dataFile;
    private FileConfiguration dataConfig;
    
    public PunishmentData(BanManagerGUI plugin) {
        this.plugin = plugin;
        setupDataFile();
    }
    
    private void setupDataFile() {
        dataFile = new File(plugin.getDataFolder(), "punishments.yml");
        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }
    
    public void addPunishment(String playerName, String type, String reason, String issuer, long duration) {
        String path = "punishments." + playerName + ".";
        List<String> history = dataConfig.getStringList(path + "history");
        
        String entry = new Date().getTime() + ";" + type + ";" + reason + ";" + issuer + ";" + duration;
        history.add(entry);
        
        dataConfig.set(path + "history", history);
        
        if (type.equals("BAN") || type.equals("TEMPBAN")) {
            dataConfig.set(path + "banned", true);
            dataConfig.set(path + "ban-reason", reason);
            dataConfig.set(path + "ban-issuer", issuer);
            dataConfig.set(path + "ban-time", new Date().getTime());
            if (duration > 0) {
                dataConfig.set(path + "ban-expires", new Date().getTime() + duration);
            }
        } else if (type.equals("MUTE") || type.equals("TEMPMUTE")) {
            dataConfig.set(path + "muted", true);
            dataConfig.set(path + "mute-reason", reason);
            dataConfig.set(path + "mute-issuer", issuer);
            dataConfig.set(path + "mute-time", new Date().getTime());
            if (duration > 0) {
                dataConfig.set(path + "mute-expires", new Date().getTime() + duration);
            }
        }
        
        saveData();
    }
    
    public void removeBan(String playerName) {
        String path = "punishments." + playerName + ".";
        dataConfig.set(path + "banned", false);
        dataConfig.set(path + "ban-reason", null);
        dataConfig.set(path + "ban-issuer", null);
        dataConfig.set(path + "ban-time", null);
        dataConfig.set(path + "ban-expires", null);
        saveData();
    }
    
    public void removeMute(String playerName) {
        String path = "punishments." + playerName + ".";
        dataConfig.set(path + "muted", false);
        dataConfig.set(path + "mute-reason", null);
        dataConfig.set(path + "mute-issuer", null);
        dataConfig.set(path + "mute-time", null);
        dataConfig.set(path + "mute-expires", null);
        saveData();
    }
    
    public boolean isBanned(String playerName) {
        if (!Bukkit.getBanList(org.bukkit.BanList.Type.NAME).isBanned(playerName)) {
            return false;
        }
        
        String path = "punishments." + playerName + ".";
        if (dataConfig.contains(path + "ban-expires")) {
            long expires = dataConfig.getLong(path + "ban-expires");
            if (new Date().getTime() > expires) {
                Bukkit.getBanList(org.bukkit.BanList.Type.NAME).pardon(playerName);
                removeBan(playerName);
                return false;
            }
        }
        
        return true;
    }
    
    public boolean isMuted(String playerName) {
        String path = "punishments." + playerName + ".";
        if (!dataConfig.getBoolean(path + "muted", false)) {
            return false;
        }
        
        if (dataConfig.contains(path + "mute-expires")) {
            long expires = dataConfig.getLong(path + "mute-expires");
            if (new Date().getTime() > expires) {
                removeMute(playerName);
                return false;
            }
        }
        
        return true;
    }
    
    public List<String> getHistory(String playerName) {
        return dataConfig.getStringList("punishments." + playerName + ".history");
    }
    
    public List<String> getMutedPlayers() {
        List<String> mutedPlayers = new ArrayList<>();
        if (dataConfig.contains("punishments")) {
            for (String playerName : dataConfig.getConfigurationSection("punishments").getKeys(false)) {
                if (isMuted(playerName)) {
                    mutedPlayers.add(playerName);
                }
            }
        }
        return mutedPlayers;
    }
    
    public long getTotalBans() {
        return countPunishmentType("BAN") + countPunishmentType("TEMPBAN");
    }
    
    public long getTotalMutes() {
        return countPunishmentType("MUTE") + countPunishmentType("TEMPMUTE");
    }
    
    public long getTotalKicks() {
        return countPunishmentType("KICK");
    }
    
    public long getTotalWarnings() {
        return countPunishmentType("WARNING");
    }
    
    private long countPunishmentType(String type) {
        long count = 0;
        if (dataConfig.contains("punishments")) {
            for (String playerName : dataConfig.getConfigurationSection("punishments").getKeys(false)) {
                List<String> history = getHistory(playerName);
                for (String entry : history) {
                    if (entry.contains(";" + type + ";")) {
                        count++;
                    }
                }
            }
        }
        return count;
    }
    
    public void addWarning(String playerName, String reason, String issuer) {
        addPunishment(playerName, "WARNING", reason, issuer, 0);
    }
    
    public int getWarningCount(String playerName) {
        int count = 0;
        for (String entry : getHistory(playerName)) {
            if (entry.contains(";WARNING;")) {
                count++;
            }
        }
        return count;
    }
    
    private void saveData() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public Map<String, Punishment> getAllMutes() {
        Map<String, Punishment> mutes = new HashMap<>();
        
        if (dataConfig.contains("punishments")) {
            for (String playerName : dataConfig.getConfigurationSection("punishments").getKeys(false)) {
                String path = "punishments." + playerName + ".";
                if (dataConfig.getBoolean(path + "muted", false)) {
                    Punishment mute = new Punishment();
                    mute.reason = dataConfig.getString(path + "mute-reason", "No reason");
                    mute.issuer = dataConfig.getString(path + "mute-issuer", "Console");
                    mute.timestamp = dataConfig.getLong(path + "mute-time", 0);
                    long expires = dataConfig.getLong(path + "mute-expires", 0);
                    mute.duration = expires > 0 ? expires - mute.timestamp : 0;
                    mutes.put(playerName, mute);
                }
            }
        }
        
        return mutes;
    }
    
    public static class Punishment {
        public String reason;
        public String issuer;
        public long timestamp;
        public long duration;
    }
}