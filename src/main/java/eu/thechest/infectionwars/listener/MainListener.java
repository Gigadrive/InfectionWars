package eu.thechest.infectionwars.listener;

import eu.thechest.chestapi.event.FinalMapLoadedEvent;
import eu.thechest.chestapi.event.VotingEndEvent;
import eu.thechest.chestapi.items.ItemUtil;
import eu.thechest.chestapi.maps.MapRatingManager;
import eu.thechest.chestapi.maps.MapVotingManager;
import eu.thechest.chestapi.server.GameState;
import eu.thechest.chestapi.server.ServerSettingsManager;
import eu.thechest.chestapi.user.ChestUser;
import eu.thechest.chestapi.util.BountifulAPI;
import eu.thechest.chestapi.util.StringUtils;
import eu.thechest.infectionwars.InfectionWars;
import eu.thechest.infectionwars.Kit;
import eu.thechest.infectionwars.inv.KitSelector;
import eu.thechest.infectionwars.user.InfectionUser;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by zeryt on 28.05.2017.
 */
public class MainListener implements Listener {
    private int y = 20;

    @EventHandler
    public void onMapVotingFinish(VotingEndEvent e){
        ServerSettingsManager.VIP_JOIN = false;
        ServerSettingsManager.updateGameState(GameState.WARMUP);
        InfectionWars.MAP = e.getFinalMap();
        MapRatingManager.MAP_TO_RATE = e.getFinalMap();

        new BukkitRunnable(){
            @Override
            public void run() {
                if(y == 20){
                    for(Player p : Bukkit.getOnlinePlayers()){
                        BountifulAPI.sendTitle(p,0,2*20,0,ChatColor.DARK_GREEN.toString() + y,"");
                        p.playSound(p.getEyeLocation(),Sound.NOTE_BASS,1f,1f);
                    }
                } else if(y == 10){
                    for(Player p : Bukkit.getOnlinePlayers()){
                        BountifulAPI.sendTitle(p,0,2*20,0,ChatColor.DARK_GREEN.toString() + y,"");
                        p.playSound(p.getEyeLocation(),Sound.NOTE_BASS,1f,1f);
                    }
                } else if(y == 5){
                    for(Player p : Bukkit.getOnlinePlayers()){
                        BountifulAPI.sendTitle(p,0,2*20,0,ChatColor.GREEN.toString() + y,"");
                        p.playSound(p.getEyeLocation(),Sound.NOTE_BASS,1f,1f);
                    }
                } else if(y == 4){
                    for(Player p : Bukkit.getOnlinePlayers()){
                        BountifulAPI.sendTitle(p,0,2*20,0,ChatColor.YELLOW.toString() + y,"");
                        p.playSound(p.getEyeLocation(),Sound.NOTE_BASS,1f,1f);
                    }
                } else if(y == 3){
                    for(Player p : Bukkit.getOnlinePlayers()){
                        BountifulAPI.sendTitle(p,0,2*20,0,ChatColor.GOLD.toString() + y,"");
                        p.playSound(p.getEyeLocation(),Sound.NOTE_BASS,1f,1f);
                    }
                } else if(y == 2){
                    for(Player p : Bukkit.getOnlinePlayers()){
                        BountifulAPI.sendTitle(p,0,2*20,0,ChatColor.RED.toString() + y,"");
                        p.playSound(p.getEyeLocation(),Sound.NOTE_BASS,1f,1f);
                    }
                } else if(y == 1){
                    for(Player p : Bukkit.getOnlinePlayers()){
                        BountifulAPI.sendTitle(p,0,2*20,0,ChatColor.DARK_RED.toString() + y,"");
                        p.playSound(p.getEyeLocation(),Sound.NOTE_BASS,1f,1f);
                    }
                } else if(y == 0){
                    for(Player p : Bukkit.getOnlinePlayers()){
                        BountifulAPI.sendTitle(p,0,4*20,1*20,ChatColor.DARK_GREEN.toString() + ChestUser.getUser(p).getTranslatedMessage("GO!"),"");
                        p.playSound(p.getEyeLocation(),Sound.NOTE_PLING,1f,1f);
                    }

                    ServerSettingsManager.updateGameState(GameState.INGAME);
                    InfectionWars.startChestMarkerScheduler();
                    InfectionWars.increaseWave();

                    cancel();
                    return;
                }

                y--;
            }
        }.runTaskTimer(InfectionWars.getInstance(),20L,20L);

        for(Player p : Bukkit.getOnlinePlayers()){
            InfectionUser i = InfectionUser.getUser(p);
            ChestUser u = i.getUser();
            InfectionWars.SURVIVORS.add(p);
            i.reviveable = true;

            if(i.activeKit == null){
                ArrayList<Kit> a = new ArrayList<Kit>();

                for(Kit k : Kit.values()){
                    if(k.active) a.add(k);
                }

                for(Kit k : i.boughtKits){
                    if(k.active) a.add(k);
                }

                Collections.shuffle(a);

                i.activeKit = a.get(0);
            }

            p.getInventory().clear();
            p.getInventory().setArmorContents(null);
            p.setFireTicks(0);
            p.setMaxHealth(20);
            p.setHealth(p.getMaxHealth());
            p.setFoodLevel(20);
            p.setFlying(false);
            p.setAllowFlight(false);
            p.setGameMode(GameMode.SURVIVAL);

            for(PotionEffect pe : p.getActivePotionEffects()) p.removePotionEffect(pe.getType());

            p.setLevel(0);
            p.setExp(0f);

            InfectionWars.updateScoreboard(p);

            if(i.activeKit == Kit.CHEMIST){
                // TODO: Add kit
            } else if(i.activeKit == Kit.ARCHER){
                ItemStack bow = new ItemStack(Material.BOW);
                bow.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE,2);
                bow.addUnsafeEnchantment(Enchantment.ARROW_INFINITE,1);
                bow = ItemUtil.setUnbreakable(bow,true);

                p.getInventory().setItem(0,ItemUtil.setUnbreakable(new ItemStack(Material.STONE_SWORD),true));
                p.getInventory().setItem(1,bow);
                p.getInventory().setItem(9,new ItemStack(Material.ARROW));

                p.getInventory().setHelmet(ItemUtil.setUnbreakable(new ItemStack(Material.CHAINMAIL_HELMET), true));
                p.getInventory().setChestplate(ItemUtil.setUnbreakable(new ItemStack(Material.CHAINMAIL_CHESTPLATE), true));
                p.getInventory().setLeggings(ItemUtil.setUnbreakable(new ItemStack(Material.CHAINMAIL_LEGGINGS), true));
                p.getInventory().setBoots(ItemUtil.setUnbreakable(new ItemStack(Material.CHAINMAIL_BOOTS), true));
            } else if(i.activeKit == Kit.ASSAULT){
                p.getInventory().setItem(0, ItemUtil.setUnbreakable(new ItemStack(Material.IRON_SWORD), true));
                p.getInventory().setHelmet(ItemUtil.setUnbreakable(new ItemStack(Material.IRON_HELMET), true));
                p.getInventory().setChestplate(ItemUtil.setUnbreakable(new ItemStack(Material.IRON_CHESTPLATE), true));
                p.getInventory().setLeggings(ItemUtil.setUnbreakable(new ItemStack(Material.IRON_LEGGINGS), true));
                p.getInventory().setBoots(ItemUtil.setUnbreakable(new ItemStack(Material.IRON_BOOTS), true));
            } else if(i.activeKit == Kit.PYRO){
                ItemStack sword = new ItemStack(Material.WOOD_SWORD);
                sword.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);
                sword = ItemUtil.setUnbreakable(sword, true);

                ItemStack chestplate = new ItemStack(Material.GOLD_CHESTPLATE);
                chestplate.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
                chestplate = ItemUtil.setUnbreakable(chestplate, true);

                ItemStack leggings = new ItemStack(Material.CHAINMAIL_LEGGINGS);
                leggings.addUnsafeEnchantment(Enchantment.PROTECTION_FIRE, 1);
                leggings = ItemUtil.setUnbreakable(leggings, true);

                ItemStack boots = new ItemStack(Material.CHAINMAIL_BOOTS);
                boots.addUnsafeEnchantment(Enchantment.PROTECTION_FIRE, 1);
                boots = ItemUtil.setUnbreakable(boots, true);

                ItemStack bow = new ItemStack(Material.BOW);
                bow.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
                bow = ItemUtil.setUnbreakable(bow, true);

                p.getInventory().setItem(0, sword);
                p.getInventory().addItem(bow);
                p.getInventory().addItem(new ItemStack(Material.FIREBALL, 15));
                p.getInventory().setItem(9, new ItemStack(Material.ARROW));
                p.getInventory().setHelmet(ItemUtil.setUnbreakable(new ItemStack(Material.CHAINMAIL_HELMET), true));
                p.getInventory().setChestplate(chestplate);
                p.getInventory().setLeggings(leggings);
                p.getInventory().setBoots(boots);
            } else if(i.activeKit == Kit.FISH){
                ItemStack sword = new ItemStack(Material.STONE_SWORD);
                sword.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
                sword = ItemUtil.setUnbreakable(sword, true);

                ItemStack head = new ItemStack(Material.SKULL_ITEM);
                head.setDurability((short)3);
                SkullMeta headM = (SkullMeta)head.getItemMeta();
                headM.setOwner("Zander21");
                head.setItemMeta(headM);

                ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
                LeatherArmorMeta chestplateM = (LeatherArmorMeta)chestplate.getItemMeta();
                chestplateM.setColor(Color.fromRGB(0, 255, 0));
                chestplate.setItemMeta(chestplateM);
                chestplate.addUnsafeEnchantment(Enchantment.DEPTH_STRIDER, 3);
                chestplate = ItemUtil.setUnbreakable(chestplate, true);

                ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
                LeatherArmorMeta leggingsM = (LeatherArmorMeta)leggings.getItemMeta();
                leggingsM.setColor(Color.fromRGB(0, 255, 0));
                leggings.setItemMeta(leggingsM);
                leggings.addUnsafeEnchantment(Enchantment.DEPTH_STRIDER, 3);
                leggings = ItemUtil.setUnbreakable(leggings, true);

                ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
                LeatherArmorMeta bootsM = (LeatherArmorMeta)boots.getItemMeta();
                bootsM.setColor(Color.fromRGB(0, 255, 0));
                boots.setItemMeta(bootsM);
                boots.addUnsafeEnchantment(Enchantment.DEPTH_STRIDER, 3);
                boots = ItemUtil.setUnbreakable(boots, true);

                ItemStack rod = new ItemStack(Material.FISHING_ROD);
                rod = ItemUtil.setUnbreakable(rod, true);

                p.getInventory().setItem(0, sword);
                p.getInventory().addItem(rod);
                p.getInventory().setHelmet(head);
                p.getInventory().setChestplate(chestplate);
                p.getInventory().setLeggings(leggings);
                p.getInventory().setBoots(boots);
            } else if(i.activeKit == Kit.HEAVY){
                ItemStack sword = new ItemStack(Material.WOOD_SWORD);
                sword.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3);
                sword.addUnsafeEnchantment(Enchantment.KNOCKBACK, 2);
                sword = ItemUtil.setUnbreakable(sword, true);

                ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET);
                helmet.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
                helmet = ItemUtil.setUnbreakable(helmet, true);

                ItemStack chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
                chestplate.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
                chestplate = ItemUtil.setUnbreakable(chestplate, true);

                ItemStack leggings = new ItemStack(Material.DIAMOND_LEGGINGS);
                leggings.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
                leggings = ItemUtil.setUnbreakable(leggings, true);

                ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);
                boots.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
                boots = ItemUtil.setUnbreakable(boots, true);

                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 1));

                p.getInventory().setItem(0, sword);
                p.getInventory().setHelmet(helmet);
                p.getInventory().setChestplate(chestplate);
                p.getInventory().setLeggings(leggings);
                p.getInventory().setBoots(boots);
            } else if(i.activeKit == Kit.SURVIVOR){
                ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
                sword.addUnsafeEnchantment(Enchantment.DAMAGE_ARTHROPODS, 5);
                sword.addUnsafeEnchantment(Enchantment.DAMAGE_UNDEAD, 5);
                sword = ItemUtil.setUnbreakable(sword, true);

                ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);
                boots.addUnsafeEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                boots.addUnsafeEnchantment(Enchantment.PROTECTION_FALL, 4);
                boots = ItemUtil.setUnbreakable(boots, true);

                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));

                p.getInventory().setItem(0, sword);
                p.getInventory().setBoots(boots);
            }

            p.getInventory().addItem(new ItemStack(Material.APPLE, 5));
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e){
        Player p = e.getPlayer();
        if(ServerSettingsManager.CURRENT_GAMESTATE == GameState.WARMUP){
            Location from = e.getFrom();
            Location to = e.getTo();
            double x = Math.floor(from.getX());
            double z = Math.floor(from.getZ());

            if(Math.floor(to.getX()) != x || Math.floor(to.getZ()) != z){
                x += .5;
                z += .5;
                e.getPlayer().teleport(new Location(from.getWorld(),x,from.getY(),z,from.getYaw(),from.getPitch()));
            }
        }
    }

    @EventHandler
    public void onSpread(BlockSpreadEvent e){
        e.setCancelled(true);
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent e){
        if(e.getEntity().getType() == EntityType.CREEPER){
            e.setYield(0f);
            InfectionWars.callEntityDeath((LivingEntity)e.getEntity());
        } else if(e.getEntity().getType() == EntityType.FIREBALL){
            ((Fireball)e.getEntity()).setBounce(false);
            ((Fireball)e.getEntity()).setYield(0f);
            e.setYield(0f);
        } else if(e.getEntity().getType() == EntityType.PRIMED_TNT){
            ((TNTPrimed)e.getEntity()).setYield(0f);
            e.setYield(0f);
        } else if(e.getEntity().getType() == EntityType.WITHER_SKULL){
            ((WitherSkull)e.getEntity()).setYield(0f);
            e.setYield(0f);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e){
        Player p = e.getEntity();
        InfectionUser i = InfectionUser.getUser(p);
        ChestUser u = i.getUser();

        e.setDeathMessage(null);

        for(Player all : Bukkit.getOnlinePlayers()){
            all.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GOLD + ChestUser.getUser(all).getTranslatedMessage("%p died.").replace("%p",p.getDisplayName() + ChatColor.GOLD));
        }

        i.addDeaths(1);
        i.reducePoints(7);

        BountifulAPI.sendTitle(p,10,5*20,2*20,ChatColor.RED.toString() + ChatColor.BOLD.toString() + u.getTranslatedMessage("You died!").toUpperCase(),"");
        InfectionWars.spectator(p);

        if(InfectionWars.SURVIVORS.size() == 0) InfectionWars.callEnd();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        Player p = e.getPlayer();
        InfectionUser i = InfectionUser.getUser(p);
        ChestUser u = i.getUser();

        if(InfectionWars.SPECTATORS.contains(p)){
            InfectionWars.SPECTATORS.remove(p);
        }

        if(InfectionWars.SURVIVORS.contains(p)){
            InfectionWars.SURVIVORS.remove(p);
            StringUtils.sendQuitMessage(p);
            if(InfectionWars.SURVIVORS.size() == 0) InfectionWars.callEnd();
        }
    }

    @EventHandler
    public void onShootBow(EntityShootBowEvent e){
        if(e.getEntity() instanceof Player){
            if(ServerSettingsManager.CURRENT_GAMESTATE != GameState.INGAME) e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e){
        LivingEntity ent = e.getEntity();

        e.getDrops().clear();
        e.setDroppedExp(0);

        if(InfectionWars.ENTITIES.contains(ent)){
            InfectionWars.callEntityDeath(ent);

            if(e.getEntity().getKiller() != null){
                InfectionUser.getUser(e.getEntity().getKiller()).addKills(1);

                for(Player all : Bukkit.getOnlinePlayers()){
                    InfectionWars.updateScoreboard(all);
                }
            }
        }
    }

    @EventHandler
    public void onFinalMapLoaded(FinalMapLoadedEvent e){
        InfectionWars.prepareWorld(Bukkit.getWorld(e.getFinalMap().getOriginalWorldName()),false);
    }

    @EventHandler
    public void onLoad(WorldLoadEvent e){
        if(e.getWorld().getName().equals("WarteLobby")){
            InfectionWars.prepareWorld(e.getWorld());
        } else {
            InfectionWars.prepareWorld(e.getWorld(),false);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e){
        if(ServerSettingsManager.CURRENT_GAMESTATE == GameState.LOBBY){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e){
        Player p = e.getPlayer();
        InfectionUser i = InfectionUser.getUser(p);
        ChestUser u = i.getUser();

        if(ServerSettingsManager.CURRENT_GAMESTATE == GameState.INGAME || ServerSettingsManager.CURRENT_GAMESTATE == GameState.WARMUP){
            if(InfectionWars.SURVIVORS.contains(p)){
                if(e.getItemDrop() != null && e.getItemDrop().getItemStack() != null){
                    Material m = e.getItemDrop().getItemStack().getType();
                    ArrayList<Material> a = new ArrayList<Material>();

                    a.add(Material.TNT);
                    a.add(Material.COOKED_BEEF);
                    a.add(Material.APPLE);
                    a.add(Material.COOKED_CHICKEN);
                    a.add(Material.BREAD);

                    if(!a.contains(m)){
                        e.setCancelled(true);
                    }
                }
            } else {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void removeEmptyBottles(final PlayerItemConsumeEvent e){
        if (e.getItem().getData().getItemType().equals(Material.POTION)) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(InfectionWars.getInstance(), new Runnable(){
                public void run() {
                    e.getPlayer().getInventory().setItem(e.getPlayer().getInventory().first(new ItemStack(Material.GLASS_BOTTLE)), new ItemStack(Material.AIR));
                }
            }, 2L);

        }
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent e){
        Player p = e.getPlayer();
        InfectionUser i = InfectionUser.getUser(p);
        ChestUser u = i.getUser();

        if(ServerSettingsManager.CURRENT_GAMESTATE == GameState.INGAME || ServerSettingsManager.CURRENT_GAMESTATE == GameState.WARMUP){
            if(!InfectionWars.SURVIVORS.contains(p)){
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e){
        e.setCancelled(true);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e){
        e.setCancelled(true);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        Player p = e.getPlayer();
        InfectionUser i = InfectionUser.getUser(p);
        ChestUser u = i.getUser();

        if(ServerSettingsManager.CURRENT_GAMESTATE == GameState.LOBBY){
            if(MapVotingManager.VOTING_OPEN){
                p.getInventory().addItem(ItemUtil.namedItem(Material.NETHER_STAR,ChatColor.AQUA + u.getTranslatedMessage("Select your class"),null));
            }
        } else {
            p.teleport(InfectionWars.MAP.getSpawnpoints().get(0).toBukkitLocation(InfectionWars.MAP.getOriginalWorldName()));
            InfectionWars.spectator(p);
        }
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent e){
        e.setCancelled(true);
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent e){
        if(ServerSettingsManager.CURRENT_GAMESTATE == GameState.LOBBY) e.setCancelled(true);
    }

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent e){
        if(e.getEntity() instanceof Player && e.getDamager() instanceof Player){
            e.setCancelled(true);
        }

        if(e.getEntity() instanceof Player && e.getDamager() instanceof Arrow && ((Arrow)e.getDamager()).getShooter() instanceof Player){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        event.setFire(false);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e){
        Player p = e.getPlayer();
        InfectionUser i = InfectionUser.getUser(p);
        ChestUser u = i.getUser();
        ItemStack hand = p.getItemInHand();

        if(e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR){
            if(hand != null && hand.getType() != null && hand.getItemMeta() != null && hand.getItemMeta().getDisplayName() != null){
                String dis = hand.getItemMeta().getDisplayName();

                if(dis.equals(ChatColor.AQUA + u.getTranslatedMessage("Select your class"))){
                    if(ServerSettingsManager.CURRENT_GAMESTATE == GameState.LOBBY && MapVotingManager.VOTING_OPEN){
                        KitSelector.openFor(p);
                    } else {
                        e.setCancelled(true);
                        e.setUseInteractedBlock(Event.Result.DENY);
                        e.setUseItemInHand(Event.Result.DENY);

                        p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + u.getTranslatedMessage("You can't choose your kit right now."));
                    }
                }
            } else if(hand != null && hand.getType() != null){
                if(hand.getType() == Material.TNT){
                    if(ServerSettingsManager.CURRENT_GAMESTATE == GameState.INGAME){
                        World world = p.getWorld();
                        double speedFactor = 1.5D;
                        Location handLocation = p.getLocation();
                        handLocation.setY(handLocation.getY() + 1.0D);

                        Vector direction = handLocation.getDirection();

                        Entity entity = null;
                        entity = world.spawn(handLocation, TNTPrimed.class);
                        entity.setVelocity(direction.multiply(speedFactor));

                        if(p.getItemInHand().getAmount() == 1){
                            p.getInventory().remove(p.getItemInHand());
                        } else {
                            p.getItemInHand().setAmount(p.getItemInHand().getAmount() - 1);
                            p.updateInventory();
                        }
                    }
                }
            }
        }

        if(e.getAction() == Action.RIGHT_CLICK_BLOCK){
            if(ServerSettingsManager.CURRENT_GAMESTATE == GameState.INGAME){
                if(e.getClickedBlock() != null && e.getClickedBlock().getType() != null && e.getClickedBlock().getType() == Material.REDSTONE_BLOCK){
                    e.setCancelled(true);
                    e.setUseItemInHand(Event.Result.DENY);
                    e.setUseInteractedBlock(Event.Result.DENY);

                    if(InfectionWars.CHEST_INVENTORIES.containsKey(e.getClickedBlock().getLocation()) && InfectionWars.CHEST_INVENTORIES.get(e.getClickedBlock().getLocation()) != null){
                        p.openInventory(InfectionWars.CHEST_INVENTORIES.get(e.getClickedBlock().getLocation()));
                    } else {
                        Random rnd = new Random();
                        int n = 1;
                        n = StringUtils.randomInteger(1, 7);
                        Inventory inv = Bukkit.createInventory(null, InventoryType.CHEST);
                        List<ItemStack> items = new ArrayList<ItemStack>();

                        if(InfectionWars.CURRENT_WAVE == 1 || InfectionWars.CURRENT_WAVE == 2 || InfectionWars.CURRENT_WAVE == 3 || InfectionWars.CURRENT_WAVE == 4 || InfectionWars.CURRENT_WAVE == 5){
                            items.add(new ItemStack(Material.COOKED_CHICKEN, StringUtils.randomInteger(1, 8)));
                            items.add(new ItemStack(Material.COOKED_BEEF, StringUtils.randomInteger(1, 3)));
                            items.add(new ItemStack(Material.BREAD, StringUtils.randomInteger(1, 2)));
                            items.add(new ItemStack(Material.APPLE, StringUtils.randomInteger(1, 4)));
                        } else if(InfectionWars.CURRENT_WAVE == 6 || InfectionWars.CURRENT_WAVE == 7 || InfectionWars.CURRENT_WAVE == 8 || InfectionWars.CURRENT_WAVE == 9 || InfectionWars.CURRENT_WAVE == 10){
                            items.add(new ItemStack(Material.COOKED_CHICKEN, StringUtils.randomInteger(1, 8)));
                            items.add(new ItemStack(Material.COOKED_BEEF, StringUtils.randomInteger(1, 3)));
                            items.add(new ItemStack(Material.BREAD, StringUtils.randomInteger(1, 2)));
                            items.add(new ItemStack(Material.APPLE, StringUtils.randomInteger(1, 4)));
                        } else if(InfectionWars.CURRENT_WAVE == 11 || InfectionWars.CURRENT_WAVE == 12 || InfectionWars.CURRENT_WAVE == 13 || InfectionWars.CURRENT_WAVE == 14 || InfectionWars.CURRENT_WAVE == 15){
                            items.add(new ItemStack(Material.TNT, StringUtils.randomInteger(1, 2)));
                            items.add(new ItemStack(Material.COOKED_CHICKEN, StringUtils.randomInteger(1, 8)));
                            items.add(new ItemStack(Material.COOKED_BEEF, StringUtils.randomInteger(1, 3)));
                            items.add(new ItemStack(Material.BREAD, StringUtils.randomInteger(1, 2)));
                            items.add(new ItemStack(Material.APPLE, StringUtils.randomInteger(1, 4)));
                        } else if(InfectionWars.CURRENT_WAVE == 16 || InfectionWars.CURRENT_WAVE == 17 || InfectionWars.CURRENT_WAVE == 18 || InfectionWars.CURRENT_WAVE == 19 || InfectionWars.CURRENT_WAVE == 20){
                            items.add(new ItemStack(Material.TNT, StringUtils.randomInteger(2, 4)));
                            items.add(new ItemStack(Material.COOKED_CHICKEN, StringUtils.randomInteger(1, 8)));
                            items.add(new ItemStack(Material.COOKED_BEEF, StringUtils.randomInteger(1, 3)));
                            items.add(new ItemStack(Material.BREAD, StringUtils.randomInteger(1, 2)));
                            items.add(new ItemStack(Material.APPLE, StringUtils.randomInteger(1, 4)));
                        }

                        while(n != 0){
                            n--;
                            inv.setItem(StringUtils.randomInteger(1, 26), items.get(StringUtils.randomInteger(0, items.size()-1)));
                        }

                        if(InfectionWars.CHEST_INVENTORIES.containsKey(e.getClickedBlock().getLocation())) InfectionWars.CHEST_INVENTORIES.remove(e.getClickedBlock().getLocation());
                        InfectionWars.CHEST_INVENTORIES.put(e.getClickedBlock().getLocation(), inv);
                        p.openInventory(inv);
                        return;
                    }
                }
            }
        }
    }
}
