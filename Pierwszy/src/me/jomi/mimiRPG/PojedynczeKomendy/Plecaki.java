package me.jomi.mimiRPG.PojedynczeKomendy;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import net.minecraft.server.v1_16_R2.NBTBase;
import net.minecraft.server.v1_16_R2.NBTTagCompound;
import net.minecraft.server.v1_16_R2.NBTTagList;

import me.jomi.mimiRPG.Komenda;
import me.jomi.mimiRPG.Main;
import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;
import me.jomi.mimiRPG.util.Krotka;
import me.jomi.mimiRPG.util.Przeładowalny;

@Moduł
public class Plecaki extends Komenda implements Przeładowalny, Listener {
	final String premOtwieranie = Func.permisja("plecaki.dostęp");
	public Plecaki() {
		super("plecaki", "/plecaki <plecak>");
		Main.dodajPermisje(premOtwieranie);
	}

	public static final String prefix = Func.prefix("Plecaki");
	static final ItemStack zablokowanySlot = Func.stwórzItem(Material.BLACK_STAINED_GLASS_PANE, "&6Slot niedostępny");
	
	static class Holder extends Func.abstractHolder {
		final Plecak plecak;
		private Holder(Plecak plecak, int maxSlot) {
			super(Func.potrzebneRzędy(Math.max(plecak.sloty, maxSlot)), plecak.nazwa);
			for (int i=plecak.sloty; i < inv.getSize(); i++)
				inv.setItem(i, zablokowanySlot);
			this.plecak = plecak;
		}
		public Holder(NBTTagCompound tag) {
			this(Plecak.wczytaj(tag.getString("nazwa")), tag.getInt("maxSlot"));
			for (NBTBase nbt : (NBTTagList) tag.get("itemy"))
				inv.setItem(
						((NBTTagCompound) nbt).getInt("Slot"),
						CraftItemStack.asBukkitCopy(net.minecraft.server.v1_16_R2.ItemStack.a((NBTTagCompound) nbt))
						);
		}
	}
	
	static class Plecak {
		final int sloty;
		final String nazwa;
		final String id;
		public Plecak(int sloty, String nazwa, String id) {
			this.nazwa = Func.koloruj(nazwa);
			this.sloty = sloty;
			this.id = id;
		}

		private static final Plecak staryPlecak = new Plecak(0, "&6&lStary Plecak", "|");
		private static final HashMap<String, Plecak> mapa = new HashMap<>();
		public static Plecak wczytaj(String id) {
			return mapa.getOrDefault(id, staryPlecak);
		}
		public ItemStack item() {
			net.minecraft.server.v1_16_R2.ItemStack item = CraftItemStack.asNMSCopy(Func.stwórzItem(Material.CLAY_BALL, nazwa));
			NBTTagCompound nmsTag = item.getOrCreateTag();
			
			NBTTagCompound tag = new NBTTagCompound();
			tag.set("itemy", new NBTTagList());
			tag.setString("nazwa", id);
			tag.setInt("maxSlot", -1);
			
			nmsTag.set("plecak", tag);
			
			item.setTag(nmsTag);
			return CraftItemStack.asBukkitCopy(item);
		}
	}
	// {plecak: {nazwa: id, itemy:[{itemy}], mxSlot: int}}
	
	
	@EventHandler
	public void klikanieEq(InventoryClickEvent ev) {
		Func.wykonajDlaNieNull(ev.getInventory().getHolder(), Holder.class, holder -> {
			if (ev.getCurrentItem() != null && ev.getCurrentItem().isSimilar(zablokowanySlot))
				ev.setCancelled(true);
			else if (ev.getCurrentItem() != null && !CraftItemStack.asNMSCopy(ev.getCurrentItem()).getOrCreateTag().getCompound("plecak").isEmpty())
				ev.setCancelled(true);
			else if (Func.multiEquals(ev.getClick(), ClickType.DOUBLE_CLICK, ClickType.NUMBER_KEY, ClickType.SWAP_OFFHAND))
				ev.setCancelled(true);
		});
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public void zamykanieEq(InventoryCloseEvent ev) {
		Func.wykonajDlaNieNull(ev.getInventory().getHolder(), Holder.class, holder -> {
			net.minecraft.server.v1_16_R2.ItemStack nmsItem = CraftItemStack.asNMSCopy(ev.getPlayer().getInventory().getItemInMainHand());
			NBTTagCompound nmstag = nmsItem.getOrCreateTag();
			
			int maxSlot = -1;
			
			NBTTagCompound tag = new NBTTagCompound();
			tag.setString("nazwa", holder.plecak.id);
			
			NBTTagList itemy = new NBTTagList();
			for (int i=0; i < ev.getInventory().getSize(); i++) {
				ItemStack item = ev.getInventory().getItem(i);
				if (item == null || item.isSimilar(zablokowanySlot))
					continue;
				
				maxSlot = Math.max(maxSlot, i);
				
				NBTTagCompound itemTag = new NBTTagCompound();
				Func.wykonajDlaNieNull(CraftItemStack.asNMSCopy(item).getTag(), tagItemu -> itemTag.set("tag", tagItemu));
				itemTag.setString("id", "minecraft:" + item.getType().toString().toLowerCase());
				itemTag.setInt("Count", item.getAmount());
				itemTag.setInt("Slot", i);
				itemy.add(itemTag);
			}
			tag.set("itemy", itemy);
			
			tag.setInt("maxSlot", maxSlot);
			
			nmstag.set("plecak", tag);
			
			nmsItem.setTag(nmstag);
			ev.getPlayer().getInventory().setItemInMainHand(CraftItemStack.asBukkitCopy(nmsItem));
		});
	}

	@EventHandler
	public void użycie(PlayerInteractEvent ev) {
		if(ev.getPlayer().hasPermission(premOtwieranie) && Func.multiEquals(ev.getAction(), Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK)) {
			ItemStack item = ev.getPlayer().getInventory().getItemInMainHand();
			net.minecraft.server.v1_16_R2.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
			NBTTagCompound tag = nmsItem.getOrCreateTag().getCompound("plecak");
			if (tag == null || tag.isEmpty())
				return;
			ev.setCancelled(true);
			if (item.getAmount() == 1)
				ev.getPlayer().openInventory(new Holder(tag).getInventory());
		}
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return utab(args, Plecak.mapa.keySet());
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 1)
			return false;
		if (!Plecak.mapa.containsKey(args[0]))
			return Func.powiadom(sender, prefix + Func.msg("Niepoprawna nazwa plecaka %s", args[0]));
		ItemStack item = Plecak.wczytaj(args[0]).item();
		Func.dajItem((Player) sender, item);
		return true;
	}
	
	@Override
	public void przeładuj() {
		Plecak.mapa.clear();
		Func.wykonajDlaNieNull(Main.ust.sekcja("Plecaki"), sekcja ->
				sekcja.getKeys(false).forEach(id ->
				Plecak.mapa.put(id, new Plecak(sekcja.getInt(id + ".sloty", 5), sekcja.getString(id + ".nazwa", "&6&lPlecak"), id))));
	}

	@Override
	public Krotka<String, Object> raport() {
		return Func.r("Wczytane plecaki", Plecak.mapa.size());
	}
}
