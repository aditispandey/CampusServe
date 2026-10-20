package campusserve.network;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

/** SOCKET CLIENT used by the volunteer dashboard ("Check Slots on Server" button). */
public final class SlotClient {
    private SlotClient() { }

    public static String ask(String command) throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("localhost", SlotServer.PORT), 2000);   // 2 s to connect
            socket.setSoTimeout(3000);                                                    // 3 s to answer
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println(command);
            return in.readLine();
        }
    }
}
