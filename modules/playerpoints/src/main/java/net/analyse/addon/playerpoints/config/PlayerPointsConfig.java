package net.analyse.addon.playerpoints.config;

import net.analyse.api.addon.AddonLogger;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Configuration for the PlayerPoints addon.
 */
public class PlayerPointsConfig {

  private final Path configFile;
  private final AddonLogger logger;

  private boolean debug;
  private boolean trackChanges;
  private boolean trackResets;
  private int minimumChange;
  private boolean includeChangeType;

  /**
   * Create a new config and load it from disk.
   *
   * @param dataFolder The addon's data folder
   * @param logger The addon logger
   */
  public PlayerPointsConfig(Path dataFolder, AddonLogger logger) {
    this.configFile = dataFolder.resolve("config.yml");
    this.logger = logger;
    load();
  }

  /**
   * Load or reload the configuration from disk.
   */
  public void load() {
    if (!Files.exists(configFile)) {
      saveDefaultConfig();
    }

    FileConfiguration config = YamlConfiguration.loadConfiguration(configFile.toFile());

    this.debug = config.getBoolean("debug", false);
    this.trackChanges = config.getBoolean("tracking.track_changes", true);
    this.trackResets = config.getBoolean("tracking.track_resets", true);
    this.minimumChange = config.getInt("tracking.minimum_change", 0);
    this.includeChangeType = config.getBoolean("tracking.include_change_type", true);

    if (debug) {
      logger.debug("Config loaded - Track changes: %s, Track resets: %s, Min change: %d",
        trackChanges, trackResets, minimumChange);
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
      # PlayerPoints Addon Configuration
      debug: false

      tracking:
        track_changes: true
        track_resets: true
        minimum_change: 0
        include_change_type: true
      """;
    Files.writeString(configFile, minimalConfig);
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
   * Check if point change events should be tracked.
   *
   * @return true if change tracking is enabled
   */
  public boolean shouldTrackChanges() {
    return trackChanges;
  }

  /**
   * Check if point reset events should be tracked.
   *
   * @return true if reset tracking is enabled
   */
  public boolean shouldTrackResets() {
    return trackResets;
  }

  /**
   * Get the minimum absolute point change that will be tracked.
   * Changes below this threshold are dropped.
   *
   * @return The minimum change threshold
   */
  public int getMinimumChange() {
    return minimumChange;
  }

  /**
   * Check if the change type (give vs. take) should be attached to events.
   *
   * @return true if the change type should be included
   */
  public boolean shouldIncludeChangeType() {
    return includeChangeType;
  }
}
