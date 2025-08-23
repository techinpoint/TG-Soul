package com.tgsoul.utils;

import com.tgsoul.TGSoulPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ItemUtil {
    
    private static final String SOUL_ITEM_KEY = "tgsoul_owner";
    private static final String SOUL_ITEM_TYPE = "tgsoul_item";
    private static final String REVIVAL_TOKEN_KEY = "tgsoul_revival_token";
    private static final String REVIVAL_TARGET_KEY = "tgsoul_revival_target";
    private static final String CUSTOM_MODEL_DATA_KEY = "tgsoul_custom_model_data";
    
    public static ItemStack createSoulItem(String ownerName, String materialName) {
        Material material;
        try {
            material = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            material = Material.GHAST_TEAR; // Default fallback
        }
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // Set display name
            meta.setDisplayName(ChatColor.GOLD + ownerName + " Soul");
            
            // Set lore
            List<String> lore = Arrays.asList(
                    ChatColor.GRAY + "A soul belonging to " + ChatColor.WHITE + ownerName,
                    ChatColor.GRAY + "Right-click to consume (if yours)",
                    ChatColor.DARK_GRAY + "Use 3 of these in Revival Token recipe"
            );
            meta.setLore(lore);
            
            // Set persistent data
            NamespacedKey ownerKey = new NamespacedKey("tgsoul", SOUL_ITEM_KEY);
            NamespacedKey typeKey = new NamespacedKey("tgsoul", SOUL_ITEM_TYPE);
            
            meta.getPersistentDataContainer().set(ownerKey, PersistentDataType.STRING, ownerName);
            meta.getPersistentDataContainer().set(typeKey, PersistentDataType.STRING, "soul");
            
            // Check if resource pack is set and apply CustomModelData
            if (!Bukkit.getServer().getResourcePack().isEmpty()) {
                Random random = new Random();
                int customModelData = random.nextInt(10) + 1; // 1-10
                meta.setCustomModelData(customModelData);
                
                // Store CustomModelData in persistent data for consistency
                NamespacedKey cmdKey = new NamespacedKey("tgsoul", CUSTOM_MODEL_DATA_KEY);
                meta.getPersistentDataContainer().set(cmdKey, PersistentDataType.INTEGER, customModelData);
            }
            
            // Add enchantment glow effect
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            
            item.setItemMeta(meta);
        }
        
        return item;
    }

    public static ItemStack createSoulItemWithCustomModelData(String ownerName, String materialName, int customModelData) {
        Material material;
        try {
            material = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            material = Material.GHAST_TEAR; // Default fallback
        }
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // Set display name
            meta.setDisplayName(ChatColor.GOLD + ownerName + " Soul");
            
            // Set lore
            List<String> lore = Arrays.asList(
                    ChatColor.GRAY + "A soul belonging to " + ChatColor.WHITE + ownerName,
                    ChatColor.GRAY + "Right-click to consume (if yours)",
                    ChatColor.DARK_GRAY + "Use 3 of these in Revival Token recipe"
            );
            meta.setLore(lore);
            
            // Set persistent data
            NamespacedKey ownerKey = new NamespacedKey("tgsoul", SOUL_ITEM_KEY);
            NamespacedKey typeKey = new NamespacedKey("tgsoul", SOUL_ITEM_TYPE);
            NamespacedKey cmdKey = new NamespacedKey("tgsoul", CUSTOM_MODEL_DATA_KEY);
            
            meta.getPersistentDataContainer().set(ownerKey, PersistentDataType.STRING, ownerName);
            meta.getPersistentDataContainer().set(typeKey, PersistentDataType.STRING, "soul");
            meta.getPersistentDataContainer().set(cmdKey, PersistentDataType.INTEGER, customModelData);
            
            // Apply CustomModelData if resource pack is present
            if (!Bukkit.getServer().getResourcePack().isEmpty()) {
                meta.setCustomModelData(customModelData);
            }
            
            // Add enchantment glow effect
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            
            item.setItemMeta(meta);
        }
        
        return item;
    }

    public static ItemStack createRevivalToken(String ownerName, String targetName, String materialName) {
        Material material;
        try {
            material = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            material = Material.BEACON; // Default fallback
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "Revival Token");
            List<String> lore = Arrays.asList(
                    ChatColor.GRAY + "For reviving " + ChatColor.WHITE + targetName,
                    ChatColor.DARK_GRAY + "Place to use"
            );
            meta.setLore(lore);

            NamespacedKey ownerKey = new NamespacedKey("tgsoul", SOUL_ITEM_KEY);
            NamespacedKey tokenKey = new NamespacedKey("tgsoul", REVIVAL_TOKEN_KEY);
            NamespacedKey targetKey = new NamespacedKey("tgsoul", REVIVAL_TARGET_KEY);

            meta.getPersistentDataContainer().set(ownerKey, PersistentDataType.STRING, ownerName);
            meta.getPersistentDataContainer().set(tokenKey, PersistentDataType.STRING, "token");
            meta.getPersistentDataContainer().set(targetKey, PersistentDataType.STRING, targetName);

            Enchantment unbreaking = Registry.ENCHANTMENT.get(NamespacedKey.minecraft("unbreaking"));
            if (unbreaking != null) {
                meta.addEnchant(unbreaking, 1, true);
            }
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

            item.setItemMeta(meta);
        }

        return item;
    }
    
    public static boolean isSoulItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        NamespacedKey typeKey = new NamespacedKey("tgsoul", SOUL_ITEM_TYPE);
        
        return meta.getPersistentDataContainer().has(typeKey, PersistentDataType.STRING);
    }
    
    public static boolean isRevivalToken(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        NamespacedKey typeKey = new NamespacedKey("tgsoul", REVIVAL_TOKEN_KEY);
        
        return meta.getPersistentDataContainer().has(typeKey, PersistentDataType.STRING);
    }
    
    public static String getSoulOwner(ItemStack item) {
        if (!isSoulItem(item)) {
            return null;
        }
        
        ItemMeta meta = item.getItemMeta();
        NamespacedKey ownerKey = new NamespacedKey("tgsoul", SOUL_ITEM_KEY);
        
        return meta.getPersistentDataContainer().get(ownerKey, PersistentDataType.STRING);
    }
    
    public static Integer getSoulCustomModelData(ItemStack item) {
        if (!isSoulItem(item)) {
            return null;
        }
        
        ItemMeta meta = item.getItemMeta();
        NamespacedKey cmdKey = new NamespacedKey("tgsoul", CUSTOM_MODEL_DATA_KEY);
        
        return meta.getPersistentDataContainer().get(cmdKey, PersistentDataType.INTEGER);
    }
    
    public static String getRevivalTokenOwner(ItemStack item) {
        if (!isRevivalToken(item)) {
            return null;
        }
        
        ItemMeta meta = item.getItemMeta();
        NamespacedKey ownerKey = new NamespacedKey("tgsoul", SOUL_ITEM_KEY);
        
        return meta.getPersistentDataContainer().get(ownerKey, PersistentDataType.STRING);
    }
    
    public static String getRevivalTokenTarget(ItemStack item) {
        if (!isRevivalToken(item)) {
            return null;
        }
        
        ItemMeta meta = item.getItemMeta();
        NamespacedKey targetKey = new NamespacedKey("tgsoul", REVIVAL_TARGET_KEY);
        
        return meta.getPersistentDataContainer().get(targetKey, PersistentDataType.STRING);
    }
    
    public static boolean isOwnedBy(ItemStack item, String playerName) {
        String owner = getSoulOwner(item);
        return owner != null && owner.equalsIgnoreCase(playerName);
    }
}