package me.jomi.mimiRPG.Edytory;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;

@Moduł
public class EdytujItem extends Komenda implements Listener {
	public EdytujItem() {
	    super("edytujitem", prefix + "/edytujItem (nazwa | lore | enchant | modifire | unbreakable | ukryj)", "ei");
	}
	public static String prefix = Func.prefix("Edytuj Item");
	
	public static List<String> enchanty = Arrays.asList("Aqua_Affinity", "Bane_of_Arthropods", "Blast_Protection", "Channeling", "Curse_of_Binding", "Curse_of_Vanishing", "Depth_Strider", "Efficiency", "Feather_Falling", "Fire_Aspect", "Fire_Protection", "Flame", "Fortune", "Frost_Walker", "Impaling", "Infinity", "Knockback", "Looting", "Loyalty", "Luck_of_the_Sea", "Lure", "Mending", "Multishot", "Piercing", "Power", "Projectile_Protection", "Protection", "Punch", "Quick_Charge", "Respiration", "Riptide", "Sharpness", "Silk_Touch", "Smite", "Sweeping_Edge", "Thorns", "Unbreaking");
	public static List<String> flagi 	= Arrays.asList("atrybuty", "unbreakable", "efekty", "enchanty");
	public static List<String> atrybuty = Arrays.asList("armor", "armor_toughness", "attack_damage", "attack_speed", "knockback_resistance", "luck", "max_health", "movement_speed", "flying_speed", "horse_jump_strength");
	public static List<String> sloty	= Arrays.asList("ręka", "druga_ręka", "głowa", "klata", "nogi", "stopy" );
	
	public static void dodajTekst(TextComponent doCzego, Action akcja, String text, String komenda) {
		TextComponent e = new TextComponent(text);
		e.setClickEvent(new ClickEvent(akcja, komenda));
		doCzego.addExtra(e);
	}
	
	private static boolean edytor(Player p, ItemStack item, String[] args) {
		String klucz;
		String kolor;
		TextComponent msg;
		ItemMeta meta = item.getItemMeta().clone();
		if (args.length < 2) return false;
		boolean odśwież = false;
		switch(args[1]) {
		case "modifire":
			msg = new TextComponent("\n\n\n\n\n\n§5§lModifire Atrybutów\n");
			if (args.length < 3) klucz = "ręka";
			else 				 klucz = args[2];
			
			if (args.length == 5) {
				p.chat("/edytujitem modifire " + args[3] + " " + args[2] + " " + args[4]);
				p.chat("/edytujitem edytorEdytuje modifire " + klucz);
				return true;
			}

			Multimap<Attribute, AttributeModifier> mapa = meta.getAttributeModifiers(Slot.valueOf(klucz).slot);
			for (String at : atrybuty) {
				String w = "";
				String plus = "";
				Attribute attr = dajAtrybut(at);
				if (mapa.containsKey(attr)) {
					AttributeModifier a = (AttributeModifier) mapa.get(attr).toArray()[0];
					double wartość = a.getAmount();
					w = Func.DoubleToString(wartość);
					if (wartość > 0)
						plus = "+";
					if (a.getOperation().equals(Operation.ADD_SCALAR)) {
						w = Func.DoubleToString(wartość*100) + "%";
					}
				}
				dodajTekst(msg, Action.SUGGEST_COMMAND, "§6§l-> §d" + at + " §a" + plus + w + "\n", "/edytujitem edytorEdytuje modifire " + klucz + " " + at + " ");
			}
			
			msg.addExtra("\n");
			
			for (String slot : sloty) {
				kolor = "§6";
				if (slot.equals(klucz))
					kolor = "§e";
				dodajTekst(msg, Action.RUN_COMMAND, kolor + "[" + slot + "] ", "/edytujitem edytorEdytuje modifire " + slot);
			}

			dodajTekst(msg, Action.RUN_COMMAND, "§6[←]\n", "/edytujitem e");
			
			p.spigot().sendMessage(msg);
			break;
		case "enchant":
			if (args.length >= 5 && args[2].equals(">>")) {
				p.chat("/edytujitem enchant " + args[3] + " " + args[4]);
				p.chat("/edytujitem edytorEdytuje enchant");
				return true;
			}
			msg = new TextComponent("\n\n§5§lEnchanty\n");
			boolean b = false;
			for (String nazwa : enchanty) {
				kolor = "§8";
				if (b) kolor = "§7";
				b = !b;
				String lvl = "";
				Enchantment enchant = dajEnchant(nazwa);
				if (meta.hasEnchant(enchant))
					lvl = "§a(" + meta.getEnchantLevel(enchant) + ")";
				dodajTekst(msg, Action.SUGGEST_COMMAND, kolor + nazwa  + lvl + " ", "/edytujitem edytorEdytuje enchant >> " + nazwa + " ");
				if (!b) msg.addExtra("\n");
			}
			dodajTekst(msg, Action.RUN_COMMAND, " §6[←]", "/edytujitem e");
			p.spigot().sendMessage(msg);
			break;
		case "lore":
			if (args.length < 3) klucz = "ustaw";
			else 				 klucz = args[2];
			switch(klucz) {
			case "usuwanie":
				if (args.length < 4) return false;
				p.chat("/edytujitem lore usuń " + args[3]);
				p.chat("/edytujitem edytorEdytuje lore usuń");
				return true;
			case "ustaw>>":
				if (args.length < 5) return false;
				p.chat("/edytujitem lore ustaw " + Func.listToString(args, 3));
				p.chat("/edytujitem edytorEdytuje lore ustaw");
				return true;
			case "wstaw>>":
				if (args.length < 5) return false;
				p.chat("/edytujitem lore wstaw " + Func.listToString(args, 3));
				p.chat("/edytujitem edytorEdytuje lore wstaw");
				return true;
			case "dodaj>>":
				if (args.length < 4) return false;
				p.chat("/edytujitem lore dodaj " + Func.listToString(args, 3));
				p.chat("/edytujitem edytorEdytuje lore");
				return true;
			}
			msg = new TextComponent("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n§5§lLore\n");
			if (meta.hasLore()) {
				List<String> lore = meta.getLore();
				for (int i=0; i<meta.getLore().size(); i++)
					switch (klucz) {
					case "ustaw":
						dodajTekst(msg, Action.SUGGEST_COMMAND, "§e§l- §5" + lore.get(i) + "\n", "/edytujitem edytorEdytuje lore ustaw>> " + (i + 1) + " " + lore.get(i).replace("§", "&"));
						break;
					case "usuń":
						dodajTekst(msg, Action.RUN_COMMAND, "§e§l- §5" + lore.get(i) + "\n", "/edytujitem edytorEdytuje lore usuwanie " + (i + 1));
						break;
					case "wstaw":
						dodajTekst(msg, Action.SUGGEST_COMMAND, "§e§l- §5" + lore.get(i) + "\n", "/edytujitem edytorEdytuje lore wstaw>> " + (i + 1) + " ");
						break;
					}
				msg.addExtra("\n");
				for (String nazwa : Arrays.asList("ustaw", "wstaw", "usuń")) {
					kolor = "§6";
					if (klucz.equals(nazwa))
						kolor = "§e";
					dodajTekst(msg, Action.RUN_COMMAND, kolor + "[" + nazwa + "] ", "/edytujitem edytorEdytuje lore " + nazwa);
				}
			}
			dodajTekst(msg, Action.SUGGEST_COMMAND, "§6[dodaj] ", "/edytujitem edytorEdytuje lore dodaj>> ");
			dodajTekst(msg, Action.RUN_COMMAND, "§6[←]\n", "/edytujitem e");
			
			p.spigot().sendMessage(msg);
			break;
		case "ukryj":
			if (args.length < 3) return false;
			ItemFlag flaga = dajFlage(args[2]);
			if (flaga == null) return false;
			if (meta.hasItemFlag(flaga)) meta.removeItemFlags(flaga);
			else 						 meta.addItemFlags(flaga);
			item.setItemMeta(meta);
			odśwież = true;
			break;
		case "unbreakable":
			meta.setUnbreakable(!meta.isUnbreakable());
			item.setItemMeta(meta);
			odśwież = true;
			break;
		}
		
		if (odśwież)
			Bukkit.getScheduler().scheduleSyncDelayedTask(Main.plugin, new Runnable() {
			    public void run() {
					p.chat("/edytujitem e");
			    }
			}, 1);
		return true;
	}
	private static boolean ustaw_mete(Player p, ItemMeta meta, String[] args) {
		TextComponent msg;
		switch (args[0]) {
		case "e":
		case "edytor":
			msg = new TextComponent("\n\n\n\n\n\n\n\n\n\n" + prefix + "§a§l ~~§aEdytor~~\n\n");
			String kolor;
			msg.addExtra("§6Ukryj: ");
			for (String flaga : flagi) {
				kolor = "§c";
				if (meta.hasItemFlag(dajFlage(flaga)))
					kolor = "§a";
				dodajTekst(msg, Action.RUN_COMMAND, "\n§e§l- " + kolor + flaga, "/edytujitem edytorEdytuje ukryj " + flaga);
			}
			msg.addExtra("\n\n");
			
			dodajTekst(msg, Action.SUGGEST_COMMAND, "§e[nazwa] ", "/edytujitem nazwa " + (meta.hasDisplayName() ? Func.odkoloruj(meta.getDisplayName()) : ""));
			
			for (String nazwa : Arrays.asList("enchant", "lore", "modifire"))
				dodajTekst(msg, Action.RUN_COMMAND, "§e[" + nazwa + "] ", "/edytujitem edytorEdytuje " + nazwa);

			kolor = "§c";
			if (meta.isUnbreakable())
				kolor = "§a";
			dodajTekst(msg, Action.RUN_COMMAND, kolor + "[Unbreakable]", "/edytujitem edytorEdytuje unbreakable");
			
			
			msg.addExtra("\n");
			
			p.spigot().sendMessage(msg);
			return true;
		case "nazwa":
			if (args.length < 2)
				return powiadom(p, "/edytujItem nazwa <nazwa>");
			meta.setDisplayName(Func.koloruj(Func.listToString(args, 1)));
			p.sendMessage(prefix + "Zmieniono nazwę przedmiotu");
			return true;
		case "lore":
			if (args.length < 2 || !(args[1].equals("dodaj") || args[1].equals("ustaw") || args[1].equals("usuń") || args[1].equals("wstaw"))) 
				return powiadom(p, "/edytujItem lore (dodaj | usuń | ustaw | wstaw)");
			
			List<String> lore;
			if (meta.hasLore()) lore = meta.getLore();
			else 				lore = Lists.newArrayList();
			
			if (args[1].equalsIgnoreCase("dodaj")) {
				if (args.length < 3)
					return powiadom(p, "/edytujItem lore dodaj <text>");
				lore.add(Func.koloruj(Func.listToString(args, 2)));
			}
			else if (args[1].equals("usuń") || args[1].equals("ustaw") || args[1].equals("wstaw")) {
				if (args.length < 3 || (args.length < 4 && (args[1].equals("ustaw") || args[1].equals("wstaw")))) {
					if (args[1].equals("usuń"))
						return powiadom(p, "/edytujItem lore usuń <nr lini>");
					return powiadom(p, "/edytujItem lore ustaw <nr lini> <text>");
				}
				int i = Func.Int(args[2], -1);
				if (i == -1)
					return powiadom(p, args[2] + " nie jest poprawną liczbą");
				if (i > lore.size() && !args[1].equals("wstaw"))
					return powiadom(p, "Ten przedmiot posiada tylko " + lore.size() + " lini lore");
				if (args[1].equals("usuń"))		  lore.remove(i-1);
				else if (args[1].equals("ustaw")) lore.set(i-1, Func.koloruj(Func.listToString(args, 3)));
				else {
					List<String> nowyLore = Lists.newArrayList(); 
					for (int j=0; j<lore.size(); j++) {
						if (j == i-1)
							nowyLore.add(Func.koloruj(Func.listToString(args, 3)));
						nowyLore.add(lore.get(j));
					}
					lore = nowyLore;
				}
			}
			meta.setLore(lore);
			p.sendMessage(prefix + "Zmodyfikowano lore");
			return true;
		case "enchant":
			if (args.length < 2)
				return powiadom(p, "/edytujItem enchant <nazwa enchantu> [lvl]");
			Enchantment enchant = dajEnchant(args[1]);
			if (enchant == null)
				return powiadom(p, "Niepoprawna nazwa enchantu: " + args[1]);
			int lvl = enchant.getMaxLevel();
			if (args.length >= 3)
				lvl = Func.Int(args[2], 0);
			if (lvl == 0) meta.removeEnchant(enchant);
			else		  meta.addEnchant(enchant, lvl, true);
			p.sendMessage(prefix + "Dodano enchant §e" + args[1] + " " + Func.IntToString(lvl));
			return true;
		case "unbreakable":
			if (args.length < 2)
				return powiadom(p, "/edytujItem unbreakable (tak | nie)");
			meta.setUnbreakable(!args[1].equals("nie"));
			if (!args[1].equals("nie")) p.sendMessage(prefix + "Ustawiono przedmiot na niezniszczalny");
			else					  p.sendMessage(prefix + "Ustawiono przedmiot na zniszczalny");
			return true;
		case "ukryj":
			if (args.length < 3)
				return powiadom(p, "/edytujItem ukryj <id> (tak | nie)");
			ItemFlag flag = dajFlage(args[1]);
			if (flag == null)
				powiadom(p, "Niepoprawne id: " + args[1]);
			if (args[2].equalsIgnoreCase("nie")) meta.removeItemFlags(flag);
			else meta.addItemFlags(flag);
			p.sendMessage(prefix + "Ukryto " + args[1]);
			return true;
		case "modifire":
			if (args.length < 4)
				return powiadom(p, "/edytujItem modifire <atrybut> <slot> <wartość>[%]");
			Attribute attr = dajAtrybut(args[1]);
			if (attr == null)
				return powiadom(p, "Nieprawidłowa nazwa atrybutu: " + args[1]);
			
			boolean b = false;
			for (Slot s : Slot.values())
				if (s.toString().equals(args[2]))
					b = true;
			if (!b)
				return powiadom(p, "Nieprawidłowy slot: " + args[2]);
			double liczba;
			Operation operacja;
			try {
				String str = args[3];
				if (str.charAt(str.length()-1) == '%') {
					liczba = Double.parseDouble(str.substring(0, str.length()-1));
					liczba /= 100;
					operacja = Operation.ADD_SCALAR;
				}
				else {
					liczba = Double.parseDouble(str);
					operacja = Operation.ADD_NUMBER;
				}
			}catch(NumberFormatException nfe) {
				return powiadom(p, "§e" + args[3] + "§6 nie jest prawidłową liczbą");
			}
			if (meta.hasAttributeModifiers()) 
				meta.removeAttributeModifier(attr);
			AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), args[1], liczba, operacja, Slot.valueOf(args[2]).slot);
			meta.addAttributeModifier(attr, modifier);
			p.sendMessage(prefix + "Dodano modifire");
			return true;
		}
		return false;
	}
	
	private static boolean powiadom(Player p, String msg) {
		p.sendMessage(prefix + msg);
		return false;
	}
	private static ItemFlag dajFlage(String nazwa) {
		switch (nazwa) {
		case "atrybuty": 	return ItemFlag.HIDE_ATTRIBUTES;
		case "efekty": 		return ItemFlag.HIDE_POTION_EFFECTS;
		case "enchanty": 	return ItemFlag.HIDE_ENCHANTS;
		case "unbreakable": return ItemFlag.HIDE_UNBREAKABLE;
		}
		return null;
	}
	private static Enchantment dajEnchant(String nazwa) {
		switch (nazwa) {
		case "Aqua_Affinity": 			return Enchantment.WATER_WORKER;
		case "Bane_of_Arthropods": 		return Enchantment.DAMAGE_ARTHROPODS;
		case "Blast_Protection": 		return Enchantment.PROTECTION_EXPLOSIONS;
		case "Channeling": 				return Enchantment.CHANNELING;
		case "Curse_of_Binding": 		return Enchantment.BINDING_CURSE;
		case "Curse_of_Vanishing": 		return Enchantment.VANISHING_CURSE;
		case "Depth_Strider": 			return Enchantment.DEPTH_STRIDER;
		case "Efficiency": 				return Enchantment.DIG_SPEED;
		case "Feather_Falling": 		return Enchantment.PROTECTION_FALL;
		case "Fire_Aspect": 			return Enchantment.FIRE_ASPECT;
		case "Fire_Protection": 		return Enchantment.PROTECTION_FIRE;
		case "Flame": 					return Enchantment.ARROW_FIRE;
		case "Fortune": 				return Enchantment.LOOT_BONUS_BLOCKS;
		case "Frost_Walker": 			return Enchantment.FROST_WALKER;
		case "Impaling": 				return Enchantment.IMPALING;
		case "Infinity": 				return Enchantment.ARROW_INFINITE;
		case "Knockback": 				return Enchantment.KNOCKBACK;
		case "Looting": 				return Enchantment.LOOT_BONUS_MOBS;
		case "Loyalty": 				return Enchantment.LOYALTY;
		case "Luck_of_the_Sea": 		return Enchantment.LUCK;
		case "Lure": 					return Enchantment.LURE;
		case "Mending": 				return Enchantment.MENDING;
		case "Multishot": 				return Enchantment.MULTISHOT;
		case "Piercing": 				return Enchantment.PIERCING;
		case "Power": 					return Enchantment.ARROW_DAMAGE;
		case "Projectile_Protection": 	return Enchantment.PROTECTION_PROJECTILE;
		case "Protection": 				return Enchantment.PROTECTION_ENVIRONMENTAL;
		case "Punch": 					return Enchantment.ARROW_KNOCKBACK;
		case "Quick_Charge": 			return Enchantment.QUICK_CHARGE;
		case "Respiration": 			return Enchantment.OXYGEN;
		case "Riptide": 				return Enchantment.RIPTIDE;
		case "Sharpness": 				return Enchantment.DAMAGE_ALL;
		case "Silk_Touch":		 		return Enchantment.SILK_TOUCH;
		case "Smite": 					return Enchantment.DAMAGE_UNDEAD;
		case "Sweeping_Edge":			return Enchantment.SWEEPING_EDGE;
		case "Thorns": 					return Enchantment.THORNS;
		case "Unbreaking": 				return Enchantment.DURABILITY;
		}
		return null;
	}
	private static Attribute dajAtrybut(String nazwa) {
		switch(nazwa) {
		case "armor": 					return Attribute.GENERIC_ARMOR;
		case "armor_toughness": 		return Attribute.GENERIC_ARMOR_TOUGHNESS;
		case "attack_damage": 			return Attribute.GENERIC_ATTACK_DAMAGE;
		case "attack_speed": 			return Attribute.GENERIC_ATTACK_SPEED;
		case "knockback_resistance": 	return Attribute.GENERIC_KNOCKBACK_RESISTANCE;
		case "luck": 					return Attribute.GENERIC_LUCK;
		case "max_health": 				return Attribute.GENERIC_MAX_HEALTH;
		case "movement_speed": 			return Attribute.GENERIC_MOVEMENT_SPEED;
		case "flying_speed": 			return Attribute.GENERIC_FLYING_SPEED;
		case "horse_jump_strength": 	return Attribute.HORSE_JUMP_STRENGTH;
		}
		return null;
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
	
	@EventHandler(priority = EventPriority.LOW)
	public void onCommandPreProcess(PlayerCommandPreprocessEvent ev) {
		Command cmd = Main.plugin.getCommand("edytujitem");
		if (!ev.getPlayer().hasPermission(cmd.getPermission()))
			return;
		String msg = ev.getMessage().substring(1);
		List<String> nazwy = cmd.getAliases();
		nazwy.add(cmd.getName());
		for (String nazwa : nazwy)
			if (msg.startsWith(nazwa)) {
				ev.setCancelled(true);
				onCommand(ev.getPlayer(), cmd, nazwa, msg.substring(nazwa.length()+1).split(" "));
				return;
			}
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> lista = null;
		if (args.length <= 1)
			lista = Arrays.asList("edytor", "nazwa", "lore", "enchant", "modifire", "unbreakable", "ukryj");
		else if (args.length == 2)
			switch(args[0]) {
			case "ukryj": 		lista = flagi; break;
			case "enchant": 	lista = enchanty; break;
			case "modifire": 	lista = atrybuty; break;
			case "unbreakable": lista = Arrays.asList("tak", "nie"); break;
			case "lore": 		lista = Arrays.asList("dodaj", "usuń", "ustaw", "wstaw"); break;
			}
		else if (args.length == 3 && args[0].equals("modifire"))
			lista = sloty;
		if (lista == null)
			return null;
		return uzupełnijTabComplete(Func.ostatni(args), lista);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player))
			return Func.powiadom(sender, prefix + "Tylko gracz może tego użyć");
		Player p = (Player) sender;
		
		if (args.length < 1) return false;
		
		ItemStack item = p.getInventory().getItem(p.getInventory().getHeldItemSlot());
		if (item == null) {
			p.sendMessage(prefix + "Musisz trzymać coś w ręce aby tego użyć");
			return true;
		}
		ItemMeta meta = item.getItemMeta();

		if (args[0].equals("edytorEdytuje"))
				return edytor(p, item, args);
		if (!ustaw_mete(p, meta, args)) return true;
		item.setItemMeta(meta);
		
		return true;
	}
}
