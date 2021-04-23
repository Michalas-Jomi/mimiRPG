package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.google.common.collect.Sets;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.InteractManager;
import me.jomi.mimiRPG.util.InteractManager.Sposób;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
public class WyrzutniaBloków implements Listener, Przeładowalny {
	final String tagBloku = "mimiWyrzutniaBloków"; 
	
	ItemStack wyrzutnia;
	double moc;
	
	Set<String> cooldown = Sets.newConcurrentHashSet();
	public boolean interackcja(PlayerInteractEvent ev) {
		if (	wyrzutnia == null ||
				cooldown.contains(ev.getPlayer().getName())
				)
			return false;
		
		PlayerInventory inv = ev.getPlayer().getInventory();
		Func.wykonajDlaNieNull(inv.getItemInOffHand(), blok -> {
			if (blok.getType().isBlock()) {
				cooldown.add(ev.getPlayer().getName());
				Func.opóznij(3, () -> cooldown.remove(ev.getPlayer().getName()));
				
				przywołajBlok(ev.getPlayer(), blok);
				
				blok.setAmount(blok.getAmount() - 1);
				inv.setItemInOffHand(blok.getAmount() <= 0 ? null : blok);
				
				ev.setCancelled(true);
			}
		});
		return false;
	}
	
	public FallingBlock przywołajBlok(Player p, ItemStack item) {
		item = Func.ilość(item.clone(), 1);
		
		Location loc = p.getEyeLocation();
		FallingBlock blok = loc.getWorld().spawnFallingBlock(loc, Bukkit.createBlockData(item.getType()));
		
		blok.setVelocity(loc.getDirection().multiply(moc));
		blok.setHurtEntities(false);
		blok.setDropItem(true);
		Func.ustawMetadate(blok, tagBloku, new Krotka<>(p, item));
		
		return blok;
	}
	
	@EventHandler
	@SuppressWarnings("unchecked")
	public void stawianie(EntityChangeBlockEvent ev) {
		if (ev.getEntity().hasMetadata(tagBloku)) {
			Krotka<Player, ItemStack> krotka = (Krotka<Player, ItemStack>) ev.getEntity().getMetadata(tagBloku).get(0).value();
			
			BlockPlaceEvent event = new BlockPlaceEvent(ev.getBlock(), ev.getBlock().getState(), ev.getBlock(), krotka.b, krotka.a, true, EquipmentSlot.OFF_HAND);
			Bukkit.getPluginManager().callEvent(event);
			
			if (event.isCancelled()) {
				ev.setCancelled(true);
				Func.dajItem(krotka.a, krotka.b);
			}
		}
	}
	@EventHandler
	@SuppressWarnings("unchecked")
	public void wypadanieItemku(EntityDropItemEvent ev) {
		if (ev.getEntity().hasMetadata(tagBloku)) {
			Krotka<Player, ItemStack> krotka = (Krotka<Player, ItemStack>) ev.getEntity().getMetadata(tagBloku).get(0).value();
			ev.getItemDrop().setItemStack(krotka.b);
		}
		
	}
	

	@Override
	public void przeładuj() {//TODO szablon
		ItemStack stara = wyrzutnia;
		
		wyrzutnia = Main.ust.wczytajItem  ("WyrzutniaBloków.item");
		moc 	  = Main.ust.wczytajDouble("WyrzutniaBloków.moc");
		
		if (!wyrzutnia.equals(wyrzutnia)) {
			InteractManager.wyrejestruj(stara, Sposób.PRAWY);
			InteractManager.zarejestruj(wyrzutnia, Sposób.PRAWY, this::interackcja);
		}
	}
	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Wyrzutnia bloków", wyrzutnia == null ? "§cNieaktywna" : "§aAktywna");
	}

}
