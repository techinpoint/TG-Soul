package com.tgsoul.utils;

import com.tgsoul.TGSoulPlugin;
import com.tgsoul.data.PlayerSoulData;
import com.tgsoul.managers.SoulManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

/**
 * Utility class for handling soul data persistence
 */
public class SoulDataUtil {
    
    private final SoulManager soulManager;
    private final TGSoulPlugin plugin;
    
    public SoulDataUtil(SoulManager soulManager, TGSoulPlugin plugin) {
        this.soulManager = soulManager;
        this.plugin = plugin;
    }
    
    /**
     * Loads player data from file
     */
    public void loadPlayerData(File dataFile, FileConfiguration dataConfig, 
                              Map<UUID, PlayerSoulData> playerData, 
                              Map<UUID, Integer> playerCustomModelData) {
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
                saveToFile(dataConfig, dataFile); // Initialize with default structure if empty
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create player data file: " + e.getMessage());
                e.printStackTrace();
                return;
            }
        }

        // Reload the data configuration
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        // Load all player data
        playerData.clear();
        playerCustomModelData.clear();
        
        for (String uuidString : dataConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidString);
                ConfigurationSection section = dataConfig.getConfigurationSection(uuidString);
                if (section != null) {
                    String playerName = section.getString("playerName", "Unknown");
                    int souls = section.getInt("souls", plugin.getConfigManager().getStartingSouls());
                    boolean needsRevival = section.getBoolean("needsRevival", false);
                    String lastSeen = section.getString("lastSeen", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    
                    playerData.put(uuid, new PlayerSoulData(uuid, playerName, souls, needsRevival, lastSeen));
                    
                    // Load CustomModelData if it exists
                    if (section.contains("customModelData")) {
                        int customModelData = section.getInt("customModelData", plugin.getConfigManager().getDefaultCustomModelData());
                        playerCustomModelData.put(uuid, customModelData);
                    }
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in playerdata.yml: " + uuidString);
            } catch (Exception e) {
                plugin.getLogger().warning("Error loading data for UUID " + uuidString + ": " + e.getMessage() + ". Skipping this entry.");
            }
        }
        plugin.getLogger().info("Loaded " + playerData.size() + " player data entries.");
    }
    
    /**
     * Saves all player data to file
     */
    public void saveAllPlayerData(Map<UUID, PlayerSoulData> playerData, 
                                 Map<UUID, Integer> playerCustomModelData,
                                 FileConfiguration dataConfig, File dataFile) {
        for (PlayerSoulData data : playerData.values()) {
            savePlayerData(data, playerCustomModelData, dataConfig);
        }
        saveToFile(dataConfig, dataFile);
    }
    
    /**
     * Saves individual player data
     */
    public void savePlayerData(PlayerSoulData data, Map<UUID, Integer> playerCustomModelData, 
                              FileConfiguration dataConfig) {
        String path = data.getUuid().toString();
        dataConfig.set(path + ".name", data.getPlayerName());
        dataConfig.set(path + ".souls", data.getSouls());
        dataConfig.set(path + ".needsRevival", data.needsRevival());
        dataConfig.set(path + ".lastSeen", data.getLastSeen());
        
        // Save CustomModelData if it exists
        Integer customModelData = playerCustomModelData.get(data.getUuid());
        if (customModelData != null) {
            dataConfig.set(path + ".customModelData", customModelData);
        }
        
        plugin.getLogger().info("Saved data for " + data.getPlayerName() + ": " + data.getSouls() + " souls");
    }
    
    /**
     * Saves configuration to file asynchronously
     */
    public void saveToFile(FileConfiguration dataConfig, File dataFile) {
        FileConfiguration configToSave = dataConfig; // Effectively final copy for lambda
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                configToSave.save(dataFile);
                plugin.getLogger().info("Player data saved to playerdata.yml successfully.");
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save player data to playerdata.yml: " + e.getMessage());
                e.printStackTrace();
                Bukkit.getScheduler().runTask(plugin, () -> {
                    for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
                        if (player.hasPermission("tgsoul.admin")) {
                            plugin.getMessageUtil().sendMessage(player, "data-save-failed");
                        }
                    }
                });
            }
        });
    }
}