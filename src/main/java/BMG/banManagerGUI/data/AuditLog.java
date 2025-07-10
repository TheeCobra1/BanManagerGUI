package BMG.banManagerGUI.data;

import BMG.banManagerGUI.BanManagerGUI;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class AuditLog {
    private final BanManagerGUI plugin;
    private File logFile;
    private FileConfiguration logConfig;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    public static class LogEntry {
        public final long timestamp;
        public final String moderator;
        public final String action;
        public final String target;
        public final String details;
        
        public LogEntry(long timestamp, String moderator, String action, String target, String details) {
            this.timestamp = timestamp;
            this.moderator = moderator;
            this.action = action;
            this.target = target;
            this.details = details;
        }
        
        public String getFormattedTime() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timestamp));
        }
    }
    
    public AuditLog(BanManagerGUI plugin) {
        this.plugin = plugin;
        setupLogFile();
    }
    
    private void setupLogFile() {
        logFile = new File(plugin.getDataFolder(), "audit-log.yml");
        if (!logFile.exists()) {
            logFile.getParentFile().mkdirs();
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        logConfig = YamlConfiguration.loadConfiguration(logFile);
    }
    
    public void log(String moderator, String action, String target, String details) {
        long timestamp = System.currentTimeMillis();
        String path = "logs." + timestamp;
        
        logConfig.set(path + ".moderator", moderator);
        logConfig.set(path + ".action", action);
        logConfig.set(path + ".target", target);
        logConfig.set(path + ".details", details);
        logConfig.set(path + ".timestamp", timestamp);
        
        saveLog();
    }
    
    public List<LogEntry> getRecentLogs(int limit) {
        List<LogEntry> logs = new ArrayList<>();
        if (logConfig.contains("logs")) {
            List<String> keys = new ArrayList<>(logConfig.getConfigurationSection("logs").getKeys(false));
            keys.sort((a, b) -> Long.compare(Long.parseLong(b), Long.parseLong(a)));
            
            for (int i = 0; i < Math.min(limit, keys.size()); i++) {
                String key = keys.get(i);
                String path = "logs." + key;
                logs.add(new LogEntry(
                    logConfig.getLong(path + ".timestamp"),
                    logConfig.getString(path + ".moderator"),
                    logConfig.getString(path + ".action"),
                    logConfig.getString(path + ".target"),
                    logConfig.getString(path + ".details")
                ));
            }
        }
        return logs;
    }
    
    public List<LogEntry> getRecentEntries(int offset, int limit) {
        List<LogEntry> logs = new ArrayList<>();
        if (logConfig.contains("logs")) {
            List<String> keys = new ArrayList<>(logConfig.getConfigurationSection("logs").getKeys(false));
            keys.sort((a, b) -> Long.compare(Long.parseLong(b), Long.parseLong(a)));
            
            int endIndex = Math.min(offset + limit, keys.size());
            for (int i = offset; i < endIndex; i++) {
                String key = keys.get(i);
                String path = "logs." + key;
                logs.add(new LogEntry(
                    logConfig.getLong(path + ".timestamp"),
                    logConfig.getString(path + ".moderator"),
                    logConfig.getString(path + ".action"),
                    logConfig.getString(path + ".target"),
                    logConfig.getString(path + ".details")
                ));
            }
        }
        return logs;
    }
    
    public List<LogEntry> getLogsByModerator(String moderator) {
        List<LogEntry> logs = new ArrayList<>();
        if (logConfig.contains("logs")) {
            for (String key : logConfig.getConfigurationSection("logs").getKeys(false)) {
                String path = "logs." + key;
                if (logConfig.getString(path + ".moderator").equalsIgnoreCase(moderator)) {
                    logs.add(new LogEntry(
                        logConfig.getLong(path + ".timestamp"),
                        logConfig.getString(path + ".moderator"),
                        logConfig.getString(path + ".action"),
                        logConfig.getString(path + ".target"),
                        logConfig.getString(path + ".details")
                    ));
                }
            }
        }
        logs.sort((a, b) -> Long.compare(b.timestamp, a.timestamp));
        return logs;
    }
    
    public List<LogEntry> getLogsByTarget(String target) {
        List<LogEntry> logs = new ArrayList<>();
        if (logConfig.contains("logs")) {
            for (String key : logConfig.getConfigurationSection("logs").getKeys(false)) {
                String path = "logs." + key;
                if (logConfig.getString(path + ".target").equalsIgnoreCase(target)) {
                    logs.add(new LogEntry(
                        logConfig.getLong(path + ".timestamp"),
                        logConfig.getString(path + ".moderator"),
                        logConfig.getString(path + ".action"),
                        logConfig.getString(path + ".target"),
                        logConfig.getString(path + ".details")
                    ));
                }
            }
        }
        logs.sort((a, b) -> Long.compare(b.timestamp, a.timestamp));
        return logs;
    }
    
    public void cleanOldLogs(int daysToKeep) {
        long cutoffTime = System.currentTimeMillis() - (daysToKeep * 24L * 60L * 60L * 1000L);
        if (logConfig.contains("logs")) {
            for (String key : new ArrayList<>(logConfig.getConfigurationSection("logs").getKeys(false))) {
                if (Long.parseLong(key) < cutoffTime) {
                    logConfig.set("logs." + key, null);
                }
            }
            saveLog();
        }
    }
    
    private void saveLog() {
        try {
            logConfig.save(logFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}