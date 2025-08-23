package com.tgsoul.utils;

import com.tgsoul.TGSoulPlugin;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class GiveSoul {
    public static void giveToNewJoiner(Player player, TGSoulPlugin plugin) {
        int startingSouls = plugin.getConfigManager().getStartingSouls();
        
        // Check if this is truly a new player (no existing data)
        boolean isNewPlayer = plugin.getSoulManager().getPlayerData(player.getUniqueId()) == null;
        
        if (isNewPlayer) {
            String material = plugin.getConfigManager().getSoulMaterial();
            
            // Generate random CustomModelData for new joiners if resource pack is set
            Integer customModelData = null;
            if (!org.bukkit.Bukkit.getServer().getResourcePack().isEmpty()) {
                Random random = new Random();
                customModelData = random.nextInt(10) + 1; // 1-10
                
                // Save the CustomModelData for this player
                plugin.getSoulManager().setPlayerCustomModelData(player.getUniqueId(), customModelData);
            }
            
            // Create the soul item
            ItemStack soulItem;
            if (customModelData != null) {
                soulItem = ItemUtil.createSoulItemWithCustomModelData(player.getName(), material, customModelData);
            } else {
                soulItem = ItemUtil.createSoulItem(player.getName(), material);
            }
            
            // Give the soul item to the player
            plugin.getSoulManager().getOrCreatePlayerData(player); // Ensure data is created
            
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(soulItem);
            } else {
                player.getWorld().dropItemNaturally(player.getLocation(), soulItem);
            }
            
            plugin.getMessageUtil().sendMessage(player, "soul-gained",
                    java.util.Map.of("souls", String.valueOf(startingSouls))); // Show starting souls count
        }
    }
}