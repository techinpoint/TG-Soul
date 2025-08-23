package com.tgsoul.commands;

import com.tgsoul.TGSoulPlugin;
import com.tgsoul.data.PlayerSoulData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

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
        
        try {
            // Check if player would have 0 souls after withdrawal
            PlayerSoulData data = plugin.getSoulManager().getOrCreatePlayerData(player);
            if (data.getSouls() <= 1) {
                plugin.getMessageUtil().sendMessage(player, "cannot-withdraw-last-soul");
                return true;
            }
            
            plugin.getSoulManager().withdrawSoul(player);
            
        } catch (Exception e) {
            plugin.getLogger().severe("Error in soulwithdraw command: " + e.getMessage());
            e.printStackTrace();
            player.sendMessage("§cAn error occurred while withdrawing soul. Please try again.");
        }
        
        return true;
    }
}