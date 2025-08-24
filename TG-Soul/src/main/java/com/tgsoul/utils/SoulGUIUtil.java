package com.tgsoul.utils;

import com.tgsoul.TGSoulPlugin;
import com.tgsoul.managers.SoulManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for handling soul-related GUIs
 */
public class SoulGUIUtil {
    
    private final TGSoulPlugin plugin;
    
    public SoulGUIUtil(TGSoulPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Opens the Revival Token recipe GUI for a player
     */
    public void openRecipeGUI(Player player, SoulManager soulManager) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&',
                plugin.getMessageUtil().getMessage("recipe-gui-title")));
        
        // Fill with glass panes
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
                    item = soulManager.createSoulItem(player.getName());
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
        
        // Add result item
        String configuredMaterial = plugin.getConfigManager().getRevivalTokenMaterial();
        Material guiMaterial = Material.matchMaterial(configuredMaterial.toUpperCase());
        if (guiMaterial == null) {
            plugin.getLogger().warning("Invalid Revival Token material in config for GUI: " + configuredMaterial + ". Using BEACON.");
            guiMaterial = Material.BEACON;
        }
        ItemStack result = ItemUtil.createRevivalToken(player.getName(), player.getName(), guiMaterial.name());
        gui.setItem(24, result);
        
        // Add info item
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
}