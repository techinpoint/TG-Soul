package com.tgsoul.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;

public class ItemUtil {
    
    private static final String SOUL_ITEM_KEY = "tgsoul_owner";
    private static final String SOUL_ITEM_TYPE = "tgsoul_item";
    
    public static ItemStack createSoulItem(String ownerName, boolean isGeyserPresent) {
        Material material = isGeyserPresent ? Material.PAPER : Material.PAPER;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // Set display name
            meta.setDisplayName(ChatColor.GOLD + ownerName + "'s Soul");
            
            // Set lore
            List<String> lore = Arrays.asList(
                    ChatColor.GRAY + "A soul belonging to " + ChatColor.WHITE + ownerName,
                    ChatColor.GRAY + "Right-click to consume (if yours)",
                    ChatColor.DARK_GRAY + "Collect 3 souls to revive " + ownerName
            );
            meta.setLore(lore);
            
            // Set persistent data
            NamespacedKey ownerKey = new NamespacedKey("tgsoul", SOUL_ITEM_KEY);
            NamespacedKey typeKey = new NamespacedKey("tgsoul", SOUL_ITEM_TYPE);
            
            meta.getPersistentDataContainer().set(ownerKey, PersistentDataType.STRING, ownerName);
            meta.getPersistentDataContainer().set(typeKey, PersistentDataType.STRING, "soul");
            
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
    
    public static String getSoulOwner(ItemStack item) {
        if (!isSoulItem(item)) {
            return null;
        }
        
        ItemMeta meta = item.getItemMeta();
        NamespacedKey ownerKey = new NamespacedKey("tgsoul", SOUL_ITEM_KEY);
        
        return meta.getPersistentDataContainer().get(ownerKey, PersistentDataType.STRING);
    }
    
    public static boolean isOwnedBy(ItemStack item, String playerName) {
        String owner = getSoulOwner(item);
        return owner != null && owner.equalsIgnoreCase(playerName);
    }
}