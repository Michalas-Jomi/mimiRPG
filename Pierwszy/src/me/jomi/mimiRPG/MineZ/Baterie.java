package me.jomi.mimiRPG.MineZ;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.Customizacja.CustomoweItemy;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
public class Baterie implements Listener, Przeładowalny {
	public static final String prefix = Func.prefix(Baterie.class);
	// nazwa bateri
	private static final NamespacedKey klucz = new NamespacedKey(Main.plugin, "baterie");
	
	private static final Map<String, Bateria> baterie = new HashMap<>();
	
	public static class Bateria {
		public final String nazwa;
		final Pattern patternLore;
		final String domyślnyLore;
		final char[] kolory;
		
		Bateria(String nazwa, ConfigurationSection sekcja) {
			this.nazwa = nazwa;
			
			patternLore = Pattern.compile(sekcja.getString("patternLore"));
			
			List<String> kolory = sekcja.getStringList(".koloryNaładowania");
			this.kolory = new char[kolory.size()];
			for (int i=0; i < this.kolory.length; i++)
				this.kolory[i] = kolory.get(i).charAt(0);
			
			domyślnyLore = sekcja.getString("lore");
			
			if (!patternLore.matcher(domyślnyLore).matches())
				throw new IllegalArgumentException("Pattern baterii " + nazwa + " nie pokrywa się z jej lore");
		}
		
		public boolean pełna(ItemStack item) {
			return getPoziomNaładowania(item) >= getMaxPoziomNaładowania();
		}
		
		public boolean jestBaterią(ItemStack item) {
			if (!item.hasItemMeta()) return false;
			if (!item.getItemMeta().getPersistentDataContainer().has(klucz, PersistentDataType.STRING)) return false;
			
			return item.getItemMeta().getPersistentDataContainer().get(klucz, PersistentDataType.STRING).equals(nazwa);
		}
		public int getPoziomNaładowania(ItemStack item) {
			if (item.hasItemMeta() && item.getItemMeta().hasLore())
				for (String linia : item.getItemMeta().getLore()) {
					Matcher matcher = patternLore.matcher(linia);
					if (!matcher.matches())
						continue;
					
					return Func.Int(matcher.group(1).substring(2));
				}
			return 0;
		}
		public int getMaxPoziomNaładowania() {
			return kolory.length - 1;
		}

		public ItemStack ustawPoziomNaładowania(ItemStack item, int poziom) {
			if (poziom > getMaxPoziomNaładowania())
				return ustawPoziomNaładowania(item, getMaxPoziomNaładowania());
			
			boolean podmienione = false;
			
			ItemMeta meta = item.getItemMeta();
			List<String> lore = meta.hasLore() ? meta.getLore() : null;
			if (lore == null) lore = new ArrayList<>();
			
			UnaryOperator<String> func = linia -> {
				Matcher matcher = patternLore.matcher(linia);
				
				if (!matcher.matches())
					return null;
				
				return new StringBuilder(linia).replace(matcher.start(1), matcher.end(1), "§" + String.valueOf(kolory[poziom]) + poziom).toString();
			};
			
			for (int i = 0; i < lore.size(); i++) {
				String nowe = func.apply(lore.get(i));
				if (nowe != null) {
					lore.set(i, nowe);
					podmienione = true;
					break;
				}
			}
			
			if (!podmienione)
				lore.add(func.apply(domyślnyLore));
			
			meta.setLore(lore);
			item.setItemMeta(meta);
			
			return item;
		}

		public ItemStack stwórz() {
			ItemStack item = Func.stwórzItem(Material.LIGHT_BLUE_DYE, "&9Bateria");
		
			ItemMeta meta = item.getItemMeta();
			meta.getPersistentDataContainer().set(klucz, PersistentDataType.STRING, nazwa);
			
			item.setItemMeta(meta);
			
			ustawPoziomNaładowania(item, 0);
			
			
			return item;
		}
	
		public String koloruj(int naładowanie) {
			return "§" + String.valueOf(kolory[Math.min(naładowanie, kolory.length - 1)]) + naładowanie;
		}
	}
	
	public static Bateria bateria(String nazwa) {
		return baterie.get(nazwa);
	}
	public static Bateria bateria(ItemStack item) {
		if (item.hasItemMeta()) {
			String klucz = item.getItemMeta().getPersistentDataContainer().get(Baterie.klucz, PersistentDataType.STRING);
			return klucz == null ? null : bateria(klucz);
		}
		return null;
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.MONITOR)
	public void interakcja(PlayerInteractEvent ev) {
		if (ev.isCancelled()) return;
		if (ev.getAction() != Action.RIGHT_CLICK_BLOCK) return;

		ItemStack item = ev.getPlayer().getInventory().getItemInMainHand();
		
		Func.wykonajDlaNieNull(bateria(item), bateria -> {
			int energia = bateria.getPoziomNaładowania(item);
			if (Main.włączonyModół(PaneleSłoneczne.class) && energia < bateria.getMaxPoziomNaładowania())
				Func.wykonajDlaNieNull(PaneleSłoneczne.znajdz(ev.getClickedBlock()), panel -> {
					int wPanelu = panel.getEnergia();
					
					int ładowane = Math.min(wPanelu, bateria.getMaxPoziomNaładowania() - energia);
					
					panel.ustawEnergię(wPanelu - ładowane);
					bateria.ustawPoziomNaładowania(item, energia + ładowane);
					
					ev.getPlayer().getInventory().setItemInMainHand(item);
					
					ev.getClickedBlock().getWorld().playSound(ev.getClickedBlock().getLocation().add(.5, .5, .5), Sound.ENTITY_VEX_DEATH, .5f, 2f);
					
					
					Func.powiadom(ev.getPlayer(), prefix + "%s -> %s (%s) (%s)",
							bateria.koloruj(energia), bateria.koloruj(energia + ładowane),  "+" + ładowane, "§9Panel Słoneczny");
					
					Main.log(prefix + Func.msg("%s naładował baterię %s -> %s (%s) w panelu słonecznym %s",
							ev.getPlayer().getName(), bateria.koloruj(energia), bateria.koloruj(energia + ładowane),  "+" + ładowane,
							Func.locBlockToString(ev.getClickedBlock().getLocation())));
				});
		});
		
	}
	
	
	@Override
	public void przeładuj() {
		baterie.values().forEach(bateria -> {
			CustomoweItemy.customoweItemy.remove("bateria_" + bateria.nazwa + "-" + "0");
			CustomoweItemy.customoweItemy.remove("bateria_" + bateria.nazwa + "-" + bateria.getMaxPoziomNaładowania());
		});
		
		baterie.clear();
		
		Func.wykonajDlaNieNull(Main.ust.sekcja("Baterie"), sekcja -> 
				sekcja.getKeys(false).forEach(klucz -> 
						baterie.put(klucz, new Bateria(klucz, sekcja.getConfigurationSection(klucz)))));
		
		baterie.values().forEach(bateria -> {
			int max = bateria.getMaxPoziomNaładowania();
			
			CustomoweItemy.customoweItemy.put("bateria_" + bateria.nazwa + "-" + "0", bateria.stwórz());
			CustomoweItemy.customoweItemy.put("bateria_" + bateria.nazwa + "-" + max, bateria.ustawPoziomNaładowania(bateria.stwórz(), max));
		});
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Baterie", baterie.size());
	}
}
