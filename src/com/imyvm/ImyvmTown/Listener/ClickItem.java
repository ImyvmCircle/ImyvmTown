package com.imyvm.ImyvmTown.Listener;

import com.earth2me.essentials.User;
import com.imyvm.ImyvmTown.Configuration;
import com.imyvm.ImyvmTown.Discord.WebHook;
import com.imyvm.ImyvmTown.Main;
import com.imyvm.ImyvmTown.Utils.JSONMessage;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.File;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;


public class ClickItem implements Listener {

    private Main plugin;

    public ClickItem(Main pl) {
        plugin = pl;
        plugin.getServer().getPluginManager().registerEvents(this, pl);
    }

    private Configuration conf = new Configuration();
    private WebHook webHook = new WebHook();
    private Economy economy = Main.getEcononomy();

    @EventHandler
    public void onPlayerClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        String tile = player.getOpenInventory().getTitle();
        if (tile.equalsIgnoreCase("§2申请村落列表")) {
            if (event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR)) {
                return;
            }
            if (!event.getClickedInventory().getTitle().equalsIgnoreCase("§2申请村落列表")) {
                event.setCancelled(true);
                return;
            }

            File file = new File(plugin.getDataFolder(), "applyInfo.yml");
            FileConfiguration data = conf.load(file);

            File playerinfo = plugin.playerinfo;
            FileConfiguration datainfo = conf.load(playerinfo);

            ItemStack itemStack = event.getCurrentItem();
            ItemMeta itemMeta = itemStack.getItemMeta();
            List<String> lores = itemMeta.getLore();
            String uuidString = lores.get(0);

            if (ChatColor.stripColor(itemMeta.getDisplayName()).equalsIgnoreCase("返回")) {
                plugin.guIs.OpenMainGUI(player, "§2村落主菜单");
                event.setCancelled(true);
                return;
            }

            ConfigurationSection config = data.getConfigurationSection("towns." + uuidString);

            if (event.getClick().isLeftClick()) {

                if (config.getString("name") == null || config.getString("book") == null) {
                    player.sendMessage("§4村落信息不完整，暂无法响应");
                    event.setCancelled(true);
                    return;
                }
                if (!config.getString("status", "").isEmpty()) {
                    player.sendMessage("§4村落申请已结束，请前往村落列表申请加入！");
                    event.setCancelled(true);
                    return;
                }
                if (datainfo.getString("players." + player.getUniqueId().toString()) != null) {
                    player.sendMessage("§4你已加入或正在申请村落，无法响应！");
                    event.setCancelled(true);
                    return;
                }
                List<String> players = config.getStringList("players");
                players.add(player.getUniqueId().toString());
                config.set("players", players);

                datainfo.set("players." + player.getUniqueId().toString(), uuidString);

                if (players.size() == 4) {
                    String pages = config.getString("book");
                    String message = "**[村落申请]: **" + config.getString("name") + "\\n"
                            + "**[申请人]: **" + config.getString("ApplyPlayer") + "\\n"
                            + "**[村落简介]: **" + config.getString("range") + "\\n"
                            + "**[申请书如下]: **\\n" + pages.replace("\n", "\\n");

                    CompletableFuture.runAsync(() -> {
                        webHook.sendMessage(plugin.config.getString("Token-ID"),
                                ChatColor.stripColor(message));
                    });
                }

                conf.save(data, file, "applyInfo.yml");
                conf.save(datainfo, playerinfo, "PlayerInfo.yml");
                player.closeInventory();
                plugin.guIs.OpenApplyListGUI(player, "§2申请村落列表", "右击获取申请书，左击响应加入");
                event.setCancelled(true);
                return;
            }
            if (event.getClick().isRightClick()) {
                if (config.getString("book", "").isEmpty()) {
                    player.sendMessage("暂无申请书");
                    event.setCancelled(true);
                    return;
                }
                List<String> getbookplayers = config.getStringList("getBooks");
                if (getbookplayers.contains(player.getName())) {
                    player.sendMessage("你已领取过申请书");
                    event.setCancelled(true);
                    return;
                }
                getbookplayers.add(player.getName());
                config.set("getBooks", getbookplayers);
                conf.save(data, file, "applyInfo.yml");

                ItemStack itemStack1 = new ItemStack(Material.WRITTEN_BOOK);
                BookMeta bookMeta = (BookMeta) itemStack1.getItemMeta();
                bookMeta.setAuthor(config.getString("ApplyPlayer"));
                bookMeta.addPage(config.getString("book", ""));
                bookMeta.setTitle(config.getString("name") + " 申请书");
                itemStack1.setItemMeta(bookMeta);
                player.getInventory().addItem(itemStack1);
                player.updateInventory();

                event.setCancelled(true);
                return;
            }
            event.setCancelled(true);
        }

        if (tile.equalsIgnoreCase("§2申请村落管理列表")) {
            if (event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR)) {
                return;
            }
            if (!event.getClickedInventory().getTitle().equalsIgnoreCase("§2申请村落管理列表")) {
                event.setCancelled(true);
                return;
            }

            File file = new File(plugin.getDataFolder(), "applyInfo.yml");
            FileConfiguration data = conf.load(file);

            ItemStack itemStack = event.getCurrentItem();
            ItemMeta itemMeta = itemStack.getItemMeta();
            List<String> lores = itemMeta.getLore();
            String uuidString = lores.get(0);

            if (ChatColor.stripColor(itemMeta.getDisplayName()).equalsIgnoreCase("返回")) {
                plugin.guIs.OpenMainGUI(player, "§2村落主菜单");
                event.setCancelled(true);
                return;
            }

            ConfigurationSection config = data.getConfigurationSection("towns." + uuidString);

            if (event.getClick().isLeftClick()) {
                if (config.getString("status") == null &&
                        config.getString("locationstatus") == null) {
                    plugin.guIs.OpenApplyAdminGUI(player, config.getString("name") + " §2村落申请审核", uuidString);
                    event.setCancelled(true);
                    return;
                }
                if (config.getString("locationstatus") == null) {
                    player.sendMessage("§4该村落无审核项目！");
                    event.setCancelled(true);
                    return;
                }
                if (config.getString("locationstatus").equalsIgnoreCase("待审核")) {
                    plugin.guIs.OpenApplyAdminGUI(player, config.getString("name") + " §2村落申请审核", uuidString);
                    event.setCancelled(true);
                    return;
                }
                player.sendMessage("§4该村落无审核项目！");
                event.setCancelled(true);
                return;
            }
            if (event.getClick().isRightClick()) {
                if (config.getString("book", "").isEmpty()) {
                    player.sendMessage("暂无申请书");
                    event.setCancelled(true);
                    return;
                }
                ItemStack itemStack1 = new ItemStack(Material.WRITTEN_BOOK);
                BookMeta bookMeta = (BookMeta) itemStack1.getItemMeta();
                bookMeta.setAuthor(config.getString("ApplyPlayer"));
                bookMeta.addPage(config.getString("book", ""));
                bookMeta.setTitle(config.getString("name") + " 申请书");
                itemStack1.setItemMeta(bookMeta);
                player.getInventory().addItem(itemStack1);
                player.updateInventory();

                event.setCancelled(true);
                return;
            }
            event.setCancelled(true);
        }

        if (tile.endsWith("§2村落申请审核")) {
            if (event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR)) {
                return;
            }
            if (!event.getClickedInventory().getTitle().endsWith("§2村落申请审核")) {
                event.setCancelled(true);
                return;
            }
            File file = plugin.applyInfo;
            FileConfiguration data = conf.load(file);

            File towns = plugin.data;
            FileConfiguration dataconf = conf.load(towns);

            ItemStack itemStack = event.getCurrentItem();
            ItemMeta itemMeta = itemStack.getItemMeta();
            List<String> lores = itemMeta.getLore();

            if (lores.contains("点击返回")) {
                plugin.guIs.OpenApplyListGUI(player, "§2申请村落管理列表", "右击获取申请书，左击审核");
                event.setCancelled(true);
                return;
            }

            Inventory inventory1 = event.getClickedInventory();
            ItemStack itemStack1 = inventory1.getItem(0);
            ItemMeta itemMeta1 = itemStack1.getItemMeta();
            String stringUUID = itemMeta1.getLore().get(0);

            ConfigurationSection config = data.getConfigurationSection("towns." + stringUUID);
            ConfigurationSection dataconfig = dataconf.getConfigurationSection("towns." + stringUUID);

            if (ChatColor.stripColor(itemMeta.getDisplayName()).equalsIgnoreCase("通过")) {
                if (config.getString("status") == null) {
                    dataconf.set("towns." + stringUUID, data.getConfigurationSection("towns." + stringUUID));
                    config.set("status", itemMeta.getDisplayName());
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    dataconf.getConfigurationSection("towns." + stringUUID).set("Time", timestamp.toString().substring(0, 19));
                    dataconf.getConfigurationSection("towns." + stringUUID).set("ApplyPlayers", new ArrayList<>());
                    conf.save(dataconf, towns, "data.yml");
                    conf.save(data, file, "applyInfo.yml");
                    plugin.guIs.OpenApplyListGUI(player, "§2申请村落管理列表", "右击获取申请书，左击审核");
                    event.setCancelled(true);
                    return;
                }
                if (config.getString("status").equalsIgnoreCase("§2通过")) {
                    if (config.getString("locationstatus").equalsIgnoreCase("待审核")) {
                        config.set("locationstatus", itemMeta.getDisplayName());
                        Location location1 = (Location) config.get("location1");
                        Location location2 = (Location) config.get("location2");
                        dataconfig.set("location1", location1);
                        dataconfig.set("location2", location2);
                        conf.save(data, file, "applyInfo.yml");
                        conf.save(dataconf, towns, "data.yml");
                        player.sendMessage("§c已同意该村落范围设置！");
                        plugin.guIs.OpenApplyListGUI(player, "§2申请村落管理列表", "右击获取申请书，左击审核");
                        event.setCancelled(true);
                        return;
                    }
                }

            }
            if (ChatColor.stripColor(itemMeta.getDisplayName()).equalsIgnoreCase("拒绝")) {
                if (config.getString("status") == null) {
                    File playerinfo = plugin.playerinfo;
                    FileConfiguration datainfo = conf.load(playerinfo);
                    config.set("status", itemMeta.getDisplayName());

                    Set<String> players = datainfo.getConfigurationSection("players").getKeys(true);
                    for (String a : players) {
                        if (datainfo.getString("players." + a).equalsIgnoreCase(stringUUID)) {
                            datainfo.set("players." + a, null);
                        }
                    }
                    conf.save(datainfo, playerinfo, "data.yml");
                    plugin.guIs.OpenApplyListGUI(player, "§2申请村落管理列表", "右击获取申请书，左击审核");
                    event.setCancelled(true);
                    return;
                }
                if (config.getString("status").equalsIgnoreCase("§2通过")) {
                    if (config.getString("locationstatus").equalsIgnoreCase("待审核")) {
                        config.set("locationstatus", itemMeta.getDisplayName());
                        conf.save(data, file, "applyInfo.yml");
                        player.sendMessage("§c已拒绝该村落范围设置！");
                        plugin.guIs.OpenApplyListGUI(player, "§2申请村落管理列表", "右击获取申请书，左击审核");
                        event.setCancelled(true);
                        return;
                    }
                }

            }
            event.setCancelled(true);
        }

        if (tile.endsWith("§2村落管理系统")) {
            if (event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR)) {
                event.setCancelled(true);
                return;
            }

            if (!event.getClickedInventory().getTitle().endsWith("§2村落管理系统")) {
                event.setCancelled(true);
                return;
            }

            ItemStack itemStack = event.getCurrentItem();
            ItemMeta itemMeta = itemStack.getItemMeta();
            List<String> lores = itemMeta.getLore();

            if (ChatColor.stripColor(itemMeta.getDisplayName()).equalsIgnoreCase("返回")) {
                plugin.guIs.OpenMainGUI(player, "§2村落主菜单");
                event.setCancelled(true);
                return;
            }

            Inventory inventory1 = event.getClickedInventory();
            Map<UUID, String> map = plugin.commands.map;

            if (lores.contains("点击修改")) {
                if (ChatColor.stripColor(itemMeta.getDisplayName()).equalsIgnoreCase("联系方式")) {
                    map.put(player.getUniqueId(), "contact");
                    player.sendMessage("在对话框输入村落联系方式");
                    player.closeInventory();
                    event.setCancelled(true);
                    return;
                }
                if (ChatColor.stripColor(itemMeta.getDisplayName()).equalsIgnoreCase("加入条件")) {
                    map.put(player.getUniqueId(), "requirements");
                    player.sendMessage("在对话框输入村落加入条件");
                    player.closeInventory();
                    event.setCancelled(true);
                    return;
                }
                if (ChatColor.stripColor(itemMeta.getDisplayName()).equalsIgnoreCase("村落口号")) {
                    map.put(player.getUniqueId(), "declaration");
                    player.sendMessage("在对话框输入村落口号");
                    player.closeInventory();
                    event.setCancelled(true);
                    return;
                }
                if (ChatColor.stripColor(itemMeta.getDisplayName()).equalsIgnoreCase("村落旗帜")) {
                    map.put(player.getUniqueId(), "banner");
                    JSONMessage.create("请手持旗帜并点击")
                            .then("这里").color(ChatColor.DARK_RED).runCommand("/itown banner").then("上传").send(player);
                    player.closeInventory();
                    event.setCancelled(true);
                    return;
                }
                if (ChatColor.stripColor(itemMeta.getDisplayName()).equalsIgnoreCase("吉祥物")) {
                    map.put(player.getUniqueId(), "items");
                    JSONMessage.create("请手持吉祥物并点击")
                            .then("这里").color(ChatColor.DARK_RED).runCommand("/itown items").then("上传").send(player);
                    player.closeInventory();
                    event.setCancelled(true);
                    return;
                }
                if (ChatColor.stripColor(itemMeta.getDisplayName()).equalsIgnoreCase("村落传送点")) {
                    map.put(player.getUniqueId(), "tplocation");
                    JSONMessage.create("请点击")
                            .then("这里").color(ChatColor.DARK_RED).runCommand("/itown tplocation").
                            then("修改脚下位置为村落传送点,花费 " + plugin.config.
                                    getDouble("ChangeTpLocationFee") + " D").send(player);
                    player.closeInventory();
                    event.setCancelled(true);
                    return;
                }
                event.setCancelled(true);
                return;
            }

            if (ChatColor.stripColor(itemMeta.getDisplayName()).equalsIgnoreCase("助理")) {
                if (!player.getUniqueId().toString().equalsIgnoreCase(inventory1.
                        getItem(10).getItemMeta().getLore().get(0))) {
                    player.sendMessage("只有村长才能设置/修改助理！");
                    event.setCancelled(true);
                    return;
                }
                plugin.guIs.OpenPlayerManagerGUI(player, "§2助理设置",
                        inventory1.getItem(10).getItemMeta().getLore().get(0), "点击设置");
            }

            if (ChatColor.stripColor(itemMeta.getDisplayName()).equalsIgnoreCase("人员管理")) {
                plugin.guIs.OpenPlayerManagerGUI(player, "§2人员管理",
                        inventory1.getItem(10).getItemMeta().getLore().get(0), "点击移除");
                event.setCancelled(true);
                return;
            }

            if (ChatColor.stripColor(itemMeta.getDisplayName()).equalsIgnoreCase("村落范围")) {
                if (event.getClick().isLeftClick()) {
                    map.put(player.getUniqueId(), "location1");
                    JSONMessage.create("请点击")
                            .then("这里").color(ChatColor.DARK_RED).runCommand("/itown location1").
                            then("修改脚下位置为村落范围点1").send(player);
                    player.closeInventory();
                    event.setCancelled(true);
                    return;
                }
                if (event.getClick().isRightClick()) {
                    map.put(player.getUniqueId(), "location2");
                    JSONMessage.create("请点击")
                            .then("这里").color(ChatColor.DARK_RED).runCommand("/itown location2").
                            then("修改脚下位置为村落范围点2").send(player);
                    player.closeInventory();
                    event.setCancelled(true);
                    return;
                }
                event.setCancelled(true);
                return;

            }

            if (lores.contains("点击查看") && ChatColor.stripColor(itemMeta.getDisplayName()).
                    equalsIgnoreCase("申请审核")) {
                plugin.guIs.OpenApplysGUI(player, "§2申请审核", inventory1.
                        getItem(10).getItemMeta().getLore().get(0));
                event.setCancelled(true);
                return;
            }
            event.setCancelled(true);
        }

        if (tile.equalsIgnoreCase("§2村落列表")) {
            if (event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR)) {
                event.setCancelled(true);
                return;
            }

            if (!event.getClickedInventory().getTitle().equalsIgnoreCase("§2村落列表")) {
                event.setCancelled(true);
                return;
            }

            File playerinfo = plugin.playerinfo;
            FileConfiguration datainfo = conf.load(playerinfo);

            File data = plugin.data;
            FileConfiguration dataConf = conf.load(data);

            ItemStack itemStack = event.getCurrentItem();
            ItemMeta itemMeta = itemStack.getItemMeta();
            List<String> lores = itemMeta.getLore();

            if (ChatColor.stripColor(itemMeta.getDisplayName()).equalsIgnoreCase("返回")) {
                plugin.guIs.OpenMainGUI(player, "§2村落主菜单");
                event.setCancelled(true);
                return;
            }

            if (datainfo.getString("players." + player.getUniqueId().toString()) == null) {
                List<String> ApplyPlayers = new ArrayList<>();
                if (!(dataConf.getConfigurationSection("towns." + lores.get(0)).getStringList("ApplyPlayers") == null)) {
                    ApplyPlayers = dataConf.getConfigurationSection("towns." + lores.get(0)).getStringList("ApplyPlayers");
                }
                if (ApplyPlayers.contains(player.getUniqueId().toString())) {
                    player.sendMessage("你已经提交过该申请！");
                    event.setCancelled(true);
                    return;
                }
                ApplyPlayers.add(player.getUniqueId().toString());
                dataConf.getConfigurationSection("towns." + lores.get(0)).set("ApplyPlayers", ApplyPlayers);
                conf.save(dataConf, data, "data.yml");
                player.sendMessage("申请提交成功");

                if (Bukkit.getOfflinePlayer(UUID.fromString(lores.get(0))).isOnline()) {
                    Bukkit.getOfflinePlayer(UUID.fromString(lores.get(0))).getPlayer().
                            sendMessage("[§2竹萌村落§r]" + player.getDisplayName() + "申请加入村落");
                } else {
                    sendMail("[竹萌村落]" + player.getDisplayName() + "申请加入村落", UUID.fromString(lores.get(0)));
                }

                event.setCancelled(true);
                return;
            }
            if (datainfo.getString("players." + player.getUniqueId().toString()).equalsIgnoreCase(lores.get(0))) {
                plugin.guIs.OpenPlayerGUI(player, dataConf.getConfigurationSection("towns." +
                        lores.get(0)).getString("name") + " §2村落系统", lores.get(0));
                event.setCancelled(true);
                return;
            }
            player.sendMessage("你不是该村落成员");
            event.setCancelled(true);
        }

        if (tile.equalsIgnoreCase("§2申请审核")) {
            if (event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR)) {
                event.setCancelled(true);
                return;
            }

            if (!event.getClickedInventory().getTitle().equalsIgnoreCase("§2申请审核")) {
                event.setCancelled(true);
                return;
            }

            ItemStack itemStack = event.getCurrentItem();
            ItemMeta itemMeta = itemStack.getItemMeta();

            if (ChatColor.stripColor(itemMeta.getDisplayName()).equalsIgnoreCase("返回")) {
                player.performCommand("itown admin");
                event.setCancelled(true);
                return;
            }

            plugin.guIs.OpenApplyPlayersAdminGUI(player, "§2人员审核", itemStack);
            event.setCancelled(true);

        }

        if (tile.endsWith("§2人员审核")) {
            if (event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR)) {
                return;
            }
            if (!event.getClickedInventory().getTitle().endsWith("§2人员审核")) {
                event.setCancelled(true);
                return;
            }

            File file = plugin.data;
            FileConfiguration dataConf = conf.load(file);

            File file1 = plugin.playerinfo;
            FileConfiguration playerinfo = conf.load(file1);

            ItemStack itemStack = event.getCurrentItem();
            ItemMeta itemMeta = itemStack.getItemMeta();
            List<String> lores = itemMeta.getLore();

            Inventory inventory1 = event.getClickedInventory();
            String StringUUID = playerinfo.getString("players." + player.getUniqueId().toString());

            if (lores.contains("Imyvm_Admin")) {
                ItemStack itemStack1 = inventory1.getItem(0);
                ItemMeta itemMeta1 = itemStack1.getItemMeta();

                SkullMeta meta = (SkullMeta) itemMeta1;
                OfflinePlayer player1 = meta.getOwningPlayer();

                if (ChatColor.stripColor(itemMeta.getDisplayName()).equalsIgnoreCase("通过")) {
                    List<String> applyPlayers = dataConf.getConfigurationSection("towns." + StringUUID).
                            getStringList("ApplyPlayers");
                    List<String> players = dataConf.getConfigurationSection("towns." + StringUUID).
                            getStringList("players");

                    applyPlayers.remove(player1.getUniqueId().toString());
                    if (!(playerinfo.getString("players." + player1.getUniqueId().toString()) == null)) {
                        player.sendMessage("该玩家已经加入了村落或正在创建村落");
                        dataConf.getConfigurationSection("towns." + StringUUID).set("ApplyPlayers", applyPlayers);
                        conf.save(dataConf, file, "data.yml");
                        plugin.guIs.OpenApplysGUI(player, "§2申请审核", StringUUID);
                        event.setCancelled(true);
                        return;
                    }
                    players.add(player1.getUniqueId().toString());
                    dataConf.getConfigurationSection("towns." + StringUUID).set("players", players);
                    dataConf.getConfigurationSection("towns." + StringUUID).set("ApplyPlayers", applyPlayers);
                    playerinfo.set("players." + player1.getUniqueId().toString(), StringUUID);
                    conf.save(dataConf, file, "data.yml");
                    conf.save(playerinfo, file1, "PlayerInfo.yml");
                    player.sendMessage("已同意");
                    plugin.guIs.OpenApplysGUI(player, "§2申请审核", StringUUID);
                    event.setCancelled(true);
                    return;
                }
                if (ChatColor.stripColor(itemMeta.getDisplayName()).equalsIgnoreCase("拒绝")) {
                    List<String> applyPlayers = dataConf.getConfigurationSection("towns." + StringUUID).
                            getStringList("ApplyPlayers");
                    applyPlayers.remove(player1.getUniqueId().toString());
                    dataConf.getConfigurationSection("towns." + StringUUID).set("ApplyPlayers", applyPlayers);
                    conf.save(dataConf, file, "data.yml");
                    player.sendMessage("已拒绝");
                    plugin.guIs.OpenApplysGUI(player, "§2申请审核", StringUUID);
                    event.setCancelled(true);
                    return;
                }
            }
            if (lores.contains("点击返回")) {
                plugin.guIs.OpenApplysGUI(player, "§2申请审核", StringUUID);
            }
            event.setCancelled(true);
        }

        if (tile.endsWith("§2村落系统")) {
            if (event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR)) {
                event.setCancelled(true);
                return;
            }

            if (!event.getClickedInventory().getTitle().endsWith("§2村落系统")) {
                event.setCancelled(true);
                return;
            }

            File data = plugin.data;
            FileConfiguration dataConf = conf.load(data);

            File playerinfo = plugin.playerinfo;
            FileConfiguration datainfo = conf.load(playerinfo);

            String StringUUID = datainfo.getString("players." + player.getUniqueId().toString());

            ItemStack itemStack = event.getCurrentItem();
            ItemMeta itemMeta = itemStack.getItemMeta();

            if (ChatColor.stripColor(itemMeta.getDisplayName()).equalsIgnoreCase("传送中心")) {
                if (dataConf.getConfigurationSection("towns." + StringUUID).get("tplocation") == null) {
                    player.sendMessage("你的村落暂未设置传送点！");
                    event.setCancelled(true);
                    return;
                }
                Location location = (Location) dataConf.getConfigurationSection("towns." + StringUUID).get("tplocation");
                player.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
                player.sendMessage("传送成功");
                event.setCancelled(true);
                return;
            }
            if (ChatColor.stripColor(itemMeta.getDisplayName()).equalsIgnoreCase("每日奖励")) {
                File re = plugin.rewards;
                FileConfiguration reinfo = conf.load(re);
                if (reinfo.getStringList("rewards").
                        contains(player.getUniqueId().toString())) {
                    player.sendMessage("你今天已领取过奖励！");
                    event.setCancelled(true);
                    return;
                }

                int base = dataConf.getConfigurationSection("towns." + StringUUID).
                        getStringList("dailyPlayers").size() + 1;
                int total = dataConf.getConfigurationSection("towns." + StringUUID).
                        getStringList("players").size() + 1;

                double d = Math.random();
                double random = (d * 0.8 + 0.6);
                double reward = (float) base / total * plugin.config.getDouble("RewardBase") * random;
                DecimalFormat df = new DecimalFormat("0.00");
                List<String> rewards = reinfo.getStringList("rewards");
                rewards.add(player.getUniqueId().toString());
                reinfo.set("rewards", rewards);
                conf.save(reinfo, re, "rewards.yml");

                economy.withdrawPlayer(Bukkit.getOfflinePlayer(UUID.fromString(plugin.config.
                        getString("TradeUUID"))), reward);
                economy.depositPlayer(player, reward);

                player.sendMessage("获得今日奖励: " + df.format(reward) + " D");
                event.setCancelled(true);
                return;
            }
            if (ChatColor.stripColor(itemMeta.getDisplayName()).equalsIgnoreCase("徽章领取")) {
                Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(),
                        "hm give " + player.getName() + " " +
                                dataConf.getConfigurationSection("towns." + StringUUID).get("name"));
            }
            if (ChatColor.stripColor(itemMeta.getDisplayName()).equalsIgnoreCase("退出村落")) {
                if (player.getUniqueId().toString().equalsIgnoreCase(StringUUID)) {
                    player.sendMessage("村长不能退出！");
                    event.setCancelled(true);
                    return;
                }
                List<String> players = dataConf.getConfigurationSection("towns." + StringUUID).
                        getStringList("players");

                Map<UUID, String> map = plugin.commands.map;

                if (map.containsKey(player.getUniqueId())) {
                    if (map.get(player.getUniqueId()).equalsIgnoreCase(player.getUniqueId().toString())) {
                        removePlayer(players, (OfflinePlayer) player, dataConf, datainfo, StringUUID);
                        map.remove(player.getUniqueId());
                        conf.save(dataConf, data, "data.yml");
                        conf.save(datainfo, playerinfo, "playerInfo.yml");
                        player.closeInventory();
                        event.setCancelled(true);
                        return;
                    }
                    map.remove(player.getUniqueId());
                }

                map.put(player.getUniqueId(), player.getUniqueId().toString());
                player.sendMessage("请再点击一次确定退出");
                plugin.guIs.OpenPlayerGUI(player, dataConf.getConfigurationSection("towns." + StringUUID)
                        .getString("name") + " 村落系统", StringUUID);
                event.setCancelled(true);

            }

            if (ChatColor.stripColor(itemMeta.getDisplayName()).equalsIgnoreCase("返回")) {
                player.performCommand("itown");
                event.setCancelled(true);
                return;
            }

            event.setCancelled(true);
        }

        if (tile.equalsIgnoreCase("§2村落主菜单")) {
            if (event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR)) {
                event.setCancelled(true);
                return;
            }

            if (!event.getClickedInventory().getTitle().equalsIgnoreCase("§2村落主菜单")) {
                event.setCancelled(true);
                return;
            }

            ItemStack itemStack = event.getCurrentItem();
            ItemMeta itemMeta = itemStack.getItemMeta();

            if (ChatColor.stripColor(itemMeta.getDisplayName()).equalsIgnoreCase("我的村落")) {
                File playerinfo = plugin.playerinfo;
                FileConfiguration datainfo = conf.load(playerinfo);

                File data = plugin.data;
                FileConfiguration dataConf = conf.load(data);

                String StringUUID = datainfo.getString("players." + player.getUniqueId().toString());
                if (StringUUID == null) {
                    player.sendMessage("你还未加入/创建村落！");
                    event.setCancelled(true);
                    return;
                }
                if (dataConf.getConfigurationSection("towns." + StringUUID) == null) {
                    player.sendMessage("你的村落的申请暂未通过！");
                    event.setCancelled(true);
                    return;
                }
                plugin.guIs.OpenPlayerGUI(player, dataConf.getConfigurationSection("towns." + StringUUID).
                        getString("name") + " §2村落系统", StringUUID);
                event.setCancelled(true);
                return;
            }

            if (ChatColor.stripColor(itemMeta.getDisplayName()).equalsIgnoreCase("村落列表")) {
                plugin.guIs.OpenListGUI(player, "§2村落列表");
                event.setCancelled(true);
                return;
            }
            if (ChatColor.stripColor(itemMeta.getDisplayName()).equalsIgnoreCase("村落创建")) {
                player.performCommand("itown create");
                player.closeInventory();
                event.setCancelled(true);
                return;
            }
            if (ChatColor.stripColor(itemMeta.getDisplayName()).equalsIgnoreCase("村落管理")) {
                player.performCommand("itown admin");
                event.setCancelled(true);
            }
            if (ChatColor.stripColor(itemMeta.getDisplayName()).equalsIgnoreCase("村落审核")) {
                player.performCommand("itown adminapplylist");
                event.setCancelled(true);
            }
            if (ChatColor.stripColor(itemMeta.getDisplayName()).equalsIgnoreCase("申请中村落")) {
                plugin.guIs.OpenApplyListGUI(player, "§2申请村落列表", "右击获取申请书，左击响应加入");
                event.setCancelled(true);
                return;
            }
            event.setCancelled(true);
        }

        if (tile.equalsIgnoreCase("§2人员管理")) {
            if (event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR)) {
                event.setCancelled(true);
                return;
            }

            if (!event.getClickedInventory().getTitle().equalsIgnoreCase("§2人员管理")) {
                event.setCancelled(true);
                return;
            }

            File file = plugin.data;
            FileConfiguration dataConf = conf.load(file);

            File file1 = plugin.playerinfo;
            FileConfiguration playerinfo = conf.load(file1);

            ItemStack itemStack = event.getCurrentItem();
            ItemMeta itemMeta = itemStack.getItemMeta();

            if (ChatColor.stripColor(itemMeta.getDisplayName()).equalsIgnoreCase("返回")) {
                player.performCommand("itown admin");
                event.setCancelled(true);
                return;
            }

            SkullMeta meta = (SkullMeta) itemMeta;
            OfflinePlayer player1 = meta.getOwningPlayer();

            String StringUUID = playerinfo.getString("players." + player.getUniqueId().toString());

            List<String> players = dataConf.getConfigurationSection("towns." + StringUUID).getStringList("players");

            Map<UUID, String> map = plugin.commands.map;

            if (map.containsKey(player.getUniqueId())) {
                if (map.get(player.getUniqueId()).equalsIgnoreCase(player1.getUniqueId().toString())) {
                    removePlayer(players, player1, dataConf, playerinfo, StringUUID);
                    map.remove(player.getUniqueId());
                    conf.save(dataConf, file, "data.yml");
                    conf.save(playerinfo, file1, "playerInfo.yml");
                    plugin.guIs.OpenPlayerManagerGUI(player, "§2人员管理", StringUUID, "点击移除");
                    event.setCancelled(true);
                    return;
                }
                map.remove(player.getUniqueId());
            }

            map.put(player.getUniqueId(), player1.getUniqueId().toString());
            player.sendMessage("请再点击一次确定删除");
            plugin.guIs.OpenPlayerManagerGUI(player, "§2人员管理", StringUUID, "点击移除");
            event.setCancelled(true);
        }

        if (tile.equalsIgnoreCase("§2助理设置")) {
            if (event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR)) {
                event.setCancelled(true);
                return;
            }

            if (!event.getClickedInventory().getTitle().equalsIgnoreCase("§2助理设置")) {
                event.setCancelled(true);
                return;
            }

            File file = plugin.data;
            FileConfiguration dataConf = conf.load(file);

            ItemStack itemStack = event.getCurrentItem();
            ItemMeta itemMeta = itemStack.getItemMeta();

            if (ChatColor.stripColor(itemMeta.getDisplayName()).equalsIgnoreCase("返回")) {
                player.performCommand("itown admin");
                event.setCancelled(true);
                return;
            }

            SkullMeta meta = (SkullMeta) itemMeta;
            OfflinePlayer player1 = meta.getOwningPlayer();

            String StringUUID = player.getUniqueId().toString();

            List<String> assistant = new ArrayList<>();

            Map<UUID, String> map = plugin.commands.map;

            if (map.containsKey(player.getUniqueId())) {
                if (map.get(player.getUniqueId()).equalsIgnoreCase(player1.getUniqueId().toString())) {
                    // players.remove(player1.getUniqueId().toString());
                    assistant.add(player1.getUniqueId().toString());
                    dataConf.getConfigurationSection("towns." + StringUUID).set("assistant", assistant);
                    map.remove(player.getUniqueId());
                    conf.save(dataConf, file, "data.yml");
                    plugin.guIs.OpenAdmin(player, dataConf.getConfigurationSection("towns." + StringUUID).
                            getString("name") + " §2村落管理系统", StringUUID);
                    event.setCancelled(true);
                    return;
                }
                map.remove(player.getUniqueId());
            }

            map.put(player.getUniqueId(), player1.getUniqueId().toString());
            player.sendMessage("请再点击一次确定设置");
            plugin.guIs.OpenPlayerManagerGUI(player, "§2助理设置", StringUUID, "点击设置");
            event.setCancelled(true);
        }

    }

    public void removePlayer(List<String> players, OfflinePlayer player, FileConfiguration dataConf,
                             FileConfiguration datainfo, String StringUUID) {
        players.remove(player.getUniqueId().toString());
        List<String> assistant = dataConf.getConfigurationSection("towns." + StringUUID).
                getStringList("assistant");
        assistant.remove(player.getUniqueId().toString());
        dataConf.getConfigurationSection("towns." + StringUUID).set("assistant", assistant);
        dataConf.getConfigurationSection("towns." + StringUUID).set("players", players);
        datainfo.set("players." + player.getUniqueId().toString(), null);
        Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(),
                "hm take " + player.getName() + " " +
                        dataConf.getConfigurationSection("towns." + StringUUID).get("name"));
    }

    private void sendMail(String message, UUID uuid) {
        User user = plugin.ess.getUser(uuid);
        List<String> mails = user.getMails();
        mails.add(message);
        user.setMails(mails);
        user.notifyOfMail();
    }
}
