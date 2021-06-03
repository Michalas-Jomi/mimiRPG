package me.jomi.mimiRPG.RPG_Ultra;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.google.common.collect.Lists;

import net.md_5.bungee.api.chat.ClickEvent.Action;

import net.minecraft.server.v1_16_R3.NBTBase;
import net.minecraft.server.v1_16_R3.NBTNumber;
import net.minecraft.server.v1_16_R3.NBTTagCompound;

import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Komenda;
import me.jomi.mimiRPG.util.Krotki.MonoKrotka;
import me.jomi.mimiRPG.util.NMS;
import me.jomi.mimiRPG.util.Napis;

/*
 * mimiFactor : {
 *   ranga: Ranga,
 *   
 *   boosty: {
 *   baza_X: double,
 *   mn_X: double
 *   },
 *   
 *   enchanty: {
 *   Enchant: lvl
 *   }
 *   
 *   id: String
 * }
 */

@Moduł(priorytet = Moduł.Priorytet.NAJWYŻSZY)
public class ZfaktoryzowaneItemy extends Komenda implements Listener {
	public static final String prefix = Func.prefix(ZfaktoryzowaneItemy.class);
	
	private static final Field NBTMap = Func.dajField(NBTTagCompound.class, "map");
	@SuppressWarnings("unchecked")
	private static Map<String, NBTBase> mapa(NBTTagCompound tag) {
		try {
			return (Map<String, NBTBase>) NBTMap.get(tag);
		} catch (Throwable e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void przerób(ItemStack item) {
		przerób(item, null);
	}
	public static void przerób(ItemStack item, String opis) {
		if (item == null) return;
		przerób(item, NMS.nms(item), opis);
	}
	private static void przerób(ItemStack bukkit, net.minecraft.server.v1_16_R3.ItemStack nms, String opis) {
		NBTTagCompound tag = tag(bukkit);
		ItemMeta meta = bukkit.getItemMeta();
		List<String> lore = new ArrayList<>();
		
		Func.tnij(opis, "\\n").forEach(lore::add);
		
		List<Boost> boosty = Boost.getBoosty(tag);
		if (!boosty.isEmpty()) {
			lore.add(" ");
			lore.add("§6§lBonusy:");
			boosty.forEach(boost -> lore.add(boost.toString()));
		}
		
		Map<String, NBTNumber> enchanty = Func.pewnyCast(mapa(Enchant.enchanty(bukkit)));
		if (!enchanty.isEmpty()) {
			lore.add(" ");
			
			boolean ench5  = enchanty.size() < 5;
			boolean ench12 = enchanty.size() < 12;
			AtomicBoolean wLini = new AtomicBoolean(false);
			Func.posortuj(Lists.newArrayList(enchanty.entrySet()), entry -> Func.stringToDouble(entry.getKey())).forEach(entry -> {
				StringBuilder strB = new StringBuilder();
				String enchant = entry.getKey();
				int lvl = entry.getValue().asInt();
				
				if (wLini.get())
					strB.append("§f, ");
				
				strB.append("§9").append(enchant).append(' ').append(Func.rzymskie(lvl)); 
				
				if (!ench12)
					wLini.set(!wLini.get());
				else if (ench5)
					Func.tnij(Enchant.getEnchant(enchant).opis, "\\n").forEach(lore::add);
				
				lore.add(strB.toString());
			});
			
			if (!meta.hasEnchants())
				meta.addEnchant(Enchantment.ARROW_KNOCKBACK, 1, true);
		}
		
		Ranga ranga = Ranga.ranga(tag);
		lore.add(" ");
		lore.add(ranga.toString());
		
		meta.setLore(lore);
		
		bukkit.setItemMeta(meta);
	}

	public static String id(ItemStack item) {
		String id = tag(item).getString("id");
		return id.isEmpty() ? null : id;
	}
	
	private static NBTTagCompound tag(ItemStack item) {
		return NMS.nms(item).getOrCreateTag().getCompound("mimiFactor");
	}
	static void ustawTag(ItemStack item, NBTTagCompound tag) {
		net.minecraft.server.v1_16_R3.ItemStack nms = NMS.nms(item);
		
		boolean miał = nms.hasTag();
		
		NBTTagCompound nbt = nms.getOrCreateTag();
		nbt.set("mimiFactor", tag);
		
		if (!miał)
			nms.setTag(nbt);
	}

	
	public static enum Ranga {
		ZWYCZAJNY("f", ChatColor.WHITE),
		NADZWYCZAJNY("a", ChatColor.GREEN),
		RZADKI("9", ChatColor.BLUE),
		EPICKI("5", ChatColor.DARK_PURPLE),
		LEGENDARNY("6", ChatColor.GOLD),
		MISTYCZNY("c", ChatColor.RED),
		
		EVENTOWY("e", ChatColor.YELLOW);
		
		public final Team team;
		public final String kolor;
		Ranga(String kolor, ChatColor chatColor) {
			this.kolor = (kolor.length() == 13 ? "&%" : "§") + kolor;
			try {
				Scoreboard sc = Bukkit.getScoreboardManager().getMainScoreboard();
				Team team = sc.getTeam("rang" + name());
				if (team == null)
					team = sc.registerNewTeam("rang" + name());
				this.team = team;
			} catch (Throwable e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			this.team.setColor(chatColor);
		}
		
		@Override
		public String toString() {
			return Func.koloruj(kolor + name());
		}
		

		public static Ranga ranga(ItemStack item) {
			return ranga(tag(item));
		}
		static Ranga ranga(NBTTagCompound tag) {
			try {
				return Func.StringToEnum(Ranga.class, tag.getString("ranga"));
			} catch (Throwable e) {
				return Ranga.ZWYCZAJNY;
			}
		}
		public static void ustawRangę(ItemStack item, Ranga ranga) {
			NBTTagCompound tag = tag(item);
			ustawRangę(tag, ranga);
			ustawTag(item, tag);
		}
		static void ustawRangę(NBTTagCompound tag, Ranga ranga) {
			tag.setString("ranga", ranga.name());
		}
	}
	
	public static class Boost {
		public static List<Boost> getBoosty(ItemStack item) {
			return getBoosty(tag(item));
		}
		static List<Boost> getBoosty(NBTTagCompound tag) {
			List<Boost> list = new ArrayList<>();
			
			NBTTagCompound boosty = tag.getCompound("boosty");
			
			boosty.getKeys().forEach(klucz -> list.add(new Boost(klucz, klucz.startsWith("baza_"), boosty.getDouble(klucz))));
			
			return list;
		}
		
		public static double getBoost(ItemStack item, Atrybut attr, boolean baza) {
			return getBoost(tag(item), attr, baza);
		}
		static double getBoost(NBTTagCompound tag, Atrybut attr, boolean baza) {
			if (!tag.hasKey("boosty")) return 0;
			
			NBTTagCompound boosty = tag.getCompound("boosty");
			String klucz = (baza ? "baza" : "mn") + "_" + attr.name();
			
			return boosty.getDouble(klucz);
		}
		
		public static void dodajBoost(ItemStack item, Atrybut attr, double ile) {
			dodajBoost(item, attr, ile, true);
		}
		public static void dodajBoost(ItemStack item, Atrybut attr, double ile, boolean baza) {
			NBTTagCompound tag = tag(item);
			dodajBoost(tag, attr, ile, baza);
			ustawTag(item, tag);
		}
		static void dodajBoost(NBTTagCompound tag, Atrybut attr, double ile, boolean baza) {
			if (!tag.hasKey("boosty"))
				tag.set("boosty", new NBTTagCompound());
			
			NBTTagCompound boosty = tag.getCompound("boosty");
			String klucz = (baza ? "baza" : "mn") + "_" + attr.name();

			double akt = boosty.getDouble(klucz);
			boosty.setDouble(klucz, akt + ile);
		}

		
		public final Atrybut attr;
		public final boolean baza; // baza : mnożnik
		public final double wartość;
		
		public Boost(Atrybut attr, boolean baza, double wartość) {
			this.wartość = wartość;
			this.baza = baza;
			this.attr = attr;
		}
		Boost(String klucz, boolean baza, double wartość) {
			this(Func.StringToEnum(Atrybut.class, klucz.substring(baza ? 5 : 3)), baza, wartość);
		}
		
		
		private void __aplikuj(GraczRPG gracz, double wartość) {
			if (baza)
				gracz.statystyka(attr).zwiększBaza(wartość);
			else
				gracz.statystyka(attr).zwiększMnożnik(wartość);
			
		}
		public void zaaplikuj(GraczRPG gracz) {
			__aplikuj(gracz, wartość);
		}
		public void odaplikuj(GraczRPG gracz) {
			__aplikuj(gracz, -wartość);
		}
		
		
		@Override
 		public String toString() {
			StringBuilder strB = new StringBuilder();
			
			strB.append(attr);
			strB.append(wartość < 0 ? "§c-" : "§a+");
			if (baza)
				strB.append((int) wartość);
			else
				strB.append(((int) ((wartość - 1) * 100)) + "%");
			
			return strB.toString();
		}
	
		

		
	}
	
	public static class Enchant {
		static final Map<String, Enchant> mapa = new HashMap<>();
		
		public final String nazwa;
		public final String opis;
		
		public Enchant(String nazwa, String opis) {
			this.nazwa = nazwa;
			this.opis = opis;
			mapa.put(nazwa, this);
		}
	
		
		static NBTTagCompound enchanty(ItemStack item) {
			return tag(item).getCompound("enchanty");
		}
		public static int getEnchantLvl(ItemStack item, String enchant) {
			return enchanty(item).getInt(enchant);
		}
		private static final Enchant pusty = new Enchant("", "");
		public static Enchant getEnchant(String enchant) {
			return mapa.getOrDefault(enchant, pusty);
		}
		public static Iterable<String> getEnchanty() {
			return mapa.keySet();
		}
	}
	
	
	
	public ZfaktoryzowaneItemy() {
		super("edytujitemrpg");
		ustawKomende("exportujitemrpg", "/exportujitemrpg <id>", null);
		BazaDanych.otwórz();
	}
		
	
	@EventHandler
	public void spawnItemu(ItemSpawnEvent ev) {
		if (id(ev.getEntity().getItemStack()) == null) {
			ev.setCancelled(true);
			return;
		}
		
		Ranga ranga = Ranga.ranga(ev.getEntity().getItemStack());
		
		Ranga.ustawRangę(ev.getEntity().getItemStack(), ranga);
		
		ranga.team.addEntry(ev.getEntity().getUniqueId().toString());
		ev.getEntity().setGlowing(true);
	}
	@EventHandler
	public void podnoszenieItemów(EntityPickupItemEvent ev) {
		if (!(ev.getEntity() instanceof Player)) return;
		
		przerób(ev.getItem().getItemStack());
	}


	public static List<String> itemy() {
		ResultSet set = BazaDanych.executeQuery("SELECT id FROM itemy");
		List<String> itemy = new ArrayList<>();
		
		try {
			while (set.next())
				itemy.add(set.getString("id"));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return itemy;
	}
	public static ItemStack dajItem(String id) {
		ResultSet set = BazaDanych.executeQuery("SELECT * FROM itemy WHERE id='" + id + "'");
		
		try {
			if (set.next()) {
				ItemStack item = CraftItemStack.asCraftCopy(Config.item(set.getString("bazowy_item")));
				
				NBTTagCompound tag = new NBTTagCompound();
				
				tag.setString("id", set.getString("id"));
				Ranga.ustawRangę(tag, Func.StringToEnum(Ranga.class, set.getString("ranga")));
				
				Func.wykonajDlaNieNull(BazaDanych.readBlob(set, "bonusy"), in -> {
					try {
						int len = in.readShort();
						while (len-- > 0) {
							Atrybut attr = Func.StringToEnum(Atrybut.class, in.readUTF());
							double baza = in.readDouble();
							double mnożnik = in.readDouble();
							
							if (baza	!= 0) Boost.dodajBoost(tag, attr, baza,		true);
							if (mnożnik	!= 0) Boost.dodajBoost(tag, attr, mnożnik,	false);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
				
				
				ustawTag(item, tag);
				
				przerób(item, set.getString("opis"));
				
				return item;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length <= 1 && cmd.getName().equals("exportujitemrpg")) {
			List<String> lista = new ArrayList<>();
			ResultSet set = BazaDanych.executeQuery("SELECT id FROM itemy");
			try {
				while (set.next())
					lista.add(set.getString("id"));
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return utab(args, lista);
		}
		return null;
	}
	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) throws MsgCmdError {
		if (!(sender instanceof Player)) return Func.powiadom(sender, prefix + "Musisz być graczem aby tego użyć");
		
		Player p = (Player) sender;
		
		ItemStack item = p.getInventory().getItemInMainHand();
		
		if (item == null)
			return Func.powiadom(sender, prefix + "Musisz trzymać coś w ręce");
		
		if (cmd.getName().equals("exportujitemrpg")) {
			if (args.length < 1)
				return false;
			String id = args[0];
			exportujDoBazy(item, id);
			return Func.powiadom(sender, prefix + "wyexportowano item pod id " + id);
		}
		
		NBTTagCompound tag = tag(item);
		
		try {
			switch (args[0].toLowerCase()) {
			case "ranga":
				Ranga ranga = Func.StringToEnum(Ranga.class, args[1]);
				Ranga.ustawRangę(tag, ranga);
				break;
			case "boost":
				switch (args[1].toLowerCase()) {
				case "usuń":
					NBTTagCompound boosty = tag.getCompound("boosty");
					boosty.remove(args[2]);
					break;
				case "dodaj":
					boolean baza = !args[6].endsWith("%");
					Boost.dodajBoost(tag,
							Func.StringToEnum(Atrybut.class, args[5]),
							Func.Double(baza ? args[6].substring(0, args[6].length() - 1) : args[6]) - (baza ? 0 : 1),
							baza
							);
					break;
				}
			}
			ustawTag(item, tag);
		} catch (Throwable e) {
			p.sendMessage("§4" + e.getMessage());
		}
		
		edytor(p);
		
		return true;
	}
	private void edytor(Player p) {
		Napis n = new Napis();
		
		ItemStack item = p.getInventory().getItemInMainHand();
		
		NBTTagCompound tag = tag(item);
		
		Ranga aktRanga = Ranga.ranga(tag);
		n.dodaj(new Napis("\n\n\n§6ranga§8: "));
		Func.forEach(Ranga.values(), ranga -> {
			Napis nRanga;
			if (aktRanga == ranga)
				nRanga = new Napis(ranga.kolor + "§n" + ranga.name());
			else
				nRanga = new Napis(ranga.toString());
			
			nRanga.clickEvent(Action.RUN_COMMAND, "/edytujitemrpg ranga " + ranga.name());
			nRanga.hover("§bKliknij aby ustawić " + ranga);
			
			nRanga.dodaj(" ");
			
			n.dodaj(nRanga);
		});
		n.dodaj("\n\n");

		List<Boost> boosty = Boost.getBoosty(tag);
		n.dodaj("§6§lBoosty§8:\n");
		boosty.forEach(boost -> {
			n.dodaj(new Napis(
						"§e§l- " + boost + " "
						),
					new Napis(
						"§c[x]",
						"§bKliknij aby usunąć " + boost,
						"/edytujitemrpg boost usuń " + (boost.baza ? "baza_" : "mn_") + boost.attr.name()
						)
					);
			n.dodaj("\n");
		});
		n.dodaj(new Napis("§a[dodaj]", "§bKliknij aby dodać boost", "/edytujitemrpg boost dodaj <Atrybut wartość(%)> >> "));
		n.dodaj("\n\n");
		
		n.wyświetl(p);
	}

	
	public static void exportujDoBazy(ItemStack item, String id) {
		tag(item).setString("id", id);
		
		String opis = null;
		if (item.hasItemMeta() && item.getItemMeta().hasLore())
			opis = Func.listToString(item.getItemMeta().getLore(), 0, "\\n");
		
		List<Boost> boosty = Boost.getBoosty(item);
		ByteArrayOutputStream blobStream = new ByteArrayOutputStream();
		DataOutputStream blobOut = boosty.isEmpty() ? null : new DataOutputStream(blobStream);
		Map<Atrybut, MonoKrotka<Double>> mapaBoostów = new HashMap<>();
		boosty.forEach(boost -> {
			MonoKrotka<Double> krotka = mapaBoostów.get(boost.attr);
			if (krotka == null) {
				krotka = new MonoKrotka<>(0d, 0d);
				mapaBoostów.put(boost.attr, krotka);
			}
			
			if (boost.baza)	krotka.a = boost.wartość;
			else			krotka.b = boost.wartość;
		});
		if (!boosty.isEmpty()) {
			try {
				blobOut.writeShort(boosty.size());
				mapaBoostów.forEach((attr, krotka) -> krotka.wykonaj((baza, mnożnik) -> {
					try {
						blobOut.writeUTF(attr.name());
						blobOut.writeDouble(baza);
						blobOut.writeDouble(mnożnik);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}));
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		
		PreparedStatement stat = BazaDanych.prepare("INSERT INTO itemy(id, opis, ranga, bazowy_item, bonusy) VALUES (?, ?, ?, ?, ?)");
		try {
			stat.setString(1, id);
			stat.setString(2, opis);
			stat.setString(3, Ranga.ranga(item).name());
			stat.setString(4, item.getType().name());
			stat.setBytes (5, blobOut == null ? null : blobStream.toByteArray());
			stat.execute();
			if (blobOut != null)
				blobOut.close();
		} catch (SQLException | IOException e) {
			throw new RuntimeException(e);
		}
	}
}
