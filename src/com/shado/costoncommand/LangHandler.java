package com.shado.costoncommand;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class LangHandler {

    private final String fileName;
    private final Plugin plugin;
    private FileConfiguration fileConfiguration;
    private final File langFile;

    public LangHandler(String langPath, String fileName, Plugin plugin) {
        langFile = new File(langPath + File.separator + fileName + ".yml");
        this.fileName = fileName;
        this.plugin = plugin;
    }

    public String read(String key) {
        return fileConfiguration.getString(key);
    }

    public void reloadConfig() {
        fileConfiguration = YamlConfiguration.loadConfiguration(langFile);

        InputStream defConfigStream = plugin.getResource(fileName);
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));
            fileConfiguration.setDefaults(defConfig);
        }
    }
}
