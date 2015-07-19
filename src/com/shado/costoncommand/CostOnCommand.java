package com.shado.costoncommand;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Logger;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("deprecation")
public class CostOnCommand extends JavaPlugin implements Listener {

    public String configName = "Commands.yml";
    public String nodeBase = "costoncommand.";
    boolean firstTime = false;

    public static YamlConfiguration configData = new YamlConfiguration();

    public final Logger logger = Logger.getLogger("minecraft");
    boolean isVault = false;

    private static HashMap<String, HashMap<String, Integer>> data = new HashMap<>();
    LangHandler lang = null;

    public static Economy econ = null;
    public static Permission perms = null;
    public static Chat chat = null;
    public int playerCost;

    @Override
    public void onEnable() {

        PluginDescriptionFile pdfFile = this.getDescription();
        String Version = pdfFile.getVersion();
        this.logger.info(pdfFile.getName() + " Version " + Version + " has been enabled! " + pdfFile.getWebsite());
        getConfig().options().copyDefaults(true);
        getConfig().options().header("If you need help with this plugin you can contact shadoking75 on teamspeak ip: goreacraft.com\n Website http://www.goreacraft.com\n"
                + "Usage:\n"
                + "Commands:\n"
                + "  back:\n"
                + "    default: <cost>\n"
                + "    group: <cost>\n"
                + "    etc..");
        if (!getConfig().getKeys(false).contains("Commands")) {
            getConfig().createSection("Commands");
        }
        getConfig().addDefault("Lang", "EN");
        saveConfig();

        logger.info("[CostOnCommand] CostOnCommand Enabled!");

        getServer().getPluginManager().registerEvents(this, this);

        loadData(getConfig().getConfigurationSection("Commands"));

        setupLanguage();
        // Metrics
        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch (IOException e) {

        }

        // Vault ------
        if (setupEconomy()) {
            if (getConfig().getBoolean("More info in logs")) {
                logger.info(String.format("[%s] vault found, enabling the economy module. ", getDescription().getName()));
            }
            isVault = true;
        } else if (getConfig().getBoolean("More info in logs")) {
            logger.warning(String.format("[%s] The economy module Disabled due to no Vault found! Get Vault from: http://dev.bukkit.org/bukkit-plugins/vault/", getDescription().getName()));
        }
        // ------- Vault Code Ends
    }

    private void setupLanguage() {
        String fileName = getConfig().getString("Lang");
        String path = this.getDataFolder().getAbsolutePath() + File.separator + "lang";
        File langFolder = new File(path);
        if (!langFolder.exists()) {
            langFolder.mkdir();
        }

        saveResource("lang" + File.separator + "EN.yml", true);
        lang = new LangHandler(path, fileName, this);
        lang.reloadConfig();
    }

    private void loadData(ConfigurationSection d) {
        HashMap<String, Integer> map;
        for (String s : d.getKeys(false)) {
            map = new HashMap<>();
            ConfigurationSection c = d.getConfigurationSection(s);
            for (String r : c.getKeys(false)) {
                map.put(r, c.getInt(r, 50));
            }
            data.put(s, map);
        }
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            econ = economyProvider.getProvider();
        }

        return (econ != null);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 1) {

            if (args[0].equalsIgnoreCase("help")) {
                sender.sendMessage(ChatColor.YELLOW + "/coc reload - " + ChatColor.WHITE + lang.read("ReloadHelp"));
                return true;
            }

            if (sender.isOp()) {

                if (args[0].equalsIgnoreCase("reload")) {
                    reloadConfig();
                    lang.reloadConfig();
                    setupLanguage();
                    loadData(getConfig().getConfigurationSection("Commands"));
                    sender.sendMessage(ChatColor.GREEN + lang.read("OnReload"));
                    return true;
                } else {
                    sender.sendMessage(args[0] + " " + lang.read("InvalidCommand"));
                }

            } else {
                sender.sendMessage(ChatColor.RED + lang.read("InvalidAccess"));
            }
        } else {
            sender.sendMessage(lang.read("HelpMessage"));
        }
        return true;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        if (!player.isOp()) {
            String command = event.getMessage().substring(1);
            command = command.split(" ")[0];

            if (data.containsKey(command)) {
                try {
                    playerCost = data.get(command).get("default");
                } catch (Exception ex) {
                    player.sendMessage(ChatColor.RED + lang.read("DefaultError"));
                }

                ConfigurationSection commandSection = getConfig().getConfigurationSection("Commands." + command);
                Set<String> set = commandSection.getKeys(false);

                for (String group : set) {
                    if (player.hasPermission(nodeBase + group)) {
                        if (playerCost >= data.get(command).get(group)) {
                            playerCost = data.get(command).get(group);
                        }
                    }
                }

                if (playerCost > econ.getBalance(player.getName())) {
                    player.sendMessage(ChatColor.YELLOW + lang.read("ShortFundsBeginning") + " " + econ.currencyNameSingular() + (playerCost - econ.getBalance(player.getName())) + " " + lang.read("ShortFundsEnding"));
                    event.setCancelled(true);
                } else {
                    econ.withdrawPlayer(player.getName(), playerCost);
                    econ.withdrawPlayer(player.getName(), econ.getBalance(player.getName()) - Math.floor(econ.getBalance(player.getName())));
                    event.setCancelled(false);
                    player.sendMessage(ChatColor.GREEN + econ.currencyNameSingular() + playerCost + ChatColor.YELLOW + " " + lang.read("PlayerBalance"));
                    player.sendMessage(ChatColor.GREEN + lang.read("NewBalance") + ": " + econ.getBalance(player.getName()));
                }
            }
        }

    }

}
