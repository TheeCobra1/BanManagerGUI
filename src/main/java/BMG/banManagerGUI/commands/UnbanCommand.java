package BMG.banManagerGUI.commands;

import BMG.banManagerGUI.BanManagerGUI;
import BMG.banManagerGUI.data.PunishmentData;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class UnbanCommand implements CommandExecutor {
    private final BanManagerGUI plugin;
    private final PunishmentData punishmentData;
    
    public UnbanCommand(BanManagerGUI plugin, PunishmentData punishmentData) {
        this.plugin = plugin;
        this.punishmentData = punishmentData;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("banmanager.unban")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }
        
        if (args.length < 1) {
            sender.sendMessage("§cUsage: /unban <player>");
            return true;
        }
        
        String targetName = args[0];
        
        Bukkit.getBanList(org.bukkit.BanList.Type.NAME).pardon(targetName);
        punishmentData.removeBan(targetName);
        punishmentData.addPunishment(targetName, "UNBAN", "Unbanned", sender.getName(), 0);
        
        sender.sendMessage("§aSuccessfully unbanned " + targetName);
        return true;
    }
}