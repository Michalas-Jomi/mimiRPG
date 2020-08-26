package me.jomi.mimiRPG.PojedynczeKomendy;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.MenuStronne;
import me.jomi.mimiRPG.Prze³adowalny;

public class Menu extends Komenda implements Listener, Prze³adowalny {
	private static JSONObject plik;
	private static HashMap<String, HashMap<Integer, MenuItem>> mapaItemow = new HashMap<>();
	private static HashMap<String, MenuStronne> mapaMenuStronnych = new HashMap<>();
	private static HashMap<String, String> mapaKomend = new HashMap<>();
	private static HashMap<String, MenuInv> mapa = new HashMap<>();
	
	public Menu() {
		super("menu");
	}
	
	private static void dajMenu(Player p, String Menu) {
		if (!mapa.containsKey(Menu))
			wczytajMenu(Menu);
		mapa.get(Menu).otwórz(p);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void wczytajMenu(String Menu) {
		List<MenuItem> itemy = Lists.newArrayList();
		JSONArray menu = (JSONArray) plik.get(Menu);
		
		Iterator<JSONObject> iterator = menu.iterator();
		int rzêdy 	 = (int) (long) iterator.next().get("rzedy");
		
		while (iterator.hasNext()) {
			JSONObject obj = iterator.next();

			int slot = (int) (long) (obj.get("slot"));
			if (obj.containsKey("sloty")) {
				for (MenuItem menuItem : itemy)
					if (slot == menuItem.slot) {
						ListIterator sloty = ((ArrayList) obj.get("sloty")).listIterator();
						while (sloty.hasNext())
							itemy.add(menuItem.kopia((int) (long) sloty.next()));
						break;
					}
				continue;
			}
			
			String id 		= (String) obj.getOrDefault("id", "STONE");
			String nazwa 	= (String) obj.getOrDefault("nazwa", " ");
			String permisja = (String) obj.get("permisja");
			String komenda	= (String) obj.get("komenda");
			String url    	= (String) obj.get("value");
			int ilosc = (int) (long) obj.getOrDefault("ilosc", 1L);
			ItemStack item 	  = new ItemStack(Material.getMaterial(id), ilosc);
			List<String> lore = null;
			if (obj.containsKey("lore")) {
				ListIterator lorejson = ((ArrayList) obj.get("lore")).listIterator();
				lore = Lists.newArrayList();
				while (lorejson.hasNext())
					lore.add(Func.koloruj("" + lorejson.next()));
			}
			
			if (id.equals("PLAYER_HEAD") && obj.containsKey("value")) 
				item = Func.dajG³ówkê(nazwa, url, lore);
			else {
				ItemMeta meta = item.getItemMeta();
				if (nazwa != null)
					meta.setDisplayName(Func.koloruj(nazwa));
				if (lore != null)
					meta.setLore(lore);
				item.setItemMeta(meta);
			}
			MenuItem menuItem = new MenuItem(slot, item, komenda, permisja);
			if (!mapaItemow.containsKey(Menu))
				mapaItemow.put(Menu, new HashMap<>());
			mapaItemow.get(Menu).put(slot, menuItem);
			itemy.add(menuItem);
			
		}
		mapa.put(Menu, new MenuInv(rzêdy*9, itemy, Menu));
	}		
	
	public static String zamienPierwsze(String wyraz, String co, String naCo) {
		String k = "";
		boolean b = true;
		for (String el : wyraz.split(" ")) {
			if (el.equalsIgnoreCase(co) && b) {
				k += naCo;
				b = false;
			}
			else
				k += el;
			k += " ";
		}
		return k.substring(0, k.length()-1);
	}
	
	private void wykonaj(Player p, String komenda) {
		p.closeInventory();
		if (komenda.contains("{gracz}")) {
			mapaKomend.put(p.getName(), komenda);
			wybórGracza(p);
		} else if (komenda.contains("{liczba}")) {
			mapaKomend.put(p.getName(), komenda);
			wybórLiczby(p, "");
		}
		else
			p.chat(komenda);
	}
	
	ItemStack[] numery = {	Func.dajG³ówkê("&41", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGQyNDU0ZTRjNjdiMzIzZDViZTk1M2I1YjNkNTQxNzRhYTI3MTQ2MDM3NGVlMjg0MTBjNWFlYWUyYzExZjUifX19", null),
							Func.dajG³ówkê("&42", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjEzYjc3OGM2ZTUxMjgwMjQyMTRmODU5YjRmYWRjNzczOGM3YmUzNjdlZTRiOWI4ZGJhZDc5NTRjZmYzYSJ9fX0=", null),
							Func.dajG³ówkê("&43", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMDMxZjY2YmUwOTUwNTg4NTk4ZmVlZWE3ZTZjNjc3OTM1NWU1N2NjNmRlOGI5MWE0NDM5MWIyZTlmZDcyIn19fQ==", null),
							Func.dajG³ówkê("&44", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTViYzQyYzY5ODQ2YzNkYTk1MzFhYzdkYmEyYjU1MzYzZjhmOTQ3MjU3NmUxN2Q0MjNiN2E5YjgxYzkxNTEifX19", null),
							Func.dajG³ówkê("&45", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGYzZjU2NWE4ODkyOGVlNWE5ZDY4NDNkOTgyZDc4ZWFlNmI0MWQ5MDc3ZjJhMWU1MjZhZjg2N2Q3OGZiIn19fQ==", null),
							Func.dajG³ówkê("&46", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzVkYTFjYjZjNGMyMzcxMDIyNGI0ZjRlOGQ2ZmZjZjhiNGI1NWY3ZmU4OTFjMTIwNGFmNzQ4NWNmMjUyYTFkOCJ9fX0=", null),
							Func.dajG³ówkê("&47", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWY0ZTdhNWNmNWI1YTRkMmZmNGZiMDQzM2IxYTY4NzUxYWExMmU5YTAyMWQzOTE4ZTkyZTIxOWE5NTNiIn19fQ==", null),
							Func.dajG³ówkê("&48", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTY4MzQ0MGM2NDQ3YzE5NWFhZjc2NGUyN2ExMjU5MjE5ZTkxYzZkOGFiNmJkODlhMTFjYThkMmNjNzk5ZmE4In19fQ==", null),
							Func.dajG³ówkê("&49", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjg5NzdhZGVkZmE2YzgxYTY3ZjgyNWVhMzdjNGQ1YWE5MGRmZTNjMmE3MmRkOTg3OTFmNDUyMWUxZGEzNiJ9fX0=", null),
							Func.dajG³ówkê("&4Anuluj", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzRkZmQ5NjYzYzNlZTRkMzQxZDk3ZmFhYzVmYTcwZDIxM2VhZmIxNDgxM2FmMjBkNGE0MjUzNDIyNDIxNDJmIn19fQ==", null),
							Func.dajG³ówkê("&40", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODViZDFlNjEzZmYzMmI1MjNjY2Y5ZTU3NGNjMzExYjc5OGMyYjNhNjgyOGYwZjcxYTI1NGM5OTVlNmRiOGU1In19fQ==", null),
							Func.dajG³ówkê("&4Zatwierdz", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmY5ZDlkZTYyZWNhZTliNzk4NTU1ZmQyM2U4Y2EzNWUyNjA1MjkxOTM5YzE4NjJmZTc5MDY2Njk4Yzk1MDhhNyJ9fX0=", null)};
	ItemStack zablokowany = Func.stwórzItem(Material.GRAY_STAINED_GLASS_PANE, 1, "&cPodaj Liczbê", null);
	private void wybórLiczby(Player p, String liczba) {
		//Inventory inv = Bukkit.createInventory(p, InventoryType.ANVIL, "Wartoœæ");
		Inventory inv = Bukkit.createInventory(p, 36, "§2§l§oLiczba:§4§l " + liczba);
		int nr = 0;
		for (int y=0; y<36; y+=9)
			for (int x=3; x<6; x++)
				inv.setItem(y+x, numery[nr++]);
		
		for (int i=0; i<36; i++)
			if (inv.getItem(i) == null)
				inv.setItem(i, zablokowany);
		p.openInventory(inv);
	}
	@SuppressWarnings("deprecation")
	private void wybórGracza(Player p) {
		MenuStronne menu = new MenuStronne(4, "§e§lWybierz Gracza");
		SkullMeta Cmeta;
		ItemStack item;
		for (Player gracz : Bukkit.getOnlinePlayers()) {
			if (p.equals(gracz)) continue;
			item = new ItemStack(Material.PLAYER_HEAD);
			Cmeta = (SkullMeta) item.getItemMeta();
			Cmeta.setOwner(gracz.getName());
			Cmeta.setDisplayName(ChatColor.BLUE + gracz.getName());
			item.setItemMeta(Cmeta);
			menu.itemy.add(item);
		}
		menu.odœwie¿();
		mapaMenuStronnych.put(p.getName(), menu);
		p.openInventory(menu.inv);
	}

	boolean poprawne = false;
	public void prze³aduj() {
		JSONParser parser = new JSONParser();
		mapa.clear();
		mapaItemow.clear();
		try {
			InputStreamReader in = new InputStreamReader(new FileInputStream("plugins/mimiRPG/menu.json"), "UTF-8");
			plik = (JSONObject) parser.parse(new BufferedReader(in));
			poprawne = true;
		} catch (IOException | ParseException e) {
			Bukkit.getLogger().warning("Nie poprawny plik plugins/mimiRPG/menu.json");
			poprawne = false;
		}
	}
	public String raport() {
		return "§6menu.json: " + (poprawne ? "§aPoprawny" : "§cNie poprawny");
	}

	@EventHandler
	public void otwarcieMenu(PlayerInteractEvent ev) {
		if (ev.getAction().toString().startsWith("LEFT")) return;
		ItemStack item = ev.getItem();
		if (item == null) return;
		if (item.getItemMeta().getDisplayName().equalsIgnoreCase("§2Menu")) {
			ev.getPlayer().chat("/menu");
			ev.setCancelled(true);
		}
	}
	@EventHandler
	public void klikniêcie(InventoryClickEvent ev) {
		int slot = ev.getRawSlot();
		if (slot >= ev.getInventory().getSize() || slot < 0) return;
		Player p = (Player) ev.getWhoClicked();
		String tytu³ = ev.getView().getTitle();
		String[] tytu³Splited = tytu³.split(" ");
		Inventory inv = ev.getInventory();
		if (tytu³Splited[0].equals("Menu")) {
			ev.setCancelled(true);
			String nazwa = tytu³Splited[1];
			if (!mapaItemow.containsKey(nazwa)) return;
			MenuItem item;
			if (mapaItemow.get(nazwa).containsKey(slot))
				item = mapaItemow.get(nazwa).get(slot);
			else
				item = mapa.get(nazwa).itemy.get(0);
			if (item.komenda != null && MenuInv.sprawdzPermisje(p, item)) {
				p.closeInventory();
				if(item.komenda.startsWith("Menu"))
					dajMenu(p, item.komenda.split(" ")[1]);
				else 
					wykonaj(p, item.komenda);
			}
			
		} else if (tytu³.equals("§e§lWybierz Gracza")) {
			ev.setCancelled(true);
			
			ItemStack item = inv.getItem(slot);
			
			if (slot >= inv.getSize()-9) {
				switch(slot % 9) {
				case 0:
					mapaMenuStronnych.get(p.getName()).poprzedniaStrona();
					return;
				case 8:
					mapaMenuStronnych.get(p.getName()).nastêpnaStrona();
					return;
				}
				return;				
			}
			if (item.getType().equals(Material.PLAYER_HEAD)) {
				String gracz = item.getItemMeta().getDisplayName().substring(2);
				String komenda = mapaKomend.get(p.getName());
				wykonaj(p, zamienPierwsze(komenda, "{gracz}", gracz));
			}
			
		} else if (tytu³Splited[0].equals("§2§l§oLiczba:§4§l")) {
			if (slot < 36) {
				ev.setCancelled(true);
				ItemStack item = ev.getCurrentItem();
				if (item.getType().equals(Material.PLAYER_HEAD)) {
					String wyraz = item.getItemMeta().getDisplayName().substring(2);
					switch (wyraz) {
					case "Anuluj":
						if (tytu³.split(" ").length == 1)
							p.closeInventory();
						else
							wybórLiczby(p, "");
						break;
					case "Zatwierdz":
						if (tytu³.split(" ").length == 1) {
							p.sendMessage("§4Nie podano ¿adnej liczby");
							break;
						}
						String komenda = mapaKomend.get(p.getName());
						wykonaj(p, zamienPierwsze(komenda, "{liczba}", tytu³Splited[1]));
						break;
					default:
						if (tytu³.split(" ").length == 1)
							wybórLiczby(p, wyraz);
						else
							wybórLiczby(p, tytu³Splited[1] + wyraz);
						break;
					}
				}
			}
		}
	}
	
	
	// Niezale¿na metoda, jest tu bo jest krótka xD
	@SuppressWarnings("deprecation")
	@EventHandler
	public void blokadaSkrzynekNaZwierzetach(PlayerInteractEntityEvent ev) {
		EntityType typ = ev.getRightClicked().getType();
		Material   mat = ev.getPlayer().getItemInHand().getType();
		if (mat.equals(Material.CHEST) || mat.equals(Material.TRAPPED_CHEST))
			if (typ.equals(EntityType.MULE) || typ.equals(EntityType.LLAMA) || typ.equals(EntityType.TRADER_LLAMA) || typ.equals(EntityType.DONKEY))
				ev.setCancelled(true);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player)
			dajMenu((Player) sender, "Skyblock");
		else
			sender.sendMessage("Tylko gracz mo¿e korzystaæ z menu");
		return true;
	}

}

class MenuInv {

	private List<Integer> specjalne = Lists.newArrayList();
	public  List<MenuItem> itemy;
	private int wielkosc;
	private String menu;
	
	public MenuInv(int Wielkosc, List<MenuItem> Itemy, String Menu) {
		wielkosc = Wielkosc;
		itemy = Itemy;
		menu = Menu;
		
		for (int i=0; i<itemy.size(); i++) {
			ItemMeta meta = itemy.get(i).item.getItemMeta();
			if (meta.hasDisplayName() && sprawdzZawartoœæ(meta.getDisplayName())) {
				specjalne.add(i);
				continue;
			}
			if (meta.hasLore()) 
				for (String linia : meta.getLore())
					if (sprawdzZawartoœæ(linia)) {
						specjalne.add(i);
						continue;
					}
		}
	}
	
	public void otwórz(Player p) {
		Inventory inv = Bukkit.createInventory(null, wielkosc, "Menu " + menu);
		for (int i=1; i<itemy.size(); i++)
			umieœæItem(p, inv, i);
		for (int i=0; i<wielkosc; i++)
			if (inv.getItem(i) == null)
				umieœæItem(p, inv, 0, i);
		p.openInventory(inv);
	}
	private void umieœæItem(Player p, Inventory inv, int i) {
		umieœæItem(p, inv, i, itemy.get(i).slot);
	}
	private void umieœæItem(Player p, Inventory inv, int i, int slot) {
		if (!sprawdzPermisje(p, itemy.get(i))) return;
		ItemStack item = itemy.get(i).item;
		if (specjalne.contains(i))
			item = podmieñCa³oœæ(p, item);
		inv.setItem(slot, item);
	}

	public static boolean sprawdzPermisje(Player p, MenuItem item) {
		if (item.permisja == null) return true;
		return p.hasPermission(item.permisja);
	}
	
	private ItemStack podmieñCa³oœæ(Player p, ItemStack item) {
		ItemStack w = item.clone();
		ItemMeta meta = w.getItemMeta();
		if (meta.hasDisplayName())
			meta.setDisplayName(podmieñLinie(p, meta.getDisplayName()));
		if (meta.hasLore()) {
			List<String> lore = meta.getLore();
			for (int i=0; i<lore.size(); i++) 
				lore.set(i, podmieñLinie(p, lore.get(i)));
			meta.setLore(lore);
		}
		w.setItemMeta(meta);
		return w;
	}
	private boolean sprawdzZawartoœæ(String text) {
		if (Main.ekonomia && text.contains("{kasa}"))
			return true;
		if (text.contains("{exp}") || text.contains("{nick}"))
			return true;
		return false;
	}
	private String podmieñLinie(Player p, String text) {
		String w = text;
		if (w.contains("{exp}"))
			w = w.replace("{exp}", Func.IntToString(Poziom.policzCa³yExp(p)));
		if (w.contains("{nick}"))
			w = w.replace("{nick}", p.getName());
		if (Main.ekonomia)
			w = w.replace("{kasa}", Func.DoubleToString(Main.econ.getBalance(p)));
		return w;
	}
	
}

class MenuItem {
	public String permisja;
	public String komenda;
	public ItemStack item;
	public int slot;
	
	public MenuItem(int Slot, ItemStack Item, String Komenda, String Permisja) {
		permisja = Permisja;
		komenda = Komenda;
		item = Item;
		slot = Slot;
	}
	public MenuItem kopia(int slot) {
		return new MenuItem(slot, item, komenda, permisja);
	}
}
