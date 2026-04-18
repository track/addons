package net.analyse.addon.votifier.config;

import net.analyse.api.addon.AddonLogger;
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

  private boolean debug;
  private Set<String> allowedServices;
  private boolean includeService;
  private boolean includeAddress;
  private boolean includeTimestamp;

  /**
   * Create a new config and load it from disk.
   *
   * @param dataFolder The addon's data folder
   * @param logger The addon logger
   */
  public VotifierConfig(Path dataFolder, AddonLogger logger) {
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

    // Parse allowed services (empty list means "allow everything")
    this.allowedServices = new HashSet<>();
    List<String> servicesList = config.getStringList("tracking.services");
    for (String service : servicesList) {
      allowedServices.add(service.toLowerCase());
    }

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
   * Check if a service should be tracked.
   *
   * @param serviceName The service name from the vote
   * @return true if it should be tracked
   */
  public boolean isServiceAllowed(String serviceName) {
    if (allowedServices.isEmpty()) {
      return true;
    }

    return allowedServices.contains(serviceName.toLowerCase());
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
   * Check if the service name should be attached to tracked events.
   *
   * @return true if the service name should be included
   */
  public boolean shouldIncludeService() {
    return includeService;
  }

  /**
   * Check if the voter's IP address should be attached to tracked events.
   *
   * @return true if the address should be included
   */
  public boolean shouldIncludeAddress() {
    return includeAddress;
  }

  /**
   * Check if the vote timestamp should be attached to tracked events.
   *
   * @return true if the timestamp should be included
   */
  public boolean shouldIncludeTimestamp() {
    return includeTimestamp;
  }

  /**
   * Get a snapshot of the allowed services.
   *
   * @return A copy of the allowed services set
   */
  public Set<String> getAllowedServices() {
    return new HashSet<>(allowedServices);
  }
}
