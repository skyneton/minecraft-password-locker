package net.mpoisv.locker.utils;

import org.bukkit.block.Block;

import java.util.HashSet;
import java.util.List;

public class Protection {
    private final boolean targetIsProtectedBlock;
    private final boolean isFind;
    private final HashSet<Block> signData;
    private final HashSet<String> players;
    public Protection(boolean targetIsProtectedBlock, boolean isFind, HashSet<Block> signData, HashSet<String> players) {
        this.targetIsProtectedBlock = targetIsProtectedBlock;
        this.isFind = isFind;
        this.signData = signData;
        this.players = players;
    }

    public boolean isTargetIsProtectedBlock() {
        return targetIsProtectedBlock;
    }

    public boolean getIsFind() {
        return isFind;
    }

    public HashSet<Block> getSignData() {
        return signData;
    }

    public HashSet<String> getPlayers() {
        return players;
    }
}
