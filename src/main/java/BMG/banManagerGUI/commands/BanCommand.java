package BMG.banManagerGUI.commands;

import BMG.banManagerGUI.BanManagerGUI;
import BMG.banManagerGUI.data.PunishmentData;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BanCommand implements CommandExecutor {
    private final BanManagerGUI plugin;
    private final PunishmentData punishmentData;
    
    public BanCommand(BanManagerGUI plugin, PunishmentData punishmentData) {
        this.plugin = plugin;
        this.punishmentData = punishmentData;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("banmanager.ban")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }
        
        if (args.length < 1) {
            sender.sendMessage("§cUsage: /ban <player> [reason]");
            return true;
        }
        
        String targetName = args[0];
        String reason = args.length > 1 ? String.join(" ", args).substring(targetName.length() + 1) : "No reason specified";
        
        Player target = Bukkit.getPlayer(targetName);
        if (target != null) {
            target.kickPlayer("§cYou have been banned!\n§7Reason: " + reason + "\n§7Banned by: " + sender.getName());
        }
        
        Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(targetName, reason, null, sender.getName());
        punishmentData.addPunishment(targetName, "BAN", reason, sender.getName(), 0);
        
        sender.sendMessage("§aSuccessfully banned " + targetName + " for: " + reason);
        return true;
    }
}