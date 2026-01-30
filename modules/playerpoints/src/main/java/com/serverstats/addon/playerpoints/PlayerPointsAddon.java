package com.serverstats.addon.playerpoints;

import com.serverstats.addon.playerpoints.config.PlayerPointsConfig;
import com.serverstats.addon.playerpoints.listener.PointsListener;
import com.serverstats.api.addon.Addon;
import com.serverstats.api.addon.AddonInfo;
import com.serverstats.api.addon.AddonLogger;
import com.serverstats.api.platform.ServerStatsPlatform;
import java.nio.file.Path;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * ServerStats addon for PlayerPoints integration.
 * Tracks point transactions (give, take, reset).
 */
@AddonInfo(
  id = "playerpoints",
  name = "PlayerPoints Integration",
  version = "1.0.0",
  author = "ServerStats",
  description = "Tracks PlayerPoints transactions in ServerStats analytics"
)
public class PlayerPointsAddon implements Addon {

  private AddonLogger logger;
  private Path dataFolder;
  private PlayerPointsConfig config;
  private PointsListener pointsListener;

  @Override
  public void onLoad(
    ServerStatsPlatform platform,
    AddonLogger logger,
    Path dataFolder
  ) {
    this.logger = logger;
    this.dataFolder = dataFolder;
  }

  @Override
  public void onEnable() {
    // Load configuration
    this.config = new PlayerPointsConfig(dataFolder, logger);

    // Check if PlayerPoints is installed
    Plugin playerPoints = Bukkit.getPluginManager().getPlugin("PlayerPoints");
    if (playerPoints == null || !playerPoints.isEnabled()) {
      logger.warning(
        "PlayerPoints is not installed or enabled! Addon will be inactive."
      );
      return;
    }

    // Get the ServerStats plugin to register events
    Plugin serverStats = Bukkit.getPluginManager().getPlugin("ServerStats");
    if (serverStats == null) {
      logger.error("ServerStats plugin not found! Cannot register listeners.");
      return;
    }

    // Register our listener
    pointsListener = new PointsListener(logger, config);
    Bukkit.getPluginManager().registerEvents(pointsListener, serverStats);

    logger.info(
      "Successfully hooked into PlayerPoints v%s",
      playerPoints.getPluginMeta().getVersion()
    );
  }

  @Override
  public void onDisable() {
    pointsListener = null;
    logger.info("PlayerPoints integration disabled");
  }

  @Override
  public void onReload() {
    if (config != null) {
      config.load();
      logger.info("Configuration reloaded");
    }
  }

  /**
   * Get the addon logger
   *
   * @return The logger
   */
  public AddonLogger getLogger() {
    return logger;
  }

  /**
   * Get the addon's data folder
   *
   * @return The data folder path
   */
  public Path getDataFolder() {
    return dataFolder;
  }

  /**
   * Get the addon configuration
   *
   * @return The configuration
   */
  public PlayerPointsConfig getConfig() {
    return config;
  }
}
