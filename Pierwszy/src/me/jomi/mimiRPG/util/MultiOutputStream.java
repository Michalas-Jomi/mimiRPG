package me.jomi.mimiRPG.util;

import java.io.IOException;
import java.io.OutputStream;

public class MultiOutputStream extends OutputStream {
	OutputStream[] nasłuchujące;
	public MultiOutputStream(OutputStream... nasłuchujące) {
		this.nasłuchujące = nasłuchujące;
	}
	
	@Override
	public void write(int b) throws IOException {
		for (OutputStream out : nasłuchujące)
			out.write(b);
	}
	
	@Override
	public void flush() throws IOException {
		for (OutputStream out : nasłuchujące)
			out.flush();
	}
	@Override
	public void close() throws IOException {
		for (OutputStream out : nasłuchujące)
			out.close();
	}
}
