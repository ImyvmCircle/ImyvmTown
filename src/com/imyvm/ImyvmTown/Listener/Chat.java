package com.imyvm.ImyvmTown.Listener;

import com.imyvm.ImyvmTown.Configuration;
import com.imyvm.ImyvmTown.Main;
import com.imyvm.ImyvmTown.Utils.JSONMessage;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class Chat implements Listener {

    private Main plugin;

    public Chat(Main pl) {
        plugin = pl;
        plugin.getServer().getPluginManager().registerEvents(this, pl);
    }

    private Configuration conf = new Configuration();

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Map<UUID, String> map = plugin.commands.map;
        File file = new File(plugin.getDataFolder(), "applyInfo.yml");
        FileConfiguration data = conf.load(file);

        File towns = plugin.data;
        FileConfiguration townsConf = conf.load(towns);

        File playerinfo = plugin.playerinfo;
        FileConfiguration datainfo = conf.load(playerinfo);

        Player player = event.getPlayer();
        if (map.containsKey(player.getUniqueId())) {
            String type = map.get(player.getUniqueId());
            String message = ChatColor.translateAlternateColorCodes('&', event.getMessage());
            if (type.equalsIgnoreCase("book")) {
                return;
            }
            if (type.equalsIgnoreCase("contact") || type.equalsIgnoreCase("requirements") ||
                    type.equalsIgnoreCase("declaration")) {
                ConfigurationSection townConfig = townsConf.getConfigurationSection("towns." +
                        datainfo.getString("players." + player.getUniqueId().toString()));
                townConfig.set(type, message);
                conf.save(townsConf, towns, "data.yml");
                player.sendMessage("输入成功：" + message);
                event.setCancelled(true);
                map.remove(player.getUniqueId());
                return;
            }

            // TODO 判断名字的合法性
            ConfigurationSection config = data.getConfigurationSection("towns." + player.getUniqueId().toString());
            config.set(type, message);
            conf.save(data, file, "applyInfo.yml");
            if (type.equalsIgnoreCase("name")) {
                player.sendMessage("村落名称输入成功：" + message);
                config.set("players", new ArrayList<>());
                conf.save(data, file, "applyInfo.yml");
                map.put(player.getUniqueId(), "range");
                player.sendMessage("请在对话框输入村落简介");
                event.setCancelled(true);
            }
            if (type.equalsIgnoreCase("range")) {
                player.sendMessage("村落简介输入成功：" + message);
                map.put(player.getUniqueId(), "book");
                JSONMessage.create("请手持申请书并点击")
                        .then("这里").color(ChatColor.DARK_RED).runCommand("/itown upload").then("上传").send(player);
                event.setCancelled(true);
            }
        }
    }
}
