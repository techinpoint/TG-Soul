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
        if (!plugin.getConfigManager().getConfig().getBoolean("soul.drop-on-mob-death", false)) {
            Entity killer = player.getKiller();
            if (killer instanceof LivingEntity && !(killer instanceof Player)) {
                shouldDropSoul = false;
            }
        }
        
        PlayerSoulData data = plugin.getSoulManager().getOrCreatePlayerData(event.getEntity());
        plugin.getSoulManager().removeSouls(event.getEntity(), 1);
        
        if (shouldDropSoul) {
            plugin.getSoulManager().dropSoulItem(event.getEntity());
        }
        
        // Get updated soul count after removal
        PlayerSoulData updatedData = plugin.getSoulManager().getOrCreatePlayerData(event.getEntity());
        int remainingSouls = updatedData.getSouls();
        
        String deathMessage = plugin.getMessageUtil().getMessage("death-message",
                Map.of("player", event.getEntity().getName(), "souls", String.valueOf(remainingSouls)));
        
        // Set death message to prevent double messages
        event.setDeathMessage(deathMessage);
    }
}