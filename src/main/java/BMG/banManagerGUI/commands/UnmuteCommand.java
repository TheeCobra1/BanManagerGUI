package BMG.banManagerGUI.commands;

import BMG.banManagerGUI.BanManagerGUI;
import BMG.banManagerGUI.data.PunishmentData;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnmuteCommand implements CommandExecutor {
    private final BanManagerGUI plugin;
    private final PunishmentData punishmentData;
    
    public UnmuteCommand(BanManagerGUI plugin, PunishmentData punishmentData) {
        this.plugin = plugin;
        this.punishmentData = punishmentData;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("banmanager.unmute")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }
        
        if (args.length < 1) {
            sender.sendMessage("§cUsage: /unmute <player>");
            return true;
        }
        
        String targetName = args[0];
        
        punishmentData.removeMute(targetName);
        punishmentData.addPunishment(targetName, "UNMUTE", "Unmuted", sender.getName(), 0);
        
        Player target = Bukkit.getPlayer(targetName);
        if (target != null) {
            target.sendMessage("§aYou have been unmuted!");
        }
        
        sender.sendMessage("§aSuccessfully unmuted " + targetName);
        return true;
    }
}