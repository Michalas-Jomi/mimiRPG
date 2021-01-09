package me.jomi.mimiRPG.Edytory;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.chat.ClickEvent.Action;

import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Napis;

@Moduł
public class EdytujItem extends Komenda {
	public static String prefix = Func.prefix("Edytuj Item");
	static class Edytor {
		static void główna(Player p, ItemMeta meta) {
			Napis n = new Napis("\n\n\n\n\n" + prefix + " §a~~Edytor~~\n");
			
			n.dodaj("§6Ukryj:");
			for (ItemFlag flaga : ItemFlag.values()) {
				n.dodaj("\n§e§l- ");
				n.dodaj(new Napis(
						"§" + (meta.hasItemFlag(flaga) ? "a" : "c") + flaga.toString().toLowerCase().substring(5),
						"§bKliknij aby zmienić",
						"/edytujitem flagi " + flaga
						));
			}
			
			n.dodaj("\n\n");
			
			opcjaGłówna(n, "nazwa", 		"ustawić",  true, Func.odkoloruj(meta.getDisplayName()), null);
			opcjaGłówna(n, "enchant", 		"edytować", false, null);
			opcjaGłówna(n, "lore", 			"edytować", false, null);
			opcjaGłówna(n, "modifire", 		"edytować", false, null);
			opcjaGłówna(n, "typ", 			"zmienić", true, null);
			opcjaGłówna(n, "unbreakable", 	"zmienić",  false, () -> meta.isUnbreakable() ? "§a" : "§c");
			
			n.dodaj("\n");
			
			n.wyświetl(p);
		}
		private static void opcjaGłówna(Napis n, String co, String hover, boolean suggest, Supplier<String> sup) {
			opcja(n, co, co, hover, suggest, sup);
		}
		private static void opcjaGłówna(Napis n, String co, String hover, boolean suggest, String suggestStr, Supplier<String> sup) {
			opcja(n, co, co, hover, suggest, suggestStr, sup);
		}
		private static void opcja(Napis n, String czynność, String co, String hover, boolean suggest, Supplier<String> sup) {
			opcja(n, czynność, co, hover, suggest, "", sup);
		}
		private static void opcja(Napis n, String czynność, String co, String hover, boolean suggest, String suggestStr, Supplier<String> sup) {
			n.dodaj(new Napis(
					(sup == null ? "§e" : sup.get()) + "[" + czynność + "]",
					"§bKliknij aby " + hover,
					suggest ? Action.SUGGEST_COMMAND : Action.RUN_COMMAND,
					"/edytujitem " + co + (suggest ? " >> " : "") + suggestStr
					));
			n.dodaj(" ");
		}
		
		static void lore(Player p, ItemMeta meta, String akt) {
			Napis n = new Napis("\n\n\n\n\n\n\n\n§5§lLore\n");
		
			List<String> lore = Func.nieNull(meta.getLore());
			if (!lore.isEmpty()) {
				for (int i=0; i<lore.size(); i++)
					n.dodaj(new Napis(
							"\n§e- §5" + lore.get(i),
							"§b" + akt,
							akt.equals("usuń") ? Action.RUN_COMMAND : Action.SUGGEST_COMMAND,
							"/edytujitem lore " + i + " " + akt + " >> " + (akt.equals("ustaw") ? Func.odkoloruj(lore.get(i)) : "")
							));
				n.dodaj("\n\n");
				for (String czynność : new String[] {"ustaw", "wstaw", "usuń"})
					opcja(n, czynność, "lore " + czynność, "wybrać", false, () -> czynność.equals(akt) ? "§e" : "§6");
			}
			opcja(n, "dodaj", "lore dodaj", "dodać nową linie", true, () -> "§6");
			opcja(n, "←", "", "powrócić", false, () -> "§6");
			n.dodaj("\n");
			
			n.wyświetl(p);
		}
		
		static void modifire(Player p, ItemMeta meta, Slot slot) {
			Napis n = new Napis("\n\n\n\n\n\n\n\n§5§lLore\n");
		
			UnaryOperator<String> wyraz = str -> str.startsWith("generic_") ? str.substring(8) : str;
			
			Function<Attribute, String> atrybut = attr -> {
				try {
					for (AttributeModifier modifire : meta.getAttributeModifiers(attr))
						if (modifire.getSlot().equals(slot.slot)) {
							String kolor = modifire.getAmount() >= 0 ? "§a" : "§c";
							if (modifire.getOperation().equals(Operation.ADD_NUMBER))
								return kolor + Func.DoubleToString(modifire.getAmount());
							else
								return kolor + Func.DoubleToString(modifire.getAmount() * 100) + "%";
						}
				} catch (Throwable e) {}
				return "";
			};
			
			for (Attribute attr : Attribute.values())
				n.dodaj(new Napis(
						"\n§6§l-> §d" + wyraz.apply(attr.toString().toLowerCase()) + " " + atrybut.apply(attr),
						"§bKliknij aby ustawć",
						"/edytujitem modifire " + slot + " " + attr + " >> "
						));
			n.dodaj("\n\n");
			for (Slot _slot : Slot.values())
				opcja(n, _slot.toString(), "modifire " + _slot, "wybrać", false, () -> slot.equals(_slot) ? "§e" : "§6");
			
			opcja(n, "←", "", "powrócić", false, () -> "§6");
			n.dodaj("\n");
			
			n.wyświetl(p);
		}
	
		static void enchanty(Player p, ItemStack item, boolean wszystkie) {
			Napis n = new Napis("\n\n\n\n\n\n§6§lEnchanty\n");
			boolean jasny = false;
			Function<Integer, String> func = lvl -> lvl != 0 ? " §a(" + lvl + ")" : "";
			for (Enchantment enchant : Enchantment.values())
				if (wszystkie || enchant.canEnchantItem(item)) {
					n.dodaj(new Napis(
							(jasny ? "§7" : "§8") + enchant.getKey().getKey().toLowerCase() + 
									func.apply(item.getType().equals(Material.ENCHANTED_BOOK) ? 
((EnchantmentStorageMeta) item.getItemMeta()).getStoredEnchantLevel(enchant) : item.getEnchantmentLevel(enchant)),
							"§bKliknij aby ustawić",
							"/edytujitem enchant" + (wszystkie ? "-w" : "") + " " + enchant.getKey().getKey().toLowerCase() + " >> "
							));
					n.dodaj(jasny ? "\n" : " ");
					jasny = !jasny;
				}
			
			n.dodaj("\n");
			if (wszystkie)
				opcja(n, "pasujące", "enchant", "wyświetlić tylko pasujące enchanty", false, () -> "§6");
			else 
				opcja(n, "wszystkie", "enchant-w", "wyświetlić wszystkie enchanty", false, () -> "§6");
			opcja(n, "←", "", "powrócić", false, () -> "§6");
			n.dodaj("\n");
			n.wyświetl(p);
		}
	}
	private enum Slot{
		ręka(EquipmentSlot.HAND),
		druga_ręka(EquipmentSlot.OFF_HAND),
		głowa(EquipmentSlot.HEAD),
		klata(EquipmentSlot.CHEST),
		nogi(EquipmentSlot.LEGS),
		stopy(EquipmentSlot.FEET);

		public EquipmentSlot slot;
		
		Slot(EquipmentSlot slot) {
			this.slot = slot;
		}
	}
	
	
	public EdytujItem() {
	    super("edytujitem", null, "ei");
	}
	
	
	void edytor(Player p, ItemStack item, String[] args) {
		ItemMeta meta = item.getItemMeta();
		if (args.length > 0)
			switch (args[0]) {
			case "flagi":
				ItemFlag flaga = ItemFlag.valueOf(args[1]);
				if (meta.hasItemFlag(flaga))
					meta.removeItemFlags(flaga);
				else
					meta.addItemFlags(flaga);
				break;
			case "typ":
				try {
					item.setType(Func.StringToEnum(Material.class, Func.listToString(args, 2)));
				} catch (Throwable e) {
					Func.powiadom(p, Func.msg(prefix + "Nie poprawny typ %s", Func.listToString(args, 2)));
					return;
				}
				break;
			case "nazwa":
				meta.setDisplayName(Func.koloruj(Func.listToString(args, 2)));
				break;
			case "enchant":
			case "enchant-w":
				if (args.length != 1) {
					int lvl = Func.Int(args[3], 0);
					Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(args[1]));
					if (meta instanceof EnchantmentStorageMeta) {
						EnchantmentStorageMeta ksiazka = (EnchantmentStorageMeta) meta;
						ksiazka.removeStoredEnchant(enchant);
						if (lvl != 0)
							ksiazka.addStoredEnchant(enchant, lvl, true);
					} else {
						meta.removeEnchant(enchant);
						if (lvl != 0)
							meta.addEnchant(enchant, lvl, true);
					}
				}
				item.setItemMeta(meta);
				Edytor.enchanty(p, item, item.getType().equals(Material.ENCHANTED_BOOK) || args[0].equals("enchant-w"));
				return;
			case "lore":
				if (args.length != 1) {
					List<String> lore = Func.nieNull(meta.getLore());
					switch(args[1]) {
					case "dodaj":
						lore.add(Func.koloruj(Func.listToString(args, 3)));
						break;
					case "ustaw":
					case "wstaw":
					case "usuń":
						Edytor.lore(p, meta, args[1]);
						return;
					default:
						int i = Func.Int(args[1], -1);
						switch(args[2]) {
						case "ustaw":
							lore.set(i, Func.koloruj(Func.listToString(args, 4)));
							break;
						case "wstaw":
							lore.add(i, Func.koloruj(Func.listToString(args, 4)));
							break;
						case "usuń":
							lore.remove(i);
							break;
						}
					}
					meta.setLore(lore);
				}
				item.setItemMeta(meta);
				Edytor.lore(p, meta, "ustaw");
				return;
			case "modifire":
				if (args.length == 1)
					Edytor.modifire(p, meta, Slot.ręka);
				else if (args.length == 2)
					Edytor.modifire(p, meta, Slot.valueOf(args[1]));
				else if (args.length <= 4)
					p.sendMessage(prefix + "Musisz podać jakąś liczbę lub procent");
				else {
					double liczba;
					Operation operacja;
					if (args[4].endsWith("%")) {
						if (args[4].length() == 1) {
							p.sendMessage(prefix + "Musisz podać jakąś liczbę lub procent");
							return;
						}
						liczba = Func.Double(args[4].substring(0, args[4].length() - 1), 0) / 100;
						operacja = Operation.ADD_SCALAR;
					} else {
						liczba = Func.Double(args[4], 0);
						operacja = Operation.ADD_NUMBER;
					}
					for (AttributeModifier attr : meta.getAttributeModifiers(Slot.valueOf(args[1]).slot).get(Attribute.valueOf(args[2])))
						meta.removeAttributeModifier(Attribute.valueOf(args[2]), attr);
					if (liczba != 0)
						meta.addAttributeModifier(Attribute.valueOf(args[2]), new AttributeModifier(UUID.randomUUID(), args[1] + args[2], liczba, operacja, Slot.valueOf(args[1]).slot));
					item.setItemMeta(meta);
					Edytor.modifire(p, meta, Slot.valueOf(args[1]));
				}
				return;
			case "unbreakable":
				meta.setUnbreakable(!meta.isUnbreakable());
				break;
			}
		item.setItemMeta(meta);
		Edytor.główna(p, meta);
	}
	
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player))
			return Func.powiadom(sender, prefix + "Tylko gracz może używać edytora");
		
		Player p = (Player) sender;
		ItemStack item = p.getInventory().getItemInMainHand();
		if (item == null || item.getType().isAir())
			return Func.powiadom(sender, prefix + "Musisz trzymać coś w ręce aby tego użyć");
		
		edytor(p, item, args);
		
		return true;
	}
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
}


