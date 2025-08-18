package com.tgsoul.listeners;

import com.tgsoul.TGSoulPlugin;
import com.tgsoul.data.PlayerSoulData;
import com.tgsoul.utils.ItemUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class PlayerInteractListener implements Listener {
    
    private final TGSoulPlugin plugin;
    
    public PlayerInteractListener(TGSoulPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        ItemStack item = event.getItem();
        if (!ItemUtil.isSoulItem(item)) {
            return;
        }
        
        String soulOwner = ItemUtil.getSoulOwner(item);
        if (soulOwner == null) {
            return;
        }
        
        // Check if the soul belongs to the player
        if (!soulOwner.equalsIgnoreCase(event.getPlayer().getName())) {
            plugin.getMessageUtil().sendMessage(event.getPlayer(), "not-your-soul");
            event.setCancelled(true);
            return;
        }
        
        // Check if player already has max souls
        PlayerSoulData data = plugin.getSoulManager().getOrCreatePlayerData(event.getPlayer());
        if (data.getSouls() >= plugin.getSoulManager().getMaxSouls()) {
            plugin.getMessageUtil().sendMessage(event.getPlayer(), "max-souls");
            event.setCancelled(true);
            return;
        }
        
        // Consume the soul item
        item.setAmount(item.getAmount() - 1);
        
        // Add soul to player
        plugin.getSoulManager().addSouls(event.getPlayer(), 1);
        plugin.getMessageUtil().sendMessage(event.getPlayer(), "soul-consumed");
        
        event.setCancelled(true);
    }
}