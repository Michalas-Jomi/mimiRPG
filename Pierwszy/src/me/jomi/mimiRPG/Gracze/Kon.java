package me.jomi.mimiRPG.Gracze;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.PojedynczeKomendy.Koniki;

public class Kon {
	public Inventory inv;
	private static final int slotyEq = 3*9;
	private static final int slotMały = 4*9 + 1;
	private static final int slotBezgłośny = 4*9 + 3;
	private static final int slotKolor = 4*9 + 5;
	private static final int slotStyl = 4*9 + 7;
	
	private static final ItemStack itemMały = Func.stwórzItem(Material.EGG, "§6Rozmiar", "§eMały");
	private static final ItemStack itemDuży = Func.stwórzItem(Material.EGG, "§6Rozmiar", "§eDuży");
	
	private static final ItemStack itemBezgłośny 	= Func.stwórzItem(Material.BONE, "§6Dzwięk", "§6Bezgłośny");
	private static final ItemStack itemNieBezgłośny = Func.stwórzItem(Material.CREEPER_HEAD, "§6Dzwięk", "§6Normalny");
	
	
	public boolean bezgłośny;
	public boolean mały;
	public Kolor kolor;
	public Gracz gracz;
	public Styl styl;
	public Player p;
	
	private int nrKolor = -1;
	private int nrStyl = -1;
	
	private static final Kolor[] kolory = Kolor.values();
	private static final Styl[] style = Styl.values();
	public Kon(Gracz gracz, boolean bezgłośny, boolean mały, String kolor, String styl, int zapas) {
		this.kolor = Kolor.valueOf(kolor);
		this.styl = Styl.valueOf(styl);
		this.bezgłośny = bezgłośny;
		this.gracz = gracz;
		this.zapas = zapas;
		this.mały = mały;
		this.p = gracz.p;
		
		nrKolor = znajdz(kolory, this.kolor);
		nrStyl = znajdz(style, this.styl);
		
		stwórzInv();
	}
	private int znajdz(Object[] objekty, Object obj) {
		for (int i=0; i< objekty.length; i++)
			if (objekty[i].equals(obj))
				return i;
		return -1;
	}
	
	private Horse kon;
	public void przywołaj() {
		if (kon != null) kon.remove();
		kon = (Horse) p.getWorld().spawnEntity(p.getLocation(), EntityType.HORSE);
		
		kon.setOwner(p);
		kon.setTamed(true);
		kon.setAgeLock(true);
		kon.setInvulnerable(true);
		kon.setRemoveWhenFarAway(true);
		
		kon.getInventory().setSaddle(new ItemStack(Material.SADDLE));
		
		kon.setMetadata("mimiKon", new FixedMetadataValue(Main.plugin, p.getName()));
		
		kon.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
		kon.setHealth(20);

		p.sendMessage(Koniki.prefix + Func.msg("Przywołałeś swojego Konika, możesz jezdzić na nim jeszcze %s bez karmienia", Koniki.czas(gracz.koń.zapas)));
		//EntityHorse e = ((EntityHorse)((CraftEntity) kon).getHandle());
		//e.goalSelector = new PathfinderGoalSelector(e.getWorld().getMethodProfilerSupplier());
		
		ustawMały();
		ustawStyl();
		ustawKolor();
		ustawBezgłośny();
	}
	
	public void usuń() {
		if (kon != null)
			kon.remove();
	}
	
	public int zapas;
	public void nakarm(int ile) {
		if (!nakarmiony())
			zapas = (int) (System.currentTimeMillis() / 1000);
		zapas += ile;
		gracz.config.ustaw_zapisz("koń.zapas", zapas);
	}
	public boolean nakarmiony() {
		return System.currentTimeMillis() / 1000 <= zapas;
	}
	public void sprawdz() {
		if (kon != null && !kon.getPassengers().isEmpty()) {
			if (nakarmiony()) return;
			kon.removePassenger(p);
			p.sendMessage(Koniki.prefix + "Twój Konik jest głodny, nie jest w stanie cie dalej wozić");
		}
	}
	
	private void stwórzInv() {
		inv = Bukkit.createInventory(p, 5*9, "§1§lTwój Konik");
		ConfigurationSection sekcja = gracz.config.sekcja("koń", "itemy");
		if (sekcja != null)
			for (String slot : sekcja.getKeys(false)) {
				int i = Func.Int(slot, -1);
				if (i <= -1 || i >= slotyEq) continue;
				inv.setItem(i, gracz.config.wczytajItem("koń", "itemy", i));
			}
		ItemStack nic = Func.stwórzItem(Material.BLACK_STAINED_GLASS_PANE, "§1§l§o §6§o");
		for (int i=slotyEq; i < 5*9; i++)
			inv.setItem(i, nic);
		Func.nazwij(inv.getItem(slotKolor), "§6Kolor");
		Func.nazwij(inv.getItem(slotStyl),  "§6Styl");
		ustawMały();
		ustawStyl();
		ustawKolor();
		ustawBezgłośny();
	}
	private void ustawMały() {
		inv.setItem(slotMały, mały ? itemMały : itemDuży);
		if (kon != null)
			kon.setAge(mały ? -1 : 0);
	}
	private void ustawBezgłośny() {
		inv.setItem(slotBezgłośny, bezgłośny ? itemBezgłośny : itemNieBezgłośny);
		if (kon != null)
			kon.setSilent(bezgłośny);
	}
	private void ustawKolor() {
		ItemStack item = inv.getItem(slotKolor);
		kolor = kolory[nrKolor];
		item.setType(kolor.mat);
		Func.ustawLore(item, "§e" + kolor, 0);
		if (kon != null)
			kon.setColor(kolor.kolor);
	}
	private void ustawStyl() {
		ItemStack item = inv.getItem(slotStyl);
		styl = style[nrStyl];
		item.setType(styl.mat);
		Func.ustawLore(item, "§e" + styl, 0);
		if (kon != null)
			kon.setStyle(styl.styl);
	}
	public void kliknięteEq(InventoryClickEvent ev) {
		int slot = ev.getSlot();
		if (slot < slotyEq || slot >= 5*9) return;
		int zmiana = ev.getClick().toString().endsWith("RIGHT") ? -1 : 1;
		switch(slot) {
		case slotMały:
			mały = !mały;
			ustawMały();
			break;
		case slotBezgłośny:
			bezgłośny = !bezgłośny;
			ustawBezgłośny();
			break;
		case slotKolor:
			nrKolor += zmiana;
			if (nrKolor >= kolory.length)
				nrKolor = 0;
			else if (nrKolor < 0)
				nrKolor = kolory.length -1;
			ustawKolor();
			break;
		case slotStyl:
			nrStyl += zmiana;
			if (nrStyl >= style.length)
				nrStyl = 0;
			else if (nrStyl < 0)
				nrStyl = style.length - 1;
			ustawStyl();
			break;
		}
		ev.setCancelled(true);
	}
	
	public void zapisz() {
		gracz.config.ustaw("koń.bezgłośny", bezgłośny);
		gracz.config.ustaw("koń.kolor", kolor.name());
		gracz.config.ustaw("koń.styl", styl.name());
		gracz.config.ustaw("koń.mały", mały);
		gracz.config.ustaw("koń.itemy", null);
		for (int i=0; i<slotyEq; i++) {
			ItemStack item = inv.getItem(i);
			if (item != null && !item.getType().isAir())
				gracz.config.ustaw("koń.itemy."+i, item);
		}
		gracz.config.zapisz();
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
