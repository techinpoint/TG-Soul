package com.tgsoul;

import com.tgsoul.commands.SoulCommand;
import com.tgsoul.commands.SoulWithdrawCommand;
import com.tgsoul.listeners.BlockBreakListener;
import com.tgsoul.listeners.CraftingListener;
import com.tgsoul.listeners.GUIListener;
import com.tgsoul.listeners.PlayerDeathListener;
import com.tgsoul.listeners.PlayerInteractListener;
import com.tgsoul.listeners.PlayerJoinListener;
import com.tgsoul.managers.ConfigManager;
import com.tgsoul.managers.ParticleManager;
import com.tgsoul.managers.SoulManager;
import com.tgsoul.utils.MessageUtil;
import com.tgsoul.utils.VersionUtil;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class TGSoulPlugin extends JavaPlugin {

    private static TGSoulPlugin instance;
    private SoulManager soulManager;
    private ConfigManager configManager;
    private ParticleManager particleManager;
    private MessageUtil messageUtil;
    private VersionUtil versionUtil;

    @Override
    public void onEnable() {
        instance = this;
        versionUtil = new VersionUtil();
        getLogger().info("Detected Minecraft version: " + versionUtil.getVersion());
        configManager = new ConfigManager(this);
        configManager.loadConfig(); // Explicitly load config
        messageUtil = new MessageUtil(this);
        particleManager = new ParticleManager(this);
        soulManager = new SoulManager(this);

        // Register commands & listeners
        registerCommands();
        registerListeners();

        // GeyserMC check
        checkGeyserCompatibility();

        // Setup auto-save if enabled
        if (configManager.isAutoSaveEnabled()) {
            int saveInterval = configManager.getSaveInterval() * 20; // Convert seconds to ticks
            new BukkitRunnable() {
                @Override
                public void run() {
                    soulManager.saveAllData();
                }
            }.runTaskTimer(this, saveInterval, saveInterval);
            getLogger().info("Auto-save enabled with interval of " + configManager.getSaveInterval() + " seconds.");
        }

        getLogger().info("TGSoul has been enabled successfully!");
        getLogger().info("Compatible with MC " + versionUtil.getVersion() +
                (isGeyserPresent() ? " with GeyserMC support" : ""));
    }

    @Override
    public void onDisable() {
        if (soulManager != null) {
            try {
                soulManager.saveAllData();
                getLogger().info("Player data saved on disable.");
            } catch (Exception e) {
                getLogger().severe("Failed to save player data on disable: " + e.getMessage());
                e.printStackTrace();
            }
        }
        getLogger().info("TGSoul has been disabled!");
    }

    private void registerCommands() {
        getCommand("soul").setExecutor(new SoulCommand(this));
        getCommand("soulwithdraw").setExecutor(new SoulWithdrawCommand(this));
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(new CraftingListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
    }

    private void checkGeyserCompatibility() {
        if (isGeyserPresent()) {
            getLogger().info("GeyserMC detected! Enabling Bedrock compatibility features.");
        }
    }

    public boolean isGeyserPresent() {
        return getServer().getPluginManager().getPlugin("Geyser-Spigot") != null;
    }

    // Getters
    public static TGSoulPlugin getInstance() {
        return instance;
    }

    public SoulManager getSoulManager() {
        return soulManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ParticleManager getParticleManager() {
        return particleManager;
    }

    public MessageUtil getMessageUtil() {
        return messageUtil;
    }

    public VersionUtil getVersionUtil() {
        return versionUtil;
    }
}