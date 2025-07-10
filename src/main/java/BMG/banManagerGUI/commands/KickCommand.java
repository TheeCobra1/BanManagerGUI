package BMG.banManagerGUI.commands;

import BMG.banManagerGUI.BanManagerGUI;
import BMG.banManagerGUI.data.PunishmentData;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KickCommand implements CommandExecutor {
    private final BanManagerGUI plugin;
    private final PunishmentData punishmentData;
    
    public KickCommand(BanManagerGUI plugin, PunishmentData punishmentData) {
        this.plugin = plugin;
        this.punishmentData = punishmentData;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("banmanager.kick")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }
        
        if (args.length < 1) {
            sender.sendMessage("§cUsage: /kick <player> [reason]");
            return true;
        }
        
        String targetName = args[0];
        String reason = args.length > 1 ? String.join(" ", args).substring(targetName.length() + 1) : "No reason specified";
        
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }
        
        target.kickPlayer("§cYou have been kicked!\n§7Reason: " + reason + "\n§7Kicked by: " + sender.getName());
        punishmentData.addPunishment(targetName, "KICK", reason, sender.getName(), 0);
        
        sender.sendMessage("§aSuccessfully kicked " + targetName + " for: " + reason);
        return true;
    }
}