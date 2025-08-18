package com.tgsoul.commands;

import com.tgsoul.TGSoulPlugin;
import com.tgsoul.data.PlayerSoulData;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class SoulCommand implements CommandExecutor, TabCompleter {
    
    private final TGSoulPlugin plugin;
    
    public SoulCommand(TGSoulPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                PlayerSoulData data = plugin.getSoulManager().getOrCreatePlayerData(player);
                plugin.getMessageUtil().sendMessage(sender, "souls-remaining", 
                        Map.of("souls", String.valueOf(data.getSouls())));
            } else {
                sender.sendMessage("Usage: /soul <subcommand>");
            }
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "give":
                return handleGive(sender, args);
            case "take":
                return handleTake(sender, args);
            case "set":
                return handleSet(sender, args);
            case "get":
                return handleGet(sender, args);
            case "reload":
                return handleReload(sender);
            case "top":
                return handleTop(sender, args);
            case "stats":
                return handleStats(sender, args);
            default:
                sender.sendMessage("Unknown subcommand. Use: give, take, set, get, reload, top, stats");
                return true;
        }
    }
    
    private boolean handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("tgsoul.admin")) {
            plugin.getMessageUtil().sendMessage(sender, "no-permission");
            return true;
        }
        
        if (args.length < 3) {
            sender.sendMessage("Usage: /soul give <player> <amount>");
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            plugin.getMessageUtil().sendMessage(sender, "player-not-found");
            return true;
        }
        
        try {
            int amount = Integer.parseInt(args[2]);
            if (amount <= 0) {
                plugin.getMessageUtil().sendMessage(sender, "invalid-amount");
                return true;
            }
            
            plugin.getSoulManager().addSouls(target, amount);
            plugin.getMessageUtil().sendMessage(sender, "admin-gave-souls", 
                    Map.of("amount", String.valueOf(amount), "player", target.getName()));
            
        } catch (NumberFormatException e) {
            plugin.getMessageUtil().sendMessage(sender, "invalid-amount");
        }
        
        return true;
    }
    
    private boolean handleTake(CommandSender sender, String[] args) {
        if (!sender.hasPermission("tgsoul.admin")) {
            plugin.getMessageUtil().sendMessage(sender, "no-permission");
            return true;
        }
        
        if (args.length < 3) {
            sender.sendMessage("Usage: /soul take <player> <amount>");
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            plugin.getMessageUtil().sendMessage(sender, "player-not-found");
            return true;
        }
        
        try {
            int amount = Integer.parseInt(args[2]);
            if (amount <= 0) {
                plugin.getMessageUtil().sendMessage(sender, "invalid-amount");
                return true;
            }
            
            PlayerSoulData data = plugin.getSoulManager().getOrCreatePlayerData(target);
            int actualTaken = Math.min(amount, data.getSouls());
            
            plugin.getSoulManager().removeSouls(target, amount);
            
            // Give soul items to admin
            if (sender instanceof Player) {
                Player admin = (Player) sender;
                for (int i = 0; i < actualTaken; i++) {
                    ItemStack soulItem = plugin.getSoulManager().createSoulItem(target.getName());
                    if (admin.getInventory().firstEmpty() != -1) {
                        admin.getInventory().addItem(soulItem);
                    } else {
                        admin.getWorld().dropItemNaturally(admin.getLocation(), soulItem);
                    }
                }
            }
            
            plugin.getMessageUtil().sendMessage(sender, "admin-took-souls", 
                    Map.of("amount", String.valueOf(actualTaken), "player", target.getName()));
            
        } catch (NumberFormatException e) {
            plugin.getMessageUtil().sendMessage(sender, "invalid-amount");
        }
        
        return true;
    }
    
    private boolean handleSet(CommandSender sender, String[] args) {
        if (!sender.hasPermission("tgsoul.admin")) {
            plugin.getMessageUtil().sendMessage(sender, "no-permission");
            return true;
        }
        
        if (args.length < 3) {
            sender.sendMessage("Usage: /soul set <player> <amount>");
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            plugin.getMessageUtil().sendMessage(sender, "player-not-found");
            return true;
        }
        
        try {
            int amount = Integer.parseInt(args[2]);
            if (amount < 0) {
                plugin.getMessageUtil().sendMessage(sender, "invalid-amount");
                return true;
            }
            
            plugin.getSoulManager().setSouls(target, amount);
            plugin.getMessageUtil().sendMessage(sender, "admin-set-souls", 
                    Map.of("amount", String.valueOf(amount), "player", target.getName()));
            
        } catch (NumberFormatException e) {
            plugin.getMessageUtil().sendMessage(sender, "invalid-amount");
        }
        
        return true;
    }
    
    private boolean handleGet(CommandSender sender, String[] args) {
        if (!sender.hasPermission("tgsoul.admin")) {
            plugin.getMessageUtil().sendMessage(sender, "no-permission");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage("Usage: /soul get <player>");
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            plugin.getMessageUtil().sendMessage(sender, "player-not-found");
            return true;
        }
        
        PlayerSoulData data = plugin.getSoulManager().getOrCreatePlayerData(target);
        sender.sendMessage("§6" + target.getName() + " §7has §6" + data.getSouls() + " §7souls.");
        
        return true;
    }
    
    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("tgsoul.admin")) {
            plugin.getMessageUtil().sendMessage(sender, "no-permission");
            return true;
        }
        
        plugin.getConfigManager().reloadConfig();
        plugin.getMessageUtil().reloadMessages();
        plugin.getMessageUtil().sendMessage(sender, "config-reloaded");
        
        return true;
    }
    
    private boolean handleTop(CommandSender sender, String[] args) {
        int limit = 10;
        if (args.length > 1) {
            try {
                limit = Integer.parseInt(args[1]);
                limit = Math.max(1, Math.min(limit, 50)); // Clamp between 1 and 50
            } catch (NumberFormatException e) {
                limit = 10;
            }
        }
        
        List<PlayerSoulData> topPlayers = plugin.getSoulManager().getTopPlayers(limit);
        
        sender.sendMessage("§6§l=== Top " + limit + " Soul Leaders ===");
        for (int i = 0; i < topPlayers.size(); i++) {
            PlayerSoulData data = topPlayers.get(i);
            sender.sendMessage("§7" + (i + 1) + ". §6" + data.getPlayerName() + " §7- §6" + data.getSouls() + " souls");
        }
        
        return true;
    }
    
    private boolean handleStats(CommandSender sender, String[] args) {
        String targetName;
        
        if (args.length < 2) {
            if (sender instanceof Player) {
                targetName = sender.getName();
            } else {
                sender.sendMessage("Usage: /soul stats <player>");
                return true;
            }
        } else {
            targetName = args[1];
        }
        
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            plugin.getMessageUtil().sendMessage(sender, "player-not-found");
            return true;
        }
        
        PlayerSoulData data = plugin.getSoulManager().getOrCreatePlayerData(target);
        
        sender.sendMessage("§6§l=== Soul Stats for " + data.getPlayerName() + " ===");
        sender.sendMessage("§7Souls: §6" + data.getSouls() + "/" + plugin.getSoulManager().getMaxSouls());
        sender.sendMessage("§7Needs Revival: §6" + (data.needsRevival() ? "Yes" : "No"));
        sender.sendMessage("§7Last Seen: §6" + data.getLastSeen());
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("give", "take", "set", "get", "reload", "top", "stats");
            for (String subCommand : subCommands) {
                if (subCommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("give") || 
                                       args[0].equalsIgnoreCase("take") || 
                                       args[0].equalsIgnoreCase("set") || 
                                       args[0].equalsIgnoreCase("get") ||
                                       args[0].equalsIgnoreCase("stats"))) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                    completions.add(player.getName());
                }
            }
        }
        
        return completions;
    }
}