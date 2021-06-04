package me.jomi.mimiRPG.RPG_Ultra;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftChatMessage;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import net.minecraft.server.v1_16_R3.EntityLiving;
import net.minecraft.server.v1_16_R3.NBTTagCompound;

import me.jomi.mimiRPG.Baza;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Komenda;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Krotki.MonoKrotka;
import me.jomi.mimiRPG.util.NMS;
import me.jomi.mimiRPG.util.Panel;
import me.jomi.mimiRPG.util.Przeładowalny;
import me.jomi.mimiRPG.util.Zegar;

import lombok.Getter;

@Moduł
public class Bestie extends Komenda implements Listener, Przeładowalny, Zegar {
	public Bestie() {
		super("bestie");

		panelKategorie.ustawClick(ev -> {
			ItemStack item = ev.getCurrentItem();
			if (Baza.pustySlotCzarny.isSimilar(item))
				return;
			String kategoria = NMS.nms(item).getTag().getString("mimiBestiaInfo");
			otwórzPanel((Player) ev.getWhoClicked(), kategoria);
		});
		panelGrupy.ustawClick(ev -> {
			ItemStack item = ev.getCurrentItem();
			if (Baza.pustySlotCzarny.isSimilar(item))
				return;
			String kategoria = (String) panelGrupy.dajDanePanelu(ev.getInventory());
			String grupa = NMS.nms(item).getTag().getString("mimiBestiaInfo");
			otwórzPanel((Player) ev.getWhoClicked(), kategoria, grupa);
		});
	}
	
	public static final String tagBesti = "mimiBestia";
	public static final String metaBesti = "mimiBestia";
	public static final String metaSpawnera = "mimiBestiaSpawner";
	
	public static class Grupa<T> {
		public final Map<String, T> mapa = new HashMap<>();
		
		public final String nazwa;
		@Getter private ItemStack item;
		@Getter int slot = 0;
		
		public Grupa(String nazwa) {
			this.nazwa = nazwa;
			setItem(new ItemStack(Material.INK_SAC));
		}
		
		public void setItem(ItemStack item) {
			Objects.requireNonNull(item);
			item = CraftItemStack.asCraftCopy(item);
			NBTTagCompound tag = NMS.nms(item).getOrCreateTag();
			
			tag.setString("mimiBestiaInfo", nazwa);
			
			NMS.nms(item).setTag(tag);
			
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName("§9" + nazwa);
			item.setItemMeta(meta);
			
			this.item = item;
		}
	}
	public static class DropRPG {
		public final ItemStack item;
		public final double szansa;
		public final int min_ilość;
		public final int max_ilość;

		public DropRPG(ItemStack item, double szansa, int min_ilość, int max_ilość) {
			this.max_ilość = max_ilość;
			this.min_ilość = min_ilość;
			this.szansa = szansa;
			this.item = Objects.requireNonNull(item);
		}
		
		public ItemStack dropnij() {
			if (Func.losuj(szansa))
				return Func.ilość(item.clone(), Func.losuj(min_ilość, max_ilość));
			return null;
		}
		public void dropnij(List<ItemStack> dropy) {
			Func.wykonajDlaNieNull(dropnij(), dropy::add);
		}
	}
	public static class Bestia {
		static final Map<String, Grupa<Grupa<Bestia>>> mapa = new HashMap<>();
		
		public static Bestia bestia(String kategoria, String grupa, String nazwa) {
			try {
				return mapa.get(kategoria).mapa.get(grupa).mapa.get(nazwa);
			} catch (NullPointerException e) {
				return null;
			}
		}
		
		public final List<DropRPG> dropy;
		public final int exp_łowcy;
		public final int exp;
		public final double kasa;
		public final String nazwa;		 // np. Miner Zombie lvl 42
		public final String grupa;		// np. Miner Zombie
		public final String kategoria; // np. Jaskiniowe
		
		public final EntityType mob;
		public final double speed;
		public final double def;
		public final double dmg;
		public final double hp;
		
		public final ItemStack ikona;
		public final int slot;
		
		Bestia(List<DropRPG> dropy, double kasa, int exp, int exp_łowcy, String kategoria, String grupa, String nazwa,
				EntityType mob, double hp, double dmg, double def, double speed, ItemStack ikona, int slot) {
			this.dropy = Func.nieNull(dropy);
			this.exp_łowcy = exp_łowcy;
			this.kasa = kasa;
			this.exp = exp;
			
			this.speed = speed;
			this.def = def;
			this.dmg = dmg;
			this.mob = mob;
			this.hp = hp;
			
			this.nazwa		= Objects.requireNonNull(nazwa);
			this.grupa		= grupa 	== null ? nazwa : grupa;
			this.kategoria	= kategoria == null ? grupa : kategoria;
			
			this.ikona = ikona;
			this.slot = slot;
			
			if (!mapa.containsKey(kategoria))
				mapa.put(kategoria, new Grupa<>(kategoria));
			Map<String, Grupa<Bestia>> m = mapa.get(kategoria).mapa;
			if (!m.containsKey(grupa))
				m.put(grupa, new Grupa<>(grupa));
			m.get(grupa).mapa.put(nazwa, this);
		}
		
		public Entity zresp(Location loc) {
			EntityLiving mob = (EntityLiving) ((CraftWorld) loc.getWorld()).createEntity(loc, this.mob.getEntityClass());
			
			mob.craftAttributes.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)	.setBaseValue(speed);
			mob.craftAttributes.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)	.setBaseValue(dmg);
			mob.craftAttributes.getAttribute(Attribute.GENERIC_MAX_HEALTH)		.setBaseValue(hp);
			mob.craftAttributes.getAttribute(Attribute.GENERIC_ARMOR)			.setBaseValue(def);
			mob.setHealth((float) hp);
			
			mob.setCustomName(CraftChatMessage.fromStringOrNull(nazwa));
			mob.setCustomNameVisible(true);
			
			((CraftWorld) loc.getWorld()).addEntity(mob, SpawnReason.CUSTOM, null);

			Func.ustawMetadate(mob.getBukkitEntity(), metaBesti, this);
			mob.addScoreboardTag(tagBesti);
			
			return mob.getBukkitEntity();
		}
	}
	
	
	
	public Bestia bestia(Entity mob) {
		if (!mob.hasMetadata(metaBesti))
			return null;
		else
			return (Bestia) mob.getMetadata(metaBesti).get(0).value();
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void chunkLoad(ChunkLoadEvent ev) {
		Func.forEach(ev.getChunk().getEntities(), e -> {
			if (e.getScoreboardTags().contains(tagBesti) && !e.hasMetadata(metaBesti))
				e.remove();
		});
	}
	@EventHandler(priority = EventPriority.LOW)
	public void śmierćMoba(EntityDeathEvent ev) {
		if (ev.getEntity() instanceof Player) {
			if (ev.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
				EntityDamageByEntityEvent ev2 = (EntityDamageByEntityEvent) ev.getEntity().getLastDamageCause();
				Func.wykonajDlaNieNull(bestia(ev2.getDamager()), bestia -> {
					GraczRPG gracz = GraczRPG.gracz((Player) ev.getEntity());
					PersistentDataContainer data = gracz.getBestie(bestia);
					PersistentDataType<Integer, Integer> type = PersistentDataType.INTEGER;
					NMS.set(data, "zgon", type, NMS.get(data, "zgon", type) + 1);
				});
			}
			return;
		}
		
		ev.getDrops().clear();
		Func.wykonajDlaNieNull(bestia(ev.getEntity()), bestia -> {
			if (ev.getEntity().hasMetadata(metaSpawnera)) {
				Spawner spawner = (Spawner) ev.getEntity().getMetadata(metaSpawnera).get(0).value();
				spawner.sprawdzZrespione();
			}
			
			bestia.dropy.forEach(drop -> drop.dropnij(ev.getDrops()));
			ev.setDroppedExp(bestia.exp);
			
			Func.wykonajDlaNieNull(ev.getEntity().getKiller(), killer -> {
				GraczRPG gracz = GraczRPG.gracz(killer);
				gracz.ścieżka_łowca.zwiększExp(bestia.exp_łowcy);
				gracz.dodajKase(bestia.kasa);

				PersistentDataContainer data = gracz.getBestie(bestia);
				PersistentDataType<Integer, Integer> type = PersistentDataType.INTEGER;
				NMS.set(data, "kill", type, NMS.get(data, "kill", type) + 1);			
			});
		}, () -> ev.setDroppedExp(0));
	}
	
	
	public static class Spawner extends Mapowany {
		@Mapowane Location locRóg1;
		@Mapowane Location locRóg2;
		@Mapowane String kategoria;
		@Mapowane String grupa;
		@Mapowane String nazwa;
		@Mapowane int coIle; // sek
		@Mapowane int limit;
		private int sprwadzanie = 0;
		Bestia bestia;
		
		List<Entity> moby = new ArrayList<>();
		
		@Override
		protected void Init() {
			double[][] w = new double[3][2];

			BiConsumer<Integer, Function<Location, Double>> bic = (i, func) -> {
				double x1 = func.apply(locRóg1);
				double x2 = func.apply(locRóg2);
				w[i][0] = Math.min(x1, x2);
				w[i][1] = Math.max(x1, x2);
			};
			
			bic.accept(0, Location::getX);
			bic.accept(1, Location::getY);
			bic.accept(2, Location::getZ);
			
			locRóg1 = new Location(locRóg1.getWorld(), w[0][0], w[1][0], w[2][0]);
			locRóg2 = new Location(locRóg2.getWorld(), w[0][1], w[1][1], w[2][1]);
			
			bestia = Objects.requireNonNull(Bestia.bestia(kategoria, grupa, nazwa));
		}

		private int timer;
		public void czas() {
			if (moby.size() >= limit) {
				if (sprwadzanie-- < 0)
					sprawdzZrespione();
			} else if (timer++ >= coIle) {
				timer = 0;
				zresp();
			}
		}
		
		private Location losuj() {
			double x = Func.losuj(locRóg1.getX(), locRóg2.getX());
			double y = Func.losuj(locRóg1.getY(), locRóg2.getY());
			double z = Func.losuj(locRóg1.getZ(), locRóg2.getZ());
			
			return new Location(locRóg1.getWorld(), x, y, z);
		}
		void zresp() {
			Location loc = null;
			for (int i=0; i < 10; i++) {
				loc = losuj();
				if (loc.getBlock().isPassable() && loc.clone().add(0, 1, 0).getBlock().isPassable())
					break;
				loc = null;
			}
			if (loc == null)
				return;
			
			
			Entity mob = bestia.zresp(loc);
			Func.ustawMetadate(mob, metaSpawnera, this);
			moby.add(mob);
		}
		
		public void sprawdzZrespione() {
			sprwadzanie = Func.losuj(180, 300);
			for (int i=moby.size() - 1; i >= 0; i--)
				if (moby.get(i).isDead())
					moby.remove(i);
		}
	}
	

	Panel panelKategorie = new Panel(true);
	Panel panelGrupy  = new Panel(true);
	Panel panelBestie = new Panel(true);
	void otwórzPanel(Player p) {
		Inventory inv = panelKategorie.stwórz(null, 4, "&4&lBestie");
		Func.ustawPuste(inv, Baza.pustySlotCzarny);
		
		Bestia.mapa.values().forEach(kategoria -> {
			ItemStack item = kategoria.item.clone();
			
			inv.setItem(kategoria.slot, item);
		});
		
		p.openInventory(inv);
	}
	void otwórzPanel(Player p, String kategoria) {
		Inventory inv = panelGrupy.stwórz(kategoria, 3, "&4&l" + kategoria);
		Func.ustawPuste(inv, Baza.pustySlotCzarny);
		
		Bestia.mapa.get(kategoria).mapa.values().forEach(grupa -> inv.setItem(grupa.slot, grupa.item));
		
		p.openInventory(inv);
	}
	void otwórzPanel(Player p, String kategoria, String grupa) {
		Inventory inv = panelBestie.stwórz(new MonoKrotka<String>(kategoria, grupa), 3, "&4&l" + grupa);
		Func.ustawPuste(inv, Baza.pustySlotCzarny);
		
		Bestia.mapa.get(kategoria).mapa.get(grupa).mapa.values().forEach(bestia -> {
			ItemStack item = bestia.ikona.clone();
			
			ItemMeta meta = item.getItemMeta();
			List<String> lore = Func.nieNull(meta.getLore());
			if (bestia.kasa		 != 0) lore.add("§7Monety§8: §6"	+ RPG.monety(bestia.kasa));
			if (bestia.exp		 != 0) lore.add("§7Exp§8: §6"		+ Func.IntToString(bestia.exp));
			if (bestia.exp_łowcy != 0) lore.add("§7Exp Łowcy§8: §6"	+ Func.IntToString(bestia.exp_łowcy));
			lore.add(" ");
			
			meta.setLore(lore);
			item.setItemMeta(meta);
			
			inv.setItem(bestia.slot, item);
		});
		
		p.openInventory(inv);
	}
	
	List<Spawner> spawnery = new ArrayList<>();
	
	@Override
	@SuppressWarnings("unchecked")
	public void przeładuj() {
		Config config = new Config("Bestie");
		Bestia.mapa.clear();
		
		List<Runnable> odłożone = new ArrayList<>();
		
		config.klucze().forEach(kategoria -> {
			ConfigurationSection sekcjaKategori = config.sekcja(kategoria);
			sekcjaKategori.getKeys(false).forEach(grupa -> {
				ConfigurationSection sekcjaGrupy = sekcjaKategori.getConfigurationSection(grupa);

				if (grupa.equals("info")) {
					odłożone.add(() -> {
						Grupa<Grupa<Bestia>> grp = Bestia.mapa.get(kategoria);
						grp.setItem(Config.item(sekcjaGrupy.get("item")));
						grp.slot = sekcjaGrupy.getInt("slot");
					});
					return;
				}
				
				sekcjaGrupy.getKeys(false).forEach(nazwa -> {
					ConfigurationSection sekcja = sekcjaGrupy.getConfigurationSection(nazwa);

					if (nazwa.equals("info")) {
						odłożone.add(() -> {
							Grupa<Bestia> grp = Bestia.mapa.get(kategoria).mapa.get(grupa);
							grp.setItem(Config.item(sekcja.get("item")));
							grp.slot = sekcja.getInt("slot");
						});
						return;
					}
					
					List<DropRPG> dropy = new ArrayList<>();
					sekcja.getStringList("dropy").forEach(drop -> {
						List<String> części = Func.tnij(drop, " ");
						List<String> min_max = Func.tnij(części.get(2), "-");
						dropy.add(new DropRPG(
								Config.item(części.get(0)),
								Func.Double(części.get(1)),
								Func.Int(min_max.get(0)),
								Func.Int(min_max.get(1))
								));
					});
					
					new Bestia(
							dropy,
							sekcja.getDouble("kasa", 0d),
							sekcja.getInt("exp", 0),
							sekcja.getInt("exp_łowcy", 0),
							kategoria,
							grupa,
							nazwa,
							Func.StringToEnum(EntityType.class, sekcja.getString("typ", "ZOMBIE")),
							sekcja.getDouble("hp", 20d),
							sekcja.getDouble("dmg", 1d),
							sekcja.getDouble("def", 0d),
							sekcja.getDouble("speed", .21d),
							Config.item(sekcja.get("ikona")),
							sekcja.getInt("slot")
							);
				});
			});
		});
		
		odłożone.forEach(runnable -> {
			try {
				runnable.run();
			} catch(Throwable e) {
				e.printStackTrace();
			}
		});
		
		spawnery = Func.nieNull((List<Spawner>) new Config("configi/Bestie Spawnery").wczytajPewny("spawnery"));
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("wczytane Bestie", Bestia.mapa.size());
	}

	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) throws MsgCmdError {
		if (!(sender instanceof Player)) return Func.powiadom(sender, "Musisz być graczem żeby tego użyć");
		
		otwórzPanel((Player) sender);
		
		return true;
	}

	
	@Override
	public int czas() {
		spawnery.forEach(Spawner::czas);
		return 20;
	}
}
