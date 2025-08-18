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
            playBasicLoseEffect(player, location);
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
            playBasicGainEffect(player, location);
        }
    }
    
    private void playBasicLoseEffect(Player player, Location location) {
        try {
            String particleName = plugin.getConfigManager().getLoseParticle();
            Particle particle = Particle.valueOf(particleName);
            int count = getAdjustedParticleCount(player, plugin.getConfigManager().getLoseParticleCount());
            
            player.spawnParticle(particle, location, count, 0.5, 0.5, 0.5, 0.1);
        } catch (IllegalArgumentException e) {
            // Fallback to smoke if particle doesn't exist
            int count = getAdjustedParticleCount(player, 20);
            player.spawnParticle(Particle.SMOKE, location, count, 0.5, 0.5, 0.5, 0.1);
        }
    }
    
    private void playBasicGainEffect(Player player, Location location) {
        try {
            String particleName = plugin.getConfigManager().getGainParticle();
            Particle particle = Particle.valueOf(particleName);
            int count = getAdjustedParticleCount(player, plugin.getConfigManager().getGainParticleCount());
            
            player.spawnParticle(particle, location, count, 0.5, 0.5, 0.5, 0.1);
        } catch (IllegalArgumentException e) {
            // Fallback to happy villager if particle doesn't exist
            int count = getAdjustedParticleCount(player, 15);
            player.spawnParticle(Particle.HAPPY_VILLAGER, location, count, 0.5, 0.5, 0.5, 0.1);
        }
    }
    
    private void playAdvancedLoseEffect(Player player, Location location) {
        try {
            String particleName = plugin.getConfigManager().getAdvancedLoseParticle();
            Particle particle = Particle.valueOf(particleName);
            
            if (particle == Particle.DUST) {
                Color color = parseColor(plugin.getConfigManager().getAdvancedLoseColor());
                double size = plugin.getConfigManager().getAdvancedParticleSize();
                Particle.DustOptions dustOptions = new Particle.DustOptions(color, (float) size);
                
                int count = getAdjustedParticleCount(player, 20);
                player.spawnParticle(particle, location, count, 0.5, 0.5, 0.5, 0, dustOptions);
            } else {
                playBasicLoseEffect(player, location);
            }
        } catch (Exception e) {
            playBasicLoseEffect(player, location);
        }
    }
    
    private void playAdvancedGainEffect(Player player, Location location) {
        try {
            String particleName = plugin.getConfigManager().getAdvancedGainParticle();
            Particle particle = Particle.valueOf(particleName);
            
            if (particle == Particle.DUST) {
                Color color = parseColor(plugin.getConfigManager().getAdvancedGainColor());
                double size = plugin.getConfigManager().getAdvancedParticleSize();
                Particle.DustOptions dustOptions = new Particle.DustOptions(color, (float) size);
                
                int count = getAdjustedParticleCount(player, 15);
                player.spawnParticle(particle, location, count, 0.5, 0.5, 0.5, 0, dustOptions);
            } else {
                playBasicGainEffect(player, location);
            }
        } catch (Exception e) {
            playBasicGainEffect(player, location);
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
}