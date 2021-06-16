package me.jomi.mimiRPG.RPG;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import me.jomi.mimiRPG.Baza;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.PojedynczeKomendy.Sklep;
import me.jomi.mimiRPG.PojedynczeKomendy.Targ;
import me.jomi.mimiRPG.RPG.GraczRPG.KolekcjaGracz;
import me.jomi.mimiRPG.RPG.GraczRPG.ŚcieżkaDoświadczeniaGracz;
import me.jomi.mimiRPG.SkyBlock.SkyBlock;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.ItemCreator;
import me.jomi.mimiRPG.util.Panel;

@Moduł
public class PanelRPG implements Listener {
	public static final ItemStack itemPanelu = Func.stwórzItem(Material.NETHER_STAR, "§bMenu");

	//static final ItemStack itemProfil = Func.stwórzItem(Material.PLAYER_HEAD, "&aTwój Profil", "&4Dostępne Niebawem");
	static final ItemStack itemUmiejętności = Func.stwórzItem(Material.IRON_SWORD, "&aUmiejętności");
	static final ItemStack itemKolekcje = Func.stwórzItem(Material.PAINTING, "&aKolekcje");
	static final ItemStack itemKsięgaReceptór = Func.stwórzItem(Material.BOOK, "&aKsięga receptór", "&4Dostępne niebawem!");
	static final ItemStack itemSklep = Func.stwórzItem(Material.DIAMOND, "&aSklep");
	static final ItemStack itemMagazyn = Func.stwórzItem(Material.CHEST, "&aMagazyn", "&4Dostępne niebawem!");
	static final ItemStack itemCrafting = Func.stwórzItem(Material.CRAFTING_TABLE, "&aCrafting");
	static final ItemStack itemSzafa = Func.stwórzItem(Material.LEATHER_CHESTPLATE, "&aSzafa", "&4Dostępne niebawem!");
	static final ItemStack itemWarpy = Func.stwórzItem(Material.ENDER_EYE, "&aWarpy", "&4Dostępne niebawem!");
	static final ItemStack itemTarg = Func.stwórzItem(Material.EMERALD, "&aTarg");
	static final ItemStack itemSkyblock = Func.stwórzItem(Material.GRASS_BLOCK, "&aSkyblock");
	static final ItemStack itemBestie = Func.stwórzItem(Material.TOTEM_OF_UNDYING, "&aBestie");
	
	static final int slotProfil = 13;
	static final int slotUmiejętności = 10;
	static final int slotKolekcje = 19;
	static final int slotKsięgaReceptór = 20;
	static final int slotSklep = 16;
	static final int slotMagazyn = 24;
	static final int slotCrafting = 29;
	static final int slotSzafa = 33;
	static final int slotWarpy = 40;
	static final int slotTarg = 25;
	static final int slotSkyblock = 31;
	static final int slotBestie = 44;
	
	static final Panel panel = new Panel(true);
	static final Panel panelKolekcji = new Panel(true);
	static final Panel panelUmiejętności = new Panel(true);
	
	public PanelRPG() {
		panel.ustawClick(this::panelClick);
		panelKolekcji.ustawClick(this::panelKolekcjiClick);
	}
	private void panelClick(InventoryClickEvent ev) {
		switch (ev.getRawSlot()) {
		case slotProfil:
			// TODO profil, niebawem
			break;
		case slotUmiejętności:
			otwórzPanelUmiejętności((Player) ev.getWhoClicked());
			break;
		case slotKolekcje:
			otwórzPanelKolekcji((Player) ev.getWhoClicked());
			break;
		case slotKsięgaReceptór:
			// TODO księga receptór, niebawem
			break;
		case slotSklep:
			ev.getWhoClicked().closeInventory();
			((Player) ev.getWhoClicked()).chat("/sklep");
			break;
		case slotMagazyn:
			// TODO magazyn, niebawem
			break;
		case slotCrafting:
			ev.getWhoClicked().openWorkbench(ev.getWhoClicked().getLocation(), true);
			break;
		case slotSzafa:
			// TODO szada, niebawem
			break;
		case slotWarpy:
			// TODO warpy, niebawem
			break;
		case slotTarg:
			ev.getWhoClicked().closeInventory();
			((Player) ev.getWhoClicked()).chat("/targ");
			break;
		case slotSkyblock:
			ev.getWhoClicked().closeInventory();
			((Player) ev.getWhoClicked()).chat("/is");
			break;
		case slotBestie:
			ev.getWhoClicked().closeInventory();
			((Player) ev.getWhoClicked()).chat("/bestie");
			break;
		}
	}
	private void panelKolekcjiClick(InventoryClickEvent ev) {
		KolekcjaKategoria kat;
		try {
			NamespacedKey klucz = new NamespacedKey(Main.plugin, "kolekcja_kategoria");
			String name = ev.getCurrentItem().getItemMeta().getPersistentDataContainer().get(klucz, PersistentDataType.STRING);
			kat = Func.StringToEnum(KolekcjaKategoria.class, name);
		} catch (Throwable e) {
			return;
		}
		
		otwórzPanelKolekcji((Player) ev.getWhoClicked(), kat);
	}
	
	
	/// EventHandler
	
	@EventHandler(priority = EventPriority.LOW)
	public void klikanieEq(InventoryClickEvent ev) {
		if (ev.getSlot() == 8 && ev.getClickedInventory() instanceof PlayerInventory)
			ev.setCancelled(true);
		else if (Func.multiEquals(ev.getAction(), InventoryAction.HOTBAR_MOVE_AND_READD, InventoryAction.HOTBAR_SWAP) && ev.getHotbarButton() == 8)
			ev.setCancelled(true);
	}
	@EventHandler(priority = EventPriority.LOW)
	public void wyrzucanie(PlayerDropItemEvent ev) {
		if (jestItememMenu(ev.getItemDrop().getItemStack()))
			ev.setCancelled(true);
	}

	@EventHandler
	public void join(PlayerJoinEvent ev) {
		ev.getPlayer().getInventory().setItem(8, itemPanelu);
	}

	@EventHandler
	public void interakcja(PlayerInteractEvent ev) {
		if (ev.getHand() != EquipmentSlot.HAND) return;
		if (ev.getPlayer().getInventory().getHeldItemSlot() != 8) return;
		if (!Func.multiEquals(ev.getAction(), Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK)) return;
		
		otwórzPanel(ev.getPlayer());
	}
	
	
	/// Panele
	public static void otwórzPanel(Player p) {
		Inventory inv = panel.stwórz(null, 5, "§b§lMenu");
		Func.ustawPuste(inv, Baza.pustySlotCzarny);
		
		inv.setItem(slotProfil,			ItemCreator.nowy(Func.dajGłowe(p)).nazwa("&aTwoje Statystyki").lore("&4Dostępne Niebawem").stwórz());
		inv.setItem(slotUmiejętności,	itemUmiejętności);
		inv.setItem(slotKolekcje,		itemKolekcje);
		inv.setItem(slotKsięgaReceptór,	itemKsięgaReceptór);
		inv.setItem(slotMagazyn,		itemMagazyn);
		inv.setItem(slotSzafa,			itemSzafa);
		inv.setItem(slotWarpy,			itemWarpy);
		if (Main.włączonyModół(Targ.class))			inv.setItem(slotTarg,		itemTarg);
		if (Main.włączonyModół(Sklep.class))		inv.setItem(slotSklep,		itemSklep);
		if (Main.włączonyModół(Bestie.class))		inv.setItem(slotBestie, 	itemBestie);
		if (Main.włączonyModół(SkyBlock.class))		inv.setItem(slotSkyblock,	itemSkyblock);
		if (Main.włączonyModół(CraftingiRPG.class))	inv.setItem(slotCrafting,	itemCrafting);
		
		p.openInventory(inv);
	}

	public static void otwórzPanelUmiejętności(Player p) {
		Inventory inv = panelUmiejętności.stwórz(null, 4, "§cUmiejętności", Baza.pustySlotCzarny);
		GraczRPG gracz = GraczRPG.gracz(p);
		
		Func.forEach(ŚcieżkaDoświadczenia.values(), ścieżka -> {
			ŚcieżkaDoświadczeniaGracz ścieżkaGracza = gracz.ścieżka(ścieżka);
			
			ItemStack item = Func.stwórzItem(ścieżka.ikona, "§b" + ścieżka.nazwa);
			List<String> lore = new ArrayList<>();
			lore.addAll(Func.tnij(ścieżka.opis, "\n"));
			lore.add(" ");
			lore.add("§6Poziom§8: §a" + ścieżkaGracza.getLvl());
			if (ścieżkaGracza.getExp() != -1) {
				lore.add(Func.progres(ścieżkaGracza.getExp(), ścieżkaGracza.getPotrzebnyExp(), 20, "-", "§2", "§7") +
						" §8(§7" + Func.zaokrąglij(ścieżkaGracza.getExp() / (double) ścieżkaGracza.getPotrzebnyExp() * 100, 1)  + "%§8)");
				lore.add("§7" + Func.IntToString(ścieżkaGracza.getExp()) + "§3 / §7" + Func.IntToString(ścieżkaGracza.getPotrzebnyExp()) + " expa");
			} else {
				lore.add("§7Lvl §4§l§oMAX");
			}
			lore.add(" ");
			
			Func.ustawLore(item, lore);
			inv.setItem(ścieżka.slotWPanelu, item);
		});
		
		p.openInventory(inv);
	}
	public static void otwórzPanelKolekcji(Player p) {
		Inventory inv = panelKolekcji.stwórz(null, 4, "§cKolekcje", Baza.pustySlotCzarny);
		
		Func.forEach(KolekcjaKategoria.values(), kat -> {
			ItemStack item = Func.stwórzItem(kat.ikona, "§b" + kat.nazwa, Func.tnij(kat.opis, "\n"));
			ItemMeta meta = item.getItemMeta();
			
			meta.getPersistentDataContainer().set(new NamespacedKey(Main.plugin, "kolekcja_kategoria"), PersistentDataType.STRING, kat.name());
			
			item.setItemMeta(meta);
			inv.setItem(kat.slotWPanelu, item);
		});
		
		p.openInventory(inv);
	}
	public static void otwórzPanelKolekcji(Player p, KolekcjaKategoria kat) {
		Inventory inv = Panel.prostyPanel.stwórz(null, 5, "§cKolekcja §4" + kat.nazwa, Baza.pustySlotCzarny);
		GraczRPG gracz = GraczRPG.gracz(p);
		
		int[] sloty = Func.sloty(kat.kolekcje.size(), 3);
		for (int i = 0; i < kat.kolekcje.size(); i++) {
			Kolekcja kolekcja = kat.kolekcje.get(i);
			KolekcjaGracz kolekcjaGracza = gracz.getKolekcja(kolekcja);
			
			ItemStack item = Func.stwórzItem(Material.GRAY_DYE, "§c" + kolekcja.nazwa);
			List<String> lore = new ArrayList<>();
			
			if (kolekcjaGracza.odblokowana()) {
				Func.typ(item, kolekcja.mat);
				lore.addAll(Func.tnij(kolekcja.opis, "\n"));
				lore.add(" ");

				lore.add("§6Poziom§8: §a" + kolekcjaGracza.getLvl());
				if (kolekcjaGracza.getExp() != -1) {
					lore.add(Func.progres(kolekcjaGracza.getExp(), kolekcjaGracza.getPotrzebnyExp(), 20, "-", "§2", "§7") +
							" §8(§7" + Func.zaokrąglij(kolekcjaGracza.getExp() / (double) kolekcjaGracza.getPotrzebnyExp() * 100, 1)  + "%§8)");
					lore.add("§7" + Func.IntToString(kolekcjaGracza.getExp()) + "§3 / §7" + Func.IntToString(kolekcjaGracza.getPotrzebnyExp()) + " expa");
				} else {
					lore.add("§7Lvl §4§l§oMAX");
				}
				lore.add(" ");
			} else
				lore.add("§7???");
			
			Func.ustawLore(item, lore);
			inv.setItem(sloty[i] + 9, item);
		}
		
		p.openInventory(inv);
	}
	
	
	
	/// util
	public static boolean jestItememMenu(ItemStack item) {
		if (item == null)
			return false;
		return item.isSimilar(itemPanelu);
	}
}
