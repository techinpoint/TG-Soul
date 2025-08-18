package com.tgsoul.listeners;

import com.tgsoul.TGSoulPlugin;
import com.tgsoul.data.PlayerSoulData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Map;

public class PlayerJoinListener implements Listener {
    
    private final TGSoulPlugin plugin;
    
    public PlayerJoinListener(TGSoulPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Initialize or update player data
        PlayerSoulData data = plugin.getSoulManager().getOrCreatePlayerData(event.getPlayer());
        
        // Welcome message with soul count
        plugin.getMessageUtil().sendMessage(event.getPlayer(), "souls-remaining", 
                Map.of("souls", String.valueOf(data.getSouls())));
    }
}