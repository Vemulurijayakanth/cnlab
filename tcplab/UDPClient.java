import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class UDPClient {
    private static final String SERVER_IP = "127.0.0.1";  // Change if needed
    private static final int SERVER_PORT = 12345;
    private static int clientId = -1;  // Stores the assigned client ID

    public static void main(String[] args) {
        try (DatagramSocket clientSocket = new DatagramSocket()) {
            InetAddress serverAddress = InetAddress.getByName(SERVER_IP);

            // Thread to receive messages from the server
            Thread receiveThread = new Thread(() -> {
                byte[] receiveBuffer = new byte[1024];
                while (true) {
                    try {
                        DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                        clientSocket.receive(receivePacket);

                        String serverMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
                        System.out.println("\nMessage from Server: " + serverMessage);

                        // Extract and store Client ID from the first response
                        if (serverMessage.startsWith("Your Client ID is")) {
                            clientId = Integer.parseInt(serverMessage.replaceAll("[^0-9]", ""));
                            System.out.println("Assigned Client ID: " + clientId);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            receiveThread.start();

            // User input to send messages
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("Enter message to send to server (or 'exit' to quit): ");
                String message = scanner.nextLine();

                if (message.equalsIgnoreCase("exit")) {
                    byte[] sendBuffer = message.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, serverAddress, SERVER_PORT);
                    clientSocket.send(sendPacket);

                    System.out.println("Exiting...");
                    break;
                }

                byte[] sendBuffer = message.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, serverAddress, SERVER_PORT);
                clientSocket.send(sendPacket);
            }

            scanner.close();
            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
