import java.io.*;
import java.net.*;
import java.time.*;

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
      BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
      BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
      DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
      outToServer.writeBytes(name + "\n");
      while (true) {
        System.out.print("Enter sentence: ");
        sentence = inFromUser.readLine();
        if (sentence == null || sentence.equalsIgnoreCase("quit")) {
          outToServer.writeBytes("quit\n");
          break;
        }
        outToServer.writeBytes(sentence + '\n');

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