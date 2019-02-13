package com.imyvm.ImyvmTown;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class Configuration {

    public FileConfiguration load(File file) {
        return YamlConfiguration.loadConfiguration(file);
    }

    public void save(FileConfiguration conf, File file, String fileName) {
        try {
            conf.save(file);
        } catch (IOException e) {
            Logger.getLogger("ImyvmTown").info("Unable to save " + fileName);
        }
    }
}
