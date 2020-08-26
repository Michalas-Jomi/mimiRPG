package me.jomi.mimiRPG.Miniony;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftItem;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.Vector;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Config;
import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.MenuStronne;
import me.jomi.mimiRPG.Edytory.EdytujItem;
import net.minecraft.server.v1_16_R1.EntityZombie;
import net.minecraft.server.v1_16_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_16_R1.PathfinderGoalSelector;

public abstract class Minion extends EntityZombie {
	public static String prefix = Miniony.prefix;
	protected static ItemStack ekwipunekObw�dka 	 = Func.stw�rzItem(Material.GRAY_STAINED_GLASS_PANE, 1, "&2Ekwipunek", 	 null);
	protected static ItemStack ekwipunekZablkowany 	 = Func.stw�rzItem(Material.BLACK_STAINED_GLASS_PANE,1, "&cZablokowane", Arrays.asList("&aMo�na odblokowa�", "&aza pewn� op�at�"));
	protected static ItemStack ulepszeniaObw�dka 	 = Func.stw�rzItem(Material.GRAY_STAINED_GLASS_PANE, 1, "&2Ulepszenia",  null);
	protected static ItemStack menuObw�dka 		 	 = Func.stw�rzItem(Material.GRAY_STAINED_GLASS_PANE, 1, "&2Menu", 		 null);
	protected static ItemStack usu�Item 			 = Func.stw�rzItem(Material.BARRIER, 1, "&9Zabierz", Arrays.asList("&bZabiera miniona", "&bDo twojego ekwipunku"));
	protected static ItemStack woda 				 = Func.stw�rzItem(Material.BLUE_STAINED_GLASS_PANE, 1, "&9Woda", 		 Arrays.asList("&3Aktualny stan:&e co� nie pyk�o, poinformuj admina&3mb"));
	protected static ItemStack jedzenie 			 = Func.stw�rzItem(Material.BAKED_POTATO, 			 1, "&6Jedzenie", 	 Arrays.asList("&3Zapas:&e co� nie pyk�o, poinformuj admina"));
	protected static ItemStack ulepszenia 			 = Func.dajG��wk�("&6Ulepszenia", 	"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmQ5Mjg3NjE2MzQzZDgzM2U5ZTczMTcxNTljYWEyY2IzZTU5NzQ1MTEzOTYyYzEzNzkwNTJjZTQ3ODg4NGZhIn19fQ==", null);
	protected static ItemStack wodaInfo 			 = Func.dajG��wk�("&2Woda", 		"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGM0ZTQ0MWVhYzg4NGRlMzM0N2E4Nzc1YTA3YTY2YmJjNGM4MmEyNGVkMmQwY2ZlYjFhY2FmNmNlOTlkNTNiNiJ9fX0=", Arrays.asList("&aTw�j minion ma te� swoje potrzeby", "&aMusisz mu regularnie przynosi�", "&aZar�wno jedzenie jak i picie"));
	protected static ItemStack jedzenieInfo 		 = Func.dajG��wk�("&2Jedzenie", 	"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGM0ZTQ0MWVhYzg4NGRlMzM0N2E4Nzc1YTA3YTY2YmJjNGM4MmEyNGVkMmQwY2ZlYjFhY2FmNmNlOTlkNTNiNiJ9fX0=", Arrays.asList("&aTw�j minion ma te� swoje potrzeby", "&aMusisz mu regularnie przynosi�", "&aZar�wno jedzenie jak i picie"));
	protected static ItemStack ulepszeniaInfo 		 = Func.dajG��wk�("&2Ulepszenia", 	"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGM0ZTQ0MWVhYzg4NGRlMzM0N2E4Nzc1YTA3YTY2YmJjNGM4MmEyNGVkMmQwY2ZlYjFhY2FmNmNlOTlkNTNiNiJ9fX0=", Arrays.asList("&aMinion to nie maszyna!", "&aGo te� trzeba nauczy�.", "&aZna tylko podstawy", "&aTutaj mo�esz go szkoli�"));
	protected static ItemStack r�kaInfo 			 = Func.dajG��wk�("&2Narz�dzie", 	"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGM0ZTQ0MWVhYzg4NGRlMzM0N2E4Nzc1YTA3YTY2YmJjNGM4MmEyNGVkMmQwY2ZlYjFhY2FmNmNlOTlkNTNiNiJ9fX0=", Arrays.asList("&aNarz�dzie to niezb�dna rzecz", "&aPotrzebna do pracy", "&aUmie�� je obok"));

	protected HashMap<String, MenuStronne> mapaMenuStronne = new HashMap<>();
	
	public static final HashMap<Integer, Minion> mapa = new HashMap<>();
	
	public String imie = "�1Minion";
	
	protected List<String> gracze = Lists.newArrayList();
	protected String stworzyciel;
	
	protected List<ItemStack> ekwipunek = Lists.newArrayList();
	protected ItemStack narz�dzie = new ItemStack(Material.AIR);
	
	protected int stanWody = 0;
	protected double stanJedzenia = 0;
	
	protected List<Statystyka> staty = Lists.newArrayList();
	
	protected Location loc;
	
	protected boolean podnoszenie = false;

	public int id;
	
	protected int czas = 200;
	
	protected static int ulepszanieEkwipunkuCena = 5000;
	protected static int ulepszaniePodnoszenieItem�wCena = 2000;
	
	
	protected static HashMap<Material, Double> mapaJedzenia = new HashMap<>();
	
	public Config zapisz() {
		Config config = new Config("miniony/" + id);
		
		config.ustaw("id", id);
		
		config.ustaw("imie", imie);
		
		config.ustaw("stanWody", stanWody);
		config.ustaw("stanJedzenia", stanJedzenia);
		
		config.ustaw("gracze", gracze);
		config.ustaw("stworzyciel", stworzyciel);
		
		config.ustaw("narz�dzie", narz�dzie);
		config.ustaw("ekwipunek", ekwipunek);
		
		config.ustaw("loc", loc);
		
		config.ustaw("podnoszenie", podnoszenie);
		
		
		for (Statystyka s : staty)
			s.zapisz(config);
		
		config.zapisz();
		
		return config;
	}
	protected abstract void init();
	protected abstract void init(Config config);
	
	
	public Minion(Player p, ItemStack item) {
		super(((CraftWorld) p.getWorld()).getHandle());
		
		loc = p.getLocation();
		
		List<String> lore = item.getItemMeta().getLore();
		imie = lore.get(2);
		gracze = Lists.newArrayList(lore.get(3).substring(15).split(", "));
		gracze.remove(0);
		stworzyciel = p.getName();
		if (!gracze.contains(stworzyciel)) gracze.add(stworzyciel);
		podnoszenie = lore.get(4).split(" ")[1].startsWith("�a");
		
		stanWody = EdytujItem.sprawdz_liczbe(lore.get(5).split(" ")[1], 0);
		stanJedzenia = Func.sprawdz_poprawno��(lore.get(6).split(" ")[1], 0);
		
		staty.add(new Statystyka(lore.get(7)));
		staty.add(new Statystyka(lore.get(8)));
		staty.add(new Statystyka(lore.get(9)));

		id = 0;
		while (mapa.containsKey(++id));
		
		init();
		
		zrespMoba();
		
	}
	@SuppressWarnings("unchecked")
	public Minion(Config config) {
		super(((CraftWorld) ((Location) config.wczytaj("loc")).getWorld()).getHandle());
		
		id = config.wczytajInt("id");
		
		loc = (Location) config.wczytaj("loc");
		imie = (String) config.wczytaj("imie");
		
		stanWody = (int) config.wczytajLubDomy�lna("stanWody", 0);
		stanJedzenia = (double) config.wczytajLubDomy�lna("stanJedzenia", 0);
		
		gracze = (List<String>) config.wczytaj("gracze");
		stworzyciel = (String) config.wczytaj("stworzyciel");
		
		narz�dzie = (ItemStack) config.wczytaj("narz�dzie");
		ekwipunek = (List<ItemStack>) config.wczytaj("ekwipunek");

		podnoszenie = (boolean) config.wczytajLubDomy�lna("podnoszenie", false);
		
		staty.add(new Statystyka(config, "Pr�dko��"));
		staty.add(new Statystyka(config, "Zu�ycieWody"));
		staty.add(new Statystyka(config, "Zu�ycieJedzenia"));

		init(config);
		
		zrespMoba();

	}
	public Minion(Location loc, String stworzyciel, String imie) {
		super(((CraftWorld) loc.getWorld()).getHandle());
		this.stworzyciel = stworzyciel;
		this.imie = imie;
		this.loc = loc;
		ekwipunek.add(new ItemStack(Material.AIR));
		ekwipunek.add(new ItemStack(Material.AIR));
		gracze.add(stworzyciel);

		staty.add(new Statystyka("Pr�dko��"));
		staty.add(new Statystyka("Zu�ycieWody"));
		staty.add(new Statystyka("Zu�ycieJedzenia"));
		
		id = 0;
		while (mapa.containsKey(++id));
		Main.log(imie, id);
		
		init();
		
		zrespMoba();
	}
	
	protected void zrespMoba() {
		getBukkitEntity().teleport(loc);
		addScoreboardTag("mimiMinion");
		addScoreboardTag("mimiMinion_" + id);
		world.addEntity(this);

		((LivingEntity) getBukkitEntity()).setRemoveWhenFarAway(false);
		((LivingEntity) getBukkitEntity()).getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(15);
		
		setBaby(false);
		setSilent(true);
		setJumping(false);
		setCanPickupLoot(false);
		setCustomNameVisible(true);
		setCustomName(ChatSerializer.a("{\"text\":\"" + imie + "\"}"));
		
		targetSelector 	= new PathfinderGoalSelector(getWorld().getMethodProfilerSupplier());

		ustawNarz�dzie(narz�dzie);
    	ubierz();
    	
		EntityEquipment eq = ((LivingEntity) getBukkitEntity()).getEquipment();
		if (eq != null) {
			eq.setHelmetDropChance(0);
			eq.setChestplateDropChance(0);
			eq.setLeggingsDropChance(0);
			eq.setBootsDropChance(0);
			eq.setItemInMainHandDropChance(0);
			eq.setItemInOffHandDropChance(0);
		}
		
		mapa.put(id, this);
		zapisz();
	}
	
	
	protected abstract void ubierz();
	protected void ubierz(ItemStack he�m, ItemStack klata, ItemStack spodnie, ItemStack buty) {
		EntityEquipment eq = ((LivingEntity) getBukkitEntity()).getEquipment();
		eq.setHelmet(he�m);
		eq.setChestplate(klata);
		eq.setLeggings(spodnie);
		eq.setBoots(buty);
	}
	
	protected void idzDo(Vector v, float speed) {
		idzDo(v.getX(), v.getY(), v.getZ(), speed);
	}
	protected void idzDo(Location v, float speed) {
		idzDo(v.getX(), v.getY(), v.getZ(), speed);
	}
	protected void idzDo(double x, double y, double z, float speed) {
		getNavigation().a(x, y, z, speed);
	}
	
	protected void u�yjNarz�dzia() {
		if (narz�dzie != null && !narz�dzie.getType().equals(Material.AIR)) {
			if (narz�dzie.getItemMeta().isUnbreakable())
				return;
			int mx = narz�dzie.getType().getMaxDurability();
			Damageable meta = (Damageable) narz�dzie.getItemMeta();
			if (mx > 0) {
				int unbr = narz�dzie.getEnchantmentLevel(Enchantment.DURABILITY);
				int los = Func.losuj(1, 100);
				if (los <= (100 / (unbr+1)))
					meta.setDamage(meta.getDamage()+1);
				if (meta.getDamage() > narz�dzie.getType().getMaxDurability())
					ustawNarz�dzie(new ItemStack(Material.AIR));
				else
					narz�dzie.setItemMeta((ItemMeta) meta);
			}
		}
	}
	protected void wykop(Block b) {
		if (b != null && !b.getType().equals(Material.AIR)) {
			b.breakNaturally(narz�dzie);
			u�yjNarz�dzia();
		}
	}
	
	protected abstract void mimiTick();
	protected boolean mimiTick(boolean ujmij) {
		if (Miniony.otwarte.containsValue(id))
			return false;
		
		if (stanWody >= staty.get(1).akt && stanJedzenia >= staty.get(2).akt) {
			if (ujmij) {
				stanWody -= staty.get(1).akt;
				stanJedzenia -= staty.get(2).akt;
			}
			return true;
		}
		return false;
	}
	protected void _mimiTick(int ile) {
		if (czas <= 0) {
			mimiTick();
			czas = (int) staty.get(0).akt;
		} else
			czas -= ile;
	}
	
	protected void ulepszenia(Player p) {
		Inventory inv = Bukkit.createInventory(null, 4*9, "�4�lUlepszenia");
		for (int i=0; i<inv.getSize(); i++)
			inv.setItem(i, ulepszeniaObw�dka);
		
		inv.setItem(18, Func.dajG��wk�("�aDodaj Gracza", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjA1NmJjMTI0NGZjZmY5OTM0NGYxMmFiYTQyYWMyM2ZlZTZlZjZlMzM1MWQyN2QyNzNjMTU3MjUzMWYifX19", 		  Arrays.asList("Dodaj gracza do miniona")));
		inv.setItem(27, Func.dajG��wk�("�cUsu� Gracza",  "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGU0YjhiOGQyMzYyYzg2NGUwNjIzMDE0ODdkOTRkMzI3MmE2YjU3MGFmYmY4MGMyYzViMTQ4Yzk1NDU3OWQ0NiJ9fX0=", Arrays.asList("Usu� gracza z miniona")));
		
		List<String> lista = Lists.newArrayList();
		for (String gracz : gracze)
			lista.add("�e�l- �a" + gracz);
		inv.setItem(28, Func.stw�rzItem(Material.BOOK, 1, "&6Gracze:", lista));
		
		inv.setItem(35, usu�Item);
		ulepszeniaOd�wie�(inv);
		
		p.openInventory(inv);
		Miniony.otwarte.put(p.getName(), id);
	}
	protected void ulepszeniaOd�wie�(Inventory inv){
		ustawItem(inv, 11, Material.CHEST, 	  	  "&2Pojemno��",		Arrays.asList("&3Aktualne sloty: &e"   + ekwipunek.size(),					"&3Nast�pny poziom: &e" + (ekwipunek.size() + 1 < 12 ? ekwipunek.size() : "&6MAX"), strCenaEkwipunek()));
		ustawItem(inv, 15, Material.PLAYER_HEAD,  "&2Pr�dko�� Pracy", 	Arrays.asList("&3Aktualna pr�dko��: &e" + tickWSekundy(staty.get(0).akt),	"&3Nast�pny poziom: &e" + tickWSekundy(staty.get(0).nst), 	staty.get(0).strCena()), "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjVhODRlNjM5NGJhZjhiZDc5NWZlNzQ3ZWZjNTgyY2RlOTQxNGZjY2YyZjFjODYwOGYxYmUxOGMwZTA3OTEzOCJ9fX0=");
		ustawItem(inv, 21, Material.WATER_BUCKET, "&2Zu�ycie Wody", 	Arrays.asList("&3Aktualne zu�ycie: &e" + staty.get(1).str(), 	  			"&3Nast�pny poziom: &e" + staty.get(1).str2(), 				staty.get(1).strCena()));
		ustawItem(inv, 23, Material.BAKED_POTATO, "&2Zu�ycie Jedzenia", Arrays.asList("&3Aktualne zu�ycie: &e" +  staty.get(2).str(),				"&3Nast�pny poziom: &e" + staty.get(2).str2(), 				staty.get(2).strCena()));
		ustawItem(inv, 13, Material.FIREWORK_ROCKET,"&2Podnoszenie Item�w", Func.BooleanToString(podnoszenie, Arrays.asList("&aZakupione") , Arrays.asList("&cNie zakupione", "&3Cena: &e" + Func.IntToString(ulepszaniePodnoszenieItem�wCena) + "$")));
	}
	protected void ustawItem(Inventory inv, int slot, Material mat, String nazwa, List<String> lore, String url) {
		inv.setItem(slot, Func.dajG��wk�(nazwa, url, lore));
	}
	protected void ustawItem(Inventory inv, int slot, Material mat, String nazwa, List<String> lore) {
		inv.setItem(slot, Func.stw�rzItem(mat, 1, nazwa, lore));
	}
	protected double tickWSekundy(double ticki) {
		return ((int) ticki) / 20   +   (ticki % 20) / 20;
	}
	public String strCenaEkwipunek() {
		if (ekwipunek.size() >= 12)
			return "�6Osi�gni�to maksymalny poziom";
		return "�3Koszt ulepszenia: �e " + Func.IntToString(ulepszanieEkwipunkuCena) + "$";
	}
	
	public void otw�rz(Player p) {
		Inventory inv = Bukkit.createInventory(null, 6*9, "�1�lMinion");
		for (int i=0; i<6*9; i+=9)
			inv.setItem(i, menuObw�dka);
		for (int i=3; i<6*9; i+=9)
			inv.setItem(i, menuObw�dka);
		inv.setItem(1,  menuObw�dka);
		inv.setItem(2,  menuObw�dka);
		inv.setItem(46, menuObw�dka);
		inv.setItem(47, menuObw�dka);

		for (int i=4; i<6*9; i+=9)
			inv.setItem(i, ekwipunekObw�dka);
		for (int i=8; i<6*9; i+=9)
			inv.setItem(i, ekwipunekObw�dka);
		for (int i=5; i<8; i++)
			inv.setItem(i, ekwipunekObw�dka);
		for (int i=50; i<54; i++)
			inv.setItem(i, ekwipunekObw�dka);
		
		for (int y=1; y<5; y++)
			for (int x=5; x<8; x++)
				inv.setItem(y*9+x, ekwipunekZablkowany);
		for (int i=0; i<ekwipunek.size(); i++)
			inv.setItem((i/3*9) + i % 3 + 14, ekwipunek.get(i));
		
		
		inv.setItem(10, wodaInfo);
		inv.setItem(19, jedzenieInfo);
		inv.setItem(28, ulepszeniaInfo);
		inv.setItem(37, r�kaInfo);
		
		inv.setItem(11, dajWode());
		inv.setItem(20, dajJedzenie());
		inv.setItem(29, ulepszenia);
		inv.setItem(38, narz�dzie);
		
		p.openInventory(inv);
		Miniony.otwarte.put(p.getName(), id);
	}
	public void zamknij(Player p, Inventory inv, boolean menu) {
		Miniony.otwarte.remove(p.getName());
		if (menu) {
			ustawNarz�dzie(inv.getItem(38));
			for (int i=0; i<ekwipunek.size(); i++)
				ekwipunek.set(i, inv.getItem((i/3*9) + i % 3 + 14));
		}
	}
	protected ItemStack dajJedzenie() { 
		ItemStack item = jedzenie.clone();
		ItemMeta meta = item.getItemMeta();
		List<String> lore = meta.getLore();
		lore.set(0, "�3Zapas:�e " + Func.DoubleToString(stanJedzenia));
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}
	protected ItemStack dajWode() {
		ItemStack item = woda.clone();
		ItemMeta meta = item.getItemMeta();
		List<String> lore = meta.getLore();
		lore.set(0, "�3Aktualny stan:�e " + Func.IntToString(stanWody) + "�3mb");
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}
	
	@SuppressWarnings("deprecation")
	protected void wybierzGracza(Player p, List<String> gracze, String nazwa) {
		if (gracze.size() == 0) {
			p.sendMessage(prefix + "�aden gracz nie spe�nia wymaga�");
			return;
		}
		MenuStronne menu = new MenuStronne(5, nazwa);
		SkullMeta Cmeta;
		ItemStack item;
		for (String gracz : gracze) {
			item = new ItemStack(Material.PLAYER_HEAD);
			Cmeta = (SkullMeta) item.getItemMeta();
			Cmeta.setOwner(gracz);
			Cmeta.setDisplayName("�1" + gracz);
			item.setItemMeta(Cmeta);
			menu.itemy.add(item);
		}
		p.openInventory(menu.inv);
		
		mapaMenuStronne.put(p.getName(), menu);
		Miniony.otwarte.put(p.getName(), id);
	}
	protected void ustawNarz�dzie(ItemStack item) {
		narz�dzie = item;
		((LivingEntity) getBukkitEntity()).getEquipment().setItemInMainHand(narz�dzie);
	}
	
	protected abstract void dajItem(Player p);
	protected abstract void _dajItem(List<String> lore);
	protected void dajItem(Player p, ItemStack item) {
		ItemStack _item = item.clone();
		ItemMeta meta = _item.getItemMeta();
		List<String> lore = meta.getLore();
		lore.set(2, imie);
		lore.add("Dodani Gracze: " + Func.listToString(gracze, 0, ", "));
		lore.add("Podnoszenie: " + zakup(podnoszenie));		
		lore.add("woda: " + stanWody);
		lore.add("jedzenie: " + stanJedzenia);
		
		for (Statystyka stat : staty)
			lore.add(stat.nazwa + ": " + stat.akt);

		_dajItem(lore);
		
		meta.setLore(lore);
		_item.setItemMeta(meta);
		p.getInventory().addItem(_item);
	}
	protected String zakup(boolean b) {
		return b ? "�aZakupione" : "�cNie zakupione";
	}
	protected void usu�(Player p) {
		p.closeInventory();
		if (p.getInventory().firstEmpty() == -1) {
			p.sendMessage(prefix + "Tw�j ekwipunek jest pe�ny");
			return;
		}
		getBukkitEntity().remove();
		
		dajItem(p);
		
		Location loc = this.getBukkitEntity().getLocation();
		
		dajItem(p, narz�dzie, loc);
		for (ItemStack item : ekwipunek)
			dajItem(p, item, loc);
		
		File f = new File("plugins/mimiRPG/miniony/" + id + ".yml");
		if (f.exists())
			f.delete();
		mapa.remove(id);
		
		p.sendMessage(prefix + "Minion zosta� usuni�ty, mo�esz go ponownie zrespi� u�ywaj�c otrzymanego itemu");
	}
	private void dajItem(Player p, ItemStack item, Location loc) {
		if (item == null) return;
		if (p.getInventory().firstEmpty() == -1) loc.getWorld().dropItem(loc, item);
		else 									 p.getInventory().addItem(item);
	}
	
	public boolean klikni�cie(Player p, InventoryClickEvent ev) {
		int slot = ev.getRawSlot();

		Inventory inv = ev.getInventory();
		if (slot >= inv.getSize() || slot < 0)
			return false;

		Player p2;
		String nick;
		switch (ev.getView().getTitle()) {
		case "�4�lUlepszenia":
			if (slot >= 4*9 || slot < 0) return false;
			List<String> lista;
			switch(ev.getCurrentItem().getItemMeta().getDisplayName()) {
			case "�2Pojemno��":
				if (ekwipunek.size() < 12) {
					if (Main.econ.getBalance(p) >= ulepszanieEkwipunkuCena) {
						Main.econ.withdrawPlayer(p, ulepszanieEkwipunkuCena);
						ekwipunek.add(new ItemStack(Material.AIR));
						ulepszeniaOd�wie�(inv);
					} else
						p.sendMessage(prefix + "Nie sta� ci� na to");
				}
				break;
			case "�2Pr�dko�� Pracy":
				if (staty.get(0).ulepsz(p))
					ulepszeniaOd�wie�(inv);
				break;
			case "�2Zu�ycie Wody":
				if (staty.get(1).ulepsz(p))
					ulepszeniaOd�wie�(inv);
				break;
			case "�2Zu�ycie Jedzenia":
				if (staty.get(2).ulepsz(p))
					ulepszeniaOd�wie�(inv);
				break;
			case "�9Zabierz":
				usu�(p);
				break;
			case "�2Podnoszenie Item�w":
				if (podnoszenie) return true;
				if (Main.econ.getBalance(p) >= ulepszaniePodnoszenieItem�wCena) {
					podnoszenie = true;
					ulepszeniaOd�wie�(inv);
				} else
					p.sendMessage(prefix + "Nie sta� ci� na to");
				break;
			case "�aDodaj Gracza":
				lista = Lists.newArrayList();
				for (Player gracz : Bukkit.getOnlinePlayers()) {
					nick = gracz.getName();
					if (!(gracze.contains(nick)))
						lista.add(nick);
				}
				wybierzGracza(p, lista, "�aDodaj Gracza do Miniona");
				break;
			case "�cUsu� Gracza":
				lista = Lists.newArrayList();
				for (String gracz : gracze)
					if (!gracz.equals(stworzyciel))
						lista.add(gracz);
				wybierzGracza(p, lista, "�cUsu� Gracza z Miniona");
				break;
			}
			return true;
		case "�aDodaj Gracza do Miniona":
			if (!ev.getCurrentItem().getType().equals(Material.PLAYER_HEAD)) return true;
			if (slot >= inv.getSize()-9) {
				switch(slot % 9) {
				case 0:
					mapaMenuStronne.get(p.getName()).poprzedniaStrona();
					return true;
				case 8:
					mapaMenuStronne.get(p.getName()).nast�pnaStrona();
					return true;
				}
				return true;
			}
			nick = ev.getCurrentItem().getItemMeta().getDisplayName().substring(2);
			p2 = Bukkit.getPlayer(nick);
			if (p2 == null) return Main.powiadom(p, prefix + "Wskazany gracz niedawno wszed� w tryb offline");
			gracze.add(nick);
			p2.sendMessage(prefix + "�e" + p.getName() + "�6 doda� ci� do jednego ze swoich minion�w");
			p.sendMessage(prefix + "Doda�e� gracza �e" + nick + "�6 do tego miniona");
			p.closeInventory();
			return true;
		case "�cUsu� Gracza z Miniona":
			if (!ev.getCurrentItem().getType().equals(Material.PLAYER_HEAD)) return true;
			if (slot >= inv.getSize()-9) {
				switch(slot % 9) {
				case 0:
					mapaMenuStronne.get(p.getName()).poprzedniaStrona();
					return true;
				case 8:
					mapaMenuStronne.get(p.getName()).nast�pnaStrona();
					return true;
				}
				return true;
			}
			nick = ev.getCurrentItem().getItemMeta().getDisplayName().substring(2);
			p2 = Bukkit.getPlayer(nick);
			gracze.remove(nick);
			if (p.getName().equals(nick)) {
				p.sendMessage(prefix + "Usuno�e� si� z jednego z Minion�w gracza �e" + stworzyciel);
				Player s = Bukkit.getPlayer(stworzyciel);
				if (s != null && s.isOnline())
					s.sendMessage(prefix + "�e" + p.getName() + "�6 usun� si� z jednego z twoich minion�w");
			}
			else {
				p.sendMessage(prefix + "Usuni�to gracza �e" + nick + "�6 z tego miniona");
				if (p2 != null && p2.isOnline())
					p2.sendMessage(prefix + "�e" + p.getName() + "�6 usun�� ci� do jednego ze swoich minion�w");
			}
			p.closeInventory();
			return true;
		}
		if (slot == 29) {
			ulepszenia(p);
			return true;
		}
		if (slot == 38) return false;
		slot -= 14;
		if (slot % 9 > 2) return true;
		slot = slot/9*3 + slot % 9;
		if (slot < ekwipunek.size() && slot >= 0)
			return false;
		return true;		
	}
	
	public void klikni�ty(Player p) {
		if (!p.hasPermission("mimiRPG.minion.bypass"))
			if (!gracze.contains(p.getName()))
				return;
		if (p.isSneaking())
			otw�rz(p);
		else {
			nawodnij(p);
			nakarm(p);
		}
	}
	protected void nawodnij(Player p) {
		if (stanWody >= 50000) return;
		ItemStack item = p.getInventory().getItemInMainHand();
		if (item.getType().equals(Material.WATER_BUCKET)) {
			stanWody += 1000;
			p.sendMessage(prefix + "Nawodniono, Aktualny stan wody: �e" + stanWody);
			p.getInventory().setItemInMainHand(new ItemStack(Material.BUCKET, item.getAmount()));
		}
	}
	protected void nakarm(Player p) {
		if (stanJedzenia >= 2000) return;
		ItemStack item = p.getInventory().getItemInMainHand();
		if (mapaJedzenia.containsKey(item.getType())) {
			stanJedzenia += item.getAmount() * mapaJedzenia.get(item.getType());
			p.sendMessage(prefix + "Nakarmiono, Aktualny zapas: �e" + stanJedzenia);
			p.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
		}
	}

	protected void podnie�Itemy(Location loc) {
		if (!podnoszenie) return;
		Entity b = loc.getWorld().spawnEntity(loc, EntityType.BAT);
		b.setSilent(true);
		for (Entity e :b.getNearbyEntities(1.2, 1.2, 1.2))
			if (e instanceof CraftItem)
				podnie�Item((CraftItem) e);
		b.remove();
	}
	protected void podnie�Itemy(double x, double y, double z) {
		if (!podnoszenie) return;
		for (Entity e : getBukkitEntity().getNearbyEntities(x, y, z))
			if (e instanceof CraftItem)
				podnie�Item((CraftItem) e);
	}
	public static boolean por�wnaj(ItemStack item1, ItemStack item2) {
		ItemStack item1c = item1.clone();
		ItemStack item2c = item2.clone();
		item1c.setAmount(1);
		item2c.setAmount(1);
		return item1c.equals(item2c);
	}
	protected void podnie�Item(CraftItem itemByt) {
		ItemStack item = itemByt.getItemStack();
		for (int i=0; i<ekwipunek.size(); i++) {
			ItemStack eqItem = ekwipunek.get(i);
			if (eqItem == null) continue;
			if (por�wnaj(eqItem, item)) {
				int ile = Math.min(eqItem.getMaxStackSize() - eqItem.getAmount(), item.getAmount());
				if (ile == 0) continue;
				item.setAmount(item.getAmount() - ile);
				eqItem.setAmount(eqItem.getAmount() + ile);
				itemByt.remove();
				if (item.getAmount() <= 0) return;
				itemByt.getWorld().dropItem(itemByt.getLocation(), item);
				return;
			}
		}
		for (int i=0; i<ekwipunek.size(); i++) {
			if (ekwipunek.get(i) == null) {
				ekwipunek.set(i, item);
				itemByt.remove();
				return;
			}
		}
	}
}
