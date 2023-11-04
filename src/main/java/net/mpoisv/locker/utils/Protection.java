package net.mpoisv.locker.utils;

import org.bukkit.block.Block;

import java.util.HashSet;

public record Protection(boolean targetIsProtectedBlock, boolean isFind, HashSet<Block> signData,
                         HashSet<String> players) {
}
