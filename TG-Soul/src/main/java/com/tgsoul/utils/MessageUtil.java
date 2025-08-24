package com.tgsoul.utils;

import com.tgsoul.TGSoulPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Map;

public class MessageUtil {
    private final TGSoulPlugin plugin;
    private FileConfiguration messages;

    public MessageUtil(TGSoulPlugin plugin) {
        this.plugin = plugin;
        reloadMessages();
    }

    public void reloadMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        this.messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public String getMessage(String key) {
        String message = messages.getString(key, "&cMessage not found: " + key);
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