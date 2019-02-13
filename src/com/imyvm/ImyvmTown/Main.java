package com.imyvm.ImyvmTown;

import com.earth2me.essentials.Essentials;
import com.imyvm.ImyvmTown.Commands.Commands;
import com.imyvm.ImyvmTown.GUI.GUIs;
import com.imyvm.ImyvmTown.Listener.Chat;
import com.imyvm.ImyvmTown.Listener.ClickItem;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Main extends JavaPlugin {
    private static final Logger log = Logger.getLogger("ImyvmTown");
    public FileConfiguration config = getConfig();
    public File applyInfo = new File(getDataFolder(), "applyInfo.yml");
    public File playerinfo = new File(getDataFolder(), "PlayerInfo.yml");
    public File data = new File(getDataFolder(), "data.yml");
    public File rewards = new File(getDataFolder(), "rewards.yml");


    public GUIs guIs;
    public Commands commands;
    public Chat chat;
    public ClickItem clickItem;
    public Essentials ess;
    private static Economy econ = null;

    private Configuration conf = new Configuration();

    private List<String> strings = new ArrayList<>();

    private FileConfiguration datainfo = conf.load(playerinfo);
    private FileConfiguration dataConf = conf.load(data);
    private FileConfiguration applys = conf.load(applyInfo);
    private FileConfiguration rewardsInfo = conf.load(rewards);


    @Override
    public void onDisable() {
        log.info(String.format("[%s] Disabled Version %s", getDescription().getName(),
                getDescription().getVersion()));
    }

    @Override
    public void onEnable() {
        log.info(String.format("[%s] Enabled Version %s", getDescription().getName(), getDescription().getVersion()));

        rewardsInfo.addDefault("rewards", strings);
        config.addDefault("ChangeTpLocationFee", 40000);
        config.addDefault("RewardBase", 100);
        config.addDefault("TradeUUID", "a641c611-21ef-4b71-b327-e45ef8fdf647");
        config.addDefault("Token-ID", "XXX");

        rewardsInfo.options().copyDefaults(true);
        config.options().copyDefaults(true);
        datainfo.options().copyDefaults(true);
        dataConf.options().copyDefaults(true);
        applys.options().copyDefaults(true);
        conf.save(rewardsInfo, rewards, "rewards.yml");
        conf.save(datainfo, playerinfo, "PlayerInfo.yml");
        conf.save(dataConf, data, "data.yml");
        conf.save(applys, applyInfo, "applyInfo.yml");
        saveConfig();

        ess = (Essentials) getServer().getPluginManager().getPlugin("Essentials");
        RegisteredServiceProvider<Economy> economyP = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyP != null)
            econ = economyP.getProvider();
        else
            Bukkit.getLogger().info("Unable to initialize Economy Interface with Vault!");

        guIs = new GUIs(this);
        commands = new Commands(this);
        chat = new Chat(this);
        clickItem = new ClickItem(this);
        getCommand("itown").setExecutor(commands);
    }

    public static Economy getEcononomy() {
        return econ;
    }
}
