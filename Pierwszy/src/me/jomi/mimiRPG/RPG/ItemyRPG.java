package me.jomi.mimiRPG.RPG;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_16_R2.util.CraftNamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.Lists;

import net.minecraft.server.v1_16_R2.NBTTagCompound;

import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.Edytory.EdytorOgólny;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Funkcje.TriConsumer;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;

// TODO bedrock useless
// TODO flint useless

// TODO handlarz narkotykami

@Moduł
public class ItemyRPG extends Komenda implements Listener, Przeładowalny {
	public static class Rozwój extends Mapowany {
		public static class Lvl extends Mapowany {
			static abstract class Bonus extends Mapowany {
				public abstract void dodajDoItemu(ItemMeta meta);
			}
			public static class BonusAttr extends Bonus {
				@Mapowane Attribute atrybut;
				@Mapowane double atrybutWartość;
				@Mapowane Operation sposóbDodania;
				
				@Override
				public void dodajDoItemu(ItemMeta meta) {
					meta.addAttributeModifier(atrybut, new AttributeModifier("mimi" + atrybut + atrybutWartość + sposóbDodania, atrybutWartość, sposóbDodania));
				}
				
			}
			public static class BonusEnch extends Bonus {
				public Enchantment ench;
				@Mapowane private String enchant;
				@Mapowane int enchantLvl = 1;
				
				
				@Override
				protected void Init() {
					if (enchant != null)
						ench = Enchantment.getByKey(CraftNamespacedKey.fromString(enchant));
				}
				
				@Override
				public void dodajDoItemu(ItemMeta meta) {
					meta.addEnchant(ench, enchantLvl, false);
				}
			}
			
			@Mapowane double potrzebnyExp; // na następny lvl
			@Mapowane List<BonusAttr> bonusyAttr;
			@Mapowane List<BonusEnch> bonusyEnch;
			@Mapowane List<ItemFlag> flagi;
			@Mapowane Integer customModelData;
			@Mapowane String nazwa;
			@Mapowane Boolean unbreakable;
			@Mapowane(nieTwórz = true) Material nowyTyp;
			
			public void dodajDoItemu(ItemStack item) {
				ItemMeta meta = item.getItemMeta();
				bonusyEnch.forEach(bonus -> bonus.dodajDoItemu(meta));
				bonusyAttr.forEach(bonus -> bonus.dodajDoItemu(meta));
				if (nazwa != null)
					meta.setDisplayName(Func.koloruj(nazwa));
				flagi.forEach(meta::addItemFlags);
				Func.wykonajDlaNieNull(unbreakable, meta::setUnbreakable);
				Func.wykonajDlaNieNull(customModelData, meta::setCustomModelData);
				Func.wykonajDlaNieNull(nowyTyp, item::setType);
				item.setItemMeta(meta);
			}
		}

		
		public ItemStack stwórzNowy() {
			net.minecraft.server.v1_16_R2.ItemStack item = CraftItemStack.asNMSCopy(podstawowyItem);
			
			NBTTagCompound tag = new NBTTagCompound();
			tag.setString("rozwoj", id);
			tag.setDouble("exp", 0);
			tag.setInt("lvl", lvle.isEmpty() ? -1 : 0);
			
			NBTTagCompound tagItemu = item.getOrCreateTag();
			tagItemu.set("mimiItemRPG", tag);
			
			item.setTag(tagItemu);
			
			ItemStack bukkitItem = CraftItemStack.asBukkitCopy(item);
			
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
	
	public ItemyRPG() {
		super("itemrpg", "/itemrpg <itemrpg> (gracz)");
		Main.dodajPermisje(permEdytor);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void zabijanie(EntityDeathEvent ev) {
		Func.wykonajDlaNieNull(ev.getEntity().getKiller(), p -> {
			p.getInventory().getItemInMainHand();
		});
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void kopanie(BlockBreakEvent ev) throws Throwable {
		podexp(ev.getPlayer(), ev.getPlayer().getInventory().getItemInMainHand(), () -> 2d);
	}
	
	//{mimiItemRPG:{rozwoj: String, lvl: int, exp: double}}

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
			Func.wykonajDlaNieNull((net.minecraft.server.v1_16_R2.ItemStack) Func.dajField(CraftItemStack.class, "handle").get(item), nmsItem -> {
				NBTTagCompound tag = nmsItem.getOrCreateTag().getCompound("mimiItemRPG");
				if (!tag.isEmpty()) {
					double ile = supIle.get();
					if (ile != 0) {
						Func.wykonajDlaNieNull(configRozwoje.wczytaj(tag.getString("rozwoj")), Rozwój.class, rozwoj -> {
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

	final static Config configRozwoje = new Config("configi/ItemyRPG");
	
	private final static Krotka<Map<Material,   Double>, Double> expZBloków = new Krotka<>(new HashMap<>(), 0d);
	private final static Krotka<Map<EntityType, Double>, Double> expZMobów  = new Krotka<>(new HashMap<>(), 0d);
	@Override
	public void przeładuj() {
		configRozwoje.przeładuj();
		
		Config config = new Config("ItemyRPG exp");
		expZMobów.b  = config.wczytajDouble("domyślne.Moby");
		expZBloków.b = config.wczytajDouble("domyślne.Bloki");
		TriConsumer<Class<?>,  Map<?, Double>, String> przetwórz = (clazz, mapaExpa, sciezka) -> {
			mapaExpa.clear();
			Func.wykonajDlaNieNull(config.sekcja(sciezka), sekcja ->
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
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Wczytane Rozwoje itemów RPG", configRozwoje.klucze(false).size());
	}
	
	EdytorOgólny<Rozwój> edytor = new EdytorOgólny<>("itemrpg", Rozwój.class);
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		switch (args.length) {
		case 0:
		case 1:
			List<String> lista = Lists.newArrayList(configRozwoje.klucze(false));
			if (sender.hasPermission(permEdytor))
				lista.add("edytor");
			return utab(args, lista);
		case 2:
			if (args[0].equalsIgnoreCase("edytor") && sender.hasPermission(permEdytor))
				return utab(args, "-t", "-u");
			break;
		case 3:
			if (args[0].equalsIgnoreCase("edytor") && sender.hasPermission(permEdytor))
				return utab(args, configRozwoje.klucze(false));
		}
		return null;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 1)
			return false;
		if (args[0].equalsIgnoreCase("edytor") && sender.hasPermission(permEdytor)) {
			if (args.length <= 2 && !edytor.maEdytor(sender))
				return Func.powiadom(sender, prefix + "/itemrpg edytor -t <itemrpg>");
			else if (args.length >= 2 && args[1].equals("-t"))
				args[2] = "configi/ItemyRPG|" + args[2];
			return edytor.onCommand(sender, "itemrpg", args);
		}
		
		Player p = null;
		
		if (sender instanceof Player)
			p = (Player) sender;
		
		if (args.length >= 2)
			try {
				p = (Player) Bukkit.selectEntities(sender, args[1]).get(0);
			} catch (Throwable e) {
				return Func.powiadom(sender, prefix + "Niepoprawny gracz %s", args[1]);
			}
		
		if (p == null)
			return Func.powiadom(sender, prefix + "/" + label + " <itemrpg> <nick>");
		
		Player gracz = p;
		Func.wykonajDlaNieNull(configRozwoje.wczytaj(args[0]), Rozwój.class,
				rozwój -> Func.dajItem(gracz, rozwój.stwórzNowy()),
				() -> sender.sendMessage(prefix + "Nieprawidłowy itemrpg: " + args[0])
				);
		
		return true;
	}
}
