package com.serverstats.addon.votifier.listener;

import com.serverstats.addon.votifier.config.VotifierConfig;
import com.serverstats.api.ServerStats;
import com.serverstats.api.addon.AddonLogger;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Listener for Votifier vote events.
 * Tracks player votes in ServerStats.
 */
public class VoteListener implements Listener {

  private final AddonLogger logger;
  private final VotifierConfig config;

  /**
   * Create a new vote listener
   *
   * @param logger The addon logger
   * @param config The addon configuration
   */
  public VoteListener(AddonLogger logger, VotifierConfig config) {
    this.logger = logger;
    this.config = config;
  }

  /**
   * Handle vote events
   *
   * @param event The votifier event
   */
  @EventHandler(priority = EventPriority.MONITOR)
  public void onVote(VotifierEvent event) {
    Vote vote = event.getVote();
    String serviceName = vote.getServiceName();
    String username = vote.getUsername();

    // Check if this service should be tracked
    if (!config.isServiceAllowed(serviceName)) {
      if (config.isDebug()) {
        logger.debug("Skipping vote from service %s - not in allowed list", serviceName);
      }
      return;
    }

    // Try to get the player's UUID
    @SuppressWarnings("deprecation")
    OfflinePlayer player = Bukkit.getOfflinePlayer(username);

    // Build the event tracker
    var tracker = ServerStats.trackEvent("votifier.vote");

    // Add player info if we have a valid player
    if (player.hasPlayedBefore() || player.isOnline()) {
      tracker.withPlayer(player.getUniqueId(), username);
    } else {
      // Player hasn't joined before, just track the username
      tracker.withData("username", username);
    }

    // Add optional data based on config
    if (config.shouldIncludeService()) {
      tracker.withData("service", serviceName);
    }

    if (config.shouldIncludeAddress()) {
      tracker.withData("address", vote.getAddress());
    }

    if (config.shouldIncludeTimestamp()) {
      tracker.withData("vote_timestamp", vote.getTimeStamp());
    }

    // Send the event
    tracker.send();

    if (config.isDebug()) {
      logger.debug("Tracked vote: %s voted via %s", username, serviceName);
    }
  }
}
