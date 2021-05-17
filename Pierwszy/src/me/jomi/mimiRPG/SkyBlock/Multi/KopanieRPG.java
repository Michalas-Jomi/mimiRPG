package me.jomi.mimiRPG.SkyBlock.Multi;

import static me.jomi.mimiRPG.util.NMS.nms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
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
import me.jomi.mimiRPG.SkyBlock.Multi.KopanieRPG.Api.LicznieSzybkościKopaniaEvent;
import me.jomi.mimiRPG.SkyBlock.Multi.KopanieRPG.Api.WykopanyBlokEvent;
import me.jomi.mimiRPG.util.Drop;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.NMS;

@Moduł
public class KopanieRPG extends PacketAdapter implements Listener {
	public static class Api {
		public static class LicznieSzybkościKopaniaEvent extends BlockEvent {
			
			private final Player p;
			private double mnożnik = 1;
			private int podstawa = 100;
			
			public LicznieSzybkościKopaniaEvent(Player p, Block blok) {
				super(blok);
				this.p = p;
			}
			
			
			public Player getPlayer() {
				return p;
			}
			
			public double getMnożnik() {
				return mnożnik;
			}
			// mnożnik > 1
			public void dodajMnożnik(double mnożnik) {
				this.mnożnik *= mnożnik;
			}
			
			public int getDodatek() {
				return podstawa;
			}
			public void dodajDodatek(int dodatek) {
				this.podstawa += dodatek;
			}
			
			
			public int wartość() {
				return (int) (podstawa * mnożnik);
			}
			
			
			private static final HandlerList handlers = new HandlerList();
			public static HandlerList getHandlerList() { return handlers; }
			@Override public HandlerList getHandlers() { return handlers; }
		}

		public static class WykopanyBlokEvent extends BlockBreakEvent {
			public final List<Drop> dropy = new ArrayList<>();
			
			public WykopanyBlokEvent(Player p, Block block) {
				super(block, p);
			}
	
			private static final HandlerList handlers = new HandlerList();
			public static HandlerList getHandlerList() { return handlers; }
			@Override public HandlerList getHandlers() { return handlers; }
		}
	}
	
	public KopanieRPG() {
		super(Main.plugin, ListenerPriority.NORMAL, PacketType.Play.Client.BLOCK_DIG);
		ProtocolLibrary.getProtocolManager().addPacketListener(this);
	}
	
	volatile Map<UUID, BlockPosition> mapaNiszczących = new HashMap<>();
	final PotionEffect efekt = new PotionEffect(PotionEffectType.SLOW_DIGGING, 10, 2, false, false, false);
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
		case START_DESTROY_BLOCK:
			mapaNiszczących.put(p.getUniqueId(), packet.b());
			niszczenie(p, packet.b());
			break;
		case STOP_DESTROY_BLOCK:
		case ABORT_DESTROY_BLOCK:
			mapaNiszczących.remove(p.getUniqueId(), packet.b());
			niszcz(p, packet.b(), -1);
			break;
		default:
			return;
		}
		
		//ev.setCancelled(true);
    }
	private void niszczenie(Player p, BlockPosition pos) {
		if (!Bukkit.isPrimaryThread() )
			Bukkit.getScheduler().runTask(Main.plugin, () -> niszczenie(p, pos));
		else {
			int mocBloku = 1000;
			
			LicznieSzybkościKopaniaEvent ev = new LicznieSzybkościKopaniaEvent(p, NMS.loc(p.getWorld(), pos).getBlock());
			Bukkit.getPluginManager().callEvent(ev);
			niszczenie(p, pos, ev.wartość(), mocBloku, mocBloku);
		}
	}
	private void niszczenie(Player p, BlockPosition pos, int mocGracza, int wytrzymałość, int pozostało) {
		BlockPosition kontrolny = mapaNiszczących.get(p.getUniqueId());
		if (kontrolny == null || !kontrolny.equals(pos))
			return;
		
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
			
			return;
		}
		
		p.addPotionEffect(efekt);
		
		double procent = (double) pozostało / (double) wytrzymałość;
		
		niszcz(p, pos, (int) ((1 - procent) * 10));
		
		Func.opóznij(pozostało > mocGracza ? 5 : (int) (5 * (1 - (double) pozostało / (double) mocGracza)), () -> niszczenie(p, pos, mocGracza, wytrzymałość, pozostało - mocGracza));
	}

	public static void niszcz(Player p, BlockPosition pos, int lvl) {
		Packet<?> packet = new PacketPlayOutBlockBreakAnimation(p.getEntityId() + 1, pos, lvl);
		double zasięg = 10;
		
		Runnable wyślij = () -> {
			p.getWorld().getNearbyEntities(new Location(p.getWorld(), pos.getX(), pos.getY(), pos.getZ()), zasięg, zasięg, zasięg,
					e -> e instanceof Player).forEach(gracz -> nms((Player) gracz).playerConnection.sendPacket(packet));
		};
		
		if (Bukkit.isPrimaryThread())
			wyślij.run();
		else
			Bukkit.getScheduler().runTask(Main.plugin, wyślij);
	}


	@EventHandler(priority = EventPriority.HIGHEST)
	public void kopanie(BlockBreakEvent ev) {
		if (ev instanceof WykopanyBlokEvent)
			return;
		
		WykopanyBlokEvent ev2 = new WykopanyBlokEvent(ev.getPlayer(), ev.getBlock());
		
		ev2.setCancelled(ev.isCancelled());
		ev2.setDropItems(ev.isDropItems());
		ev2.setExpToDrop(ev.getExpToDrop());
		
		ev.getBlock().getDrops(ev.getPlayer().getInventory().getItemInMainHand(), ev.getPlayer()).forEach(item -> ev2.dropy.add(new Drop(item)));
		
		Bukkit.getPluginManager().callEvent(ev2);

		ev.setCancelled(ev2.isCancelled());
		ev.setDropItems(ev2.isDropItems());
		ev.setExpToDrop(ev2.getExpToDrop());
		
		if (ev.isDropItems())
			ev2.dropy.forEach(drop -> drop.dropnij(ev.getBlock().getLocation().add(.5, .5, .5)));
		ev.setDropItems(false);
	}
}
