package me.jomi.mimiRPG.RPG;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.minecraft.network.chat.ChatComponentText;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.RPG.GraczRPG.Api.ZmianaStatystykiGraczaEvent;
import me.jomi.mimiRPG.RPG.GraczRPG.Statystyka;
import me.jomi.mimiRPG.RPG.GraczRPG.StatystykaProcentowa;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.NMS;

@Moduł
public class TabInfo implements Listener {
	static final EntityPlayer[] fakeGracze = new EntityPlayer[80];
	static {
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer worldServer = null;
		try {
			worldServer = (WorldServer) ((Map<?, ?>) Func.dajZField(server, "R")).values().iterator().next();
		} catch (Throwable e) {
			e.printStackTrace();
		}
        
		for (int i=0; i < 80; i++) {
			GameProfile prof = new GameProfile(UUID.randomUUID(), i < 10 ? "!0" + i : "!" + i);
			prof.getProperties().put("textures", new Property("textures",
					"ewogICJ0aW1lc3RhbXAiIDogMTYxMzczNTk2NDgxMSwKICAicHJvZmlsZUlkIiA6ICI3MjM3MWFkZjJlOGQ0YzkyYTczNGE5M2M4Mjc5ZGViOSIsCiAgInByb2ZpbGVOYW1lIiA6ICJTdG9uZSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS85NmRhNzM1Yzg4ZTA4YjgwYmRmODE0MGIwYWRkOGQxODY5MGI4NWI0ZjkwMjViYjg4MWQ3YTE5MTM0MGYzYjMxIgogICAgfQogIH0KfQ==",
					"mSgiOk3sj2eX3KNQydJebpx6sjn7X9XdXDSZqSA+Tbs66gt5sQnXrE4E2dNr5ddSP1B1ldosNVEUc1nzlWalqyzD+lekDHNo3YuNSFtoAFIVyKfNqfwiAlyFFYvaPFSwA/v/pyjoCaKYt/oh8pLDcuNDVRxft3uTFG9RspzaOm7w40znFWOLKesSK2l5q+ijfH1hwFoBGloxHR5U/mC8tzox1gSlHNCZJfRMiOEPQ5ZnlHCCQDwLoRTpblFFRv8+iesLCqelxPPDodMhalIqmVfHPqys+KzYrSAYiuTCmL6TMo5Mzf/sgDZZupCHGpgwA6juJOXqIPUDL9fqY5a8xnSnGJB4bqG+93Fd/oOgO9zJYsWbOX07aiumzezkyl9EOoDKHw5O6Y4lPJiKzyEXB7E1Ig9l5PW8Rc6fxu9Qltqia3EJcx/ApZb3mG3WOsq6aBs1SSs3lCij/bUDUplKxDePGHppAj5+i3ogq9PWgYhco1QAY2XYAwEGEjAcnwGbNKwxaQNXYIbwAz7CG192kN3FGQXTiH5tGN0t+EmhTtkyFFA4TL7XHRUJ3S1Z34lRkVxHNbQX1Y+NC6DBNkWy6CxDLX6jDA0e6vMBDyfYRkQBlNvJMFTQW2lVS8vQ5i16SUf6O5l/soQYlppqvp5vpWJV7Km+4dnkPA8xVcj9PxA="));
			fakeGracze[i] = new EntityPlayer(server, worldServer, prof);
			ustawNazwe(i, " ");
		}
		
		ustawNazwe(62, "§4§lStatystyki");
		ustawNazwe(20, "§a§lOnline");
		ustawNazwe(40, "§a§lOnline");
		ustawNazwe(02, "§e§lInfo");
	}
	
	static EntityPlayer ustawNazwe(int id, String nazwa) {
		EntityPlayer ep = fakeGracze[id];
		ep.listName = new ChatComponentText(nazwa);
		return ep;
	}
	
	private static Thread threadOdświeżający;
	public static void odświeżGraczyOnline() {
		if (threadOdświeżający != null)
			try {
				threadOdświeżający.interrupt();
			} catch (Throwable e) {
			}
		
		threadOdświeżający = new Thread(() -> {
			EntityPlayer[] fakeGracze = new EntityPlayer[38];
			
			for (int i = 21; i < 40; i++) fakeGracze[i - 21]	  = TabInfo.fakeGracze[i];
			for (int i = 41; i < 60; i++) fakeGracze[i - 41 + 19] = TabInfo.fakeGracze[i];
			
			List<String> gracze = Func.wykonajWszystkim(Bukkit.getOnlinePlayers(), p -> Func.getDisplayName(p));
			Func.posortuj(gracze, Func::stringToDouble);
			for (int i = 0; i < fakeGracze.length; i++) {
				String nick = gracze.isEmpty() ? "" : gracze.remove(0);
				fakeGracze[i].listName = new ChatComponentText(nick);
			}
			
			PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.d, fakeGracze);
			Bukkit.getOnlinePlayers().forEach(p -> NMS.wyślij(p, packet));
		});
		threadOdświeżający.start();
	}
	
	
	EntityPlayer odświeżStatystykę(GraczRPG gracz, Statystyka stat) {
		int id = 63;
		switch (stat.atrybut) {
		case SZCZĘŚCIE: 		id++;
		case DEF_NIEZALEŻNY: 	id++;
		case DEF: 				id++;
		case SIŁA: 				id++;
		case KRYT_SZANSA: 		id++;
		case KRYT_DMG: 			id++;
		case PRĘDKOŚĆ_KOPANIA: 	id++;
		case PRĘDKOŚĆ_CHODZENIA:id++;
		break;
		default: return null;
		}
		
		return ustawNazwe(id, stat.atrybut + "§8: §f" + (stat instanceof StatystykaProcentowa ? ((StatystykaProcentowa) stat).procent() : (int) stat.wartość()));
	}
	
	@EventHandler
	public void zmianaStatystyki(ZmianaStatystykiGraczaEvent ev) {
		Func.wykonajDlaNieNull(odświeżStatystykę(GraczRPG.gracz(ev.getPlayer()), ev.statystyka),
				ep -> NMS.nms(ev.getPlayer()).b.sendPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.d, ep)));
	}
	@EventHandler
	public void join(PlayerJoinEvent ev) {
		GraczRPG gracz = GraczRPG.gracz(ev.getPlayer());
		gracz.getStaty().forEach(stat -> odświeżStatystykę(gracz, stat));
		
		NMS.nms(ev.getPlayer()).b.sendPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.a, fakeGracze));
		
		Bukkit.getScheduler().runTask(Main.plugin, TabInfo::odświeżGraczyOnline);
	}
	@EventHandler
	public void quit(PlayerQuitEvent ev) {
		Bukkit.getScheduler().runTask(Main.plugin, TabInfo::odświeżGraczyOnline);
	}
}
