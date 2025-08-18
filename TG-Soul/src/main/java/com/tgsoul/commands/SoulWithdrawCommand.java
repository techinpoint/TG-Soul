package com.tgsoul.commands;

import com.tgsoul.TGSoulPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SoulWithdrawCommand implements CommandExecutor {
    
    private final TGSoulPlugin plugin;
    
    public SoulWithdrawCommand(TGSoulPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }
        
        if (!sender.hasPermission("tgsoul.withdraw")) {
            plugin.getMessageUtil().sendMessage(sender, "no-permission");
            return true;
        }
        
        Player player = (Player) sender;
        plugin.getSoulManager().withdrawSoul(player);
        
        return true;
    }
}