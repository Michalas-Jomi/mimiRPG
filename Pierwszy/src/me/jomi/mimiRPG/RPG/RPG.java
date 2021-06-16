package me.jomi.mimiRPG.RPG;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.GenericAttributes;
import net.minecraft.server.v1_16_R3.NBTTagCompound;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.RPG.GraczRPG.Api.ZmianaStatystykiGraczaEvent;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.MimiThread;
import me.jomi.mimiRPG.util.NMS;

@Moduł(priorytet = Moduł.Priorytet.NAJWYŻSZY)
public class RPG implements Listener {
	public static final String prefix = Func.prefix(RPG.class);
	
	public RPG() {
		new MimiThread(() -> {
			while (true) {
				Bukkit.getOnlinePlayers().forEach(p -> {
					GraczRPG gracz = GraczRPG.gracz(p);
					synchronized(gracz) {
						if (System.currentTimeMillis() - gracz.ostActionBar < 1_500)
							return;
					}
					actionBar(gracz);
				});
				try {
					Thread.sleep(1_600L);
				} catch (InterruptedException e) {
					Func.throwEx(e);
				}
			}
		}).start();
	}
	
	// EventHandler
	@EventHandler(priority = EventPriority.LOWEST)
	public void zmianaStatystyki(ZmianaStatystykiGraczaEvent ev) {
		Consumer<Attribute> attr = at -> ev.getPlayer().getAttribute(at).setBaseValue(ev.statystyka.wartość());
		switch (ev.statystyka.atrybut) {
		case PRĘDKOŚĆ_CHODZENIA:	
			EntityPlayer nms = NMS.nms(ev.getPlayer());
			nms.abilities.walkSpeed = (float) (ev.statystyka.wartość() / 10f);
			nms.updateAbilities();
			nms.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(nms.abilities.walkSpeed);
			break;
		case SIŁA: 					attr.accept(Attribute.GENERIC_ATTACK_DAMAGE); 	break;
		case PRĘDKOŚĆ_ATAKU: 		attr.accept(Attribute.GENERIC_ATTACK_SPEED); 	break;
		case DEF:					attr.accept(Attribute.GENERIC_ARMOR); 			break;
		case SZCZĘŚCIE: 			attr.accept(Attribute.GENERIC_LUCK); 			break;
		case HP:
			attr.accept(Attribute.GENERIC_MAX_HEALTH);
			int scale;
			double hp = ev.statystyka.wartość();
			if		(hp <  60) 	scale = 2;
			else if (hp <  100)	scale = 4;
			else if (hp == 100)	scale = 6;
			else if (hp <  140)	scale = 8;
			else if (hp <  200)	scale = 10;
			else if (hp <  300)	scale = 12;
			else if (hp <  500)	scale = 14;
			else if (hp <  750)	scale = 16;
			else if (hp <  1000)scale = 18;
			else				scale = 20;
			ev.getPlayer().setHealthScale(scale);
			Bukkit.getScheduler().runTask(Main.plugin, () -> actionBar(GraczRPG.gracz(ev.getPlayer())));
			break;
		case PRĘDKOŚĆ_KOPANIA: 	break;
		case DEF_NIEZALEŻNY: 	break;
		case INTELIGENCJA: 		break;
		case KRYT_SZANSA: 		break;
		case KRYT_DMG: 			break;
		case UNIK:				break;
		}
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void zmianaItemu(PlayerItemHeldEvent ev) {
		if (ev.isCancelled()) return;
		
		PlayerInventory inv = ev.getPlayer().getInventory();
		GraczRPG gracz = GraczRPG.gracz(ev.getPlayer());
		
		BiConsumer<Integer, BiConsumer<Boost, GraczRPG>> cons = (slot, bic) ->
				Func.wykonajDlaNieNull(inv.getItem(slot),
					item -> Boost.getBoosty(item).forEach(
							boost -> bic.accept(boost, gracz)));
		
		cons.accept(ev.getPreviousSlot(), Boost::odaplikuj);
		cons.accept(ev.getNewSlot(),      Boost::zaaplikuj);
	}
	@EventHandler
	public void disconnect(PlayerQuitEvent ev) {
		GraczRPG.gracz(ev.getPlayer()).zapisz();
		ev.getPlayer().removeMetadata("mimiGraczRPG", Main.plugin);
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void dmgMonitor(EntityDamageEvent ev) {
		if (ev.isCancelled()) return;
		if (ev.getDamage() == 0) return;
		
		if (ev.getEntity() instanceof Player)
			Bukkit.getScheduler().runTask(Main.plugin, () -> actionBar(GraczRPG.gracz((Player) ev.getEntity())));
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public void dmg(EntityDamageEvent ev) {
		if (ev.getEntity() instanceof Player)
			ev.setDamage(ev.getDamage() - GraczRPG.gracz((Player) ev.getEntity()).defNiezależny.wartość());
	}
	@EventHandler
	public void dmgEntity(EntityDamageByEntityEvent ev) {
		if (ev.getEntity() instanceof Player && Func.losuj(GraczRPG.gracz((Player) ev.getEntity()).unik.wartość())) {
			ev.setCancelled(true);
			PokazywanyDmg.zrespDmg(ev.getEntity(), "§f§lUNIK");
		}
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void heal(EntityRegainHealthEvent ev) {
		if (ev.isCancelled()) return;
		if (ev.getAmount() == 0) return;
		
		if (ev.getEntity() instanceof Player)
			Bukkit.getScheduler().runTask(Main.plugin, () -> actionBar(GraczRPG.gracz((Player) ev.getEntity())));
	}
	@EventHandler
	public void join(PlayerJoinEvent ev) {
		Player p = ev.getPlayer();
		p.setHealthScaled(true);
		
		GraczRPG gracz = GraczRPG.gracz(p);
		
		PlayerInventory inv = p.getInventory();
		for (ItemStack item : new ItemStack[] {inv.getItemInMainHand(), inv.getHelmet(), inv.getChestplate(), inv.getLeggings(), inv.getBoots()})
			if (item != null && !item.getType().isAir())
				Boost.getBoosty(item).forEach(boost -> boost.zaaplikuj(gracz));
		
		if (!p.hasPlayedBefore())
			p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
	}
	@EventHandler
	public void jedzenie(FoodLevelChangeEvent ev) {
		ev.setCancelled(true);
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public void stawianie(BlockPlaceEvent ev) {
		if (ev.isCancelled()) return;
		if (ev.getPlayer().getGameMode() == GameMode.CREATIVE) return;
		if (TypItemu.typ(ev.getItemInHand()) == TypItemu.BLOK) return;
		
		ev.setCancelled(true);
	}
	@EventHandler(priority = EventPriority.LOWEST)
	public void zgon(PlayerDeathEvent ev) {
		ev.setKeepInventory(true);
		ev.setKeepLevel(true);
	}
	@EventHandler
	public void podnoszenie(EntityPickupItemEvent ev) {
		if (ev.getEntity() instanceof Player)
			Kolekcja.podniósł((Player) ev.getEntity(), ev.getItem().getItemStack());
	}
	
	// util
	public static String monety(double ile) {
		return "§6Ⓞ " + Func.DoubleToString(ile);
	}
	public static NBTTagCompound dataDajUtwórz(NBTTagCompound bazowy, String klucz) {
		if (!bazowy.hasKey(klucz))
			bazowy.set(klucz, new NBTTagCompound());
		return bazowy.getCompound(klucz);
	}
	
	public final static void actionBar(GraczRPG gracz) {
		actionBar(gracz, null);
	}
	public static void actionBar(GraczRPG gracz, Consumer<StringBuilder> cons) {
		double maxHp = gracz.p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
		double hp = gracz.p.getHealth();
		
		
		StringBuilder strB = new StringBuilder();
		
		strB.append("§c");
		strB.append(Func.IntToString((int) Math.min(hp, maxHp)));
		strB.append(" / ");
		strB.append(Func.IntToString((int) maxHp));
		strB.append(" ❤ ");
		
		if (cons != null)
			cons.accept(strB);
		
		gracz.p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(strB.toString()));
		
		synchronized(gracz) {
			gracz.ostActionBar = System.currentTimeMillis();
		}
	}
}
