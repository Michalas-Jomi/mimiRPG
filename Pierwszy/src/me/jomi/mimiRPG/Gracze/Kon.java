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
	private static final int slotMa³y = 4*9 + 1;
	private static final int slotBezg³oœny = 4*9 + 3;
	private static final int slotKolor = 4*9 + 5;
	private static final int slotStyl = 4*9 + 7;
	
	private static final ItemStack itemMa³y = Func.stwórzItem(Material.EGG, "§6Rozmiar", "§eMa³y");
	private static final ItemStack itemDu¿y = Func.stwórzItem(Material.EGG, "§6Rozmiar", "§eDu¿y");
	
	private static final ItemStack itemBezg³oœny 	= Func.stwórzItem(Material.BONE, "§6Dzwiêk", "§6Bezg³oœny");
	private static final ItemStack itemNieBezg³oœny = Func.stwórzItem(Material.CREEPER_HEAD, "§6Dzwiêk", "§6Normalny");
	
	
	public boolean bezg³oœny;
	public boolean ma³y;
	public Kolor kolor;
	public Gracz gracz;
	public Styl styl;
	public Player p;
	
	private int nrKolor = -1;
	private int nrStyl = -1;
	
	private static final Kolor[] kolory = Kolor.values();
	private static final Styl[] style = Styl.values();
	public Kon(Gracz gracz, boolean bezg³oœny, boolean ma³y, String kolor, String styl, int zapas) {
		this.kolor = Kolor.valueOf(kolor);
		this.styl = Styl.valueOf(styl);
		this.bezg³oœny = bezg³oœny;
		this.gracz = gracz;
		this.zapas = zapas;
		this.ma³y = ma³y;
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
	public void przywo³aj() {
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

		p.sendMessage(Koniki.prefix + Func.msg("Przywo³a³eœ swojego Konika, mo¿esz jezdziæ na nim jeszcze %s bez karmienia", Koniki.czas(gracz.koñ.zapas)));
		
		ustawMa³y();
		ustawStyl();
		ustawKolor();
		ustawBezg³oœny();
	}
	
	public void usuñ() {
		if (kon != null)
			kon.remove();
	}
	
	public int zapas;
	public void nakarm(int ile) {
		if (!nakarmiony())
			zapas = (int) (System.currentTimeMillis() / 1000);
		zapas += ile;
		gracz.config.ustaw_zapisz("koñ.zapas", zapas);
	}
	public boolean nakarmiony() {
		return System.currentTimeMillis() / 1000 <= zapas;
	}
	public void sprawdz() {
		if (kon != null && !kon.getPassengers().isEmpty()) {
			if (nakarmiony()) return;
			kon.removePassenger(p);
			p.sendMessage(Koniki.prefix + "Twój Konik jest g³odny, nie jest w stanie cie dalej woziæ");
		}
	}
	
	private void stwórzInv() {
		inv = Bukkit.createInventory(p, 5*9, "§1§lTwój Konik");
		ConfigurationSection sekcja = gracz.config.sekcja("koñ", "itemy");
		if (sekcja != null)
			for (String slot : sekcja.getKeys(false)) {
				int i = Func.Int(slot, -1);
				if (i <= -1 || i >= slotyEq) continue;
				inv.setItem(i, gracz.config.wczytajItem("koñ", "itemy", i));
			}
		ItemStack nic = Func.stwórzItem(Material.BLACK_STAINED_GLASS_PANE, "§1§l§o §6§o");
		for (int i=slotyEq; i < 5*9; i++)
			inv.setItem(i, nic);
		Func.nazwij(inv.getItem(slotKolor), "§6Kolor");
		Func.nazwij(inv.getItem(slotStyl),  "§6Styl");
		ustawMa³y();
		ustawStyl();
		ustawKolor();
		ustawBezg³oœny();
	}
	private void ustawMa³y() {
		inv.setItem(slotMa³y, ma³y ? itemMa³y : itemDu¿y);
		if (kon != null)
			kon.setAge(ma³y ? -1 : 0);
	}
	private void ustawBezg³oœny() {
		inv.setItem(slotBezg³oœny, bezg³oœny ? itemBezg³oœny : itemNieBezg³oœny);
		if (kon != null)
			kon.setSilent(bezg³oœny);
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
	public void klikniêteEq(InventoryClickEvent ev) {
		int slot = ev.getSlot();
		if (slot < slotyEq || slot >= 5*9) return;
		int zmiana = ev.getClick().toString().endsWith("RIGHT") ? -1 : 1;
		switch(slot) {
		case slotMa³y:
			ma³y = !ma³y;
			ustawMa³y();
			break;
		case slotBezg³oœny:
			bezg³oœny = !bezg³oœny;
			ustawBezg³oœny();
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
		gracz.config.ustaw("koñ.bezg³oœny", bezg³oœny);
		gracz.config.ustaw("koñ.kolor", kolor.name());
		gracz.config.ustaw("koñ.styl", styl.name());
		gracz.config.ustaw("koñ.ma³y", ma³y);
		gracz.config.ustaw("koñ.itemy", null);
		for (int i=0; i<slotyEq; i++) {
			ItemStack item = inv.getItem(i);
			if (item != null && !item.getType().isAir())
				gracz.config.ustaw("koñ.itemy."+i, item);
		}
		gracz.config.zapisz();
	}
	
	public static enum Styl {
		Brak(Style.NONE, Material.BARRIER),
		Bia³e_Nogi(Style.WHITE, Material.WHITE_DYE),
		Bia³e_Plamki(Style.WHITE_DOTS, Material.WOLF_SPAWN_EGG),
		Bia³e_£atki(Style.WHITEFIELD, Material.WHITE_STAINED_GLASS_PANE),
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
		Bia³y(Color.WHITE, Material.WHITE_WOOL),
		Czarny(Color.BLACK, Material.BLACK_WOOL),
		Szary(Color.GRAY, Material.GRAY_WOOL),
		Jasny_Br¹z(Color.CREAMY, Material.BROWN_CONCRETE),
		Br¹zowy_Mieszany(Color.CHESTNUT, Material.BROWN_CONCRETE_POWDER),
		Br¹zowy(Color.BROWN, Material.BROWN_WOOL),
		Ciemny_Br¹z(Color.DARK_BROWN, Material.BROWN_TERRACOTTA);
		
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
