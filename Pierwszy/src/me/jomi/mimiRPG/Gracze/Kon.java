package me.jomi.mimiRPG.Gracze;

import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.PojedynczeKomendy.Koniki;

public class Kon implements ConfigurationSerializable {
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
	
	@Override
	public Map<String, Object> serialize() {
		return Func.zmapuj(this);
	}
	public Kon(Map<String, Object> mapa) {
		Func.zdemapuj(this, mapa);
		init();
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
