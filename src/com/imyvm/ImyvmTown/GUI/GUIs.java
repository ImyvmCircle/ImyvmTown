package com.imyvm.ImyvmTown.GUI;

import com.imyvm.ImyvmTown.Configuration;
import com.imyvm.ImyvmTown.Main;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.File;
import java.text.DecimalFormat;
import java.util.*;

public class GUIs {

    private Main plugin;

    public GUIs(Main pl) {
        plugin = pl;
    }

    private Configuration conf = new Configuration();

    public void OpenMainGUI(Player player, String inv_name) {
        Inventory inv = Bukkit.createInventory(null, 27, inv_name);
        List<String> lore0 = new ArrayList<>();
        List<String> lore10 = new ArrayList<>();
        List<String> lore13 = new ArrayList<>();
        List<String> lore16 = new ArrayList<>();


        lore0.add("点击进入");
        ItemStack itemStack0 = createItem("SIGN", "§f我的村落", lore0);
        inv.setItem(0, itemStack0);

        lore10.add("点击查看");
        ItemStack itemStack10 = createItem("SIGN", "§f村落列表", lore10);
        inv.setItem(10, itemStack10);

        lore13.add("点击查看");
        ItemStack itemStack13 = createItem("SIGN", "§f村落创建", lore13);
        inv.setItem(13, itemStack13);

        lore16.add("点击查看");
        ItemStack itemStack16 = createItem("SIGN", "§f申请中村落", lore16);
        inv.setItem(16, itemStack16);

        File file = plugin.playerinfo;
        File file1 = plugin.data;
        FileConfiguration playerinfo = conf.load(file);
        FileConfiguration data = conf.load(file1);
        String StringUUID = playerinfo.getString("players." + player.getUniqueId().toString());
        if ((StringUUID != null) && (data.getConfigurationSection("towns." + StringUUID) != null)) {
            if (data.getConfigurationSection("towns." + StringUUID).getStringList("assistant").
                    contains(player.getUniqueId().toString()) || player.getUniqueId().toString().
                    equalsIgnoreCase(StringUUID)) {
                List<String> lore9 = new ArrayList<>();
                lore9.add("点击查看");
                ItemStack itemStack9 = createItem("SIGN", "§f村落管理", lore9);
                inv.setItem(9, itemStack9);
            }
        }
        if (player.isOp()) {
            List<String> lore18 = new ArrayList<>();
            lore18.add("点击查看");
            ItemStack itemStack18 = createItem("SIGN", "§f村落审核", lore18);
            inv.setItem(18, itemStack18);
        }

        player.openInventory(inv);
        player.updateInventory();
    }

    public void OpenListGUI(Player player, String inv_name) {
        Inventory inv = Bukkit.createInventory(null, 9, inv_name);

        File file = plugin.data;
        FileConfiguration data = conf.load(file);

        List<String> lore8 = new ArrayList<>();
        lore8.add("点击返回");
        ItemStack itemStack8 = createItem("BARRIER", "§f返回", lore8);
        inv.setItem(8, itemStack8);

        if (!data.isConfigurationSection("towns")) {
            player.openInventory(inv);
            player.updateInventory();
            return;
        }
        Set<String> towns = data.getConfigurationSection("towns").getKeys(true);

        for (String a : towns) {
            if (a.contains(".")) {
                continue;
            }
            ConfigurationSection conf = data.getConfigurationSection("towns." + a);
            if (conf.getString("status").equalsIgnoreCase("§4deleted")) {
                continue;
            }
            List<String> lores = new ArrayList<>();
            lores.add(a);
            lores.add("§f管理者: §7" + conf.getString("ApplyPlayer"));
            lores.add("§f人数: §7" + (conf.getStringList("players").size() + 1));
            lores.add("§f村落口号: §7" + conf.getString("declaration"));
            lores.add("§f成立时间: §7" + conf.getString("Time"));
            lores.add("§f昨日活跃人数: §7" + conf.getStringList("dailyPlayers").size());
            lores.add("§f上周活跃人数: §7" + conf.getStringList("WeeklyPlayers").size());
            lores.add("§f上月活跃人数: §7" + conf.getStringList("WeeklyPlayers").size());
            lores.add("§f加入条件: §7" + conf.getString("requirements"));
            lores.add("§f联系方式: §7" + conf.getString("contact"));
            lores.add("§f状态: §7" + conf.getString("status"));
            lores.add("§f点击申请加入或进入村落菜单");
            // TODO
            ItemStack item = createItem("SIGN", conf.getString("name"), lores);
            inv.addItem(item);
        }

        player.openInventory(inv);
        player.updateInventory();
    }

    public void OpenApplyListGUI(Player player, String inv_name, String info) {
        Inventory inv = Bukkit.createInventory(null, 27, inv_name);
        File file = new File(plugin.getDataFolder(), "applyInfo.yml");
        FileConfiguration data = conf.load(file);

        List<String> lore26 = new ArrayList<>();
        lore26.add("点击返回");
        ItemStack itemStack26 = createItem("BARRIER", "§f返回", lore26);
        inv.setItem(26, itemStack26);

        if (!data.isConfigurationSection("towns")) {
            player.openInventory(inv);
            player.updateInventory();
            return;
        }

        Set<String> towns = data.getConfigurationSection("towns").getKeys(true);

        for (String a : towns) {
            if (a.contains(".")) {
                continue;
            }
            List<String> lores = new ArrayList<>();
            ConfigurationSection config = data.getConfigurationSection("towns." + a);

            List<String> playersUUID = config.getStringList("players");
            List<String> playersName = new ArrayList<>();
            for (String uuid : playersUUID) {
                playersName.add(Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName());
            }

            lores.add(a);
            lores.add("§f申请者: §7" + config.getString("ApplyPlayer"));
            lores.add("§f申请时间: §7" + config.getString("ApplyTime"));
            lores.add("§f村落简介: §7" + config.getString("range"));
            lores.add("§f响应人员: §7" + playersName.toString());
            lores.add("§f审核状态: §f" + config.getString("status", "待审核"));
            if (!(config.getString("locationstatus") == null)) {
                lores.add("§f村落范围审核状态: §7" + config.getString("locationstatus"));
            }
            lores.add("---------");
            lores.add(info);
            // TODO
            ItemStack item = createItem("SIGN", config.getString("name"), lores);
            inv.addItem(item);
        }

        player.openInventory(inv);
        player.updateInventory();
    }

    public void OpenApplyAdminGUI(Player player, String inv_name, String uuidString) {
        Inventory inv = Bukkit.createInventory(null, 9, inv_name);
        File file = plugin.applyInfo;
        FileConfiguration data = conf.load(file);

        if (!data.isConfigurationSection("towns")) {
            player.openInventory(inv);
            player.updateInventory();
            return;
        }

        ConfigurationSection config = data.getConfigurationSection("towns." + uuidString);

        List<String> lores = new ArrayList<>();
        List<String> playersUUID = config.getStringList("players");
        List<String> playersName = new ArrayList<>();
        for (String uuid : playersUUID) {
            playersName.add(Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName());
        }
        lores.add(uuidString);
        lores.add("§f申请者: §7" + config.getString("ApplyPlayer"));
        lores.add("§f申请时间: §7" + config.getString("ApplyTime"));
        lores.add("§f村落简介: §7" + config.getString("range"));
        lores.add("§f响应人员: §7" + playersName.toString());
        lores.add("§f审核状态: §7" + config.getString("status", "待审核"));
        if (!(config.getString("locationstatus") == null)) {
            lores.add("§f村落范围审核状态: §7" + config.getString("locationstatus"));
            if (config.get("location1") != null && config.get("location2") != null) {
                Location location1 = (Location) config.get("location1");
                Location location2 = (Location) config.get("location2");
                DecimalFormat df = new DecimalFormat("0.0");
                lores.add("§f村落范围");
                lores.add("§fworld: §7" + location1.getWorld());
                lores.add("§fX: §7" + df.format(location1.getX()) + " §fZ: §7" + df.format(location1.getZ()) +
                        " §f~ X: §7" + df.format(location2.getX()) + " §fZ: §7" + df.format(location2.getZ()));
            }
        }
        // TODO
        ItemStack item1 = createItem("SIGN", config.getString("name"), lores);
        ItemStack item2 = createItem("SIGN", "§2通过", Arrays.asList("Imyvm_Admin"));
        ItemStack item3 = createItem("SIGN", "§4拒绝", Arrays.asList("Imyvm_Admin"));
        ItemStack item4 = createItem("SIGN", "§f返回", Arrays.asList("点击返回"));

        inv.addItem(item1);
        inv.setItem(5, item2);
        inv.setItem(6, item3);
        inv.setItem(8, item4);

        player.openInventory(inv);
        player.updateInventory();
    }

    public void OpenAdmin(Player player, String inv_name, String stringUUID) {
        Inventory inv = Bukkit.createInventory(null, 36, inv_name);

        File file = plugin.data;
        FileConfiguration data = conf.load(file);
        ConfigurationSection conf = data.getConfigurationSection("towns." + stringUUID);

        List<String> lores = new ArrayList<>();
        lores.add("§f管理者: §7" + conf.getString("ApplyPlayer"));
        lores.add("§f人数: §7" + (conf.getStringList("players").size() + 1));
        lores.add("§f成立时间: §7" + conf.getString("Time"));
        lores.add("§f昨日活跃人数: §7" + conf.getStringList("dailyPlayers").size());
        lores.add("§f上周活跃人数: §7" + conf.getStringList("WeeklyPlayers").size());
        lores.add("§f上月活跃人数: §7" + conf.getStringList("MonthlyPlayers").size());

        List<String> assistantuuid = conf.getStringList("assistant");
        List<String> assistant = new ArrayList<>();
        if (!assistantuuid.isEmpty()) {
            assistant.add(Bukkit.getOfflinePlayer(UUID.fromString(assistantuuid.get(0))).getName());
        }
        assistant.add("点击修改/添加");
        DecimalFormat df = new DecimalFormat("0.0");

        ItemStack item1 = createItem("SIGN", conf.getString("name"), lores);
        ItemStack item2 = createItem("SIGN", "§fID", Arrays.asList(stringUUID));
        ItemStack item3 = createItem("SIGN", "§f联系方式", Arrays.asList(conf.getString("contact", "暂无"), "点击修改"));
        ItemStack item4 = createItem("SIGN", "§f加入条件", Arrays.asList(conf.getString("requirements"), "点击修改"));

        ItemStack itemStack = conf.getItemStack("Banner", new ItemStack(Material.SIGN));
        ItemStack item5 = createItem(itemStack, "§f村落旗帜", Arrays.asList(conf.getString("declaration"), "点击修改"));

        ItemStack item6 = createItem("SIGN", "§f村落传送点", Arrays.asList(conf.getString("tplocation"), "点击修改"));
        if (conf.get("tplocation") != null) {
            Location location = (Location) conf.get("tplocation");
            item6 = createItem("SIGN", "§f村落传送点", Arrays.asList("§fworld: §7" + location.getWorld(),
                    "§fX: §7" + df.format(location.getX()), "§fY: §7" + df.format(location.getY())
                    , "§fZ: §7" + df.format(location.getZ()), "点击修改"));
        }

        ItemStack itemStack1 = conf.getItemStack("Items", new ItemStack(Material.SIGN));
        ItemStack item7 = createItem(itemStack1, "§f吉祥物", Arrays.asList(conf.getString("declaration"), "点击修改"));

        ItemStack item8 = createItem("SIGN", "§f村落口号", Arrays.asList(conf.getString("declaration"), "点击修改"));
        ItemStack item9 = createItem("SIGN", "§f助理", assistant);
        ItemStack item10 = createItem("SIGN", "§f申请审核", Arrays.asList("点击查看"));
        ItemStack item11 = createItem("SIGN", "§f人员管理", Arrays.asList("点击查看"));


        ItemStack item12 = createItem("SIGN", "§f村落范围", Arrays.asList("左击设置第一个点", "右击设置第二个点"));
        if (conf.get("location1") != null && conf.get("location2") != null) {
            Location location1 = (Location) conf.get("location1");
            Location location2 = (Location) conf.get("location2");

            item12 = createItem("SIGN", "§f村落范围", Arrays.asList("§fworld: §7" + location1.getWorld(),
                    "§fX: §7" + df.format(location1.getX()) + " §fZ: §7" + df.format(location1.getZ()) +
                            " §f~ X: §7" + df.format(location2.getX()) + " §fZ: §7" + df.format(location2.getZ()),
                    "左击设置第一个点", "右击设置第二个点"));
        }

        ItemStack item13 = createItem("SIGN", "§f村落解散", Arrays.asList("危险"));

        inv.setItem(4, item1);
        inv.setItem(10, item2);
        inv.setItem(12, item3);
        inv.setItem(14, item4);
        inv.setItem(16, item5);
        inv.setItem(19, item6);
        inv.setItem(21, item7);
        inv.setItem(23, item8);
        inv.setItem(25, item9);
        inv.setItem(28, item10);
        inv.setItem(30, item11);
        inv.setItem(32, item12);

        List<String> lore35 = new ArrayList<>();
        lore35.add("点击返回");
        ItemStack itemStack35 = createItem("BARRIER", "§f返回", lore35);
        inv.setItem(35, itemStack35);

        player.openInventory(inv);
        player.updateInventory();
    }

    public void OpenApplysGUI(Player player, String inv_name, String stringUUID) {
        Inventory inv = Bukkit.createInventory(null, 27, inv_name);

        File file = plugin.data;
        FileConfiguration data = conf.load(file);
        ConfigurationSection conf = data.getConfigurationSection("towns." + stringUUID);

        List<String> ApplyPlayers = conf.getStringList("ApplyPlayers");

        for (String a : ApplyPlayers) {
            OfflinePlayer player1 = Bukkit.getOfflinePlayer(UUID.fromString(a));
            String name = player1.getName();
            ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            meta.setDisplayName(name);
            meta.setOwningPlayer(player1);
            List<String> lore = new ArrayList<>();
            lore.add("§2点击审核");
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.addItem(item);
        }

        List<String> lore26 = new ArrayList<>();
        lore26.add("点击返回");
        ItemStack itemStack26 = createItem("BARRIER", "§f返回", lore26);
        inv.setItem(26, itemStack26);

        player.openInventory(inv);
        player.updateInventory();

    }

    public void OpenPlayerGUI(Player player, String inv_name, String stringUUID) {
        Inventory inv = Bukkit.createInventory(null, 9, inv_name);
        File file = plugin.data;
        FileConfiguration data = conf.load(file);
        ConfigurationSection conf = data.getConfigurationSection("towns." + stringUUID);

        if (conf.getString("status").equalsIgnoreCase("§4locked")) {
            player.sendMessage("村落已被锁定");
            return;
        }

        ItemStack item1 = createItem("SIGN", "§f传送中心", Arrays.asList("点击传送"));
        ItemStack item2 = createItem("SIGN", "§f每日奖励", Arrays.asList("点击领取"));
        ItemStack item3 = createItem("SIGN", "§f徽章领取", Arrays.asList("点击领取"));
        ItemStack item4 = createItem("SIGN", "§f退出村落", Arrays.asList("点击退出"));

        inv.setItem(0, item1);
        inv.setItem(1, item2);
        inv.setItem(2, item3);
        inv.setItem(3, item4);

        List<String> lore8 = new ArrayList<>();
        lore8.add("点击返回");
        ItemStack itemStack8 = createItem("BARRIER", "§f返回", lore8);
        inv.setItem(8, itemStack8);

        player.openInventory(inv);
        player.updateInventory();
    }

    public void OpenPlayerManagerGUI(Player player, String inv_name, String stringUUID, String s) {
        Inventory inv = Bukkit.createInventory(null, 27, inv_name);
        File file = plugin.data;
        FileConfiguration data = conf.load(file);
        ConfigurationSection conf = data.getConfigurationSection("towns." + stringUUID);

        List<String> playersUUID = conf.getStringList("players");

        for (String uuid : playersUUID) {
            OfflinePlayer player1 = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
            String name = player1.getName();
            ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            meta.setDisplayName(name);
            meta.setOwningPlayer(player1);
            List<String> lore = new ArrayList<>();
            lore.add(s);
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.addItem(item);
        }

        List<String> lore26 = new ArrayList<>();
        lore26.add("点击返回");
        ItemStack itemStack26 = createItem("BARRIER", "§f返回", lore26);
        inv.setItem(26, itemStack26);

        player.openInventory(inv);
        player.updateInventory();
    }

    public void OpenApplyPlayersAdminGUI(Player player, String inv_name, ItemStack itemStack) {
        Inventory inv = Bukkit.createInventory(null, 9, inv_name);

        ItemMeta itemMeta = itemStack.getItemMeta();

        List<String> lores = new ArrayList<>();

        // TODO
        ItemStack item1 = createItem(itemStack, itemMeta.getDisplayName(), lores);
        ItemStack item2 = createItem("SIGN", "§2通过", Arrays.asList("Imyvm_Admin"));
        ItemStack item3 = createItem("SIGN", "§4拒绝", Arrays.asList("Imyvm_Admin"));
        ItemStack item4 = createItem("SIGN", "§f返回", Arrays.asList("点击返回"));

        inv.addItem(item1);
        inv.setItem(5, item2);
        inv.setItem(6, item3);
        inv.setItem(8, item4);

        player.openInventory(inv);
        player.updateInventory();
    }

    private ItemStack createItem(String material, String name, List<String> lore) {
        ItemStack itemStack = new ItemStack(Material.getMaterial(material));
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(name);
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private ItemStack createItem(ItemStack itemStack, String name, List<String> lore) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(name);
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
