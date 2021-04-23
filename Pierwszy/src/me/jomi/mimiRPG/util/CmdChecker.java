package me.jomi.mimiRPG.util;

import me.jomi.mimiRPG.util.Komenda.MsgCmdError;

public class CmdChecker {
	final Komenda komenda;
	
	public CmdChecker(Komenda komenda) {
		this.komenda = komenda;
	}
	
	
	public void check(boolean warunek, String lokalizacja, Object... args) throws MsgCmdError {
		if (!warunek)
			komenda.throwMsg(lokalizacja, args);
	}
	public void checkFormat(boolean warunek, String format, Object... uzupełnienia) throws MsgCmdError {
		if (!warunek)
			komenda.throwFormatMsg(format, uzupełnienia);
	}
	
	public <T> T nieNull(T obj, String lokalizacja, Object... args) throws MsgCmdError {
		if (obj == null)
			komenda.throwMsg(lokalizacja, args);
		return obj;
	}
	public <T> T nieNullFormat(T obj, String format, Object... uzupełnienia) throws MsgCmdError {
		if (obj == null)
			komenda.throwFormatMsg(format, uzupełnienia);
		return obj;
	}
}
