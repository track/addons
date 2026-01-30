package com.serverstats.addon.shopguiplus.listener;

import com.serverstats.addon.shopguiplus.config.ShopGuiPlusConfig;
import com.serverstats.api.ServerStats;
import com.serverstats.api.addon.AddonLogger;
import net.brcdev.shopgui.event.ShopPostTransactionEvent;
import net.brcdev.shopgui.shop.ShopManager.ShopAction;
import net.brcdev.shopgui.shop.ShopTransactionResult;
import net.brcdev.shopgui.shop.ShopTransactionResult.ShopTransactionResultType;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

/**
 * Listener for ShopGUIPlus transaction events.
 * Tracks purchases and sales in ServerStats.
 */
public class ShopListener implements Listener {

  private final AddonLogger logger;
  private final ShopGuiPlusConfig config;

  /**
   * Create a new shop listener
   *
   * @param logger The addon logger
   * @param config The addon configuration
   */
  public ShopListener(AddonLogger logger, ShopGuiPlusConfig config) {
    this.logger = logger;
    this.config = config;
  }

  /**
   * Handle shop transactions (buy and sell)
   *
   * @param event The transaction event
   */
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onShopTransaction(ShopPostTransactionEvent event) {
    ShopTransactionResult result = event.getResult();
    ShopTransactionResultType resultType = result.getResult();
    ShopAction action = result.getShopAction();

    // Check if this result type should be tracked
    if (!config.isResultEnabled(resultType)) {
      if (config.isDebug()) {
        logger.debug("Skipping transaction - result type %s is disabled", resultType);
      }
      return;
    }

    // Check if this action should be tracked
    if (!config.isActionEnabled(action)) {
      if (config.isDebug()) {
        logger.debug("Skipping transaction - action %s is disabled", action);
      }
      return;
    }

    Player player = result.getPlayer();
    ItemStack item = result.getShopItem().getItem();
    double price = result.getPrice();
    int amount = result.getAmount();

    // Determine event name based on action type
    String eventName;
    switch (action) {
      case BUY -> eventName = "shopguiplus.purchase";
      case SELL -> eventName = "shopguiplus.sell";
      case SELL_ALL -> eventName = "shopguiplus.sell_all";
      default -> {
        return; // Unknown action, skip
      }
    }

    // Get item details
    String itemType = item.getType().name();
    String itemName = item.hasItemMeta() && item.getItemMeta().hasDisplayName()
      ? PlainTextComponentSerializer.plainText()
        .serialize(item.getItemMeta().displayName())
      : itemType;
    String shopId = result.getShopItem().getShop().getId();
    String shopName = result.getShopItem().getShop().getName();

    // Track the event
    ServerStats.trackEvent(eventName)
      .withPlayer(player.getUniqueId(), player.getName())
      .withValue(price)
      .withData("item_type", itemType)
      .withData("item_name", itemName)
      .withData("amount", amount)
      .withData("shop_id", shopId)
      .withData("shop_name", shopName)
      .withData("action", action.name())
      .withData("result", resultType.name())
      .send();

    logger.debug(
      "Tracked %s: %s bought %dx %s for %.2f from shop %s",
      eventName,
      player.getName(),
      amount,
      itemType,
      price,
      shopName
    );
  }
}
