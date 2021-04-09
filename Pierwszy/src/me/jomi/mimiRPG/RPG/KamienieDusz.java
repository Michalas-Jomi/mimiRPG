package me.jomi.mimiRPG.RPG;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.Customizacja.CustomoweItemy;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.LepszaMapa;
import me.jomi.mimiRPG.util.MultiConfig;
import me.jomi.mimiRPG.util.Napis;
import me.jomi.mimiRPG.util.PersistentDataTypeCustom;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
public class KamienieDusz implements Przeładowalny, Listener {
	public static final String prefix = Func.prefix(KamienieDusz.class);
	
	static ItemStack pustyKamień = Func.stwórzItem(Material.FIREWORK_STAR, "&8Pusty Kamień Dusz", 1, "&aZużyty kamień", "&aNic już z niego");
	static int maxKamienieNaItem = 2;
	
	public static abstract class Mechanika {
		public final String nazwa;
		
		public Mechanika(String nazwa, LepszaMapa<String> mapa) {
			this.nazwa = nazwa;
			wczytaj(mapa);
		}
		
		public abstract void zaaplikuj(ItemMeta meta);
		public abstract void odaplikuj(ItemMeta meta);
		public abstract void wczytaj(LepszaMapa<String> mapa);
		
		public void onAttack(EntityDamageByEntityEvent ev)	{};
		public void onDamaged(EntityDamageEvent ev)			{};
		public void onBlockBreak(BlockBreakEvent ev)		{};
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
		
		double szansa;
		PotionEffect effekt;
		
		Moment moment;
		Target target;
		
		public MechanikaEfekt(String nazwa, LepszaMapa<String> mapa) {
			super(nazwa, mapa);
		}
		
		@Override public void zaaplikuj(ItemMeta meta) {}
		@Override public void odaplikuj(ItemMeta meta) {}
		
		@Override
		public void wczytaj(LepszaMapa<String> mapa ) {
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
		}
	
		
		private void fabric(Moment moment, Entity self, Entity other) {
			if (this.moment == moment) {
				Entity e = null;
				switch (target) {
				case SELF:	e = self;	break;
				case OTHER:	e = other; 	break;
				}
				
				if (e != null && e instanceof LivingEntity && Func.losuj(szansa))
					((LivingEntity) e).addPotionEffect(effekt);
			}
		}
		
		@Override
		public void onAttack(EntityDamageByEntityEvent ev) {
			fabric(Moment.ATTACK, ev.getDamager(), ev.getEntity());
		}
		@Override
		public void onDamaged(EntityDamageEvent ev) {
			fabric(Moment.DAMAGED, ev.getEntity(), null); // TODO Damaged by entity
		}
		@Override
		public void onBlockBreak(BlockBreakEvent ev) {
			fabric(Moment.BLOCK_BREAK, ev.getPlayer(), null);
		}
	}
 
	public static class MechanikaAtrybut extends Mechanika {
		public MechanikaAtrybut(String nazwa, LepszaMapa<String> mapa) {
			super(nazwa, mapa);
		}
		
		Attribute attr;
		AttributeModifier attrMod;
		
		
		@Override
		public void zaaplikuj(ItemMeta meta) {
			meta.addAttributeModifier(attr, attrMod);
		}
		@Override
		public void odaplikuj(ItemMeta meta) {
			meta.removeAttributeModifier(attr, attrMod);
		}
		@Override
		public void wczytaj(LepszaMapa<String> mapa) {
			attr = Func.StringToEnum(Attribute.class, mapa.getString("atrybut"));
			attrMod = new AttributeModifier(
					getUUID(),
					null,
					mapa.getDouble("wartość"),
					Func.StringToEnum(AttributeModifier.Operation.class, mapa.get("sposób", "ADD_NUMBER")),
					Func.StringToEnum(EquipmentSlot.class, mapa.get("slot", "HAND"))
					);
		}
		private UUID getUUID() {
			String sc = "mechanika.atrybut.uuid." + nazwa;
			
			String uuid = configDane.wczytajStr(sc);
			if (uuid == null) {
				uuid = UUID.randomUUID().toString();
				configDane.ustaw_zapisz(sc, uuid);
			}
			
			return UUID.fromString(uuid);
		}
	}
	
	public static enum Slot {
		ARMOR(item -> EnchantmentTarget.ARMOR.includes(item)),
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
		private static final NamespacedKey kluczKamieni = new NamespacedKey(Main.plugin, "kamieniedusz");
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

		public void zaaplikuj(ItemMeta meta) {
			for (int i=0; i < mechaniki.length; i++)
				mechaniki[i].zaaplikuj(meta);
			
			String[] stare = nałożone(meta);
			String[] nowe = new String[stare.length + 1];
			int i = -1;
			while (++i < stare.length)
				nowe[i] = stare[i];
			
			nowe[i] = nazwa;
			
			meta.getPersistentDataContainer().set(kluczKamieni, PersistentDataTypeCustom.StringArray, nowe);
			
			List<String> lore = meta.hasLore() ? Func.nieNull(meta.getLore()) : new ArrayList<>();
			
			if (lore.isEmpty() || !lore.get(0).equals(linia0)) {
				lore.add(0, linia0);
				lore.add(1, " ");
			}
			lore.add(1, "§8§l- §d" + Func.koloruj(nazwa));
			
			meta.setLore(lore);
			
		}
		public boolean możnaNałożyć(ItemStack item) {
			return	slot.możnaZałożyć(item) && 
					nałożone(item.getItemMeta()).length < maxKamienieNaItem;
		}
		
		private static String[] nałożone(ItemMeta meta) {
			if (meta == null) return new String[0];
			String[] array = meta.getPersistentDataContainer().get(kluczKamieni, PersistentDataTypeCustom.StringArray);
			return array == null ? new String[0] : array;
		}
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
	
	private void wykonaj(PlayerInventory inv, Consumer<KamieńDusz> cons) {
		wczytaj(inv.getItemInMainHand()).forEach(cons);
		wczytaj(inv.getItemInOffHand()).forEach(cons);
		wczytaj(inv.getHelmet()).forEach(cons);
		wczytaj(inv.getChestplate()).forEach(cons);
		wczytaj(inv.getLeggings()).forEach(cons);
		wczytaj(inv.getBoots()).forEach(cons);
	}
	private <E extends Event> void wykonaj(E ev, Entity e, BiConsumer<Mechanika, E> bic) {
		if (e instanceof Player && (!(ev instanceof Cancellable) || !((Cancellable) ev).isCancelled()))
			wykonaj(((Player) e).getInventory(), kamień -> Func.forEach(kamień.mechaniki, mechanika -> bic.accept(mechanika, ev)));
	}
	@EventHandler(priority = EventPriority.MONITOR) public void otrzymywanieDmg	(EntityDamageEvent ev) 			{wykonaj(ev, ev.getEntity(),  Mechanika::onDamaged);}
	@EventHandler(priority = EventPriority.MONITOR) public void zadanieDmg		(EntityDamageByEntityEvent ev)	{wykonaj(ev, ev.getDamager(), Mechanika::onAttack);}
	@EventHandler(priority = EventPriority.MONITOR) public void kopanie			(BlockBreakEvent ev)			{wykonaj(ev, ev.getPlayer(),  Mechanika::onBlockBreak);}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void klikanieEq(InventoryClickEvent ev) {
		if (!ev.getClick().equals(ClickType.RIGHT)) return;
		if (ev.getCurrentItem() == null) return;
		Func.wykonajDlaNieNull(rozpoznaj(ev.getCursor()), kamień -> {
			if (!kamień.możnaNałożyć(ev.getCurrentItem()))
				return;
			
			ItemMeta meta = ev.getCurrentItem().getItemMeta();
			
			kamień.zaaplikuj(meta);
			ev.getCurrentItem().setItemMeta(meta);
			
			ev.setCancelled(true);
			
			ev.getCursor().setType(pustyKamień.getType());
			ev.getCursor().setAmount(pustyKamień.getAmount());
			ev.getCursor().setItemMeta(pustyKamień.getItemMeta());
			
			Main.log(Func.msg(prefix + "%s nałożył kamień %s na %s", ev.getWhoClicked().getName(), kamień.nazwa, Napis.item(ev.getCurrentItem()).toString()));
		});
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
				Main.log(prefix + e.getMessage());
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
		case "efekt":	clazz = MechanikaEfekt.class;	break;
		case "atrybut":	clazz = MechanikaAtrybut.class;	break;
		default:		throw new IllegalArgumentException("Niepoprawny rodzaj mechaniki " + nazwa + ": " + rodzaj);
		}
		
		try {
			return clazz.getDeclaredConstructor(String.class, LepszaMapa.class).newInstance(nazwa, lmapa);
		} catch (Throwable e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Nie udało się wczytaj mechaniki " + nazwa);
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
}
