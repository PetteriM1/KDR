package kdr;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.player.PlayerDeathEvent;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;

public class Main extends PluginBase implements Listener {

    @SuppressWarnings("WeakerAccess")
    public static Main plugin;
    private static Config data;

    public void onLoad() {
        plugin = this;
    }

    public void onEnable() {
        data = new Config(getDataFolder() + "/kdr_data.yml", Config.YAML);
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getScheduler().scheduleDelayedRepeatingTask(this, this::save, 36000, 36000);
    }

    public void onDisable() {
        save();
    }

    private void save() {
        data.save();
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("\u00A7cYou can run this command only as a player");
            return true;
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("top")) {
                if (!sender.hasPermission("kdr.top")) {
                    sender.sendMessage("\u00A7cYou don't have permission to see top KDR list");
                } else {
                    sender.sendMessage("\u00A76Top KDR on this server: \u00A77" + getTopKDRScore() + " ("+ getTopKDRPlayer() + ")");
                }

                return true;
            }

            if (!sender.hasPermission("kdr.others")) {
                sender.sendMessage("\u00A7cYou don't have permission to see KDR of other players");
                return true;
            }

            Player target = getServer().getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage("\u00A7cUnknown player");
            } else {
                sender.sendMessage("\u00A7a" + target.getName() + "'s kills: " + getKills(target));
                sender.sendMessage("\u00A7c" + target.getName() + "'s deaths: " + getDeaths(target));
                sender.sendMessage("\u00A7e" + target.getName() + "'s KDR: " + getKDR(target));
            }

            return true;
        }

        Player p = (Player) sender;
        sender.sendMessage("\u00A7aKills: " + getKills(p));
        sender.sendMessage("\u00A7cDeaths: " + getDeaths(p));
        sender.sendMessage("\u00A7eKDR: " + getKDR(p));
        return true;
    }

    public String getTopKDRPlayer() {
        String topPlayer = "null";
        double topKDR = 0;

        for (String name : data.getSection("kills").getKeys()) {
            double kdr = (double) data.getInt("kills." + name) / data.getInt("deaths." + name);
            if (kdr > topKDR) {
                topKDR = kdr;
                topPlayer = name;
            }
        }

        return topPlayer;
    }

    public double getTopKDRScore() {
        double topKDR = 0;

        for (String name : data.getSection("kills").getKeys()) {
            double kdr = (double) data.getInt("kills." + name) / data.getInt("deaths." + name);
            if (kdr > topKDR) {
                topKDR = kdr;
            }
        }

        return topKDR;
    }

    public int getKills(Player p) {
        return data.getInt("kills." + p.getName(), 0);
    }

    public int getDeaths(Player p) {
        return data.getInt("deaths." + p.getName(), 0);
    }

    public void addKill(Player p) {
        data.set("kills." + p.getName(), getKills(p) + 1);
    }

    public void addDeath(Player p) {
        data.set("deaths." + p.getName(), getDeaths(p) + 1);
    }

    public double getKDR(Player p) {
        return (double) getKills(p) / getDeaths(p);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        addDeath(e.getEntity());
        if (e.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent) e.getEntity().getLastDamageCause();
            if (ev.getDamager() instanceof Player) {
                addKill((Player) ev.getDamager());
            }
        }
    }
}
