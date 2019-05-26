package com.imyvm.ImyvmTown.Commands;

import com.earth2me.essentials.User;
import com.imyvm.ImyvmTown.Configuration;
import com.imyvm.ImyvmTown.Discord.WebHook;
import com.imyvm.ImyvmTown.Main;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.ShulkerBox;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BookMeta;

import java.io.File;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class Commands implements CommandExecutor {
    private Main plugin;

    public Commands(Main pl) {
        plugin = pl;
    }

    private Configuration conf = new Configuration();
    public Map<UUID, String> map = new HashMap<>();
    private Economy economy = Main.getEcononomy();

    private WebHook webHook = new WebHook();

    @Override
    public boolean onCommand(CommandSender sender, Command cmdObj, String label, String[] args) {

        Player player = (Player) sender;
        if (args.length <= 0) {
            plugin.guIs.OpenMainGUI(player, "§2村落主菜单");
            return true;
        }

        if (args[0].equalsIgnoreCase("list")) {

            plugin.guIs.OpenListGUI(player, "§2村落列表");
            return true;
        }
        if (args[0].equalsIgnoreCase("create")) {
            if (!map.containsKey(player.getUniqueId())) {
                return false;
            }
            if (!map.get(player.getUniqueId()).equalsIgnoreCase("Create")) {
                return false;
            }
            File file = new File(plugin.getDataFolder(), "applyInfo.yml");
            FileConfiguration data = conf.load(file);

            File playerinfo = plugin.playerinfo;
            FileConfiguration datainfo = conf.load(playerinfo);

            if (datainfo.getString("players." + player.getUniqueId().toString()) != null) {
                player.sendMessage("§4你已加入村落或正在申请村落！");
                return false;
            }

            HashMap<String, String> applyInfo = new HashMap<>();
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            applyInfo.put("ApplyPlayer", sender.getName());
            applyInfo.put("ApplyTime", timestamp.toString().substring(0, 19));
            data.createSection("towns." + player.getUniqueId().toString(), applyInfo);

            datainfo.set("players." + player.getUniqueId().toString(), player.getUniqueId().toString());

            conf.save(datainfo, playerinfo, "PlayerInfo.yml");
            conf.save(data, file, "applyInfo.yml");

            map.put(player.getUniqueId(), "name");
            sender.sendMessage("在对话框输入申请村落名字");
            return true;
        }

        if (args[0].equalsIgnoreCase("upload")) {
            if (map.containsKey(player.getUniqueId())) {
                if (!map.get(player.getUniqueId()).equalsIgnoreCase("book")) {
                    map.remove(player.getUniqueId());
                    player.sendMessage("§4You don't have the permission!");
                    return false;
                }
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item == null || item.getType().equals(Material.AIR)) {
                    player.sendMessage("§4请手持书写好的书！");
                    return false;
                }
                if (!(item.getItemMeta() instanceof BookMeta)) {
                    player.sendMessage("§4请手持书写好的书！");
                    return false;
                }
                if (!(item.getType().equals(Material.WRITABLE_BOOK) || item.getType().equals(Material.WRITTEN_BOOK))) {
                    player.sendMessage("§4请手持书写好的书！");
                    return false;
                }
                File file = plugin.applyInfo;
                FileConfiguration data = conf.load(file);
                ConfigurationSection config = data.getConfigurationSection("towns." + player.getUniqueId().toString());
                BookMeta bookMeta = (BookMeta) item.getItemMeta();
                String pages = String.join("\n", bookMeta.getPages());

                config.set("book", pages);
                config.set("getBooks", new ArrayList<>());
                conf.save(data, file, "applyInfo.yml");
                map.remove(player.getUniqueId());

                player.sendMessage("§c申请书上传成功");
            }
        }
        if (args[0].equalsIgnoreCase("banner")) {
            if (map.containsKey(player.getUniqueId())) {
                if (!map.get(player.getUniqueId()).equalsIgnoreCase("banner")) {
                    map.remove(player.getUniqueId());
                    player.sendMessage("§4You don't have the permission!");
                    return false;
                }
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item == null || item.getType().equals(Material.AIR)) {
                    player.sendMessage("§4请手持旗帜！");
                    return false;
                }
                if (!(item.getItemMeta() instanceof BannerMeta)) {
                    player.sendMessage("§4请手持旗帜！");
                    return false;
                }

                File file = plugin.data;
                FileConfiguration data = conf.load(file);

                File file1 = plugin.playerinfo;
                FileConfiguration data1 = conf.load(file1);

                ConfigurationSection config = data.getConfigurationSection("towns." + data1.
                        getString("players." + player.getUniqueId().toString()));
                config.set("Banner", item);
                conf.save(data, file, "data.yml");
                map.remove(player.getUniqueId());
                player.sendMessage("§c村落旗帜上传成功");
            }
        }

        if (args[0].equalsIgnoreCase("items")) {
            if (map.containsKey(player.getUniqueId())) {
                if (!map.get(player.getUniqueId()).equalsIgnoreCase("items")) {
                    map.remove(player.getUniqueId());
                    player.sendMessage("§4You don't have the permission！");
                    return false;
                }
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item == null || item.getType().equals(Material.AIR)) {
                    player.sendMessage("§4请手持要上传的物品！");
                    return false;
                }
                if (item.getItemMeta() instanceof BlockStateMeta) {
                    BlockStateMeta im = (BlockStateMeta) item.getItemMeta();
                    if (im.getBlockState() instanceof ShulkerBox) {
                        player.sendMessage("§4违禁物品！");
                        return false;
                    }
                    if (im.getBlockState() instanceof Chest) {
                        player.sendMessage("§4违禁物品！");
                        return false;
                    }
                }

                File file = plugin.data;
                FileConfiguration data = conf.load(file);

                File file1 = plugin.playerinfo;
                FileConfiguration data1 = conf.load(file1);

                ConfigurationSection config = data.getConfigurationSection("towns." + data1.
                        getString("players." + player.getUniqueId().toString()));
                config.set("Items", item);
                conf.save(data, file, "data.yml");
                map.remove(player.getUniqueId());
                player.sendMessage("§c村落吉祥物上传成功");
            }
        }
        if (args[0].equalsIgnoreCase("tplocation")) {
            if (map.containsKey(player.getUniqueId())) {
                double fee = plugin.config.getDouble("ChangeTpLocationFee");
                if (!economy.has(player, fee)) {
                    player.sendMessage("§4余额不足");
                    return false;
                }
                if (addlocation(player, args[0])) {
                    economy.withdrawPlayer(player, fee);
                    economy.depositPlayer(Bukkit.getServer().getOfflinePlayer(UUID.fromString(plugin.config.
                            getString("TradeUUID"))), fee);
                    player.sendMessage("§c村落传送点设置成功, 消费" + plugin.config.getDouble("ChangeTpLocationFee") + " D");
                    return true;
                }
            }
            return false;
        }

        if (args[0].equalsIgnoreCase("location1")) {
            if (map.containsKey(player.getUniqueId())) {
                if (addlocation(player, args[0])) {
                    player.sendMessage("§c村落范围点1设置成功");
                    return true;
                }
            }
            return false;
        }

        if (args[0].equalsIgnoreCase("location2")) {
            if (map.containsKey(player.getUniqueId())) {
                if (addlocation(player, args[0])) {
                    player.sendMessage("§c村落范围点2设置成功");
                    return true;
                }
            }
            return false;
        }

        if (args[0].equalsIgnoreCase("applyList")) {
            plugin.guIs.OpenApplyListGUI(player, "§2申请村落列表", "右击获取申请书，左击响应加入");
            return true;
        }
        if (args[0].equalsIgnoreCase("adminApplyList")) {
            if (!player.isOp()) {
                player.sendMessage("§4You don't have the permission!");
                return false;
            }
            plugin.guIs.OpenApplyListGUI(player, "§2申请村落管理列表", "右击获取申请书，左击审核");
            return true;
        }

        if (args[0].equalsIgnoreCase("admin")) {
            File playerinfo = plugin.playerinfo;
            FileConfiguration datainfo = conf.load(playerinfo);

            File data = plugin.data;
            FileConfiguration dataConf = conf.load(data);

            if (datainfo.getString("players." + player.getUniqueId().toString()) == null) {
                player.sendMessage("§4你未加入任何村落！");
                return false;
            }
            String stringUUID = datainfo.getString("players." + player.getUniqueId().toString());
            if (dataConf.getConfigurationSection("towns." + stringUUID) == null) {
                player.sendMessage("§4你的村落暂未创建成功！");
                return false;
            }
            if (!(stringUUID.equalsIgnoreCase(player.getUniqueId().toString()))) {
                if (!dataConf.getConfigurationSection("towns." + stringUUID).getStringList("assistant").
                        contains(player.getUniqueId().toString())) {
                    player.sendMessage("§4你不是村落管理人员！");
                    return false;
                }
            }
            if (dataConf.getConfigurationSection("towns." + stringUUID).getString("status")
                    .equalsIgnoreCase("§4locked")) {
                player.sendMessage("§4你的村落已被锁定");
                return false;
            }
            if (dataConf.getConfigurationSection("towns." + stringUUID).getString("status")
                    .equalsIgnoreCase("§4deleted")) {
                player.sendMessage("§4你的村落暂未创建成功！");
                return false;
            }
            plugin.guIs.OpenAdmin(player, dataConf.getConfigurationSection("towns." + stringUUID).
                    getString("name") + " §2村落管理系统", stringUUID);
            return true;
        }

        // 统计日活跃玩家
        if (args[0].equalsIgnoreCase("dailycheck")) {
            return check("dailyPlayers", 1000 * 60 * 60 * 24L);
        }

        // 统计周活跃玩家
        if (args[0].equalsIgnoreCase("Weeklycheck")) {
            if (!player.isOp()) {
                player.sendMessage("§4You don't have the permission!");
                return false;
            }
            return check("WeeklyPlayers", 1000 * 60 * 60 * 24 * 7L);
        }

        // 统计月活跃玩家
        if (args[0].equalsIgnoreCase("Monthlycheck")) {
            if (!player.isOp()) {
                player.sendMessage("§4You don't have the permission!");
                return false;
            }
            LocalDateTime ldt = LocalDateTime.now().minusMonths(1);
            ZonedDateTime zdt = ldt.atZone(ZoneId.of("Asia/Shanghai"));
            return check("MonthlyPlayers", zdt.toInstant().toEpochMilli());
        }

        // 剔除不活跃玩家
        if (args[0].equalsIgnoreCase("inactiveRemove")) {
            if (!player.isOp()) {
                player.sendMessage("§4You don't have the permission!");
                return false;
            }
            LocalDateTime ldt = LocalDateTime.now().minusMonths(3);
            ZonedDateTime zdt = ldt.atZone(ZoneId.of("Asia/Shanghai"));
            return RemoveInactivePlayers(zdt.toInstant().toEpochMilli());
        }

        // 锁定检查
        if (args[0].equalsIgnoreCase("lockcheck")) {
            if (!player.isOp()) {
                player.sendMessage("§4You don't have the permission!");
                return false;
            }
            if (!player.isOp()) {
                player.sendMessage("§4You don't have the permission!");
                return false;
            }
            return LockCheck();
        }

        // 解锁村落
        if (args[0].equalsIgnoreCase("unlock")) {
            if (!player.isOp()) {
                player.sendMessage("§4You don't have the permission!");
                return false;
            }
            if (unlockTown(args[1])) {
                player.sendMessage("§c解除锁定成功");
                return true;
            } else {
                player.sendMessage("§4解锁失败");
                return false;
            }
        }

        // 删除村落
        if (args[0].equalsIgnoreCase("delete")) {
            if (!player.isOp()) {
                player.sendMessage("§4You don't have the permission!");
                return false;
            }
            if (deleteTown(args[1])) {
                player.sendMessage("§c删除成功");
                return true;
            } else {
                player.sendMessage("§4删除失败");
                return false;
            }
        }

        // 重置奖励
        if (args[0].equalsIgnoreCase("resetReward")) {
            if (!player.isOp()) {
                player.sendMessage("§4You don't have the permission!");
                return false;
            }
            List<String> strings = new ArrayList<>();
            File file = plugin.rewards;
            FileConfiguration rewards = conf.load(file);
            rewards.set("rewards", strings);
            conf.save(rewards, file, "rewards.yml");
            return true;
        }
        return true;
    }

    private boolean check(String type, Long time) {
        File file = plugin.data;
        FileConfiguration data = conf.load(file);

        if (!data.isConfigurationSection("towns")) {
            return false;
        }
        Set<String> towns = data.getConfigurationSection("towns").getKeys(true);

        for (String a : towns) {
            if (a.contains(".")) {
                continue;
            }
            ConfigurationSection dataconf = data.getConfigurationSection("towns." + a);
            if (dataconf.getString("status").equalsIgnoreCase("§4locked") ||
                    dataconf.getString("status").equalsIgnoreCase("§4deleted")) {
                continue;
            }
            List<String> players = dataconf.getStringList("players");
            List<String> Players = new ArrayList<>();
            for (String s : players) {
                User user = plugin.ess.getUser(UUID.fromString(s));
                Long usertime = user.getLastLogin();
                Long currentTimeMillis = System.currentTimeMillis();
                if (currentTimeMillis - usertime < time) {
                    Players.add(s);
                }
            }
            dataconf.set(type, Players);
            conf.save(data, file, "data.yml");
        }
        System.console().printf(type + " success\n");
        return true;
    }

    private boolean RemoveInactivePlayers(Long time) {
        File file = plugin.data;
        FileConfiguration data = conf.load(file);

        File file1 = plugin.playerinfo;
        FileConfiguration playerinfo = conf.load(file1);

        if (!data.isConfigurationSection("towns")) {
            return false;
        }
        Set<String> towns = data.getConfigurationSection("towns").getKeys(true);

        for (String a : towns) {
            if (a.contains(".")) {
                continue;
            }
            if (data.getConfigurationSection("towns." + a).getString("status").
                    equalsIgnoreCase("§4locked") || data.
                    getConfigurationSection("towns." + a).getString("status").
                    equalsIgnoreCase("§4deleted")) {
                continue;
            }
            ConfigurationSection dataconf = data.getConfigurationSection("towns." + a);
            List<String> players = dataconf.getStringList("players");
            for (String s : players) {
                User user = plugin.ess.getUser(UUID.fromString(s));
                Long usertime = user.getLastLogin();
                Long currentTimeMillis = System.currentTimeMillis();
                if (currentTimeMillis - usertime > time) {
                    plugin.clickItem.removePlayer(players, s, user.getDisplayName(),
                            data, playerinfo, a);
                    System.console().printf(Bukkit.getServer().getOfflinePlayer(UUID.fromString(s)) + " removed");
                }
            }
            conf.save(data, file, "data.yml");
            conf.save(playerinfo, file1, "playerInfo.yml");
        }
        return true;
    }

    private boolean LockCheck() {
        File file = plugin.data;
        FileConfiguration data = conf.load(file);

        if (!data.isConfigurationSection("towns")) {
            return false;
        }
        Set<String> towns = data.getConfigurationSection("towns").getKeys(true);

        for (String a : towns) {
            if (a.contains(".")) {
                continue;
            }
            if (data.getConfigurationSection("towns." + a).getString("status").
                    equalsIgnoreCase("§4locked") || data.
                    getConfigurationSection("towns." + a).getString("status").
                    equalsIgnoreCase("§4deleted")) {
                continue;
            }
            ConfigurationSection dataconf = data.getConfigurationSection("towns." + a);
            if (dataconf.getStringList("players").size() <= 3) {
                data.getConfigurationSection("towns." + a).set("status", "§4locked");
            }
        }
        conf.save(data, file, "data.yml");
        return true;

    }

    private boolean unlockTown(String uuid) {
        File file = plugin.data;
        FileConfiguration data = conf.load(file);

        if (!data.isConfigurationSection("towns." + uuid)) {
            return false;
        }
        if (data.getConfigurationSection("towns." + uuid).getString("status")
                .equalsIgnoreCase("§4deleted")) {
            return false;
        }
        data.getConfigurationSection("towns." + uuid).set("status", "通过");
        conf.save(data, file, "data.yml");
        return true;
    }

    private boolean deleteTown(String uuid) {
        File file = plugin.data;
        FileConfiguration data = conf.load(file);

        File file1 = plugin.playerinfo;
        FileConfiguration playerinfo = conf.load(file1);

        if (!data.isConfigurationSection("towns." + uuid) || (playerinfo.getString("players." + uuid) == null)) {
            return false;
        }

        List<String> pl = new ArrayList<>();
        List<String> players = data.getConfigurationSection("towns." + uuid).getStringList("players");
        players.add(uuid);

        for (String a : players) {
            if (playerinfo.getString("players." + a).equalsIgnoreCase(uuid)) {
                playerinfo.set("players." + a, null);
            }
        }
        data.getConfigurationSection("towns." + uuid).set("players", pl);
        data.getConfigurationSection("towns." + uuid).set("assistant", pl);
        data.getConfigurationSection("towns." + uuid).set("status", "§4deleted");

        conf.save(data, file, "data.yml");
        conf.save(playerinfo, file1, "playerInfo.yml");
        return true;
    }

    private boolean addlocation(Player player, String type) {
        if (!map.get(player.getUniqueId()).equalsIgnoreCase(type)) {
            return false;
        }
        map.remove(player.getUniqueId());
        Location location = player.getLocation();

        File file = plugin.applyInfo;
        FileConfiguration applyinfo = conf.load(file);

        File file1 = plugin.playerinfo;
        FileConfiguration data1 = conf.load(file1);

        File file2 = plugin.data;
        FileConfiguration data = conf.load(file2);

        ConfigurationSection config = applyinfo.getConfigurationSection("towns." + data1.
                getString("players." + player.getUniqueId().toString()));

        ConfigurationSection dataconfig = data.getConfigurationSection("towns." + data1.
                getString("players." + player.getUniqueId().toString()));

        if (type.equalsIgnoreCase("tplocation")) {
            if (dataconfig.get("location1") == null || dataconfig.get("location2") == null) {
                player.sendMessage("§4你应该先设置村落范围!");
                return false;
            }
            Location location1 = (Location) dataconfig.get("location1");
            Location location2 = (Location) dataconfig.get("location2");
            if (!locationIsInRegion(location, location1, location2)) {
                player.sendMessage("§4你的传送点应该设置在村落范围内!");
                return false;
            }
            dataconfig.set("tplocation", location);
            conf.save(data, file2, "data.yml");
            return true;
        }
        if (type.equalsIgnoreCase("location1") || type.equalsIgnoreCase("location2")) {
            config.set("locationstatus", "待审核");
            Location location1 = (Location) config.get("location1");
            Location location2 = (Location) config.get("location2");
            if (type.equalsIgnoreCase("location2") && location1 != null && location2 != null) {
                String message = "[竹萌村落] 你有新的村落范围待审核: " + config.getString("name") +
                        "\n[范围] World:" + location1.getWorld() + "X1:" + location1.getBlockX() + ",Z1:" + location1.getBlockZ() + "~World:" + location2.getWorld() + "X2:" + location2.getBlockX() + ",Z2:" + location2.getBlockZ();
                CompletableFuture.runAsync(() -> {
                    webHook.sendMessage(plugin.config.getString("Token-ID"),
                            ChatColor.stripColor(message.replace("\n", "\\n")));
                });
            }
        }
        config.set(type, location);
        conf.save(applyinfo, file, "applyInfo.yml");
        return true;
    }

    private boolean locationIsInRegion(Location loc, Location firstPoint, Location secondPoint) {
        UUID worldUniqueId = firstPoint.getWorld().getUID();

        double maxX = Math.max(firstPoint.getX(), secondPoint.getX());
        double maxY = Math.max(firstPoint.getY(), secondPoint.getY());

        double minX = Math.min(firstPoint.getX(), secondPoint.getX());
        double minY = Math.min(firstPoint.getY(), secondPoint.getY());

        return loc.getWorld().getUID().equals(worldUniqueId)
                && loc.getX() > minX && loc.getX() < maxX
                && loc.getY() > minY && loc.getY() < maxY;
    }

}
