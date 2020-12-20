package me.jomi.mimiRPG.MineZ;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
public class SkinyItemków extends Komenda implements Listener, Przeładowalny {
	public static class Holder extends Func.abstractHolder {
		public Holder(int rzędy) {
			super(rzędy, "&4&lSkiny");
			Func.ustawPuste(inv);
		}
	}
	
	// typ itemku: ilość skinów
	static final HashMap<Material, Integer> mapaIlościSkinów = new HashMap<>();

	
	public SkinyItemków() {
		super("skinyitemków", null, "skiny");
	}
	
	
	// EventHandler
	
	@EventHandler
	public void klikanie(InventoryClickEvent ev) {
		Func.wykonajDlaNieNull(ev.getInventory().getHolder(), Holder.class, holder -> {
			ev.setCancelled(true);
			
			int slot = ev.getRawSlot();
			if (slot < 0)
				return;
			
			ItemStack item = ev.getWhoClicked().getInventory().getItemInMainHand();
			
			int ile = mapaIlościSkinów.getOrDefault(item, 0);

			
			if (slot < ile || posiada((Player) ev.getWhoClicked(), item.getType(), slot))
				ev.getWhoClicked().getInventory().setItemInMainHand(zmień(item, slot));
		});
	}
	
	
	@EventHandler(priority = EventPriority.HIGH)
	public void wyrzucanie(PlayerDropItemEvent ev) {
		if (mapaIlościSkinów.containsKey(ev.getItemDrop().getItemStack().getType()))
			ev.getItemDrop().setItemStack(Func.customModelData(ev.getItemDrop().getItemStack(), 0));
	}
	@EventHandler(priority = EventPriority.LOW)
	public void śmierć(PlayerDeathEvent ev) {
		ev.getDrops().forEach(item -> {
			if (mapaIlościSkinów.containsKey(item.getType()))
				Func.customModelData(item, 0);
		});
	}
	@EventHandler(priority = EventPriority.HIGH)
	public void klikanie2(InventoryClickEvent ev) {
		if (!ev.isCancelled())
			Func.wykonajDlaNieNull(ev.getCurrentItem(), item -> {
				if (item.getItemMeta().hasCustomModelData() &&
						mapaIlościSkinów.containsKey(item.getType()) &&
						!posiada((Player) ev.getWhoClicked(), item.getType(), item.getItemMeta().getCustomModelData()))
					ev.setCurrentItem(Func.customModelData(item, 0));
			});
	}
	
	
	// util
	
	boolean posiada(Player p, Material mat, int id) {
		return id == 0 || p.hasPermission(String.format("mimirpg.skinyitemków.%s.%s", mat.toString().toLowerCase(), id));
	}
	ItemStack zmień(ItemStack item, int id) {
		return Func.customModelData(item, id);
	}
	
	void otwórzPanel(Player p) {
		p.openInventory(dajPanel(p));
		p.addScoreboardTag(Main.tagBlokWyciąganiaZEq); // TODO blokować własnoręcznie
	}
	Inventory dajPanel(Player p) {
		ItemStack item = p.getInventory().getItemInMainHand();
		int ile = mapaIlościSkinów.get(item.getType());
		Inventory inv = new Holder(Func.potrzebneRzędy(ile)).getInventory();
		for (int i = 0; i < ile; i++)
			inv.setItem(i, posiada(p, item.getType(), i) ? Func.customModelData(item.clone(), i) : Func.stwórzItem(Material.BARRIER, "&4&lSkin Nieodblokowany"));
		return inv;
	}
	
	
	// Override
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player))
			return Func.powiadom(sender, "Dostępne tylko dla Graczy");
		otwórzPanel((Player) sender);
		return true;
	}


	
	@Override
	public void przeładuj() {
		ConfigurationSection sekcja = Main.ust.sekcja("Skiny itemków");
		sekcja.getValues(false).entrySet().forEach(entry -> {
			try {
				int ile = (int) entry.getValue();
				Material mat = Material.valueOf(entry.getKey().toUpperCase().replace(" ", "_"));
				mapaIlościSkinów.put(mat, ile);
			} catch (Throwable e) {
				Main.warn("Nieprawidłowość w ustawienia.yml Skiny itemków." + entry.getKey() + ": " + entry.getValue() + ", powinno być \"<Typ itemku>: <ilość>\" (np. diamond sword: 3)");
			}
		});
	}
	@Override
	public Krotka<String, Object> raport() {
		int ile = 0;
		for (int skiny :mapaIlościSkinów.values())
			ile += skiny - 1;
		return Func.r("Skiny premium", ile);
	}
}
