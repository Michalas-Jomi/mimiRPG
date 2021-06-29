package me.jomi.mimiRPG.RPG;

import static me.jomi.mimiRPG.util.NMS.nms;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

import net.minecraft.core.BlockPosition;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayInBlockDig;
import net.minecraft.network.protocol.game.PacketPlayOutBlockBreakAnimation;
import net.minecraft.sounds.SoundCategory;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.RPG.KopanieRPG.Api.WykopanyBlokEvent;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.ItemCreator;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.NMS;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
public class KopanieRPG extends PacketAdapter implements Listener, Przeładowalny {
	public static final String prefix = Func.prefix(KopanieRPG.class);
	
	public static class Api {
		public static class WykopanyBlokEvent extends BlockBreakEvent {
			public final List<DropRPG> dropy = new ArrayList<>();
			public final Blok blok;
			
			public WykopanyBlokEvent(Player p, Blok blok, Block block) {
				super(block, p);
				this.blok = blok;
				if (blok != null)
					dropy.add(blok.drop);
			}
			
			private static final HandlerList handlers = new HandlerList();
			public static HandlerList getHandlerList() { return handlers; }
			@Override public HandlerList getHandlers() { return handlers; }
		}
		public static class LiczeniePrędkościKopaniaEvent extends BlockBreakEvent {
			public final GraczRPG graczRPG;
			public final Blok blok;
			public double dodatkowa_baza = 0;
			public double dodatkowy_mnożnik = 1;
			public int wytrzymałośćBloku;
			public LiczeniePrędkościKopaniaEvent(Player p, Block blok) {
				super(blok, p);
				this.graczRPG = GraczRPG.gracz(p);
				this.blok = Blok.daj(blok.getType());
				this.wytrzymałośćBloku = this.blok == null ? Integer.MAX_VALUE : this.blok.wytrzymałośćBloku;
			}
			
			private static final HandlerList handlers = new HandlerList();
			public static HandlerList getHandlerList() { return handlers; }
			@Override public HandlerList getHandlers() { return handlers; }
		}
	}
	public static class Blok {
		static final Map<Material, Blok> bloki = new EnumMap<>(Material.class);
		
		public final int exp;
		public final DropRPG drop;
		public final Material mat;
		public final int exp_drwala;
		public final int exp_kopacza;
		public final DropRPG dropSilk;
		public final int wytrzymałośćBloku;
		public final TypItemu efektywneNarzędzie;
		
		public Blok(Material mat, TypItemu efektywneNarzędzie, DropRPG drop, DropRPG dropSilk, int wytrzymałośćBloku, int exp, int exp_kopacza, int exp_drwala) {
			this.exp = exp;
			this.mat = mat;
			this.drop = drop;
			this.dropSilk = dropSilk;
			this.exp_drwala = exp_drwala;
			this.exp_kopacza = exp_kopacza;
			this.wytrzymałośćBloku = wytrzymałośćBloku;
			this.efektywneNarzędzie = efektywneNarzędzie;
			
			bloki.put(mat, this);
		}
		
		
		public static Blok daj(Material mat) {
			return bloki.get(mat);
		}
		public static int wytrzymałość(Material mat) {
			Blok blok = daj(mat);
			return blok == null ? Integer.MAX_VALUE : blok.wytrzymałośćBloku;
		}
		
		@Override
		public String toString() {
			return String.format("Blok(%s, %s, %s)", mat, drop, wytrzymałośćBloku);
		}
	}
	
	public KopanieRPG() {
		super(Main.plugin, ListenerPriority.NORMAL, PacketType.Play.Client.BLOCK_DIG);
		ProtocolLibrary.getProtocolManager().addPacketListener(this);
	}
	
	volatile Map<UUID, BlockPosition> mapaNiszczących = new HashMap<>();
	final PotionEffect efekt = new PotionEffect(PotionEffectType.SLOW_DIGGING, 10, -1, false, false, false);
	@Override
    public void onPacketReceiving(PacketEvent ev) {
		PacketPlayInBlockDig packet;
		try {
			packet = (PacketPlayInBlockDig) ev.getPacket().getHandle();
		} catch (Throwable e) {
			return;
		}

		Player p = ev.getPlayer().getPlayer();
		
		if (p.getGameMode() == GameMode.CREATIVE) return;
		if (NMS.loc(p.getWorld(), packet.b()).getBlock().getType().getHardness() == 0) return;

		switch (packet.d()) {
		case e:
		case d:
			niszcz(p, packet.b(), -1);
		case a:
			niszczenie(p, packet.b());
			break;
		case c:
		case b:
			p.removeMetadata(metaKontrolny, Main.plugin);
			niszcz(p, packet.b(), -1);
			break;
		default:
			break;
		}
		
		//ev.setCancelled(true);
    }
	private int kontrolny = 0;
	private final String metaKontrolny = "mimiKopanieRPGKontrolny";
	private void niszczenie(Player p, BlockPosition pos) {
		if (!Bukkit.isPrimaryThread())
			Bukkit.getScheduler().runTask(Main.plugin, () -> niszczenie(p, pos));
		else {
			Blok blok = Blok.daj(NMS.loc(p.getWorld(), pos).getBlock().getType());
			int mocBloku  = blok == null ? -1 : blok.wytrzymałośćBloku;
			int kontrolny = this.kontrolny++;
			boolean efektywne = blok == null ? false : TypItemu.typ(p.getInventory().getItemInMainHand()).pasuje(blok.efektywneNarzędzie);
			
			Func.ustawMetadate(p, metaKontrolny, kontrolny);
			niszczenie(p, blok, efektywne, pos, GraczRPG.gracz(p), mocBloku, mocBloku == -1 ? Integer.MAX_VALUE : mocBloku, kontrolny);
		}
	}
	private void niszczenie(Player p, Blok blok, boolean efektywneNarzędzie, BlockPosition pos, GraczRPG graczRPG, int wytrzymałość, int pozostało, int kontrolny) {
		if (!p.hasMetadata(metaKontrolny) || p.getMetadata(metaKontrolny).get(0).asInt() != kontrolny) return;
		niszcz(p, p.getEntityId(), pos, -1);
		
		if (pozostało <= 0) {
			Location loc = new Location(p.getWorld(), pos.getX(), pos.getY(), pos.getZ());
			
			WykopanyBlokEvent ev = new WykopanyBlokEvent(p, blok, loc.getBlock());
			Bukkit.getPluginManager().callEvent(ev);
			
			if (ev.isCancelled()) return;
			
			nms(p).getWorld().playSound(null, pos, ((CraftBlock) ev.getBlock()).getNMS().getStepSound().aA, SoundCategory.e, 1f, 1f);
			
			if (ev.isDropItems()) {
				ev.getBlock().breakNaturally(p.getInventory().getItemInMainHand());
				ev.dropy.forEach(drop -> drop.dropnij(ev.getBlock().getLocation().add(.5, .5, .5)));
			} else
				ev.getBlock().setType(Material.AIR);
			
			mapaNiszczących.remove(p.getUniqueId(), pos);
			
			niszcz(p, pos, -1);
			return;
		}
		
		p.addPotionEffect(efekt);
		
		if (wytrzymałość != -1) {
			double procent = (double) pozostało / (double) wytrzymałość;
			
			niszcz(p, pos, (int) ((1 - procent) * 11) - 1);
			
			double prędkośćKopania = efektywneNarzędzie ? graczRPG.prędkośćKopania.wartość() : 100;
			Func.opóznij(pozostało > prędkośćKopania ? 5 : (int) (5 * (1 - pozostało / prędkośćKopania)),
					() -> niszczenie(p, blok, efektywneNarzędzie, pos, graczRPG, wytrzymałość, (int) (pozostało - prędkośćKopania), kontrolny));
		} else
			Func.opóznij(5, () -> niszczenie(p, blok, efektywneNarzędzie, pos, graczRPG, wytrzymałość, pozostało, kontrolny));
	}

	public static void niszcz(Player p, BlockPosition pos, int lvl) {
		niszcz(p, p.getEntityId() + 1, pos, lvl);
	}
	public static void niszcz(Player p, int entityId, BlockPosition pos, int lvl) {
		Packet<?> packet = new PacketPlayOutBlockBreakAnimation(entityId, pos, lvl);
		double zasięg = 10;
		
		Runnable wyślij = () -> p.getWorld().getNearbyEntities(new Location(p.getWorld(), pos.getX(), pos.getY(), pos.getZ()), zasięg, zasięg, zasięg,
				e -> e instanceof Player).forEach(gracz -> nms((Player) gracz).b.sendPacket(packet));
		
		if (Bukkit.isPrimaryThread())
			wyślij.run();
		else
			Bukkit.getScheduler().runTask(Main.plugin, wyślij);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void kopanie(BlockBreakEvent ev) {
		if (ev instanceof WykopanyBlokEvent)
			return;
		
		if (ev.getBlock().getType().getHardness() != 0 && ev.getPlayer().getGameMode() != GameMode.CREATIVE) {
			ev.setCancelled(true);
			return;
		}
		
		
		WykopanyBlokEvent ev2 = new WykopanyBlokEvent(ev.getPlayer(), Blok.daj(ev.getBlock().getType()), ev.getBlock());
		
		ev2.setCancelled(ev.isCancelled());
		ev2.setDropItems(ev.isDropItems());
		ev2.setExpToDrop(ev2.blok == null ? ev.getExpToDrop() : ev2.blok.exp);
		
		ev.getBlock().getDrops(ev.getPlayer().getInventory().getItemInMainHand(), ev.getPlayer()).forEach(item -> ev2.dropy.add(new DropRPG(item, 1, item.getAmount(), item.getAmount())));
		
		Bukkit.getPluginManager().callEvent(ev2);

		ev.setCancelled(ev2.isCancelled());
		ev.setDropItems(ev2.isDropItems());
		ev.setExpToDrop(ev2.getExpToDrop());
		
		if (!ev.isCancelled() && ev.isDropItems() && ev.getPlayer().getGameMode() != GameMode.CREATIVE)
			ev2.dropy.forEach(drop -> drop.dropnij(ev.getBlock().getLocation().add(.5, .5, .5)));
		ev.setDropItems(false);
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void wykopany(WykopanyBlokEvent ev) {
		if (!ev.isCancelled() && ev.blok != null) {
			GraczRPG gracz = GraczRPG.gracz(ev.getPlayer());
			gracz.ścieżka_kopacz.zwiększExp(ev.blok.exp_kopacza);
			gracz.ścieżka_drwal .zwiększExp(ev.blok.exp_drwala);
		}
	}
	
	@Override
	public void przeładuj() {
		Blok.bloki.clear();
		
		Config config = new Config("BlokiRPG");
		
		config.klucze().forEach(klucz -> {
			try {
				Material mat = Func.StringToEnum(Material.class, klucz);
				ConfigurationSection sekcja = config.sekcja(klucz);
				TypItemu narzędzie = Func.StringToEnum(TypItemu.class, sekcja.getString("efektywne narzędzie", "BRAK"));
				
				DropRPG drop = DropRPG.parse(sekcja.getString("drop"));
				
				new Blok(
						mat,
						narzędzie,
						drop,
						sekcja.contains("drop silk") ? DropRPG.parse(sekcja.getString("drop silk")) : drop,
								sekcja.getInt("wytrzymałość", 2000),
								sekcja.getInt("exp", 0),
								sekcja.getInt("exp kopacza", 1),
								sekcja.getInt("exp drwala", 0)
						);
			} catch (Throwable e) {
				Main.warn(prefix + Func.msg("%s problem z %s", e.getMessage(), klucz));
			}
		});
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Bloki RPG", Blok.bloki.size());
	}



	// DEBUG mdebug RPG.KopanieRPG generowanieConfiga()
	static void generowanieConfiga() {
		Config config = new Config("BlokiRPG");
		Func.forEach(Material.values(), mat -> {
			if (mat.toString().contains("shulker_box".toUpperCase()))
				return;
			
			if (mat.isBlock() && !mat.isAir()) {
				String sc = Func.enumToString(mat) + ".";
				config.ustaw(sc + "wytrzymałość", mat.getHardness() == -1 ? -1 : mat.getHardness() * 2000);
				config.ustaw(sc + "exp " + (mat.name().contains("_LOG") || mat.name().contains("_WOOD") ? "drwala" : "kopacza"), 1);
				
				World w = Bukkit.getWorlds().get(0);
				Location loc = new Location(w, 0, 1, 0);
				Block blok = loc.getBlock();
				blok.setType(mat, false);
				
				
				float[] speed = new float[] {0F};
				ItemStack item0[] = new ItemStack[1];
				Func.forEach(Material.values(), mat2 -> {
					if (mat2.isItem()) {
						ItemStack tool = Func.stwórzItem(mat2);
						net.minecraft.world.item.ItemStack nms = CraftItemStack.asNMSCopy(tool);
						float _speed = nms.getItem().getDestroySpeed(nms, NMS.nms(blok));
						if (_speed > speed[0]) {
							speed[0] = _speed;
							item0[0] = tool;
						}
					}
				});
				
				String eff = null;
				switch (item0[0].getType()) {
				case AIR:	eff = "Brak";			break;
				case GOLDEN_AXE: eff = "Siekiera";	break;
				case GOLDEN_PICKAXE: eff = "Kilof";	break;
				case GOLDEN_HOE: eff = "Motyka";	break;
				case GOLDEN_SHOVEL: eff = "Łopata";	break;
				case SHEARS: eff = "Norzyce";		break;
				case WOODEN_SWORD: eff = "Miecz";	break;
				default:
					Main.log("%s %s %s", item0[0], speed[0], mat);
				}
				
				if (eff != null)
					config.ustaw(sc + "efektywne narzędzie", eff);
				
				
				ItemStack itemK = item0[0];
				
				StringBuilder strB = new StringBuilder();
				blok.getDrops(itemK).forEach(item -> {
					if (!item.getType().isAir())
						strB.append(item.getType().toString().toLowerCase()).append(" 1.0 1-1 ");
				});
				if (!strB.toString().isEmpty())
					config.ustaw(sc + "drop", strB.toString().substring(0, strB.toString().length() - 1));
				
				StringBuilder strB2 = new StringBuilder();
				blok.getDrops(itemK.getType().isAir() ? itemK : ItemCreator.nowy(itemK).enchant(Enchantment.SILK_TOUCH, 1).stwórz()).forEach(item -> {
					if (!item.getType().isAir())
						strB2.append(item.getType().toString().toLowerCase()).append(" 1.0 1-1 ");
				});
				if (!strB2.toString().isEmpty())
					config.ustaw(sc + "drop silk", strB2.toString().substring(0, strB2.toString().length() - 1));
			}
		});
		config.zapisz();
	}
}
