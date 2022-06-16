package me.jomi.mimiRPG.bungee;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Connection {
    static Thread thread;
    static ServerSocket socket;

    protected static void start(int port) {
        thread = new Thread(() -> {
            try {
                socket = new ServerSocket(port);
            } catch (IOException e) {
                e.printStackTrace();
            }

            while (true) {
                Socket client = null;
                try {
                    client = socket.accept();
                    DataInputStream in = new DataInputStream(client.getInputStream());
                    DataOutputStream out = new DataOutputStream(client.getOutputStream());
                    new ClientHandler(client, in, out).start();
                } catch (Exception exception) {
                    try {
                        client.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    exception.printStackTrace();
                }
            }
        });
        thread.start();
    }
}
