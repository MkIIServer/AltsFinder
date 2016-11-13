package tw.mics.spigot.plugin.altsfinder.data;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

import tw.mics.spigot.plugin.altsfinder.AltsFinder;

public class Database {
    private AltsFinder plugin;
    
    private File dbfile;
    Connection db_conn;
    
    public Database(AltsFinder p, File dataFolder){
        this.plugin = p;
        dbfile = new File(dataFolder, "database.db");
        this.initDatabase();
    }
    
    public Connection getConnection(){
        return db_conn;
    }
    
    private void initDatabase() {
        try {
          Class.forName("org.sqlite.JDBC");
          db_conn = DriverManager.getConnection("jdbc:sqlite:"+dbfile.getPath());
       
          //新增表格
          Statement stmt = db_conn.createStatement();
          String sql = "CREATE TABLE IF NOT EXISTS ALTS " +
                  "(UUID    TEXT     NOT NULL, " +
                  "IP      TEXT     NOT NULL)";
          stmt.executeUpdate(sql);
          stmt.close();
          db_conn.setAutoCommit(false);
          
        } catch ( SQLException | ClassNotFoundException e ) {
            plugin.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            plugin.getLogger().log(Level.WARNING, e.getClass().getName() + ": " + e.getMessage());
            plugin.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    }
    
    public void close(){
        try {
            db_conn.close();
        } catch ( SQLException e ) {
            plugin.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            plugin.getLogger().log(Level.WARNING, e.getClass().getName() + ": " + e.getMessage());
            plugin.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    }
}
