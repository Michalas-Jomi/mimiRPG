package me.jomi.mimiRPG.RPG_Ultra;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.minecraft.server.v1_16_R3.NBTTagCompound;

import me.jomi.mimiRPG.Baza;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.RPG_Ultra.Enchant.PE;
import me.jomi.mimiRPG.RPG_Ultra.Enchant.PESW;
import me.jomi.mimiRPG.RPG_Ultra.Enchant.PEW;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Panel;

@Moduł
public class CustomoweEnchanty implements Listener {
	public static final String prefix = Func.prefix("Enchanty");
	public static class MinecraftowyEnchant extends Enchant<PE> {
		public final Enchantment ench;
		
		public MinecraftowyEnchant(String nazwa, TypItemu typItemu, Enchantment ench, String opis, PE... poziomy) {
			super(nazwa, typItemu, opis, poziomy);
			this.ench = ench;
		}
		
		@Override
		public void zaaplikuj(ItemStack item, int lvl) {
			Func.enchantuj(item, ench, lvl);
			super.zaaplikuj(item, lvl);
		}
		@Override
		public void odaplikuj(ItemStack item) {
			ItemMeta meta = item.getItemMeta();
			meta.removeEnchant(ench);
			item.setItemMeta(meta);
			super.odaplikuj(item);
		}
	}
	public static class AtrybutowyEnchant extends Enchant<PEW> {
		public final Atrybut attr;
		
		public AtrybutowyEnchant(String nazwa, TypItemu typItemu, Atrybut attr, String opis, PEW... poziomy) {
			super(nazwa, typItemu, opis, poziomy);
			this.attr = attr;
		}
		@Override
		public void zaaplikuj(ItemStack item, int lvl) {
			Boost.dodajBoost(item, attr, getPoziom(lvl).wartość);
			super.zaaplikuj(item, lvl);
		}
		@Override
		public void odaplikuj(ItemStack item) {
			Boost.dodajBoost(item, attr, getPoziom(item).wartość);
			super.odaplikuj(item);
		}
	}
	
	public static final Enchant<?> ciernie				= new MinecraftowyEnchant("Ciernie",				TypItemu.ARMOR,		Enchantment.THORNS,						"Zadaje niewielke obrażenia atakującemu.", new PE(10), new PE(30), new PE(50));
	public static final Enchant<?> głębinowy_wędrowiec	= new MinecraftowyEnchant("Głębinowy wędrowiec",	TypItemu.BUTY,		Enchantment.DEPTH_STRIDER,				"Umożliwia szybsze poruszanie się w wodzie.", new PE(10), new PE(20), new PE(30));
	public static final Enchant<?> jedwabny_dotyk		= new MinecraftowyEnchant("Jedwabny dotyk",			TypItemu.NARZĘDZIE,	Enchantment.SILK_TOUCH,					"Gracz uzyskuje blok który wydobywa,\nczyli np. gdy wydobywa kamień to uzyska kamień.", new PE(30));
	public static final Enchant<?> moc					= new MinecraftowyEnchant("Moc",					TypItemu.ŁUK,		Enchantment.ARROW_DAMAGE,				"Zwiększa dmg łuku o {%}", new PE(10), new PE(20), new PE(30), new PE(40), new PE(50));
	public static final Enchant<?> morska_fortuna		= new MinecraftowyEnchant("Morska fortuna",			TypItemu.WĘDKA,		Enchantment.LUCK,						"Zwiększa szanse na złowienie cennych przedmiotów.", new PE(10), new PE(20), new PE(30));
	public static final Enchant<?> mroźny_pierchur		= new MinecraftowyEnchant("Mroźny pierchur",		TypItemu.BUTY,		Enchantment.FROST_WALKER,				"Zamienia wodę pod graczem w oszroniony lód.", new PE(10), new PE(20), new PE(30));
	public static final Enchant<?> ochrona				= new MinecraftowyEnchant("Ochrona",				TypItemu.ARMOR,		Enchantment.PROTECTION_ENVIRONMENTAL,	"Zmniejsza obrażenia o {%}", new PE(10), new PE(20), new PE(30));
	public static final Enchant<?> ochrona_ogień		= new MinecraftowyEnchant("Ochrona ogień",			TypItemu.ARMOR,		Enchantment.PROTECTION_FIRE,			"Zmniejsza obrażenia od ognia o {%}.", new PE(10), new PE(20), new PE(30));
	public static final Enchant<?> ochrona_pociski		= new MinecraftowyEnchant("Ochrona pociski",		TypItemu.ARMOR,		Enchantment.PROTECTION_PROJECTILE,		"Zmniejsza obrażenia od strzał o {%}.\nOchrona przed strzałami, ognistymi kulami,\nkulami smoka i kulami lewitacji.", new PE(10), new PE(20), new PE(30));
	public static final Enchant<?> ochrona_wybuch		= new MinecraftowyEnchant("Ochrona wybuch",			TypItemu.ARMOR,		Enchantment.PROTECTION_EXPLOSIONS,		"Zmniejsza obrażenia od wybuchów o {%}.", new PE(10), new PE(20), new PE(30));
	public static final Enchant<?> oddychanie			= new MinecraftowyEnchant("Oddychanie",				TypItemu.HEŁM,		Enchantment.OXYGEN,						"Zmniejsza utratę powietrza przy długim nurkowaniu.", new PE(10), new PE(20), new PE(30));
	public static final Enchant<?> odrzut				= new MinecraftowyEnchant("Odrzut",					TypItemu.BROŃ_BIAŁA,Enchantment.KNOCKBACK,					"Odrzuca moby.", new PE(10), new PE(20), new PE(30));
	public static final Enchant<?> odrzutłuk			= new MinecraftowyEnchant("Odrzutłuk",				TypItemu.ŁUK,		Enchantment.ARROW_KNOCKBACK,			"Moby są odrzucane dalej niż zwykle.", new PE(10), new PE(20), new PE(30));
	public static final Enchant<?> pogromca_nieumarłuch	= new MinecraftowyEnchant("Pogromca nieumarłuch",	TypItemu.BROŃ_BIAŁA,Enchantment.DAMAGE_UNDEAD,				"Zwiększa obrażenia przeciwko zombie o {%}", new PE(10), new PE(20), new PE(30));
	public static final Enchant<?> powolne_opadanie		= new MinecraftowyEnchant("Powolne opadanie",		TypItemu.BUTY,		Enchantment.PROTECTION_FALL,			"Zmniejsza obrażenia od upadku o {%}.", new PE(10), new PE(20), new PE(30));
	public static final Enchant<?> szerokie_ostrze		= new MinecraftowyEnchant("Szerokie ostrze",		TypItemu.BROŃ_BIAŁA,Enchantment.SWEEPING_EDGE,				"Zwiększa zadawane obrażenia pobliskim mobom\npodczas serii ataków o {%}.", new PE(10), new PE(20), new PE(30));
	public static final Enchant<?> szybkie_ładowanie	= new MinecraftowyEnchant("Szybkie ładowanie",		TypItemu.KUSZA,		Enchantment.QUICK_CHARGE,				"Skraca czas ładowania kuszy.", new PE(10), new PE(20), new PE(30));
	public static final Enchant<?> wydajność_pod_wodą	= new MinecraftowyEnchant("Wydajność pod wodą",		TypItemu.HEŁM,		Enchantment.WATER_WORKER,				"Przyspiesza wydobywanie pod wodą.", new PE(10), new PE(20), new PE(30));
	public static final Enchant<?> zaklęty_ogień		= new MinecraftowyEnchant("Zaklęty ogień",			TypItemu.BROŃ_BIAŁA,Enchantment.FIRE_ASPECT,				"Moby stają w ogniu.\nZadaje {serca} dodatkowych obrażeń na sekunde.", new PE(10), new PE(20), new PE(30));
	public static final Enchant<?> zmora_stawonogów		= new MinecraftowyEnchant("Zmora stawonogów",		TypItemu.BROŃ_BIAŁA,Enchantment.DAMAGE_ARTHROPODS,			"Zwiększa obrażenia przeciwko pająkom o {%}.\nDodatkowo nadaje tym mobom\nefekt spowolnienia IV na {sekundy}", new PE(10), new PE(20), new PE(30));
	
	public static final Enchant<?> ostrość				= new AtrybutowyEnchant("Ostrość",	TypItemu.BROŃ_BIAŁA,Atrybut.SIŁA,				"Zwiększa obrażenia o {wartość}",			new PEW(10, 1), new PEW(30, 3),	new PEW(50, 5));
	public static final Enchant<?> wydajność			= new AtrybutowyEnchant("Wydajność",TypItemu.NARZĘDZIE,	Atrybut.PRĘDKOŚĆ_KOPANIA,	"Zwiększa prędkość kopania o {wartość}",	new PEW(10, 10),new PEW(30, 20),new PEW(50, 30));
	public static final Enchant<?> mocarz				= new AtrybutowyEnchant("Mocarz",	TypItemu.ARMOR,		Atrybut.HP,					"Zwiększa hp o {wartość}",					new PEW(10, 10),new PEW(20, 25),new PEW(30, 40), new PEW(40, 60), new PEW(50, 75));
	public static final Enchant<?> piechur				= new AtrybutowyEnchant("Piechur",	TypItemu.BUTY,		Atrybut.PRĘDKOŚĆ_CHODZENIA,	"Zwiększa prędkość chodzenia o {wartość}",	new PEW(10, 5),	new PEW(30, 10),new PEW(50, 15), new PEW(75, 20),  new PEW(100, 25));
	public static final Enchant<?> krytyki				= new AtrybutowyEnchant("Krytyki",	TypItemu.BROŃ,		Atrybut.KRYT_DMG,			"Zwiększa twój dmg z kryta o {wartość}.",	new PEW(30, 10),new PEW(50, 20),new PEW(75, 30), new PEW(100, 40), new PEW(150, 50));
	
	public static final Enchant<PESW> biorca_życia		= new Enchant<>("Biorca życia",			TypItemu.BROŃ_BIAŁA,		"Za każdym razem, gdy trafisz moba,\nz szansą {szansa}% leczy cię o {wartość}%\ntwojego maksymalnego zdrowia.", new PESW(20, .1, 1), new PESW(40, .15, 2), new PESW(75, .2, 3));
	public static final Enchant<?> exp					= new Enchant<>("Exp",					TypItemu.SPRZĘT,			"Zwiększa ci szanse o {szansa}% na podwójny EXP", new PE(10), new PE(20), new PE(30));
	public static final Enchant<?> exp_oceanu			= new Enchant<>("Exp oceanu",			TypItemu.WĘDKA,				"Wiecej expa z łowienia", new PE(10), new PE(20), new PE(30));
	public static final Enchant<?> fortuna				= new Enchant<>("Fortuna",				TypItemu.NARZĘDZIE,			"{szansa}% szansy na podwójny drop.", new PE(10), new PE(20), new PE(30));
	public static final Enchant<?> gorący_dotyk			= new Enchant<>("Gorący dotyk",			TypItemu.NARZĘDZIE,			"Wykopane itemy zostają przepalone.", new PE(10), new PE(20), new PE(30));
	public static final Enchant<?> grabież				= new Enchant<>("Grabież",				TypItemu.BROŃ,				"Zwiększa szansę na drop o {szansa}%.", new PE(10), new PE(20), new PE(30));
	public static final Enchant<PEW> jednostrzałowiec	= new Enchant<>("Jednostrzałowiec",		TypItemu.BROŃ_BIAŁA,		"Mocno zwiększony dmg z pierwszego\nuderzenia w moba o {wartość}%", new PEW(10, 25), new PEW(30, 50), new PEW(50, 75), new PEW(75, 100), new PEW(100, 125));
	public static final Enchant<?> moneciarz			= new Enchant<>("Moneciarz",			TypItemu.BROŃ,				"Dodatkowe {wartość} monety za zabójstwa mobów.", new PE(10), new PE(20), new PE(30));
	public static final Enchant<?> multishot			= new Enchant<>("Multishot",			TypItemu.BROŃ_DYSTANSOWA,	"Wystrzeliwuje pod rząd 2 strzały\njeżeli ile jest w eq.", new PE(10), new PE(20), new PE(30));
	public static final Enchant<?> oszczędny			= new Enchant<>("Oszczędny",			TypItemu.BROŃ_DYSTANSOWA,	"{szansa}% szansy na pozostawienie\nstrzały użytej w twoim eq", new PE(10), new PE(20), new PE(30));
	public static final Enchant<?> pogromca_gigantów	= new Enchant<>("Pogromca gigantów",	TypItemu.BROŃ,				"Im więcej mob ma hp względem ciebie,\ntym mocniej go bijesz.", new PE(10), new PE(20), new PE(30));
	public static final Enchant<?> przeszycie			= new Enchant<>("Przeszycie",			TypItemu.BROŃ_DYSTANSOWA,	"{szansa}% szansy na to że strzała\nprzeleci przez moba zadając mu obrażenia.", new PE(10), new PE(20), new PE(30));
	public static final Enchant<?> przynęta				= new Enchant<>("Przynęta",				TypItemu.WĘDKA,				"Zwiększa szanse o {szansa}% na złowienie\nryby i przyśpiesza łowienie.", new PE(10), new PE(20), new PE(30));
	public static final Enchant<?> płomień				= new Enchant<>("Płomień",				TypItemu.BROŃ_DYSTANSOWA,	"Strzały i moby stają w płomieniach.", new PE(10), new PE(20), new PE(30));
	public static final Enchant<?> snajper				= new Enchant<>("Snajper",				TypItemu.BROŃ_DYSTANSOWA,	"Im więcej bloków strzała przeleci,\ntym większy dmg zadany o {wartość}%", new PE(10), new PE(20), new PE(30));
	public static final Enchant<?> szczęście			= new Enchant<>("Szczęście",			TypItemu.BROŃ,				"{szansa}% szansy na podwójny drop.", new PE(10), new PE(20), new PE(30));
	public static final Enchant<?> telekineza			= new Enchant<>("Telekineza",			TypItemu.SPRZĘT,			"Itemki z mobów i bloków trafiają\nbezpośrednio do twojego eq.", new PE(10), new PE(20), new PE(30));
	public static final Enchant<?> tęczowy_dotyk		= new Enchant<>("Tęczowy dotyk",		TypItemu.NORZYCE,			"Golone owce dropią wełnę w losowych kolorach.", new PE(10), new PE(20), new PE(30));
	public static final Enchant<PEW> wampir				= new Enchant<>("Wampir",				TypItemu.BROŃ_BIAŁA,		"Za każdym razem, gdy zabijesz moba,\nleczy cię o {wartość}% twojego maksymalnego zdrowia.", new PEW(10, 1), new PEW(30, 2), new PEW(50, 3));
	public static final Enchant<?> wodny_farciarz		= new Enchant<>("Wodny farciarz",		TypItemu.WĘDKA,				"{szansa}% szansy na podwójny drop z łowienia.", new PE(10), new PE(20), new PE(30));
	public static final Enchant<?> włóczęga				= new Enchant<>("Włóczęga",				TypItemu.BROŃ_BIAŁA,		"Przy każdym uderzeniu moba {szansa}% szansy\nna spowalnienie moba na {wartość} sekund.", new PE(10), new PE(20), new PE(30));
	public static final Enchant<PESW> zeus				= new Enchant<>("Zeus",					TypItemu.BROŃ_BIAŁA,		"{szansa}% szansy na piorun który uderzy\nw moba z mocą {wartość}% twojej siły.", new PESW(10, .1, 33), new PESW(20, .15, 66), new PESW(30, .20, 100), new PESW(50, .3, 120));
	
	static ItemStack itemStrzełkaWGórę = Func.dajGłówkę("&6Poprzednia Strona",	"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODczMzdjMmI0YzFhMDNlN2MwYzA0MjIzNTUxMDNhNzgzMmRjNjQ4N2Q3NjZmY2RmNTA5ZDkyZjIyMWUxNDFmOSJ9fX0=");
	static ItemStack itemStrzałkaWDół  = Func.dajGłówkę("&6Następna Strona",	"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDRmN2JjMWZhODIxN2IxOGIzMjNhZjg0MTM3MmEzZjdjNjAyYTQzNWM4MjhmYWE0MDNkMTc2YzZiMzdiNjA1YiJ9fX0=");
	static int slotStrzałkaWGórę = 26;
	static int slotStrzałkaWDół = 35;
	static int slotItemku = 19;
	static final Panel panel = new Panel(true);
	static final Panel panelEnchantu = new Panel(true);
	
	public CustomoweEnchanty() {
		panel.ustawClose(ev -> Func.wykonajDlaNieNull(ev.getInventory().getItem(slotItemku), item -> Func.dajItem((Player) ev.getPlayer(), item)));
		panel.ustawClick(ev -> {
			Inventory inv = ev.getInventory();
			int slot = ev.getRawSlot();
			if (slot == slotItemku) {
				Bukkit.getScheduler().runTask(Main.plugin, () -> odświeżEnchanty(inv));
				ev.setCancelled(false);
			} else if (slot == slotStrzałkaWGórę) {
				if (!ev.getCurrentItem().isSimilar(Baza.pustySlotCzarny)) {
					panel.ustawDanePanelu(inv, (int) panel.dajDanePanelu(inv) - 1);
					odświeżEnchanty(inv);
				}
			} else if (slot == slotStrzałkaWDół) {
				if (!ev.getCurrentItem().isSimilar(Baza.pustySlotCzarny)) {
					panel.ustawDanePanelu(inv, (int) panel.dajDanePanelu(inv) + 1);
					odświeżEnchanty(inv);
				}
			}
			else {
				int r = slot / 9;
				int mod = slot % 9;
				
				if (r >= 1 && r <= 3 && mod >= 3 && mod <= 7) {
					if (ev.getCurrentItem().isSimilar(Baza.pustySlotCzarny))
						return;
					
					ItemStack item = inv.getItem(slotItemku);
					Enchant<?> ench = Enchant.getEnchant(ev.getCurrentItem().getItemMeta().getDisplayName().substring(2));
					
					inv.setItem(slotItemku, null);
					otwórzEnchant((Player) ev.getWhoClicked(), item, ench);
				}
			}
		});
		panelEnchantu.ustawClick(ev -> {
			if (!ev.getCurrentItem().getItemMeta().hasCustomModelData()) return;
			Krotka<ItemStack, Enchant<?>> krotka = panelEnchantu.dajDanePaneluPewny(ev.getInventory());
			krotka.wykonaj((item, ench) -> {
				int enchLvl = ev.getCurrentItem().getItemMeta().getCustomModelData();
				PE poziom = ench.getPoziom(enchLvl);
				Player p = (Player) ev.getWhoClicked();
				
				if (p.getLevel() < poziom.cena)
					Func.powiadom(prefix, p, "Nie masz wystarczająco dużo expa");
				else {
					p.giveExpLevels(-poziom.cena);
					if (ench.getLvl(item) != 0)
						ench.odaplikuj(item);
					ench.zaaplikuj(item, enchLvl);
					Func.powiadom(prefix, p, "Zenchantowałeś item %s na poziom %s", Func.nazwaItemku(item), enchLvl);
					Main.log(prefix + "%s zenchantował item %s na poziom %s", p.getName(), Func.nazwaItemku(item), enchLvl);
				}
			});
		});
		panelEnchantu.ustawClose(ev -> {
			Krotka<ItemStack, Enchant<?>> krotka = panelEnchantu.dajDanePaneluPewny(ev.getInventory());
			krotka.wykonaj((item, ench) -> 
				Bukkit.getScheduler().runTask(Main.plugin, () -> {
					Inventory inv = otwórzEnchant((Player) ev.getPlayer());
					inv.setItem(slotItemku, item);
					odświeżEnchanty(inv);
			}));
		});
	}
	private void otwórzEnchant(Player p, ItemStack item, Enchant<?> ench) {
		Inventory inv = panelEnchantu.stwórz(new Krotka<>(item, ench), 3, "§5§lEnchant §6§l" + ench.nazwa);
		Func.ustawPuste(inv, Baza.pustySlotCzarny);
		
		int[] sloty = Func.sloty(ench.getPoziomy(), 1);
		for (int i=0; i < sloty.length; i++)
			inv.setItem(sloty[i] + 9, ench.ikona(i + 1));
		
		p.openInventory(inv);
	}
	public static Inventory otwórzEnchant(Player p) {
		Inventory inv = panel.stwórz(0, 5, "§5§lEnchanting");
		Func.ustawPuste(inv, Baza.pustySlotCzarny);
		
		inv.setItem(slotItemku, null);
		inv.setItem(slotItemku + 9, Func.stwórzItem(Material.ENCHANTING_TABLE, "§aZenchantój item", "§7Umieść item do zenchantowania wyżej", "§7a następnie wybierz enchant dla niego"));
		
		p.openInventory(inv);
		return inv;
	}
	static void odświeżEnchanty(Inventory inv) {
		ItemStack item = inv.getItem(slotItemku);
		if (item == null) return;
		
		Player p = (Player) inv.getViewers().get(0);
		GraczRPG gracz = GraczRPG.gracz(p);
		
		
		for (int i=0; i < 15; i++)
			inv.setItem((i / 5 + 1) * 9 + i % 5 + 3, Baza.pustySlotCzarny);
		
		int i = 0;
		int strona = panel.dajDanePaneluPewny(inv);
		inv.setItem(slotStrzałkaWDół, Baza.pustySlotCzarny);
		inv.setItem(slotStrzałkaWGórę, strona > 0 ? itemStrzełkaWGórę : Baza.pustySlotCzarny);
		for (Enchant<?> ench : Enchant.getEnchanty()) {
			if (!TypItemu.typ(item).pasuje(ench.typItemu)) continue;
			if (!ench.maDostęp(gracz)) continue;
			if (i >= (strona + 1) * 15) {
				inv.setItem(slotStrzałkaWDół, itemStrzałkaWDół);
				break;
			}
			
			if (i >= strona * 15) {
				int slot = i % 15;
				slot = (slot / 5 + 1) * 9 + slot % 5 + 3;
				inv.setItem(slot, ench.ikona());
			}
			i++;
		}
	}
	
	@EventHandler
	public void otwieranieEnchantu(InventoryOpenEvent ev) {
		if (ev.getInventory().getType() != InventoryType.ENCHANTING) return;
		
		ev.setCancelled(true);
		otwórzEnchant((Player) ev.getPlayer());
	}
	@EventHandler
	public void klikanieEq(InventoryClickEvent ev) {
		if (ev.getRawSlot() >= ev.getInventory().getSize() && panel.jestPanelem(ev.getInventory()) && ev.isShiftClick())
			Bukkit.getScheduler().runTask(Main.plugin, () -> odświeżEnchanty(ev.getInventory()));
	}
	

	// Obsługa Eventów
	@EventHandler(priority = EventPriority.HIGH)
	public void uderzanie(EntityDamageByEntityEvent ev) {
		if (ev.isCancelled() || ev.getDamage() <= 0) return;
		if (ev.getDamager() instanceof Player) {
			Player p = (Player) ev.getDamager();
			NBTTagCompound tag;
			try {
				tag = Enchant.enchantyUnsafe(p.getInventory().getItemInMainHand());
			} catch (Throwable e) {
				return;
			}
			if (tag.isEmpty())
				return;
			
			Func.wykonajDlaNieNull(zeus.getPoziom(tag), lvl -> {
				if (lvl.losuj()) {
					ev.getEntity().getWorld().strikeLightningEffect(ev.getEntity().getLocation());
					ev.setDamage(ev.getDamage() + GraczRPG.gracz(p).siła.wartość() * lvl.wartość());
				}
			});
			Func.wykonajDlaNieNull(jednostrzałowiec.getPoziom(tag), lvl -> {
				if (ev.getEntity() instanceof LivingEntity) {
					LivingEntity mob = (LivingEntity) ev.getEntity();
					if (mob.getHealth() == mob.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue())
						ev.setDamage(ev.getDamage() * lvl.wartość());
				}
			});
			Func.wykonajDlaNieNull(biorca_życia.getPoziom(tag), lvl -> {
				if (lvl.losuj())
					p.setHealth(p.getHealth() + p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * lvl.wartość());
			});
		}
	}
	@EventHandler(priority = EventPriority.HIGH)
	public void śmierć(EntityDeathEvent ev) {
		Func.wykonajDlaNieNull(ev.getEntity().getKiller(), p -> {
			NBTTagCompound tag;
			try {
				tag = Enchant.enchantyUnsafe(p.getInventory().getItemInMainHand());
			} catch (Throwable e) {
				return;
			}
			if (tag.isEmpty())
				return;
			
			Func.wykonajDlaNieNull(wampir.getPoziom(tag), lvl -> {
				p.setHealth(p.getHealth() + p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * lvl.wartość());
			});
			
		});
	}
}
