package com.tgsoul.listeners;

import com.tgsoul.TGSoulPlugin;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public class GUIListener implements Listener {
    
    private final TGSoulPlugin plugin;
    
    public GUIListener(TGSoulPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = ChatColor.stripColor(event.getView().getTitle());
        
        if (title.equals(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', 
                plugin.getMessageUtil().getMessage("recipe-gui-title"))))) {
            
            // Cancel all clicks in the recipe GUI
            event.setCancelled(true);
            
            // Close the inventory immediately when clicked
            event.getWhoClicked().closeInventory();
        }
    }
    
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        String title = ChatColor.stripColor(event.getView().getTitle());
        
        if (title.equals(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', 
                plugin.getMessageUtil().getMessage("recipe-gui-title"))))) {
            
            // Cancel all drags in the recipe GUI
            event.setCancelled(true);
        }
    }
}