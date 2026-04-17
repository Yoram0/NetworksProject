import java.io.*;
import java.net.*;
import java.time.*;
import java.util.Random;

class TCPClient {

  public static void main(String argv[]) throws Exception {
    String sentence;
    String modifiedSentence;

    // Allow IP and port to be passed as arguments, default to localhost:6789
    String name = (argv.length > 0) ? argv[0] : "user";
    String host = (argv.length > 1) ? argv[1] : "127.0.0.1";
    int port = (argv.length > 2) ? Integer.parseInt(argv[2]) : 6789;

    System.out.println("Client is running:");

    try (Socket clientSocket = new Socket(host, port)) {

      // Input from user and server
      BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
      BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
      DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());

      Random rand = new Random();

      // Send client name to server
      outToServer.writeBytes(name + "\n");

      // Wait for server acknowledgement
      modifiedSentence = inFromServer.readLine();
      if (modifiedSentence == null) {
        System.out.println("Server closed the connection.");
        return;
      }
      System.out.println("FROM SERVER: " + modifiedSentence);

      // ----------- Send 3 random math requests -----------
      String[] sampleRequests = {
        "1+2",
        "10-3",
        "4*5",
        "20/4",
        "7+8"
      };

      for (int i = 0; i < 3; i++) {
        String request = sampleRequests[rand.nextInt(sampleRequests.length)];

        System.out.println("AUTO Sending: " + request);
        outToServer.writeBytes(request + "\n");

        // Receive result from server
        modifiedSentence = inFromServer.readLine();
        if (modifiedSentence == null) {
          System.out.println("Server closed the connection.");
          break;
        }
        System.out.println("FROM SERVER: " + modifiedSentence);

        // Random delay between requests (1–3 seconds)
        int delay = 1000 + rand.nextInt(2000);
        Thread.sleep(delay);
      }
      // --------------------------------------------------

      // Manual mode after automatic requests
      while (true) {
        System.out.print("Enter sentence: ");
        sentence = inFromUser.readLine();

        // Send quit request to close connection
        if (sentence == null || sentence.equalsIgnoreCase("quit")) {
          outToServer.writeBytes("quit\n");
          break;
        }

        outToServer.writeBytes(sentence + '\n');

        // Receive response from server
        modifiedSentence = inFromServer.readLine();
        if (modifiedSentence == null) {
          System.out.println("Server closed the connection.");
          break;
        }

        System.out.println("FROM SERVER: " + modifiedSentence);
      }

      clientSocket.close();
      System.out.println("Connection closed.");

    } catch (ConnectException e) {
      System.out.println("Could not connect to server at " + host + ":" + port + ".");
    } catch (SocketException e) {
      System.out.println("Lost connection to server. Message not recieved.");
    }
  }
}