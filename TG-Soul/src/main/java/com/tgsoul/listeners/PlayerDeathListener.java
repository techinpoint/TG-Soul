package com.tgsoul.listeners;

import com.tgsoul.TGSoulPlugin;
import com.tgsoul.data.PlayerSoulData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
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
        Player player = event.getEntity();
        
        // Check if soul should drop based on death cause
        boolean shouldDropSoul = true;
        if (!plugin.getConfigManager().shouldDropOnMobDeath()) {
            Entity killer = player.getKiller();
            if (killer instanceof LivingEntity && !(killer instanceof Player)) {
                shouldDropSoul = false;
            }
        }
        
        // Remove soul first
        plugin.getSoulManager().removeSouls(player, 1);
        
        // Drop soul item if conditions are met
        if (shouldDropSoul) {
            plugin.getSoulManager().dropSoulItem(player);
        }
        
        // Get updated soul count after removal
        PlayerSoulData updatedData = plugin.getSoulManager().getOrCreatePlayerData(player);
        int remainingSouls = updatedData.getSouls();
        
        // Create custom death message
        String deathMessage = plugin.getMessageUtil().getMessage("death-message",
                Map.of("player", player.getName(), "souls", String.valueOf(remainingSouls)));
        
        // Set the death message (this prevents default death message)
        event.setDeathMessage(deathMessage);
    }
}