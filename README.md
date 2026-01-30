# ServerStats Addons

Official addon collection for [ServerStats](https://serverstats.com) - the analytics platform for Minecraft servers.

These addons extend ServerStats by integrating with popular plugins to automatically track events and player activity.

## Available Addons

| Addon | Plugin | Events Tracked |
|-------|--------|----------------|
| **ShopGUIPlus** | [ShopGUIPlus](https://www.spigotmc.org/resources/shopgui-1-7-1-21.6515/) | Purchases, sales, sell-all transactions |
| **Votifier** | [NuVotifier](https://www.spigotmc.org/resources/nuvotifier.13449/) | Player votes from voting sites |
| **PlayerPoints** | [PlayerPoints](https://www.spigotmc.org/resources/playerpoints.80745/) | Point transactions (give, take, reset) |

## Installation

1. Download the addon JAR(s) from the [Releases](../../releases) page
2. Place them in your server's `plugins/ServerStats/addons/` folder
3. Restart your server or run `/serverstats reload`
4. Configure the addon in `plugins/ServerStats/addons/<addon-name>/config.yml`

## Requirements
- **ServerStats** plugin (v0.6.0+)
- **Java 21** or higher
- **Paper/Spigot** 1.21.4+ (or compatible fork)
- The corresponding plugin for each addon

## Configuration

Each addon has its own configuration file located at:
```
plugins/ServerStats/addons/<addon-name>/config.yml
```

Configurations are automatically created on first load. Use `/serverstats reload` to reload addon configs without restarting.

### ShopGUIPlus Config

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

  # Which actions to track
  actions:
    - BUY
    - SELL
    - SELL_ALL
```

### Votifier Config

```yaml
debug: false

tracking:
  # Only track specific voting services (empty = all)
  services: []

  # Data to include in tracked events
  include_service: true
  include_address: false  # Privacy consideration
  include_timestamp: true
```

### PlayerPoints Config

```yaml
debug: false

tracking:
  track_changes: true    # Track give/take
  track_resets: true     # Track point resets
  minimum_change: 0      # Filter small changes
  include_change_type: true
```

## Building from Source

### Prerequisites

- Java 21 JDK
- Gradle (wrapper included)

### Build Commands

```bash
# Build all addons
./gradlew build

# Build specific addon
./gradlew :modules:shopguiplus:build
./gradlew :modules:votifier:build
./gradlew :modules:playerpoints:build

# Clean and build
./gradlew clean build
```

Output JARs are located in `modules/<addon>/build/libs/`.

## Creating a New Addon

1. Create a new module folder:
   ```bash
   mkdir -p modules/myaddon/src/main/java/com/serverstats/addon/myaddon
   mkdir -p modules/myaddon/src/main/resources
   ```

2. Create `modules/myaddon/build.gradle`:
   ```groovy
   plugins {
       id 'java'
   }

   dependencies {
       compileOnly "io.papermc.paper:paper-api:${project.property('paperVersion')}"
       // Add your plugin's API dependency here
   }

   jar {
       archiveBaseName.set('serverstats-addon-myaddon')
   }
   ```

3. Create your addon class with the `@AddonInfo` annotation:
   ```java
   @AddonInfo(
     id = "myaddon",
     name = "My Addon",
     version = "1.0.0",
     author = "YourName",
     description = "Description of what this addon tracks"
   )
   public class MyAddon implements Addon {
       // Implementation
   }
   ```

4. The module is automatically discovered - just run `./gradlew build`

### Addon Lifecycle

```java
public class MyAddon implements Addon {
    
    @Override
    public void onLoad(ServerStatsPlatform platform, AddonLogger logger, Path dataFolder) {
        // Called when addon is loaded - store references
    }
    
    @Override
    public void onEnable() {
        // Called when addon is enabled - register listeners
    }
    
    @Override
    public void onDisable() {
        // Called when addon is disabled - cleanup
    }
    
    @Override
    public void onReload() {
        // Called on /serverstats reload - reload config
    }
}
```

### Tracking Events

```java
ServerStats.trackEvent("myaddon.event_name")
    .withPlayer(player.getUniqueId(), player.getName())
    .withValue(123.45)  // Optional numeric value
    .withData("key", "value")  // Additional data
    .send();
```

## Project Structure

```
addons/
├── .github/workflows/    # CI/CD workflows
├── modules/              # Addon modules (auto-discovered)
│   ├── shopguiplus/
│   │   ├── build.gradle
│   │   └── src/main/
│   │       ├── java/...
│   │       └── resources/config.yml
│   └── votifier/
│       ├── build.gradle
│       └── src/main/
│           ├── java/...
│           └── resources/config.yml
├── build.gradle          # Root build config
├── settings.gradle       # Module discovery
└── gradle.properties     # Shared properties
```

## Contributing

1. Fork this repository
2. Create a feature branch (`git checkout -b feature/my-addon`)
3. Commit your changes (`git commit -am 'Add my addon'`)
4. Push to the branch (`git push origin feature/my-addon`)
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Links

- [ServerStats Website](https://serverstats.com)
- [ServerStats Documentation](https://serverstats.com/docs)
- [Discord Support](https://discord.gg/serverstats)
