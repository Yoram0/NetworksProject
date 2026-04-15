import java.io.*;
import java.net.*;
import java.time.*;

class TCPServer {
  static int clientCount = 0;

  public static void main(String argv[]) throws Exception {

    ServerSocket welcomeSocket = new ServerSocket(6789);

    try {

      System.out.println("Server is UP and running!");
      System.out.println("Connected clients: " + clientCount);
      while (true) {
        Socket connectionSocket = welcomeSocket.accept();
        clientCount++;
        // Spawn a new thread for each client so multiple can connect at once
        new Thread(() -> {
          String clientName = "";
          String clientIP;
          int clientPort;
          try {
            String clientSentence;
            String capitalizedSentence;
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
            clientName = inFromClient.readLine();
            clientIP = connectionSocket.getInetAddress().getHostAddress();
            clientPort = connectionSocket.getPort();
            System.out.println("Client '" + clientName + "' connected at " + LocalDateTime.now());
            System.out.println("Client IP:" + clientIP + " Port: " + clientPort);
            System.out.println("Connected clients: " + clientCount);
            while (true) {
              clientSentence = inFromClient.readLine();

              if (clientSentence == null || clientSentence.equalsIgnoreCase("quit")) {
                System.out.println("Client '" + clientName + "' disconnected at " + LocalDateTime.now());
                break;
              }

              System.out.println("Server received message from '" + clientName + "': " + clientSentence);
              capitalizedSentence = clientSentence.toUpperCase() + '\n';
              outToClient.writeBytes(capitalizedSentence);
            }
          } catch (SocketException e) {
            System.out.println("Client '" + clientName + "' abruptly lost connection at " + LocalDateTime.now());
          } catch (IOException e) {
            e.printStackTrace();
          } finally {
            clientCount--;
            try {
              connectionSocket.close();
            } catch (IOException e) {
            }
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