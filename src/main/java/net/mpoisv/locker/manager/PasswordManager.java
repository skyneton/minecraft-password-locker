package net.mpoisv.locker.manager;

import com.mojang.util.UUIDTypeAdapter;
import net.minecraft.util.Tuple;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class PasswordManager {
    private final static BlockFace[] checkRelativeFaces = new BlockFace[] { BlockFace.UP, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST };
    public static String getPassword(String title) {
        if(title.length() <= ConfigManager.guiTitle.length() + ConfigManager.guiSeparator.length()) return "";
        return title.substring(ConfigManager.guiTitle.length() + ConfigManager.guiSeparator.length());
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
        var world = Bukkit.getWorld(UUIDTypeAdapter.fromString(lore.get(0).substring(2)));
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

    public static List<Block> getConnectedSignBlock(Location loc) {
        var b = loc.getBlock();
        var list = new ArrayList<Block>();
        for(var face : checkRelativeFaces) {
            var rb = b.getRelative(face);
            var rbs = rb.getState();
            var rbd = rb.getBlockData();
            if(rbs instanceof Sign) {
                var isWallSign = rbd instanceof WallSign;
                if(face == BlockFace.UP) {
                    if(!isWallSign) list.add(rb);
                    continue;
                }
                if(!isWallSign) continue;
                if(((WallSign) rbd).getFacing() == face) list.add(rb);
            }
        }
        return list;
    }

    public static List<Block> getSignNearBy(Block b) {
        var data = b.getState();
        if(data instanceof Chest) {
            var holder = ((Chest) data).getInventory().getHolder();
            if(holder instanceof DoubleChest) {
                var aPos = ((DoubleChest) holder).getLeftSide().getInventory().getLocation();
                var x = aPos.getX();
                var z = aPos.getZ();
                aPos = aPos.getBlock().getLocation();
                Location bPos = aPos;
                if(Math.abs(x - (int)x) > 0.1) bPos = aPos.clone().add(1, 0, 0);
                if(Math.abs(z - (int)z) > 0.1) bPos = aPos.clone().add(0, 0, 1);
                var list = getConnectedSignBlock(aPos);
                list.addAll(getConnectedSignBlock(bPos));
                return list;
            }
        }
        if(data.getBlockData() instanceof Door) {
            var aPos = data.getLocation();
            var bPos = aPos;
            if(((Door) data.getBlockData()).getHalf() == Bisected.Half.TOP) bPos = aPos.clone().subtract(0, 1, 0);
            else bPos = aPos.clone().add(0, 1, 0);
            var list = getConnectedSignBlock(aPos);
            list.addAll(getConnectedSignBlock(bPos));
            return list;
        }
        return getConnectedSignBlock(data.getLocation());
    }

    public static Tuple<List<Block>, HashSet<String>> getPrivateSignNearBy(Block b) {
        var list = new ArrayList<Block>();
        var players = new HashSet<String>();
        for(var sign : getSignNearBy(b)) {
            var lines = ((Sign)sign.getState()).getLines();
            if(lines.length == 0 || !ConfigManager.privateTexts.contains(lines[0])) continue;
            list.add(sign);
            for(int i = 1; i < lines.length; i++) players.add(lines[i]);
        }
        return new Tuple<>(list, players);
    }
}
