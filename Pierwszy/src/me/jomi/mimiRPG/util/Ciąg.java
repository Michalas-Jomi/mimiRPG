package me.jomi.mimiRPG.util;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import me.jomi.mimiRPG.Mapowane;
import me.jomi.mimiRPG.Mapowany;

public class Ciąg<T> extends Mapowany {
	static final Random random = new Random();
	@Mapowane List<Krotka<Integer, T>> lista = Lists.newArrayList();
	@Mapowane int suma = 0;
	
	public Ciąg() {}
	public Ciąg(List<Krotka<Integer, T>> lista) {
		this.lista = lista;
		przetwórz();
	}
	void Init() {
		if (suma == 0)
			przetwórz();
	}
	
	private void przetwórz() {
		List<Krotka<Integer, T>> nowa = Lists.newArrayList(lista.iterator());
		this.lista = Lists.newArrayList();
		for (Krotka<Integer, T> krotka : nowa)
			dodaj(krotka.b, krotka.a);
	}

	public void wyczyść() {
		lista.clear();
		suma = 0;
	}
	public int wielkość() {
		return lista.size();
	}
	
	public List<T> klucze() {
		List<T> lista = Lists.newArrayList();
		
		this.lista.forEach(k -> lista.add(k.b));
		
		return lista;
	}
	
	public HashMap<T, Double> szanse() {
		HashMap<T, Double> mapa = new HashMap<>();
		
		lista.forEach(k -> mapa.put(k.b, mapa.getOrDefault(k.b, 0.0) + (k.a / ((double) suma))));
		
		return mapa;
	}
	
	public void dodaj(T co, int szansa) {
		lista.add(new Krotka<>(szansa + suma, co));
		suma += szansa;
	}

	public T znajdz(int numer) {
		return Func.wyszukajBinarnieP(numer, lista, k -> (double)k.a).b;
	}
	public T losuj() {
		return znajdz(random.nextInt(suma) + 1);
	}
}
