package eu.thechest.infectionwars;

import eu.thechest.chestapi.maps.Map;
import eu.thechest.chestapi.maps.MapLocationData;
import eu.thechest.chestapi.maps.MapLocationType;
import eu.thechest.chestapi.maps.MapVotingManager;
import eu.thechest.chestapi.server.GameState;
import eu.thechest.chestapi.server.GameType;
import eu.thechest.chestapi.server.ServerSettingsManager;
import eu.thechest.chestapi.user.ChestUser;
import eu.thechest.chestapi.user.Rank;
import eu.thechest.chestapi.util.BountifulAPI;
import eu.thechest.chestapi.util.StringUtils;
import eu.thechest.infectionwars.inv.KitSelector;
import eu.thechest.infectionwars.listener.MainListener;
import eu.thechest.infectionwars.user.InfectionUser;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by zeryt on 28.05.2017.
 */
public class InfectionWars extends JavaPlugin {
    public static HashMap<Location, Inventory> CHEST_INVENTORIES = new HashMap<Location, Inventory>();

    public static ArrayList<Player> SURVIVORS = new ArrayList<Player>();
    public static ArrayList<Player> SPECTATORS = new ArrayList<Player>();

    public static int CURRENT_WAVE = 0;
    public static ArrayList<LivingEntity> ENTITIES = new ArrayList<LivingEntity>();

    private static InfectionWars instance;
    public static Map MAP;

    public static int nextWaveCount;

    public void onEnable(){
        instance = this;

        ServerSettingsManager.RUNNING_GAME = GameType.INFECTION_WARS;
        ServerSettingsManager.MAP_VOTING = true;
        ServerSettingsManager.SHOW_FAME_TITLE_ABOVE_HEAD = true;
        ServerSettingsManager.MIN_PLAYERS = 1;
        ServerSettingsManager.setMaxPlayers(12);
        ServerSettingsManager.CURRENT_GAMESTATE = GameState.LOBBY;
        ServerSettingsManager.PROTECT_ARMORSTANDS = true;
        ServerSettingsManager.PROTECT_ITEM_FRAMES = true;
        ServerSettingsManager.VIP_JOIN = true;
        ServerSettingsManager.KILL_EFFECTS = true;
        ServerSettingsManager.ARROW_TRAILS = true;
        ServerSettingsManager.UPDATE_TAB_NAME_WITH_SCOREBOARD = true;
        ServerSettingsManager.ALLOW_MULITPLE_MAPS = false;

        MapVotingManager.chooseMapsForVoting();

        Bukkit.getPluginManager().registerEvents(new MainListener(), this);
        Bukkit.getPluginManager().registerEvents(new KitSelector(), this);

        for(World w : Bukkit.getWorlds()){
            prepareWorld(w);
        }
    }

    public static InfectionWars getInstance(){
        return instance;
    }

    public static void callEnd(){
        if(ServerSettingsManager.CURRENT_GAMESTATE != GameState.ENDING){
            ServerSettingsManager.updateGameState(GameState.ENDING);
            for(Player all : Bukkit.getOnlinePlayers()){
                all.playSound(all.getEyeLocation(), Sound.ENDERDRAGON_DEATH, 1F, 1F);

                if((InfectionUser.getUser(all).getPoints()-InfectionUser.getUser(all).getStartPoints()) > 0){
                    double score = (InfectionUser.getUser(all).getPoints()-InfectionUser.getUser(all).getStartPoints()) / 2;
                    InfectionUser.getUser(all).addPoints(((Double)score).intValue());
                }

                // TODO: Store highest wave

                BountifulAPI.sendTitle(all,1*20,10*20,1*20,ChatColor.DARK_RED + ChestUser.getUser(all).getTranslatedMessage("GAME OVER!"),"");
                all.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + ChestUser.getUser(all).getTranslatedMessage("You survived until wave %w!").replace("%w",String.valueOf(InfectionWars.CURRENT_WAVE)));
            }
            Bukkit.getScheduler().scheduleSyncDelayedTask(InfectionWars.getInstance(), new Runnable(){
                public void run(){
                    for(Player all : Bukkit.getOnlinePlayers()){
                        ChestUser.getUser(all).connectToLobby();
                    }
                }
            }, 10*20);
            Bukkit.getScheduler().scheduleSyncDelayedTask(InfectionWars.getInstance(), new Runnable(){
                public void run(){
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart");
                }
            }, 13*20);
        }
    }

    public static void callWin(){
        if(ServerSettingsManager.CURRENT_GAMESTATE != GameState.ENDING){
            ServerSettingsManager.updateGameState(GameState.ENDING);
            for(Player all : Bukkit.getOnlinePlayers()){
                all.playSound(all.getEyeLocation(), Sound.PORTAL_TRAVEL, 1F, 1F);

                InfectionUser.getUser(all).addVictories(1);
                // TODO: Store highest wave

                ChestUser.getUser(all).addCoins(100);

                BountifulAPI.sendTitle(all,1*20,10*20,1*20,ChatColor.DARK_GREEN + ChestUser.getUser(all).getTranslatedMessage("YOU WON!"),"");
                all.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + ChestUser.getUser(all).getTranslatedMessage("You survived all the waves!"));
                all.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + ChestUser.getUser(all).getTranslatedMessage("Congratulations!"));
            }

            Bukkit.getScheduler().scheduleSyncDelayedTask(InfectionWars.getInstance(), new Runnable(){
                public void run(){
                    for(Player all : Bukkit.getOnlinePlayers()){
                        ChestUser.getUser(all).connectToLobby();
                    }
                }
            }, 10*20);
            Bukkit.getScheduler().scheduleSyncDelayedTask(InfectionWars.getInstance(), new Runnable(){
                public void run(){
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart");
                }
            }, 13*20);
        }
    }

    public static void callEntityDeath(LivingEntity e){
        InfectionWars.ENTITIES.remove(e);

        if(InfectionWars.ENTITIES.size() == 0){
            if(InfectionWars.CURRENT_WAVE == 20){
                callWin();
            } else {
                for(Player all : Bukkit.getOnlinePlayers()){
                    ChestUser a = ChestUser.getUser(all);
                    all.setLevel(0);
                    all.setExp(0);
                    all.playSound(all.getEyeLocation(), Sound.ORB_PICKUP, 1F, 1F);
                    all.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GOLD + a.getTranslatedMessage("You have survived wave number %w!").replace("%w",String.valueOf(InfectionWars.CURRENT_WAVE)));
                    all.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GOLD + a.getTranslatedMessage("%c chests spawned on the map!").replace("%c",String.valueOf(InfectionWars.CURRENT_WAVE)));

                    a.addCoins(5*InfectionWars.CURRENT_WAVE);
                }
                ArrayList<MapLocationData> a = MAP.getLocations(MapLocationType.INFW_CHESTSPAWN);
                for (int i = 0; i < InfectionWars.CURRENT_WAVE; i++) {
                    Collections.shuffle(a);

                    Collections.shuffle(a);
                    a.get(0).toBukkitLocation(InfectionWars.MAP.getOriginalWorldName()).getBlock().setType(Material.REDSTONE_BLOCK);
                    InfectionWars.CHEST_INVENTORIES.put(a.get(0).toBukkitLocation(InfectionWars.MAP.getOriginalWorldName()),null);
                }

                nextWaveCount = 60;

                new BukkitRunnable(){
                    public void run(){
                        if(nextWaveCount >= 0){
                            if(nextWaveCount == 60 || nextWaveCount == 50 || nextWaveCount == 40 || nextWaveCount == 30 || nextWaveCount == 20 || nextWaveCount == 10 || nextWaveCount == 9 || nextWaveCount == 8 || nextWaveCount == 7 || nextWaveCount == 6 || nextWaveCount == 5 || nextWaveCount == 4 || nextWaveCount == 3 || nextWaveCount == 2){
                                for(Player p : Bukkit.getOnlinePlayers()){
                                    ChestUser u = ChestUser.getUser(p);

                                    p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GOLD + u.getTranslatedMessage("Next wave starts in %s seconds!").replace("%s",String.valueOf(nextWaveCount)));
                                }
                            } else if(nextWaveCount == 1){
                                for(Player p : Bukkit.getOnlinePlayers()){
                                    ChestUser u = ChestUser.getUser(p);

                                    p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GOLD + u.getTranslatedMessage("Next wave starts in %s second!").replace("%s",String.valueOf(nextWaveCount)));
                                }
                            } else if(nextWaveCount == 0){
                                InfectionWars.increaseWave();
                                for(Player p : Bukkit.getOnlinePlayers()){
                                    ChestUser u = ChestUser.getUser(p);

                                    p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GOLD + u.getTranslatedMessage("Next wave starts NOW!").replace("%s",String.valueOf(nextWaveCount)));
                                }
                                InfectionWars.ENTITIES.get(0).getLocation().getWorld().strikeLightningEffect(InfectionWars.ENTITIES.get(0).getLocation());
                                for(Player all : Bukkit.getOnlinePlayers()){
                                    all.setLevel(InfectionWars.ENTITIES.size());
                                    all.setExp((float) ((double) InfectionWars.ENTITIES.size() / (double)InfectionWars.getMobsPerWave(InfectionWars.CURRENT_WAVE).size()));
                                }
                                cancel();
                            }

                            nextWaveCount--;
                        }
                    }
                }.runTaskTimer(InfectionWars.getInstance(),20L,20L);
            }
        } else {
            for(Player all : Bukkit.getOnlinePlayers()){
                all.setLevel(InfectionWars.ENTITIES.size());
                all.setExp((float) ((double) InfectionWars.ENTITIES.size() / (double)InfectionWars.getMobsPerWave(InfectionWars.CURRENT_WAVE).size()));
                all.playSound(all.getEyeLocation(), Sound.ORB_PICKUP, 1F, 1F);
            }
        }
    }

    public static void updateScoreboard(Player p){
        InfectionUser i = InfectionUser.getUser(p);
        ChestUser u = i.getUser();

        Objective ob = null;
        if(u.getScoreboard().getObjective(DisplaySlot.SIDEBAR) != null){
            ob = u.getScoreboard().getObjective(DisplaySlot.SIDEBAR);
        } else {
            ob = u.getScoreboard().registerNewObjective("side","dummy");
            ob.setDisplayName(ServerSettingsManager.RUNNING_GAME.getColor() + ServerSettingsManager.RUNNING_GAME.getName());
            ob.setDisplaySlot(DisplaySlot.SIDEBAR);
        }

        for(Player s : SURVIVORS){
            InfectionUser is = InfectionUser.getUser(s);
            ChestUser us = is.getUser();

            if(us.isNicked()){
                if(u.hasPermission(Rank.VIP)){
                    ob.getScore(s.getName()).setScore(is.getKills()-is.getStartKills());
                } else {
                    ob.getScore(us.getNick()).setScore(is.getKills()-is.getStartKills());
                }
            } else {
                ob.getScore(s.getName()).setScore(is.getKills()-is.getStartKills());
            }
        }

        ob.getScore(StringUtils.SCOREBOARD_LINE_SEPERATOR).setScore(-1);
        ob.getScore(StringUtils.SCOREBOARD_FOOTER_IP).setScore(-2);
    }

    public static void spectator(Player p){
        if(SPECTATORS.contains(p)) return;
        if(SURVIVORS.contains(p)) SURVIVORS.remove(p);

        SPECTATORS.add(p);

        p.setHealth(p.getMaxHealth());
        p.setFireTicks(0);
        p.setGameMode(GameMode.SPECTATOR);
        p.getInventory().clear();
        p.getInventory().setArmorContents(null);
    }

    public static void increaseWave(){
        CURRENT_WAVE++;

        for(LivingEntity e : ENTITIES){
            e.remove();
        }

        ENTITIES.clear();

        for(Location loc : CHEST_INVENTORIES.keySet()){
            loc.getBlock().setType(Material.AIR);
        }

        CHEST_INVENTORIES.clear();

        ArrayList<MapLocationData> a = MAP.getLocations(MapLocationType.INFW_MOBSPAWN);

        for (int i = 0; i < getMobsPerWave(CURRENT_WAVE).size(); i++) {
            Collections.shuffle(a);

            Entity e = a.get(0).toBukkitLocation(InfectionWars.MAP.getOriginalWorldName()).getWorld().spawnEntity(a.get(0).toBukkitLocation(InfectionWars.MAP.getOriginalWorldName()), getMobsPerWave(CURRENT_WAVE).get(i));
            ENTITIES.add((LivingEntity)e);
            ((LivingEntity) e).setRemoveWhenFarAway(false);
        }
    }

    public static void startChestMarkerScheduler(){
        Bukkit.getScheduler().scheduleSyncRepeatingTask(InfectionWars.getInstance(), new Runnable(){
            public void run(){
                for(Location loc : CHEST_INVENTORIES.keySet()){
                    for(Player all : Bukkit.getOnlinePlayers()){
                        all.playSound(loc, Sound.ITEM_PICKUP, 1F, 1F);
                        all.getWorld().playEffect(loc, Effect.LAVADRIP, -10);
                        all.playEffect(loc, Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
                    }
                }
            }
        }, 1*20, 1*20);
    }

    public void onDisable(){

    }

    public static void prepareWorld(World w){
        prepareWorld(w,true);
    }

    public static void prepareWorld(World w, boolean lobby){
        if(lobby){
            for(Entity e : w.getEntities()){
                if(e.getType() != EntityType.PLAYER && e.getType() != EntityType.ARMOR_STAND && e.getType() != EntityType.PAINTING && e.getType() != EntityType.ITEM_FRAME) e.remove();
            }

            w.setTime(5000L);
            w.setGameRuleValue("doDaylightCycle","false");
            w.setGameRuleValue("doMobSpawning","false");
            w.setGameRuleValue("doTileDrops","false");
            w.setDifficulty(Difficulty.PEACEFUL);
        } else {
            for(Entity e : w.getEntities()){
                if(e.getType() != EntityType.PLAYER && e.getType() != EntityType.ARMOR_STAND && e.getType() != EntityType.PAINTING && e.getType() != EntityType.ITEM_FRAME) e.remove();
            }

            w.setTime(14000L);
            w.setGameRuleValue("doDaylightCycle","false");
            w.setGameRuleValue("doMobSpawning","false");
            w.setGameRuleValue("doTileDrops","false");
            w.setGameRuleValue("mobGriefing","true");
            w.setDifficulty(Difficulty.NORMAL);
        }
    }

    public static ArrayList<EntityType> getMobsPerWave(int wave){
        ArrayList<EntityType> a = new ArrayList<EntityType>();

        if(wave == 1){
            for (int i = 0; i < 10; i++) {
                a.add(EntityType.ZOMBIE);
            }
        } else if(wave == 2){
            for (int i = 0; i < 10; i++) {
                a.add(EntityType.ZOMBIE);
            }
            for (int i = 0; i < 5; i++) {
                a.add(EntityType.SKELETON);
            }
        } else if(wave == 3){
            for (int i = 0; i < 15; i++) {
                a.add(EntityType.ZOMBIE);
            }
            for (int i = 0; i < 5; i++) {
                a.add(EntityType.SKELETON);
            }
        } else if(wave == 4){
            for (int i = 0; i < 15; i++) {
                a.add(EntityType.ZOMBIE);
            }
            for (int i = 0; i < 10; i++) {
                a.add(EntityType.SKELETON);
            }
        } else if(wave == 5){
            for (int i = 0; i < 15; i++) {
                a.add(EntityType.ZOMBIE);
            }
            for (int i = 0; i < 10; i++) {
                a.add(EntityType.SKELETON);
            }
            for (int i = 0; i < 5; i++) {
                a.add(EntityType.SPIDER);
            }
        } else if(wave == 6){
            for (int i = 0; i < 15; i++) {
                a.add(EntityType.ZOMBIE);
            }
            for (int i = 0; i < 15; i++) {
                a.add(EntityType.SKELETON);
            }
            for (int i = 0; i < 5; i++) {
                a.add(EntityType.SPIDER);
            }
        } else if(wave == 7){
            for (int i = 0; i < 20; i++) {
                a.add(EntityType.ZOMBIE);
            }
            for (int i = 0; i < 15; i++) {
                a.add(EntityType.SKELETON);
            }
            for (int i = 0; i < 5; i++) {
                a.add(EntityType.SPIDER);
            }
        } else if(wave == 8){
            for (int i = 0; i < 20; i++) {
                a.add(EntityType.ZOMBIE);
            }
            for (int i = 0; i < 15; i++) {
                a.add(EntityType.SKELETON);
            }
            for (int i = 0; i < 10; i++) {
                a.add(EntityType.SPIDER);
            }
        } else if(wave == 9){
            for (int i = 0; i < 20; i++) {
                a.add(EntityType.ZOMBIE);
            }
            for (int i = 0; i < 20; i++) {
                a.add(EntityType.SKELETON);
            }
            for (int i = 0; i < 10; i++) {
                a.add(EntityType.SPIDER);
            }
        } else if(wave == 10){
            for (int i = 0; i < 20; i++) {
                a.add(EntityType.ZOMBIE);
            }
            for (int i = 0; i < 20; i++) {
                a.add(EntityType.SKELETON);
            }
            for (int i = 0; i < 15; i++) {
                a.add(EntityType.SPIDER);
            }
        } else if(wave == 11){
            for (int i = 0; i < 20; i++) {
                a.add(EntityType.ZOMBIE);
            }
            for (int i = 0; i < 20; i++) {
                a.add(EntityType.SKELETON);
            }
            for (int i = 0; i < 20; i++) {
                a.add(EntityType.SPIDER);
            }
        } else if(wave == 12){
            for (int i = 0; i < 20; i++) {
                a.add(EntityType.ZOMBIE);
            }
            for (int i = 0; i < 20; i++) {
                a.add(EntityType.SKELETON);
            }
            for (int i = 0; i < 20; i++) {
                a.add(EntityType.SPIDER);
            }
            for (int i = 0; i < 5; i++) {
                a.add(EntityType.CREEPER);
            }
        } else if(wave == 13){
            for (int i = 0; i < 20; i++) {
                a.add(EntityType.ZOMBIE);
            }
            for (int i = 0; i < 20; i++) {
                a.add(EntityType.SKELETON);
            }
            for (int i = 0; i < 20; i++) {
                a.add(EntityType.SPIDER);
            }
            for (int i = 0; i < 10; i++) {
                a.add(EntityType.CREEPER);
            }
        } else if(wave == 14){
            for (int i = 0; i < 25; i++) {
                a.add(EntityType.ZOMBIE);
            }
            for (int i = 0; i < 20; i++) {
                a.add(EntityType.SKELETON);
            }
            for (int i = 0; i < 20; i++) {
                a.add(EntityType.SPIDER);
            }
            for (int i = 0; i < 15; i++) {
                a.add(EntityType.CREEPER);
            }
        } else if(wave == 15){
            for (int i = 0; i < 25; i++) {
                a.add(EntityType.ZOMBIE);
            }
            for (int i = 0; i < 20; i++) {
                a.add(EntityType.SKELETON);
            }
            for (int i = 0; i < 20; i++) {
                a.add(EntityType.SPIDER);
            }
            for (int i = 0; i < 20; i++) {
                a.add(EntityType.CREEPER);
            }
        } else if(wave == 16){
            for (int i = 0; i < 30; i++) {
                a.add(EntityType.ZOMBIE);
            }
            for (int i = 0; i < 20; i++) {
                a.add(EntityType.SKELETON);
            }
            for (int i = 0; i < 20; i++) {
                a.add(EntityType.SPIDER);
            }
            for (int i = 0; i < 20; i++) {
                a.add(EntityType.CREEPER);
            }
        } else if(wave == 17){
            for (int i = 0; i < 35; i++) {
                a.add(EntityType.ZOMBIE);
            }
            for (int i = 0; i < 25; i++) {
                a.add(EntityType.SKELETON);
            }
            for (int i = 0; i < 20; i++) {
                a.add(EntityType.SPIDER);
            }
            for (int i = 0; i < 20; i++) {
                a.add(EntityType.CREEPER);
            }
        } else if(wave == 18){
            for (int i = 0; i < 40; i++) {
                a.add(EntityType.ZOMBIE);
            }
            for (int i = 0; i < 25; i++) {
                a.add(EntityType.SKELETON);
            }
            for (int i = 0; i < 20; i++) {
                a.add(EntityType.SPIDER);
            }
            for (int i = 0; i < 20; i++) {
                a.add(EntityType.CREEPER);
            }
            for (int i = 0; i < 5; i++) {
                a.add(EntityType.WITCH);
            }
        } else if(wave == 19){
            for (int i = 0; i < 40; i++) {
                a.add(EntityType.ZOMBIE);
            }
            for (int i = 0; i < 25; i++) {
                a.add(EntityType.SKELETON);
            }
            for (int i = 0; i < 20; i++) {
                a.add(EntityType.SPIDER);
            }
            for (int i = 0; i < 20; i++) {
                a.add(EntityType.CREEPER);
            }
            for (int i = 0; i < 5; i++) {
                a.add(EntityType.WITCH);
            }
            a.add(EntityType.GHAST);
        } else if(wave == 20){
            for (int i = 0; i < 40; i++) {
                a.add(EntityType.ZOMBIE);
            }
            for (int i = 0; i < 25; i++) {
                a.add(EntityType.SKELETON);
            }
            for (int i = 0; i < 20; i++) {
                a.add(EntityType.SPIDER);
            }
            for (int i = 0; i < 20; i++) {
                a.add(EntityType.CREEPER);
            }
            for (int i = 0; i < 5; i++) {
                a.add(EntityType.WITCH);
            }
            a.add(EntityType.GHAST);
            a.add(EntityType.GHAST);
            a.add(EntityType.WITHER);
        }
        return a;
    }
}
