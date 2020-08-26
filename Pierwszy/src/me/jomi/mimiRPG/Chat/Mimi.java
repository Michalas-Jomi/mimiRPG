package me.jomi.mimiRPG.Chat;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.jomi.mimiRPG.Func;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Napis;

public class Mimi extends Komenda {

	public Mimi() {
		super("mimi");
		Main.plugin.getCommand("mimi").setPermission(null);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

	@Override
	public boolean onCommand(CommandSender p, Command cmd, String label, String[] args) {
		p.sendMessage("§b>─────────{ §c§l◄§6mimiRPG§c§l► §b}─────────<");
		p.sendMessage("");
		p.sendMessage("§2ⓜⓘⓜⓘⓇⓅⒼ");
		p.sendMessage("");
		p.sendMessage("§ePlugin w całości napisany przezemnie");
		p.sendMessage("§ePonadto jest to mój pierwszy plugin więc");
		p.sendMessage("§eSię sporo przy nim nauczyłem");
		p.sendMessage("§1§l~~§9Michałas§1§l~~");
		p.sendMessage("");
		p.sendMessage("§b>─────────{ §c§l◄§6mimiRPG§c§l► §b}─────────<");
		
		Napis.wczytaj(Func.listToString(args, 0)).wyświetl(p);
		
		return true;
	}

}
