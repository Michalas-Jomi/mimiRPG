package me.jomi.mimiRPG.Minigry;

import me.jomi.mimiRPG.Moduł;
import me.jomi.mimiRPG.util.Func;

@Moduł
public class BedWarsAlaZigaEdytor/* extends Komenda implements Listener TODO odnowić */{
	public static final String prefix = Func.prefix("BedWars Ala Ziga &5Edytor");
	/*
	public BedWarsAlaZigaEdytor() {
		super("edytujBedWarsAlaZiga", "/edytujBedWarsAlaZiga <czynność> <arena>");
	}
	
	class Box {
		String nazwa;
		PanelStronny panel;
		BedWarsAlaZiga.Arena arena;
		
		// położenie [kolumna] = (strona.index(), strona.item.idex())
		List<Krotka<Integer, Integer>> położenie = Lists.newArrayList();
	}
	
	// box.nazwa : nazwa
	final HashMap<String, Box> mapa = new HashMap<>();
	
	void otwórzPanelEdycjiItemów(Player p, String nazwa) {
		Box box = mapa.get(nazwa);
		if (box == null) {
			BedWarsAlaZiga.Arena arena = (Arena) BedWarsAlaZiga.getconfigAreny.wczytaj(nazwa);
			if (arena != null)
				box = nowyPanelEdycjiItemów(nazwa, arena);
			else {
				p.sendMessage(prefix + Func.msg("Arena %s nie istnieje", nazwa));
				return;
			}
		}
		box.panel.otwórzPanel(p);
		
	}
	final String nazwaPaneluItemów = Func.koloruj("&4&lEdytor BedWarsAlaZiga &9&lItemki &2&lCeny &8&l");
	// Nie sprawdza syndaxu config.wczytaj(nazwa) != null
	private Box nowyPanelEdycjiItemów(String nazwa, BedWarsAlaZiga.Arena arena) {
		Box box = new Box();
		
		box.nazwa = nazwa;
		
		box.panel = new PanelStronny(5, nazwaPaneluItemów + nazwa);
		
		int idStrona = -1;
		for (BedWarsAlaZiga.SklepItemStrona strona : arena.itemyDoKupienia) {
			idStrona++;
			int idSitem = -1;
			for (BedWarsAlaZiga.SklepItem sitem : strona.itemy) {
				idSitem++;
				
				if (sitem.zmieńStrone == -1) 
					continue;
				
				if (sitem.cena.size() > 4)
					continue;
				
				int i=0;
				box.panel.ustawItem(i++, box.położenie.size(), sitem.item);
				for (ItemStack item : sitem.cena)
					box.panel.ustawItem(i++, box.położenie.size(), item);
				
				box.położenie.add(new Krotka<>(idStrona, idSitem));
				
			}
		}
		
		return box;
	}
	void zapiszOferty(Box box) {
		int i=-1;
		List<Krotka<Integer, Integer>> doUsunięcia = Lists.newArrayList();
		for (Krotka<Integer, Integer> położenie : box.położenie) {
			i++;
			BedWarsAlaZiga.SklepItem sitem = box.arena.itemyDoKupienia.get(położenie.a).itemy.get(położenie.b);
			sitem.item = box.panel.wezItem(0, i);
			sitem.cena.clear();
			if (sitem.item == null)
				doUsunięcia.add(położenie);
			else
				for (int j=1; j<6; j++)
					Func.wykonajDlaNieNull(box.panel.wezItem(j, i), sitem.cena::add);
		}
		for (Krotka<Integer, Integer> położenie : Lists.reverse(doUsunięcia))
			box.arena.itemyDoKupienia.get(położenie.a).itemy.remove((int) położenie.b);
		BedWarsAlaZiga.configAreny.ustaw_zapisz(box.nazwa, box.arena);
	}
	
	
	
	@EventHandler
	public void zamykanieEq(InventoryCloseEvent ev) {
		if (ev.getView().getTitle().startsWith(nazwaPaneluItemów))
			Func.wykonajDlaNieNull(mapa.get(ev.getView().getTitle().substring(nazwaPaneluItemów.length())), this::zapiszOferty);
	}
	@EventHandler
	public void klikanieEq(InventoryClickEvent ev) {
		if (ev.getView().getTitle().startsWith(nazwaPaneluItemów))
			Func.wykonajDlaNieNull(mapa.get(ev.getView().getTitle().substring(nazwaPaneluItemów.length())), box -> box.panel.clickEvent(ev));	
	}
	
	
	
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length <= 1)
			return utab(args, "itemyCeny");
		if (args.length == 2)
			return utab(args, BedWarsAlaZiga.configAreny.klucze(false));
		return null;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player))
			return Func.powiadom(sender, "Tylko gracz może tego użyć");
		
		Player p = (Player) sender;
		
		if (args.length < 2)
			return false;
		
		switch (args[0].toLowerCase()) {
		case "itemyCeny":
			otwórzPanelEdycjiItemów(p, args[1]);
			break;
		}
		
		return true;
	}
*/}
