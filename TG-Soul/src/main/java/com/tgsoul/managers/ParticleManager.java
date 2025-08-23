package com.tgsoul.managers;

import com.tgsoul.TGSoulPlugin;
import com.tgsoul.utils.GeyserUtil;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class ParticleManager {

    private final TGSoulPlugin plugin;

    public ParticleManager(TGSoulPlugin plugin) {
        this.plugin = plugin;
    }

    public void playLoseEffect(Player player) {
        if (!plugin.getConfigManager().areLoseEffectsEnabled()) {
            return;
        }

        Location location = player.getLocation().add(0, 1, 0);

        if (plugin.getVersionUtil().isVersion121OrHigher()) {
            playAdvancedLoseEffect(player, location);
        } else {
            playLegacyLoseEffect(player, location);
        }
    }

    public void playGainEffect(Player player) {
        if (!plugin.getConfigManager().areGainEffectsEnabled()) {
            return;
        }

        Location location = player.getLocation().add(0, 1, 0);

        if (plugin.getVersionUtil().isVersion121OrHigher()) {
            playAdvancedGainEffect(player, location);
        } else {
            playLegacyGainEffect(player, location);
        }
    }

    /**
     * Legacy effects for Minecraft 1.20 and below
     * Uses "smash_ground" particle with gray color for lose
     */
    private void playLegacyLoseEffect(Player player, Location location) {
        try {
            // For legacy versions, use SMOKE_NORMAL as fallback for "smash_ground"
            Particle particle = Particle.SMOKE_NORMAL;
            int count = getAdjustedParticleCount(player, plugin.getConfigManager().getLoseParticleCount());
            
            // Spawn particles with spread
            player.spawnParticle(particle, location, count, 0.5, 0.5, 0.5, 0.1);
            
            plugin.getLogger().fine("Played legacy lose effect for " + player.getName() + " with " + count + " particles");
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to spawn legacy lose particle for " + player.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Legacy effects for Minecraft 1.20 and below
     * Uses "smash_ground" particle with cyan color for gain
     */
    private void playLegacyGainEffect(Player player, Location location) {
        try {
            // For legacy versions, use VILLAGER_HAPPY as fallback for "smash_ground"
            Particle particle = Particle.VILLAGER_HAPPY;
            int count = getAdjustedParticleCount(player, plugin.getConfigManager().getGainParticleCount());
            
            // Spawn particles with spread
            player.spawnParticle(particle, location, count, 0.5, 0.5, 0.5, 0.1);
            
            plugin.getLogger().fine("Played legacy gain effect for " + player.getName() + " with " + count + " particles");
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to spawn legacy gain particle for " + player.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Advanced effects for Minecraft 1.21+
     * Uses DUST_PILLAR particle with gray color and size 1.0 for lose
     */
    private void playAdvancedLoseEffect(Player player, Location location) {
        try {
            Particle particle = Particle.DUST_PILLAR;
            String colorHex = plugin.getConfigManager().getAdvancedLoseColor();
            Color color = parseColor(colorHex);
            double size = plugin.getConfigManager().getAdvancedLoseSize();
            
            // Create dust options for the pillar effect
            Particle.DustOptions dustOptions = new Particle.DustOptions(color, (float) size);
            
            int count = getAdjustedParticleCount(player, plugin.getConfigManager().getLoseParticleCount());
            
            // Spawn DUST_PILLAR particles
            player.spawnParticle(particle, location, count, 0.5, 0.5, 0.5, 0, dustOptions);
            
            plugin.getLogger().fine("Played advanced lose effect for " + player.getName() + 
                " with color " + colorHex + ", size " + size + ", count " + count);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to spawn advanced lose particle for " + player.getName() + 
                ": " + e.getMessage() + ". Falling back to legacy effect.");
            playLegacyLoseEffect(player, location);
        }
    }

    /**
     * Advanced effects for Minecraft 1.21+
     * Uses DUST_PILLAR particle with cyan color and size 1.2 for gain
     */
    private void playAdvancedGainEffect(Player player, Location location) {
        try {
            Particle particle = Particle.DUST_PILLAR;
            String colorHex = plugin.getConfigManager().getAdvancedGainColor();
            Color color = parseColor(colorHex);
            double size = plugin.getConfigManager().getAdvancedGainSize();
            
            // Create dust options for the pillar effect
            Particle.DustOptions dustOptions = new Particle.DustOptions(color, (float) size);
            
            int count = getAdjustedParticleCount(player, plugin.getConfigManager().getGainParticleCount());
            
            // Spawn DUST_PILLAR particles
            player.spawnParticle(particle, location, count, 0.5, 0.5, 0.5, 0, dustOptions);
            
            plugin.getLogger().fine("Played advanced gain effect for " + player.getName() + 
                " with color " + colorHex + ", size " + size + ", count " + count);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to spawn advanced gain particle for " + player.getName() + 
                ": " + e.getMessage() + ". Falling back to legacy effect.");
            playLegacyGainEffect(player, location);
        }
    }

    /**
     * Parses hex color string to Bukkit Color
     */
    private Color parseColor(String hexColor) {
        try {
            if (hexColor.startsWith("#")) {
                hexColor = hexColor.substring(1);
            }

            int rgb = Integer.parseInt(hexColor, 16);
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;

            return Color.fromRGB(r, g, b);
        } catch (Exception e) {
            plugin.getLogger().warning("Invalid color format: " + hexColor + ". Using default gray.");
            return Color.GRAY; // Default color
        }
    }

    /**
     * Adjusts particle count for Bedrock players if Geyser is present
     */
    private int getAdjustedParticleCount(Player player, int baseCount) {
        if (plugin.isGeyserPresent() && GeyserUtil.isBedrockPlayer(player)) {
            double multiplier = plugin.getConfigManager().getBedrockParticleMultiplier();
            return Math.max(1, (int) (baseCount * multiplier));
        }
        return baseCount;
    }

    /**
     * Reload method for configuration changes
     */
    public void reload() {
        try {
            plugin.getLogger().info("ParticleManager reloaded successfully.");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to reload ParticleManager: " + e.getMessage());
            e.printStackTrace();
        }
    }
}