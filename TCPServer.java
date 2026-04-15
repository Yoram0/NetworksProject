import java.io.*;
import java.net.*;

class TCPServer {

  public static void main(String argv[]) throws Exception {
    
    int clientCount = 0;

    ServerSocket welcomeSocket = new ServerSocket(6789);

    try {
      System.out.println("Server is UP and running!");

      while (true) {
        Socket connectionSocket = welcomeSocket.accept();
        System.out.println("Connected to client: " + clientCount++);

        // Spawn a new thread for each client so multiple can connect at once
        new Thread(() -> {
          try {
            String clientSentence;
            String capitalizedSentence;
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

            while (true) {
              clientSentence = inFromClient.readLine();

              if (clientSentence == null || clientSentence.equalsIgnoreCase("quit")) {
                break;
              }

              System.out.println("Server received message: " + clientSentence);
              capitalizedSentence = clientSentence.toUpperCase() + '\n';
              outToClient.writeBytes(capitalizedSentence);
            }
            connectionSocket.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }).start();
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      welcomeSocket.close(); // always close the welcome socket on exit
    }
  }
}