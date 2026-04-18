package net.analyse.addon.votifier.listener;

import net.analyse.addon.votifier.config.VotifierConfig;
import net.analyse.api.Analyse;
import net.analyse.api.addon.AddonLogger;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Listener for Votifier vote events.
 * Tracks player votes in Analyse.
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

  @EventHandler(priority = EventPriority.MONITOR)
  public void onVote(VotifierEvent event) {
    Vote vote = event.getVote();
    String serviceName = vote.getServiceName();
    String username = vote.getUsername();

    // Filter by allow-listed services
    if (!config.isServiceAllowed(serviceName)) {
      if (config.isDebug()) {
        logger.debug("Skipping vote from service %s - not in allowed list", serviceName);
      }

      return;
    }

    // Resolve the player's UUID where possible
    @SuppressWarnings("deprecation")
    OfflinePlayer player = Bukkit.getOfflinePlayer(username);

    var tracker = Analyse.trackEvent("votifier.vote");

    // Attach the player if we recognise them, otherwise keep the raw username
    if (player.hasPlayedBefore() || player.isOnline()) {
      tracker.withPlayer(player.getUniqueId(), username);
    } else {
      tracker.withData("username", username);
    }

    // Attach optional metadata based on config
    if (config.shouldIncludeService()) {
      tracker.withData("service", serviceName);
    }

    if (config.shouldIncludeAddress()) {
      tracker.withData("address", vote.getAddress());
    }

    if (config.shouldIncludeTimestamp()) {
      tracker.withData("vote_timestamp", vote.getTimeStamp());
    }

    tracker.send();

    if (config.isDebug()) {
      logger.debug("Tracked vote: %s voted via %s", username, serviceName);
    }
  }
}
