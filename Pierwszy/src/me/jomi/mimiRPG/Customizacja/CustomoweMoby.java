package me.jomi.mimiRPG.Customizacja;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;

import net.minecraft.nbt.MojangsonParser;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.ai.goal.PathfinderGoalSelector;
import net.minecraft.world.entity.ai.goal.target.PathfinderGoalNearestAttackableTarget;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.MineZ.Bazy;
import me.jomi.mimiRPG.api._WorldGuard;
import me.jomi.mimiRPG.util.Ciąg;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;
import me.jomi.mimiRPG.util.Zegar;

@Moduł
public class CustomoweMoby implements Listener, Zegar, Przeładowalny {
	static class Mob {
		EntityType typ;
		List<Krotka<Attribute, Double>> atrybuty;
		List<Krotka<EquipmentSlot, ItemStack>> eq;
		List<Krotka<Double, ItemStack>> drop;
		NBTTagCompound tag = new NBTTagCompound();
		List<Material> bloki;
		Mob rumak;
		Integer exp = -1;
		double szansa = 1;
		boolean atakujGraczy = false;
		boolean blokujTransformacje = false;
		
		@SuppressWarnings("unchecked")
		public Mob(ConfigurationSection sekcja) {
			typ = EntityType.valueOf(sekcja.getString("Typ", "Zombie").toUpperCase());
			
			try {
				tag = MojangsonParser.parse(sekcja.getString("NBT", "{}"));
			} catch (CommandSyntaxException e) {
				Main.warn("Niepoprawny nbttag " + sekcja.getCurrentPath() + " w Customowe Moby.yml");
			}
			
			if (sekcja.contains("Imie"))
				tag.setString("CustomName", "\"\\\"" + Func.koloruj(sekcja.getString("Imie")) + "\\\"\""); // TODO sprawdzić
			if (sekcja.contains("Imie zawsze widoczne"))
				tag.setBoolean("CustomNameVisible", sekcja.getBoolean("Imie zawsze widoczne"));
			if (tag.isEmpty())
				tag = null;
			
			ConfigurationSection _sekcja = sekcja.getConfigurationSection("Atrybuty");
			if (_sekcja != null) {
				atrybuty = Lists.newArrayList();
				for (String attr : _sekcja.getKeys(false))
					atrybuty.add(new Krotka<>(Attribute.valueOf(attr.toUpperCase()), _sekcja.getDouble(attr)));
			}
			
			_sekcja = sekcja.getConfigurationSection("Itemki");
			if (_sekcja != null) {
				eq = Lists.newArrayList();
				for (Entry<String, Object> en : _sekcja.getValues(false).entrySet())
					eq.add(new Krotka<>(EquipmentSlot.valueOf(slot(en.getKey())), Config.item(en.getValue())));
			}
			
			if (sekcja.contains("Bloki")) {
				bloki = Lists.newArrayList();
				for (String klucz : (List<String>) sekcja.getList("Bloki"))
					bloki.add(Material.valueOf(klucz.toUpperCase()));
			}
			
			
			if (sekcja.contains("drop")) {
				drop = Lists.newArrayList();
				for (Map<String, Object> mapa : (List<Map<String, Object>>) sekcja.get("drop"))
					drop.add(new Krotka<>((double) mapa.get("szansa"), Config.item(mapa.get("item"))));
			}
			
			
			szansa = sekcja.getDouble("Szansa", 1);

			atakujGraczy = sekcja.getBoolean("atakujGraczy", false);
			
			blokujTransformacje = sekcja.getBoolean("blokujTransformacje", false);
			

			if (sekcja.contains("Rumak"))
				rumak = new Mob(sekcja.getConfigurationSection("Rumak"));
		
			exp = (Integer) sekcja.get("exp", null);
		}
		
		ItemStack randItem(Set<Material> mat) {
			if (Func.losuj(.7)) return null;
			ItemStack item = new ItemStack(Func.losuj(Lists.newArrayList(mat)));
			return Func.losuj(.3) ? Func.połysk(item) : item;
		}
		void ubierzRandomowo(LivingEntity mob) {
			EntityEquipment _eq = mob.getEquipment();
			ItemStack czapka = randItem(CustomoweMoby.inst.czapki);
			_eq.setHelmet(czapka == null ? new ItemStack(Material.STONE_BUTTON) : czapka);
			_eq.setChestplate(randItem(CustomoweMoby.inst.klaty));
			_eq.setLeggings(randItem(CustomoweMoby.inst.spodnie));
			_eq.setBoots(randItem(CustomoweMoby.inst.buty));
		}
		

		Entity zresp(Location loc) {
			Entity mob = loc.getWorld().spawnEntity(loc, typ);
			
			mob.addScoreboardTag("mimiCustomowyMob");
			Func.ustawMetadate(mob, "mimiCustomowyMob", this);
			
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
						Func.wykonajDlaNieNull(mobAtt.getAttribute(krotka.a), attr -> attr.setBaseValue(krotka.b));
					if (mob instanceof Damageable)
						((Damageable) mob).setHealth(mobAtt.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
				}
			}
			
			if (mob instanceof LivingEntity) {
				EntityEquipment eqMoba = ((LivingEntity) mob).getEquipment();
				if (eq == null)
					ubierzRandomowo((LivingEntity) mob);
				else
					for (Krotka<EquipmentSlot, ItemStack> krotka : eq)
						if (krotka.b != null)
							eqMoba.setItem(krotka.a, krotka.b.clone());
				eqMoba.setHelmetDropChance(0f);
				eqMoba.setChestplateDropChance(0f);
				eqMoba.setLeggingsDropChance(0f);
				eqMoba.setBootsDropChance(0f);
			}
			
			if (rumak != null)
				rumak.zresp(loc).addPassenger(mob);
			
			if (atakujGraczy) {
				EntityInsentient ei = (EntityInsentient) ((CraftEntity) mob).getHandle();
				ei.bQ = new PathfinderGoalSelector(ei.getWorld().getMethodProfilerSupplier());
				ei.bQ.a(2, new PathfinderGoalNearestAttackableTarget<EntityPlayer>(ei, EntityPlayer.class, true));
			}
			
			
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
	public static boolean warunekModułu() {
		return _WorldGuard.rg != null;
	}

	int max_odległość_od_gracza;
	int min_odległość_od_gracza;
	int czas_odświeżania_ticki;
	double dzienna_szansa_zrespawnowania_dla_gracza;
	double nocna_szansa_zrespawnowania_dla_gracza;
	
	final HashMap<String, Mob> mapaMobów = new HashMap<>();
	final HashMap<String, Ciąg<Mob>> mapaFlag = new HashMap<>();
	
	final Config config = new Config("Customowe Moby");
	
	final Set<Material> czapki 	= Sets.newConcurrentHashSet();
	final Set<Material> klaty 	= Sets.newConcurrentHashSet();
	final Set<Material> spodnie = Sets.newConcurrentHashSet();
	final Set<Material> buty 	= Sets.newConcurrentHashSet();
	static CustomoweMoby inst;
	public CustomoweMoby() {
		inst = this;
		for (String typ : new String[]{"LEATHER", "IRON", "GOLDEN", "CHAINMAIL"}) {
			czapki.	add(Material.valueOf(typ + "_HELMET"));
			klaty.	add(Material.valueOf(typ + "_CHESTPLATE"));
			spodnie.add(Material.valueOf(typ + "_LEGGINGS"));
			buty.	add(Material.valueOf(typ + "_BOOTS"));
		}
		for (World świat : Bukkit.getWorlds())
			for (Entity mob : świat.getEntities())
				if (mob.getScoreboardTags().contains("mimiCustomowyMob"))
					mob.remove();
	}
	
	@Override
	public void przeładuj() {
		config.przeładuj();
		
		max_odległość_od_gracza 			 	 = config.wczytaj("max odległość od gracza", 	  50);
		min_odległość_od_gracza 			 	 = config.wczytaj("min odległość od gracza", 	  50);
		czas_odświeżania_ticki 			 		 = config.wczytaj("czas odświeżania", 	 		  120) * 20;
		dzienna_szansa_zrespawnowania_dla_gracza = config.wczytaj("szansa zrespawnowania za dnia", .02);
		nocna_szansa_zrespawnowania_dla_gracza 	 = config.wczytaj("szansa zrespawnowania w nocy",  dzienna_szansa_zrespawnowania_dla_gracza);
		
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
		mapaFlag.clear();
		sekcja = config.sekcja("Flagi");
		if (sekcja != null)
			for (String flaga : sekcja.getKeys(false)) {
				ConfigurationSection sekcjaFlag = sekcja.getConfigurationSection(flaga);
				List<Krotka<Integer, Mob>> lista = Lists.newArrayList();
				for (Entry<String, Object> entry : sekcjaFlag.getValues(false).entrySet())
					lista.add(new Krotka<>((int) entry.getValue(), mapaMobów.get(entry.getKey())));
				mapaFlag.put(flaga, new Ciąg<>(lista));
			}
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Customwe Moby", mapaMobów.size());
	}
	
	public boolean dzień(World świat) {
	    long time = świat.getTime();
	    return time < 12300 || time > 23850;
	}
	
	@Override
	public int czas() {
		for (Player p : Bukkit.getOnlinePlayers())
			if (Func.multiEquals(p.getGameMode(), GameMode.SURVIVAL, GameMode.ADVENTURE) &&
					Func.losuj(dzień(p.getWorld()) ? dzienna_szansa_zrespawnowania_dla_gracza : nocna_szansa_zrespawnowania_dla_gracza))
				if (!Main.włączonyModół(Bazy.class) || Bazy.inst.znajdzBaze(p.getLocation()) == null)
					zresp(p.getLocation());
		return czas_odświeżania_ticki;
	}

	@EventHandler
	public void ładowanieChunków(ChunkLoadEvent ev) {
		for (Entity mob : ev.getChunk().getEntities())
			if (!mob.hasMetadata("mimiCustomowyMob") && mob.getScoreboardTags().contains("mimiCustomowyMob"))
				mob.remove();
	}
	
	@EventHandler
	public void śmierćMoba(EntityDeathEvent ev) {
		LivingEntity mob = ev.getEntity();
		if (!mob.hasMetadata("mimiCustomowyMob")) return;
		Mob m = (Mob) mob.getMetadata("mimiCustomowyMob").get(0).value();
		if (m.drop == null) return;
		for (Krotka<Double, ItemStack> krotka : m.drop)
			if (krotka.b != null && Func.losuj(krotka.a))
				mob.getWorld().dropItem(mob.getLocation(), krotka.b);
		ev.getDrops().clear();
		if (m.exp != null)
			ev.setDroppedExp(m.exp);
	}
	
	@EventHandler
	public void transformacja(EntityTransformEvent ev) {
		Entity mob = ev.getEntity();
		if (!mob.hasMetadata("mimiCustomowyMob")) return;
		Mob m = (Mob) mob.getMetadata("mimiCustomowyMob").get(0).value();
		if (m.blokujTransformacje)
			ev.setCancelled(true);
	}
	
	void zresp(Location loc) {
		Supplier<Integer> los = () -> Func.losuj(min_odległość_od_gracza, max_odległość_od_gracza) * (Func.losuj(.5) ? 1 : -1);
		loc.add(los.get(), 0, los.get());
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
				if (loc.getBlockY() >= 0)
					loc.add(0, dokładność, 0);
			}
		};
		znajdz.accept(1);
		int y = loc.getBlockY();
		if (0 < y && y < 256)
			return loc;
		return null;
	}
	void zrespMoba(Location loc) {
		if (loc == null || !(!loc.getBlock().isSolid() && !loc.clone().add(0, 1, 0).getBlock().getType().isSolid())) return;
	
		String _flagi = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(loc.getWorld()))
				.getApplicableRegions(BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()))
				.queryValue(null, _WorldGuard.flagaCustomoweMoby);
		if (_flagi == null) return;
		String[] flagi = _flagi.split(",");
		Ciąg<Mob> moby = mapaFlag.get(flagi[Func.losuj(0, flagi.length-1)]);
		if (moby == null) return;
		Mob mob = moby.losuj();
		if (mob == null) return;
		if ((mob.bloki == null || mob.bloki.contains(loc.add(0, -1, 0).getBlock().getType())) && Func.losuj(mob.szansa))	
			mob.zresp(loc.add(.5, 1, .5));
	}
}






