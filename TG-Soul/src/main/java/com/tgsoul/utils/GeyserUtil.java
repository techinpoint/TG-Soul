package com.tgsoul.utils;

import org.bukkit.entity.Player;

public class GeyserUtil {
    
    /**
     * Check if a player is connecting from Bedrock Edition via GeyserMC
     * This is a simplified check - in a real implementation, you would
     * integrate with GeyserMC's API to properly detect Bedrock players
     */
    public static boolean isBedrockPlayer(Player player) {
        try {
            // This is a placeholder implementation
            // In a real plugin, you would use GeyserMC's API:
            // return GeyserApi.api().isBedrockPlayer(player.getUniqueId());
            
            // For now, we'll use a simple heuristic based on the player's client brand
            // This is not 100% accurate but works as a fallback
            return false; // Placeholder - implement proper Geyser detection
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get the Bedrock username for a Java player (if applicable)
     */
    public static String getBedrockUsername(Player player) {
        if (isBedrockPlayer(player)) {
            // In a real implementation, you would get this from GeyserMC's API
            return player.getName();
        }
        return null;
    }
    
    /**
     * Check if GeyserMC is available and properly loaded
     */
    public static boolean isGeyserAvailable() {
        try {
            Class.forName("org.geysermc.geyser.api.GeyserApi");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}