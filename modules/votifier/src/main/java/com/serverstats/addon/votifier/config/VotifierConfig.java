package com.serverstats.addon.votifier.config;

import com.serverstats.api.addon.AddonLogger;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Configuration for the Votifier addon.
 */
public class VotifierConfig {

  private final Path configFile;
  private final AddonLogger logger;

  // Config values
  private boolean debug;
  private Set<String> allowedServices;
  private boolean includeService;
  private boolean includeAddress;
  private boolean includeTimestamp;

  public VotifierConfig(Path dataFolder, AddonLogger logger) {
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

    // Load allowed services (empty = all services)
    this.allowedServices = new HashSet<>();
    List<String> servicesList = config.getStringList("tracking.services");
    for (String service : servicesList) {
      allowedServices.add(service.toLowerCase());
    }

    // Load tracking options
    this.includeService = config.getBoolean("tracking.include_service", true);
    this.includeAddress = config.getBoolean("tracking.include_address", false);
    this.includeTimestamp = config.getBoolean("tracking.include_timestamp", true);

    if (debug) {
      logger.debug("Config loaded - Services filter: %s, Include service: %s, Include address: %s",
        allowedServices.isEmpty() ? "all" : allowedServices,
        includeService,
        includeAddress);
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
      # Votifier Addon Configuration
      debug: false
      
      tracking:
        services: []
        include_service: true
        include_address: false
        include_timestamp: true
      """;
    Files.writeString(configFile, minimalConfig);
  }

  /**
   * Check if a service should be tracked
   */
  public boolean isServiceAllowed(String serviceName) {
    if (allowedServices.isEmpty()) {
      return true; // Empty = allow all
    }
    return allowedServices.contains(serviceName.toLowerCase());
  }

  /**
   * Check if debug mode is enabled
   */
  public boolean isDebug() {
    return debug;
  }

  /**
   * Check if service name should be included in tracking data
   */
  public boolean shouldIncludeService() {
    return includeService;
  }

  /**
   * Check if voter address should be included in tracking data
   */
  public boolean shouldIncludeAddress() {
    return includeAddress;
  }

  /**
   * Check if vote timestamp should be included in tracking data
   */
  public boolean shouldIncludeTimestamp() {
    return includeTimestamp;
  }

  /**
   * Get allowed services
   */
  public Set<String> getAllowedServices() {
    return new HashSet<>(allowedServices);
  }
}
