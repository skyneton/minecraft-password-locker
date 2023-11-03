package net.mpoisv.locker.manager;

import org.bukkit.Material;

import java.util.HashSet;
import java.util.List;

public class ConfigManager {
    public static List<String> privateTexts;
    public static int minPasswordLength, maxPasswordLength;
    public static boolean passwordEnabled;
    public static String guiTitle, guiSeparator, guiPasswordFinishBtn;
    public static HashSet<Material> protectBlocks;
}
