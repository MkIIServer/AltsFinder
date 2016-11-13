package tw.mics.spigot.plugin.altsfinder;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

import tw.mics.spigot.plugin.altsfinder.command.AltsCommand;
import tw.mics.spigot.plugin.altsfinder.data.Database;

public class AltsFinder extends JavaPlugin implements Listener {
    private static AltsFinder INSTANCE;
    public Database database;
    @Override
    public void onEnable() {
        INSTANCE = this;
        this.getDataFolder().mkdirs();
        database = new Database(this, getDataFolder());
        this.getCommand("alts").setExecutor(new AltsCommand(this));
        this.getServer().getPluginManager().registerEvents(this, this);
        clearUser();
    }
    
    @Override
    public void onDisable() {
        database.close();
    }
    
    
    @SuppressWarnings("deprecation")
    @EventHandler
    public void PlayerLoginEvent(PlayerLoginEvent e){
        String uuid = e.getPlayer().getUniqueId().toString();
        String ip = e.getAddress().getHostAddress();
        Bukkit.getScheduler().scheduleAsyncDelayedTask(this, new Runnable(){
            @Override
            public void run() {
                Connection conn = database.getConnection();
                try {
                    //insert cupboard
                    String sql = "INSERT INTO ALTS (UUID, IP) "
                            + "SELECT * FROM (SELECT ?, ?) AS tmp "
                            + "WHERE NOT EXISTS ("
                            + "    SELECT UUID, IP FROM ALTS WHERE UUID=? AND IP=?"
                            + ") LIMIT 1;"; 
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, uuid);
                    pstmt.setString(2, ip);
                    pstmt.setString(3, uuid);
                    pstmt.setString(4, ip);
                    pstmt.execute();
                    conn.commit();
                } catch ( SQLException e ) {
                    log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    getLogger().log(Level.WARNING, e.getClass().getName() + ": " + e.getMessage());
                    log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                }
            }
        });
    }
    
    @SuppressWarnings("deprecation")
    private void clearUser(){
        Bukkit.getScheduler().scheduleAsyncDelayedTask(this, new Runnable(){
            @Override
            public void run() {
                List<String> remove_uuid_list = new ArrayList<String>();
                Connection conn = database.getConnection();
                try {
                    Statement stmt = conn.createStatement();
                    //find non-exist player data
                    String sql = "SELECT UUID FROM ALTS GROUP BY UUID;";
                    ResultSet rs = stmt.executeQuery(sql);
                    while(rs.next()){
                        File player_file = new File(getServer().getWorlds().get(0).getWorldFolder(),
                                 File.separatorChar + "playerdata" + File.separatorChar + rs.getString(1) + ".dat");
                        if(!player_file.exists()){
                            remove_uuid_list.add(rs.getString(1));
                        }
                    }
                    
                    //delete non-exist player data
                    String sql_remove_player = "DELETE FROM ALTS WHERE UUID = ?";
                    PreparedStatement pstmt_player = conn.prepareStatement(sql_remove_player);  
                    for(String uuid : remove_uuid_list){
                        pstmt_player.setString(1, uuid);
                        pstmt_player.addBatch();
                    }
                    pstmt_player.executeBatch();
                    pstmt_player.close();
                    stmt.close();
                    conn.commit();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void log(String str, Object... args) {
        String message = String.format(str, args);
        getLogger().info(message);
    }

    public static AltsFinder getInstance() {
        return INSTANCE;
    }

}
