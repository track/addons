package com.serverstats.addon.playerpoints.listener;

import com.serverstats.addon.playerpoints.config.PlayerPointsConfig;
import com.serverstats.api.ServerStats;
import com.serverstats.api.addon.AddonLogger;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.event.PlayerPointsChangeEvent;
import org.black_ixx.playerpoints.event.PlayerPointsResetEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.UUID;

/**
 * Listener for PlayerPoints events.
 * Tracks point changes and resets in ServerStats.
 */
public class PointsListener implements Listener {

  private final AddonLogger logger;
  private final PlayerPointsConfig config;

  /**
   * Create a new points listener
   *
   * @param logger The addon logger
   * @param config The addon configuration
   */
  public PointsListener(AddonLogger logger, PlayerPointsConfig config) {
    this.logger = logger;
    this.config = config;
  }

  /**
   * Handle point change events
   *
   * @param event The point change event
   */
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPointsChange(PlayerPointsChangeEvent event) {
    if (!config.shouldTrackChanges()) {
      return;
    }

    int change = event.getChange();

    // Check minimum change threshold
    if (Math.abs(change) < config.getMinimumChange()) {
      if (config.isDebug()) {
        logger.debug("Skipping point change - below minimum threshold (%d < %d)",
          Math.abs(change), config.getMinimumChange());
      }
      return;
    }

    UUID playerId = event.getPlayerId();
    OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);
    String playerName = player.getName() != null ? player.getName() : playerId.toString();

    // Determine event type based on change direction
    String eventName = change > 0 ? "playerpoints.give" : "playerpoints.take";

    // Build the tracker
    var tracker = ServerStats.trackEvent(eventName)
      .withPlayer(playerId, playerName)
      .withValue(Math.abs(change))
      .withData("change", change);

    // Get balance after change if possible
    try {
      int currentBalance = PlayerPoints.getInstance().getAPI().look(playerId);
      int newBalance = currentBalance + change;
      tracker.withData("balance_before", currentBalance);
      tracker.withData("balance_after", newBalance);
    } catch (Exception e) {
      // API might not be available, skip balance info
    }

    tracker.send();

    if (config.isDebug()) {
      logger.debug("Tracked %s: %s changed by %d points",
        eventName, playerName, change);
    }
  }

  /**
   * Handle point reset events
   *
   * @param event The point reset event
   */
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPointsReset(PlayerPointsResetEvent event) {
    if (!config.shouldTrackResets()) {
      return;
    }

    UUID playerId = event.getPlayerId();
    OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);
    String playerName = player.getName() != null ? player.getName() : playerId.toString();

    // Get balance before reset if possible
    var tracker = ServerStats.trackEvent("playerpoints.reset")
      .withPlayer(playerId, playerName);

    try {
      int currentBalance = PlayerPoints.getInstance().getAPI().look(playerId);
      tracker.withValue(currentBalance);
      tracker.withData("balance_before_reset", currentBalance);
    } catch (Exception e) {
      // API might not be available, skip balance info
    }

    tracker.send();

    if (config.isDebug()) {
      logger.debug("Tracked playerpoints.reset: %s points reset", playerName);
    }
  }
}
