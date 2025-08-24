package com.tgsoul.utils;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundUtil {

    /**
     * Plays a sound for a specific player at their location.
     * Compatible with Paper 1.21.1 API
     *
     * @param player The player to play the sound for.
     * @param soundName The name of the sound from config (e.g., "BLOCK_BEACON_ACTIVATE").
     */
    public static void playSound(Player player, String soundName) {
        if (player == null || soundName == null || soundName.isEmpty()) return;
        
        try {
            // Use Sound.sound() method for modern Paper API
            Sound sound = Sound.sound(org.bukkit.NamespacedKey.minecraft(soundName.toLowerCase()));
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } catch (Exception e) {
            // Fallback to legacy enum method if modern API fails
            try {
                Sound legacySound = Sound.valueOf(soundName.toUpperCase());
                player.playSound(player.getLocation(), legacySound, 1.0f, 1.0f);
            } catch (IllegalArgumentException ex) {
                // Invalid sound name; silently ignore
            }
        }
    }

    /**
     * Plays a sound at a specific location for all nearby players.
     * Compatible with Paper 1.21.1 API
     *
     * @param location The location to play the sound at.
     * @param soundName The name of the sound from config.
     */
    public static void playSoundAtLocation(Location location, String soundName) {
        if (location == null || location.getWorld() == null || soundName == null || soundName.isEmpty()) return;
        
        try {
            // Use Sound.sound() method for modern Paper API
            Sound sound = Sound.sound(org.bukkit.NamespacedKey.minecraft(soundName.toLowerCase()));
            location.getWorld().playSound(location, sound, 1.0f, 1.0f);
        } catch (Exception e) {
            // Fallback to legacy enum method if modern API fails
            try {
                Sound legacySound = Sound.valueOf(soundName.toUpperCase());
                location.getWorld().playSound(location, legacySound, 1.0f, 1.0f);
            } catch (IllegalArgumentException ex) {
                // Invalid sound name; silently ignore
            }
        }
    }
}