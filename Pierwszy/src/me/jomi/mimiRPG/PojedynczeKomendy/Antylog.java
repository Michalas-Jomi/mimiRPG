package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.api._WorldGuard;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Komenda;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;
import me.jomi.mimiRPG.util.Zegar;

@Moduł
public class Antylog extends Komenda implements Listener, Zegar, Przeładowalny {
	public static final String prefix = Func.prefix("Antylog");
	final HashMap<String, Set<String>> gracze = new HashMap<>();
	final HashMap<String, Integer> 	   czasy  = new HashMap<>();

	public Antylog() {
		super("antylogbypass");
	}
	
	
	static final String metaBypass = "mimiAntylogBypass";
	public static void włączBypass(Player p) {
		int lvl = maBypass(p) ? p.getMetadata(metaBypass).get(0).asInt() : 0;
		Func.ustawMetadate(p, metaBypass, lvl + 1);
	}
	public static void wyłączBypass(Player p) {
		if (!maBypass(p)) return;
		int lvl = p.getMetadata(metaBypass).get(0).asInt();
		if (lvl <= 1)
			p.removeMetadata(metaBypass, Main.plugin);
		else
			Func.ustawMetadate(p, metaBypass, lvl - 1);
	}
	public static boolean maBypass(Player p) {
		return p.hasMetadata(metaBypass);
	}
	
	int maxCzas = 40;
	
	@Override
	public int czas() {
		for (String klucz : Lists.newArrayList(czasy.keySet()))
			if (czasy.containsKey(klucz)) {
				czasy.put(klucz, czasy.get(klucz)+1);
				info(klucz);
				if (czasy.get(klucz) >= maxCzas)
					koniec(klucz);
			}
		return 5;
	}
	
	void info(String nick) {
		int czas = czasy.getOrDefault(nick, maxCzas);
		String walka = "§"+(czas % 2 == 0 ? '6' : 'e')+"Walka";
		StringBuilder txt = new StringBuilder(walka);
		txt.append(" §a");
		czas /= 2;
		for (int i=0; i<czas; i++)
			txt.append('|');
		txt.append("§c");
		for (int i=czas; i<maxCzas/2; i++)
			txt.append('|');
		txt.append(' ').append(walka);
		Bukkit.getPlayer(nick).spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(txt.toString()));
	}
	@EventHandler
	public void dołączanieDoGry(PlayerJoinEvent ev) {
		ev.getPlayer().removeScoreboardTag("mimiAntylog");
	}
	
	void koniec(String nick) {
		czasy.remove(nick);
		for (String _nick : gracze.get(nick)) {
			gracze.get(_nick).remove(nick);
			if (gracze.get(_nick).isEmpty())
				koniec(_nick);
		}
		gracze.remove(nick);
		
		Func.wykonajDlaNieNull(Bukkit.getPlayer(nick), p -> {
			p.sendMessage(prefix + "Nie jesteś już w walce");
			p.removePotionEffect(PotionEffectType.GLOWING);
			p.removeScoreboardTag("mimiAntylog");
			info(nick);
		});
	}

	@EventHandler(priority=EventPriority.MONITOR)
	public void uderzenie(EntityDamageByEntityEvent ev) {
		if (ev.isCancelled()) return;
		if (ev.getDamage() <= 0) return;
		if (!(ev.getEntity() instanceof Player)) return;
		Player atakowany = (Player) ev.getEntity();
		if (maBypass(atakowany)) return;
		
		Player atakujący = null;
		Entity _atakujący = ev.getDamager();
		if (_atakujący instanceof Player)
			atakujący = (Player) ev.getDamager();
		else if (_atakujący instanceof Projectile) {
			 ProjectileSource __atakujący = ((Projectile) _atakujący).getShooter();
			 if (__atakujący instanceof Player)
				 atakujący = (Player) __atakujący;
		}
		if (atakujący == null) return;
		
		if (maBypass(atakujący)) return;
		
		if (Bukkit.getPlayer(atakowany.getName()) == null) return;
		
		if (atakowany.getName().equals(atakujący.getName())) return;
		
		czasy.put(atakujący.getName(), 0);
		czasy.put(atakowany.getName(), 0);
		
		if (!gracze.containsKey(atakowany.getName())) {
			atakowany.sendMessage(prefix + Func.msg("%s zaatakował cię, nie wychodz teraz z gry!", atakujący.getDisplayName()));
			gracze.put(atakowany.getName(), Sets.newHashSet());
		}
		if (!gracze.containsKey(atakujący.getName())) {
			atakujący.sendMessage(prefix + Func.msg("Zaatakowałeś gracza %s, nie wychodz teraz z gry!", atakowany.getDisplayName()));
			gracze.put(atakujący.getName(), Sets.newHashSet());
		}
		
		gracze.get(atakowany.getName()).add(atakujący.getName());
		gracze.get(atakujący.getName()).add(atakowany.getName());
		
		info(atakujący.getName());
		info(atakowany.getName());
		
		if (Main.ust.wczytaj("Antylog.glowing", true)) {
			atakujący.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, maxCzas*5, 1, false, false, false));
			atakowany.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, maxCzas*5, 1, false, false, false));
		}

		atakowany.addScoreboardTag("mimiAntylog");
		atakujący.addScoreboardTag("mimiAntylog");
	}
	
	@EventHandler
	public void śmierć(PlayerDeathEvent ev) {
		if (czasy.containsKey(ev.getEntity().getName()))
			koniec(ev.getEntity().getName());
	}
	@EventHandler
	public void opuszczeniegry(PlayerQuitEvent ev) {
		if (czasy.containsKey(ev.getPlayer().getName())) {
			ev.getPlayer().setHealth(0);
			Bukkit.broadcastMessage(prefix + Func.msg("%s uciekł z walki!", ev.getPlayer().getDisplayName()));
		}
	}
	
	@EventHandler(priority=EventPriority.LOW)
	public void komendy(PlayerCommandPreprocessEvent ev) {
		if (czasy.containsKey(ev.getPlayer().getName())) {
			String msg = ev.getMessage();
			for (String komenda : dozwolone)
				if (msg.startsWith(komenda))
					return;
			ev.setCancelled(true);
			ev.getPlayer().sendMessage(prefix + "Ta komenda nie jest dozwola w trakcie walki");
			Main.log(prefix + Func.msg("Anulowano graczowi %s komendę %s", ev.getPlayer().getDisplayName(), msg));
		}
	}
	
	@EventHandler
	public void chodzenie(PlayerMoveEvent ev) {
		if (_WorldGuard.rg != null && czasy.containsKey(ev.getPlayer().getName()) &&
				!Func.regiony(ev.getTo().getWorld()).getApplicableRegions(Func.locToVec3(ev.getTo())).testState(WorldGuardPlugin.inst().wrapPlayer(ev.getPlayer()), Flags.PVP)) {
			ev.setCancelled(true);
			ev.getPlayer().sendMessage(prefix + "Nie możesz tam wejść w trakcie walki!");
		}
	}
	@EventHandler
	public void tp(PlayerTeleportEvent ev) {
		if (czasy.containsKey(ev.getPlayer().getName()))
			if (Main.ust.wczytaj("Antylog.tp." + ev.getCause(), false)) {
				ev.setCancelled(true);
				ev.getPlayer().sendMessage(prefix + "Nie możesz się teleportować w trakcie walki");
			}
	}

	Set<String> dozwolone;
	@Override
	public void przeładuj() {
		dozwolone = Sets.newHashSet(Main.ust.wczytajListe("Antylog.Dozwolone Komendy"));
		dozwolone.add("/antylogbypass");
		maxCzas = Main.ust.wczytaj("Antylog.Czas", 10) * 4;
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Dozwolone Komendy Antyloga", dozwolone.size());
	}
	
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) return Func.powiadom(sender, prefix + "Tylko dla graczy");
		Player p = (Player) sender;
		if (maBypass(p)) {
			p.removeMetadata(metaBypass, Main.plugin);
			p.sendMessage(prefix + "Wyłączono Bypass");
		} else {
			włączBypass(p);
			p.sendMessage(prefix + "Właczono Bypass");
		}
		return true;
	}
}
