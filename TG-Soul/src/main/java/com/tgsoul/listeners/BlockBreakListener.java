package com.tgsoul.listeners;

import com.tgsoul.TGSoulPlugin;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {
    
    private final TGSoulPlugin plugin;
    
    public BlockBreakListener(TGSoulPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.BEACON) {
            // Revival tokens are consumed when placed, so no special handling needed
            // This listener can be used for other block-related functionality if needed
        }
    }
}