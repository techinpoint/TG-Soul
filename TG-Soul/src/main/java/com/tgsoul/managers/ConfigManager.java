package com.tgsoul.managers;

import com.tgsoul.TGSoulPlugin;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    
    private final TGSoulPlugin plugin;
    private FileConfiguration config;
    
    public ConfigManager(TGSoulPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
    }
    
    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }
    
    // Soul settings
    public int getStartingSouls() {
        return config.getInt("soul.starting", 3);
    }
    
    public int getMaxSouls() {
        return config.getInt("soul.max", 3);
    }
    
    public String getBanMode() {
        return config.getString("soul.ban-mode", "permanent");
    }
    
    public String getBanTime() {
        return config.getString("soul.ban-time", "7d");
    }
    
    public boolean isReviveAllowed() {
        return config.getBoolean("soul.allow-revive", true);
    }
    
    // Effect settings
    public boolean areEffectsEnabled() {
        return config.getBoolean("soul.effects.lose.enabled", true) || 
               config.getBoolean("soul.effects.gain.enabled", true);
    }
    
    public boolean areLoseEffectsEnabled() {
        return config.getBoolean("soul.effects.lose.enabled", true);
    }
    
    public boolean areGainEffectsEnabled() {
        return config.getBoolean("soul.effects.gain.enabled", true);
    }
    
    public String getLoseParticle() {
        return config.getString("soul.effects.lose.particle", "SMOKE_NORMAL");
    }
    
    public String getGainParticle() {
        return config.getString("soul.effects.gain.particle", "VILLAGER_HAPPY");
    }
    
    public int getLoseParticleCount() {
        return config.getInt("soul.effects.lose.count", 20);
    }
    
    public int getGainParticleCount() {
        return config.getInt("soul.effects.gain.count", 15);
    }
    
    // Advanced effects for 1.21+
    public String getAdvancedLoseParticle() {
        return config.getString("soul.advanced-effects.lose.particle", "DUST");
    }
    
    public String getAdvancedGainParticle() {
        return config.getString("soul.advanced-effects.gain.particle", "DUST");
    }
    
    public String getAdvancedLoseColor() {
        return config.getString("soul.advanced-effects.lose.color", "#808080");
    }
    
    public String getAdvancedGainColor() {
        return config.getString("soul.advanced-effects.gain.color", "#00FF00");
    }
    
    public double getAdvancedParticleSize() {
        return config.getDouble("soul.advanced-effects.lose.size", 1.0);
    }
    
    // GeyserMC settings
    public boolean isGeyserEnabled() {
        return config.getBoolean("geyser.enabled", true);
    }
    
    public String getBedrockSoulItem() {
        return config.getString("geyser.bedrock-items.soul-item", "PAPER");
    }
    
    public double getBedrockParticleMultiplier() {
        return config.getDouble("geyser.bedrock-particles.multiplier", 0.5);
    }
    
    // Database settings
    public String getDatabaseType() {
        return config.getString("database.type", "yaml");
    }
    
    public boolean isAutoSaveEnabled() {
        return config.getBoolean("database.auto-save", true);
    }
    
    public int getSaveInterval() {
        return config.getInt("database.save-interval", 300);
    }
}