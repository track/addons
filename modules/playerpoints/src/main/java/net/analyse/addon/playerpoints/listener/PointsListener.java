package net.analyse.addon.playerpoints.listener;

import net.analyse.addon.playerpoints.config.PlayerPointsConfig;
import net.analyse.api.Analyse;
import net.analyse.api.addon.AddonLogger;
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
 * Tracks point changes and resets in Analyse.
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

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPointsChange(PlayerPointsChangeEvent event) {
    if (!config.shouldTrackChanges()) {
      return;
    }

    int change = event.getChange();

    // Skip changes below the configured minimum
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

    // Give vs. take events, based on direction
    String eventName = change > 0 ? "playerpoints.give" : "playerpoints.take";

    var tracker = Analyse.trackEvent(eventName)
      .withPlayer(playerId, playerName)
      .withValue(Math.abs(change))
      .withData("change", change);

    // Attach the balance snapshot when the PlayerPoints API is reachable
    try {
      int currentBalance = PlayerPoints.getInstance().getAPI().look(playerId);
      int newBalance = currentBalance + change;
      tracker.withData("balance_before", currentBalance);
      tracker.withData("balance_after", newBalance);
    } catch (Exception e) {
      // API might not be available yet, just drop the balance fields
    }

    tracker.send();

    if (config.isDebug()) {
      logger.debug("Tracked %s: %s changed by %d points",
        eventName, playerName, change);
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPointsReset(PlayerPointsResetEvent event) {
    if (!config.shouldTrackResets()) {
      return;
    }

    UUID playerId = event.getPlayerId();
    OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);
    String playerName = player.getName() != null ? player.getName() : playerId.toString();

    var tracker = Analyse.trackEvent("playerpoints.reset")
      .withPlayer(playerId, playerName);

    // Record the balance that's about to be wiped, if the API is reachable
    try {
      int currentBalance = PlayerPoints.getInstance().getAPI().look(playerId);
      tracker.withValue(currentBalance);
      tracker.withData("balance_before_reset", currentBalance);
    } catch (Exception e) {
      // API might not be available yet, just drop the balance fields
    }

    tracker.send();

    if (config.isDebug()) {
      logger.debug("Tracked playerpoints.reset: %s points reset", playerName);
    }
  }
}
