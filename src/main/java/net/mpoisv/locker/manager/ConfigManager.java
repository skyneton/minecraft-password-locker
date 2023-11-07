package net.mpoisv.locker.manager;

import org.bukkit.Material;

import java.util.HashSet;
import java.util.List;

public class ConfigManager {
    public static List<String> privateTexts;
    public static int minPasswordLength, maxPasswordLength;
    public static boolean passwordEnabled;
    public static long passwordAllowTime;
    public static boolean updateCheck;
    public static String guiTitle, guiSeparator, guiPasswordFinishBtn;
    public static HashSet<Material> protectBlocks;

    public static String langGuiError;
    public static String langPasswordChange;
    public static String langPasswordWrong;
    public static String langPasswordCorrect;
    public static String langAllowOnlyBlockUse;
    public static String langPasswordUserSignUse;
}
