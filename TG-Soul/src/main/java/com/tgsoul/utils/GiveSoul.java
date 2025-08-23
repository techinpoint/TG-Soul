package com.tgsoul.utils;

import com.tgsoul.TGSoulPlugin;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class GiveSoul {
    
    /**
     * Gives soul items to new players with proper CustomModelData assignment
     */
    public static void giveToNewJoiner(Player player, TGSoulPlugin plugin) {
        int startingSouls = plugin.getConfigManager().getStartingSouls();
        
        // Check if this is truly a new player (no existing data)
        boolean isNewPlayer = plugin.getSoulManager().getPlayerData(player.getUniqueId()) == null;
        
        if (isNewPlayer) {
            String material = plugin.getConfigManager().getSoulMaterial();
            
            // Assign CustomModelData for new players
            int customModelData = assignCustomModelData(player, plugin);
            
            // Create the soul item with the assigned CustomModelData
            ItemStack soulItem = ItemUtil.createSoulItem(player.getName(), material, customModelData);
            
            // Ensure player data is created
            plugin.getSoulManager().getOrCreatePlayerData(player);
            
            // Give the soul item to the player
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(soulItem);
            } else {
                player.getWorld().dropItemNaturally(player.getLocation(), soulItem);
            }
            
            plugin.getMessageUtil().sendMessage(player, "soul-gained",
                    java.util.Map.of("souls", String.valueOf(startingSouls)));
            
            plugin.getLogger().info("New player " + player.getName() + " assigned CustomModelData: " + customModelData);
        }
    }
    
    /**
     * Assigns CustomModelData to a new player
     * @param player The player to assign CMD to
     * @param plugin The plugin instance
     * @return The assigned CustomModelData value
     */
    private static int assignCustomModelData(Player player, TGSoulPlugin plugin) {
        if (!plugin.getConfigManager().isCustomModelDataEnabled()) {
            return plugin.getConfigManager().getDefaultCustomModelData();
        }
        
        // Check if resource pack is available
        boolean hasResourcePack = !org.bukkit.Bukkit.getServer().getResourcePack().isEmpty();
        
        if (!hasResourcePack) {
            // No resource pack, use default CMD
            int defaultCmd = plugin.getConfigManager().getDefaultCustomModelData();
            plugin.getSoulManager().setPlayerCustomModelData(player.getUniqueId(), defaultCmd);
            return defaultCmd;
        }
        
        // Resource pack available, assign random CMD
        Random random = new Random();
        int minCmd = plugin.getConfigManager().getMinCustomModelData();
        int maxCmd = plugin.getConfigManager().getMaxCustomModelData();
        int randomCmd = random.nextInt(maxCmd - minCmd + 1) + minCmd; // Random between min and max inclusive
        
        // Save the CustomModelData for this player
        plugin.getSoulManager().setPlayerCustomModelData(player.getUniqueId(), randomCmd);
        
        return randomCmd;
    }
    
    /**
     * Gives a specific number of soul items to a player
     * @param player The player to give souls to
     * @param plugin The plugin instance
     * @param amount The number of souls to give
     */
    public static void giveSoulsToPlayer(Player player, TGSoulPlugin plugin, int amount) {
        String material = plugin.getConfigManager().getSoulMaterial();
        int customModelData = plugin.getSoulManager().getPlayerCustomModelData(player.getUniqueId());
        
        for (int i = 0; i < amount; i++) {
            ItemStack soulItem = ItemUtil.createSoulItem(player.getName(), material, customModelData);
            
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(soulItem);
            } else {
                player.getWorld().dropItemNaturally(player.getLocation(), soulItem);
            }
        }
    }
}