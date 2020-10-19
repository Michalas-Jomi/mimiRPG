package me.jomi.mimiRPG.Minigry;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.jomi.mimiRPG.EdytorOgólny;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Moduł;

@Moduł
public class TestCmd extends Komenda {

	EdytorOgólny edytor = new EdytorOgólny("/testcmd", Test.class);
	
	public TestCmd() {
		super("testcmd");
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return utab(args, "edytor");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		return edytor.onCommand(sender, label, args);
	}

}
