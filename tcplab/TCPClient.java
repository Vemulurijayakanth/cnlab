import java.io.*;
import java.net.*;

public class TCPClient {
    public static void main(String[] args) {
        String serverAddress = "localhost";
        int port = 5000;

        try (Socket socket = new Socket(serverAddress, port);
             BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
             BufferedReader serverInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true)) {

            System.out.println("Connected to server. Type messages:");

            // Listening for messages from the server
            new Thread(() -> {
                try {
                    String message;
                    while ((message = serverInput.readLine()) != null) {
                        System.out.println(message);
                    }
                } catch (IOException e) {
                    System.out.println("Connection closed.");
                }
            }).start();

            // Sending messages to the server
            String userMessage;
            while ((userMessage = userInput.readLine()) != null) {
                output.println(userMessage);
                if ("exit".equalsIgnoreCase(userMessage)) break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
