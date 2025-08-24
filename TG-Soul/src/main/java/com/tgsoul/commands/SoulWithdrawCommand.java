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
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }

        if (!plugin.isEnabled()) {
            sender.sendMessage("§cPlugin is currently disabled.");
            return true;
        }

        if (args.length > 0) {
            sender.sendMessage("§cUsage: /" + label + " (no arguments needed)");
            return true;
        }

        Player player = (Player) sender;

        if (!sender.hasPermission("tgsoul.withdraw")) {
            plugin.getMessageUtil().sendMessage(sender, "no-permission");
            return true;
        }

        try {
            // Check if player would have 0 souls after withdrawal
            PlayerSoulData data = plugin.getSoulManager().getOrCreatePlayerData(player);
            if (data.getSouls() <= 1) {
                plugin.getMessageUtil().sendMessage(player, "cannot-withdraw-last-soul");
                return true;
            }

            plugin.getSoulManager().withdrawSoul(player);
            plugin.getMessageUtil().sendMessage(player, "soul-withdrawn",
                    Map.of("souls", String.valueOf(data.getSouls() - 1)));
        } catch (Exception e) {
            plugin.getLogger().severe("Error in soulwithdraw command: " + e.getMessage());
            e.printStackTrace();
            player.sendMessage("§cAn error occurred while withdrawing soul. Please try again.");
        }

        return true;
    }
}