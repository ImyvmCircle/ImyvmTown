package com.imyvm.ImyvmTown.Listener;

import com.imyvm.ImyvmTown.Configuration;
import com.imyvm.ImyvmTown.Main;
import com.imyvm.ImyvmTown.Regions.Region;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.io.File;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;


public class RegionListener implements Listener {

    private Main plugin;
    private HashMap<UUID, String> currentRegion = new HashMap<>();
    private Configuration configuration = new Configuration();

    public RegionListener(Main pl) {
        plugin = pl;
        plugin.getServer().getPluginManager().registerEvents(this, pl);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Region region1 = getRegion(event.getTo());
        Region region2 = getRegion(event.getFrom());
        if (region1.uuid == null && region2.uuid == null) {
            return;
        }
        if (region1.uuid != null && region2.uuid != null) {
            return;
        }
        if (region1.uuid != null) {
            // TODO Message
            player.sendTitle("进入" + region1.name, "", 10, 70, 20);
        }
        if (region2.uuid != null) {
            // TODO Message
            player.sendTitle("离开" + region2.name, "", 10, 70, 20);
        }
    }

    private Region getRegion(Location location) {
        Region region = new Region(plugin);
        File file = new File(plugin.getDataFolder(), "data.yml");
        FileConfiguration data = configuration.load(file);
        Set<String> towns = data.getConfigurationSection("towns").getKeys(true);

        for (String a : towns) {
            if (a.contains(".")) {
                continue;
            }
            ConfigurationSection conf = data.getConfigurationSection("towns." + a);
            if (conf.get("location1") == null || conf.get("location2") == null) {
                continue;
            }
            if (conf.getString("status").equalsIgnoreCase("§4deleted")) {
                continue;
            }

            Location location3 = (Location) conf.get("location1");
            Location location4 = (Location) conf.get("location2");
            if (!location.getWorld().equals(location3.getWorld()) &&
                    !location.getWorld().equals(location4.getWorld())) {
                continue;
            }
            int minX1 = Math.min(location3.getBlockX(), location4.getBlockX());
            int minZ1 = Math.min(location3.getBlockZ(), location4.getBlockZ());
            int maxX1 = Math.max(location3.getBlockX(), location4.getBlockX());
            int maxZ1 = Math.max(location3.getBlockZ(), location4.getBlockZ());
            if (location.getBlockX() >= minX1 && location.getBlockX() <= maxX1 &&
                    location.getBlockZ() >= minZ1 && location.getBlockZ() <= maxZ1) {
                region.world = location.getWorld();
                region.minX = minX1;
                region.minZ = minZ1;
                region.maxX = maxX1;
                region.maxZ = maxZ1;
                region.name = conf.getString("name");
                region.uuid = a;
                break;
            }
        }
        return region;
    }
}
