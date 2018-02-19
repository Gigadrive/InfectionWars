package eu.thechest.infectionwars;

import org.bukkit.ChatColor;
import org.bukkit.Material;

/**
 * Created by zeryt on 29.05.2017.
 */
public enum Kit {
    CHEMIST("Chemist",ChatColor.GREEN,0,Material.POTION,0,true),
    ARCHER("Archer",ChatColor.RED,0,Material.BOW,0,true),
    ASSAULT("Assault",ChatColor.WHITE,0,Material.SHEARS,0,true),
    PYRO("Pyro",ChatColor.DARK_RED,0,Material.FLINT_AND_STEEL,0,true),
    FISH("Fish",ChatColor.BLUE,0,Material.RAW_FISH,0,true),
    HEAVY("Heavy",ChatColor.AQUA,0,Material.DIAMOND_CHESTPLATE,0,true),
    SURVIVOR("Survivor",ChatColor.DARK_AQUA,0,Material.DIAMOND_SWORD,0,true);

    public String name;
    public ChatColor color;
    public int price;
    public Material icon;
    public int iconDurability;
    public boolean active;

    Kit(String name, ChatColor color, int price, Material icon, int iconDurability, boolean active){
        this.name = name;
        this.color = color;
        this.price = price;
        this.icon = icon;
        this.iconDurability = iconDurability;
        this.active = active;
    }
}
