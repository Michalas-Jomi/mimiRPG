package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Gracz;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;
import me.jomi.mimiRPG.util.Zegar;

@Moduł
public class Koniki extends Komenda implements Listener, Przeładowalny, Zegar {
	public static class Kon implements ConfigurationSerializable {
		private static final int slotyEq 		= 3*9;
		private static final int slotMały 		= 4*9 + 1;
		private static final int slotStyl 		= 4*9 + 7;
		private static final int slotKolor 		= 4*9 + 5;
		private static final int slotBezgłośny 	= 4*9 + 3;
		
		private static final ItemStack itemMały = Func.stwórzItem(Material.EGG, "§6Rozmiar", "§eMały");
		private static final ItemStack itemDuży = Func.stwórzItem(Material.EGG, "§6Rozmiar", "§eDuży");
		private static final ItemStack itemBezgłośny 	= Func.stwórzItem(Material.BONE, 		 "§6Dzwięk", "§6Bezgłośny");
		private static final ItemStack itemNieBezgłośny = Func.stwórzItem(Material.CREEPER_HEAD, "§6Dzwięk", "§6Normalny");
		
		private static final Kolor[] kolory = Kolor.values();
		private static final Styl[] style = Styl.values();

		@Mapowane public List<ItemStack> itemy = Lists.newArrayList();
		@Mapowane public Kolor kolor = Kolor.Biały;
		@Mapowane public Styl styl = Styl.Brak;
		@Mapowane public String właściciel;
		@Mapowane public boolean bezgłośny;
		@Mapowane public boolean mały;
		@Mapowane public int zapas;

		private int nrKolor = -1;
		private int nrStyl = -1;
		
		public Kon(Map<String, Object> mapa) {
			Func.zdemapuj(this, mapa);
			init();
		}
		@Override
		public Map<String, Object> serialize() {
			return Func.zmapuj(this);
		}
		public Kon(Gracz g) {
			właściciel = g.nick;
			init();
		}
		private void init() {
			nrKolor = znajdz(kolory, this.kolor);
			nrStyl = znajdz(style, this.styl);
		}
		
		public Inventory dajInv(Horse kon) {
			return stwórzInv(kon);
		}
		
		private int znajdz(Object[] objekty, Object obj) {
			for (int i=0; i< objekty.length; i++)
				if (objekty[i].equals(obj))
					return i;
			return -1;
		}
		
		public void przywołaj(Player p) {
			usuń();
			Horse kon = (Horse) p.getWorld().spawnEntity(p.getLocation(), EntityType.HORSE);
			
			kon.setOwner(p);
			kon.setTamed(true);
			kon.setAgeLock(true);
			kon.setInvulnerable(true);
			kon.setRemoveWhenFarAway(true);
			
			kon.getInventory().setSaddle(new ItemStack(Material.SADDLE));
			
			kon.setMetadata("mimiKon", new FixedMetadataValue(Main.plugin, p.getName()));
			
			kon.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
			kon.setHealth(20);

			p.sendMessage(Koniki.prefix + Func.msg("Przywołałeś swojego Konika, możesz jezdzić na nim jeszcze %s bez karmienia", Koniki.czas(zapas)));
			//EntityHorse e = ((EntityHorse)((CraftEntity) kon).getHandle());
			//e.goalSelector = new PathfinderGoalSelector(e.getWorld().getMethodProfilerSupplier());
			
			ustawMały(kon, null);
			ustawStyl(kon, null);
			ustawKolor(kon, null);
			ustawBezgłośny(kon, null);
		}
		
		public void usuń() {
			Horse kon = znajdzKonia();
			if (kon != null)
				kon.remove();
		}
		public Horse znajdzKonia() {
			for (World świat : Bukkit.getWorlds())
				for (Horse h : świat.getEntitiesByClass(Horse.class))
					if (h.hasMetadata("mimiKon") && h.getMetadata("mimiKon").get(0).asString().equals(właściciel))
						return h;
			return null;
		}
		
		public void nakarm(int ile, Gracz g) {
			if (!nakarmiony())
				zapas = (int) (System.currentTimeMillis() / 1000);
			zapas += ile;
			g.zapisz();
		}
		public boolean nakarmiony() {
			return System.currentTimeMillis() / 1000 <= zapas;
		}
		public void sprawdz(Horse kon) {
			if (kon != null && !kon.getPassengers().isEmpty()) {
				if (nakarmiony()) return;
				for (Entity e : kon.getPassengers())
					if (e instanceof Player) {
						Player p = (Player) e;
						kon.removePassenger(p);
						p.sendMessage(Koniki.prefix + "Konik jest głodny, nie jest w stanie cie dalej wozić");
					}
			}
		}
		
		private Inventory stwórzInv(Horse kon) {
			Inventory inv = Bukkit.createInventory(Bukkit.getPlayer(właściciel), 5*9, "§1§lTwój Konik");
			for (int i=0; i<itemy.size(); i++) {
				if (i >= slotyEq) continue;
				inv.setItem(i, itemy.get(i));
			}
			ItemStack nic = Func.stwórzItem(Material.BLACK_STAINED_GLASS_PANE, "§1§l§o §6§o");
			for (int i=slotyEq; i < 5*9; i++)
				inv.setItem(i, nic);
			Func.nazwij(inv.getItem(slotKolor), "§6Kolor");
			Func.nazwij(inv.getItem(slotStyl),  "§6Styl");
			ustawMały(kon, inv);
			ustawStyl(kon, inv);
			ustawKolor(kon, inv);
			ustawBezgłośny(kon, inv);
			return inv;
		}
		private void ustawMały(Horse kon, Inventory inv) {
			if (inv != null)
				inv.setItem(slotMały, mały ? itemMały : itemDuży);
			if (kon != null)
				kon.setAge(mały ? -1 : 0);
		}
		private void ustawBezgłośny(Horse kon, Inventory inv) {
			if (inv != null)
				inv.setItem(slotBezgłośny, bezgłośny ? itemBezgłośny : itemNieBezgłośny);
			if (kon != null)
				kon.setSilent(bezgłośny);
		}
		private void ustawKolor(Horse kon, Inventory inv) {
			kolor = kolory[nrKolor];
			if (inv != null) {
				ItemStack item = inv.getItem(slotKolor);
				item.setType(kolor.mat);
				Func.ustawLore(item, "§e" + kolor, 0);
			}
			if (kon != null)
				kon.setColor(kolor.kolor);
		}
		private void ustawStyl(Horse kon, Inventory inv) {
			styl = style[nrStyl];
			if (inv != null) {
				ItemStack item = inv.getItem(slotStyl);
				item.setType(styl.mat);
				Func.ustawLore(item, "§e" + styl, 0);
			}
			if (kon != null)
				kon.setStyle(styl.styl);
		}
		public void kliknięteEq(InventoryClickEvent ev, Gracz g) {
			int slot = ev.getSlot();
			if (slot < slotyEq || slot >= 5*9) return;
			ev.setCancelled(true);
			int zmiana = ev.getClick().toString().endsWith("RIGHT") ? -1 : 1;
			Horse kon = znajdzKonia();
			switch(slot) {
			case slotMały:
				mały = !mały;
				ustawMały(kon, ev.getInventory());
				break;
			case slotBezgłośny:
				bezgłośny = !bezgłośny;
				ustawBezgłośny(kon, ev.getInventory());
				break;
			case slotKolor:
				nrKolor += zmiana;
				if (nrKolor >= kolory.length)
					nrKolor = 0;
				else if (nrKolor < 0)
					nrKolor = kolory.length -1;
				ustawKolor(kon, ev.getInventory());
				break;
			case slotStyl:
				nrStyl += zmiana;
				if (nrStyl >= style.length)
					nrStyl = 0;
				else if (nrStyl < 0)
					nrStyl = style.length - 1;
				ustawStyl(kon, ev.getInventory());
				break;
			}
			g.zapisz();
		}
		
		public void ustawItemy(Inventory inv) {
			itemy.clear();
			for (int i=0; i<slotyEq; i++)
				if (inv.getItem(i) != null)
					itemy.add(inv.getItem(i));
		}
		
		public static enum Styl {
			Brak(Style.NONE, Material.BARRIER),
			Białe_Nogi(Style.WHITE, Material.WHITE_DYE),
			Białe_Plamki(Style.WHITE_DOTS, Material.WOLF_SPAWN_EGG),
			Białe_Łatki(Style.WHITEFIELD, Material.WHITE_STAINED_GLASS_PANE),
			Czarne_Plamki(Style.BLACK_DOTS, Material.SQUID_SPAWN_EGG);
			
			public Style styl;
			public Material mat;
			Styl(Style styl, Material mat) {
				this.styl = styl;
				this.mat = mat;
			}
			public String toString() {
				return this.name().replace("_", " ");
			}
		}
		public static enum Kolor {
			Biały(Color.WHITE, Material.WHITE_WOOL),
			Czarny(Color.BLACK, Material.BLACK_WOOL),
			Szary(Color.GRAY, Material.GRAY_WOOL),
			Jasny_Brąz(Color.CREAMY, Material.BROWN_CONCRETE),
			Brązowy_Mieszany(Color.CHESTNUT, Material.BROWN_CONCRETE_POWDER),
			Brązowy(Color.BROWN, Material.BROWN_WOOL),
			Ciemny_Brąz(Color.DARK_BROWN, Material.BROWN_TERRACOTTA);
			
			public Color kolor;
			public Material mat;
			Kolor(Color kolor, Material mat) {
				this.kolor = kolor;
				this.mat = mat;
			}
			public String toString() {
				return this.name().replace("_", " ");
			}
		}
	}
	
	
	public static final String prefix = Func.prefix("Koniki");
	
	public Koniki() {
		super("konik", null, "kon", "horse");
		Main.dodajPermisje("konik.bypass");
		int ile = usuńWszystkie();
		if (ile != 0)
			Main.log("§eUsunięto §b" + ile + "§e Niechcianych Koników");
	}
	
	@Override
	public int czas() {
		for (World świat : Bukkit.getWorlds())
			for (Horse h : świat.getEntitiesByClass(Horse.class))
				if (h.hasMetadata("mimiKon")) {
					Gracz.wczytaj(h.getOwner().getName()).kon.sprawdz(h);
				}
		return 400;
	}
	
	private int maxCzas;
	private final HashMap<Material, Integer> mapa = new HashMap<>(); 

	@Override
	public void przeładuj() {
		mapa.clear();
		ConfigurationSection sekcja = Main.ust.sekcja("Koniki.jedzenie");
		if (sekcja != null)
			for (Entry<String, Object> en : sekcja.getValues(false).entrySet())
				mapa.put(Material.valueOf(en.getKey().toUpperCase()), (int) en.getValue()*60);
		maxCzas = (int) Main.ust.wczytajLubDomyślna("Koniki.maxCzas", 3) * 60;
		usuńWszystkie();
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Potrawy dla Koników", mapa.size());
	}
	
	public static int usuńWszystkie() {
		int ile = 0;
		for (World world : Bukkit.getWorlds())
		for (Entity en : world.getEntitiesByClasses(Horse.class)) {
			if (en.hasMetadata("mimiKon")) {
				for (Entity _en : en.getPassengers())
					_en.sendMessage(prefix + "Przeładowywanie Pluginu");
				en.remove();
				ile++;
			}
		}
		return ile;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player)
			przywołaj(Gracz.wczytaj(sender.getName()));
		else
			sender.sendMessage("Tylko gracz może tego użyć");
		return true;
	}

	@EventHandler
	public void siadanie(VehicleEnterEvent ev) {
		if (!(ev.getEntered() instanceof Player)) return;
		if (!(ev.getVehicle() instanceof Horse)) return;
		Player p = (Player) ev.getEntered();
		Horse kon = (Horse) ev.getVehicle();
		if (kon.hasMetadata("mimiKon")) {
			if (!p.hasPermission("mimiRPG.konik.bypass") &&
					!kon.getMetadata("mimiKon").get(0).asString().equals(p.getName())) {
				p.sendMessage(prefix + "§4Ej! §cTo nie twój Konik ziom, nie siadaj.");
				ev.setCancelled(true);
				return;
			}
			if (!Gracz.wczytaj(kon.getMetadata("mimiKon").get(0).asString()).kon.nakarmiony()) {
				p.sendMessage(prefix + "Ten Konik jest zbyt głodny aby mógł cie wozić");
				ev.setCancelled(true);
				return;
			}
		}
	}
	@EventHandler
	public void otwieranieEqKonika(InventoryOpenEvent ev) {
		if (!(ev.getInventory() instanceof HorseInventory)) return;
		HorseInventory inv = (HorseInventory) ev.getInventory();
		Player p = (Player) ev.getPlayer();
		if (!p.hasPermission("mimiRPG.konik")) return;
		Horse kon = (Horse) inv.getHolder();
		otwórz(p, kon, ev);
	}
	@EventHandler
	public void klikanieKonika(PlayerInteractAtEntityEvent ev) {
		if (!(ev.getRightClicked() instanceof Horse)) return;
		Horse kon = (Horse) ev.getRightClicked();
		Player p = ev.getPlayer();
		if (!kon.hasMetadata("mimiKon")) return;
		if (ev.getPlayer().isSneaking() && !kon.isAdult())
			otwórz(p, kon, ev);
		else {
			ItemStack item = p.getInventory().getItemInMainHand();
			if (item != null && mapa.containsKey(item.getType())) {
				int ile = mapa.get(item.getType());
				ev.setCancelled(true);
				Gracz gracz = Gracz.wczytaj(kon.getMetadata("mimiKon").get(0).asString());
				Player graczP = Bukkit.getPlayer(gracz.nick);
				if (gracz.kon.zapas >= System.currentTimeMillis() / 1000 + maxCzas) {
					p.sendMessage(prefix + "Ten Konik jest już najedzony");
					return;
				}
				gracz.kon.nakarm(ile, gracz);
				item.setAmount(item.getAmount() - 1);
				if (!gracz.kon.właściciel.equals(p.getName())) {
					p.sendMessage(prefix + Func.msg("Nakarmiłeś Konika gracza %s może on jezdzić na nim jeszcze %s bez karmienia", gracz.kon.właściciel, czas(gracz.kon.zapas)));
					graczP.sendMessage(prefix + Func.msg("%s Nakarmił twojego Konika, możesz jezdzić na nim jeszcze %s bez karmienia", p.getName(), czas(gracz.kon.zapas)));
				} else {
					p.sendMessage(prefix + Func.msg("Nakarmiłeś swojego Konika możesz jezdzić na nim jeszcze %s bez karmienia", czas(gracz.kon.zapas)));
				}
				return;
			}
			if (!kon.isAdult())
				kon.addPassenger(p);
		}
	}
	public static String czas(int zapas) {
		return Func.czas((int) (zapas - (System.currentTimeMillis() / 1000)));
	}
	private void otwórz(Player p, Horse kon, Cancellable ev) {
		if (kon.hasMetadata("mimiKon")) {
			ev.setCancelled(true);
			String nick = kon.getMetadata("mimiKon").get(0).asString();
			if (p.hasPermission("mimiRPG.konik.bypass") || 
					nick.equals(p.getName()))
				p.openInventory(Gracz.wczytaj(nick).kon.dajInv(kon));
			else
				p.sendMessage(prefix + "§4Ej! §cTo nie twój Konik ziom, nie dotykaj.");
		}
	}

	void przywołaj(Gracz g) {
		if (g.kon == null) {
			g.kon = new Kon(g);
			g.zapisz();
		}
		g.kon.przywołaj(Bukkit.getPlayer(g.nick));
	}
	
	@EventHandler
	public void klikanieEq(InventoryClickEvent ev) {
		if (ev.getView().getTitle().equals("§1§lTwój Konik")) {
			Gracz g = Gracz.wczytaj(((Player) ev.getInventory().getHolder()).getName());
			g.kon.kliknięteEq(ev, g);
		}
	}
	@EventHandler
	public void zamykanieEq(InventoryCloseEvent ev) {
		if (ev.getView().getTitle().equals("§1§lTwój Konik")) {
			Gracz g = Gracz.wczytaj(((Player) ev.getInventory().getHolder()).getName());
			g.kon.ustawItemy(ev.getInventory());
			g.zapisz();
		}
	}
	@EventHandler
	public void opuszczenieGry(PlayerQuitEvent ev) {
		Kon kon = Gracz.wczytaj(ev.getPlayer().getName()).kon;
		if (kon != null)
			kon.usuń();
	}
}

