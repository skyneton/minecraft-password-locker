package net.mpoisv.locker.manager;

import net.mpoisv.locker.Permissions;
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
        if(!ConfigManager.protectBlocks.contains(Objects.requireNonNull(block).getType())) return;
        var protection = ProtectionManager.getPrivateSignNearBy(block);
        var player = event.getPlayer();
        if(!protection.getIsFind()) {
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
        if(protection.getPlayers().contains(player.getName()) || event.getPlayer().hasPermission(Permissions.BYPASS_PERMISSION)) {
            if(player.isSneaking()) {
                player.openInventory(PasswordManager.getInventory(protection.getSignData().stream().findFirst().get().getLocation(), ""));
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
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    private void onBlockBreakEvent(BlockBreakEvent event) {
        if(event.getPlayer().hasPermission(Permissions.BYPASS_PERMISSION)) return;
        var protection = ProtectionManager.getFindPrivateSignRelative(event.getBlock());
        if(!protection.getIsFind() || protection.getPlayers().contains(event.getPlayer().getName())) return;
        event.setCancelled(true);
    }

    @EventHandler
    private void onBlockExplodeEvent(BlockExplodeEvent event) {
        event.blockList().removeIf(block -> ProtectionManager.getFindPrivateSignRelative(block).getIsFind());
    }

    @EventHandler
    private void onBlockBurnEvent(BlockBurnEvent event) {
        if(!ProtectionManager.getFindPrivateSignRelative(event.getBlock()).getIsFind()) return;
        event.setCancelled(true);
    }

    @EventHandler
    private void onBlockPistonExtendEvent(BlockPistonExtendEvent event) {
        event.getBlocks().removeIf(block -> ProtectionManager.getFindPrivateSignRelative(block).getIsFind());
    }

    @EventHandler
    private void onBlockPistonRetractEvent(BlockPistonRetractEvent event) {
        event.getBlocks().removeIf(block -> ProtectionManager.getFindPrivateSignRelative(block).getIsFind());
    }

    @EventHandler
    private void onEntityChangeBlockEvent(EntityChangeBlockEvent event) {
        if(!ProtectionManager.getFindPrivateSignRelative(event.getBlock()).getIsFind()) return;
        event.setCancelled(true);
    }

    @EventHandler
    private void onEntityExplodeEvent(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> ProtectionManager.getFindPrivateSignRelative(block).getIsFind());
    }

    @EventHandler
    private void onStructureGrowEvent(StructureGrowEvent event) {
        if(event.getPlayer() != null && event.getPlayer().hasPermission(Permissions.BYPASS_PERMISSION)) return;
        event.getBlocks().removeIf(block -> ProtectionManager.getFindPrivateSignRelative(block.getBlock()).getIsFind());
    }

    @EventHandler
    private void onBlockPoweredEvent(BlockRedstoneEvent event) {
        if(event.getNewCurrent() == event.getOldCurrent()) return;
        var signs = ProtectionManager.getPrivateSignNearBy(event.getBlock());
        if(!signs.getIsFind()) return;
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
        //TODO: CHECK PASSWORD OR SAVE PASSWORD
    }
}
