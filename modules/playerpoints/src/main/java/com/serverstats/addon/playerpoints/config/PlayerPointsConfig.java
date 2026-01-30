package com.serverstats.addon.playerpoints.config;

import com.serverstats.api.addon.AddonLogger;
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

  // Config values
  private boolean debug;
  private boolean trackChanges;
  private boolean trackResets;
  private int minimumChange;
  private boolean includeChangeType;

  public PlayerPointsConfig(Path dataFolder, AddonLogger logger) {
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

    // Load settings
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
   * Save the default configuration file
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
   * Create a minimal config if default resource is not found
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

  public boolean isDebug() {
    return debug;
  }

  public boolean shouldTrackChanges() {
    return trackChanges;
  }

  public boolean shouldTrackResets() {
    return trackResets;
  }

  public int getMinimumChange() {
    return minimumChange;
  }

  public boolean shouldIncludeChangeType() {
    return includeChangeType;
  }
}
