package BMG.banManagerGUI.data;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.BanEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class BanCache {
    private List<BannedPlayerInfo> cachedBans = new ArrayList<>();
    private long lastUpdate = 0;
    private static final long CACHE_DURATION = 5000;
    
    public static class BannedPlayerInfo {
        public final String name;
        public final String reason;
        public final String source;
        public final long created;
        public final long expires;
        
        public BannedPlayerInfo(BanEntry entry) {
            this.name = entry.getTarget();
            this.reason = entry.getReason();
            this.source = entry.getSource();
            this.created = entry.getCreated() != null ? entry.getCreated().getTime() : 0;
            this.expires = entry.getExpiration() != null ? entry.getExpiration().getTime() : 0;
        }
    }
    
    public CompletableFuture<List<BannedPlayerInfo>> getBannedPlayersAsync() {
        if (System.currentTimeMillis() - lastUpdate < CACHE_DURATION && !cachedBans.isEmpty()) {
            return CompletableFuture.completedFuture(new ArrayList<>(cachedBans));
        }
        
        return CompletableFuture.supplyAsync(() -> {
            Set<BanEntry> banEntries = Bukkit.getBanList(org.bukkit.BanList.Type.NAME).getBanEntries();
            List<BannedPlayerInfo> bans = banEntries.stream()
                    .map(BannedPlayerInfo::new)
                    .collect(Collectors.toList());
            
            cachedBans = bans;
            lastUpdate = System.currentTimeMillis();
            return bans;
        });
    }
    
    public void invalidateCache() {
        lastUpdate = 0;
        cachedBans.clear();
    }
}