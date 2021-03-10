package me.jomi.mimiRPG.Maszyny;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Container;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Mapowany;
import me.jomi.mimiRPG.Customizacja.CustomoweItemy;
import me.jomi.mimiRPG.SkyBlock.SkyBlock;
import me.jomi.mimiRPG.SkyBlock.SkyBlock.Wyspa;
import me.jomi.mimiRPG.util.Cena;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Panel;
import me.jomi.mimiRPG.util.Przeładowalny;
import me.jomi.mimiRPG.util.Zegar;

public abstract class ModułMaszyny implements Listener, Zegar, Przeładowalny {
	public static final String prefix = Func.prefix("Maszyny");
	
	public abstract static class Maszyna extends Mapowany {
		@Mapowane Location locShulker;
		@Mapowane int lvlTicki;
		
		Holder holder;
		
		protected void postaw() {
			locShulker.getBlock().setType(getModuł().getShulkerType());
			Container shulker = (Container) locShulker.getBlock().getState();
			shulker.setCustomName(Func.losujPrzejścieKolorów("&l&o" + getModuł().getClass().getSimpleName()));
			shulker.update();
			getModuł().dodajMaszyne(this);
			zapisz();
		}
		protected void zlikwiduj() {
			locShulker.getBlock().setType(Material.AIR);
			getModuł().getConfig().ustaw_zapisz(getConfigSc(), null);
			getModuł().zapomnijMaszyne(this);
		}

		private int wykonaneTicki = 0;
 		public boolean włącz() {
			if (++wykonaneTicki >= getModuł().ulepszeniaPrędkości.get(lvlTicki).b) {
				if (locShulker.getBlock().getType() != getModuł().getShulkerType())
					return false;
				wykonaj();
				wykonaneTicki = 0;
				return true;
			}
			return false;
		}
 		
		protected abstract void wykonaj();
		
		public String getConfigSc() {
			return Func.locBlockToString(locShulker);
		}
		public Container getContainer() {
			return (Container) locShulker.getBlock().getState();
		}
		
		public abstract ModułMaszyny getModuł();
		
		public void zapisz() {
			getModuł().getConfig().ustaw_zapisz(getConfigSc(), this);
		}
	}
	
	final Panel panel = new Panel(true);
	final String permBypass = Func.permisja(this.getClass() + ".bypass");
	protected final Map<Location, Maszyna> mapaMaszyn = new HashMap<>();
	
	public ModułMaszyny() {
		Main.dodajPermisje(permBypass);
	}
	
	protected void odświeżMaszyny(Collection<Maszyna> maszyny) {
		mapaMaszyn.clear();
		maszyny.forEach(this::dodajMaszyne);
	}
	protected void dodajMaszyne(Maszyna maszyna) {
		mapaMaszyn.put(maszyna.locShulker, maszyna);
	}
	protected void zapomnijMaszyne(Maszyna maszyna) {
		mapaMaszyn.remove(maszyna.locShulker);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void niszczenie(BlockBreakEvent ev) {
		if (!ev.isCancelled())
			Func.wykonajDlaNieNull(mapaMaszyn.get(ev.getBlock().getLocation()), maszyna -> {
				ev.setCancelled(true);
				maszyna.zlikwiduj();
				if (ev.getPlayer().getGameMode() != GameMode.CREATIVE)
					ev.getBlock().getWorld().dropItem(ev.getBlock().getLocation().add(.5, .5, .5), itemMaszyny.clone());
				ev.getPlayer().sendMessage(prefix + Func.msg("%s usuniety", this.getClass().getSimpleName()));
				Main.log(prefix + Func.msg("%s usunoł %s na koordynatach %s w świecie %s",
						ev.getPlayer().getName(), this.getClass().getSimpleName(), Func.locBlockToString(ev.getBlock().getLocation()), ev.getBlock().getWorld().getName()));
			});
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void stawianie(BlockPlaceEvent ev) {
		if (!ev.isCancelled() && itemMaszyny.isSimilar(ev.getItemInHand()))
			Bukkit.getScheduler().runTask(Main.plugin, () -> {
				ev.getBlock().setType(Material.AIR);
				postawMaszyne(ev.getPlayer(), ev.getBlock().getLocation()).postaw();
				ev.getBlock().getWorld().spawnParticle(Particle.CLOUD, ev.getBlock().getLocation().clone().add(.5, .5, .5), 200, 1, 1, 1, 0);
				ev.getPlayer().sendMessage(prefix + Func.msg("%s postawiony", this.getClass().getSimpleName()));
				Main.log(prefix + Func.msg("%s postawił %s na koordynatach %s w świecie %s",
						ev.getPlayer().getName(), this.getClass().getSimpleName(), Func.locBlockToString(ev.getBlock().getLocation()), ev.getBlock().getWorld().getName()));
			});
	}
	
	public class Holder extends Func.abstractHolder {
		final Map<ItemStack, BiConsumer<Player, Maszyna>> mapaItemów;
		final Maszyna maszyna;
		
		public Holder(int rzędy, String nazwa, Map<ItemStack, BiConsumer<Player, Maszyna>> mapaItemów, Maszyna maszyna) {
			super(rzędy, nazwa);
			Func.ustawPuste(inv);
			
			this.mapaItemów = mapaItemów;
			this.maszyna = maszyna;
			maszyna.holder = this;
			
			Iterator<ItemStack> it = mapaItemów.keySet().iterator();
			for (int i : Func.sloty(mapaItemów.size(), 1))
				inv.setItem(9 + i, it.next());
		}
		
	}
	@EventHandler
	public void interakcja(PlayerInteractEvent ev) {
		if (!ev.getPlayer().isSneaking() || ev.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		Func.wykonajDlaNieNull(mapaMaszyn.get(ev.getClickedBlock().getLocation()), maszyna -> {
			Wyspa wyspa;
			if (
					ev.getPlayer().hasPermission(permBypass) ||
					(
						Main.włączonyModół(SkyBlock.class) &&
						(wyspa = SkyBlock.Wyspa.wczytaj(ev.getClickedBlock().getLocation())) != null &&
						wyspa.permisje(ev.getPlayer()).dostęp_do_spawnerów_i_maszyn
					)) {
				ev.setCancelled(true);
				otwórzPanelMaszyny(ev.getPlayer(), maszyna);
			}
		});
	}
	@EventHandler
	public void klikanieEq(InventoryClickEvent ev) {
		Func.wykonajDlaNieNull(ev.getInventory().getHolder(), Holder.class, holder -> {
			ev.setCancelled(true);
			Func.wykonajDlaNieNull(holder.mapaItemów.get(ev.getCurrentItem()), cons -> cons.accept((Player) ev.getWhoClicked(), holder.maszyna));
		});
	}
	@EventHandler
	public void zamykanieEq(InventoryCloseEvent ev) {
		Func.wykonajDlaNieNull(ev.getInventory().getHolder(), Holder.class, holder ->
			Bukkit.getScheduler().runTask(Main.plugin, () -> {
				if (holder.getInventory().getViewers().size() <= 0)
					holder.maszyna.holder = null;
		}));
	}

	public static void otwórzPanelMaszyny(Player p, Maszyna maszyna) {
		if (maszyna.holder != null)
			p.openInventory(maszyna.holder.getInventory());
		else if (!maszyna.getModuł().getFunkcjePanelu(maszyna).isEmpty())
			p.openInventory(maszyna.getModuł().new Holder(3, "&1Konfiguracja " + maszyna.getModuł().getClass().getSimpleName(),
					maszyna.getModuł().getFunkcjePanelu(maszyna), maszyna).getInventory());
	}
	
	protected abstract Map<ItemStack, BiConsumer<Player, Maszyna>> getFunkcjePanelu(Maszyna m);
	public abstract Material getShulkerType();
	public final Config getConfig() {
		return new Config("configi/Maszyny/" + this.getClass().getSimpleName());
	};
	public abstract Maszyna postawMaszyne(Player p, Location loc);
	
	
	protected static ItemStack fabrykatorItemów(Material mat, String nazwa, int lvl, List<Krotka<Cena, Integer>> ceny, Function<Integer, String> poziomStr) {
		Krotka<Cena, Integer> upgr = ceny.get(lvl);
		ItemStack item = Func.stwórzItem(mat, "&9" + nazwa, "&aAktualny poziom: &e" + poziomStr.apply(upgr.b));
		Func.wykonajDlaNieNull((ceny.size() > lvl + 1) ? ceny.get(lvl + 1) : null, nast -> {
			Func.dodajLore(item, "&aNastępny poziom: &e" + poziomStr.apply(nast.b));
			Func.dodajLore(item, "&aCena: &e" + nast.a);
		});
		return item;
	}
	protected static BiConsumer<Player, Maszyna> fabrykatorFunkcji(List<Krotka<Cena, Integer>> ulepszenia,
			Function<Maszyna, Integer> lvl, Consumer<Maszyna> ulepsz) {
		return (p, m) -> 
			Func.wykonajDlaNieNull((ulepszenia.size() > lvl.apply(m) + 1) ? ulepszenia.get(lvl.apply(m) + 1).a : null, cena -> {
				if (!cena.staćGo(p))
					p.sendMessage(prefix + "Nie stać cię na to");
				else {
					cena.zabierz(p);
					ulepsz.accept(m);
					m.zapisz();
					p.closeInventory();
					if (m.holder != null) {
						ArrayList<HumanEntity> kopia = Lists.newArrayList(m.holder.getInventory().getViewers());
						kopia.forEach(e -> Func.wykonajDlaNieNull(e, HumanEntity::closeInventory));
						m.holder = null;
						kopia.forEach(e -> otwórzPanelMaszyny((Player) e, m));
					} else
						otwórzPanelMaszyny(p, m);
				}
			});
		
	}
	protected ItemStack standardowyItemFunckjiPrędkości(Maszyna m) {
		return fabrykatorItemów(
				Material.SUGAR,
				"Prędkość",
				m.lvlTicki,
				ulepszeniaPrędkości,
				ile -> Func.DoubleToString(ile / 20d) + " sek"
				);
	}
	protected void standardowaFunckjaPrędkości(Player p, Maszyna maszyna) {
		fabrykatorFunkcji(
				ulepszeniaPrędkości,
				m -> m.lvlTicki,
				m -> m.lvlTicki++
				).accept(p, maszyna);
	}
	
	ItemStack itemMaszyny;
	
	@Override
	public int czas() {
		mapaMaszyn.values().forEach(Maszyna::włącz);
		return 1;
	}

	List<Krotka<Cena, Integer>> ulepszeniaPrędkości = new ArrayList<>();
	
	
	protected void wczytajUlepszeniaStandardowo(List<Krotka<Cena, Integer>> ulepszenia, String nazwa, String kryterium) {
		ulepszenia.clear();
		Func.wykonajDlaNieNull(new Config("Maszyny").wczytajListeMap(this.getClass().getSimpleName() + "." + nazwa), lista ->
			lista.forEach(mapa ->
				ulepszenia.add(new Krotka<>(
					Cena.deserialize(mapa.getMap("cena"), Cena.class),
					mapa.getInt(kryterium)
				))));
		
	}
	@Override
	public void przeładuj() {
		wczytajUlepszeniaStandardowo(ulepszeniaPrędkości, "prędkość", "ticki");
		
		Config config = getConfig();
		config.przeładuj();
		
		odświeżMaszyny(config.wartości(Maszyna.class));
		
		itemMaszyny = Main.ust.wczytajItem("Maszyny." + this.getClass().getSimpleName() + ".Item Maszyny");
		if (itemMaszyny == null)
			itemMaszyny = Func.stwórzItem(getShulkerType(), "&6&l" + this.getClass().getSimpleName(),
					"&aTen przedmiot", "&aTo potężna maszyna", "&aZasilana tlenem z atmosfery");
		
		CustomoweItemy.customoweItemy.put(this.getClass().getSimpleName(), itemMaszyny);
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Wczytane maszyny " + this.getClass().getSimpleName(), mapaMaszyn.size());
	}
}

