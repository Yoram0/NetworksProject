import java.io.*;
import java.net.*;
import java.io.IOException;

class TCPServer {

  public static void main(String argv[]) throws Exception
    {
      String clientSentence;
      String capitalizedSentence;
      int clientCount = 0;

      ServerSocket welcomeSocket = new ServerSocket(6789);

      try {

            System.out.println("Server is UP and running!");
            while(true) 
            {
            Socket connectionSocket = welcomeSocket.accept(); //listens for connections from clients
            System.out.println("Conntected to client: " + clientCount++);
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

           DataOutputStream  outToClient =
             new DataOutputStream(connectionSocket.getOutputStream());

           clientSentence = inFromClient.readLine();

            System.out.println("Server received message!: " + clientSentence);
           capitalizedSentence = clientSentence.toUpperCase() + '\n';

           outToClient.writeBytes(capitalizedSentence);
            }
        } 
        catch (IOException e) 
        {
          e.printStackTrace();
        }
    }
}