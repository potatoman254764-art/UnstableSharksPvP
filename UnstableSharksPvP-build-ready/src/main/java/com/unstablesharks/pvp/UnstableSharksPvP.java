package com.unstablesharks.pvp;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.*;

public final class UnstableSharksPvP extends JavaPlugin implements Listener {
    private final Map<UUID, Integer> streaks = new HashMap<>();
    private File statsFile;
    private YamlConfiguration stats;

    @Override public void onEnable() {
        saveDefaultConfig();
        statsFile = new File(getDataFolder(), "stats.yml");
        stats = YamlConfiguration.loadConfiguration(statsFile);
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("UnstableSharksPvP enabled. Kits remain managed by PlayerKits2.");
    }
    @Override public void onDisable() { saveStats(); }
    private void saveStats() {
        try { stats.save(statsFile); }
        catch (IOException ex) { getLogger().severe("Could not save stats.yml: " + ex.getMessage()); }
    }
    private boolean enabledWorld(World world) {
        List<String> names = getConfig().getStringList("pvp-worlds");
        return names.isEmpty() || names.contains(world.getName());
    }
    private int get(UUID id, String field) { return stats.getInt("players." + id + "." + field, 0); }
    private void add(UUID id, String field) { stats.set("players." + id + "." + field, get(id, field) + 1); }
    @EventHandler public void mobDamage(EntityDamageByEntityEvent event) {
        if (!getConfig().getBoolean("prevent-mob-damage-to-players") || !(event.getEntity() instanceof Player) || !enabledWorld(event.getEntity().getWorld())) return;
        if (event.getDamager() instanceof Monster) { event.setCancelled(true); return; }
        if (event.getDamager() instanceof Projectile projectile) {
            ProjectileSource source = projectile.getShooter();
            if (source instanceof Monster) event.setCancelled(true);
        }
    }
    @EventHandler public void monsterSpawn(CreatureSpawnEvent event) {
        if (getConfig().getBoolean("prevent-natural-monster-spawning") && event.getEntity() instanceof Monster && enabledWorld(event.getLocation().getWorld()) && event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL) event.setCancelled(true);
    }
    @EventHandler public void death(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        if (!enabledWorld(victim.getWorld())) return;
        UUID victimId = victim.getUniqueId();
        add(victimId, "deaths");
        streaks.remove(victimId);
        Player killer = victim.getKiller();
        if (killer != null && !killer.getUniqueId().equals(victimId)) {
            UUID killerId = killer.getUniqueId();
            add(killerId, "kills");
            int streak = streaks.merge(killerId, 1, Integer::sum);
            if (getConfig().getIntegerList("killstreak-announcements").contains(streak)) {
                Bukkit.broadcastMessage(color(getConfig().getString("killstreak-message", "&c[UnstableSharks] &e%player% &freached &6%streak% &fkillstreak!")
                    .replace("%player%", killer.getName()).replace("%streak%", String.valueOf(streak))));
            }
        }
        saveStats();
    }
    private String color(String s) { return ChatColor.translateAlternateColorCodes('&', s == null ? "" : s); }
    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("uspvp")) {
            if (!sender.hasPermission("unstablesharks.admin")) { sender.sendMessage(color("&cNo permission.")); return true; }
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                reloadConfig(); sender.sendMessage(color("&aUnstableSharksPvP configuration reloaded."));
            } else sender.sendMessage(color("&eUsage: /uspvp reload"));
            return true;
        }
        if (!(sender instanceof Player player)) { sender.sendMessage("Players only."); return true; }
        UUID id = player.getUniqueId();
        int kills = get(id, "kills"), deaths = get(id, "deaths");
        String kdr = String.format(Locale.US, "%.2f", deaths == 0 ? (double) kills : (double) kills / deaths);
        for (String line : getConfig().getStringList("stats-format")) {
            player.sendMessage(color(line.replace("%kills%", String.valueOf(kills)).replace("%deaths%", String.valueOf(deaths))
                .replace("%kdr%", kdr).replace("%streak%", String.valueOf(streaks.getOrDefault(id, 0)))));
        }
        return true;
    }
}
