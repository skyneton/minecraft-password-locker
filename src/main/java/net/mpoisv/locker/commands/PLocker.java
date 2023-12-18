package net.mpoisv.locker.commands;

import net.mpoisv.locker.Main;
import net.mpoisv.locker.Permissions;
import net.mpoisv.locker.VersionChecker;
import net.mpoisv.locker.manager.ConfigManager;
import net.mpoisv.locker.manager.ProtectionManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.generator.WorldInfo;

import java.util.Arrays;
import java.util.List;

public class PLocker implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(args.length == 0)
            return helpMessage(sender, s);
        switch(args[0].toLowerCase()) {
            case "help" -> { return helpMessage(sender, s); }
            case "strictlock" -> { return strictLock(sender, args); }
            case "passwordenable" -> { return passwordEnable(sender, args); }
            case "disableworld" -> {return disableWorld(sender, args); }
        }
        return false;
    }

    private boolean disableWorld(CommandSender sender, String[] args) {
        World world = null;
        var worldName = "";
        if(args.length >= 2) {
            world = Bukkit.getWorld(args[1]);
            worldName = args[1];
        }else if(sender instanceof Entity) {
            world = ((Entity) sender).getWorld();
        }
        if(world == null) {
            sender.sendMessage(String.format("§b:§r %s §b:§f %s", Main.instance.getDescription().getName(), ConfigManager.langEmptyWorld.replace("%world%", worldName)));
            var builder = new StringBuilder();
            for(World w : Bukkit.getWorlds()) {
                if(!builder.isEmpty()) builder.append(", ");
                builder.append(w.getName());
            }
            sender.sendMessage("Worlds: ", builder.toString());
            return true;
        }
        if(ConfigManager.disableWorlds.contains(world.getName())) {
            ConfigManager.disableWorlds.remove(world.getName());
            sender.sendMessage("§b:§r " + Main.instance.getDescription().getName() + " §b:§f "+ world.getName() +" is prevented now.");
        }else {
            ConfigManager.disableWorlds.add(world.getName());
            sender.sendMessage("§b:§r " + Main.instance.getDescription().getName() + " §b:§f "+ world.getName() +" is not prevent work now.");
        }

        Main.instance.getConfig().set("disable_worlds", ConfigManager.disableWorlds);
        Main.instance.saveConfig();
        return true;
    }

    private boolean helpMessage(CommandSender sender, String label) {
        var desc = Main.instance.getDescription();
        sender.sendMessage(String.format("§b:§r %s §b: §a%s§r - VER. §c%s", desc.getName(), desc.getFullName(), desc.getVersion()));
        sender.sendMessage("§b:§r " + desc.getName() + " §b: §r/" + label + " help");
        sender.sendMessage("§b:§r " + desc.getName() + " §b: §r/" + label + " strictlock [x y z] - can't use password look or position block.");
        sender.sendMessage("§b:§r " + desc.getName() + " §b: §r/" + label + " passwordenable [true/false]");
        sender.sendMessage("§b:§r " + desc.getName() + " §b: §r/" + label + " disableworld [world]");
        if(ConfigManager.updateCheck && sender.hasPermission(Permissions.UPDATE_INFO_PERMISSION) && !VersionChecker.isLatestVersion(Main.instance.getDescription().getVersion())) {
            sender.sendMessage(String.format("§b:§r %s §b:§e Latest version: %s. Update please.", Main.instance.getDescription().getName(), VersionChecker.getVersionCode()));
            sender.sendMessage(String.format("§b:§r %s §b:§e https://www.spigotmc.org/resources/passwordlocker.113386/", Main.instance.getDescription().getName()));
        }
        return true;
    }

    private boolean passwordEnable(CommandSender sender, String[] args) {
        var to = !ConfigManager.passwordEnabled;
        if(args.length >= 2) {
            switch(args[1].toLowerCase()) {
                case "0", "false" -> to = false;
                case "1", "true" -> to = true;
                default -> {
                    sender.sendMessage("§b:§r " + Main.instance.getDescription().getName() + " §b:§r must be true or false.");
                    return false;
                }
            }
        }
        sender.sendMessage("§b:§r " + Main.instance.getDescription().getName() + " §b:§r changed to " + ConfigManager.passwordEnabled + " -> " + to);
        ConfigManager.passwordEnabled = to;
        Main.instance.getConfig().set("password.enable", ConfigManager.passwordEnabled);
        Main.instance.saveConfig();
        return true;
    }

    private boolean strictLock(CommandSender sender, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage("§b:§r " + Main.instance.getDescription().getName() + " §b: §r this command can use only player.");
            return true;
        }
        if(args.length >= 2 && args.length != 5) {
            sender.sendMessage("§b:§r " + Main.instance.getDescription().getName() + " §b: §rif you use position, you should input x y z value.");
            return true;
        }
        var x = 0;
        var y = 0;
        var z = 0;
        if(args.length >= 2) {
            if(args[1].equals("~")) x = ((Player) sender).getLocation().getBlockX();
            else if(args[1].chars().allMatch(Character::isDigit))
                x = Integer.parseInt(args[1]);
            else {
                sender.sendMessage("§b:§r " + Main.instance.getDescription().getName() + " §b: §rx, y, or z value must be ~ or number.");
                return true;
            }
            if(args[2].equals("~")) y = ((Player) sender).getLocation().getBlockX();
            else if(args[2].chars().allMatch(Character::isDigit))
                y = Integer.parseInt(args[2]);
            else {
                sender.sendMessage("§b:§r " + Main.instance.getDescription().getName() + " §b: §rx, y, or z value must be ~ or number.");
                return true;
            }
            if(args[3].equals("~")) x = ((Player) sender).getLocation().getBlockX();
            else if(args[3].chars().allMatch(Character::isDigit))
                z = Integer.parseInt(args[3]);
            else {
                sender.sendMessage("§b:§r " + Main.instance.getDescription().getName() + " §b: §rx, y, or z value must be ~ or number.");
                return true;
            }
        }else {
            var targetBlock = ((Player) sender).getTargetBlockExact(10);
            x = targetBlock.getX();
            y = targetBlock.getY();
            z = targetBlock.getZ();
        }
        var protection = ProtectionManager.getFindPrivateSignRelative(new Location(((Player) sender).getWorld(), x, y, z).getBlock());
        if(!protection.isFind()) {
            sender.sendMessage("§b:§r " + Main.instance.getDescription().getName() + " §b:§r It's not protected block.");
            return true;
        }
        if(!protection.players().contains(sender.getName())) {
            sender.sendMessage("§b:§r " + Main.instance.getDescription().getName() + " §b:§r You wasn't contains protect group in protected block.");
            return true;
        }
        var loc = protection.signData().stream().findFirst().get().getLocation();
        try {
            if(Main.instance.databaseManager.select(loc) == null)
                Main.instance.databaseManager.insert(loc, true);
            else
                Main.instance.databaseManager.update(loc, true);
        }catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(String.format("§b:§r %s §b:§r Database error. in PLocker-StrictLock(%s).", Main.instance.getDescription().getName(), e.getLocalizedMessage()));
            sender.sendMessage("§b:§r " + Main.instance.getDescription().getName() + " §b: Error.");
            return true;
        }
        sender.sendMessage("§b:§r " + Main.instance.getDescription().getName() + " §b:§r Protected block changed to strict lock mode.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        switch(args.length) {
            case 1 -> { return Arrays.asList("help", "strictlock", "passwordenable"); }
            case 2 -> {
                switch(args[0].toLowerCase()) {
                    case "strictlock" -> { return List.of("~"); }
                    case "passwordenable" -> { return Arrays.asList("true", "false"); }
                    case "disableworld" -> { return Bukkit.getWorlds().stream().map(WorldInfo::getName).toList(); }
                }
            }
            case 3 -> { if(args[0].equalsIgnoreCase("strictlock")) return List.of("~"); }
            case 4 -> { if(args[0].equalsIgnoreCase("strictlock")) return List.of("~"); }
        }
        return null;
    }
}
