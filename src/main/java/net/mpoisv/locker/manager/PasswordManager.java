package net.mpoisv.locker.manager;

import net.mpoisv.locker.utils.UUIDAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Objects;

public class PasswordManager {
    public static String getPassword(String title) {
        if(title.length() <= ConfigManager.guiTitle.length() + ConfigManager.guiSeparator.length()) return "";
        return title.substring(ConfigManager.guiTitle.length() + ConfigManager.guiSeparator.length());
    }

    public static boolean isPassword(String realPassword, String password) {
        if(realPassword != null && realPassword.length() >= ConfigManager.minPasswordLength && realPassword.length() <= ConfigManager.maxPasswordLength && realPassword.equals(password)) return true;
        if(realPassword != null && realPassword.length() >= ConfigManager.minPasswordLength && realPassword.length() <= ConfigManager.maxPasswordLength || password.length() != ConfigManager.minPasswordLength) return false;
        for(char c : password.toCharArray()) if(c != '0') return false;
        return true;
    }

    public static Location getLocation(ItemMeta meta) {
        var name = meta.getDisplayName();
        var lore = meta.getLore();
        if(!name.startsWith("§fx=") || lore == null || lore.size() != 1) return null;
        var pos = name.split(",");
        if(pos.length != 3) return null;
        var x = Integer.parseInt(pos[0].substring(6));
        var y = Integer.parseInt(pos[1].substring(6));
        var z = Integer.parseInt(pos[2].substring(6));
        var world = Bukkit.getWorld(Objects.requireNonNull(UUIDAdapter.fromString(lore.get(0).substring(2))));
        return new Location(world, x, y, z);
    }

    private static ItemStack getItem(Material material, String name, String ...lore) {
        var item = new ItemStack(material);
        var meta = item.getItemMeta();
        Objects.requireNonNull(meta).setDisplayName(name);
        if(lore != null && lore.length > 0)
            meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    public static Inventory getInventory(Location location, String password) {
        var inv = Bukkit.createInventory(null, 54, String.format("%s%s%s", ConfigManager.guiTitle, ConfigManager.guiSeparator, password));
        inv.setItem(0, getItem(Material.BIRCH_SIGN, String.format("§fx=§b%d,§fy=§b%d,§fz=§b%d", location.getBlockX(), location.getBlockY(), location.getBlockZ()), String.format("§f%s", Objects.requireNonNull(location.getWorld()).getUID())));
        for(int i = 0; i < 3; i++) {
            for(int j = 9 * i + 12; j < 9 * i + 15; j++) {
                inv.setItem(j, getItem(Material.RED_CONCRETE, String.format("§a%d", j - 6 * i - 11)));
            }
        }
        inv.setItem(40, getItem(Material.RED_CONCRETE, "§a0"));

        inv.setItem(41, getItem(Material.LIME_CONCRETE, ConfigManager.guiPasswordFinishBtn));
        return inv;
    }
}
