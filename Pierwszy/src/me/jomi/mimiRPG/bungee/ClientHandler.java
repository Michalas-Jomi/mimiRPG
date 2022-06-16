package me.jomi.mimiRPG.bungee;

import com.google.common.collect.Queues;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Queue;
import java.util.function.Consumer;

public class ClientHandler extends Thread {
    private final DataInputStream in;
    private final DataOutputStream out;
    private final Socket s;

    private String idSocket;

    public static HashMap<String, Socket> sockets = new HashMap<>();


    public ClientHandler(Socket s, DataInputStream in, DataOutputStream out) {
        this.s = s;
        this.in = in;
        this.out = out;
    }

    @Override
    public void run() {
        String received;
        while (true) {
            try {
                received = in.readUTF();
                try {
                    ClientHandler.class.getDeclaredMethod(received, DataInputStream.class).invoke(this, in);
                } catch (NoSuchMethodException ex) {
                    System.err.println("Nieznany Kanał: " + received);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            } catch (SocketException e) {
                System.out.println("Bungee zgubilo lacznosc z socketem " + idSocket);
                synchronized (sockets) {
                    sockets.remove(idSocket);
                }
                break;
            } catch (Throwable e) {
                e.printStackTrace();
                break;
            }
        }
    }

    // kanały
    void forward(DataInputStream in) throws IOException {
        String socketName = in.readUTF();
        String channel = in.readUTF();

        int len = in.readShort();
        byte[] data = new byte[len];
        in.readFully(data);

        send(_out -> {
            Consumer<Socket> send = socketToForward -> {
                try {
                    DataOutputStream out = new DataOutputStream(socketToForward.getOutputStream());

                    out.writeUTF(channel);

                    out.writeShort(len);
                    out.write(data);

                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };

            if (socketName.equals("ALL"))
                synchronized (sockets) {
                    sockets.values().forEach(send::accept);
                }
            else {
                Socket socket = sockets.get(socketName);
                if (socket != null)
                    send.accept(socket);
                else
                    System.out.println("Wysłano pakiet na nieistniejący serwer: " + socketName);
            }
        });
    }

    void id(DataInputStream in) throws IOException {
        idSocket = in.readUTF();
        synchronized (sockets) {
            sockets.put(idSocket, s);
        }
    }


    private static volatile Queue<Consumer<OutputStream>> sendQueue = Queues.newConcurrentLinkedQueue();
    private static volatile boolean canSend = true;

    private void send(Consumer<OutputStream> cons) throws IOException {
        synchronized (sendQueue) {
            sendQueue.add(cons);
            if (canSend) {
                canSend = false;
                while (!sendQueue.isEmpty())
                    sendQueue.remove().accept(s.getOutputStream());
                canSend = true;
            }
        }
    }
}