package com.imyvm.ImyvmTown.Regions;

import com.imyvm.ImyvmTown.Main;
import org.bukkit.Location;
import org.bukkit.World;

public class Region {

    private Main plugin;

    public Region(Main pl) {
        plugin = pl;
    }

    public static final String __DEFAULT__ = "__DEFAULT__";
    public World world;
    public int minX;
    public int minZ;
    public int maxX;
    public int maxZ;
    public String name;
    public String uuid;

    public World getWorld() {
        return world;
    }

    public int getMinX() {
        return minX;
    }

    public int getMinZ() {
        return minZ;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMaxZ() {
        return maxZ;
    }

    public String getName() {
        return name;
    }

    public String getUuid() {
        return uuid;
    }

    public boolean contains(Location location) {
        return contains(location.getBlockX(), location.getBlockZ());
    }

    private boolean contains(int x, int z) {
        return x >= minX && x <= maxX &&
                z >= minZ && z <= maxZ;
    }
}
