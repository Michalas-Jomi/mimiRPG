package me.jomi.mimiRPG;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value=ElementType.TYPE)
@Retention(value=RetentionPolicy.RUNTIME)
public @interface Moduł {
	public Priorytet priorytet() default Priorytet.NORMALNY;
	public enum Priorytet {
		NAJWYŻSZY(0),
		WYSOKI(1),
		NORMALNY(2),
		NISKI(3),
		NAJNIŻSZY(4);

		int poziom;
		Priorytet(int poziom) {
			this.poziom = poziom;
		}
	}
}
