package com.shado.costoncommand;

import java.io.File;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.RegisteredServiceProvider;

@SuppressWarnings("deprecation")
public class CostOnCommand extends JavaPlugin implements Listener {

    public static File pluginFolder;
    public static File configFile;
    public String configName = "Commands.yml";
    public String nodeBase = "costoncommand.";
    boolean firstTime = false;

    public static YamlConfiguration configData = new YamlConfiguration();

    public final Logger logger = Logger.getLogger("minecraft");
    boolean isVault = false;

    private static HashMap<String, HashMap<String, Integer>> data = new HashMap<String, HashMap<String, Integer>>();

    public static Economy econ = null;
    public static Permission perms = null;
    public static Chat chat = null;
    public int playerCost;

    @Override
    public void onEnable() {
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
        saveConfig();

        logger.info("[CostOnCommand] CostOnCommand Enabled!");

        getServer().getPluginManager().registerEvents(this, this);

        loadData(getConfig().getConfigurationSection("Commands"));

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

    private void loadData(ConfigurationSection d) {

        HashMap<String, Integer> map;
        for (String s : d.getKeys(false)) {
            map = new HashMap<String, Integer>();
            ConfigurationSection c = d.getConfigurationSection(s);
            for (String r : c.getKeys(false)) {
                map.put(r, c.getInt(r, 50));
            }
            data.put(s, map);
        }

        for (String cmd : d.getKeys(false)) {
            //d.getConfigurationSection(cmd);
            //data.put(cmd, 
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
    public void onDisable() {
        try {
            //
        } catch (Exception ex) {
            // Failed to save config file
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 1) {

            if (args[0].equalsIgnoreCase("help")) {
                sender.sendMessage(ChatColor.YELLOW + "/coc reload - " + ChatColor.WHITE + "Reload the plugin config file.");
                return true;
            }

            if (sender.isOp()) {

                if (args[0].equalsIgnoreCase("reload")) {
                    reloadConfig();
                    loadData(getConfig().getConfigurationSection("Commands"));
                    sender.sendMessage(ChatColor.GREEN + "Config Reloaded");
                    return true;
                } else {
                    sender.sendMessage(args[0] + "is not a command!");
                }

            } else {
                sender.sendMessage(ChatColor.RED + "You do no have access to this command!");
            }
        } else {
            sender.sendMessage("use /coc help for usage.");
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
                    player.sendMessage(ChatColor.RED + "There is an error in the CoC config files, please contact an admin");
                }

                ConfigurationSection commandSection = getConfig().getConfigurationSection("Commands." + command);
                Set<String> set = commandSection.getKeys(false);

                for (String group : set) {
                    if (player.hasPermission(nodeBase + group)) {
                        if (playerCost >= data.get(command).get(group)){
                            playerCost = data.get(command).get(group);
                        }
                    }
                }

                if (playerCost > econ.getBalance(player.getName())) {
                    player.sendMessage(ChatColor.YELLOW + "You are missing " + "$" + (playerCost - econ.getBalance(player.getName())) + " to use this command. ");
                    event.setCancelled(true);
                } else {
                    econ.withdrawPlayer(player.getName(), playerCost);
                    event.setCancelled(false);
                    player.sendMessage(ChatColor.GREEN + "$" + playerCost + ChatColor.YELLOW + " was taken from your balance.");
                    player.sendMessage(ChatColor.GREEN + "New Balance: " + econ.getBalance(player.getName()));
                }
            }
        }

    }

}
