package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftEntity;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;

import me.jomi.mimiRPG.Config;
import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Krotka;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Przeładowalny;
import me.jomi.mimiRPG.Zegar;
import net.minecraft.server.v1_16_R2.MojangsonParser;
import net.minecraft.server.v1_16_R2.NBTTagCompound;


public class CustomoweMoby implements Zegar, Przeładowalny {
	public static boolean warunekModułu() {
		return Main.rg != null;
	}
	
	int odległość_od_gracza;
	int czas_odświeżania_ticki;
	double szansa_zrespawnowania_dla_gracza;
	
	final HashMap<String, Mob> mapaMobów = new HashMap<>();
	
	final Config config = new Config("Customowe Moby");
	
	@Override
	public void przeładuj() {
		config.przeładuj();
		
		odległość_od_gracza 			 = config.wczytajLubDomyślna("odległość od gracza", 	50);
		czas_odświeżania_ticki 			 = config.wczytajLubDomyślna("czas odświeżania", 	 	120) * 20;
		szansa_zrespawnowania_dla_gracza = config.wczytajLubDomyślna("szansa zrespawnowania", .02);
		
		mapaMobów.clear();
		ConfigurationSection sekcja = config.sekcja("Moby");
		if (sekcja != null)
			for (String nazwa : sekcja.getKeys(false)) {
				try {
					mapaMobów.put(nazwa, new Mob(sekcja.getConfigurationSection(nazwa)));
				} catch (Exception e) {
					Main.warn("Niepoprawny mob " + nazwa + " w Customowe Moby.yml");
				}
			}
		
	}
	@Override
	public String raport() {
		return "§6Customwe Moby: §e" + mapaMobów.size();
	}
	
	@Override
	public int czas() {
		for (Player p : Bukkit.getOnlinePlayers())
			if (Func.losuj(szansa_zrespawnowania_dla_gracza))
				zresp(p.getLocation());
		return czas_odświeżania_ticki;
	}
	
	void zresp(Location loc) {
		int r = odległość_od_gracza;
		loc.add(Func.losuj(-r, r), 0, Func.losuj(-r, r));
		zrespMoba(znajdzMiejsce(loc));
	}
	Location znajdzMiejsce(Location loc) {
		Consumer<Integer> znajdz = dokładność -> {
			int y = loc.getBlockY();
			if (loc.getBlock().getType().isSolid() && 0 <= y && y <= 256) {
				while (loc.getBlock().getType().isSolid() && loc.getBlockY() <= 256)
					loc.add(0, dokładność, 0);
			} else {
				while (!loc.getBlock().getType().isSolid() && loc.getBlockY() >= 0)
					loc.add(0, -dokładność, 0);
				if (loc.getBlockX() >= 0)
					loc.add(0, dokładność, 0);
			}
		};
		znajdz.accept(5);
		znajdz.accept(1);
		int y = loc.getBlockY();
		if (0 < y && y < 256)
			return loc;
		return null;
	}
	void zrespMoba(Location loc) {
		if (loc == null || !(loc.getBlock().getType().isAir() && loc.clone().add(0, 1, 0).getBlock().getType().isAir())) return;
		
		String _typy = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(loc.getWorld()))
				.getApplicableRegions(BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()))
				.queryValue(null, Main.flagaCustomoweMoby);
		if (_typy == null) return;
		String[] typy = _typy.split(",");
		String typ = typy[Func.losuj(0, typy.length-1)];
		
		if (mapaMobów.containsKey(typ))
			mapaMobów.get(typ).zresp(loc.add(.5, 0, .5));
	}
}

// TODO mnożnik respu
class Mob {
	EntityType typ;
	List<Krotka<Attribute, Double>> atrybuty;
	List<Krotka<EquipmentSlot, ItemStack>> eq;
	NBTTagCompound tag = new NBTTagCompound();
	Mob rumak;
	public Mob(ConfigurationSection sekcja) {
		typ = EntityType.valueOf(sekcja.getString("Typ", "Zombie").toUpperCase());
		
		try {
			tag = MojangsonParser.parse(sekcja.getString("NBT", "{}"));
		} catch (CommandSyntaxException e) {
			Main.warn("Niepoprawny nbttag " + sekcja.getCurrentPath() + " w Customowe Moby.yml");
		}
		
		if (sekcja.contains("Imie"))
			tag.setString("CustomName", "'" + Func.koloruj(sekcja.getString("Imie")) + "'"); // TODO sprawdzić
		if (sekcja.contains("Imie zawsze widoczne"))
			tag.setBoolean("CustomNameVisible", sekcja.getBoolean("Imie zawsze widoczne")); // TODO sprawdzić
		if (tag.isEmpty())
			tag = null;
		
		ConfigurationSection _sekcja = sekcja.getConfigurationSection("Atrybuty");
		if (_sekcja != null) {
			atrybuty = Lists.newArrayList();
			for (String attr : _sekcja.getKeys(false))
				atrybuty.add(Krotka.stwórz(Attribute.valueOf(attr.toUpperCase()), _sekcja.getDouble(attr)));
		}
		
		_sekcja = sekcja.getConfigurationSection("Itemki");
		if (_sekcja != null) {
			eq = Lists.newArrayList();
			for (Entry<String, Object> en : _sekcja.getValues(false).entrySet())
				eq.add(Krotka.stwórz(EquipmentSlot.valueOf(slot(en.getKey())), Config.item(en.getValue())));
		}
		
		if (sekcja.contains("Rumak"))
			rumak = new Mob(sekcja.getConfigurationSection("Rumak"));
		
	}
	
	
	Entity zresp(Location loc) {
		Entity mob = loc.getWorld().spawnEntity(loc, typ);
		
		if (tag != null) {
			NBTTagCompound tagStary = new NBTTagCompound();
			((CraftEntity) mob).getHandle().save(tagStary);
			for (String klucz : tag.getKeys())
				tagStary.set(klucz, tag.get(klucz));
			((CraftEntity) mob).getHandle().load(tagStary);
		}
		
		if (mob instanceof Attributable) {
			Attributable mobAtt = (Attributable) mob;
			if (atrybuty != null) {
				for (Krotka<Attribute, Double> krotka : atrybuty)
					mobAtt.getAttribute(krotka.a).setBaseValue(krotka.b);
				if (mob instanceof Damageable)
					((Damageable) mob).setHealth(mobAtt.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
			}
		}
		
		if (mob instanceof LivingEntity) {
			EntityEquipment eqMoba = ((LivingEntity) mob).getEquipment();
			if (eq != null)
				for (Krotka<EquipmentSlot, ItemStack> krotka : eq)
					if (krotka.b != null)
						eqMoba.setItem(krotka.a, krotka.b.clone());
		}
		
		if (rumak != null)
			rumak.zresp(loc).addPassenger(mob);
		
		return mob;
	}
	

	String slot(String nazwa) {
		switch(nazwa.toLowerCase()) {
		case "głowa":
		case "glowa": 
			return "HEAD";
		case "klata":
		case "napiersnik":
		case "napierśnik":
			return "CHEST";
		case "spodnie":
			return "LEGS";
		case "buty":
			return "FEET";
		case "ręka":
		case "reka":
		case "prawa ręka":
		case "prawa reka":
			return "HAND";
		case "lewa ręka":
		case "lewa reka":
		case "druga ręka":
		case "druga reka":
			return "OFF_HAND";
		}
		return nazwa.toUpperCase();
	}
}




