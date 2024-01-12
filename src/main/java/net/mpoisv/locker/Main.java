package net.mpoisv.locker;

import net.mpoisv.locker.commands.PLocker;
import net.mpoisv.locker.manager.ConfigManager;
import net.mpoisv.locker.manager.DatabaseManager;
import net.mpoisv.locker.manager.EventManager;
import net.mpoisv.locker.manager.ProtectionManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class Main extends JavaPlugin {
    public static Main instance;
    public DatabaseManager databaseManager;
    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        try {
            databaseManager = new DatabaseManager(getDataFolder().getAbsolutePath(), "locker.db");
        }catch (SQLException e) {
            throw new RuntimeException(e);
        }

        initCommand(Objects.requireNonNull(getCommand("plocker")), new PLocker());
        Bukkit.getPluginManager().registerEvents(new EventManager(), this);
        loadConfig();

        if(ConfigManager.updateCheck)
            VersionChecker.getLatestVersionFromServer();

        Bukkit.getConsoleSender().sendMessage(String.format("§b:§r %s §b:§r Plugin Loading finished. Current version: %s.", getDescription().getName(), getDescription().getVersion()));
        Bukkit.getConsoleSender().sendMessage(String.format("§b:§r %s §b:§r total protected materials: §a%d.", getDescription().getName(), ConfigManager.protectBlocks.size()));
        if(!VersionChecker.isLatestVersion(getDescription().getVersion())) {
            Bukkit.getConsoleSender().sendMessage(String.format("§b:§r %s §b:§e Latest version: %s. Update please.", getDescription().getName(), VersionChecker.getVersionCode()));
            Bukkit.getConsoleSender().sendMessage(String.format("§b:§r %s §b:§e https://www.spigotmc.org/resources/passwordlocker.113386/", getDescription().getName()));
        }
    }

    private void initCommand(PluginCommand command, CommandExecutor executor) {
        command.setExecutor(executor);
        command.setTabCompleter((TabCompleter) executor);
    }

    @Override
    public void onDisable() {
        instance = null;
        ProtectionManager.timerClose();
        try {
            databaseManager.close();
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T getOrDefault(T t, T def) {
        if(t == null) return def;
        return t;
    }

    private void loadConfig() {
        var config = getConfig();
        ConfigManager.privateTexts = getOrDefault(config.getStringList("private"), List.of("[Private]"));

        ConfigManager.minPasswordLength = getOrDefault(config.getInt("password.min_length"), 4);
        ConfigManager.maxPasswordLength = getOrDefault(config.getInt("password.max_length"), 4);
        ConfigManager.passwordEnabled = getOrDefault(config.getBoolean("password.enable"), true);
        ConfigManager.passwordAllowTime = getOrDefault(config.getLong("password.correct_allow_time"), 30L);

        ConfigManager.guiTitle = getOrDefault(config.getString("password_gui.title"), "password");
        ConfigManager.guiSeparator = getOrDefault(config.getString("password_gui.separator"), ": ");
        ConfigManager.guiPasswordFinishBtn = getOrDefault(config.getString("password_gui.password_finish_btn"), "OK");

        var protectList = new HashSet<Material>();
        for(var m : config.getStringList("protect_blocks")) {
            protectList.add(Material.valueOf(m.toUpperCase()));
        }
        ConfigManager.protectBlocks = protectList;

        if(!config.contains("update_check")) {
            config.set("update_check", true);
            saveConfig();
        }
        ConfigManager.updateCheck = getOrDefault(config.getBoolean("update_check"), true);

        ConfigManager.langGuiError = getOrDefault(config.getString("lang.gui_error"), "§ePassword Input Error. Maybe private block doesn't exists.");
        ConfigManager.langPasswordChange = getOrDefault(config.getString("lang.password_change"), "§ePassword Change Completely. Current password: %password%");
        ConfigManager.langPasswordWrong = getOrDefault(config.getString("lang.password_wrong"), "§ePassword Wrong.");
        ConfigManager.langPasswordCorrect = getOrDefault(config.getString("lang.password_correct"), "§aPassword Corrected. You allowed use block during %allow-time% minutes.");
        ConfigManager.langAllowOnlyBlockUse = getOrDefault(config.getString("lang.allow_only_block_use"), "§ePassword Permission User allow only use/break block without sign.");
        ConfigManager.langPasswordUserSignUse = getOrDefault(config.getString("lang.password_user_sign_use"), "§ePassword Permission User can't use sign for protected block.");

        if(!config.contains("lang.world_empty")) {
            config.set("world_empty", "§c%world% is not a world.");
            saveConfig();
        }
        ConfigManager.langEmptyWorld = getOrDefault(config.getString("lang.world_empty"), "§c%world% is not a world.");

        if(ConfigManager.disableWorlds == null) ConfigManager.disableWorlds = new HashSet<>();
        else ConfigManager.disableWorlds.clear();
        ConfigManager.disableWorlds.addAll(config.getStringList("disable_worlds"));
    }
}