package yourpackage;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Main extends JavaPlugin implements Listener {

    private List<PotionEffectType> effectPool = new ArrayList<>();
    private File playerDataFolder;

    @Override
    public void onEnable() {
        getLogger().info("EffectPoolPlugin has been enabled!");
        loadConfig();
        loadPlayerData();
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        getLogger().info("EffectPoolPlugin has been disabled!");
        savePlayerData();
    }

    private void loadConfig() {
        saveDefaultConfig();
        FileConfiguration config = getConfig();

        effectPool.clear();
        if (config.contains("effect_pool")) {
            List<String> effectNames = config.getStringList("effect_pool");
            for (String effectName : effectNames) {
                PotionEffectType effectType = PotionEffectType.getByName(effectName);
                if (effectType != null) {
                    effectPool.add(effectType);
                } else {
                    getLogger().warning("Invalid effect name in effect_pool: " + effectName);
                }
            }
        } else {
            getLogger().warning("No effect_pool defined in config.yml. Defaulting to REGENERATION.");
            effectPool.add(PotionEffectType.REGENERATION);
        }
    }

    private void loadPlayerData() {
        playerDataFolder = new File(getDataFolder(), "playerdata");
        if (!playerDataFolder.exists()) {
            playerDataFolder.mkdirs();
        }
    }

    private void savePlayerData() {
        // Nothing to save in this example, but you might need it for future features
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        File playerFile = new File(playerDataFolder, playerUUID + ".yml");

        if (!playerFile.exists()) {
            // Player has not received effect before
            giveRandomEffect(player);
            createPlayerFile(playerFile);
        }
    }

    private void giveRandomEffect(Player player) {
        if (effectPool.isEmpty()) {
            getLogger().warning("No effects in the pool.");
            return;
        }

        PotionEffectType effectType = getRandomEffect();
        if (effectType != null) {
            int duration = getConfig().getInt("duration", 999999);
            int amplifier = getConfig().getInt("amplifier", 1);

            player.addPotionEffect(new PotionEffect(effectType, duration, amplifier, true, false));
            player.sendMessage(ChatColor.GREEN + "You've been given a " + effectType.getName() + " effect!");
        }
    }

    private PotionEffectType getRandomEffect() {
        return effectPool.get((int) (Math.random() * effectPool.size()));
    }

    private void createPlayerFile(File playerFile) {
        try {
            playerFile.createNewFile();
        } catch (IOException e) {
            getLogger().warning("Failed to create player data file for " + playerFile.getName());
        }
    }
}
