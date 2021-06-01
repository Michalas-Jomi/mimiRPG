package me.jomi.mimiRPG.RPG_Ultra;

import java.util.function.Consumer;

import org.bukkit.attribute.Attribute;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.RPG_Ultra.GraczRPG.Api.ZmianaStatystykiGraczaEvent;

@Moduł(priorytet = Moduł.Priorytet.NAJWYŻSZY)
public class RPG implements Listener {
	@EventHandler(priority = EventPriority.LOWEST)
	public void zmianaStatystyki(ZmianaStatystykiGraczaEvent ev) {
		Consumer<Attribute> attr = at -> ev.getPlayer().getAttribute(at).setBaseValue(ev.statystyka.wartość());
		switch (ev.statystyka.atrybut) {
		case PRĘDKOŚĆ_CHODZENIA:	ev.getPlayer().setWalkSpeed((float) ev.statystyka.wartość()); break;
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
}
