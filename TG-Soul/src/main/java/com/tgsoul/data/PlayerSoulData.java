package com.tgsoul.data;

import java.util.UUID;

public class PlayerSoulData {
    
    private final UUID uuid;
    private String playerName;
    private int souls;
    private boolean needsRevival;
    private String lastSeen;
    
    public PlayerSoulData(UUID uuid, String playerName, int souls, boolean needsRevival, String lastSeen) {
        this.uuid = uuid;
        this.playerName = playerName;
        this.souls = souls;
        this.needsRevival = needsRevival;
        this.lastSeen = lastSeen;
    }
    
    // Getters
    public UUID getUuid() {
        return uuid;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public int getSouls() {
        return souls;
    }
    
    public boolean needsRevival() {
        return needsRevival;
    }
    
    public String getLastSeen() {
        return lastSeen;
    }
    
    // Setters
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
    
    public void setSouls(int souls) {
        this.souls = souls;
    }
    
    public void setNeedsRevival(boolean needsRevival) {
        this.needsRevival = needsRevival;
    }
    
    public void setLastSeen(String lastSeen) {
        this.lastSeen = lastSeen;
    }
    
    @Override
    public String toString() {
        return "PlayerSoulData{" +
                "uuid=" + uuid +
                ", playerName='" + playerName + '\'' +
                ", souls=" + souls +
                ", needsRevival=" + needsRevival +
                ", lastSeen='" + lastSeen + '\'' +
                '}';
    }
}