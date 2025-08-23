package com.tgsoul.utils;

import com.tgsoul.TGSoulPlugin;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class GiveSoul {
    public static void giveToNewJoiner(Player player, TGSoulPlugin plugin) {
        int startingSouls = plugin.getConfigManager().getStartingSouls();
        if (plugin.getSoulManager().getOrCreatePlayerData(player).getSouls() == startingSouls) {
            String material = plugin.getConfigManager().getSoulMaterial();
            
            // Generate random CustomModelData for new joiners
            Random random = new Random();
            int customModelData = random.nextInt(10) + 1; // 1-10
            
            // Save the CustomModelData for this player
            plugin.getSoulManager().setPlayerCustomModelData(player.getUniqueId(), customModelData);
            
            ItemStack soulItem = ItemUtil.createSoulItemWithCustomModelData(player.getName(), material, customModelData);
            
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(soulItem);
            } else {
                player.getWorld().dropItemNaturally(player.getLocation(), soulItem);
            }
            
            plugin.getMessageUtil().sendMessage(player, "soul-gained"); // Reuse existing message
        }
    }
}