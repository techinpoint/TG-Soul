package com.tgsoul.managers;

import com.tgsoul.TGSoulPlugin;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final TGSoulPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(TGSoulPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig(); // ✅ ensures it's never null
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

    // --- Existing getters remain unchanged ---
    public int getStartingSouls() { return config.getInt("soul.starting", 3); }
    public int getMaxSouls() { return config.getInt("soul.max", 3); }
    public String getSoulMaterial() { return config.getString("soul.material", "GHAST_TEAR"); }
    public String getBanMode() { return config.getString("soul.ban-mode", "permanent"); }
    public String getBanTime() { return config.getString("soul.ban-time", "7d"); }
    public boolean isReviveAllowed() { return config.getBoolean("soul.allow-revive", true); }
    public boolean areEffectsEnabled() {
        return config.getBoolean("soul.effects.lose.enabled", true) ||
                config.getBoolean("soul.effects.gain.enabled", true);
    }
    public boolean areLoseEffectsEnabled() { return config.getBoolean("soul.effects.lose.enabled", true); }
    public boolean areGainEffectsEnabled() { return config.getBoolean("soul.effects.gain.enabled", true); }
    public String getLoseParticle() { return config.getString("soul.effects.lose.particle", "SMOKE_NORMAL"); }
    public String getGainParticle() { return config.getString("soul.effects.gain.particle", "VILLAGER_HAPPY"); }
    public int getLoseParticleCount() { return config.getInt("soul.effects.lose.count", 20); }
    public int getGainParticleCount() { return config.getInt("soul.effects.gain.count", 15); }
    public String getAdvancedLoseParticle() { return config.getString("soul.advanced-effects.lose.particle", "DUST"); }
    public String getAdvancedGainParticle() { return config.getString("soul.advanced-effects.gain.particle", "DUST"); }
    public String getAdvancedLoseColor() { return config.getString("soul.advanced-effects.lose.color", "#808080"); }
    public String getAdvancedGainColor() { return config.getString("soul.advanced-effects.gain.color", "#00FF00"); }
    public double getAdvancedLoseSize() { return config.getDouble("soul.advanced-effects.lose.size", 1.0); }
    public double getAdvancedGainSize() { return config.getDouble("soul.advanced-effects.gain.size", 1.2); }
    public boolean isGeyserEnabled() { return config.getBoolean("geyser.enabled", true); }
    public String getBedrockSoulItem() { return config.getString("geyser.bedrock-items.soul-item", "GHAST_TEAR"); }
    public double getBedrockParticleMultiplier() { return config.getDouble("geyser.bedrock-particles.multiplier", 0.5); }
    public String getDatabaseType() { return config.getString("database.type", "yaml"); }
    public boolean isAutoSaveEnabled() { return config.getBoolean("database.auto-save", true); }
    public int getSaveInterval() { return config.getInt("database.save-interval", 300); }

    // New getter for Revival Token material
    public String getRevivalTokenMaterial() {
        return config.getString("revival-token.material", "BEACON");
    }

    // New getters for sounds
    public String getRevivalSound() {
        return config.getString("soul.sounds.revival", "BLOCK_BEACON_ACTIVATE");
    }

    public String getWithdrawSound() {
        return config.getString("soul.sounds.withdraw", "BLOCK_GLASS_BREAK");
    }

    public String getGainSound() {
        return config.getString("soul.sounds.gain", "ENTITY_EXPERIENCE_ORB_PICKUP");
    }

    // New getter for drop-on-mob-death setting
    public boolean shouldDropOnMobDeath() {
        return config.getBoolean("soul.drop-on-mob-death", false);
    }

    // CustomModelData system getters
    public boolean isCustomModelDataEnabled() {
        return config.getBoolean("soul.custom-model-data.enabled", true);
    }

    public int getDefaultCustomModelData() {
        return config.getInt("soul.custom-model-data.default", 0);
    }

    public int getMinCustomModelData() {
        return config.getInt("soul.custom-model-data.min", 1);
    }

    public int getMaxCustomModelData() {
        return config.getInt("soul.custom-model-data.max", 10);
    }

    // New getters for advanced lose effect particles
    public String getAdvancedLoseParticle1() {
        return config.getString("advanced-effects.lose.particle1", "TRIAL_SPAWNER_DETECTION");
    }

    public String getAdvancedLoseParticle2() {
        return config.getString("advanced-effects.lose.particle2", "FALLING_DUST");
    }
}