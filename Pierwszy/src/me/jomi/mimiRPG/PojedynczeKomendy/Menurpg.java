package me.jomi.mimiRPG.PojedynczeKomendy;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;

@Moduł
public class Menurpg extends Komenda implements Listener {
	static class Atrybut extends ItemStack {
		public static String prefix = Func.koloruj("&2[&aRPG&2] &6");
		private Attribute atrybut;
		private double maxIlość;
		private double przyrost;
		private int bazowaCena;
		private double domyślna;
		private Inventory inv;
		private String nazwa;
		private int slot;
		private Player p;
		
		public Atrybut(Player gracz, HashMap<String, Atrybut> mapa, String Nazwa, int Slot, double Przyrost, double MaxIlość, int BazowaCena, Inventory Inv, Attribute Atrybut, String url) {
			super(Material.PLAYER_HEAD);
			domyślna = domysnaWartoscAtrybutu(Atrybut);
			bazowaCena = BazowaCena;
			maxIlość = MaxIlość;
			przyrost = Przyrost;
			atrybut = Atrybut;
			nazwa = Nazwa;
			slot = Slot;
			inv = Inv;
			p = gracz;
			mapa.put(Nazwa, this);
			ustawTeksture(url);
			ustaw();
		}
		
		public void ustaw() {
			ustawOpis();
			inv.setItem(slot, this);
		}
		
		public void kliknięty() {
			double at = p.getAttribute(atrybut).getBaseValue();
			if (at >= maxIlość) return;
			int exp = Poziom.policzCałyExp(p);
			int ile = przeliczCene();
			if (exp < ile) {
				p.sendMessage(prefix + "Posiadasz jedynie§e " + Func.IntToString(exp) + " §6expa.");
				return;
			}
			p.giveExp(-ile);
			p.getAttribute(atrybut).setBaseValue(at + przyrost);
			ustaw();
		}
		
		private int przeliczCene() {
			int akt = (int) ((p.getAttribute(atrybut).getBaseValue() - domyślna) / przyrost);
			int ile = bazowaCena;
			ile += bazowaCena * akt;
			return ile;
		}
		private static double domysnaWartoscAtrybutu(Attribute at) {
			switch (at) {
			case GENERIC_ATTACK_DAMAGE:
				return 1;
			case GENERIC_ATTACK_SPEED:
				return 4;
			case GENERIC_MAX_HEALTH:
				return 20;
			case GENERIC_KNOCKBACK_RESISTANCE:
			case GENERIC_ARMOR:
				return 0;
			default:
				return -1;
			}
		}
		
		private void ustawOpis() {
			SkullMeta meta = (SkullMeta) this.getItemMeta();
			double at = Func.zaokrąglij(p.getAttribute(atrybut).getBaseValue(), 2);
			String nextlvl = "Następny poziom: §e" + Func.zaokrąglij(at+przyrost, 2);
			String cenaexp = "Potrzebny exp: §e" + Func.IntToString(przeliczCene());
			if (at >= maxIlość) {
				nextlvl = "§6§oOsiągnięto już maksymalny poziom";
				cenaexp = null;
			}
			meta.setLore(Arrays.asList("Zwiększa " + nazwa.toLowerCase(), 
					  "Aktualny poziom: §e" + at, nextlvl, cenaexp));
			this.setItemMeta(meta);
			
		}
		private void ustawTeksture(String url) {
			SkullMeta meta = (SkullMeta) this.getItemMeta();
			meta.setDisplayName(nazwa);
			// tekstura
			GameProfile profile = new GameProfile(UUID.randomUUID(), null);
	        profile.getProperties().put("textures", new Property("textures", url));
	        try {
	            Field profileField = meta.getClass().getDeclaredField("profile");
	            profileField.setAccessible(true);
	            profileField.set(meta, profile);
	        }
	        catch (IllegalArgumentException|NoSuchFieldException|SecurityException | IllegalAccessException error) {
	            error.printStackTrace();
	        }
			this.setItemMeta(meta);
		}
	}
	
	
	private static HashMap<String, HashMap<String, Atrybut>> mapa = new HashMap<String, HashMap<String, Atrybut>>();

	public Menurpg() {
		super("menurpg");
	}
	
	private static void dajInv(Player p) {
		Inventory inv = Bukkit.createInventory(p, 27, "menu RPG");
		
		// Itemy
		HashMap<String, Atrybut> m = new HashMap<String, Atrybut>();
		new Atrybut(p, m, "Zdrowie", 			10, 1,    40, 500,  inv, Attribute.GENERIC_MAX_HEALTH, 		 	"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjEyNjZiNzQ4MjQyMTE1YjMwMzcwOGQ1OWNlOWQ1NTIzYjdkNzljMTNmNmRiNGViYzkxZGQ0NzIwOWViNzU5YyJ9fX0=");
		new Atrybut(p, m, "Pancerz", 			11, 0.2,  5,  1_000, inv, Attribute.GENERIC_ARMOR, 				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjUyNTU5ZjJiY2VhZDk4M2Y0YjY1NjFjMmI1ZjJiNTg4ZjBkNjExNmQ0NDY2NmNlZmYxMjAyMDc5ZDI3Y2E3NCJ9fX0=");
		new Atrybut(p, m, "Obrazenia", 		  	16, 0.1,  5,  3_000, inv, Attribute.GENERIC_ATTACK_DAMAGE, 		"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTBkZmM4YTM1NjNiZjk5NmY1YzFiNzRiMGIwMTViMmNjZWIyZDA0Zjk0YmJjZGFmYjIyOTlkOGE1OTc5ZmFjMSJ9fX0=");
		new Atrybut(p, m, "Szybkość ataku", 	15, 0.1,  6,  4_000, inv, Attribute.GENERIC_ATTACK_SPEED, 			"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzdlNmM0MGY2OGI3NzVmMmVmY2Q3YmQ5OTE2YjMyNzg2OWRjZjI3ZTI0Yzg1NWQwYTE4ZTA3YWMwNGZlMSJ9fX0=");
		new Atrybut(p, m, "Odporność na odrzut",13, 0.01, 0.2,10_000, inv, Attribute.GENERIC_KNOCKBACK_RESISTANCE,	"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGZlNjY4ZTBkMzE0MTk3OGI4YWUyN2JmMjExYjAxYjQ0ZjEwNmI5ZDY0NzQxN2I4NTIwYTBhZGJjZjJlZjM1ZiJ9fX0=");
		mapa.put(p.getName(), m);
		
		// Wykończenie
		WypełnijSloty(inv);
		p.openInventory(inv);
	}
	
	@EventHandler
	public void kliknięcie(InventoryClickEvent ev) {
		if (!ev.getView().getTitle().equalsIgnoreCase("menu RPG")) return;
		
		Player p = (Player) ev.getWhoClicked();
		int slot = ev.getRawSlot();
		
		if (slot < 0 || slot >= 27) return;
		
		if (ev.getCurrentItem().getType().equals(Material.PLAYER_HEAD))
			mapa.get(p.getName()).get(ev.getCurrentItem().getItemMeta().getDisplayName()).kliknięty();
		ev.setCancelled(true);
	}
	
	private static void WypełnijSloty(Inventory inv) {
		ItemStack item;
		item = Func.stwórzItem(Material.RED_STAINED_GLASS_PANE, 1, "&4Menu", Arrays.asList("&fTutaj możesz ulepszać", "&fswoje statystyki"));
		for (int i=0; i<27; i++) {
			if (inv.getItem(i) == null)
				inv.setItem(i, item);
		}
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player)
			dajInv((Player) sender);
		else
			sender.sendMessage("Tylko Gracz może z tego korzystać");
		return false;
	}
}

