package net.mpoisv.locker.commands;

import net.mpoisv.locker.Main;
import net.mpoisv.locker.manager.ProtectionManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

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
        }
        return false;
    }

    private boolean helpMessage(CommandSender sender, String label) {
        var desc = Main.instance.getDescription();
        sender.sendMessage(String.format("§b:§r %s §b: §a%s§r - VER. §c%s", desc.getName(), desc.getDescription(), desc.getVersion()));
        sender.sendMessage("§b:§r " + desc.getName() + " §b: §r/" + label + " help");
        sender.sendMessage("§b:§r " + desc.getName() + " §b: §r/" + label + " strictlock [x y z] - can't use password look or position block.");
        return true;
    }

    private boolean strictLock(CommandSender sender, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage("§b:§r " + Main.instance.getDescription().getName() + " §b: §r this command can use only player.");
            return true;
        }
        if(args.length >= 2 && args.length != 4) {
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
            sender.sendMessage("§b:§r " + Main.instance.getDescription().getName() + " §b: It's not protected block.");
            return true;
        }
        if(!protection.players().contains(sender.getName())) {
            sender.sendMessage("§b:§r " + Main.instance.getDescription().getName() + " §b: You wasn't contains protect group in protected block.");
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
        sender.sendMessage("§b:§r " + Main.instance.getDescription().getName() + " §b: Protected block changed to strict lock mode.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        switch(args.length) {
            case 1 -> { return Arrays.asList("help", "strictlock"); }
            case 2 -> { if(args[0].equalsIgnoreCase("strictlock")) return List.of("~"); }
            case 3 -> { if(args[0].equalsIgnoreCase("strictlock")) return List.of("~"); }
            case 4 -> { if(args[0].equalsIgnoreCase("strictlock")) return List.of("~"); }
        }
        return null;
    }
}
