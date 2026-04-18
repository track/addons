# Analyse Addons

The source repository for the official [Analyse](https://analyse.net) addons &mdash; plug-in integrations that teach Analyse about events coming from other plugins (shop sales, votes, point transactions, and so on).

Analyse is the analytics platform purpose-built for Minecraft and Hytale servers. The main plugin lives in [`Analyse-net/analyse-java`](https://github.com/Analyse-net/analyse-java). This repository is the home of the first-party addons that ship alongside it.

## Available addons

| Addon | Plugin it hooks | Events tracked |
| --- | --- | --- |
| **ShopGUIPlus** | [ShopGUIPlus](https://www.spigotmc.org/resources/shopgui-1-7-1-21.6515/) | `shopguiplus.purchase`, `shopguiplus.sell`, `shopguiplus.sell_all` |
| **Votifier** | [NuVotifier](https://www.spigotmc.org/resources/nuvotifier.13449/) | `votifier.vote` |
| **PlayerPoints** | [PlayerPoints](https://www.spigotmc.org/resources/playerpoints.80745/) | `playerpoints.give`, `playerpoints.take`, `playerpoints.reset` |

## Getting started

1. Install the [Analyse plugin](https://analyse.net/downloads) (v1.0.0 or newer) and configure your API key.
2. Download the addon jar(s) you want from the [releases page](https://github.com/Analyse-net/addons/releases).
3. Drop them in `plugins/Analyse/addons/`.
4. Start the server once so the addon config generates, or run `/analyse reload`.
5. Edit `plugins/Analyse/addons/<addon>/config.yml` if you want to tweak what gets tracked.

## Requirements

- **Analyse** plugin 1.0.0+
- **Java 21** or higher
- **Paper / Spigot / Purpur / Folia** 1.21.4+ (or a compatible fork)
- The corresponding third-party plugin for each addon (ShopGUIPlus, NuVotifier, PlayerPoints, etc.)

## Configuration

Each addon has its own config under:

```
plugins/Analyse/addons/<addon>/config.yml
```

The file is generated on first load. Use `/analyse reload` to pick up changes without restarting.

### ShopGUIPlus

```yaml
debug: false

tracking:
  # Which transaction results to track
  results:
    - SUCCESS
    # - FAIL
    # - NOT_ENOUGH_MONEY
    # - NOT_ENOUGH_SPACE
    # - NOT_ENOUGH_ITEMS

  # Which shop actions to track
  actions:
    - BUY
    - SELL
    - SELL_ALL
```

### Votifier

```yaml
debug: false

tracking:
  # Only track specific voting services (empty = all)
  services: []

  include_service: true
  include_address: false  # privacy consideration, off by default
  include_timestamp: true
```

### PlayerPoints

```yaml
debug: false

tracking:
  track_changes: true    # give / take
  track_resets: true     # resets
  minimum_change: 0      # filter tiny changes
  include_change_type: true
```

## Building from source

```bash
./gradlew clean build
```

Per-addon builds:

```bash
./gradlew :modules:shopguiplus:build
./gradlew :modules:votifier:build
./gradlew :modules:playerpoints:build
```

Output jars land in `modules/<addon>/build/libs/analyse-addon-<addon>-<version>.jar`.

The `scripts/release.sh` script builds every addon and bundles the jars into a single `analyse-addons-<version>.zip` archive.

## Creating a new addon

1. Create a module folder:

   ```bash
   mkdir -p modules/myaddon/src/main/java/net/analyse/addon/myaddon
   mkdir -p modules/myaddon/src/main/resources
   ```

2. Create `modules/myaddon/build.gradle`:

   ```groovy
   plugins {
       id 'java'
   }

   dependencies {
       compileOnly "io.papermc.paper:paper-api:${project.property('paperVersion')}"
       // Add the third-party plugin's API dependency here
   }

   jar {
       archiveBaseName.set('analyse-addon-myaddon')
   }
   ```

3. Create your addon class with the `@AddonInfo` annotation:

   ```java
   package net.analyse.addon.myaddon;

   import net.analyse.api.addon.Addon;
   import net.analyse.api.addon.AddonInfo;
   import net.analyse.api.addon.AddonLogger;
   import net.analyse.api.platform.AnalysePlatform;
   import java.nio.file.Path;

   @AddonInfo(
     id = "myaddon",
     name = "My Addon",
     version = "1.0.0",
     author = "YourName",
     description = "Description of what this addon tracks"
   )
   public class MyAddon implements Addon {
     // ...
   }
   ```

4. The module is auto-discovered by `settings.gradle`. Just run `./gradlew build`.

### Addon lifecycle

```java
public class MyAddon implements Addon {

  @Override
  public void onLoad(AnalysePlatform platform, AddonLogger logger, Path dataFolder) {
    // Store references
  }

  @Override
  public void onEnable() {
    // Register listeners, open resources
  }

  @Override
  public void onDisable() {
    // Clean up
  }

  @Override
  public void onReload() {
    // Reload config on /analyse reload
  }
}
```

### Tracking events

```java
Analyse.trackEvent("myaddon.event_name")
  .withPlayer(player.getUniqueId(), player.getName())
  .withValue(123.45)           // optional numeric value
  .withData("key", "value")    // optional extra fields
  .send();
```

## Project structure

```
addons/
├── .cursor/rules/        # Java style guide + commit rules
├── .github/              # CI, release, issue / PR templates, Dependabot
├── modules/              # Addon modules (auto-discovered)
│   ├── shopguiplus/
│   ├── votifier/
│   └── playerpoints/
├── scripts/release.sh    # Local release bundler
├── build.gradle          # Root build
├── settings.gradle       # Module discovery
└── gradle.properties     # Shared properties
```

## Documentation

- Main plugin docs: [analyse.net/docs](https://analyse.net/docs)
- SDK overview: [analyse-java/docs/sdk](https://github.com/Analyse-net/analyse-java/blob/main/docs/sdk/README.md)

## Support

- Website: [analyse.net](https://analyse.net)
- Dashboard: [analyse.net/dashboard](https://analyse.net/dashboard)
- Discord: linked from [analyse.net](https://analyse.net)

## License

Copyright &copy; VertCode Development E.E. All rights reserved.

The source in this repository is published for transparency and reference. It is **not** open source; copying, modifying, redistributing, or running modified builds is not permitted. See [`LICENSE`](LICENSE) for the full terms.
