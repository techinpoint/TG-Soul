package com.tgsoul.listeners;

import com.tgsoul.TGSoulPlugin;
import com.tgsoul.data.PlayerSoulData;
import com.tgsoul.utils.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
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
            plugin.getMessageUtil().sendMessage(event.getPlayer(), "wrong-soul-owner");
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

        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();

        if (ItemUtil.isRevivalToken(item)) {
            String tokenOwner = ItemUtil.getRevivalTokenOwner(item);
            String targetPlayer = ItemUtil.getRevivalTokenTarget(item);

            if (targetPlayer != null) {
                final Block block = event.getBlockPlaced(); // Already final
                String configuredMaterial = plugin.getConfigManager().getRevivalTokenMaterial();
                Material tempMaterial = Material.matchMaterial(configuredMaterial.toUpperCase());
                if (tempMaterial == null || block.getType() != tempMaterial) {
                    plugin.getLogger().warning("Invalid Revival Token material in config: " + configuredMaterial + ". Using BEACON as fallback.");
                    tempMaterial = Material.BEACON;
                }
                final Material material = tempMaterial; // Final variable for lambda

                // Attempt to revive the target player at this location
                if (plugin.getSoulManager().revivePlayerAtLocation(tokenOwner, targetPlayer, block.getLocation())) {
                    // Consume the revival token from the player's inventory
                    ItemStack handItem = event.getItemInHand();
                    if (handItem.getAmount() > 1) {
                        handItem.setAmount(handItem.getAmount() - 1);
                    } else {
                        event.getPlayer().getInventory().setItem(event.getHand(), null);
                    }

                    plugin.getMessageUtil().sendMessage(event.getPlayer(), "revival-token-placed",
                            Map.of("player", targetPlayer));

                    // Schedule block removal after a short delay
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (block.getType() == material) { // Use final block and material
                            block.setType(Material.AIR); // Remove the block
                        }
                    }, 1L);
                } else {
                    plugin.getMessageUtil().sendMessage(event.getPlayer(), "revive-failed",
                            Map.of("player", targetPlayer));
                    event.setCancelled(true);
                }
            } else {
                plugin.getMessageUtil().sendMessage(event.getPlayer(), "revival-token-no-target");
                event.setCancelled(true);
            }
        }
    }
}