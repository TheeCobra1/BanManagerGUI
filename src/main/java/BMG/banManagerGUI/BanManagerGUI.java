package BMG.banManagerGUI;

import BMG.banManagerGUI.commands.*;
import BMG.banManagerGUI.data.AuditLog;
import BMG.banManagerGUI.data.IPData;
import BMG.banManagerGUI.data.PunishmentData;
import BMG.banManagerGUI.gui.GUIManager;
import BMG.banManagerGUI.listeners.ChatListener;
import BMG.banManagerGUI.listeners.GUIListener;
import BMG.banManagerGUI.listeners.JoinListener;
import BMG.banManagerGUI.utils.AutoModerator;
import BMG.banManagerGUI.utils.BulkActions;
import BMG.banManagerGUI.utils.ImportExport;
import BMG.banManagerGUI.utils.PluginHooks;
import BMG.banManagerGUI.utils.FreeVPNDetector;
import org.bukkit.plugin.java.JavaPlugin;

public final class BanManagerGUI extends JavaPlugin {
    private GUIManager guiManager;
    private PunishmentData punishmentData;
    private PluginHooks pluginHooks;
    private FreeVPNDetector vpnDetector;
    private IPData ipData;
    private AuditLog auditLog;
    private AutoModerator autoModerator;
    private BulkActions bulkActions;
    private ImportExport importExport;
    
    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        punishmentData = new PunishmentData(this);
        ipData = new IPData(this);
        auditLog = new AuditLog(this);
        vpnDetector = new FreeVPNDetector(this);
        autoModerator = new AutoModerator(this, punishmentData);
        bulkActions = new BulkActions(this);
        importExport = new ImportExport(this);
        guiManager = new GUIManager(this);
        pluginHooks = new PluginHooks();
        
        registerCommands();
        registerListeners();
        
        getLogger().info("BanManagerGUI has been enabled!");
        getLogger().info(pluginHooks.getIntegrationStatus());
    }
    
    @Override
    public void onDisable() {
        getLogger().info("BanManagerGUI has been disabled!");
    }
    
    private void registerCommands() {
        getCommand("banmanager").setExecutor(new BanManagerCommand(this, guiManager));
        getCommand("ban").setExecutor(new BanCommand(this, punishmentData));
        getCommand("unban").setExecutor(new UnbanCommand(this, punishmentData));
        getCommand("mute").setExecutor(new MuteCommand(this, punishmentData));
        getCommand("unmute").setExecutor(new UnmuteCommand(this, punishmentData));
        getCommand("kick").setExecutor(new KickCommand(this, punishmentData));
    }
    
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new GUIListener(this, guiManager, punishmentData), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this, punishmentData), this);
        getServer().getPluginManager().registerEvents(new JoinListener(this, punishmentData), this);
    }
    
    public PunishmentData getPunishmentData() {
        return punishmentData;
    }
    
    public PluginHooks getPluginHooks() {
        return pluginHooks;
    }
    
    public FreeVPNDetector getVPNDetector() {
        return vpnDetector;
    }
    
    public IPData getIPData() {
        return ipData;
    }
    
    public AuditLog getAuditLog() {
        return auditLog;
    }
    
    public AutoModerator getAutoModerator() {
        return autoModerator;
    }
    
    public BulkActions getBulkActions() {
        return bulkActions;
    }
    
    public ImportExport getImportExport() {
        return importExport;
    }
}
