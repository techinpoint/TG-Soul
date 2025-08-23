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
            playDustPillarEffect(player, location, true); // true for lose effect
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
            playDustPillarEffect(player, location, false); // false for gain effect
        } else {
            playLegacyGainEffect(player, location);
        }
    }

    private void playLegacyLoseEffect(Player player, Location location) {
        try {
            // Use smoke for 1.10-1.20 via ViaBackwards
            Particle particle = Particle.SMOKE;
            int count = getAdjustedParticleCount(player, 20);
            player.spawnParticle(particle, location, count, 0.5, 0.5, 0.5, 0.1);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Failed to spawn smoke particle. Falling back to default.");
            int count = getAdjustedParticleCount(player, 20);
            player.spawnParticle(Particle.SMOKE, location, count, 0.5, 0.5, 0.5, 0.1);
        }
    }

    private void playLegacyGainEffect(Player player, Location location) {
        try {
            // Use happy_villager for 1.10-1.20 via ViaBackwards
            Particle particle = Particle.HAPPY_VILLAGER;
            int count = getAdjustedParticleCount(player, 15);
            player.spawnParticle(particle, location, count, 0.5, 0.5, 0.5, 0.1);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Failed to spawn happy_villager particle. Falling back to default.");
            int count = getAdjustedParticleCount(player, 15);
            player.spawnParticle(Particle.HAPPY_VILLAGER, location, count, 0.5, 0.5, 0.5, 0.1);
        }
    }

    private void playDustPillarEffect(Player player, Location location, boolean isLoseEffect) {
        try {
            // Use dust_pillar (mace smash particle) for 1.21+
            Particle particle = Particle.DUST_PILLAR;
            
            // Get color based on effect type
            String colorHex = isLoseEffect ? 
                plugin.getConfigManager().getAdvancedLoseColor() : 
                plugin.getConfigManager().getAdvancedGainColor();
            Color color = parseColor(colorHex);
            double size = plugin.getConfigManager().getAdvancedParticleSize();
            
            // Create dust options for the pillar effect
            Particle.DustOptions dustOptions = new Particle.DustOptions(color, (float) size);
            
            int count = getAdjustedParticleCount(player, isLoseEffect ? 20 : 15);
            player.spawnParticle(particle, location, count, 0.5, 0.5, 0.5, 0, dustOptions);
        } catch (Exception e) {
            // Fallback to legacy effects if dust_pillar is not available
            if (isLoseEffect) {
                playLegacyLoseEffect(player, location);
            } else {
                playLegacyGainEffect(player, location);
            }
        }
    }

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
            return Color.GRAY; // Default color
        }
    }

    private int getAdjustedParticleCount(Player player, int baseCount) {
        if (plugin.isGeyserPresent() && GeyserUtil.isBedrockPlayer(player)) {
            double multiplier = plugin.getConfigManager().getBedrockParticleMultiplier();
            return Math.max(1, (int) (baseCount * multiplier));
        }
        return baseCount;
    }

    // Optional reload method for consistency (no-op since no caching, but safe)
    public void reload() {
        try {
            // No cached values to reload, but validate particle names if needed in future
            plugin.getLogger().info("ParticleManager reloaded successfully (no changes needed).");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to reload ParticleManager: " + e.getMessage());
            e.printStackTrace();
        }
    }
}