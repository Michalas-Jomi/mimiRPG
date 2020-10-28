package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.HashMap;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;
import me.jomi.mimiRPG.util.Zegar;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

@Moduł
public class Antylog implements Listener, Zegar, Przeładowalny {
	public static final String prefix = Func.prefix("Antylog");
	final HashMap<String, Set<String>> gracze = new HashMap<>();
	final HashMap<String, Integer> 	   czasy  = new HashMap<>();

	
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
	
	final int maxCzas = 40;
	
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
	
	void koniec(String nick) {
		czasy.remove(nick);
		for (String _nick : gracze.get(nick)) {
			gracze.get(_nick).remove(nick);
			if (gracze.get(_nick).isEmpty())
				koniec(_nick);
		}
		gracze.remove(nick);
		
		Player p = Bukkit.getPlayer(nick);
		if (p == null) return;
		p.sendMessage(prefix + "Nie jesteś już w walce");
		p.removePotionEffect(PotionEffectType.GLOWING);
		info(nick);
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
		
		atakujący.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 200, 1, false, false, false));
		atakowany.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 200, 1, false, false, false));
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

	Set<String> dozwolone;
	@Override
	public void przeładuj() {
		dozwolone = Sets.newHashSet(Main.ust.wczytajListe("Antylog.Dozwolone Komendy"));
	}

	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Dozwolone Komendy Antyloga", dozwolone.size());
	}
}
