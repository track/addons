package net.analyse.addon.shopguiplus.config;

import net.analyse.api.addon.AddonLogger;
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

  private Set<ShopTransactionResultType> enabledResults;
  private Set<ShopAction> enabledActions;
  private boolean debug;

  /**
   * Create a new config and load it from disk.
   *
   * @param dataFolder The addon's data folder
   * @param logger The addon logger
   */
  public ShopGuiPlusConfig(Path dataFolder, AddonLogger logger) {
    this.configFile = dataFolder.resolve("config.yml");
    this.logger = logger;
    load();
  }

  /**
   * Load or reload the configuration from disk.
   */
  public void load() {
    // Generate the default config on first launch
    if (!Files.exists(configFile)) {
      saveDefaultConfig();
    }

    FileConfiguration config = YamlConfiguration.loadConfiguration(configFile.toFile());

    this.debug = config.getBoolean("debug", false);

    // Parse the list of result types we care about
    this.enabledResults = EnumSet.noneOf(ShopTransactionResultType.class);
    List<String> resultsList = config.getStringList("tracking.results");
    for (String result : resultsList) {
      try {
        enabledResults.add(ShopTransactionResultType.valueOf(result.toUpperCase()));
      } catch (IllegalArgumentException e) {
        logger.warning("Unknown result type in config: %s", result);
      }
    }

    // Default to SUCCESS only if nothing was configured
    if (enabledResults.isEmpty()) {
      enabledResults.add(ShopTransactionResultType.SUCCESS);
    }

    // Parse the list of shop actions we care about
    this.enabledActions = EnumSet.noneOf(ShopAction.class);
    List<String> actionsList = config.getStringList("tracking.actions");
    for (String action : actionsList) {
      try {
        enabledActions.add(ShopAction.valueOf(action.toUpperCase()));
      } catch (IllegalArgumentException e) {
        logger.warning("Unknown action type in config: %s", action);
      }
    }

    // Default to tracking every action
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
   * Save the default config.yml bundled with the addon to disk.
   */
  private void saveDefaultConfig() {
    try {
      Files.createDirectories(configFile.getParent());

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
   * Fallback config writer used when the bundled resource is missing.
   *
   * @throws IOException if the file can't be written
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
   * Check if a result type should be tracked.
   *
   * @param result The result type
   * @return true if the result should be tracked
   */
  public boolean isResultEnabled(ShopTransactionResultType result) {
    return enabledResults.contains(result);
  }

  /**
   * Check if an action should be tracked.
   *
   * @param action The shop action
   * @return true if the action should be tracked
   */
  public boolean isActionEnabled(ShopAction action) {
    return enabledActions.contains(action);
  }

  /**
   * Check if debug mode is enabled.
   *
   * @return true if debug mode is on
   */
  public boolean isDebug() {
    return debug;
  }

  /**
   * Get a snapshot of enabled result types.
   *
   * @return A copy of the enabled results set
   */
  public Set<ShopTransactionResultType> getEnabledResults() {
    return EnumSet.copyOf(enabledResults);
  }

  /**
   * Get a snapshot of enabled shop actions.
   *
   * @return A copy of the enabled actions set
   */
  public Set<ShopAction> getEnabledActions() {
    return EnumSet.copyOf(enabledActions);
  }
}
