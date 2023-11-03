package net.mpoisv.locker;

import net.mpoisv.locker.manager.ConfigManager;
import net.mpoisv.locker.manager.DatabaseManager;
import net.mpoisv.locker.manager.EventManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;

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

//        getCommand("plocker");
        Bukkit.getPluginManager().registerEvents(new EventManager(), this);
        loadConfig();

        Bukkit.getConsoleSender().sendMessage(String.format("§b:§r %s §b:§r Plugin Loading finished. Current version: %s.", getDescription().getName(), getDescription().getVersion()));
        Bukkit.getConsoleSender().sendMessage(String.format("§b:§r %s §b:§r total protected materials: §a%d.", getDescription().getName(), ConfigManager.protectBlocks.size()));
    }

    @Override
    public void onDisable() {
        instance = null;
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

        ConfigManager.guiTitle = getOrDefault(config.getString("password_gui.title"), "password");
        ConfigManager.guiSeparator = getOrDefault(config.getString("password_gui.separator"), ": ");
        ConfigManager.guiPasswordFinishBtn = getOrDefault(config.getString("password_gui.password_finish_btn"), "OK");

        var protectList = new HashSet<Material>();
        for(var m : config.getStringList("protect_blocks")) {
            protectList.add(Material.valueOf(m.toUpperCase()));
        }
        ConfigManager.protectBlocks = protectList;
    }
}