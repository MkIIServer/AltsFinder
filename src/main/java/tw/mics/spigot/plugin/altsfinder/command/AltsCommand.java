package tw.mics.spigot.plugin.altsfinder.command;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.md_5.bungee.api.ChatColor;
import tw.mics.spigot.plugin.altsfinder.AltsFinder;

public class AltsCommand implements CommandExecutor {
	AltsFinder plugin;
	public AltsCommand(AltsFinder i){
		this.plugin = i;
	}

    @SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length != 1){
            sender.sendMessage("用法: /alts 玩家名稱");
            return true;
        }
        Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable(){
            @Override
            public void run() {
            OfflinePlayer p = Bukkit.getOfflinePlayer(args[0]);
                String alts = new String();
                try {
                    String uuid = p.getUniqueId().toString();
                    Connection conn = plugin.database.getConnection();
                    Statement stmt = conn.createStatement();
                    String sql = "SELECT UUID FROM ALTS WHERE IP IN "
                            + "(SELECT IP FROM ALTS WHERE UUID = '" + uuid + "')"
                            + "GROUP BY UUID";
                    ResultSet rs = stmt.executeQuery(sql);
                    int count = 0;
                    while(rs.next()){
                        if(!rs.isFirst()){
                            alts += ", ";
                        }
                        count++;
                        OfflinePlayer op = Bukkit.getServer().getOfflinePlayer(UUID.fromString(rs.getString(1)));
                        alts += op.getName();
                    }
                    if(count == 0){
                        sender.sendMessage(ChatColor.GREEN + "這個玩家不存在");
                    } else if(count == 1){
                        sender.sendMessage(ChatColor.GREEN + "這個玩家沒有分帳紀錄");
                    } else {
                        sender.sendMessage(ChatColor.YELLOW + "以下帳號可能為同一人使用: ");
                        sender.sendMessage(ChatColor.AQUA + alts);
                    }
                } catch (SQLException e) {
                    sender.sendMessage(ChatColor.DARK_RED + "系統出了一些問題, 無法分析, 請立即回報管理員");
                    e.printStackTrace();
                }
            }
        });

        return true;
    }

}
