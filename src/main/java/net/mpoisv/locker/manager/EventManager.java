package net.mpoisv.locker.manager;

import net.minecraft.world.item.ItemSign;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.material.MaterialData;

public class EventManager implements Listener {
    @EventHandler
    private void onInteractEvent(PlayerInteractEvent event) {
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        var block = event.getClickedBlock();
        if(!ConfigManager.protectBlocks.contains(block.getType())) return;
        var signs = PasswordManager.getPrivateSignNearBy(block);
        var player = event.getPlayer();
        if(signs.a().isEmpty()) {
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
                event.setCancelled(true);
            }
            return;
        }
        if(signs.b().contains(player.getName())) {
            if(player.isSneaking()) {
                player.openInventory(PasswordManager.getInventory(signs.a().get(0).getLocation(), ""));
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

    }

    @EventHandler
    private void onInventoryClickEvent(InventoryClickEvent event) {
        if(event.getClickedInventory() == null || event.getCurrentItem() == null || !event.getView().getTitle().startsWith(ConfigManager.guiTitle)) return;
        event.setCancelled(true);
        if(event.getView().getTopInventory() != event.getClickedInventory() || event.getSlot() < 12) return;
        var password = PasswordManager.getPassword(event.getView().getTitle());
        if(event.getSlot() < 41) {
            var number = event.getCurrentItem().getItemMeta().getDisplayName().substring(2);
            password += number;
            ((Player)event.getWhoClicked()).playSound(event.getWhoClicked(), Sound.UI_BUTTON_CLICK, 1, 1);
            if(password.length() < ConfigManager.maxPasswordLength) {
                event.getWhoClicked().openInventory(PasswordManager.getInventory(PasswordManager.getLocation(event.getClickedInventory().getItem(0).getItemMeta()), password));
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
