package me.jomi.mimiRPG.Customizacja;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduły.Moduł;
import me.jomi.mimiRPG.util.Drop;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
public class CustomowyDropMiedziany implements Listener, Przeładowalny {
	
	final Pattern pattern = Pattern.compile("(WAXED_)?(EXPOSED_)?(WEATHERED_)?(OXIDIZED_)?(CUT_)?COPPER(_BLOCK)?(_SLAB)?(_STAIRS)?");
	
	final Drop[] dropy = new Drop[3];
	
	@EventHandler
	public void odrdzewianie(PlayerInteractEvent ev) {
		if (ev.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		
		Block blok = ev.getClickedBlock();
		if (blok == null) return;
		
		Material mat = blok.getType();
		if (mat == Material.COPPER_ORE) return;
		if (!mat.toString().contains("COPPER")) return;
		
		Matcher matcher = pattern.matcher(mat.toString());
		if (!matcher.matches()) return;
		if (matcher.group(1) != null) return;
		
		
		int drop;
		
		if (matcher.group(2) != null)	   drop = 0;
		else if (matcher.group(3) != null) drop = 1;
		else if (matcher.group(4) != null) drop = 2;
		else return;
		
		
		Bukkit.getScheduler().runTask(Main.plugin, () -> {
			if (mat != blok.getType() && !blok.getType().toString().startsWith("WAXED_"))
				Func.wykonajDlaNieNull(dropy[drop], _drop -> _drop.dropnij(ev.getInteractionPoint()));
		});
	}

	
	
	@Override
	public void przeładuj() {
		dropy[0] = Main.ust.wczytajDrop("CustomowyDropMiedziany.exposed");
		dropy[1] = Main.ust.wczytajDrop("CustomowyDropMiedziany.weathered");
		dropy[2] = Main.ust.wczytajDrop("CustomowyDropMiedziany.oxidized");
	}
	@Override
	public Krotka<String, Object> raport() {
		int ile = 0;
		for (Drop drop : dropy)
			if (drop != null)
				ile++;
		return Func.r("Dropy z Miedzi", ile);
	}
}
