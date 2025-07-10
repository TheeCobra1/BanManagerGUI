package BMG.banManagerGUI.utils;

import BMG.banManagerGUI.BanManagerGUI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class FreeVPNDetector {
    private final BanManagerGUI plugin;
    private final Map<String, VPNCheckResult> cache = new ConcurrentHashMap<>();
    private final Set<String> whitelist = new HashSet<>();
    private final Set<String> knownVPNRanges = new HashSet<>();
    private final Set<String> datacenterRanges = new HashSet<>();
    private final Set<String> residentialISPs = new HashSet<>();
    private File whitelistFile;
    private FileConfiguration whitelistConfig;
    
    private static final Pattern PRIVATE_IP = Pattern.compile(
        "^(10\\.|172\\.(1[6-9]|2[0-9]|3[01])\\.|192\\.168\\.|127\\.|0\\.)"
    );
    
    public FreeVPNDetector(BanManagerGUI plugin) {
        this.plugin = plugin;
        loadWhitelist();
        loadIPDatabases();
        loadResidentialISPs();
    }
    
    private void loadWhitelist() {
        whitelistFile = new File(plugin.getDataFolder(), "vpn-whitelist.yml");
        if (!whitelistFile.exists()) {
            whitelistFile.getParentFile().mkdirs();
            try {
                whitelistFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        whitelistConfig = YamlConfiguration.loadConfiguration(whitelistFile);
        whitelist.addAll(whitelistConfig.getStringList("whitelist"));
    }
    
    private void loadIPDatabases() {
        loadVPNDatabase();
        loadDatacenterDatabase();
    }
    
    private void loadVPNDatabase() {
        File vpnFile = new File(plugin.getDataFolder(), "vpn-ips.txt");
        if (!vpnFile.exists()) {
            plugin.getLogger().info("VPN database not found. Downloading...");
            downloadVPNDatabase();
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(vpnFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty() && !line.startsWith("#")) {
                    knownVPNRanges.add(line.trim());
                }
            }
            plugin.getLogger().info("Loaded " + knownVPNRanges.size() + " VPN ranges");
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to load VPN database: " + e.getMessage());
        }
    }
    
    private void downloadVPNDatabase() {
        CompletableFuture.runAsync(() -> {
            try {
                URL url = new URL("https://raw.githubusercontent.com/X4BNet/lists_vpn/main/output/vpn/ipv4.txt");
                File vpnFile = new File(plugin.getDataFolder(), "vpn-ips.txt");
                vpnFile.getParentFile().mkdirs();
                
                try (InputStream in = url.openStream();
                     FileOutputStream out = new FileOutputStream(vpnFile)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
                
                plugin.getLogger().info("VPN database downloaded successfully");
                Bukkit.getScheduler().runTask(plugin, () -> loadVPNDatabase());
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to download VPN database: " + e.getMessage());
            }
        });
    }
    
    private void loadDatacenterDatabase() {
        File dcFile = new File(plugin.getDataFolder(), "datacenter-ips.txt");
        if (!dcFile.exists()) {
            plugin.getLogger().info("Datacenter database not found. Downloading...");
            downloadDatacenterDatabase();
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(dcFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty() && !line.startsWith("#")) {
                    datacenterRanges.add(line.trim());
                }
            }
            plugin.getLogger().info("Loaded " + datacenterRanges.size() + " datacenter ranges");
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to load datacenter database: " + e.getMessage());
        }
    }
    
    private void downloadDatacenterDatabase() {
        CompletableFuture.runAsync(() -> {
            try {
                URL url = new URL("https://raw.githubusercontent.com/X4BNet/lists_vpn/main/output/datacenter/ipv4.txt");
                File dcFile = new File(plugin.getDataFolder(), "datacenter-ips.txt");
                dcFile.getParentFile().mkdirs();
                
                try (InputStream in = url.openStream();
                     FileOutputStream out = new FileOutputStream(dcFile)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
                
                plugin.getLogger().info("Datacenter database downloaded successfully");
                Bukkit.getScheduler().runTask(plugin, () -> loadDatacenterDatabase());
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to download datacenter database: " + e.getMessage());
            }
        });
    }
    
    private void loadResidentialISPs() {
        residentialISPs.addAll(Arrays.asList(
            "comcast", "verizon", "att", "spectrum", "cox", "centurylink",
            "frontier", "windstream", "mediacom", "optimum", "suddenlink",
            "bt", "virgin", "sky", "talktalk", "plusnet", "ee",
            "telstra", "optus", "tpg", "telus", "rogers", "bell",
            "orange", "sfr", "free", "deutsche telekom", "vodafone",
            "telefonica", "movistar", "tim", "fastweb", "kpn", "ziggo"
        ));
    }
    
    public CompletableFuture<VPNCheckResult> checkIP(String ip) {
        if (whitelist.contains(ip)) {
            return CompletableFuture.completedFuture(new VPNCheckResult(false, false, 0, "Whitelisted", ""));
        }
        
        VPNCheckResult cached = cache.get(ip);
        if (cached != null && System.currentTimeMillis() - cached.timestamp < 86400000) {
            return CompletableFuture.completedFuture(cached);
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                boolean isVPN = false;
                boolean isProxy = false;
                int riskScore = 0;
                String isp = "";
                String country = "";
                
                if (PRIVATE_IP.matcher(ip).find()) {
                    return new VPNCheckResult(false, false, 0, "Private IP", "Local");
                }
                
                if (isInIPRange(ip, knownVPNRanges)) {
                    isVPN = true;
                    riskScore += 80;
                }
                
                if (isInIPRange(ip, datacenterRanges)) {
                    isProxy = true;
                    riskScore += 60;
                }
                
                String reverseDNS = getReverseDNS(ip);
                if (reverseDNS != null) {
                    String lowerDNS = reverseDNS.toLowerCase();
                    
                    if (containsVPNKeywords(lowerDNS)) {
                        isVPN = true;
                        riskScore += 70;
                    }
                    
                    if (containsDatacenterKeywords(lowerDNS)) {
                        isProxy = true;
                        riskScore += 40;
                    }
                    
                    isp = extractISPFromDNS(reverseDNS);
                    
                    if (isResidentialISP(isp)) {
                        riskScore = Math.max(0, riskScore - 50);
                    }
                }
                
                if (checkOpenPorts(ip)) {
                    isProxy = true;
                    riskScore += 30;
                }
                
                riskScore = Math.min(100, riskScore);
                
                VPNCheckResult result = new VPNCheckResult(isVPN, isProxy, riskScore, isp, country);
                cache.put(ip, result);
                
                return result;
                
            } catch (Exception e) {
                plugin.getLogger().warning("Error checking IP " + ip + ": " + e.getMessage());
                return new VPNCheckResult(false, false, 0, "Error", "");
            }
        });
    }
    
    private boolean isInIPRange(String ip, Set<String> ranges) {
        try {
            InetAddress addr = InetAddress.getByName(ip);
            byte[] ipBytes = addr.getAddress();
            
            for (String range : ranges) {
                if (range.contains("/")) {
                    String[] parts = range.split("/");
                    InetAddress rangeAddr = InetAddress.getByName(parts[0]);
                    int prefixLength = Integer.parseInt(parts[1]);
                    
                    if (isInCIDR(ipBytes, rangeAddr.getAddress(), prefixLength)) {
                        return true;
                    }
                } else if (range.equals(ip)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    private boolean isInCIDR(byte[] ip, byte[] network, int prefixLength) {
        int bytesToCheck = prefixLength / 8;
        int bitsToCheck = prefixLength % 8;
        
        for (int i = 0; i < bytesToCheck; i++) {
            if (ip[i] != network[i]) {
                return false;
            }
        }
        
        if (bitsToCheck > 0 && bytesToCheck < ip.length) {
            int mask = 0xFF << (8 - bitsToCheck);
            return (ip[bytesToCheck] & mask) == (network[bytesToCheck] & mask);
        }
        
        return true;
    }
    
    private String getReverseDNS(String ip) {
        try {
            InetAddress addr = InetAddress.getByName(ip);
            return addr.getCanonicalHostName();
        } catch (Exception e) {
            return null;
        }
    }
    
    private boolean containsVPNKeywords(String dns) {
        String[] vpnKeywords = {
            "vpn", "proxy", "tor", "relay", "anonymizer", "hide",
            "cyberghost", "nordvpn", "expressvpn", "surfshark",
            "privateinternetaccess", "pia", "tunnelbear", "windscribe",
            "mullvad", "ivpn", "protonvpn", "ipvanish"
        };
        
        for (String keyword : vpnKeywords) {
            if (dns.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean containsDatacenterKeywords(String dns) {
        String[] dcKeywords = {
            "amazon", "aws", "ec2", "digitalocean", "linode",
            "vultr", "ovh", "hetzner", "google", "gcp", "azure",
            "alibaba", "oracle", "ibm", "rackspace", "scaleway",
            "contabo", "hosting", "server", "dedicated", "vps"
        };
        
        for (String keyword : dcKeywords) {
            if (dns.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    private String extractISPFromDNS(String dns) {
        String[] parts = dns.split("\\.");
        if (parts.length >= 2) {
            return parts[parts.length - 2];
        }
        return dns;
    }
    
    private boolean isResidentialISP(String isp) {
        String lowerISP = isp.toLowerCase();
        for (String residential : residentialISPs) {
            if (lowerISP.contains(residential)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean checkOpenPorts(String ip) {
        int[] suspiciousPorts = {1080, 8080, 3128, 9050, 9051};
        
        for (int port : suspiciousPorts) {
            try (Socket socket = new Socket()) {
                socket.setSoTimeout(200);
                socket.connect(new InetSocketAddress(ip, port), 200);
                return true;
            } catch (Exception e) {
            }
        }
        return false;
    }
    
    public void addToWhitelist(String ip) {
        whitelist.add(ip);
        List<String> list = new ArrayList<>(whitelist);
        whitelistConfig.set("whitelist", list);
        saveWhitelist();
    }
    
    public void removeFromWhitelist(String ip) {
        whitelist.remove(ip);
        List<String> list = new ArrayList<>(whitelist);
        whitelistConfig.set("whitelist", list);
        saveWhitelist();
    }
    
    public List<String> getWhitelist() {
        return new ArrayList<>(whitelist);
    }
    
    private void saveWhitelist() {
        try {
            whitelistConfig.save(whitelistFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void updateDatabases() {
        CompletableFuture.runAsync(() -> {
            downloadVPNDatabase();
            downloadDatacenterDatabase();
        });
    }
    
    public static class VPNCheckResult {
        public final boolean isVPN;
        public final boolean isProxy;
        public final int risk;
        public final String isp;
        public final String country;
        public final long timestamp;
        
        public VPNCheckResult(boolean isVPN, boolean isProxy, int risk, String isp, String country) {
            this.isVPN = isVPN;
            this.isProxy = isProxy;
            this.risk = risk;
            this.isp = isp;
            this.country = country;
            this.timestamp = System.currentTimeMillis();
        }
    }
}