package eu.thechest.infectionwars.inv;

import eu.thechest.chestapi.items.ItemUtil;
import eu.thechest.chestapi.maps.MapVotingManager;
import eu.thechest.chestapi.server.GameState;
import eu.thechest.chestapi.server.ServerSettingsManager;
import eu.thechest.chestapi.user.ChestUser;
import eu.thechest.infectionwars.Kit;
import eu.thechest.infectionwars.user.InfectionUser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.Arrays;

/**
 * Created by zeryt on 29.05.2017.
 */
public class KitSelector implements Listener {
    public static void openFor(Player p){
        InfectionUser i = InfectionUser.getUser(p);
        ChestUser u = ChestUser.getUser(p);
        Inventory inv = Bukkit.createInventory(null,9,"Class Selector");

        for(Kit kit : Kit.values()){
            if(!kit.active) continue;
            if(kit.price <= 0 || i.boughtKits.contains(kit)){
                inv.addItem(ItemUtil.hideFlags(ItemUtil.namedItem(kit.icon, kit.color + u.getTranslatedMessage(kit.name + " Class"), new String[]{" ", net.md_5.bungee.api.ChatColor.DARK_GREEN + ">> " + net.md_5.bungee.api.ChatColor.GREEN + u.getTranslatedMessage("Click to play with this kit.")}, kit.iconDurability)));
            } else {
                inv.addItem(ItemUtil.hideFlags(ItemUtil.namedItem(kit.icon, kit.color + u.getTranslatedMessage(kit.name + " Class"), new String[]{" ", net.md_5.bungee.api.ChatColor.DARK_RED + ">> " + net.md_5.bungee.api.ChatColor.RED + u.getTranslatedMessage("Click to buy this kit for %p.").replace("%p", net.md_5.bungee.api.ChatColor.GOLD.toString() + kit.price + " " + u.getTranslatedMessage("Coins") + net.md_5.bungee.api.ChatColor.RED)}, kit.iconDurability)));
            }
        }

        p.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e){
        if(e.getWhoClicked() instanceof Player){
            Player p = (Player)e.getWhoClicked();
            InfectionUser i = InfectionUser.getUser(p);
            ChestUser u = ChestUser.getUser(p);
            Inventory inv = e.getInventory();
            int slot = e.getRawSlot();

            if(inv.getName().equals("Class Selector")){
                if(MapVotingManager.VOTING_OPEN && ServerSettingsManager.CURRENT_GAMESTATE == GameState.LOBBY){
                    if(e.getCurrentItem() != null && e.getCurrentItem().getItemMeta() != null && e.getCurrentItem().getItemMeta().getDisplayName() != null){
                        if(Kit.values().length > slot){
                            Kit kit = Arrays.asList(Kit.values()).get(slot);

                            if(kit.price <= 0 || i.boughtKits.contains(kit)){
                                i.activeKit = kit;
                                p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GREEN + u.getTranslatedMessage("You've selected the %k.").replace("%k",kit.color + u.getTranslatedMessage(kit.name + " Class") + ChatColor.GREEN));
                                e.setCancelled(true);
                                p.closeInventory();
                                p.playSound(p.getEyeLocation(), Sound.NOTE_PLING,1f,1f);
                            } else {
                                // TODO: Add kit buying menu
                                p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("Buying classes isn't possible right now."));
                            }
                        } else {
                            e.setCancelled(true);
                        }
                    } else {
                        e.setCancelled(true);
                    }
                } else {
                    e.setCancelled(true);
                    p.closeInventory();
                    p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("You can't choose your class right now."));
                }
            }
        }
    }
}
