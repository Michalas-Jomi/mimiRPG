package me.jomi.mimiRPG.RPG_Ultra;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.minecraft.server.v1_16_R3.ChatComponentText;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.MinecraftServer;
import net.minecraft.server.v1_16_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_16_R3.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import net.minecraft.server.v1_16_R3.PlayerInteractManager;
import net.minecraft.server.v1_16_R3.WorldServer;

import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.RPG_Ultra.GraczRPG.Api.ZmianaStatystykiGraczaEvent;
import me.jomi.mimiRPG.RPG_Ultra.GraczRPG.Statystyka;
import me.jomi.mimiRPG.RPG_Ultra.GraczRPG.StatystykaProcentowa;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.NMS;

@Moduł
public class TabInfo implements Listener {
	static final EntityPlayer[] fakeGracze = new EntityPlayer[80];
	static {
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer worldServer = server.worldServer.values().iterator().next();
        
		for (int i=0; i < 80; i++) {
			GameProfile prof = new GameProfile(UUID.randomUUID(), i < 10 ? "!0" + i : "!" + i);
			prof.getProperties().put("textures", new Property("textures",
					"ewogICJ0aW1lc3RhbXAiIDogMTYxMzczNTk2NDgxMSwKICAicHJvZmlsZUlkIiA6ICI3MjM3MWFkZjJlOGQ0YzkyYTczNGE5M2M4Mjc5ZGViOSIsCiAgInByb2ZpbGVOYW1lIiA6ICJTdG9uZSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS85NmRhNzM1Yzg4ZTA4YjgwYmRmODE0MGIwYWRkOGQxODY5MGI4NWI0ZjkwMjViYjg4MWQ3YTE5MTM0MGYzYjMxIgogICAgfQogIH0KfQ==",
					"mSgiOk3sj2eX3KNQydJebpx6sjn7X9XdXDSZqSA+Tbs66gt5sQnXrE4E2dNr5ddSP1B1ldosNVEUc1nzlWalqyzD+lekDHNo3YuNSFtoAFIVyKfNqfwiAlyFFYvaPFSwA/v/pyjoCaKYt/oh8pLDcuNDVRxft3uTFG9RspzaOm7w40znFWOLKesSK2l5q+ijfH1hwFoBGloxHR5U/mC8tzox1gSlHNCZJfRMiOEPQ5ZnlHCCQDwLoRTpblFFRv8+iesLCqelxPPDodMhalIqmVfHPqys+KzYrSAYiuTCmL6TMo5Mzf/sgDZZupCHGpgwA6juJOXqIPUDL9fqY5a8xnSnGJB4bqG+93Fd/oOgO9zJYsWbOX07aiumzezkyl9EOoDKHw5O6Y4lPJiKzyEXB7E1Ig9l5PW8Rc6fxu9Qltqia3EJcx/ApZb3mG3WOsq6aBs1SSs3lCij/bUDUplKxDePGHppAj5+i3ogq9PWgYhco1QAY2XYAwEGEjAcnwGbNKwxaQNXYIbwAz7CG192kN3FGQXTiH5tGN0t+EmhTtkyFFA4TL7XHRUJ3S1Z34lRkVxHNbQX1Y+NC6DBNkWy6CxDLX6jDA0e6vMBDyfYRkQBlNvJMFTQW2lVS8vQ5i16SUf6O5l/soQYlppqvp5vpWJV7Km+4dnkPA8xVcj9PxA="));
			fakeGracze[i] = new EntityPlayer(server, worldServer, prof, new PlayerInteractManager(worldServer));
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
				ep -> NMS.nms(ev.getPlayer()).playerConnection.sendPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, ep)));
	}
	@EventHandler
	public void join(PlayerJoinEvent ev) {
		GraczRPG gracz = GraczRPG.gracz(ev.getPlayer());
		gracz.getStaty().forEach(stat -> odświeżStatystykę(gracz, stat));
		
		NMS.nms(ev.getPlayer()).playerConnection.sendPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, fakeGracze));
	}
}
