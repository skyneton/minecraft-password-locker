package net.mpoisv.locker.manager;

import net.mpoisv.locker.Main;
import net.mpoisv.locker.Permissions;
import net.mpoisv.locker.utils.LockData;
import net.mpoisv.locker.utils.Position;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Objects;

public class EventManager implements Listener {
    @EventHandler
    private void onInteractEvent(PlayerInteractEvent event) {
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        var block = event.getClickedBlock();
        var protection = ProtectionManager.getFindPrivateSignRelative(block);
        var player = event.getPlayer();
        if(!protection.isFind()) {
            if(!player.isSneaking() && event.getItem() != null && event.getItem().getType().toString().endsWith("_SIGN")) {
                var face = player.getFacing().getOppositeFace();
                var placePos = block.getRelative(face.getModX(), 0, face.getModX() == 0 ? face.getModZ() : 0);
                if(!placePos.isEmpty()) return;

                placePos.setType(Material.OAK_WALL_SIGN);
                var blockData = (WallSign) placePos.getBlockData();
                blockData.setFacing(face);
                placePos.setType(event.getItem().getType());
                placePos.setBlockData(blockData);
                var sign = (Sign) placePos.getState();
                sign.setLine(0, ConfigManager.privateTexts.get(0));
                sign.setLine(1, event.getPlayer().getName());
                sign.update();

                if(player.getGameMode() != GameMode.CREATIVE) {
                    event.getItem().setAmount(event.getItem().getAmount() - 1);
                }

                event.setCancelled(true);
            }
            return;
        }
        var realPermission = protection.players().contains(player.getName()) || player.hasPermission(Permissions.BYPASS_PERMISSION);
        if(realPermission || protection.targetIsProtectedBlock() && ProtectionManager.isAllowPlayer(Position.CreatePosition(protection.signData().stream().findFirst().get().getLocation()), player.getUniqueId())) {
            if(player.isSneaking() && realPermission) {
                player.openInventory(PasswordManager.getInventory(protection.signData().stream().findFirst().get().getLocation(), ""));
                event.setCancelled(true);
                return;
            }
            if(event.getHand() == EquipmentSlot.HAND) {
                var state = block.getState();
                var blockData = state.getBlockData();
                boolean updated = false;
                if (blockData instanceof Powerable powerable) {
                    powerable.setPowered(!powerable.isPowered());
                    updated = true;
                }
                if (blockData instanceof Openable openable) {
                    openable.setOpen(!openable.isOpen());
                    updated = true;
                }
                if (updated) {
                    state.setBlockData(blockData);
                    state.update();
                }
            }
            if(!realPermission && (event.getItem() != null && event.getItem().getType().toString().endsWith("_SIGN")))  {
                player.sendMessage(ConfigManager.langPasswordUserSignUse);
                event.setCancelled(true);
            }
            return;
        }
        event.setCancelled(true);
        if(ConfigManager.passwordEnabled)
            player.openInventory(PasswordManager.getInventory(protection.signData().stream().findFirst().get().getLocation(), ""));
    }

    @EventHandler
    private void onBlockBreakEvent(BlockBreakEvent event) {
        var data = ProtectionManager.getFindPrivateSignRelative(event.getBlock());
        var realPermission = event.getPlayer().hasPermission(Permissions.BYPASS_PERMISSION)
                || ProtectionManager.isProtectedPassable(data, event.getPlayer().getName());
        if(realPermission
                || ProtectionManager.isAllowPlayer(Position.CreatePosition(data.signData().stream().findFirst().get().getLocation()), event.getPlayer().getUniqueId())) {
            data = ProtectionManager.getFindBreakablePrivateSign(event.getBlock());
            if(!realPermission && !data.targetIsProtectedBlock()) {
                event.getPlayer().sendMessage(ConfigManager.langAllowOnlyBlockUse);
                event.setCancelled(true);
                return;
            }
            var blocks = data.signData();
            if(event.getBlock().getState() instanceof Sign) {
                blocks.add(event.getBlock());
            }
            for(var it = blocks.iterator(); it.hasNext();) {
                try {
                    Main.instance.databaseManager.delete(it.next().getLocation());
                } catch (Exception e) {
                    Bukkit.getConsoleSender().sendMessage(String.format("§b:§r %s §b:§r Database error. in BlockBreakEvent(%s).", Main.instance.getDescription().getName(), e.getLocalizedMessage()));
                }
            }
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    private void onBlockExplodeEvent(BlockExplodeEvent event) {
        event.blockList().removeIf(block -> ProtectionManager.getFindPrivateSignRelative(block).isFind());
    }

    @EventHandler
    private void onBlockBurnEvent(BlockBurnEvent event) {
        if(!ProtectionManager.getFindPrivateSignRelative(event.getBlock()).isFind()) return;
        event.setCancelled(true);
    }

    @EventHandler
    private void onBlockPistonExtendEvent(BlockPistonExtendEvent event) {
        event.getBlocks().removeIf(block -> ProtectionManager.getFindPrivateSignRelative(block).isFind());
    }

    @EventHandler
    private void onBlockPistonRetractEvent(BlockPistonRetractEvent event) {
        event.getBlocks().removeIf(block -> ProtectionManager.getFindPrivateSignRelative(block).isFind());
    }

    @EventHandler
    private void onEntityChangeBlockEvent(EntityChangeBlockEvent event) {
        if(!ProtectionManager.getFindPrivateSignRelative(event.getBlock()).isFind()) return;
        event.setCancelled(true);
    }

    @EventHandler
    private void onEntityExplodeEvent(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> ProtectionManager.getFindPrivateSignRelative(block).isFind());
    }

    @EventHandler
    private void onStructureGrowEvent(StructureGrowEvent event) {
        if(event.getPlayer() != null && event.getPlayer().hasPermission(Permissions.BYPASS_PERMISSION)) return;
        event.getBlocks().removeIf(block -> ProtectionManager.getFindPrivateSignRelative(block.getBlock()).isFind());
    }

    @EventHandler
    private void onBlockPoweredEvent(BlockRedstoneEvent event) {
        if(event.getNewCurrent() == event.getOldCurrent()) return;
        var signs = ProtectionManager.getPrivateSignNearBy(event.getBlock());
        if(!signs.isFind()) return;
        event.setNewCurrent(event.getOldCurrent());
    }

    @EventHandler
    private void onInventoryClickEvent(InventoryClickEvent event) {
        if(event.getClickedInventory() == null || event.getCurrentItem() == null || !event.getView().getTitle().startsWith(ConfigManager.guiTitle)) return;
        event.setCancelled(true);
        if(event.getView().getTopInventory() != event.getClickedInventory() || event.getSlot() < 12) return;
        var password = PasswordManager.getPassword(event.getView().getTitle());
        if(event.getSlot() < 41) {
            var number = Objects.requireNonNull(event.getCurrentItem().getItemMeta()).getDisplayName().substring(2);
            password += number;
            ((Player)event.getWhoClicked()).playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
            if(password.length() < ConfigManager.maxPasswordLength) {
                event.getWhoClicked().openInventory(PasswordManager.getInventory(Objects.requireNonNull(PasswordManager.getLocation(Objects.requireNonNull(Objects.requireNonNull(event.getClickedInventory().getItem(0)).getItemMeta()))), password));
                return;
            }
        }
        if(password.length() < ConfigManager.minPasswordLength) {
            ((Player)event.getWhoClicked()).playSound(event.getWhoClicked(), Sound.BLOCK_ANVIL_LAND, 1, 1);
            return;
        }
        var loc = PasswordManager.getLocation(Objects.requireNonNull(Objects.requireNonNull(event.getClickedInventory().getItem(0)).getItemMeta()));
        var protection = ProtectionManager.getFindPrivateSignRelative(Objects.requireNonNull(loc).getBlock());
        var player = event.getWhoClicked();
        player.closeInventory();

        if(!protection.isFind()) {
            player.sendMessage(ConfigManager.langGuiError);
            return;
        }
        loc = protection.signData().stream().findFirst().get().getLocation();
        LockData selectData;
        try {
            selectData = Main.instance.databaseManager.select(loc);
        }catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(String.format("§b:§r %s §b:§r Database error. in BlockBreakEvent(%s).", Main.instance.getDescription().getName(), e.getLocalizedMessage()));
            player.sendMessage(ConfigManager.langGuiError);
            return;
        }
        if(protection.players().contains(player.getName())) {
            try {
                if(selectData == null)
                    Main.instance.databaseManager.insert(loc, password);
                else
                    Main.instance.databaseManager.update(loc, password);
            }catch(Exception e) {
                Bukkit.getConsoleSender().sendMessage(String.format("§b:§r %s §b:§r Database error. in BlockBreakEvent(%s).", Main.instance.getDescription().getName(), e.getLocalizedMessage()));
                player.sendMessage(ConfigManager.langGuiError);
                return;
            }
            player.sendMessage(ConfigManager.langPasswordChange.replace("%password%", password));
            return;
        }
        if(selectData != null && selectData.strictLock() || !PasswordManager.isPassword(selectData == null ? null : selectData.password(), password)) {
            player.sendMessage(ConfigManager.langPasswordWrong);
            return;
        }
        ProtectionManager.addAllowPlayer(Position.CreatePosition(loc), player.getUniqueId());
        player.sendMessage(ConfigManager.langPasswordCorrect.replace("%allow-time%", String.valueOf(ConfigManager.passwordAllowTime)));
    }
}
