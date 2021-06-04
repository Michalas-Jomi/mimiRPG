package me.jomi.mimiRPG.RPG_Ultra;

import java.util.function.Consumer;

import org.bukkit.attribute.Attribute;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.PlayerInventory;

import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.GenericAttributes;

import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.RPG_Ultra.GraczRPG.Api.ZmianaStatystykiGraczaEvent;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.NMS;

@Moduł(priorytet = Moduł.Priorytet.NAJWYŻSZY)
public class RPG implements Listener {
	public static final String prefix = Func.prefix(RPG.class);
	
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void zmianaStatystyki(ZmianaStatystykiGraczaEvent ev) {
		Consumer<Attribute> attr = at -> ev.getPlayer().getAttribute(at).setBaseValue(ev.statystyka.wartość());
		switch (ev.statystyka.atrybut) {
		case PRĘDKOŚĆ_CHODZENIA:	
			EntityPlayer nms = NMS.nms(ev.getPlayer());
			nms.abilities.walkSpeed = (float) (ev.statystyka.wartość() / 2.0F);
			nms.updateAbilities();
			nms.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(nms.abilities.walkSpeed);
			break;
		case SIŁA: 					attr.accept(Attribute.GENERIC_ATTACK_DAMAGE); 	break;
		case PRĘDKOŚĆ_ATAKU: 		attr.accept(Attribute.GENERIC_ATTACK_SPEED); 	break;
		case HP: 					attr.accept(Attribute.GENERIC_MAX_HEALTH); 		break;
		case DEF:					attr.accept(Attribute.GENERIC_ARMOR); 			break;
		case SZCZĘŚCIE: 			attr.accept(Attribute.GENERIC_LUCK); 			break;
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
		
		Func.wykonajDlaNieNull(inv.getItem(ev.getPreviousSlot()),
				item -> Boost.getBoosty(item).forEach(
						boost -> boost.odaplikuj(gracz)));
		Func.wykonajDlaNieNull(inv.getItem(ev.getNewSlot()),
				item -> Boost.getBoosty(item).forEach(
						boost -> boost.zaaplikuj(gracz)));
	}

	public static String monety(double ile) {
		return "§6Ⓞ " + Func.DoubleToString(ile);
	}
}
