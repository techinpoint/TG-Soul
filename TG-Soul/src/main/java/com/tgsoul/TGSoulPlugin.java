package com.tgsoul;

import com.tgsoul.commands.ReviveCommand;
import com.tgsoul.commands.SoulCommand;
import com.tgsoul.commands.SoulWithdrawCommand;
import com.tgsoul.listeners.PlayerDeathListener;
import com.tgsoul.listeners.PlayerInteractListener;
import com.tgsoul.listeners.PlayerJoinListener;
import com.tgsoul.managers.ConfigManager;
import com.tgsoul.managers.ParticleManager;
import com.tgsoul.managers.SoulManager;
import com.tgsoul.utils.MessageUtil;
import com.tgsoul.utils.VersionUtil;
import org.bukkit.plugin.java.JavaPlugin;

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
        
        // Initialize version utility first
        versionUtil = new VersionUtil();
        getLogger().info("Detected Minecraft version: " + versionUtil.getVersion());
        
        // Initialize managers
        configManager = new ConfigManager(this);
        messageUtil = new MessageUtil(this);
        particleManager = new ParticleManager(this);
        soulManager = new SoulManager(this);
        
        // Load configuration
        configManager.loadConfig();
        
        // Register commands
        registerCommands();
        
        // Register listeners
        registerListeners();
        
        // Check for GeyserMC
        checkGeyserCompatibility();
        
        getLogger().info("TGSoul has been enabled successfully!");
        getLogger().info("Compatible with MC " + versionUtil.getVersion() + 
                        (isGeyserPresent() ? " with GeyserMC support" : ""));
    }
    
    @Override
    public void onDisable() {
        if (soulManager != null) {
            soulManager.saveAllData();
        }
        getLogger().info("TGSoul has been disabled!");
    }
    
    private void registerCommands() {
        getCommand("soul").setExecutor(new SoulCommand(this));
        getCommand("soulwithdraw").setExecutor(new SoulWithdrawCommand(this));
        getCommand("revive").setExecutor(new ReviveCommand(this));
    }
    
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
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