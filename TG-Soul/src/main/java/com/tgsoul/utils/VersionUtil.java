package com.tgsoul.utils;

import org.bukkit.Bukkit;

public class VersionUtil {
    
    private final String version;
    private final int majorVersion;
    private final int minorVersion;
    
    public VersionUtil() {
        String bukkitVersion = Bukkit.getBukkitVersion();
        this.version = parseVersion(bukkitVersion);
        
        String[] parts = version.split("\\.");
        this.majorVersion = Integer.parseInt(parts[0]);
        this.minorVersion = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
    }
    
    private String parseVersion(String bukkitVersion) {
        // Extract version from strings like "1.21.3-R0.1-SNAPSHOT"
        if (bukkitVersion.contains("-")) {
            return bukkitVersion.split("-")[0];
        }
        return bukkitVersion;
    }
    
    public String getVersion() {
        return version;
    }
    
    public int getMajorVersion() {
        return majorVersion;
    }
    
    public int getMinorVersion() {
        return minorVersion;
    }
    
    public boolean isVersion120() {
        return majorVersion == 1 && minorVersion == 20;
    }
    
    public boolean isVersion121OrHigher() {
        return majorVersion > 1 || (majorVersion == 1 && minorVersion >= 21);
    }
    
    public boolean isVersionSupported() {
        // Support 1.20.x to 1.21.x
        return majorVersion == 1 && minorVersion >= 20 && minorVersion <= 21;
    }
    
    public boolean supportsAdvancedParticles() {
        return isVersion121OrHigher();
    }
}