package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

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
import org.bukkit.event.Listener;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;

import me.jomi.mimiRPG.Config;
import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Przeładowalny;
import me.jomi.mimiRPG.Zegar;
import net.minecraft.server.v1_16_R2.MojangsonParser;
import net.minecraft.server.v1_16_R2.NBTTagCompound;

public class CustomoweMoby implements Listener, Zegar, Przeładowalny {
	public static boolean warunekModułu() {
		return Main.rg != null;
	}
	
	int odległość_od_gracza;
	int czas_odświeżania_ticki;
	double szansa_zrespawnowania_dla_gracza;
	
	final Config config = new Config("Customowe Moby");
	
	@Override
	public void przeładuj() {
		config.przeładuj();
		// TODO dodać szablon
		odległość_od_gracza 			 = config.wczytajLubDomyślna("odległość od gracza", 	50);
		czas_odświeżania_ticki 			 = config.wczytajLubDomyślna("czas odświeżania", 	 	120) * 20;
		szansa_zrespawnowania_dla_gracza = config.wczytajLubDomyślna("szansa zrespawnowania", .02);
	}
	@Override
	public String raport() {
		return ""; // TODO raport
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
		
		zresp(loc.add(.5, 0, .5), "Moby." + typ + ".");
		//ulepszMoba(mob, );
		
		/*
		 * Moby:
		 *   <nazwa>:
		 *     Typ: typMoba
		 *     imie: string 
		 *     imie zawsze widoczne: boolean
		 *     Atrybuty:
		 *       <attr1>: double
		 *     Itemki:
		 *       Głowa: item
		 *       Klata: item
		 *       Spodnie: item
		 *       Buty: item
		 *       Prawa ręka: item
		 *       Lewa ręka: item
		 *     Rumak:
		 *       Typ: typMoba
		 *       Atrybuty:
		 *         ...
		 *       Itemki:
		 *         ...
		 * 
		 * 
		 * 
		 * 
		 * odległość od gracza: int
		 * czas odświeżania: int
		 * szansa zrespawnowania: double
		 * 
		 * 
		 */
		
		
	}
	
	Entity zresp(Location loc, String scieżka) {
		UnaryOperator<String> slot = nazwa -> {
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
		};
		
		ConfigurationSection sekcja;
		
		Entity mob = loc.getWorld().spawnEntity(loc, EntityType.valueOf(config.wczytajLubDomyślna(scieżka + "Typ", "Zombie").toUpperCase()));
		
		String imie = config.wczytajStr(scieżka + "Imie");
		if (imie != null)
			mob.setCustomName(imie);
		mob.setCustomNameVisible(config.wczytajLubDomyślna(scieżka + "Imie zawsze widoczne", false));
		
		String nbt = config.wczytajStr(scieżka + "NBT");
		if (nbt != null) {
			NBTTagCompound tag = new NBTTagCompound();
			try {
				tag = MojangsonParser.parse(nbt);
			} catch (CommandSyntaxException e) {} // TODO pliczek z problemami albo komenda /problemy z jednym wystąpieniem tego albo ten syntax na przeładowaniu
			
			if (!tag.isEmpty()) {
				NBTTagCompound tagStary = new NBTTagCompound();
				((CraftEntity) mob).getHandle().save(tagStary);
				for (String klucz : tag.getKeys())
					tagStary.set(klucz, tag.get(klucz));
				((CraftEntity) mob).getHandle().load(tagStary);
			}
		}
		if (mob instanceof Attributable) {
			Attributable mobAtt = (Attributable) mob;
			sekcja = config.sekcja(scieżka + "Atrybuty");
			if (sekcja != null) {
				for (String attr : sekcja.getKeys(false))
					mobAtt.getAttribute(Attribute.valueOf(attr)).setBaseValue(sekcja.getDouble(attr));
				if (mob instanceof Damageable)
					((Damageable) mob).setHealth(mobAtt.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
			}
		}
		if (mob instanceof LivingEntity) {
			EntityEquipment eq = ((LivingEntity) mob).getEquipment();
			sekcja = config.sekcja(scieżka + "Itemki");
			if (sekcja != null)
				for (String _slot : sekcja.getKeys(false))
					eq.setItem(EquipmentSlot.valueOf(slot.apply(_slot)), config.wczytajItem(scieżka + "Itemki." + _slot));
		}
		
		sekcja = config.sekcja(scieżka + "Rumak");
		if (sekcja != null) {
			zresp(loc, scieżka + "Rumak.").addPassenger(mob);
		}
		
		
		return mob;
	}
}






