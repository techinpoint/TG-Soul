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
            if (sender instanceof Player player) {
                PlayerSoulData data = plugin.getSoulManager().getOrCreatePlayerData(player);
                plugin.getMessageUtil().sendMessage(sender, "souls-remaining",
                        Map.of("souls", String.valueOf(data.getSouls())));
            } else {
                sender.sendMessage("Usage: /soul <subcommand>");
            }
            return true;
        }

        String subCommand = args[0].toLowerCase();

        return switch (subCommand) {
            case "help" -> handleHelp(sender);
            case "recipe" -> handleRecipe(sender);
            case "give" -> handleGive(sender, args);
            case "take" -> handleTake(sender, args);
            case "set" -> handleSet(sender, args);
            case "get" -> handleGet(sender, args);
            case "unban" -> handleUnban(sender, args);
            case "reload" -> handleReload(sender);
            case "top" -> handleTop(sender, args);
            case "stats" -> handleStats(sender, args);
            case "pack" -> handlePack(sender, args);
            default -> {
                handleHelp(sender);
                yield false;
            }
        };
    }

    private boolean handleHelp(CommandSender sender) {
        plugin.getMessageUtil().sendMessageWithoutPrefix(sender, "help-header");
        plugin.getMessageUtil().sendMessageWithoutPrefix(sender, "help-soul");
        plugin.getMessageUtil().sendMessageWithoutPrefix(sender, "help-soul-help");
        plugin.getMessageUtil().sendMessageWithoutPrefix(sender, "help-soul-recipe");
        plugin.getMessageUtil().sendMessageWithoutPrefix(sender, "help-soul-top");
        plugin.getMessageUtil().sendMessageWithoutPrefix(sender, "help-soul-stats");
        plugin.getMessageUtil().sendMessageWithoutPrefix(sender, "help-soulwithdraw");
        if (sender.hasPermission("tgsoul.admin")) {
            plugin.getMessageUtil().sendMessageWithoutPrefix(sender, "help-admin-header");
            plugin.getMessageUtil().sendMessageWithoutPrefix(sender, "help-admin-give");
            plugin.getMessageUtil().sendMessageWithoutPrefix(sender, "help-admin-take");
            plugin.getMessageUtil().sendMessageWithoutPrefix(sender, "help-admin-set");
            plugin.getMessageUtil().sendMessageWithoutPrefix(sender, "help-admin-get");
            plugin.getMessageUtil().sendMessageWithoutPrefix(sender, "help-admin-reload");
            plugin.getMessageUtil().sendMessageWithoutPrefix(sender, "help-admin-unban");
            plugin.getMessageUtil().sendMessageWithoutPrefix(sender, "help-admin-pack");
        }
        return true;
    }

    private boolean handleRecipe(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        plugin.getSoulManager().openRecipeGUI(player);
        return true;
    }

    private boolean handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("tgsoul.admin")) {
            plugin.getMessageUtil().sendMessage(sender, "no-permission");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("Usage: /soul give <player> <amount>");
            return false;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            plugin.getMessageUtil().sendMessage(sender, "player-not-found");
            return true;
        }

        try {
            int amount = Integer.parseInt(args[2]);
            if (amount < 1) {
                sender.sendMessage("Amount must be positive.");
                return true;
            }

            PlayerSoulData data = plugin.getSoulManager().getOrCreatePlayerData(target);
            if (data.getSouls() + amount > plugin.getSoulManager().getMaxSouls()) {
                sender.sendMessage("§cPlayer already has maximum no of Souls");
                return true;
            }

            plugin.getSoulManager().addSouls(target, amount);
            sender.sendMessage("§aGave §6" + amount + " §asouls to §6" + target.getName() + "§a.");
        } catch (NumberFormatException e) {
            sender.sendMessage("Invalid amount.");
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
            return false;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            plugin.getMessageUtil().sendMessage(sender, "player-not-found");
            return true;
        }

        try {
            int amount = Integer.parseInt(args[2]);
            if (amount < 1) {
                sender.sendMessage("Amount must be positive.");
                return true;
            }

            plugin.getSoulManager().removeSouls(target, amount);
            sender.sendMessage("§aTook §6" + amount + " §asouls from §6" + target.getName() + "§a.");
        } catch (NumberFormatException e) {
            sender.sendMessage("Invalid amount.");
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
            return false;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            plugin.getMessageUtil().sendMessage(sender, "player-not-found");
            return true;
        }

        try {
            int amount = Integer.parseInt(args[2]);
            plugin.getSoulManager().setSouls(target, amount);
            sender.sendMessage("§aSet §6" + target.getName() + "§a's souls to §6" + amount + "§a.");
        } catch (NumberFormatException e) {
            sender.sendMessage("Invalid amount.");
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
            return false;
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

    private boolean handleUnban(CommandSender sender, String[] args) {
        if (!sender.hasPermission("tgsoul.admin")) {
            plugin.getMessageUtil().sendMessage(sender, "no-permission");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("Usage: /soul unban <player>");
            return false;
        }

        String playerName = args[1];
        Player target = Bukkit.getPlayer(playerName);
        
        if (plugin.getSoulManager().unbanPlayer(playerName)) {
            sender.sendMessage("§aUnbanned and revived §6" + playerName + "§a.");
        } else {
            // If player is not banned but has < max souls, set to max
            if (target != null) {
                PlayerSoulData data = plugin.getSoulManager().getOrCreatePlayerData(target);
                if (data.getSouls() < plugin.getSoulManager().getMaxSouls()) {
                    plugin.getSoulManager().setSouls(target, plugin.getSoulManager().getMaxSouls());
                    sender.sendMessage("§aSet §6" + playerName + "§a's souls to maximum.");
                } else {
                    sender.sendMessage("§cPlayer not found or not banned, and already has maximum souls.");
                }
            } else {
                sender.sendMessage("§cPlayer not found or not banned.");
            }
        }

        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("tgsoul.admin")) {
            plugin.getMessageUtil().sendMessage(sender, "no-permission");
            return true;
        }

        try {
            // Save current SoulManager data to avoid loss
            plugin.getSoulManager().saveAllData();
            // Reload the configuration
            plugin.getConfigManager().reloadConfig();
            // Reload messages
            plugin.getMessageUtil().reloadMessages();
            // Reload SoulManager with new config
            plugin.getSoulManager().reload();
            // Confirm success to the sender
            plugin.getMessageUtil().sendMessage(sender, "config-reloaded");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to reload TGSoul configuration: " + e.getMessage());
            e.printStackTrace();
            plugin.getMessageUtil().sendMessage(sender, "reload-failed");
            return false;
        }

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
            if (sender instanceof Player player) {
                targetName = player.getName();
            } else {
                sender.sendMessage("Usage: /soul stats <player>");
                return false;
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

    private boolean handlePack(CommandSender sender, String[] args) {
        if (!sender.hasPermission("tgsoul.admin")) {
            plugin.getMessageUtil().sendMessage(sender, "no-permission");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("Usage: /soul pack <player> <CustomModelData>");
            return false;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            plugin.getMessageUtil().sendMessage(sender, "player-not-found");
            return true;
        }

        try {
            int customModelData = Integer.parseInt(args[2]);
            if (customModelData < 1 || customModelData > 10) {
                sender.sendMessage("§cCustomModelData must be between 1 and 10.");
                return true;
            }

            // Set the player's CustomModelData
            plugin.getSoulManager().setPlayerCustomModelData(target.getUniqueId(), customModelData);
            
            sender.sendMessage("§aSet §6" + target.getName() + "§a's soul CustomModelData to §6" + customModelData + "§a.");
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid CustomModelData. Must be a number between 1 and 10.");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("help", "recipe", "give", "take", "set", "get", "unban", "reload", "top", "stats", "pack");
            for (String subCommand : subCommands) {
                if (subCommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("give") ||
                args[0].equalsIgnoreCase("take") ||
                args[0].equalsIgnoreCase("set") ||
                args[0].equalsIgnoreCase("get") ||
                args[0].equalsIgnoreCase("unban") ||
                args[0].equalsIgnoreCase("stats") ||
                args[0].equalsIgnoreCase("pack"))) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                    completions.add(player.getName());
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("pack")) {
            for (int i = 1; i <= 10; i++) {
                completions.add(String.valueOf(i));
            }
        }

        return completions;
    }
}