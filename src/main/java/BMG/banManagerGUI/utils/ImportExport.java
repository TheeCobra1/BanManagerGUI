package BMG.banManagerGUI.utils;

import BMG.banManagerGUI.BanManagerGUI;
import BMG.banManagerGUI.data.PunishmentData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ImportExport {
    private final BanManagerGUI plugin;
    private final PunishmentData punishmentData;
    private final Gson gson;
    
    public ImportExport(BanManagerGUI plugin) {
        this.plugin = plugin;
        this.punishmentData = plugin.getPunishmentData();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }
    
    public CompletableFuture<ExportResult> exportBans() {
        return CompletableFuture.supplyAsync(() -> {
            ExportResult result = new ExportResult();
            ExportData data = new ExportData();
            
            try {
                BanList banList = Bukkit.getBanList(BanList.Type.NAME);
                Set<BanEntry> banEntries = banList.getBanEntries();
                for (BanEntry entry : banEntries) {
                    BanData ban = new BanData();
                    ban.player = entry.getTarget();
                    ban.reason = entry.getReason();
                    ban.source = entry.getSource();
                    ban.created = entry.getCreated();
                    ban.expiration = entry.getExpiration();
                    data.bans.add(ban);
                }
                
                Map<String, PunishmentData.Punishment> mutes = punishmentData.getAllMutes();
                for (Map.Entry<String, PunishmentData.Punishment> entry : mutes.entrySet()) {
                    MuteData mute = new MuteData();
                    mute.player = entry.getKey();
                    mute.reason = entry.getValue().reason;
                    mute.issuer = entry.getValue().issuer;
                    mute.timestamp = entry.getValue().timestamp;
                    mute.duration = entry.getValue().duration;
                    data.mutes.add(mute);
                }
                
                File exportFile = new File(plugin.getDataFolder(), "exports/export_" + System.currentTimeMillis() + ".json");
                exportFile.getParentFile().mkdirs();
                
                try (FileWriter writer = new FileWriter(exportFile)) {
                    gson.toJson(data, writer);
                }
                
                result.success = true;
                result.exportedBans = data.bans.size();
                result.exportedMutes = data.mutes.size();
                result.filePath = exportFile.getAbsolutePath();
                
            } catch (Exception e) {
                result.success = false;
                result.error = e.getMessage();
            }
            
            return result;
        });
    }
    
    public CompletableFuture<ImportResult> importBans(String filePath) {
        return CompletableFuture.supplyAsync(() -> {
            ImportResult result = new ImportResult();
            
            try {
                File importFile = new File(filePath);
                if (!importFile.exists()) {
                    importFile = new File(plugin.getDataFolder(), filePath);
                    if (!importFile.exists()) {
                        result.success = false;
                        result.error = "File not found: " + filePath;
                        return result;
                    }
                }
                
                ExportData data;
                try (FileReader reader = new FileReader(importFile)) {
                    data = gson.fromJson(reader, ExportData.class);
                }
                
                BanList banList = Bukkit.getBanList(BanList.Type.NAME);
                
                for (BanData ban : data.bans) {
                    try {
                        if (!banList.isBanned(ban.player)) {
                            banList.addBan(ban.player, ban.reason, ban.expiration, ban.source);
                            result.importedBans++;
                        } else {
                            result.skippedBans++;
                        }
                    } catch (Exception e) {
                        result.failedBans++;
                    }
                }
                
                for (MuteData mute : data.mutes) {
                    try {
                        if (!punishmentData.isMuted(mute.player)) {
                            punishmentData.addPunishment(mute.player, 
                                mute.duration > 0 ? "TEMPMUTE" : "MUTE", 
                                mute.reason, mute.issuer, mute.duration);
                            result.importedMutes++;
                        } else {
                            result.skippedMutes++;
                        }
                    } catch (Exception e) {
                        result.failedMutes++;
                    }
                }
                
                result.success = true;
                
            } catch (Exception e) {
                result.success = false;
                result.error = e.getMessage();
            }
            
            return result;
        });
    }
    
    public List<File> getExportFiles() {
        File exportDir = new File(plugin.getDataFolder(), "exports");
        if (!exportDir.exists()) {
            return new ArrayList<>();
        }
        
        File[] files = exportDir.listFiles((dir, name) -> name.endsWith(".json"));
        return files != null ? Arrays.asList(files) : new ArrayList<>();
    }
    
    private static class ExportData {
        List<BanData> bans = new ArrayList<>();
        List<MuteData> mutes = new ArrayList<>();
        long exportTime = System.currentTimeMillis();
        String version = "1.0";
    }
    
    private static class BanData {
        String player;
        String reason;
        String source;
        Date created;
        Date expiration;
    }
    
    private static class MuteData {
        String player;
        String reason;
        String issuer;
        long timestamp;
        long duration;
    }
    
    public static class ExportResult {
        public boolean success;
        public int exportedBans;
        public int exportedMutes;
        public String filePath;
        public String error;
    }
    
    public static class ImportResult {
        public boolean success;
        public int importedBans;
        public int importedMutes;
        public int skippedBans;
        public int skippedMutes;
        public int failedBans;
        public int failedMutes;
        public String error;
    }
}