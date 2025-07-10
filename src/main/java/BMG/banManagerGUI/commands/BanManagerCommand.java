package BMG.banManagerGUI.commands;

import BMG.banManagerGUI.BanManagerGUI;
import BMG.banManagerGUI.gui.GUIManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BanManagerCommand implements CommandExecutor {
    private final BanManagerGUI plugin;
    private final GUIManager guiManager;
    
    public BanManagerCommand(BanManagerGUI plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("banmanager.use")) {
            player.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }
        
        guiManager.openHomeGUI(player);
        return true;
    }
}