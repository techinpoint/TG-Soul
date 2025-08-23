package com.tgsoul.utils;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundUtil {

    /**
     * Plays a sound for a specific player at their location.
     *
     * @param player The player to play the sound for.
     * @param soundName The name of the sound from config (e.g., "BLOCK_BEACON_ACTIVATE").
     */
    public static void playSound(Player player, String soundName) {
        if (player == null || soundName == null || soundName.isEmpty()) return;
        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);  // Volume 1.0, pitch 1.0
        } catch (IllegalArgumentException e) {
            // Invalid sound name; silently ignore or log if needed
        }
    }

    /**
     * Plays a sound at a specific location for all nearby players.
     *
     * @param location The location to play the sound at.
     * @param soundName The name of the sound from config.
     */
    public static void playSoundAtLocation(Location location, String soundName) {
        if (location == null || location.getWorld() == null || soundName == null || soundName.isEmpty()) return;
        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            location.getWorld().playSound(location, sound, 1.0f, 1.0f);  // Volume 1.0, pitch 1.0
        } catch (IllegalArgumentException e) {
            // Invalid sound name; silently ignore or log if needed
        }
    }
}