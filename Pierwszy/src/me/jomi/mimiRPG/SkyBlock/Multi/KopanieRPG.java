package me.jomi.mimiRPG.SkyBlock.Multi;

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
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.Packet;
import net.minecraft.server.v1_16_R3.PacketPlayInBlockDig;
import net.minecraft.server.v1_16_R3.PacketPlayOutBlockBreakAnimation;
import net.minecraft.server.v1_16_R3.SoundCategory;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.SkyBlock.Multi.KopanieRPG.Api.LiczeniePrędkościKopaniaEvent;
import me.jomi.mimiRPG.SkyBlock.Multi.KopanieRPG.Api.WykopanyBlokEvent;
import me.jomi.mimiRPG.util.Config;
import me.jomi.mimiRPG.util.Drop;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.NMS;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
public class KopanieRPG extends PacketAdapter implements Listener, Przeładowalny {
	public static class Api {
		public static class WykopanyBlokEvent extends BlockBreakEvent {
			public final List<Drop> dropy = new ArrayList<>();
			
			public WykopanyBlokEvent(Player p, Block block) {
				super(block, p);
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
		public final Material mat;
		public final Drop drop;
		public final int wytrzymałośćBloku;

		public Blok(Material mat, Drop drop, int wytrzymałośćBloku) {
			this.mat = mat;
			this.drop = drop;
			this.wytrzymałośćBloku = wytrzymałośćBloku;
			bloki.put(mat, this);
		}
		
		
		static final Map<Material, Blok> bloki = new EnumMap<>(Material.class);
		public static Blok daj(Material mat) {
			return bloki.get(mat);
		}
		public static int wytrzymałość(Material mat) {
			Blok blok = daj(mat);
			return blok == null ? Integer.MAX_VALUE : blok.wytrzymałośćBloku;
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
		case DROP_ITEM:
		case DROP_ALL_ITEMS:
			niszcz(p, packet.b(), -1);
		case START_DESTROY_BLOCK:
			niszczenie(p, packet.b());
			break;
		case STOP_DESTROY_BLOCK:
		case ABORT_DESTROY_BLOCK:
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
			LiczeniePrędkościKopaniaEvent ev = new LiczeniePrędkościKopaniaEvent(p, NMS.loc(p.getWorld(), pos).getBlock());
			Bukkit.getPluginManager().callEvent(ev);
			
			if (ev.isCancelled())
				return;
			
			int mocGracza = (int) ((ev.graczRPG.prędkośćKopania.getBaza() + ev.dodatkowa_baza) * ev.graczRPG.prędkośćKopania.getMnożnik() * ev.dodatkowy_mnożnik);
			int mocBloku  = ev.wytrzymałośćBloku;
			int kontrolny = this.kontrolny++;
			
			Func.ustawMetadate(p, metaKontrolny, kontrolny);
			niszczenie(p, pos, mocGracza, mocBloku, mocBloku, kontrolny);
		}
	}
	private void niszczenie(Player p, BlockPosition pos, int mocGracza, int wytrzymałość, int pozostało, int kontrolny) {
		if (!p.hasMetadata(metaKontrolny) || p.getMetadata(metaKontrolny).get(0).asInt() != kontrolny) return;
		
		if (pozostało <= 0) {
			Location loc = new Location(p.getWorld(), pos.getX(), pos.getY(), pos.getZ());
			
			WykopanyBlokEvent ev = new WykopanyBlokEvent(p, loc.getBlock());
			Bukkit.getPluginManager().callEvent(ev);
			
			if (ev.isCancelled()) return;
			
			nms(p).getWorld().playSound(null, pos, ((CraftBlock) ev.getBlock()).getNMS().getStepSound().breakSound, SoundCategory.BLOCKS, 1f, 1f);
			
			if (ev.isDropItems())
				ev.getBlock().breakNaturally(p.getInventory().getItemInMainHand());
			else
				ev.getBlock().setType(Material.AIR);
			
			mapaNiszczących.remove(p.getUniqueId(), pos);
			return;
		}
		
		p.addPotionEffect(efekt);
		
		double procent = (double) pozostało / (double) wytrzymałość;
		
		niszcz(p, pos, (int) ((1 - procent) * 11) - 1);
		
		Func.opóznij(pozostało > mocGracza ? 5 : (int) (5 * (1 - (double) pozostało / (double) mocGracza)),
				() -> niszczenie(p, pos, mocGracza, wytrzymałość, pozostało - mocGracza, kontrolny));
	}

	public static void niszcz(Player p, BlockPosition pos, int lvl) {
		Packet<?> packet = new PacketPlayOutBlockBreakAnimation(p.getEntityId() + 1, pos, lvl);
		double zasięg = 10;
		
		Runnable wyślij = () -> p.getWorld().getNearbyEntities(new Location(p.getWorld(), pos.getX(), pos.getY(), pos.getZ()), zasięg, zasięg, zasięg,
				e -> e instanceof Player).forEach(gracz -> nms((Player) gracz).playerConnection.sendPacket(packet));
		
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
		
		
		WykopanyBlokEvent ev2 = new WykopanyBlokEvent(ev.getPlayer(), ev.getBlock());
		
		ev2.setCancelled(ev.isCancelled());
		ev2.setDropItems(ev.isDropItems());
		ev2.setExpToDrop(ev.getExpToDrop());
		
		ev.getBlock().getDrops(ev.getPlayer().getInventory().getItemInMainHand(), ev.getPlayer()).forEach(item -> ev2.dropy.add(new Drop(item)));
		
		Bukkit.getPluginManager().callEvent(ev2);

		ev.setCancelled(ev2.isCancelled());
		ev.setDropItems(ev2.isDropItems());
		ev.setExpToDrop(ev2.getExpToDrop());
		
		if (!ev.isCancelled() && ev.isDropItems() && ev.getPlayer().getGameMode() != GameMode.CREATIVE)
			ev2.dropy.forEach(drop -> drop.dropnij(ev.getBlock().getLocation().add(.5, .5, .5)));
		ev.setDropItems(false);
	}
	
	
	@Override
	public void przeładuj() {
		Blok.bloki.clear();
		
		Config config = new Config("BlokiRPG");
		
		config.klucze().forEach(klucz -> {
			Material mat = Func.StringToEnum(Material.class, klucz);
			ConfigurationSection sekcja = config.sekcja(klucz);
			
			new Blok(mat, Config.drop(sekcja.get("drop")), sekcja.getInt("wytrzymałość", 2000));
		});
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Bloki RPG", Blok.bloki.size());
	}
}
