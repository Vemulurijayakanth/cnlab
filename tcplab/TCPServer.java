import java.io.*;
import java.net.*;
import java.util.*;

public class TCPServer {
    private static final int PORT = 5000;
    private static final Map<Integer, ClientHandler> clients = new HashMap<>();
    private static int clientIdCounter = 1;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);

            // Thread to accept new clients
            new Thread(() -> {
                while (true) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        int clientId = clientIdCounter++;
                        ClientHandler clientHandler = new ClientHandler(clientSocket, clientId);
                        clients.put(clientId, clientHandler);
                        new Thread(clientHandler).start();
                        System.out.println("Client " + clientId + " connected.");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            // Server admin console for messaging clients
            try (BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in))) {
                while (true) {
                    System.out.println("Enter client ID to send a message (or 'exit' to stop server):");
                    String input = consoleInput.readLine();
                    if ("exit".equalsIgnoreCase(input)) break;

                    try {
                        int clientId = Integer.parseInt(input);
                        ClientHandler client = clients.get(clientId);
                        if (client != null) {
                            System.out.println("Enter message for client " + clientId + ":");
                            client.sendMessage(consoleInput.readLine());
                        } else {
                            System.out.println("Invalid client ID.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Please enter a valid client ID.");
                    }
                }
            }

            System.out.println("Server shutting down.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Handles client communication
    static class ClientHandler implements Runnable {
        private final Socket socket;
        private final int clientId;
        private final BufferedReader input;
        private final PrintWriter output;

        public ClientHandler(Socket socket, int clientId) throws IOException {
            this.socket = socket;
            this.clientId = clientId;
            this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.output = new PrintWriter(socket.getOutputStream(), true);
        }

        @Override
        public void run() {
            try {
                String message;
                while ((message = input.readLine()) != null) {
                    System.out.println("Client " + clientId + ": " + message);
                    output.println("Server: Received - " + message);
                }
            } catch (IOException e) {
                System.out.println("Client " + clientId + " disconnected.");
            } finally {
                closeConnection();
            }
        }

        public void sendMessage(String message) {
            output.println("Server: " + message);
        }

        private void closeConnection() {
            try {
                socket.close();
                clients.remove(clientId);
                System.out.println("Client " + clientId + " removed.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
