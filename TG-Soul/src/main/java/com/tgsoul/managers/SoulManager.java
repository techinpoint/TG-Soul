package com.tgsoul.managers;

import com.tgsoul.TGSoulPlugin;
import com.tgsoul.data.PlayerSoulData;
import com.tgsoul.utils.ItemUtil;
import com.tgsoul.utils.SoulDataUtil;
import com.tgsoul.utils.SoulGUIUtil;
import org.bukkit.*;
import com.tgsoul.utils.SoundUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.BanList;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SoulManager {

    private final TGSoulPlugin plugin;
    private final Map<UUID, PlayerSoulData> playerData;
    private final Map<UUID, Integer> playerCustomModelData;
    private final File dataFile;
    private FileConfiguration dataConfig;
    private final SoulDataUtil dataUtil;
    private final SoulGUIUtil guiUtil;

    public SoulManager(TGSoulPlugin plugin) {
        this.plugin = plugin;
        this.playerData = new ConcurrentHashMap<>();
        this.playerCustomModelData = new ConcurrentHashMap<>();
        this.dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        this.dataUtil = new SoulDataUtil(this, plugin);
        this.guiUtil = new SoulGUIUtil(plugin);
        initializeDataConfig();
        loadData();
    }

    private void initializeDataConfig() {
        this.dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void loadData() {
        dataUtil.loadPlayerData(dataFile, dataConfig, playerData, playerCustomModelData);
    }

    public void saveAllData() {
        dataUtil.saveAllPlayerData(playerData, playerCustomModelData, dataConfig, dataFile);
    }

    private void savePlayerData(PlayerSoulData data) {
        dataUtil.savePlayerData(data, playerCustomModelData, dataConfig);
    }

    private void saveToFile() {
        dataUtil.saveToFile(dataConfig, dataFile);
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
            
            // Assign CustomModelData for new players
            if (!playerCustomModelData.containsKey(uuid)) {
                int defaultCmd = plugin.getConfigManager().getDefaultCustomModelData();
                playerCustomModelData.put(uuid, defaultCmd);
            }
            
            savePlayerData(data); // Save immediately on creation
            saveToFile(); // Ensure file is updated
        } else {
            // Update player name and last seen
            data.setPlayerName(player.getName());
            data.setLastSeen(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            savePlayerData(data); // Save updates
            saveToFile(); // Ensure file is updated
        }

        return data;
    }

    public void removeSouls(Player player, int amount) {
        PlayerSoulData data = getOrCreatePlayerData(player);
        int newAmount = Math.max(data.getSouls() - amount, 0);
        data.setSouls(newAmount);
        savePlayerData(data); // Save after change
        saveToFile(); // Ensure file is updated
        plugin.getParticleManager().playLoseEffect(player);
        if (newAmount == 0) {
            handleNoSoulsLeft(player, data);
        } else {
            // Only send message if not handling death (to prevent double messages)
            if (player.getHealth() > 0) {
                plugin.getMessageUtil().sendMessage(player, "soul-lost", Map.of("souls", String.valueOf(newAmount)));
            }
        }
    }

    public void setSouls(Player player, int amount) {
        PlayerSoulData data = getOrCreatePlayerData(player);
        int clampedAmount = Math.max(0, Math.min(amount, getMaxSouls()));
        data.setSouls(clampedAmount);
        savePlayerData(data); // Save after change
        saveToFile(); // Ensure file is updated
        if (clampedAmount == 0) {
            handleNoSoulsLeft(player, data);
        }
    }

    public void setSouls(UUID uuid, int amount) {
        PlayerSoulData data = playerData.get(uuid);
        if (data != null) {
            int clampedAmount = Math.max(0, Math.min(amount, getMaxSouls()));
            data.setSouls(clampedAmount);
            savePlayerData(data); // Save after change
            saveToFile(); // Ensure file is updated
        }
    }

    private void handleNoSoulsLeft(Player player, PlayerSoulData data) {
        String banMode = plugin.getConfigManager().getBanMode();
        switch (banMode.toLowerCase()) {
            case "permanent":
                data.setNeedsRevival(true);
                savePlayerData(data); // Save after change
                saveToFile(); // Ensure file is updated
                Bukkit.getScheduler().runTask(plugin, () -> {
                    Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(),
                            plugin.getMessageUtil().getMessage("banned-permanent"), null, null);
                    if (player.isOnline()) {
                        player.kickPlayer(plugin.getMessageUtil().getMessage("banned-permanent"));
                    }
                });
                break;
            case "temp":
                data.setNeedsRevival(true);
                savePlayerData(data); // Save after change
                saveToFile(); // Ensure file is updated
                String banTime = plugin.getConfigManager().getBanTime();
                Date banExpiry = parseBanTime(banTime);
                Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(),
                        plugin.getMessageUtil().getMessage("banned-temporary", Map.of("time", banTime)),
                        banExpiry, null);
                player.kickPlayer(plugin.getMessageUtil().getMessage("banned-temporary", Map.of("time", banTime)));
                break;
            case "spectator":
                data.setNeedsRevival(true);
                savePlayerData(data); // Save after change
                saveToFile(); // Ensure file is updated
                player.setGameMode(GameMode.SPECTATOR);
                plugin.getMessageUtil().sendMessage(player, "spectator-mode");
                break;
        }
    }

    private Date parseBanTime(String timeString) {
        try {
            char unit = timeString.charAt(timeString.length() - 1);
            int amount = Integer.parseInt(timeString.substring(0, timeString.length() - 1));
            Calendar cal = Calendar.getInstance();
            switch (unit) {
                case 'd': cal.add(Calendar.DAY_OF_MONTH, amount); break;
                case 'h': cal.add(Calendar.HOUR_OF_DAY, amount); break;
                case 'm': cal.add(Calendar.MINUTE, amount); break;
                default: cal.add(Calendar.DAY_OF_MONTH, 7); // Default to 7 days
            }
            return cal.getTime();
        } catch (Exception e) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 7); // Default to 7 days
            return cal.getTime();
        }
    }

    public void addSouls(Player player, int amount) {
        PlayerSoulData data = getOrCreatePlayerData(player);
        int newAmount = Math.min(data.getSouls() + amount, getMaxSouls());
        int oldAmount = data.getSouls();
        data.setSouls(newAmount);
        savePlayerData(data); // Save after change
        saveToFile(); // Ensure file is updated
        plugin.getParticleManager().playGainEffect(player);
        SoundUtil.playSound(player, plugin.getConfigManager().getGainSound());
        
        // Only send message if souls actually increased
        if (newAmount > oldAmount) {
            plugin.getMessageUtil().sendMessage(player, "soul-gained", Map.of("souls", String.valueOf(newAmount)));
        }
    }

    public boolean canRevivePlayer(String playerName) {
        if (!plugin.getConfigManager().isReviveAllowed()) return false;
        PlayerSoulData data = findPlayerDataByName(playerName);
        return data != null && data.needsRevival();
    }

    public boolean revivePlayer(Player reviver, String targetName) {
        PlayerSoulData data = findPlayerDataByName(targetName);
        if (data == null || !data.needsRevival()) return false;
        List<ItemStack> soulItems = findSoulItemsInInventory(reviver, targetName);
        if (soulItems.size() < 3) return false;
        for (ItemStack item : soulItems) reviver.getInventory().remove(item);
        data.setSouls(getMaxSouls());
        data.setNeedsRevival(false);
        savePlayerData(data); // Save after change
        saveToFile(); // Ensure file is updated
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        if (target.isBanned()) Bukkit.getBanList(BanList.Type.NAME).pardon(targetName);
        Player onlineTarget = Bukkit.getPlayer(targetName);
        if (onlineTarget != null) {
            onlineTarget.setGameMode(GameMode.SURVIVAL);
            plugin.getParticleManager().playGainEffect(onlineTarget);
            SoundUtil.playSound(onlineTarget, plugin.getConfigManager().getRevivalSound());
            plugin.getMessageUtil().sendMessage(onlineTarget, "revive-success",
                    Map.of("player", targetName, "souls", String.valueOf(getMaxSouls())));
        }
        plugin.getParticleManager().playGainEffect(reviver);
        return true;
    }

    private PlayerSoulData findPlayerDataByName(String name) {
        return playerData.values().stream()
                .filter(data -> data.getPlayerName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }

    private List<ItemStack> findSoulItemsInInventory(Player player, String targetName) {
        List<ItemStack> soulItems = new ArrayList<>();
        for (ItemStack item : player.getInventory().getContents()) {
            if (ItemUtil.isSoulItem(item) && ItemUtil.getSoulOwner(item).equalsIgnoreCase(targetName)) {
                soulItems.add(item);
                if (soulItems.size() >= 3) break;
            }
        }
        return soulItems;
    }

    public ItemStack createSoulItem(String ownerName) {
        String material = plugin.getConfigManager().getSoulMaterial();
        
        // Find the player's UUID to get their CustomModelData
        UUID playerUUID = null;
        for (Map.Entry<UUID, PlayerSoulData> entry : playerData.entrySet()) {
            if (entry.getValue().getPlayerName().equalsIgnoreCase(ownerName)) {
                playerUUID = entry.getKey();
                break;
            }
        }
        
        // Get CustomModelData for the player
        if (playerUUID != null && playerCustomModelData.containsKey(playerUUID)) {
            int customModelData = playerCustomModelData.get(playerUUID);
            return ItemUtil.createSoulItem(ownerName, material, customModelData);
        } else {
            // Use default CustomModelData
            int defaultCmd = plugin.getConfigManager().getDefaultCustomModelData();
            return ItemUtil.createSoulItem(ownerName, material, defaultCmd);
        }
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
        data.setSouls(data.getSouls() - 1);
        savePlayerData(data); // Save after change
        saveToFile(); // Ensure file is updated
        
        // Create soul item with the player's CustomModelData
        String material = plugin.getConfigManager().getSoulMaterial();
        Integer customModelData = playerCustomModelData.get(player.getUniqueId());
        if (customModelData == null) {
            customModelData = plugin.getConfigManager().getDefaultCustomModelData();
        }
        
        ItemStack soulItem = ItemUtil.createSoulItem(player.getName(), material, customModelData);
        
        // Give the soul item to the player
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(soulItem);
        } else {
            player.getWorld().dropItemNaturally(player.getLocation(), soulItem);
        }
        
        plugin.getParticleManager().playLoseEffect(player);
        SoundUtil.playSound(player, plugin.getConfigManager().getWithdrawSound());
        plugin.getMessageUtil().sendMessage(player, "soul-withdrawn", 
                Map.of("souls", String.valueOf(data.getSouls())));
        return true;
    }

    public List<PlayerSoulData> getTopPlayers(int limit) {
        return playerData.values().stream()
                .sorted((a, b) -> Integer.compare(b.getSouls(), a.getSouls()))
                .limit(limit).toList();
    }

    public int getStartingSouls() {
        return plugin.getConfigManager().getStartingSouls();
    }

    public int getMaxSouls() {
        return plugin.getConfigManager().getMaxSouls();
    }

    public void setPlayerCustomModelData(UUID uuid, int customModelData) {
        playerCustomModelData.put(uuid, customModelData);
        
        // Save to file immediately
        PlayerSoulData data = playerData.get(uuid);
        if (data != null) {
            savePlayerData(data);
            saveToFile();
        }
    }

    public Integer getPlayerCustomModelData(UUID uuid) {
        return playerCustomModelData.get(uuid);
    }

    public int getPlayerCustomModelDataByName(String playerName) {
        for (Map.Entry<UUID, PlayerSoulData> entry : playerData.entrySet()) {
            if (entry.getValue().getPlayerName().equalsIgnoreCase(playerName)) {
                Integer cmd = playerCustomModelData.get(entry.getKey());
                return cmd != null ? cmd : plugin.getConfigManager().getDefaultCustomModelData();
            }
        }
        return plugin.getConfigManager().getDefaultCustomModelData();
    }

    public void openRecipeGUI(Player player) {
        guiUtil.openRecipeGUI(player, this);
    }

    public boolean unbanPlayer(String playerName) {
        PlayerSoulData data = findPlayerDataByName(playerName);
        if (data == null) {
            plugin.getLogger().warning("No data found for player: " + playerName);
            return false;
        }
        data.setSouls(getMaxSouls());
        data.setNeedsRevival(false);
        savePlayerData(data); // Save after change
        saveToFile(); // Ensure file is updated
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        if (target.isBanned()) Bukkit.getBanList(BanList.Type.NAME).pardon(playerName);
        Player onlineTarget = Bukkit.getPlayer(playerName);
        if (onlineTarget != null) {
            onlineTarget.setGameMode(GameMode.SURVIVAL);
            plugin.getParticleManager().playGainEffect(onlineTarget);
            SoundUtil.playSound(onlineTarget, plugin.getConfigManager().getRevivalSound());
            plugin.getMessageUtil().sendMessage(onlineTarget, "revive-success",
                    Map.of("player", playerName, "souls", String.valueOf(getMaxSouls())));
        } else {
            plugin.getLogger().info("Player " + playerName + " is offline, data updated but no effects applied.");
        }
        return true;
    }

    public boolean revivePlayerAtLocation(String reviverName, String targetName, Location revivalLocation) {
        PlayerSoulData data = findPlayerDataByName(targetName);
        if (data == null || !data.needsRevival()) {
            plugin.getLogger().warning("Cannot revive " + targetName + ": Data not found or not needing revival.");
            return false;
        }
        data.setSouls(getMaxSouls());
        data.setNeedsRevival(false);
        savePlayerData(data); // Save after change
        saveToFile(); // Ensure file is updated
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        if (target.isBanned()) Bukkit.getBanList(BanList.Type.NAME).pardon(targetName);
        Player onlineTarget = Bukkit.getPlayer(targetName);
        if (onlineTarget != null) {
            onlineTarget.teleport(revivalLocation.add(0, 1, 0));
            onlineTarget.setGameMode(GameMode.SURVIVAL);
            plugin.getParticleManager().playGainEffect(onlineTarget);
            SoundUtil.playSound(onlineTarget, plugin.getConfigManager().getRevivalSound());
            plugin.getMessageUtil().sendMessage(onlineTarget, "revival-token-used", Map.of("reviver", reviverName));
        } else {
            plugin.getLogger().info("Player " + targetName + " is offline, revival data updated but no teleport/effects applied.");
        }
        SoundUtil.playSoundAtLocation(revivalLocation, plugin.getConfigManager().getRevivalSound());
        return true;
    }

    public void reload() {
        try {
            saveAllData(); // Save current data before reloading
            loadData();
            plugin.getLogger().info("SoulManager reloaded successfully.");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to reload SoulManager: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Getters for utility classes
    public Map<UUID, PlayerSoulData> getPlayerDataMap() {
        return playerData;
    }

    public Map<UUID, Integer> getPlayerCustomModelDataMap() {
        return playerCustomModelData;
    }
}