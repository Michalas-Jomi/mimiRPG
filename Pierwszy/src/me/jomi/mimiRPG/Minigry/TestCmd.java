package me.jomi.mimiRPG.Minigry;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.jomi.mimiRPG.Config;
import me.jomi.mimiRPG.EdytorOgólny;
import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Moduł;

@Moduł
public class TestCmd extends Komenda {

	EdytorOgólny<Test> edytor;
	Config config = new Config("test");
	
	public TestCmd() {
		super("testcmd");
		edytor = new EdytorOgólny<>(config, "testsc1.testsc2", "/testcmd", Test.class);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return utab(args, "edytor");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		return edytor.onCommand(sender, args);
	}

}
