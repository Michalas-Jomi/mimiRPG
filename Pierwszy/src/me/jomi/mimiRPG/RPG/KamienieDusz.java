package me.jomi.mimiRPG.RPG;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.json.simple.JSONObject;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Baza;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.Customizacja.CustomoweItemy;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.LepszaMapa;
import me.jomi.mimiRPG.util.MultiConfig;
import me.jomi.mimiRPG.util.Napis;
import me.jomi.mimiRPG.util.Panel;
import me.jomi.mimiRPG.util.PersistentDataTypeCustom;
import me.jomi.mimiRPG.util.Przeładowalny;
import me.jomi.mimiRPG.util.Zegar;

@Moduł
public class KamienieDusz implements Zegar, Przeładowalny, Listener {
	public static abstract class Mechanika {
		public final String nazwa;
		
		public Mechanika(String nazwa, LepszaMapa<String> mapa) {
			this.nazwa = nazwa;
			wczytaj(mapa);
		}
		
		public abstract void zaaplikuj(ItemStack kopia, ItemMeta meta);
		public abstract void odaplikuj(ItemStack kopia, ItemMeta meta);
		public abstract void wczytaj(LepszaMapa<String> mapa);
		
		public long onAttack(EntityDamageByEntityEvent ev)	{return 0L;}
		public long onDamaged(EntityDamageEvent ev)			{return 0L;}
		public long onBlockBreak(BlockBreakEvent ev)		{return 0L;}
		public long czas(Player p)							{return 0L;}
	}
	public static class MechanikaEfekt extends Mechanika {
		public static enum Moment {
			ATTACK,
			DAMAGED,
			BLOCK_BREAK
		}
		public static enum Target {
			SELF,
			OTHER
		}
		
		int odnowienie; // w milisekundach
		double szansa;
		PotionEffect effekt;
		
		Moment moment;
		Target target;
		
		Enum<?> poCzym;
		
		public MechanikaEfekt(String nazwa, LepszaMapa<String> mapa) {
			super(nazwa, mapa);
		}
		
		@Override public void zaaplikuj(ItemStack kopia, ItemMeta meta) {}
		@Override public void odaplikuj(ItemStack kopia, ItemMeta meta) {}
		
		@Override
		public void wczytaj(LepszaMapa<String> mapa ) {
			odnowienie = mapa.get("odnowienie", 0);
			szansa = mapa.get("szansa", 1d);
			effekt = new PotionEffect(
					PotionEffectType.getByName(mapa.getString("efekt")),
					mapa.getInt("czas"),
					mapa.getInt("poziom"),
					mapa.getBoolean("ambient"),
					mapa.getBoolean("particle"),
					mapa.getBoolean("ikona")
					);
			
			moment = Func.StringToEnum(Moment.class, mapa.getString("moment"));
			target = Func.StringToEnum(Target.class, mapa.get("target", "self"));
			

			String strPoCzym = mapa.getString("poCzym");
			if (strPoCzym == null) strPoCzym = mapa.getString("po czym");
			if (strPoCzym != null)
				switch (moment) {
				case ATTACK:		poCzym = Func.StringToEnum(EntityType.class,	strPoCzym);	break;
				case DAMAGED:		poCzym = Func.StringToEnum(DamageCause.class,	strPoCzym);	break;
				case BLOCK_BREAK:	poCzym = Func.StringToEnum(Material.class,		strPoCzym);	break;
				}
		}
	
		
		private long fabric(Moment moment, Entity self, Entity other) {
			if (this.moment == moment) {
				Entity e = null;
				switch (target) {
				case SELF:	e = self;	break;
				case OTHER:	e = other; 	break;
				}
				
				if (e != null && e instanceof LivingEntity && Func.losuj(szansa))
					if (((LivingEntity) e).addPotionEffect(effekt))
						return odnowienie;
			}
			return 0L;
		}
		
		@Override
		public long onAttack(EntityDamageByEntityEvent ev) {
			if (poCzym == null || poCzym == ev.getEntity().getType())
				return fabric(Moment.ATTACK, ev.getDamager(), ev.getEntity());
			return 0L;
		}
		@Override
		public long onDamaged(EntityDamageEvent ev) {
			if (poCzym == null || poCzym == ev.getCause())
				return fabric(Moment.DAMAGED, ev.getEntity(), null); // TODO Damaged by entity
			return 0L;
		}
		@Override
		public long onBlockBreak(BlockBreakEvent ev) {
			if (poCzym == null || poCzym == ev.getBlock().getType())
				return fabric(Moment.BLOCK_BREAK, ev.getPlayer(), null);
			return 0L;
		}
	}
	public static class MechanikaAtrybut extends Mechanika {
		public MechanikaAtrybut(String nazwa, LepszaMapa<String> mapa) {
			super(nazwa, mapa);
		}
		
		Attribute attr;

		double wartość;
		AttributeModifier.Operation sposób;
		EquipmentSlot slot;
		
		public AttributeModifier getAttributeModifier(Material mat) {
			UUID uuid = getUUID(mat);
			return new AttributeModifier(
					uuid,
					uuid.toString().replace("-", ""),
					wartość,
					sposób,
					slot == null ? odpowiedniSlot(mat) : slot
					);
		}
		
		
		private void odświeżInv(Player p) {
			p.updateInventory();
			try {
				Object handle = Func.dajField(p.getClass(), "entity").get(p);
				Func.dajMetode(handle.getClass(), "playerTick").invoke(handle);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		@Override
		public void zaaplikuj(ItemStack kopia, ItemMeta meta) {
			if (!meta.hasAttributeModifiers()) {
				EquipmentSlot slot = odpowiedniSlot(kopia.getType());
				
				Player p = Bukkit.getOnlinePlayers().iterator().next();
				PlayerInventory inv = p.getInventory();
				
				ItemStack stary = inv.getItem(slot);
				if (stary != null) stary = stary.clone();
				
				inv.setItem(slot, null);
				odświeżInv(p);
				
				Map<Attribute, Double> mapa = new HashMap<>();
				
				Func.forEach(Attribute.values(), attr ->
					Func.wykonajDlaNieNull(p.getAttribute(attr), ai -> {
						double akt = ai.getValue();
						if (akt != 0)
							mapa.put(attr, akt);
					}));
				
				inv.setItem(slot, kopia);
				odświeżInv(p);
				
				ItemStack fstary = stary;
				Func.forEach(Attribute.values(), attr -> {
					double akt = 0;
					AttributeInstance ai = p.getAttribute(attr);
					if (ai != null)
						akt = ai.getValue();
					akt = akt - mapa.getOrDefault(attr, 0d);
					if (akt != 0)
						mapa.put(attr, akt);
					else
						mapa.remove(attr);
				});
				
				inv.setItem(slot, fstary);
				odświeżInv(p);
				
				Bukkit.getScheduler().runTask(Main.plugin, () -> odświeżInv(p));
				
				mapa.forEach((attr, val) -> {
					UUID uuid = UUID.randomUUID();
					meta.addAttributeModifier(attr, new AttributeModifier(uuid, uuid.toString().replace("-", "") , val, Operation.ADD_NUMBER, slot));
				});
			}
			
			meta.addAttributeModifier(attr, getAttributeModifier(kopia.getType()));
		}
		@Override
		public void odaplikuj(ItemStack kopia, ItemMeta meta) {
			meta.removeAttributeModifier(attr, getAttributeModifier(kopia.getType()));
		}
		@Override
		public void wczytaj(LepszaMapa<String> mapa) {
			attr = Func.StringToEnum(Attribute.class, mapa.getString("atrybut"));
			wartość = mapa.getDouble("wartość");
			sposób = Func.StringToEnum(AttributeModifier.Operation.class, mapa.get("sposób", "ADD_NUMBER"));
			String slot = mapa.get("slot", "HAND");
			if (!slot.equalsIgnoreCase("AUTO"))
				this.slot = Func.StringToEnum(EquipmentSlot.class, slot);
		}
		
		
		private UUID getUUID(Material mat) {
			EquipmentSlot slot = this.slot == null ? odpowiedniSlot(mat) : this.slot;
			
			String sc = "mechanika.atrybut.uuid." + slot + "." + nazwa;
			
			String uuid = configDane.wczytajStr(sc);
			if (uuid == null) {
				uuid = UUID.randomUUID().toString();
				configDane.ustaw_zapisz(sc, uuid);
			}
			
			return UUID.fromString(uuid);
		}
	}
	public static class MechanikaTrwałyEfekt extends Mechanika {
		public MechanikaTrwałyEfekt(String nazwa, LepszaMapa<String> mapa) {
			super(nazwa, mapa);
		}
		
		PotionEffect effekt;

		@Override public void zaaplikuj(ItemStack kopia, ItemMeta meta) {}
		@Override public void odaplikuj(ItemStack kopia, ItemMeta meta) {}

		@Override
		public void wczytaj(LepszaMapa<String> mapa) {
			effekt = new PotionEffect(
					PotionEffectType.getByName(mapa.getString("efekt")),
					mapa.getInt("czas"),
					mapa.getInt("poziom"),
					mapa.getBoolean("ambient"),
					mapa.getBoolean("particle"),
					mapa.getBoolean("ikona")
					);
		}
		
		@Override
		public long czas(Player p) {
			p.addPotionEffect(effekt);
			return 0L;
		}
	}
	
	public static enum Slot {
		ZBROJA(item -> EnchantmentTarget.ARMOR.includes(item)),
		HEŁM(item -> EnchantmentTarget.ARMOR_HEAD.includes(item)),
		KLATA(item -> EnchantmentTarget.ARMOR_TORSO.includes(item)),
		SPODNIE(item -> EnchantmentTarget.ARMOR_LEGS.includes(item)),
		BUTY(item -> EnchantmentTarget.ARMOR_FEET.includes(item)),
		
		BROŃ(item -> EnchantmentTarget.WEAPON.includes(item)),
		MIECZ(item -> item.getType().toString().contains("_SWORLD")),
		ŁUK(item -> EnchantmentTarget.BOW.includes(item)),
		KUSZA(item -> EnchantmentTarget.CROSSBOW.includes(item)),
		TRÓJZĄB(item -> EnchantmentTarget.TRIDENT.includes(item)),
		
		NARZĘDZIA(item -> EnchantmentTarget.TOOL.includes(item)),
		KILOF(item -> item.getType().toString().contains("_PICKAXE")),
		ŁOPATA(item -> item.getType().toString().contains("_SHOVEL")),
		SIEKIERA(item -> item.getType().toString().contains("_AXE")),
		MOTYKA(item -> item.getType().toString().contains("_HOE")),
		WĘDKA(item -> EnchantmentTarget.FISHING_ROD.includes(item)),
		
		WSZYSTKO(item -> true);
		
		private final Predicate<ItemStack> możnaZałożyć;
		Slot(Predicate<ItemStack> możnaZałożyć) {
			this.możnaZałożyć = możnaZałożyć;
		}
		
		public boolean możnaZałożyć(ItemStack item) {
			return możnaZałożyć.test(item);
		}
		
	}
	public static class KamieńDusz {
		private static final String linia0 = "§6Kamienie Dusz§8:";
		
		public final String nazwa;
		public final Mechanika[] mechaniki; 
		public final ItemStack item;
		public final Slot slot;
		
		public KamieńDusz(String nazwa, Slot slot, Mechanika[] mechaniki, ItemStack item) {
			this.mechaniki = mechaniki;
			this.nazwa = nazwa;
			this.slot = slot;
			this.item = item;
		}

		public void zaaplikuj(ItemStack kopia, ItemMeta meta) {
			for (int i=0; i < mechaniki.length; i++)
				mechaniki[i].zaaplikuj(kopia, meta);
			
			String[] stare = nałożone(meta);
			String[] nowe = new String[stare.length + 1];
			int i = -1;
			while (++i < stare.length)
				nowe[i] = stare[i];
			
			nowe[i] = nazwa;
			
			meta.getPersistentDataContainer().set(kluczKamieni, PersistentDataTypeCustom.stringArray, nowe);
			
			List<String> lore = meta.hasLore() ? Func.nieNull(meta.getLore()) : new ArrayList<>();
			
			if (lore.isEmpty() || !lore.get(0).equals(linia0)) {
				lore.add(0, linia0);
				lore.add(1, " ");
			}
			lore.add(1, "§8§l- §d" + Func.koloruj(nazwa));
			
			meta.setLore(lore);
			
		}
		public void odaplikuj(ItemStack kopia, ItemMeta meta, int indexWLiście) {
			for (int i=0; i < mechaniki.length; i++)
				mechaniki[i].odaplikuj(kopia, meta);
			

			List<String> lore = meta.getLore();
			lore.remove(indexWLiście);
			meta.setLore(lore);
			
			
			List<String> nałożone = Lists.newArrayList(nałożone(meta));
			String[] nowe = new String[nałożone.size() - 1];
			int i=0;
			
			while (i < nowe.length) {
				String akt = nałożone.remove(0);
				if (i == indexWLiście) {
					indexWLiście = -1;
					continue;
				}
				nowe[i++] = akt;
			}
			
			meta.getPersistentDataContainer().set(kluczKamieni, PersistentDataTypeCustom.stringArray, nowe);

		}
		public boolean możnaNałożyć(ItemStack item) {
			if (!slot.możnaZałożyć(item))
				return false;
			String[] nałożone = nałożone(item.getItemMeta());
			if (nałożone.length >= maxKamienieNaItem)
				return false;
			
			for (String nałożony : nałożone)
				if (nazwa.equals(nałożony))
					return false;
			
			return true;
		}
		
		private static String[] nałożone(ItemMeta meta) {
			if (meta == null) return new String[0];
			String[] array = meta.getPersistentDataContainer().get(kluczKamieni, PersistentDataTypeCustom.stringArray);
			return array == null ? new String[0] : array;
		}
	}
	
	
	public static final String prefix = Func.prefix(KamienieDusz.class);

	private static final NamespacedKey kluczKamieni = new NamespacedKey(Main.plugin, "kamieniedusz");
	private static final NamespacedKey kluczKamieniOdnowienia = new NamespacedKey(Main.plugin, "kamieniedusz_odnowienie");
	
	static ItemStack pustyKamień = Func.stwórzItem(Material.FIREWORK_STAR, "&8Pusty Kamień Dusz", 1, "&aZużyty kamień", "&aNic już z niego");
	static int maxKamienieNaItem = 2;
	
	
	@SuppressWarnings("deprecation")
	public KamienieDusz() {
		CustomoweItemy.customoweItemy.put("kamienieDusz_Ekstraktor", itemEkstarktor);
		
		panelMenu.ustawClick(ev -> {
			if (!Func.multiEquals(ev.getClick(), ClickType.RIGHT, ClickType.LEFT)) return;
			ItemStack item = ev.getInventory().getItem(panel_slotItemu);
			if (!ev.getCurrentItem().equals(itemBrakuKamienia))
				Func.wykonajDlaNieNull(rozpoznaj(ev.getCurrentItem()), kamień -> {
					if (!ev.getCursor().equals(itemEkstarktor)) return;
					
					ev.setCursor(ev.getCurrentItem());
					ev.getCursor().setType(ev.getCurrentItem().getType());
					ev.getCursor().setAmount(ev.getCurrentItem().getAmount());
					ev.getCursor().setItemMeta(ev.getCurrentItem().getItemMeta());
					ev.setCurrentItem(itemBrakuKamienia);
					
					ItemMeta meta = item.getItemMeta();
					
					kamień.odaplikuj(item, meta, slotWIndex(ev.getRawSlot()));
					
					item.setItemMeta(meta);
					ev.getInventory().setItem(panel_slotItemu, item);
					
					ev.getWhoClicked().addScoreboardTag("mimiKamienieDuszDropBlok");
					ev.getWhoClicked().closeInventory();
					ev.getWhoClicked().removeScoreboardTag("mimiKamienieDuszDropBlok");
					ev.getWhoClicked().openInventory(stwórzMenu(ev.getWhoClicked().getInventory().getItemInMainHand()));
				});
			else 
				Func.wykonajDlaNieNull(rozpoznaj(ev.getCursor()), kamień -> {
					if (!kamień.możnaNałożyć(item))
						return;
					
					ItemMeta meta = item.getItemMeta();
					
					kamień.zaaplikuj(item, meta);
					item.setItemMeta(meta);
					ev.getInventory().setItem(panel_slotItemu, item);
					
					ev.setCancelled(true);
					
					ev.setCurrentItem(ev.getCursor());
					
					ev.setCursor(pustyKamień);
					ev.getCursor().setType(pustyKamień.getType());
					ev.getCursor().setAmount(pustyKamień.getAmount());
					ev.getCursor().setItemMeta(pustyKamień.getItemMeta());
					
					Main.log(Func.msg(prefix + "%s nałożył kamień %s na %s przez panel", ev.getWhoClicked().getName(), kamień.nazwa, Napis.item(ev.getCurrentItem()).toString()));
				});
		});
		panelMenu.ustawClose(ev -> {
			if (ev.getPlayer().getInventory().getItemInMainHand().equals(panelMenu.dajDanePanelu(ev.getInventory()))) {
				ev.getPlayer().getInventory().setItemInMainHand(ev.getInventory().getItem(panel_slotItemu));
			} else {
				ev.getPlayer().sendMessage(prefix + "Twój item uległ awarii, zgłoś się z tym problemem do administratora");
				Main.log(prefix + ev.getPlayer().getName() + " był bliski kopiowania itemów przez panel kamieni dusz!");
			}
		});
	}
	
	/**
	 * Rozpoznaje którym Kamieniem dusz jest dany item
	 * @param item item kamienia dusz
	 * @return kamień dusz którym jest dany item
	 */
	public KamieńDusz rozpoznaj(ItemStack item) {
		return kamienieZItemów.get(item);
	}
	/**
	 * Zwraca wszystkie Kamienie Dusz z danego itemka
	 * @param item z którego są czytane kamienie
	 * @return Kamienie Dusz nałożone na ten itemek
	 */
	public List<KamieńDusz> wczytaj(ItemStack item) {
		List<KamieńDusz> kamienie = new ArrayList<>();
		
		if (item != null && item.hasItemMeta())
			Func.forEach(KamieńDusz.nałożone(item.getItemMeta()), nazwa -> kamienie.add(KamienieDusz.kamienie.get(nazwa)));
		
		return kamienie;
	}
	@SuppressWarnings("unchecked")
	public List<KamieńDusz> wczytajGotowe(ItemStack item) {
		List<KamieńDusz> kamienie = wczytaj(item);
		if (item == null || !item.hasItemMeta())
			return kamienie;
		
		
		JSONObject odnowienia = item.getItemMeta().getPersistentDataContainer().getOrDefault(kluczKamieniOdnowienia, PersistentDataTypeCustom.json, new JSONObject());
		
		long teraz = System.currentTimeMillis();
		for (int i = kamienie.size() - 1; i >= 0; i--) {
			KamieńDusz kamień = kamienie.get(i);
			
			if (((long) odnowienia.getOrDefault(kamień.nazwa, 0L)) > teraz)
				kamienie.remove(i);
		}
		
		return kamienie;
	}
	
	/**
	 * zwraca index na liście kamieni dusz poprzez slot
	 * @param slot w eq
	 * @return index na liście kamieni dusz
	 */
	private static int slotWIndex(int slot) {
		// TODO rozbudować
		return slot - (9 + 5);
	}
	
	@SuppressWarnings("unchecked")
	private void wykonaj(ItemStack item, Consumer<ItemStack> setter, Function<KamieńDusz, Long> func) {
		wczytajGotowe(item).forEach(kamień -> {
			long odnowienie = func.apply(kamień);
			if (odnowienie > 0) {
				ItemMeta meta = item.getItemMeta();
				
				JSONObject json = meta.getPersistentDataContainer().getOrDefault(kluczKamieniOdnowienia, PersistentDataTypeCustom.json, new JSONObject());
				json.put(kamień.nazwa, System.currentTimeMillis() + odnowienie);
				meta.getPersistentDataContainer().set(kluczKamieniOdnowienia, PersistentDataTypeCustom.json, json);
				
				item.setItemMeta(meta);
				setter.accept(item);
			}
		});
		
	}
	private void wykonaj(PlayerInventory inv, Function<KamieńDusz, Long> func) {
		wykonaj(inv.getItemInMainHand(), inv::setItemInMainHand, func);
		wykonaj(inv.getHelmet(), inv::setHelmet, func);
		wykonaj(inv.getChestplate(), inv::setChestplate, func);
		wykonaj(inv.getLeggings(), inv::setLeggings, func);
		wykonaj(inv.getBoots(), inv::setBoots, func);
	}
	private <E> void wykonaj(E ev, Entity e, BiFunction<Mechanika, E, Long> bif) {
		if (e instanceof Player && (!(ev instanceof Cancellable) || !((Cancellable) ev).isCancelled()))
			wykonaj(((Player) e).getInventory(), kamień -> {
				AtomicLong odnowienie = new AtomicLong(0L);
				Func.forEach(kamień.mechaniki, mechanika -> odnowienie.set(Math.max(odnowienie.get(), bif.apply(mechanika, ev))));
				return odnowienie.get();
			});
	}
	@EventHandler(priority = EventPriority.MONITOR) public void otrzymywanieDmg	(EntityDamageEvent ev) 			{wykonaj(ev, ev.getEntity(),  Mechanika::onDamaged);}
	@EventHandler(priority = EventPriority.MONITOR) public void zadanieDmg		(EntityDamageByEntityEvent ev)	{wykonaj(ev, ev.getDamager(), Mechanika::onAttack);}
	@EventHandler(priority = EventPriority.MONITOR) public void kopanie			(BlockBreakEvent ev)			{wykonaj(ev, ev.getPlayer(),  Mechanika::onBlockBreak);}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void klikanieEq(InventoryClickEvent ev) {
		if (ev.getRawSlot() >= ev.getInventory().getSize() && panelMenu.jestPanelem(ev.getInventory()) && ev.getCurrentItem() != null && ev.getCurrentItem().equals(ev.getWhoClicked().getInventory().getItemInMainHand())) {
			ev.setCancelled(true);
			return;
		}
		
		if (!ev.getClick().equals(ClickType.RIGHT)) return;
		if (ev.getCurrentItem() == null) return;
		Func.wykonajDlaNieNull(rozpoznaj(ev.getCursor()), kamień -> {
			if (!kamień.możnaNałożyć(ev.getCurrentItem()))
				return;
			
			ItemMeta meta = ev.getCurrentItem().getItemMeta();
			
			kamień.zaaplikuj(ev.getCurrentItem(), meta);
			ev.getCurrentItem().setItemMeta(meta);
			
			ev.setCancelled(true);
			
			ev.getCursor().setType(pustyKamień.getType());
			ev.getCursor().setAmount(pustyKamień.getAmount());
			ev.getCursor().setItemMeta(pustyKamień.getItemMeta());
			
			Main.log(Func.msg(prefix + "%s nałożył kamień %s na %s przez inv", ev.getWhoClicked().getName(), kamień.nazwa, Napis.item(ev.getCurrentItem()).toString()));
		});
	}
	@EventHandler
	public void zmianaRęki(PlayerSwapHandItemsEvent ev) {
		if (!ev.getPlayer().isSneaking()) return;
		if (wczytaj(ev.getOffHandItem()).isEmpty()) return;
		
		ev.setCancelled(true);
		ev.getPlayer().openInventory(stwórzMenu(ev.getOffHandItem()));
	}
	@EventHandler
	public void wyrzucanieItemków(PlayerDropItemEvent ev) {
		if (ev.getPlayer().getScoreboardTags().contains("mimiKamienieDuszDropBlok"))
			ev.setCancelled(true);
	}
	
	private final ItemStack itemEkstarktor = Func.połysk(Func.stwórzItem(Material.SCUTE, "&bEkstraktor Kamieni Dusz", "&aKliknij na kamień w panelu", "&aAby go wyjąć", "&cItem Jednorazowy!"));
	
	private final int panel_slotItemu = 9 + 2;
	private final Panel panelMenu = new Panel(true);
	private final ItemStack itemBrakuKamienia = Func.stwórzItem(Material.PINK_STAINED_GLASS_PANE, "&aSlot na kamień dusz");
	Inventory stwórzMenu(ItemStack item) {
		Inventory inv = panelMenu.stwórz(item.clone(), 3, "&1&lKamienie Dusz");
		
		Func.ustawPuste(inv, Baza.pustySlotCzarny);
		
		inv.setItem(panel_slotItemu, item.clone());
		AtomicInteger slot = new AtomicInteger(9 + 5);
		wczytaj(item).forEach(kamień -> inv.setItem(slot.getAndIncrement(), kamień.item));
		while (slot.get() <= 9 + 6)
			inv.setItem(slot.getAndIncrement(), itemBrakuKamienia);
		
		return inv;
	}
	
	
	static EquipmentSlot odpowiedniSlot(Material mat) {
		ItemStack item = new ItemStack(mat);
		
		if (EnchantmentTarget.ARMOR_HEAD.includes(item)) return EquipmentSlot.HEAD;
		if (EnchantmentTarget.ARMOR_TORSO.includes(item))return EquipmentSlot.CHEST;
		if (EnchantmentTarget.ARMOR_LEGS.includes(item)) return EquipmentSlot.LEGS;
		if (EnchantmentTarget.ARMOR_FEET.includes(item)) return EquipmentSlot.FEET;
		
		return EquipmentSlot.HAND;
	}
	
	
	static final Map<String, Mechanika> mechaniki = new HashMap<>();
	static final Map<String, KamieńDusz> kamienie = new HashMap<>();
	static final Map<ItemStack, KamieńDusz> kamienieZItemów = new HashMap<>();
	
	static final MultiConfig configKamienie  = new MultiConfig("Kamienie Dusz/Kamienie");
	static final MultiConfig configMechaniki = new MultiConfig("Kamienie Dusz/Mechaniki");
	private static final Config configDane = new Config("configi/Kamienie Dusz");
	
	
	@Override
	public void przeładuj() {
		kamienie.keySet().forEach(nazwa -> CustomoweItemy.customoweItemy.remove("kamieńDuszy_" + nazwa));
		
		configDane.przeładuj();
		configMechaniki.przeładuj();
		configKamienie.przeładuj();
		
		mechaniki.clear();
		kamienie.clear();
		kamienieZItemów.clear();

		wczytajZConfigów(configMechaniki,	mechaniki,	this::wczytajMechanike);
		wczytajZConfigów(configKamienie,	kamienie,	this::wczytajKamień);
		
		kamienie.forEach((nazwa, kamień) -> CustomoweItemy.customoweItemy.put("kamieńDuszy_" + nazwa, kamień.item));
	}
	private <T> void wczytajZConfigów(MultiConfig config, Map<String, T> mapa, BiFunction<String, Map<String, Object>, T> bif) {
		config.klucze().forEach(nazwa -> {
			try {
				mapa.put(nazwa, bif.apply(nazwa, config.sekcja(nazwa).getValues(false)));
			} catch (IllegalArgumentException e) {
				if (e.getMessage() != null)
					Main.log(prefix + nazwa + e.getMessage());
				else {
					Main.warn(prefix + nazwa);
					e.printStackTrace();
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		});
	}
	private Mechanika wczytajMechanike(String nazwa, Map<String, Object> mapa) {
		LepszaMapa<String> lmapa = new LepszaMapa<>(mapa);
		
		Class<? extends Mechanika> clazz;
		
		String rodzaj = lmapa.get("rodzaj", "efekt");
		switch (rodzaj.toLowerCase()) {
		case "efekt":		clazz = MechanikaEfekt.class; break;
		case "trwałyefekt": clazz = MechanikaTrwałyEfekt.class; break;
		case "atrybut":		clazz = MechanikaAtrybut.class; break;
		default: throw new IllegalArgumentException("Niepoprawny rodzaj mechaniki " + nazwa + ": " + rodzaj);
		}
		
		try {
			return clazz.getDeclaredConstructor(String.class, LepszaMapa.class).newInstance(nazwa, lmapa);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Nie udało się wczytać mechaniki " + nazwa + " \n" + e.getMessage());
		} catch (Throwable e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Nie udało się wczytać mechaniki " + nazwa);
		}
		
	}
	private KamieńDusz wczytajKamień(String nazwa, Map<String, Object> mapa) {
		LepszaMapa<String> lmap = new LepszaMapa<>(mapa);
		
		ItemStack item = lmap.getItemStack("item");
		
		Slot slot = Func.StringToEnum(Slot.class, lmap.get("slot", "WSZYSTKO"));

		List<String> lista = lmap.getList("mechaniki");
		Mechanika[] mechaniki = new Mechanika[lista.size()];
		for (int i=0; i < mechaniki.length; i++) {
			Mechanika mechanika = KamienieDusz.mechaniki.get(lista.get(i));
			if (mechanika == null)
				throw new IllegalArgumentException("Nie odnaleziono mechaniki \"" + lista.get(i) + "\" z Kamienia \"" + nazwa + "\"");
			mechaniki[i] = mechanika;
		}
		
		KamieńDusz kamień = new KamieńDusz(nazwa, slot, mechaniki, item);
		kamienieZItemów.put(item, kamień);
		return kamień;
	}

	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Wczytane Kamienie Dusz", kamienie.size());
	}

	
	
	@Override
	public int czas() {
		Bukkit.getOnlinePlayers().forEach(p -> wykonaj(p, p, Mechanika::czas));
		return 5 * 20;
	}
}
