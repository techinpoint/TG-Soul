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
        plugin.getSoulManager().removeSouls(event.getEntity(), 1);
        plugin.getSoulManager().dropSoulItem(event.getEntity());
        
        // Get updated soul count after removal
        PlayerSoulData updatedData = plugin.getSoulManager().getOrCreatePlayerData(event.getEntity());
        int remainingSouls = updatedData.getSouls();
        
        String deathMessage = plugin.getMessageUtil().getMessage("death-message",
                Map.of("player", event.getEntity().getName(), "souls", String.valueOf(remainingSouls)));
        
        // Set death message only once to prevent double messages
        event.setDeathMessage(deathMessage);
    }
}