package net.analyse.addon.shopguiplus;

import net.analyse.addon.shopguiplus.config.ShopGuiPlusConfig;
import net.analyse.addon.shopguiplus.listener.ShopListener;
import net.analyse.api.addon.Addon;
import net.analyse.api.addon.AddonInfo;
import net.analyse.api.addon.AddonLogger;
import net.analyse.api.platform.AnalysePlatform;
import java.nio.file.Path;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * Analyse addon for ShopGUIPlus integration.
 * Tracks shop purchase and sell events.
 */
@AddonInfo(
  id = "shopguiplus",
  name = "ShopGUI+ Integration",
  version = "1.0.0",
  author = "Analyse",
  description = "Tracks ShopGUI+ purchase and sell events in Analyse"
)
public class ShopGuiPlusAddon implements Addon {

  private AddonLogger logger;
  private Path dataFolder;
  private ShopGuiPlusConfig config;
  private ShopListener shopListener;

  @Override
  public void onLoad(
    AnalysePlatform platform,
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

    // Get the Analyse plugin to register events under
    Plugin analyse = Bukkit.getPluginManager().getPlugin("Analyse");
    if (analyse == null) {
      logger.error("Analyse plugin not found! Cannot register listeners.");
      return;
    }

    // Register our listener
    shopListener = new ShopListener(logger, config);
    Bukkit.getPluginManager().registerEvents(shopListener, analyse);

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
