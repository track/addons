package com.serverstats.addon.shopguiplus.config;

import com.serverstats.api.addon.AddonLogger;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.brcdev.shopgui.shop.ShopManager.ShopAction;
import net.brcdev.shopgui.shop.ShopTransactionResult.ShopTransactionResultType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Configuration for the ShopGUIPlus addon.
 */
public class ShopGuiPlusConfig {

  private final Path configFile;
  private final AddonLogger logger;

  // Config values
  private Set<ShopTransactionResultType> enabledResults;
  private Set<ShopAction> enabledActions;
  private boolean debug;

  public ShopGuiPlusConfig(Path dataFolder, AddonLogger logger) {
    this.configFile = dataFolder.resolve("config.yml");
    this.logger = logger;
    load();
  }

  /**
   * Load or reload the configuration
   */
  public void load() {
    // Create default config if it doesn't exist
    if (!Files.exists(configFile)) {
      saveDefaultConfig();
    }

    FileConfiguration config = YamlConfiguration.loadConfiguration(configFile.toFile());

    // Load debug setting
    this.debug = config.getBoolean("debug", false);

    // Load enabled results
    this.enabledResults = EnumSet.noneOf(ShopTransactionResultType.class);
    List<String> resultsList = config.getStringList("tracking.results");
    for (String result : resultsList) {
      try {
        enabledResults.add(ShopTransactionResultType.valueOf(result.toUpperCase()));
      } catch (IllegalArgumentException e) {
        logger.warning("Unknown result type in config: %s", result);
      }
    }

    // If no results configured, default to SUCCESS only
    if (enabledResults.isEmpty()) {
      enabledResults.add(ShopTransactionResultType.SUCCESS);
    }

    // Load enabled actions
    this.enabledActions = EnumSet.noneOf(ShopAction.class);
    List<String> actionsList = config.getStringList("tracking.actions");
    for (String action : actionsList) {
      try {
        enabledActions.add(ShopAction.valueOf(action.toUpperCase()));
      } catch (IllegalArgumentException e) {
        logger.warning("Unknown action type in config: %s", action);
      }
    }

    // If no actions configured, default to all
    if (enabledActions.isEmpty()) {
      enabledActions.add(ShopAction.BUY);
      enabledActions.add(ShopAction.SELL);
      enabledActions.add(ShopAction.SELL_ALL);
    }

    if (debug) {
      logger.debug("Config loaded - Results: %s, Actions: %s", enabledResults, enabledActions);
    }
  }

  /**
   * Save the default configuration file
   */
  private void saveDefaultConfig() {
    try {
      // Ensure parent directory exists
      Files.createDirectories(configFile.getParent());

      // Copy default config from resources
      try (InputStream defaultConfig = getClass().getResourceAsStream("/config.yml")) {
        if (defaultConfig != null) {
          Files.copy(defaultConfig, configFile);
          logger.info("Created default configuration file");
        } else {
          logger.warning("Default config.yml not found in resources, creating minimal config");
          createMinimalConfig();
        }
      }
    } catch (IOException e) {
      logger.error("Failed to save default config: %s", e.getMessage());
    }
  }

  /**
   * Create a minimal config if default resource is not found
   */
  private void createMinimalConfig() throws IOException {
    String minimalConfig = """
      # ShopGUIPlus Addon Configuration
      
      # Enable debug logging
      debug: false
      
      tracking:
        # Which transaction results to track
        # Available: SUCCESS, FAIL, NO_PERMISSION, NOT_ENOUGH_MONEY, NOT_ENOUGH_SPACE, NOT_ENOUGH_ITEMS
        results:
          - SUCCESS
        
        # Which actions to track
        # Available: BUY, SELL, SELL_ALL
        actions:
          - BUY
          - SELL
          - SELL_ALL
      """;
    Files.writeString(configFile, minimalConfig);
  }

  /**
   * Check if a result type should be tracked
   */
  public boolean isResultEnabled(ShopTransactionResultType result) {
    return enabledResults.contains(result);
  }

  /**
   * Check if an action should be tracked
   */
  public boolean isActionEnabled(ShopAction action) {
    return enabledActions.contains(action);
  }

  /**
   * Check if debug mode is enabled
   */
  public boolean isDebug() {
    return debug;
  }

  /**
   * Get enabled results
   */
  public Set<ShopTransactionResultType> getEnabledResults() {
    return EnumSet.copyOf(enabledResults);
  }

  /**
   * Get enabled actions
   */
  public Set<ShopAction> getEnabledActions() {
    return EnumSet.copyOf(enabledActions);
  }
}
