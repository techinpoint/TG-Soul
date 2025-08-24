package com.tgsoul.managers;

import com.tgsoul.TGSoulPlugin;
import com.tgsoul.utils.GeyserUtil;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
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
     * Legacy lose effect for Minecraft 1.10 to 1.20.x
     * Uses SMOKE_NORMAL as a fallback particle
     */
    private void playLegacyLoseEffect(Player player, Location location) {
        try {
            Particle particle = Particle.SMOKE;
            int count = getAdjustedParticleCount(player, plugin.getConfigManager().getLoseParticleCount());
            player.spawnParticle(particle, location, count, 0.5, 0.5, 0.5, 0.1);

            plugin.getLogger().fine("Played legacy lose effect for " + player.getName() + " with " + count + " particles");
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to spawn legacy lose particle for " + player.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Legacy gain effect for Minecraft 1.10 to 1.20.x
     * Uses HAPPY_VILLAGER as a celebratory particle
     */
    private void playLegacyGainEffect(Player player, Location location) {
        try {
            Particle particle = Particle.HAPPY_VILLAGER;
            int count = getAdjustedParticleCount(player, plugin.getConfigManager().getGainParticleCount());
            player.spawnParticle(particle, location, count, 0.5, 0.5, 0.5, 0.1);

            plugin.getLogger().fine("Played legacy gain effect for " + player.getName() + " with " + count + " particles");
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to spawn legacy gain particle for " + player.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Advanced lose effect for Minecraft 1.21+
     * Uses configurable particles (e.g., TRIAL_SPAWNER_DETECTION and FALLING_DUST)
     */
    private void playAdvancedLoseEffect(Player player, Location location) {
        if (!plugin.getConfigManager().areLoseEffectsEnabled()) {
            return;
        }

        try {
            int count = getAdjustedParticleCount(player, plugin.getConfigManager().getLoseParticleCount());
            double size = plugin.getConfigManager().getAdvancedLoseSize();

            // Get configurable particles from config
            String particle1Name = plugin.getConfigManager().getAdvancedLoseParticle1().toUpperCase();
            String particle2Name = plugin.getConfigManager().getAdvancedLoseParticle2().toUpperCase();
            Particle particle1 = Particle.valueOf(particle1Name);
            Particle particle2 = Particle.valueOf(particle2Name);

            // Spawn first particle (e.g., TRIAL_SPAWNER_DETECTION for cyan stars)
            player.spawnParticle(particle1, location, count, 0.5, 0.5, 0.5, size);

            // Spawn second particle (e.g., FALLING_DUST for gray dust)
            if (particle2 == Particle.FALLING_DUST) {
                BlockData blockData = Material.STONE.createBlockData(); // Gray dust from stone
                player.spawnParticle(particle2, location, count, 0.5, 0.5, 0.5, size, blockData);
            } else {
                player.spawnParticle(particle2, location, count, 0.5, 0.5, 0.5, size);
            }

            plugin.getLogger().fine("Played advanced lose effect for " + player.getName() +
                    " with " + particle1Name + " and " + particle2Name + ", count " + count);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid particle name in config for lose effect: " + e.getMessage() +
                    ". Falling back to legacy effect.");
            playLegacyLoseEffect(player, location);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to spawn advanced lose particle for " + player.getName() +
                    ": " + e.getMessage() + ". Falling back to legacy effect.");
            playLegacyLoseEffect(player, location);
        }
    }

    /**
     * Advanced gain effect for Minecraft 1.21+
     * Uses DUST_PILLAR with configurable color
     */
    private void playAdvancedGainEffect(Player player, Location location) {
        if (!plugin.getConfigManager().areGainEffectsEnabled()) {
            return;
        }

        try {
            Particle particle = Particle.DUST_PILLAR;
            String colorHex = plugin.getConfigManager().getAdvancedGainColor();
            Color color = parseColor(colorHex);
            double size = plugin.getConfigManager().getAdvancedGainSize();
            int count = getAdjustedParticleCount(player, plugin.getConfigManager().getGainParticleCount());

            Particle.DustOptions dustOptions = new Particle.DustOptions(color, (float) size);
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
            plugin.getLogger().warning("Invalid color format: " + hexColor + ". Using default cyan.");
            return Color.fromRGB(0, 255, 255); // Default to cyan (#00FFFF)
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