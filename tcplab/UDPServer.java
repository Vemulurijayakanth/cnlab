import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

public class UDPServer {
    private static final int PORT = 12345;
    private static int clientCounter = 1;
    private static Map<Integer, InetAddress> clientAddresses = new HashMap<>();
    private static Map<Integer, Integer> clientPorts = new HashMap<>();

    public static void main(String[] args) {
        try (DatagramSocket serverSocket = new DatagramSocket(PORT)) {
            System.out.println("UDP Server is running on port " + PORT);

            // Thread to receive messages from clients
            Thread receiveThread = new Thread(() -> {
                byte[] receiveBuffer = new byte[1024];
                while (true) {
                    try {
                        DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                        serverSocket.receive(receivePacket);

                        String clientMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
                        InetAddress clientAddress = receivePacket.getAddress();
                        int clientPort = receivePacket.getPort();

                        // Check if the client is new
                        int clientId = getClientId(clientAddress, clientPort);
                        if (!clientAddresses.containsKey(clientId)) {
                            clientAddresses.put(clientId, clientAddress);
                            clientPorts.put(clientId, clientPort);
                            System.out.println("New client registered: ID = " + clientId);
                        }

                        // Handle client exit
                        if (clientMessage.equalsIgnoreCase("exit")) {
                            System.out.println("Client ID " + clientId + " disconnected.");
                            clientAddresses.remove(clientId);
                            clientPorts.remove(clientId);
                            continue;
                        }

                        System.out.println("Received from Client ID " + clientId + ": " + clientMessage);

                        // Send acknowledgment with Client ID
                        String response = "Your Client ID is " + clientId;
                        byte[] sendBuffer = response.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, clientAddress, clientPort);
                        serverSocket.send(sendPacket);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            receiveThread.start(); // Start listening for client messages

            // Admin input thread to send messages to specific clients
            Scanner scanner = new Scanner(System.in);
            while (true) {
                // Remove disconnected clients from the list
                cleanDisconnectedClients();

                System.out.println("\nConnected Clients:");
                for (int clientId : clientAddresses.keySet()) {
                    System.out.println("Client ID: " + clientId);
                }

                System.out.println("Enter Client ID to send a message (or 'exit' to stop): ");
                String input = scanner.nextLine();

                if (input.equalsIgnoreCase("exit")) {
                    break;
                }

                int clientId;
                try {
                    clientId = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid ID! Try again.");
                    continue;
                }

                if (!clientAddresses.containsKey(clientId)) {
                    System.out.println("Client ID not found! Try again.");
                    continue;
                }

                System.out.println("Enter message for Client ID " + clientId + ": ");
                String message = scanner.nextLine();

                InetAddress clientAddress = clientAddresses.get(clientId);
                int clientPort = clientPorts.get(clientId);

                byte[] sendBuffer = message.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, clientAddress, clientPort);
                serverSocket.send(sendPacket);

                System.out.println("Sent to Client ID " + clientId + ": " + message);
            }

            scanner.close();
            serverSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int getClientId(InetAddress address, int port) {
        for (Map.Entry<Integer, InetAddress> entry : clientAddresses.entrySet()) {
            if (entry.getValue().equals(address) && clientPorts.get(entry.getKey()) == port) {
                return entry.getKey();
            }
        }
        return clientCounter++; // Assign a new ID if not found
    }

    private static void cleanDisconnectedClients() {
        Iterator<Integer> iterator = clientAddresses.keySet().iterator();
        while (iterator.hasNext()) {
            int clientId = iterator.next();
            if (!clientPorts.containsKey(clientId)) {
                iterator.remove();
                System.out.println("Client ID " + clientId + " removed from list.");
            }
        }
    }
}
