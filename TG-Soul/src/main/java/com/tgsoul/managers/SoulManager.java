package com.tgsoul.managers;

import com.tgsoul.TGSoulPlugin;
import com.tgsoul.data.PlayerSoulData;
import com.tgsoul.utils.ItemUtil;
import org.bukkit.*;
import com.tgsoul.utils.SoundUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
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
    private final FileConfiguration dataConfig; // Remains final
    private final NamespacedKey recipeKey;

    public SoulManager(TGSoulPlugin plugin) {
        this.plugin = plugin;
        this.playerData = new ConcurrentHashMap<>();
        this.playerCustomModelData = new ConcurrentHashMap<>();
        this.dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        this.dataConfig = YamlConfiguration.loadConfiguration(dataFile); // Initialized once
        this.recipeKey = new NamespacedKey(plugin, "revival_token");
        loadData();
        registerRevivalTokenRecipe();
    }

    public void loadData() {
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create player data file: " + e.getMessage());
                e.printStackTrace();
                return;
            }
        }

        // Load configuration into a temporary variable
        FileConfiguration tempConfig = YamlConfiguration.loadConfiguration(dataFile);

        // Update dataConfig with new content without reassignment
        for (String key : tempConfig.getKeys(true)) {
            dataConfig.set(key, tempConfig.get(key));
        }

        // Load all player data
        for (String uuidString : dataConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidString);
                String playerName = dataConfig.getString(uuidString + ".name", "Unknown");
                int souls = dataConfig.getInt(uuidString + ".souls", getStartingSouls());
                boolean needsRevival = dataConfig.getBoolean(uuidString + ".needsRevival", false);
                String lastSeen = dataConfig.getString(uuidString + ".lastSeen", "Never");
                int customModelData = dataConfig.getInt(uuidString + ".customModelData", 1);

                PlayerSoulData data = new PlayerSoulData(uuid, playerName, souls, needsRevival, lastSeen);
                playerData.put(uuid, data);
                playerCustomModelData.put(uuid, customModelData);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in data file: " + uuidString + " - " + e.getMessage());
            }
        }

        plugin.getLogger().info("Loaded data for " + playerData.size() + " players.");
    }

    public void saveAllData() {
        for (PlayerSoulData data : playerData.values()) {
            savePlayerData(data);
        }
        saveToFile();
    }

    private void savePlayerData(PlayerSoulData data) {
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

    private void saveToFile() {
        FileConfiguration configToSave = dataConfig; // Effectively final copy for lambda
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                configToSave.save(dataFile);
                plugin.getLogger().info("Player data saved to playerdata.yml successfully.");
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save player data to playerdata.yml: " + e.getMessage());
                e.printStackTrace();
                Bukkit.getScheduler().runTask(plugin, () -> {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.hasPermission("tgsoul.admin")) {
                            plugin.getMessageUtil().sendMessage(player, "data-save-failed");
                        }
                    }
                });
            }
        });
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
            plugin.getMessageUtil().sendMessage(player, "soul-lost", Map.of("souls", String.valueOf(newAmount)));
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
        data.setSouls(newAmount);
        savePlayerData(data); // Save after change
        saveToFile(); // Ensure file is updated
        plugin.getParticleManager().playGainEffect(player);
        SoundUtil.playSound(player, plugin.getConfigManager().getGainSound());
        plugin.getMessageUtil().sendMessage(player, "soul-gained", Map.of("souls", String.valueOf(newAmount)));
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
        
        if (playerUUID != null && playerCustomModelData.containsKey(playerUUID)) {
            int customModelData = playerCustomModelData.get(playerUUID);
            return ItemUtil.createSoulItemWithCustomModelData(ownerName, material, customModelData);
        } else {
            return ItemUtil.createSoulItem(ownerName, material);
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
        
        // Create soul item with player's CustomModelData
        String material = plugin.getConfigManager().getSoulMaterial();
        Integer customModelData = playerCustomModelData.get(player.getUniqueId());
        ItemStack soulItem;
        if (customModelData != null) {
            soulItem = ItemUtil.createSoulItemWithCustomModelData(player.getName(), material, customModelData);
        } else {
            soulItem = ItemUtil.createSoulItem(player.getName(), material);
        }
        
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(soulItem);
        } else {
            player.getWorld().dropItemNaturally(player.getLocation(), soulItem);
        }
        plugin.getParticleManager().playLoseEffect(player);
        SoundUtil.playSound(player, plugin.getConfigManager().getWithdrawSound());  // Add this line
        plugin.getMessageUtil().sendMessage(player, "soul-withdrawn");
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
    private void registerRevivalTokenRecipe() {
        try {
            Bukkit.removeRecipe(recipeKey);
        } catch (Exception ignored) {}
        String configuredMaterial = plugin.getConfigManager().getRevivalTokenMaterial();
        Material material = Material.matchMaterial(configuredMaterial.toUpperCase());
        if (material == null) {
            plugin.getLogger().warning("Invalid Revival Token material in config: " + configuredMaterial + ". Using BEACON as fallback.");
            material = Material.BEACON;
        }
        ItemStack result = ItemUtil.createRevivalToken("System", "System", material.name());
        ShapedRecipe recipe = new ShapedRecipe(recipeKey, result);
        recipe.shape("ABC", "DEF", "GHI");
        ConfigurationSection recipeConfig = plugin.getConfig().getConfigurationSection("revival-token.recipe");
        boolean hasIngredients = false;
        if (recipeConfig != null) {
            for (String key : recipeConfig.getKeys(false)) {
                String materialName = recipeConfig.getString(key);
                if (key.length() != 3 || key.charAt(0) != 'a' || !Character.isDigit(key.charAt(1)) || !Character.isDigit(key.charAt(2))) {
                    plugin.getLogger().warning("Invalid recipe key in config: " + key);
                    continue;
                }
                int row = key.charAt(1) - '0';
                int col = key.charAt(2) - '0';
                char recipeChar = (char) ('A' + (row - 1) * 3 + (col - 1));
                if ("SOUL_ITEM".equals(materialName)) {
                    String soulMaterialName = plugin.getConfigManager().getSoulMaterial();
                    try {
                        Material placeholder = Material.valueOf(soulMaterialName.toUpperCase());
                        recipe.setIngredient(recipeChar, placeholder);
                        hasIngredients = true;
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid soul material for placeholder: " + soulMaterialName);
                    }
                    continue;
                }
                try {
                    Material mat = Material.valueOf(materialName.toUpperCase());
                    recipe.setIngredient(recipeChar, mat);
                    hasIngredients = true;
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid material in revival token recipe: " + materialName + " for key " + key);
                }
            }
        } else {
            plugin.getLogger().warning("Missing 'revival-token.recipe' section in config.yml. Recipe registration skipped.");
            return;
        }
        if (!hasIngredients) {
            plugin.getLogger().warning("No valid ingredients found for revival token recipe. Using defaults.");
            recipe.setIngredient('A', Material.NETHERITE_BLOCK);
            recipe.setIngredient('B', Material.NETHER_STAR);
            recipe.setIngredient('C', Material.NETHERITE_BLOCK);
            recipe.setIngredient('D', Material.GHAST_TEAR);
            recipe.setIngredient('E', Material.GHAST_TEAR);
            recipe.setIngredient('F', Material.GHAST_TEAR);
            recipe.setIngredient('G', Material.NETHERITE_BLOCK);
            recipe.setIngredient('H', Material.NETHER_STAR);
            recipe.setIngredient('I', Material.NETHERITE_BLOCK);
        }
        try {
            Bukkit.addRecipe(recipe);
            plugin.getLogger().info("Revival token recipe registered successfully with material: " + material.name());
        } catch (IllegalStateException e) {
            plugin.getLogger().severe("Failed to register revival token recipe: " + e.getMessage());
        }
    }

    public void openRecipeGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&',
                plugin.getMessageUtil().getMessage("recipe-gui-title")));
        ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glassPane.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glassPane.setItemMeta(glassMeta);
        }
        for (int i = 0; i < 54; i++) gui.setItem(i, glassPane);
        // Define the 3x3 crafting grid slots in the GUI
        int[] craftingSlots = {10, 11, 12, 19, 20, 21, 28, 29, 30};

        // Get recipe configuration from the correct path
        ConfigurationSection recipeConfig = plugin.getConfig().getConfigurationSection("revival-token.recipe");
        if (recipeConfig != null) {
            String[] positions = {"a11", "a12", "a13", "a21", "a22", "a23", "a31", "a32", "a33"};
            for (int i = 0; i < positions.length && i < craftingSlots.length; i++) {
                String materialName = recipeConfig.getString(positions[i]);
                if (materialName == null) continue;

                ItemStack item;
                if ("SOUL_ITEM".equals(materialName)) {
                    item = createSoulItem(player.getName());
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        List<String> lore = meta.getLore();
                        if (lore == null) lore = new ArrayList<>();
                        lore.add(ChatColor.YELLOW + "Use YOUR OWN souls here!");
                        meta.setLore(lore);
                        item.setItemMeta(meta);
                    }
                } else {
                    try {
                        Material material = Material.valueOf(materialName);
                        item = new ItemStack(material);
                    } catch (IllegalArgumentException e) {
                        item = new ItemStack(Material.BARRIER);
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            meta.setDisplayName(ChatColor.RED + "Invalid Material: " + materialName);
                            item.setItemMeta(meta);
                        }
                    }
                }
                gui.setItem(craftingSlots[i], item);
            }
        }
        String configuredMaterial = plugin.getConfigManager().getRevivalTokenMaterial();
        Material guiMaterial = Material.matchMaterial(configuredMaterial.toUpperCase());
        if (guiMaterial == null) {
            plugin.getLogger().warning("Invalid Revival Token material in config for GUI: " + configuredMaterial + ". Using BEACON.");
            guiMaterial = Material.BEACON;
        }
        ItemStack result = ItemUtil.createRevivalToken(player.getName(), player.getName(), guiMaterial.name());
        gui.setItem(24, result);
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName(ChatColor.GOLD + "Recipe Information");
            infoMeta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Place these items in a crafting table",
                    ChatColor.GRAY + "to create a Revival Token.",
                    "",
                    ChatColor.YELLOW + "Important:",
                    ChatColor.RED + "You must use YOUR OWN souls!",
                    ChatColor.RED + "Other players' souls won't work!"
            ));
            info.setItemMeta(infoMeta);
        }
        gui.setItem(49, info);
        player.openInventory(gui);
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
            registerRevivalTokenRecipe();
            loadData();
            plugin.getLogger().info("SoulManager reloaded successfully.");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to reload SoulManager: " + e.getMessage());
            e.printStackTrace();
        }
    }
}