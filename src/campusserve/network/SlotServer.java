package campusserve.network;

import campusserve.dao.ShiftDAO;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * SOCKET SERVER. Listens on port 5000. Every client gets its own thread (ClientHandler is a Runnable).
 * Commands:  SLOTS <shiftId>   |   BYE
 */
public class SlotServer implements Runnable {
    public static final int PORT = 5000;

    @Override
    public void run() {
        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("[Socket] Slot server listening on port " + PORT);
            while (true) {
                Socket client = server.accept();                 // waits for a client
                new Thread(new ClientHandler(client)).start();   // one thread per client
            }
        } catch (IOException e) {
            System.out.println("[Socket] Server stopped: " + e.getMessage());
        }
    }

    /** Static nested class that serves ONE client. */
    private static class ClientHandler implements Runnable {
        private final Socket socket;
        ClientHandler(Socket socket) { this.socket = socket; }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                String line = in.readLine();
                System.out.println("[Socket] Received: " + line);
                out.println(answer(line));
            } catch (IOException e) {
                System.out.println("[Socket] Client error: " + e.getMessage());
            } finally {
                try { socket.close(); } catch (IOException e) { }
            }
        }

        private String answer(String command) {
            if (command == null) return "Empty command.";
            String[] parts = command.trim().split(" ");
            try {
                if (parts[0].equalsIgnoreCase("SLOTS") && parts.length == 2) {
                    int shiftId = Integer.parseInt(parts[1]);
                    int open = new ShiftDAO().getOpenSlots(shiftId);
                    return open < 0 ? "Shift " + shiftId + " not found." : "Shift " + shiftId + " has " + open + " open slot(s).";
                }
                return "Unknown command. Use: SLOTS <shiftId>";
            } catch (NumberFormatException e) {
                return "Shift id must be a number.";
            } catch (Exception e) {
                return "Server error: " + e.getMessage();
            }
        }
    }
}
