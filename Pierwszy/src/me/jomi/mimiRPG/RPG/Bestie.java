package me.jomi.mimiRPG.RPG;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Color;
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
import org.bukkit.entity.LivingEntity;
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

import net.minecraft.server.v1_16_R3.EntityLiving;
import net.minecraft.server.v1_16_R3.NBTTagCompound;

import me.jomi.mimiRPG.Baza;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.Edytory.EdytorOgólny;
import me.jomi.mimiRPG.util.AutoString;
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

// TODO: szablon

@Moduł
public class Bestie extends Komenda implements Listener, Przeładowalny, Zegar {
	public static final String prefix = Func.prefix(Bestie.class);
	public Bestie() {
		super("bestie");
		ustawKomende("edytujspawnerbesti", null, null);

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
		
		Bukkit.getWorlds().forEach(world -> Func.forEach(world.getLoadedChunks(), this::chunkLoad));
	}
	
	public static final String tagBesti = "mimiBestia";
	public static final String metaBesti = "mimiBestia";
	public static final String metaSpawnera = "mimiBestiaSpawner";
	
	public static class Grupa<T> extends AutoString {
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
	public static class DropRPG extends AutoString {
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
		public boolean dropnij(List<ItemStack> dropy) {
			ItemStack item = dropnij();
			Func.wykonajDlaNieNull(item, dropy::add);
			return item != null;
		}
	
		private String str;
		@Override
		public String toString() {
			if (str == null) {
				StringBuilder strB = new StringBuilder();
				
				strB.append(Ranga.ranga(item).kolor);
				strB.append(Func.nazwaItemku(item));
				strB.append("§a");
				boolean nierówne = min_ilość != max_ilość;
				if (min_ilość != 1 || nierówne)
					strB.append(" x").append(min_ilość);
				if (nierówne)
					strB.append('-').append(max_ilość);
				
				if (szansa < 1)
					strB.append(' ').append(Func.DoubleToString(Func.zaokrąglij(szansa * 100, 2))).append('%');
				
				str = strB.toString();
				if (str.contains("&%"))
					str = Func.koloruj(str);
				
			}
			return str;
		}
	}
	public static class Bestia extends AutoString{
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
		
		public final ItemStack hełm;
		public final ItemStack klata;
		public final ItemStack spodnie;
		public final ItemStack buty;
		public final ItemStack broń;
		public final ItemStack broń_lewa;
		
		Bestia(List<DropRPG> dropy, double kasa, int exp, int exp_łowcy, String kategoria, String grupa, String nazwa,
				EntityType mob, double hp, double dmg, double def, double speed, ItemStack ikona, int slot,
				ItemStack hełm, ItemStack klata, ItemStack spodnie, ItemStack buty, ItemStack broń, ItemStack broń_lewa) {
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
			
			this.hełm = hełm;
			this.klata = klata;
			this.spodnie = spodnie;
			this.buty = buty;
			this.broń = broń;
			this.broń_lewa = broń_lewa;
			
			this.ikona = ikona != null ? ikona : Func.stwórzItem(Material.BONE, "&c" + nazwa);
			if (!this.ikona.hasItemMeta() || !this.ikona.getItemMeta().hasDisplayName())
				Func.nazwij(this.ikona, "&c" + nazwa);
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

			LivingEntity bukkit = (LivingEntity) mob.getBukkitEntity();
			
			Func.ustawMetadate(bukkit, metaBesti, this);
			mob.addScoreboardTag(tagBesti);
			
			Func.wykonajDlaNieNull(hełm,	 bukkit.getEquipment()::setHelmet);
			Func.wykonajDlaNieNull(klata,	 bukkit.getEquipment()::setChestplate);
			Func.wykonajDlaNieNull(spodnie,	 bukkit.getEquipment()::setLeggings);
			Func.wykonajDlaNieNull(buty,	 bukkit.getEquipment()::setBoots);
			Func.wykonajDlaNieNull(broń,	 bukkit.getEquipment()::setItemInMainHand);
			Func.wykonajDlaNieNull(broń_lewa,bukkit.getEquipment()::setItemInOffHand);
			
			return bukkit;
		}
	
		public int getKille(GraczRPG gracz) {
			return gracz.getBestie(this).getInt("kill");
		}
	}
	
	
	public Bestia bestia(Entity mob) {
		if (mob.hasMetadata(metaBesti))
			return (Bestia) mob.getMetadata(metaBesti).get(0).value();
		else
			return null;
	}
	
	private void chunkLoad(Chunk chunk) {
		Func.forEach(chunk.getEntities(), e -> {
			if (e.getScoreboardTags().contains(tagBesti) && !e.hasMetadata(metaBesti))
				e.remove();
		});
	}
	@EventHandler(priority = EventPriority.LOW)
	public void chunkLoad(ChunkLoadEvent ev) {
		chunkLoad(ev.getChunk());
	}
	@EventHandler(priority = EventPriority.LOW)
	public void śmierćMoba(EntityDeathEvent ev) {
		if (ev.getEntity() instanceof Player) {
			if (ev.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
				EntityDamageByEntityEvent ev2 = (EntityDamageByEntityEvent) ev.getEntity().getLastDamageCause();
				Func.wykonajDlaNieNull(bestia(ev2.getDamager()), bestia -> {
					NBTTagCompound data = GraczRPG.gracz((Player) ev.getEntity()).getBestie(bestia);
					data.setInt("zgon", data.getInt("zgon") + 1);
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
			
			List<DropRPG> dropnięte = new ArrayList<>();
			bestia.dropy.forEach(drop -> {
				if (drop.dropnij(ev.getDrops()))
					dropnięte.add(drop);
			});
			ev.setDroppedExp(bestia.exp);
			
			Func.wykonajDlaNieNull(ev.getEntity().getKiller(), killer -> {
				GraczRPG gracz = GraczRPG.gracz(killer);
				gracz.ścieżka_łowca.zwiększExp(bestia.exp_łowcy);
				gracz.dodajKase(bestia.kasa);

				NBTTagCompound data = gracz.getBestie(bestia);
				int kille = data.getInt("kill");
				if (kille == 0) {
					Func.powiadom(prefix, killer, "Pokonałeś %s! Od znajdziesz go pod §a/bestie", bestia.nazwa);
					Main.log(prefix + "%s zabił po raz pierwszy bestie %s", killer.getName(), bestia.nazwa);
				}
				data.setInt("kill", kille + 1);
				
				dropnięte.forEach(drop -> {
					int[] dropy = data.getIntArray("dropy");
					int hash = Func.nazwaItemku(drop.item).hashCode();
					boolean był = false;
					for (int _drop : dropy)
						if (był = (_drop == hash))
							break;
					if (!był) {
						int[] nowe = new int[dropy.length + 1];
						for (int i=0; i < dropy.length; i++)
							nowe[i] = dropy[i];
						nowe[dropy.length] = hash;
						data.setIntArray("dropy", nowe);
						Func.powiadom(prefix, killer, "Zdobyłeś %s z bestii %s, od teraz będziesz widzieć to pod /bestie!", Func.nazwaItemku(drop.item), bestia.nazwa);
						Main.log(prefix + "%s wydropił %s z besti %s po raz pierwszy", killer.getName(), Func.nazwaItemku(drop.item), bestia.nazwa);
					} else
						if (Ranga.ranga(drop.item).ordinal() > 1)
							Func.powiadom(prefix, killer, "Znalazłeś %s!", Func.nazwaItemku(drop.item));
				});
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
			if (locRóg1 == null || locRóg2 == null || kategoria == null)
				return;
			
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
				if (loc.getBlock().isPassable() && loc.clone().add(0, 1, 0).getBlock().isPassable()) {
					if (loc.clone().add(0, -1, 0).getBlock().getType().isAir()) {
						while (loc.add(0, -1, 0).getBlock().getType().isAir());
						loc.add(0, 1, 0);
					}
					break;
				}
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
	
	EdytorOgólny<Spawner> edytor = new EdytorOgólny<>("edytujspawnerbesti", Spawner.class);
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

		GraczRPG gracz = GraczRPG.gracz(p);
		
		Bestia.mapa.get(kategoria).mapa.values().forEach(grupa -> {
			ItemStack item = grupa.item.clone();
			
			ItemMeta meta = item.getItemMeta();
			List<String> lore = Func.nieNull(meta.getLore());
			
			AtomicInteger odblokowane = new AtomicInteger();
			AtomicInteger kille = new AtomicInteger();
			AtomicInteger zgony = new AtomicInteger();
			
			grupa.mapa.values().forEach(bestia -> {
				NBTTagCompound dane = gracz.getBestie(bestia);
				int kile = dane.getInt("kill");
				if (kile > 0)
					odblokowane.getAndIncrement();
				kille.getAndAdd(kile);
				zgony.getAndAdd(dane.getInt("zgon"));
			});
			
			lore.add("§7Kille§8:§a " + kille.get());
			lore.add("§7Zgony§8:§a " + zgony.get());
			lore.add(" ");
			
			lore.add(Func.msg("Odblokowano %s/%s", odblokowane.get(), grupa.mapa.size()));
			lore.add("§a|" + Func.progres(odblokowane.get(), grupa.mapa.size(), 20, "-", "§a", "§7") + "| (" +
						Func.zaokrąglij(odblokowane.get() / (double) grupa.mapa.size() * 100, 1) + "%)");
			lore.add(" ");
			

			meta.setLore(lore);
			item.setItemMeta(meta);
			
			inv.setItem(grupa.slot, item);
		});
		
		p.openInventory(inv);
	}
	void otwórzPanel(Player p, String kategoria, String grupa) {
		Inventory inv = panelBestie.stwórz(new MonoKrotka<String>(kategoria, grupa), 3, "&4&l" + grupa);
		Func.ustawPuste(inv, Baza.pustySlotCzarny);
		
		GraczRPG gracz = GraczRPG.gracz(p);
		
		Bestia.mapa.get(kategoria).mapa.get(grupa).mapa.values().forEach(bestia -> {
			ItemStack item = bestia.ikona.clone();
			
			ItemMeta meta = item.getItemMeta();
			List<String> lore = Func.nieNull(meta.getLore());
			
			NBTTagCompound dane = gracz.getBestie(bestia);
			int kille = dane.getInt("kill");
			if (kille > 0) {
				
				if (bestia.kasa		 != 0) lore.add("§7Monety§8: §6"	+ RPG.monety(bestia.kasa));
				if (bestia.exp_łowcy != 0) lore.add("§7Exp Łowcy§8: §6"	+ Func.IntToString(bestia.exp_łowcy));
				if (bestia.exp		 != 0) lore.add("§7Exp§8: §6"		+ Func.IntToString(bestia.exp));
				lore.add(" ");
				
				lore.add("§7Kille§8: §a" + kille);
				lore.add("§7Zgony§8: §a" + dane.getInt("zgon"));
				lore.add(" ");
				
				if (!bestia.dropy.isEmpty()) {
					lore.add("§6§lDropy:");
					Map<Ranga, List<DropRPG>> mapaDropów = new EnumMap<>(Ranga.class);
					bestia.dropy.forEach(drop -> {
						Ranga ranga = Ranga.ranga(drop.item);
						List<DropRPG> lista = mapaDropów.get(ranga);
						if (lista == null)
							mapaDropów.put(ranga, lista = new ArrayList<>());
						lista.add(drop);
					});
					
					Func.forEach(Ranga.values(), ranga -> {
						Func.wykonajDlaNieNull(mapaDropów.get(ranga), lista -> {
							lore.add(Func.enumToString(ranga));
							lista.forEach(drop -> {
								String nazwa = Func.nazwaItemku(drop.item);
								int hash = nazwa.hashCode();
								boolean był = false;
								for (int dropnięte : dane.getIntArray("dropy"))
									if (był = (dropnięte == hash))
										break;
								lore.add(był ? "§b- " + drop.toString() : "§7???");
							});
							lore.add(" ");
						});
					});
				}
			} else {
				item.setType(Material.GRAY_DYE);
				lore.add("§7????");
			}
			
			meta.setLore(lore);
			item.setItemMeta(meta);
			
			inv.setItem(bestia.slot, item);
		});
		
		p.openInventory(inv);
	}
	
	static List<Spawner> spawnery = new ArrayList<>();
	
	@Override
	public void przeładuj() {
		Bestia.mapa.clear();
		
		List<Runnable> odłożone = new ArrayList<>();
		
		File dir = new File(Main.path + "Bestie");
		dir.mkdirs();
		Func.forEach(dir.listFiles(), dirKategoria-> {
			String kategoria = dirKategoria.getName();
			Func.forEach(dirKategoria.listFiles(), fConfigGrupy -> {
				Config config = new Config(fConfigGrupy);

				if (fConfigGrupy.getName().equals("info.yml")) {
					odłożone.add(() -> {
						Grupa<Grupa<Bestia>> grp = Bestia.mapa.get(kategoria);
						grp.setItem(config.wczytajItem("item"));
						grp.slot = config.wczytajInt("slot");
					});
					return;
				}
				
				String grupa = fConfigGrupy.getName().substring(0, fConfigGrupy.getName().length() - 4);
				config.klucze().forEach(nazwa -> {
					ConfigurationSection sekcja = config.sekcja(nazwa);
					
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
								ZfaktoryzowaneItemy.dajItem(części.get(0)),
								Func.Double(części.get(1)),
								Func.Int(min_max.get(0)),
								Func.Int(min_max.get(1))
								));
					});
					
					Function<String, ItemStack> func = str -> {
						if (str == null)
							return null;
						
						List<String> części = Func.tnij(str, " ");
						
						ItemStack item = Func.stwórzItem(Func.StringToEnum(Material.class, części.remove(0)));
						
						for (String część : części) {
							if (część.equalsIgnoreCase("ench"))
								Func.połysk(item);
							else if (część.startsWith("#")) {
								UnaryOperator<Integer> parse = i -> Integer.parseInt(część.substring(i, i+2), 16);
								Func.pokolorujZbroje(item, Color.fromRGB(parse.apply(1), parse.apply(3), parse.apply(5)));
							} else
								Main.warn(prefix + "Niepoprawny argument \"" + część + "\" w dropie bestii " + nazwa);
						}
						
						return item;
					};
					
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
							Config.item(sekcja.get("info.item")),
							sekcja.getInt("info.slot"),
							func.apply(sekcja.getString("itemy.hełm")),
							func.apply(sekcja.getString("itemy.zbroja")),
							func.apply(sekcja.getString("itemy.spodnie")),
							func.apply(sekcja.getString("itemy.buty")),
							func.apply(sekcja.getString("itemy.broń")),
							func.apply(sekcja.getString("itemy.broń_lewa"))
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
		
		spawnery.clear();
		Config config = new Config("configi/Bestie Spawnery");
		config.klucze().forEach(klucz -> spawnery.add(config.wczytajPewny(klucz)));
		
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("wczytane Bestie/Spawnery", Bestia.mapa.size() + "/" + spawnery.size());
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equals("edytujspawnerbesti"))
			return edytor.wymuśConfig_onTabComplete(new Config("configi/Bestie Spawnery"), sender, label, args);
		return null;
	}
	@Override
	public boolean wykonajKomende(CommandSender sender, Command cmd, String label, String[] args) throws MsgCmdError {
		if (cmd.getName().equals("edytujspawnerbesti"))
			return edytor.wymuśConfig_onCommand(prefix, "configi/Bestie Spawnery", sender, label, args);
		
		if (!(sender instanceof Player))
			throwFormatMsg("Musisz być graczem żeby tego użyć");
		
		otwórzPanel((Player) sender);
		
		return true;
	}

	@Override
	public int czas() {
		spawnery.forEach(Spawner::czas);
		return 20;
	}
}
