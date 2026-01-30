package com.serverstats.addon.shopguiplus;

import com.serverstats.addon.shopguiplus.config.ShopGuiPlusConfig;
import com.serverstats.addon.shopguiplus.listener.ShopListener;
import com.serverstats.api.addon.Addon;
import com.serverstats.api.addon.AddonInfo;
import com.serverstats.api.addon.AddonLogger;
import com.serverstats.api.platform.ServerStatsPlatform;
import java.nio.file.Path;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * ServerStats addon for ShopGUIPlus integration.
 * Tracks shop purchase and sell events.
 */
@AddonInfo(
  id = "shopguiplus",
  name = "ShopGUI+ Integration",
  version = "1.0.0",
  author = "ServerStats",
  description = "Tracks ShopGUI+ purchase and sell events in ServerStats analytics"
)
public class ShopGuiPlusAddon implements Addon {

  private AddonLogger logger;
  private Path dataFolder;
  private ShopGuiPlusConfig config;
  private ShopListener shopListener;

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
    this.config = new ShopGuiPlusConfig(dataFolder, logger);

    // Check if ShopGUIPlus is installed
    Plugin shopGuiPlus = Bukkit.getPluginManager().getPlugin("ShopGUIPlus");
    if (shopGuiPlus == null || !shopGuiPlus.isEnabled()) {
      logger.warning(
        "ShopGUIPlus is not installed or enabled! Addon will be inactive."
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
    shopListener = new ShopListener(logger, config);
    Bukkit.getPluginManager().registerEvents(shopListener, serverStats);

    logger.info(
      "Successfully hooked into ShopGUIPlus v" +
      shopGuiPlus.getPluginMeta().getVersion()
    );
  }

  @Override
  public void onReload() {
    // Reload the configuration
    if (config != null) {
      config.load();
      logger.info("Configuration reloaded");
    }
  }

  @Override
  public void onDisable() {
    // Bukkit automatically unregisters listeners when the plugin is disabled
    shopListener = null;
    logger.info("ShopGUI+ integration disabled");
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
  public ShopGuiPlusConfig getConfig() {
    return config;
  }
}
