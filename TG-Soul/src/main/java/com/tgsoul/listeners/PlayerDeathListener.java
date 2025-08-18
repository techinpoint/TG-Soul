package com.tgsoul.listeners;

import com.tgsoul.TGSoulPlugin;
import com.tgsoul.data.PlayerSoulData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Map;

public class PlayerDeathListener implements Listener {
    
    private final TGSoulPlugin plugin;
    
    public PlayerDeathListener(TGSoulPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        PlayerSoulData data = plugin.getSoulManager().getOrCreatePlayerData(event.getEntity());
        
        // Remove one soul
        plugin.getSoulManager().removeSouls(event.getEntity(), 1);
        
        // Drop soul item at death location
        plugin.getSoulManager().dropSoulItem(event.getEntity());
        
        // Broadcast death message with soul count
        int remainingSouls = data.getSouls() - 1; // -1 because removeSouls hasn't updated data yet
        String deathMessage = plugin.getMessageUtil().getMessage("death-message", 
                Map.of("player", event.getEntity().getName(), "souls", String.valueOf(Math.max(0, remainingSouls))));
        
        // Replace the default death message
        event.setDeathMessage(deathMessage);
    }
}