package BMG.banManagerGUI.data;

import BMG.banManagerGUI.BanManagerGUI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class IPData {
    private final BanManagerGUI plugin;
    private File dataFile;
    private FileConfiguration dataConfig;
    
    public IPData(BanManagerGUI plugin) {
        this.plugin = plugin;
        setupDataFile();
    }
    
    private void setupDataFile() {
        dataFile = new File(plugin.getDataFolder(), "ip-data.yml");
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
    
    public void recordPlayerIP(String playerName, String ip) {
        String playerPath = "players." + playerName;
        List<String> ips = dataConfig.getStringList(playerPath + ".ips");
        if (!ips.contains(ip)) {
            ips.add(ip);
            dataConfig.set(playerPath + ".ips", ips);
            dataConfig.set(playerPath + ".last-ip", ip);
            dataConfig.set(playerPath + ".last-seen", System.currentTimeMillis());
        }
        
        String ipPath = "ips." + ip.replace(".", "_");
        List<String> players = dataConfig.getStringList(ipPath + ".players");
        if (!players.contains(playerName)) {
            players.add(playerName);
            dataConfig.set(ipPath + ".players", players);
        }
        dataConfig.set(ipPath + ".last-seen", System.currentTimeMillis());
        
        saveData();
    }
    
    public List<String> getPlayerIPs(String playerName) {
        return dataConfig.getStringList("players." + playerName + ".ips");
    }
    
    public List<String> getPlayersWithIP(String ip) {
        return dataConfig.getStringList("ips." + ip.replace(".", "_") + ".players");
    }
    
    public void banIP(String ip, String reason, String issuer) {
        Bukkit.getBanList(org.bukkit.BanList.Type.IP).addBan(ip, reason, null, issuer);
        String ipPath = "ips." + ip.replace(".", "_");
        dataConfig.set(ipPath + ".banned", true);
        dataConfig.set(ipPath + ".ban-reason", reason);
        dataConfig.set(ipPath + ".ban-issuer", issuer);
        dataConfig.set(ipPath + ".ban-time", System.currentTimeMillis());
        saveData();
    }
    
    public void unbanIP(String ip) {
        Bukkit.getBanList(org.bukkit.BanList.Type.IP).pardon(ip);
        String ipPath = "ips." + ip.replace(".", "_");
        dataConfig.set(ipPath + ".banned", false);
        dataConfig.set(ipPath + ".ban-reason", null);
        dataConfig.set(ipPath + ".ban-issuer", null);
        dataConfig.set(ipPath + ".ban-time", null);
        saveData();
    }
    
    public List<String> getBannedIPs() {
        return Bukkit.getBanList(org.bukkit.BanList.Type.IP).getBanEntries().stream()
                .map(entry -> entry.getTarget())
                .collect(Collectors.toList());
    }
    
    public Map<String, List<String>> getAllIPMappings() {
        Map<String, List<String>> mappings = new HashMap<>();
        if (dataConfig.contains("ips")) {
            for (String ipKey : dataConfig.getConfigurationSection("ips").getKeys(false)) {
                String ip = ipKey.replace("_", ".");
                List<String> players = dataConfig.getStringList("ips." + ipKey + ".players");
                if (!players.isEmpty()) {
                    mappings.put(ip, players);
                }
            }
        }
        return mappings;
    }
    
    public String getPlayerLastIP(String playerName) {
        return dataConfig.getString("players." + playerName + ".last-ip", "Unknown");
    }
    
    private void saveData() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}