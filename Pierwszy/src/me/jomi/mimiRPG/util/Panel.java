package me.jomi.mimiRPG.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.jomi.mimiRPG.Main;

public class Panel {
	public static class PanelListener implements Listener {
		private static List<WeakReference<Panel>> odwołania;
		@EventHandler public void open(InventoryOpenEvent ev)   { wykonaj(p -> p.open,  ev); }
		@EventHandler public void close(InventoryCloseEvent ev) { wykonaj(p -> p.close, ev); }
		@EventHandler public void click(InventoryClickEvent ev) {
			if (ev.getRawSlot() >= 0 && ev.getRawSlot() < ev.getInventory().getSize())
				wykonaj(p -> p.click, ev);
		}
		
		private <T extends InventoryEvent> void wykonaj(Function<Panel, Consumer<T>> getter, T ev) {
			for (int i=0; i < odwołania.size(); i++) {
				Panel panel = odwołania.get(i).get();
				if (panel == null)
					odwołania.remove(i--);
				else if (panel.jestPanelem(ev.getInventory()))
					Func.wykonajDlaNieNull(getter.apply(panel), cons -> cons.accept(ev));
			}
		}
	}
	
	private static int panelIdLicznik = 0;
	private int panelId = panelIdLicznik++;
	public class Holder extends Func.abstractHolder {
		public Object dane;
		
		private int Id = panelId;
		
		public Holder(Object dane, int rzędy, String nazwa) {
			this(dane, rzędy, nazwa, null);
		}
		public Holder(Object dane, int rzędy, String nazwa, ItemStack pustySlot) {
			super(rzędy, nazwa);
			
			this.dane = dane;
			
			if (pustySlot != null)
				Func.ustawPuste(inv, pustySlot);
			else if (blokujKlikanie)
				Func.ustawPuste(inv);
		}
		
		public <T> T pewneDane() {
			return Func.pewnyCast(dane);
		}
	}
	
	public boolean blokujKlikanie = false;
	
	private Consumer<InventoryOpenEvent> open;
	private Consumer<InventoryCloseEvent> close;
	private Consumer<InventoryClickEvent> click;
	
	
	public Panel(boolean blokujKlikanie) {
		if (PanelListener.odwołania == null) {
			PanelListener.odwołania = new ArrayList<>();
			Main.zarejestruj(new PanelListener());
		}
		
		this.blokujKlikanie = blokujKlikanie;
		PanelListener.odwołania.add(new WeakReference<>(this));
		
		ustawClick(null);
	}

	public boolean jestPanelem(Inventory inv) {
		return inv.getHolder() != null && inv.getHolder() instanceof Holder && ((Holder) inv.getHolder()).Id == panelId;
	}
	
	public Inventory stwórz(Object dane, int rzędy, String nazwa) {
		return stwórz(dane, rzędy, nazwa, null);
	}
	public Inventory stwórz(Object dane, int rzędy, String nazwa, ItemStack pustySlot) {
		return new Holder(dane, rzędy, nazwa, pustySlot).getInventory();
	}
	
	public void ustawOpen(Consumer<InventoryOpenEvent> open) {
		this.open = open;
	}
	public void ustawClose(Consumer<InventoryCloseEvent> close) {
		this.close = close;
	}
	public void ustawClick(Consumer<InventoryClickEvent> click) {
		this.click = ev -> {
			if (blokujKlikanie)
				ev.setCancelled(true);
			if (click != null)
				click.accept(ev);
		};
	}
}
