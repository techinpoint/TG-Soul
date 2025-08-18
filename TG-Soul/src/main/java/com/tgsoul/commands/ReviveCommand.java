package com.tgsoul.commands;

import com.tgsoul.TGSoulPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReviveCommand implements CommandExecutor, TabCompleter {
    
    private final TGSoulPlugin plugin;
    
    public ReviveCommand(TGSoulPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }
        
        if (!sender.hasPermission("tgsoul.revive")) {
            plugin.getMessageUtil().sendMessage(sender, "no-permission");
            return true;
        }
        
        if (args.length < 1) {
            sender.sendMessage("Usage: /revive <player>");
            return true;
        }
        
        Player reviver = (Player) sender;
        String targetName = args[0];
        
        if (!plugin.getSoulManager().canRevivePlayer(targetName)) {
            plugin.getMessageUtil().sendMessage(sender, "revive-not-needed", 
                    Map.of("player", targetName));
            return true;
        }
        
        if (plugin.getSoulManager().revivePlayer(reviver, targetName)) {
            plugin.getMessageUtil().sendMessage(sender, "revive-success", 
                    Map.of("player", targetName, "souls", String.valueOf(plugin.getSoulManager().getMaxSouls())));
        } else {
            plugin.getMessageUtil().sendMessage(sender, "revive-failed", 
                    Map.of("player", targetName));
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(player.getName());
                }
            }
        }
        
        return completions;
    }
}