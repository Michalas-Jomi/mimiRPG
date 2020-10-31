package me.jomi.mimiRPG.util;

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
	
	void przetwórz() {
		List<Krotka<Integer, T>> nowa = Lists.newArrayList();
		for (Krotka<Integer, T> krotka : lista)
			dodaj(krotka.b, krotka.a);
		this.lista = nowa;
	}

	public void wyczyść() {
		lista.clear();
		suma = 0;
	}
	public int wielkość() {
		return lista.size();
	}
	
	
	public void dodaj(T co, int szansa) {
		lista.add(new Krotka<>(szansa + suma, co));
		suma += szansa;
	}

	public T znajdz(int numer) {
		int l = 0;
		int r = lista.size() - 1;
		
		while (l < r) {
			int s = l + ((r - l) / 2);
			int w = lista.get(s).a;
			
			if (w < numer)
				l = s + 1;
			else
				r = s;
		}
		return lista.get(l).b;
	}
	public T losuj() {
		return znajdz(random.nextInt(suma) + 1);
	}
	
}
