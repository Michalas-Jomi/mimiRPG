package me.jomi.mimiRPG.RPG;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.minecraft.server.v1_16_R2.NBTTagCompound;

import me.jomi.mimiRPG.Baza;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.RPG.ItemyRPG.Rozwój;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Funkcje.TriConsumer;
import me.jomi.mimiRPG.util.KomendaZMapowanymiItemami;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Krotki.TriKrotka;
import me.jomi.mimiRPG.util.Panel;
import me.jomi.mimiRPG.util.Przeładowalny;

// TODO bedrock useless
// TODO flint useless

// TODO handlarz narkotykami

@Moduł
public class ItemyRPG extends KomendaZMapowanymiItemami<Rozwój> implements Listener, Przeładowalny {
	public static class Rozwój extends Mapowany {
		public static class Lvl extends Bonusy {
			@Mapowane double potrzebnyExp; // na następny lvl
			@Mapowane int dodatkowe_punkty = 1;
			
			@Override
			public void dodajDoItemu(ItemStack item) {
				super.dodajDoItemu(item);
				dodajPunkty(item, dodatkowe_punkty);
			}
		}
		
		public ItemStack stwórzNowy() {
			net.minecraft.server.v1_16_R2.ItemStack item = CraftItemStack.asNMSCopy(podstawowyItem);
			
			NBTTagCompound tag = new NBTTagCompound();
			tag.setInt("lvl", lvle.isEmpty() ? -1 : 0);
			tag.setString("rozwoj", id);
			tag.setDouble("exp", 0);
			tag.setInt("punkty", 0);
			
			NBTTagCompound tagItemu = item.getOrCreateTag();
			tagItemu.set("mimiItemRPG", tag);
			
			item.setTag(tagItemu);
			
			ItemStack bukkitItem = CraftItemStack.asCraftMirror(item);
			
			if (!lvle.isEmpty())
				lvle.get(0).dodajDoItemu(bukkitItem);
			
			ustawProgres(bukkitItem, 0, 1, 0);
			
			return bukkitItem;
		}
		
		@Mapowane private ItemStack podstawowyItem;
		@Mapowane String id;// musi sie zgadzać z kluczem w configu
		@Mapowane List<Lvl> lvle;
	}
	
	public static final String prefix = Func.prefix("Itemy RPG");
	public static final String permEdytor = Func.permisja("itemrpg.edytor");

	Panel panel = new Panel(true);
	public ItemyRPG() {
		super("itemrpg", new Config("configi/ItemyRPG"), Rozwój.class);
		
		edytor.zarejestrujOnZatwierdz((rozwój, ścieżka) -> rozwój.id = ścieżka);
		edytor.zarejestrójWyjątek("/itemrpg edytor id", (rozwój, ścieżka) -> null);
		
		final String tempTagNieOddawaniaItemku = "mimitempTagNieOddawaniaItemkuItemyRPG";
		panel.ustawClose(ev -> {
			if (!ev.getPlayer().getScoreboardTags().contains(tempTagNieOddawaniaItemku))
				if (ev.getPlayer().getInventory().getItemInMainHand().getType() == Material.AIR)
					ev.getPlayer().getInventory().setItemInMainHand(ev.getInventory().getItem(slotItemuWPanelu));
				else
					Func.dajItem((Player) ev.getPlayer(), ev.getInventory().getItem(slotItemuWPanelu));
		});
		panel.ustawClick(ev -> {
			ItemStack item = ev.getInventory().getItem(slotItemuWPanelu);
			ItemStack klikany = ev.getCurrentItem();
			if (ev.getRawSlot() >= slotPierwszyEnchantów) {
				if (!klikany.isSimilar(Baza.pustySlot)) {
					int punkty = dajPunkty(item);
					if (punkty >= klikany.getItemMeta().getCustomModelData()) {
						Enchantment ench = Enchantment.getByKey(NamespacedKey.minecraft(klikany.getItemMeta().getDisplayName().substring(2)));
						Func.enchantuj(item, ench, item.getEnchantmentLevel(ench) + 1);
						ustawPunkty(item, punkty - klikany.getItemMeta().getCustomModelData());
						ev.getWhoClicked().addScoreboardTag(tempTagNieOddawaniaItemku);
						otwórzPanel((Player) ev.getWhoClicked(), item);
						ev.getWhoClicked().removeScoreboardTag(tempTagNieOddawaniaItemku);
					}
				}
			}
		});
	}
	
	static final int slotPierwszyEnchantów = 3 * 9;
	static final int slotItemuWPanelu = 13;
	static final int slotPktewWPanelu = 10;
	void otwórzPanel(Player p, ItemStack item) {
		Inventory inv = panel.stwórz(null, 5, "&4&lUlepsz Item");
		
		NBTTagCompound tag = nmsItem(item).getOrCreateTag().getCompound("mimiItemRPG");
		int punkty = tag.getInt("punkty");
		String rozwój = tag.getString("rozwoj");
		
		
		
		inv.setItem(slotItemuWPanelu, item);
		inv.setItem(slotPktewWPanelu, Func.stwórzItem(Material.SUNFLOWER, "&6&lPunkty", punkty,
				"&aPosiadasz: &e" + punkty, "&aMożesz ulepszyć za nie itemek"));
		
		// (Enchant, aktlvl, potrzebne pkt)
		List<TriKrotka<Enchantment, Integer, Integer>> enchanty = new ArrayList<>();
		Func.wykonajDlaNieNull(configUst.sekcja("ceny." + rozwój + ".enchanty"), sekcja -> {
			sekcja.getKeys(false).forEach(nazwa -> {
				Enchantment ench = Enchantment.getByKey(NamespacedKey.minecraft(nazwa));
				
				if (!configUst.wczytajBoolean("ceny." + rozwój + ".konflikty") && item.getItemMeta().hasConflictingEnchant(ench))
					return;
				
				List<Integer> pkt = sekcja.getIntegerList(nazwa);
				int lvl = item.getEnchantmentLevel(ench);
				
				enchanty.add(new TriKrotka<>(ench, lvl, pkt.size() <= lvl ? -1 : pkt.get(lvl)));
			});
		});
		
		Iterator<TriKrotka<Enchantment, Integer, Integer>> it = enchanty.iterator();
		for (int slot : Func.sloty(enchanty.size(), enchanty.size() > 7 ? 2 : 1)) {
			slot += slotPierwszyEnchantów;
			
			TriKrotka<Enchantment, Integer, Integer> krotka = it.next();
			ItemStack ikona = Func.stwórzItem(Material.ENCHANTED_BOOK,
					"&c" + krotka.a.getKey().getKey().replace('_', ' '), krotka.b,
					"&aAktualny poziom&8: &e" + krotka.b);
			
			if (krotka.c != -1) Func.dodajLore(ikona, "&aPotrzebne punkty&8: &e" + krotka.c);
			else 				Func.dodajLore(ikona, "&4&lMax poziom");
			
			Func.customModelData(ikona, krotka.c);
			
			inv.setItem(slot, ikona);
		}

		p.openInventory(inv);
	}
/*
aqua_affinity: turtle_helmet
bane_of_arthropods: spider_eye
blast_protection: tnt
channeling: arrow
depth_strider: sea_grass
efficiency: sugar
feather_falling: feather
fire_aspect: fire
fire_protection: fire
flame: fire
fortune: diamond
frost_walker: packed_ice
impaling: blaze_rod
infinity: end_crystal
knockback: netherite_ingot
looting: bone
loyalty: bone
luck_of_the_sea: sea_heart
lure: cocoa_beans
mending: experience_bottle
multishot: tripped_arrow
piercing: arrow
power: blaze_powder
projectile_protection: arrow
protection: shield
punch: cod
quick_charge: sugar
respiration: potion
riptide: elytra
sharpness: iron_sword
silk_touch: white_wool
smite: zombie_head
soul_speed: soul_sand
sweeping: diamond_sword
thorns: weeping_vines
unbreaking: anvil
 */
	
	private static Field fieldHandle = null; 
	static {
		try {
			fieldHandle = Func.dajField(CraftItemStack.class, "handle");
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	static net.minecraft.server.v1_16_R2.ItemStack nmsItem(ItemStack item) {
		try {
			return (net.minecraft.server.v1_16_R2.ItemStack) fieldHandle.get(item);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}
	@EventHandler(priority = EventPriority.LOW)
	public void zmianaRęki(PlayerSwapHandItemsEvent ev) {
		if (ev.getPlayer().isSneaking())
			try {
				ItemStack item = ev.getPlayer().getInventory().getItemInMainHand();
				Func.wykonajDlaNieNull(nmsItem(item), nmsItem -> {
					NBTTagCompound tag = nmsItem.getOrCreateTag().getCompound("mimiItemRPG");
					if (!tag.isEmpty()) {
						ev.setCancelled(true);
						otwórzPanel(ev.getPlayer(), item);
						ev.getPlayer().getInventory().setItemInMainHand(null);
					}
				});
			} catch (Throwable e) {
				e.printStackTrace();
			}
	}
	
	
	static int dajPunkty(ItemStack item) {
		return nmsItem(item).getTag().getCompound("mimiItemRPG").getInt("punkty");
	}
	static void dodajPunkty(ItemStack item, int ile) {
		ustawPunkty(item, dajPunkty(item) + ile);
	}
	static void ustawPunkty(ItemStack item, int ile) {
		nmsItem(item).getTag().getCompound("mimiItemRPG").setInt("punkty", ile);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void zabijanie(EntityDeathEvent ev) {
		Func.wykonajDlaNieNull(ev.getEntity().getKiller(), p -> podexp(p, p.getInventory().getItemInMainHand(), () -> exp(ev.getEntity().getType())));
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void kopanie(BlockBreakEvent ev) throws Throwable {
		if (!ev.isCancelled())
			podexp(ev.getPlayer(), ev.getPlayer().getInventory().getItemInMainHand(), () -> exp(ev.getBlock().getType()));
	}
	
	//{mimiItemRPG:{rozwoj: String, lvl: int, exp: double, punkty: int}}

	private static final String maxLvlLore = Func.koloruj("&6Level&8: &%aa3333-773333&l&oMAX");
	static void ustawProgres(ItemStack item, double exp, double potrzebnyExp, int lvl) {
		ItemMeta meta = item.getItemMeta();
		List<String> lore = Func.nieNull(meta.getLore());
		
		if (lvl == -1) {
			if (!lore.get(0).equals(maxLvlLore)) {
				lore.set(0, maxLvlLore);
				if (lore.size() >= 2)
					lore.remove(1);
				meta.setLore(lore);
				item.setItemMeta(meta);
			}
			return;
		}
		
		while (lore.size() < 3)
			lore.add("");
		
		// Level: <lvl>
		// <pasek progresu> (50 x |)
		//
		
		StringBuilder strB = new StringBuilder("§8>| §a");
		double zielone = exp / potrzebnyExp * 50;
		int i=0;
		while (i++ < zielone)
			strB.append('|');
		strB.append("§c");
		while (i++ < 50)
			strB.append('|');
		strB.append(" §8|<");
		
		String progres = strB.toString();

		if (!lore.get(1).equals(progres)) {
			lore.set(0, "§6Level§8: §e" + (lvl + 1));
			lore.set(1, progres);
			meta.setLore(lore);
			item.setItemMeta(meta);
		}
	}
	
	public void podexp(Player p, ItemStack item, Supplier<Double> supIle) {
		if (item.getAmount() != 1)
			return;
		try {
			Func.wykonajDlaNieNull(nmsItem(item), nmsItem -> {
				NBTTagCompound tag = nmsItem.getOrCreateTag().getCompound("mimiItemRPG");
				if (!tag.isEmpty()) {
					double ile = supIle.get();
					if (ile != 0) {
						Func.wykonajDlaNieNull(config.wczytaj(tag.getString("rozwoj")), Rozwój.class, rozwoj -> {
							int ilvl = tag.getInt("lvl");
							if (ilvl == -1)
								return;
							
							double exp = tag.getDouble("exp") + ile;
							
							Rozwój.Lvl lvl = null;
							try {
								while (exp >= (lvl = rozwoj.lvle.get(ilvl)).potrzebnyExp) {
									ilvl++;
									exp -= lvl.potrzebnyExp;
									if (rozwoj.lvle.size() > ilvl) {
										Func.powiadom(p, prefix + "Wylevelowałeś %s na poziom %s!", nmsItem.getName().getString(), ilvl + 1);
										Main.log(prefix + Func.msg("Wylevelował %s na poziom %s w ścieżce rozwoju %s", p.getDisplayName(), ilvl + 1, rozwoj.id));
										for (int i=0; i < 3; i++)
											try {
												p.getWorld().spawnParticle(Func.losuj(Particle.values()),
														p.getLocation().add(0, 1, 0), Func.losuj(20, 50), 1.5, 1.5, 1.5, 0);
											} catch (Throwable e) {
												i--;
											}
										p.getWorld().playSound(p.getLocation(), Sound.ITEM_TOTEM_USE, .1f, 1f);
									}
									rozwoj.lvle.get(ilvl).dodajDoItemu(item);
								}
								if (ilvl >= rozwoj.lvle.size() - 1) {
									ilvl = -1;
								}
							} catch (IndexOutOfBoundsException e) {
								ilvl = -1;
								exp = 0;
							}

							ustawProgres(item, exp, lvl.potrzebnyExp, ilvl);
							
							tag.setInt("lvl", ilvl);
							tag.setDouble("exp", ilvl == -1 ? 0 : exp);
						});
					}
				}
			});
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private final static Krotka<Map<Material,   Double>, Double> expZBloków = new Krotka<>(new HashMap<>(), 0d);
	private final static Krotka<Map<EntityType, Double>, Double> expZMobów  = new Krotka<>(new HashMap<>(), 0d);
	
	Config configUst = new Config("ItemyRPG exp");
	@Override
	public void przeładuj() {
		super.przeładuj();
		
		configUst.przeładuj();
		expZMobów.b  = configUst.wczytajDouble("domyślne.Moby");
		expZBloków.b = configUst.wczytajDouble("domyślne.Bloki");
		TriConsumer<Class<?>,  Map<?, Double>, String> przetwórz = (clazz, mapaExpa, sciezka) -> {
			mapaExpa.clear();
			Func.wykonajDlaNieNull(configUst.sekcja(sciezka), sekcja ->
				sekcja.getValues(false).forEach((str, exp) ->
					mapaExpa.put(Func.pewnyCast(Func.StringToEnum(clazz, str)), Func.DoubleObj(exp))));
		};
		przetwórz.accept(Material.class,   expZBloków.a, "Bloki");
		przetwórz.accept(EntityType.class, expZMobów.a,  "Moby");
	}
	public double exp(Material mat) {
		return expZBloków.a.getOrDefault(mat, expZBloków.b);
	}
	public double exp(EntityType mob) {
		return expZMobów.a.getOrDefault(mob, expZMobów.b);
	}
	
	@Override public ItemStack	getItem(Rozwój rozwój)	{ return rozwój.stwórzNowy(); }
	@Override public String		getPrefix()				{ return prefix; }
}
