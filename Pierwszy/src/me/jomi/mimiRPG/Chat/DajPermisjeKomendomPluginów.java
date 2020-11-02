package me.jomi.mimiRPG.Chat;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommandYamlParser;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;

@Moduł
public class DajPermisjeKomendomPluginów {
	PluginManager pluginManager = Main.plugin.getServer().getPluginManager();
	
	public DajPermisjeKomendomPluginów() {
		Func.opóznij(1, this::ustawOgólne);
	}
	
	void ustawOgólne() {
		for (Plugin plugin: Bukkit.getPluginManager().getPlugins())
			for (Command _cmd : PluginCommandYamlParser.parse(plugin))
				ustaw(plugin, _cmd);
	}
	
	void ustaw(Plugin plugin, Command _cmd) {
		Command cmd = ((JavaPlugin) plugin).getCommand(_cmd.getName());
		if (cmd == null) {
			Main.warn("[DajPermisjeKomendomPluginów] utrudniona " + _cmd.getName());
			cmd = _cmd;
		}
		String perm = (plugin.getName() + "." + cmd.getName()).toLowerCase();
		if (pluginManager.getPermission(perm) == null)
			pluginManager.addPermission(new Permission(perm));
		cmd.setPermission(perm);
		if (cmd.getPermissionMessage() == null)
			cmd.setPermissionMessage("§cNie możesz tego użyć");
	}

}
