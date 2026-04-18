package net.analyse.addon.votifier;

import net.analyse.addon.votifier.config.VotifierConfig;
import net.analyse.addon.votifier.listener.VoteListener;
import net.analyse.api.addon.Addon;
import net.analyse.api.addon.AddonInfo;
import net.analyse.api.addon.AddonLogger;
import net.analyse.api.platform.AnalysePlatform;
import java.nio.file.Path;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * Analyse addon for Votifier/NuVotifier integration.
 * Tracks player votes from voting sites.
 */
@AddonInfo(
  id = "votifier",
  name = "Votifier Integration",
  version = "1.0.0",
  author = "Analyse",
  description = "Tracks player votes from NuVotifier in Analyse"
)
public class VotifierAddon implements Addon {

  private AddonLogger logger;
  private Path dataFolder;
  private VotifierConfig config;
  private VoteListener voteListener;

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
    this.config = new VotifierConfig(dataFolder, logger);

    // Look up Votifier/NuVotifier on the server
    Plugin votifier = Bukkit.getPluginManager().getPlugin("Votifier");
    if (votifier == null) {
      votifier = Bukkit.getPluginManager().getPlugin("NuVotifier");
    }

    if (votifier == null || !votifier.isEnabled()) {
      logger.warning(
        "Votifier/NuVotifier is not installed or enabled! Addon will be inactive."
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
    voteListener = new VoteListener(logger, config);
    Bukkit.getPluginManager().registerEvents(voteListener, analyse);

    logger.info(
      "Successfully hooked into %s v%s",
      votifier.getName(),
      votifier.getPluginMeta().getVersion()
    );
  }

  @Override
  public void onDisable() {
    voteListener = null;
    logger.info("Votifier integration disabled");
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
  public VotifierConfig getConfig() {
    return config;
  }
}
