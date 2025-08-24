package com.tgsoul.managers;

import com.tgsoul.TGSoulPlugin;
import com.tgsoul.utils.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import java.util.List;

/**
 * Manages recipe registration and validation for TGSoul
 */
public class RecipeManager {
    
    private final TGSoulPlugin plugin;
    private final NamespacedKey revivalTokenRecipeKey;
    
    public RecipeManager(TGSoulPlugin plugin) {
        this.plugin = plugin;
        this.revivalTokenRecipeKey = new NamespacedKey(plugin, "revival_token");
    }
    
    /**
     * Registers all recipes based on configuration
     */
    public void registerRecipes() {
        registerRevivalTokenRecipe();
    }
    
    /**
     * Registers the Revival Token recipe if enabled in config
     */
    private void registerRevivalTokenRecipe() {
        // Check if revival token is enabled
        if (!plugin.getConfigManager().isRevivalTokenEnabled()) {
            plugin.getLogger().info("Revival Token recipe disabled in config.");
            return;
        }
        
        try {
            // Remove existing recipe if it exists
            Bukkit.removeRecipe(revivalTokenRecipeKey);
        } catch (Exception ignored) {
            // Recipe doesn't exist, continue
        }
        
        // Get configured material for revival token
        String configuredMaterial = plugin.getConfigManager().getRevivalTokenMaterial();
        Material material = Material.matchMaterial(configuredMaterial.toUpperCase());
        if (material == null) {
            plugin.getLogger().warning("Invalid Revival Token material in config: " + configuredMaterial + ". Using BEACON as fallback.");
            material = Material.BEACON;
        }
        
        // Create result item
        ItemStack result = ItemUtil.createRevivalToken("System", "System", material.name());
        ShapedRecipe recipe = new ShapedRecipe(revivalTokenRecipeKey, result);
        
        // Get recipe pattern from config
        List<String> recipePattern = plugin.getConfigManager().getRevivalTokenRecipePattern();
        if (recipePattern == null || recipePattern.size() != 3) {
            plugin.getLogger().warning("Invalid recipe pattern in config. Using default pattern.");
            recipe.shape("ABC", "DEF", "GHI");
            setDefaultRecipeIngredients(recipe);
        } else {
            // Set pattern from config
            recipe.shape(recipePattern.get(0), recipePattern.get(1), recipePattern.get(2));
            setRecipeIngredientsFromConfig(recipe);
        }
        
        try {
            Bukkit.addRecipe(recipe);
            plugin.getLogger().info("Revival token recipe registered successfully with material: " + material.name());
        } catch (IllegalStateException e) {
            plugin.getLogger().severe("Failed to register revival token recipe: " + e.getMessage());
        }
    }
    
    /**
     * Sets recipe ingredients from configuration
     */
    private void setRecipeIngredientsFromConfig(ShapedRecipe recipe) {
        ConfigurationSection recipeConfig = plugin.getConfig().getConfigurationSection("revival-token.recipe");
        if (recipeConfig == null) {
            plugin.getLogger().warning("Missing 'revival-token.recipe' section in config.yml. Using defaults.");
            setDefaultRecipeIngredients(recipe);
            return;
        }
        
        String[] positions = {"a11", "a12", "a13", "a21", "a22", "a23", "a31", "a32", "a33"};
        char[] recipeChars = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I'};
        boolean hasIngredients = false;
        
        for (int i = 0; i < positions.length; i++) {
            String materialName = recipeConfig.getString(positions[i]);
            if (materialName == null) continue;
            
            char recipeChar = recipeChars[i];
            
            if ("SOUL_ITEM".equals(materialName)) {
                // Use soul material as placeholder
                String soulMaterialName = plugin.getConfigManager().getSoulMaterial();
                try {
                    Material placeholder = Material.valueOf(soulMaterialName.toUpperCase());
                    recipe.setIngredient(recipeChar, placeholder);
                    hasIngredients = true;
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid soul material for placeholder: " + soulMaterialName);
                }
            } else {
                try {
                    Material mat = Material.valueOf(materialName.toUpperCase());
                    recipe.setIngredient(recipeChar, mat);
                    hasIngredients = true;
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid material in revival token recipe: " + materialName + " for position " + positions[i]);
                }
            }
        }
        
        if (!hasIngredients) {
            plugin.getLogger().warning("No valid ingredients found for revival token recipe. Using defaults.");
            setDefaultRecipeIngredients(recipe);
        }
    }
    
    /**
     * Sets default recipe ingredients as fallback
     */
    private void setDefaultRecipeIngredients(ShapedRecipe recipe) {
        recipe.setIngredient('A', Material.NETHERITE_BLOCK);
        recipe.setIngredient('B', Material.NETHER_STAR);
        recipe.setIngredient('C', Material.NETHERITE_BLOCK);
        recipe.setIngredient('D', Material.GHAST_TEAR);
        recipe.setIngredient('E', Material.GHAST_TEAR);
        recipe.setIngredient('F', Material.GHAST_TEAR);
        recipe.setIngredient('G', Material.NETHERITE_BLOCK);
        recipe.setIngredient('H', Material.NETHER_STAR);
        recipe.setIngredient('I', Material.NETHERITE_BLOCK);
    }
    
    /**
     * Reloads all recipes
     */
    public void reload() {
        try {
            registerRecipes();
            plugin.getLogger().info("RecipeManager reloaded successfully.");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to reload RecipeManager: " + e.getMessage());
            e.printStackTrace();
        }
    }
}