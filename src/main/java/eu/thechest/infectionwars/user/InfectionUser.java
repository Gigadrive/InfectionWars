package eu.thechest.infectionwars.user;

import eu.thechest.chestapi.mysql.MySQLManager;
import eu.thechest.chestapi.server.ServerSettingsManager;
import eu.thechest.chestapi.user.ChestUser;
import eu.thechest.infectionwars.Kit;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by zeryt on 28.05.2017.
 */
public class InfectionUser {
    public static HashMap<Player,InfectionUser> STORAGE = new HashMap<Player,InfectionUser>();

    public static InfectionUser getUser(Player p){
        if(STORAGE.containsKey(p)){
            return STORAGE.get(p);
        } else {
            new InfectionUser(p);

            if(STORAGE.containsKey(p)){
                return STORAGE.get(p);
            } else {
                return null;
            }
        }
    }

    public static void unregister(Player p){
        if(STORAGE.containsKey(p)){
            STORAGE.get(p).saveData();
            STORAGE.remove(p);
        }
    }

    private Player p;

    private int startPoints;
    private int points;
    private int startKills;
    private int kills;
    private int startDeaths;
    private int deaths;
    private int startPlayedGames;
    private int playedGames;
    private int startVictories;
    private int victories;
    private int startGivenRevives;
    private int givenRevives;
    private int startTakenRevives;
    private int takenRevives;
    private Timestamp firstGame;

    public ArrayList<Kit> boughtKits;
    public Kit activeKit;
    public boolean reviveable = false;

    public InfectionUser(Player p){
        this.p = p;

        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `infw_stats` WHERE `uuid` = ?");
            ps.setString(1,p.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();

            if(rs.first()){
                this.startPoints = rs.getInt("points");
                this.startKills = rs.getInt("kills");
                this.startDeaths = rs.getInt("deaths");
                this.startPlayedGames = rs.getInt("playedGames");
                this.startVictories = rs.getInt("victories");
                this.startGivenRevives = rs.getInt("revives.given");
                this.startTakenRevives = rs.getInt("revives.taken");
                this.firstGame = rs.getTimestamp("firstGame");

                loadKits();

                STORAGE.put(p,this);
            } else {
                PreparedStatement insert = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `infw_stats` (`uuid`,`username`) VALUES(?,?);");
                insert.setString(1,p.getUniqueId().toString());
                insert.setString(2,p.getName());
                insert.executeUpdate();
                insert.close();

                new InfectionUser(p);
            }

            MySQLManager.getInstance().closeResources(rs,ps);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private void loadKits(){
        boughtKits = new ArrayList<Kit>();

        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("SELECT * FROM `infw_boughtKits` WHERE `uuid` = ?");
            ps.setString(1,p.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                Kit kit = Kit.valueOf(rs.getString("kit"));

                if(!boughtKits.contains(kit)) boughtKits.add(kit);
            }

            MySQLManager.getInstance().closeResources(rs,ps);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void buyKit(Kit kit){
        if(!boughtKits.contains(kit)){
            try {
                if(kit.price > 0){
                    if(getUser().getCoins() >= kit.price){
                        getUser().reduceCoins(kit.price);
                    } else {
                        return;
                    }
                } else {
                    return;
                }

                PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `infw_boughtKits` (`uuid`,`kit`) VALUES(?,?);");
                ps.setString(1,p.getUniqueId().toString());
                ps.setString(2,kit.toString());
                ps.executeUpdate();
                ps.close();

                boughtKits.add(kit);
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public Player getBukkitPlayer(){
        return this.p;
    }

    public ChestUser getUser(){
        return ChestUser.getUser(getBukkitPlayer());
    }

    public int getStartPoints(){
        return this.startPoints;
    }

    public int getPoints(){
        return this.startPoints+this.points;
    }

    public void addPoints(int points){
        for(int i = 0; i < points; i++){
            //if((startPoints+this.points+i)<=0) break;

            this.points++;
        }

        p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.GREEN + getUser().getTranslatedMessage("You now have %p points.").replace("%p",ChatColor.YELLOW.toString() + getPoints() + ChatColor.GREEN));
    }

    public void reducePoints(int points){
        for(int i = 0; i < points; i++){
            if((startPoints+this.points+(i/-1))<=0) break;

            this.points--;
        }

        p.sendMessage(ServerSettingsManager.RUNNING_GAME.getPrefix() + ChatColor.RED + getUser().getTranslatedMessage("You now have %p points.").replace("%p",ChatColor.YELLOW.toString() + getPoints() + ChatColor.RED));
    }

    public int getStartKills(){
        return this.startKills;
    }

    public int getKills(){
        return this.startKills+this.kills;
    }

    public void addKills(int i){
        this.kills += i;
    }

    public int getStartDeaths(){
        return this.startDeaths;
    }

    public int getDeaths(){
        return this.startDeaths+this.deaths;
    }

    public void addDeaths(int i){
        this.deaths += i;
    }

    public int getStartPlayedGames(){
        return this.startPlayedGames;
    }

    public int getPlayedGames(){
        return this.startPlayedGames+this.playedGames;
    }

    public void addPlayedGames(int i){
        this.playedGames += i;
    }

    public int getStartVictories(){
        return this.startVictories;
    }

    public int getVictories(){
        return this.startVictories+this.victories;
    }

    public void addVictories(int i){
        this.victories += i;
    }

    public int getStartGivenRevives(){
        return this.startGivenRevives;
    }

    public int getGivenRevives(){
        return this.startGivenRevives+this.givenRevives;
    }

    public void addGivenRevives(int i){
        this.givenRevives += i;
    }

    public int getStartTakenRevives(){
        return this.startTakenRevives;
    }

    public int getTakenRevives(){
        return this.startTakenRevives+this.takenRevives;
    }

    public void addTakenRevives(int i){
        this.takenRevives += i;
    }

    public void saveData(){
        try {
            PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("UPDATE `infw_stats` SET " +
                    "`username` = ?," +
                    "`points` = `points`+?," +
                    "`kills` = `kills`+?," +
                    "`deaths` = `deaths`+?," +
                    "`playedGames` = `playedGames`+?," +
                    "`victories` = `victories`+?," +
                    "`revives.given` = `revives.given`+?," +
                    "`revives.taken` = `revives.taken`+?" +
                    "WHERE `uuid` = ?");
            ps.setString(1,p.getName());
            ps.setInt(2,this.points);
            ps.setInt(3,this.kills);
            ps.setInt(4,this.deaths);
            ps.setInt(5,this.playedGames);
            ps.setInt(6,this.victories);
            ps.setInt(7,this.givenRevives);
            ps.setInt(8,this.takenRevives);
            ps.setString(9,p.getUniqueId().toString());
            ps.executeUpdate();
            ps.close();
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
