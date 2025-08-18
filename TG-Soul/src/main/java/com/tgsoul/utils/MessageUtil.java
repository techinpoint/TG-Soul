package com.tgsoul.utils;

import com.tgsoul.TGSoulPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Map;

public class MessageUtil {
    
    private final TGSoulPlugin plugin;
    private FileConfiguration config;
    
    public MessageUtil(TGSoulPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }
    
    public void reloadMessages() {
        this.config = plugin.getConfig();
    }
    
    public String getMessage(String key) {
        String message = config.getString("messages." + key, "&cMessage not found: " + key);
        return colorize(message);
    }
    
    public String getMessage(String key, Map<String, String> placeholders) {
        String message = getMessage(key);
        
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        
        return message;
    }
    
    public void sendMessage(CommandSender sender, String key) {
        String prefix = getMessage("prefix");
        String message = getMessage(key);
        sender.sendMessage(prefix + message);
    }
    
    public void sendMessage(CommandSender sender, String key, Map<String, String> placeholders) {
        String prefix = getMessage("prefix");
        String message = getMessage(key, placeholders);
        sender.sendMessage(prefix + message);
    }
    
    public void sendMessageWithoutPrefix(CommandSender sender, String key) {
        String message = getMessage(key);
        sender.sendMessage(message);
    }
    
    public void sendMessageWithoutPrefix(CommandSender sender, String key, Map<String, String> placeholders) {
        String message = getMessage(key, placeholders);
        sender.sendMessage(message);
    }
    
    public void sendRawMessage(CommandSender sender, String message) {
        sender.sendMessage(colorize(message));
    }
    
    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}