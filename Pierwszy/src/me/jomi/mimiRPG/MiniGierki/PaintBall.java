package me.jomi.mimiRPG.MiniGierki;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.util.Vector;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Func;

public class PaintBall extends MiniGra{
	private ItemStack œnie¿ka = Func.stwórzItem(Material.SNOWBALL, 8, "&1Pocisk", null);
	private ItemStack Dczerwona  = Func.stwórzItem(Material.RED_WOOL,  1, "&cWybierz dru¿ynê czerwon¹",  null);
	private ItemStack Dniebieska = Func.stwórzItem(Material.BLUE_WOOL, 1, "&9Wybierz dru¿ynê niebieska", null);
	private ItemStack DWczerwona  = Func.stwórzItem(Material.RED_WOOL,  1, "&cWybra³eœ dru¿ynê czerwon¹",  null);
	private ItemStack DWniebieska = Func.stwórzItem(Material.BLUE_WOOL, 1, "&9Wybra³eœ dru¿ynê niebiesk¹", null);
	private ItemStack Kask = Func.dajG³ówkê("&bKask Paintballowca", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjkzN2VhZGM1M2MzOWI5Njg4Y2MzOTY1NWQxYjc4ZmQ2MTJjMWNkNjI1YzJhODk2MzhjNWUyNzIxNmM2ZTRkIn19fQ==", null);
	private ItemStack Tczerwona  = kolorowaZbroja(Material.LEATHER_CHESTPLATE, "§bPaintballowa Tunika", 255, 0, 0);
	private ItemStack Tniebieska = kolorowaZbroja(Material.LEATHER_CHESTPLATE, "§bPaintballowa Tunika", 0, 0, 255);
	private ItemStack Sczerwone  = kolorowaZbroja(Material.LEATHER_LEGGINGS,   "§bPaintballowe Spodnie",255, 0, 0);
	private ItemStack Sniebieskie= kolorowaZbroja(Material.LEATHER_LEGGINGS,   "§bPaintballowe Spodnie",0, 0, 255);
	private ItemStack Bczerwone  = kolorowaZbroja(Material.LEATHER_BOOTS, 	   "§bPaintballowe Buty", 	255, 0, 0);
	private ItemStack Bniebieskie= kolorowaZbroja(Material.LEATHER_BOOTS, 	   "§bPaintballowe Buty", 	0, 0, 255);
	private List<Player> czerwoni  = Lists.newArrayList();
	private List<Player> niebiescy = Lists.newArrayList();
	private List<Location> Sczerwoni;
	private List<Location> Sniebiescy;	
	private boolean koñczenie;
	private int pktKoñczenie = 20;
	private int Cpkt;
	private int Npkt;
	private Arena arena;
	
	public PaintBall() {
		super("Paintball", "PaintBall");
		ItemMeta meta;
		for (ItemStack item : Arrays.asList(DWczerwona, DWniebieska)) {
			meta = item.getItemMeta();
			meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			item.setItemMeta(meta);
		}
	}
	
	public static ItemStack kolorowaZbroja(Material zbroja, String nazwa, int r, int g, int b) {
		ItemStack item = new ItemStack(zbroja);
		LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
		meta.setDisplayName(nazwa);
		meta.setColor(Color.fromRGB(r, g, b));
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		item.setItemMeta(meta);
		return item;
	}
	
	protected void zwyciêstwo(Arena arena) {}
	
	public boolean dolacz(Player p) {
		if (arena.gracze.contains(p) && !(niebiescy.contains(p) || czerwoni.contains(p))) {
			p.sendMessage(prefix + "Najpierw wybierz swoj¹ dru¿ynê zanim zag³osujesz");
			return true;
		}
		if (super.dolacz(p)) return true;
		dajWybór(p);
		return false;
	}
	private void dajWybór(Player p) {
		Inventory inv = p.getInventory();
		inv.setItem(1, Dczerwona);
		inv.setItem(2, Dniebieska);
		if (czerwoni.contains(p)) 		inv.setItem(1, DWczerwona);
		else if (niebiescy.contains(p)) inv.setItem(2, DWniebieska);
	}
	
	public void opuœæ(Player p, boolean komunikat) {
		super.opuœæ(p, komunikat);
		niebiescy.remove(p);
		czerwoni.remove(p);
		koniec();
	}
	
	protected void start(Arena arena) {
		if (!(niebiescy.size() + czerwoni.size() == arena.gracze.size())) return;
		if (niebiescy.size() == 0 || czerwoni.size() == 0) {
			powiadom("Aby rozpocz¹æ rozgrywkê w ka¿dej dru¿ynie musi byæ przynajmniej 1 gracz", arena.gracze);
			return;
		}
		Cpkt = 0;
		Npkt = 0;
		super.start(arena);
		koñczenie = false;
		for (Player p : arena.gracze) {
			dajŒnie¿ke(p);
			zresp(p);
		}
		PlayerInventory inv;
		for (Player p : czerwoni) {
			inv = p.getInventory();
			inv.setHelmet(Kask);
			inv.setChestplate(Tczerwona);
			inv.setLeggings(Sczerwone);
			inv.setBoots(Bczerwone);
		}
		for (Player p : niebiescy) {
			inv = p.getInventory();
			inv.setHelmet(Kask);
			inv.setChestplate(Tniebieska);
			inv.setLeggings(Sniebieskie);
			inv.setBoots(Bniebieskie);
		}
		pktKoñczenie = niebiescy.size() + czerwoni.size();
		pktKoñczenie *= 5;
	}
	
	protected void koniec() {
		if (!arena.grane || koñczenie) return;
		String wygrana = "";
		if (Cpkt >= pktKoñczenie || niebiescy.size() == 0) 
			wygrana = "§cCzerwonych§6";
		else if (Npkt >= pktKoñczenie || czerwoni.size() == 0)
			wygrana = "§9Niebieskich§6";
		if (wygrana.equals("")) return;
		koñczenie = true;
		Bukkit.broadcastMessage(prefix + "Wygra³a dru¿yna " + wygrana + "!");
		String w = "";
		for (Player p : czerwoni)
			w += "§c" + p.getName() + "§f, ";
		for (Player p : niebiescy)
			w += "§9" + p.getName() + "§f, ";
		Bukkit.broadcastMessage("§6Gracze: " + w.substring(0, w.length()-4));
		while (arena.gracze.size() > 0) {
			arena.gracze.get(0).playSound(arena.gracze.get(0).getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, .3f, 1);
			opuœæ(arena.gracze.get(0), false);
		}
		arena.grane = false;
	}
	protected void zbij(Player p, Player strzelec) {
		if ((czerwoni.contains(p) && czerwoni.contains(strzelec)) || (niebiescy.contains(p) && niebiescy.contains(strzelec))) return;
		String kolor  = "§9";
		String kolor2 = "§c";
		int pkt = Cpkt+1;
		if (czerwoni.contains(p)) {
			Npkt += 1;
			pkt = Npkt;
			kolor  = "§c";
			kolor2 = "§9";
		}
		else
			Cpkt += 1;
		p.getWorld().spawnParticle(Particle.SPIT, p.getLocation().add(new Vector(0, 1.5, 0)), 20, .4, .6, .4, .1);
		powiadom(kolor2 + strzelec.getName() + "§6 zestrzeli³ gracza " + kolor + p.getName() + "§6! (" + kolor2 + pkt + "§6/" + kolor2 + pktKoñczenie + "§6)", arena.gracze);
		koniec();
		zresp(p);
		p.playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.4f, 1);
		strzelec.playSound(strzelec.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.3f, 1);
	}
	protected void zresp(Player p) {
		if (!arena.grane) return;
		if (czerwoni.contains(p))
			p.teleport(Sczerwoni.get(Func.losuj(0, Sczerwoni.size()-1)));
		else
			p.teleport(Sniebiescy.get(Func.losuj(0, Sniebiescy.size()-1)));
	}
	protected void wybierzDru¿yne(Player p, List<Player> dru¿yna) {
		if ((czerwoni.contains(p) && dru¿yna.equals(czerwoni)) || (niebiescy.contains(p) && dru¿yna.equals(niebiescy))) {
			p.sendMessage(prefix + "Nale¿ysz ju¿ do tej dru¿yny");
			return;
		}
		czerwoni.remove(p);
		niebiescy.remove(p);
		dru¿yna.add(p);
		String d = "§cCzerwonej";
		ItemStack tunika = Tczerwona;
		if (dru¿yna.equals(niebiescy)) {
			d = "§9Niebieskiej";
			tunika = Tniebieska;
		}
		p.getInventory().setChestplate(tunika);
		p.playSound(p.getLocation(), Sound.BLOCK_TRIPWIRE_CLICK_ON, .4f, 1);
		powiadom("Gracz §e" + p.getName() + "§6 do³¹czy³ do dru¿yny " + d, arena.gracze);
		for (Player gracz : arena.gracze)
			if (!gracz.equals(p))
				gracz.playSound(gracz.getLocation(), Sound.ENTITY_ENDER_EYE_DEATH, .4f, 1);
		for (boolean b : arena.g³osy)
			if (!b) return;
		arena.policzG³osy(this);
	}
	protected void dajŒnie¿ke(Player p) {
		p.getInventory().setItem(4, œnie¿ka);
	}
	public void ustawLokacje(Player p, String args[]) {
		if (args[1].equals("czerwoni")) {
			if (args.length >= 3 && args[2].equalsIgnoreCase("reset"))
				Sczerwoni = Lists.newArrayList();
			else
				Sczerwoni.add(p.getLocation());
			config.ustaw_zapisz(nazwa + ".czerwoni", Sczerwoni);
			if (Sczerwoni.size() == 0) {
				p.sendMessage(prefix + "Usuniêto wszystkie lokalizacje respu dru¿yny §cczerwonej");
				return;
			}
			p.sendMessage(prefix + "Dodano now¹ lokalizacjê respu dru¿yny §cczerwonej");
			return;
		} 
		else if (args[1].equals("niebiescy")) {
			if (args.length >= 3 && args[2].equalsIgnoreCase("reset"))
				Sniebiescy = Lists.newArrayList();
			else
				Sniebiescy.add(p.getLocation());
			config.ustaw_zapisz(nazwa + ".niebiescy", Sniebiescy);
			if (Sniebiescy.size() == 0) {
				p.sendMessage(prefix + "Usuniêto wszystkie lokalizacje respu dru¿yny §9niebieskiej");
				return;
			}
			p.sendMessage(prefix + "Dodano now¹ lokalizacjê respu dru¿yny §9niebieskiej");
			return;
		} 
		else
			super.ustawLokacje(p, args);
	}
	@SuppressWarnings("unchecked")
	public void prze³aduj() {
		super.prze³aduj();
		
		Sczerwoni  = (List<Location>) config.wczytaj(nazwa + ".czerwoni");
		Sniebiescy = (List<Location>) config.wczytaj(nazwa + ".niebiescy");
		
		if (Sczerwoni  == null) {powiadomOp(prefix + "§cNie ustawiono jeszcze §4¿adnej§c lokacji respu dla dru¿yny Czerwonej");   Sczerwoni  = Lists.newArrayList();}
		if (Sniebiescy == null) {powiadomOp(prefix + "§cNie ustawiono jeszcze §4¿adnej§c lokacji respu dla dru¿yny Niebieskiej"); Sniebiescy = Lists.newArrayList();}
	
		if (!areny.obiekty.isEmpty())
			arena = areny.obiekty.get(0);
		else
			powiadomOp(prefix + "§cNie ustawiono ¿adnej areny");
	}
	
	@EventHandler
	public void u¿ycie(PlayerInteractEvent ev) {
		if (arena == null) return;
		Player p = ev.getPlayer();
		if (!(arena.gracze.contains(p) && ev.getItem() != null)) return;
		if (ev.getItem().getType().equals(Material.SNOWBALL)) {
			if (ev.getAction().toString().startsWith("RIGHT"))
				dajŒnie¿ke(p);
			else
				ev.setCancelled(true);
			return;
		}
		super.u¿ycie(ev);
		if (!arena.grane) {
			switch (ev.getItem().getType()) {
			case RED_WOOL:
				wybierzDru¿yne(p, czerwoni);
				ev.setCancelled(true);
				dajWybór(p);
				return;
			case BLUE_WOOL:
				wybierzDru¿yne(p, niebiescy);
				ev.setCancelled(true);
				dajWybór(p);
				return;
			default:
				break;
			}
		}
	}
	@EventHandler
	public void uderzenie(ProjectileHitEvent ev) {
		if (arena == null) return;
		if (!(arena.grane || ev.getEntity().getType().equals(EntityType.SNOWBALL))) return;
		if (arena.grane && arena.gracze.contains(ev.getEntity().getShooter()))
			ev.getEntity().getWorld().spawnParticle(Particle.CRIT_MAGIC, ev.getEntity().getLocation(), 4, .2, .2, .2, .1);
		if (!arena.gracze.contains(ev.getHitEntity())) return;
		zbij((Player) ev.getHitEntity(), (Player) ev.getEntity().getShooter());
	}
	@EventHandler
	public void komendy(PlayerCommandPreprocessEvent ev) {
		if (arena == null) return;
		if (arena.gracze.contains(ev.getPlayer())) {
			String[] s³owa = ev.getMessage().split(" ");
			if (s³owa[0].equalsIgnoreCase("/tc")) {
				if (czerwoni.contains(ev.getPlayer())) 	powiadom("§b" + ev.getPlayer().getName() + "§7: §r" + Func.listToString(s³owa, 1), czerwoni);
				else 									powiadom("§b" + ev.getPlayer().getName() + "§7: §r" + Func.listToString(s³owa, 1), niebiescy);
				ev.setCancelled(true);
				return;
			}
			super.komendy(ev);
		}
	}
}
