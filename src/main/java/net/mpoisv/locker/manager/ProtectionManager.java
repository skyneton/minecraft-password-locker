package net.mpoisv.locker.manager;

import net.minecraft.util.Tuple;
import net.mpoisv.locker.utils.Protection;
import org.bukkit.Location;
import org.bukkit.block.*;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.WallSign;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class ProtectionManager {
    private final static BlockFace[] checkRelativeFaces = new BlockFace[] { BlockFace.UP, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST };

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
            else aPos = aPos.clone().add(0, 1, 0);
            var list = getConnectedSignBlock(aPos);
            list.addAll(getConnectedSignBlock(bPos));
            return list;
        }
        return getConnectedSignBlock(data.getLocation());
    }

    public static Protection getPrivateSignNearBy(Block b) {
        var list = new HashSet<Block>();
        var players = new HashSet<String>();
        for(var sign : getSignNearBy(b)) {
            var lines = ((Sign)sign.getState()).getLines();
            if(lines.length == 0 || !ConfigManager.privateTexts.contains(lines[0])) continue;
            list.add(sign);
            players.addAll(Arrays.asList(lines).subList(1, lines.length));
        }
        return new Protection(true, !list.isEmpty(), list, players);
    }

    public static Protection getFindPrivateSignRelative(Block b) {
        var targetIsProtect = false;
        var list = new HashSet<Block>();
        var players = new HashSet<String>();

        if(ConfigManager.protectBlocks.contains(b.getType())) {
            var target = getPrivateSignNearBy(b);
            list.addAll(target.getSignData());
            players.addAll(target.getPlayers());
            targetIsProtect = true;
        }

        if(b.getState() instanceof Sign) {
            var bd = b.getBlockData();
            if(!(bd instanceof WallSign))
                b = b.getRelative(BlockFace.DOWN);
            else
                b = b.getRelative(((WallSign) bd).getFacing().getOppositeFace());
        }else {
            if(b.getBlockData() instanceof Door)
                return new Protection(targetIsProtect, !list.isEmpty(), list, players);

            b = b.getRelative(BlockFace.UP);
        }

        if(ConfigManager.protectBlocks.contains(b.getType())) {
            var target = getPrivateSignNearBy(b);
            list.addAll(target.getSignData());
            players.addAll(target.getPlayers());
        }
        return new Protection(targetIsProtect, !list.isEmpty(), list, players);
    }
}
