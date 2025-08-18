package com.tgsoul.managers;

import com.tgsoul.TGSoulPlugin;
import com.tgsoul.data.PlayerSoulData;
import com.tgsoul.utils.ItemUtil;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SoulManager {
    
    private final TGSoulPlugin plugin;
    private final Map<UUID, PlayerSoulData> playerData;
    private final File dataFile;
    private FileConfiguration dataConfig;
    
    public SoulManager(TGSoulPlugin plugin) {
        this.plugin = plugin;
        this.playerData = new ConcurrentHashMap<>();
        this.dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        loadData();
    }
    
    public void loadData() {
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create player data file!");
                e.printStackTrace();
                return;
            }
        }
        
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        
        // Load all player data
        for (String uuidString : dataConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidString);
                String playerName = dataConfig.getString(uuidString + ".name", "Unknown");
                int souls = dataConfig.getInt(uuidString + ".souls", getStartingSouls());
                boolean needsRevival = dataConfig.getBoolean(uuidString + ".needsRevival", false);
                String lastSeen = dataConfig.getString(uuidString + ".lastSeen", "Never");
                
                PlayerSoulData data = new PlayerSoulData(uuid, playerName, souls, needsRevival, lastSeen);
                playerData.put(uuid, data);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in data file: " + uuidString);
            }
        }
        
        plugin.getLogger().info("Loaded data for " + playerData.size() + " players.");
    }
    
    public void saveAllData() {
        for (PlayerSoulData data : playerData.values()) {
            savePlayerData(data);
        }
        
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save player data!");
            e.printStackTrace();
        }
    }
    
    private void savePlayerData(PlayerSoulData data) {
        String path = data.getUuid().toString();
        dataConfig.set(path + ".name", data.getPlayerName());
        dataConfig.set(path + ".souls", data.getSouls());
        dataConfig.set(path + ".needsRevival", data.needsRevival());
        dataConfig.set(path + ".lastSeen", data.getLastSeen());
    }
    
    public PlayerSoulData getPlayerData(UUID uuid) {
        return playerData.get(uuid);
    }
    
    public PlayerSoulData getOrCreatePlayerData(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerSoulData data = playerData.get(uuid);
        
        if (data == null) {
            data = new PlayerSoulData(uuid, player.getName(), getStartingSouls(), false, 
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            playerData.put(uuid, data);
            savePlayerData(data);
        } else {
            // Update player name and last seen
            data.setPlayerName(player.getName());
            data.setLastSeen(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        
        return data;
    }
    
    public void addSouls(Player player, int amount) {
        PlayerSoulData data = getOrCreatePlayerData(player);
        int newAmount = Math.min(data.getSouls() + amount, getMaxSouls());
        data.setSouls(newAmount);
        savePlayerData(data);
        
        // Play gain effect
        plugin.getParticleManager().playGainEffect(player);
        
        // Send message
        plugin.getMessageUtil().sendMessage(player, "soul-gained", 
                Map.of("souls", String.valueOf(newAmount)));
    }
    
    public void removeSouls(Player player, int amount) {
        PlayerSoulData data = getOrCreatePlayerData(player);
        int newAmount = Math.max(data.getSouls() - amount, 0);
        data.setSouls(newAmount);
        savePlayerData(data);
        
        // Play lose effect
        plugin.getParticleManager().playLoseEffect(player);
        
        // Check if player has no souls left
        if (newAmount == 0) {
            handleNoSoulsLeft(player, data);
        } else {
            plugin.getMessageUtil().sendMessage(player, "soul-lost", 
                    Map.of("souls", String.valueOf(newAmount)));
        }
    }
    
    public void setSouls(Player player, int amount) {
        PlayerSoulData data = getOrCreatePlayerData(player);
        int clampedAmount = Math.max(0, Math.min(amount, getMaxSouls()));
        data.setSouls(clampedAmount);
        savePlayerData(data);
        
        if (clampedAmount == 0) {
            handleNoSoulsLeft(player, data);
        }
    }
    
    public void setSouls(UUID uuid, int amount) {
        PlayerSoulData data = playerData.get(uuid);
        if (data != null) {
            int clampedAmount = Math.max(0, Math.min(amount, getMaxSouls()));
            data.setSouls(clampedAmount);
            savePlayerData(data);
        }
    }
    
    private void handleNoSoulsLeft(Player player, PlayerSoulData data) {
        String banMode = plugin.getConfigManager().getBanMode();
        
        switch (banMode.toLowerCase()) {
            case "permanent":
                data.setNeedsRevival(true);
                savePlayerData(data);
                Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(), 
                        plugin.getMessageUtil().getMessage("banned-permanent"), 
                        null, null);
                player.kickPlayer(plugin.getMessageUtil().getMessage("banned-permanent"));
                break;
                
            case "temp":
                data.setNeedsRevival(true);
                savePlayerData(data);
                String banTime = plugin.getConfigManager().getBanTime();
                Date banExpiry = parseBanTime(banTime);
                Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(), 
                        plugin.getMessageUtil().getMessage("banned-temporary", 
                                Map.of("time", banTime)), 
                        banExpiry, null);
                player.kickPlayer(plugin.getMessageUtil().getMessage("banned-temporary", 
                        Map.of("time", banTime)));
                break;
                
            case "spectator":
                data.setNeedsRevival(true);
                savePlayerData(data);
                player.setGameMode(GameMode.SPECTATOR);
                plugin.getMessageUtil().sendMessage(player, "spectator-mode");
                break;
        }
    }
    
    private Date parseBanTime(String timeString) {
        // Parse time strings like "7d", "1h", "30m"
        try {
            char unit = timeString.charAt(timeString.length() - 1);
            int amount = Integer.parseInt(timeString.substring(0, timeString.length() - 1));
            
            Calendar cal = Calendar.getInstance();
            switch (unit) {
                case 'd':
                    cal.add(Calendar.DAY_OF_MONTH, amount);
                    break;
                case 'h':
                    cal.add(Calendar.HOUR_OF_DAY, amount);
                    break;
                case 'm':
                    cal.add(Calendar.MINUTE, amount);
                    break;
                default:
                    cal.add(Calendar.DAY_OF_MONTH, 7); // Default to 7 days
            }
            return cal.getTime();
        } catch (Exception e) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 7); // Default to 7 days
            return cal.getTime();
        }
    }
    
    public boolean canRevivePlayer(String playerName) {
        if (!plugin.getConfigManager().isReviveAllowed()) {
            return false;
        }
        
        // Find player data by name
        PlayerSoulData data = findPlayerDataByName(playerName);
        return data != null && data.needsRevival();
    }
    
    public boolean revivePlayer(Player reviver, String targetName) {
        PlayerSoulData data = findPlayerDataByName(targetName);
        if (data == null || !data.needsRevival()) {
            return false;
        }
        
        // Check if reviver has all 3 soul items for the target
        List<ItemStack> soulItems = findSoulItemsInInventory(reviver, targetName);
        if (soulItems.size() < 3) {
            return false;
        }
        
        // Remove the soul items
        for (ItemStack item : soulItems) {
            reviver.getInventory().remove(item);
        }
        
        // Revive the player
        data.setSouls(getMaxSouls());
        data.setNeedsRevival(false);
        savePlayerData(data);
        
        // Unban if necessary
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        if (target.isBanned()) {
            Bukkit.getBanList(BanList.Type.NAME).pardon(targetName);
        }
        
        // If player is online, restore them
        Player onlineTarget = Bukkit.getPlayer(targetName);
        if (onlineTarget != null) {
            onlineTarget.setGameMode(GameMode.SURVIVAL);
            plugin.getParticleManager().playGainEffect(onlineTarget);
            plugin.getMessageUtil().sendMessage(onlineTarget, "revive-success", 
                    Map.of("player", targetName, "souls", String.valueOf(getMaxSouls())));
        }
        
        // Play effect for reviver
        plugin.getParticleManager().playGainEffect(reviver);
        
        return true;
    }
    
    private PlayerSoulData findPlayerDataByName(String name) {
        return playerData.values().stream()
                .filter(data -> data.getPlayerName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
    
    private List<ItemStack> findSoulItemsInInventory(Player player, String targetName) {
        List<ItemStack> soulItems = new ArrayList<>();
        
        for (ItemStack item : player.getInventory().getContents()) {
            if (ItemUtil.isSoulItem(item) && ItemUtil.getSoulOwner(item).equalsIgnoreCase(targetName)) {
                soulItems.add(item);
                if (soulItems.size() >= 3) {
                    break;
                }
            }
        }
        
        return soulItems;
    }
    
    public ItemStack createSoulItem(String ownerName) {
        return ItemUtil.createSoulItem(ownerName, plugin.isGeyserPresent());
    }
    
    public void dropSoulItem(Player player) {
        ItemStack soulItem = createSoulItem(player.getName());
        player.getWorld().dropItemNaturally(player.getLocation(), soulItem);
        plugin.getMessageUtil().sendMessage(player, "soul-dropped");
    }
    
    public boolean withdrawSoul(Player player) {
        PlayerSoulData data = getOrCreatePlayerData(player);
        
        if (data.getSouls() <= 0) {
            plugin.getMessageUtil().sendMessage(player, "no-souls");
            return false;
        }
        
        // Remove one soul
        data.setSouls(data.getSouls() - 1);
        savePlayerData(data);
        
        // Give soul item
        ItemStack soulItem = createSoulItem(player.getName());
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(soulItem);
        } else {
            player.getWorld().dropItemNaturally(player.getLocation(), soulItem);
        }
        
        // Play effect and send message
        plugin.getParticleManager().playLoseEffect(player);
        plugin.getMessageUtil().sendMessage(player, "soul-withdrawn");
        
        return true;
    }
    
    public List<PlayerSoulData> getTopPlayers(int limit) {
        return playerData.values().stream()
                .sorted((a, b) -> Integer.compare(b.getSouls(), a.getSouls()))
                .limit(limit)
                .toList();
    }
    
    // Configuration getters
    public int getStartingSouls() {
        return plugin.getConfigManager().getStartingSouls();
    }
    
    public int getMaxSouls() {
        return plugin.getConfigManager().getMaxSouls();
    }
}